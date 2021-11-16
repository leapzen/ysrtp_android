package ysrtp.party.app.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.BuildConfig;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.common.model.RedirectLogin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Created by mahesh on 26/3/18.
 */

public class ServerRequest  {

    private Context context;
    private ResponseModel serverResponse;
    private RequestModel requestModelTemp;
    private ServerResponseListener serverResponseListenerTemp;
    private int requestTagTemp;
    private boolean noConnectionError;
    private FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();


    public ServerRequest(Context context) {
        serverResponse = new ResponseModel();
        this.context = context;
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onNetworkConnectionChanged(NetworkConnection networkConnection) {
        if(networkConnection.isNetworkConnected() && noConnectionError){
            VolleyInstance.clearQueue();
            sendRequest(requestModelTemp,serverResponseListenerTemp,requestTagTemp);
        }
    }

    public void sendRequest(final RequestModel requestModel, final ServerResponseListener serverResponseListener,
                            final int requestTag) {
//        Log.e("sendRequest: ", "URL:- " + requestModel.getURL());
        requestModelTemp = requestModel;
        serverResponseListenerTemp = serverResponseListener;
        requestTagTemp = requestTag;


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (requestModel.getRequestType(), requestModel.getURL(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.e("onResponse: ", "Response:- "+response);
                        noConnectionError = false;
                        serverResponse.setPayload(response.toString());
                        serverResponse.setSuccess(true);
                        serverResponseListener.getResponse(serverResponse,requestTag);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.e("onErrorResponse: ", "VolleyError:- "+error);

                        serverResponse.setSuccess(false);
//                            NetworkResponse networkResponse = error.networkResponse;
                        if (error instanceof NoConnectionError) {
                            serverResponse.setPayload(firebaseRemoteConfig.getString("no_connection_error"));
                            if(Connectivity.isConnected(context)){
                                serverResponse.setPayload(firebaseRemoteConfig.getString("allow_network_access"));
                            }
                            serverResponse.setStatusCode(100);
                            noConnectionError = true;
                        } else if (error instanceof AuthFailureError) {
                            serverResponse.setPayload(firebaseRemoteConfig.getString("session_expired"));
                            serverResponse.setStatusCode(99);
                            noConnectionError = false;
                            EventBus.getDefault().post(new RedirectLogin(true));
                        }else if( error instanceof ServerError) {
                            serverResponse.setPayload(firebaseRemoteConfig.getString("server_error"));
                            serverResponse.setStatusCode(98);
                            noConnectionError = false;
                        }else if (error instanceof NetworkError) {
                            serverResponse.setPayload(firebaseRemoteConfig.getString("network_error"));
                            serverResponse.setStatusCode(97);
                            noConnectionError = false;
                        } else if (error instanceof ParseError) {
                            serverResponse.setPayload(firebaseRemoteConfig.getString("parse_error"));
                            serverResponse.setStatusCode(96);
                            noConnectionError = false;
                        }else if (error instanceof TimeoutError) {
                            serverResponse.setPayload(firebaseRemoteConfig.getString("timeout_error"));
                            serverResponse.setStatusCode(95);
                            noConnectionError = false;
                        }
                        serverResponseListener.getResponse(serverResponse,requestTag);
                        if(!noConnectionError){
                            EventBus.getDefault().unregister(this);
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("os", "Android");
                headers.put("app_version", BuildConfig.VERSION_NAME);
//                Log.e("getHeaders: ", "=="+new SessionManager(context).getAccessToken());
                if(new SessionManager(context).getAccessToken() != null){
                    headers.put("UNIQUE-ID", new SessionManager(context).getAccessToken());
                }
                return headers;
            }


            @Override
            public byte[] getBody() {
                try {
                    return new JSONObject(requestModel.getPayload()).toString().getBytes(Charset.forName("UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return new JSONObject().toString().getBytes();
                }
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    serverResponse.setStatusCode(response.statusCode);
                    JSONObject result = null;

                    if (jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }


        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyInstance.addToRequestQueue(jsObjRequest,context);

    }

    @SuppressLint("StaticFieldLeak")
    public void sendMultiPartRequest(final ServerResponseListener serverResponseListener, final String filePath, final int type) {
        new AsyncTask<String,Void,okhttp3.Response>(){
            @Override
            protected okhttp3.Response doInBackground(String... strings) {

                File f = new File(filePath) ;
                String content_type  = getMimeType(f.getPath());
                String file_path = f.getAbsolutePath();
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                RequestBody request_body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("profile_picture",file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(Constants.getInstance().getUpdateProfilePicUrl())
                        .post(request_body)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("os", "Android")
                        .addHeader("app_version", BuildConfig.VERSION_NAME)
                        .addHeader("UNIQUE-ID", new SessionManager(context).getAccessToken())
                        .build();
                try {
                    return client.newCall(request).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(okhttp3.Response response) {
                super.onPostExecute(response);
                if(response != null){
                    try {
                        serverResponse.setSuccess(true);
                        serverResponse.setStatusCode(response.code());
                        serverResponse.setPayload(response.body().string());
                    } catch (IOException e) {
                        serverResponse.setSuccess(false);
                        serverResponse.setStatusCode(400);
                        serverResponse.setPayload(firebaseRemoteConfig.getString("pic_upload_failed"));
                        e.printStackTrace();
                    }
                }else{
                    serverResponse.setSuccess(false);
                    serverResponse.setStatusCode(400);
                    serverResponse.setPayload(firebaseRemoteConfig.getString("pic_upload_failed"));
                }
                serverResponseListener.getResponse(serverResponse,type);
            }
        }.execute(filePath);
    }



    private String getMimeType(String path) {
        path = path.replace(" ", "");
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
//
//    public void downloadFile(final ServerResponseListener serverResponseListener,final int type,final String URL) {
//
//        final ProgressDialog dialog = new ProgressDialog(activity);
//
//
//        @SuppressLint("StaticFieldLeak")
//        AsyncTask<Void, Void, File> task = new AsyncTask<Void, Void, File>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                dialog.setMessage("Downloading...");
//                dialog.setCancelable(false);
//                dialog.show();
//            }
//
//            @Override
//            protected File doInBackground(Void... params) {
//
//                int TIMEOUT_CONNECTION = 5000;//5sec
//                int TIMEOUT_SOCKET = 30000;//30sec
//
//                java.net.URL url = null;
//                HttpURLConnection ucon = null;
//                String fileName = "";
//
//                try {
//                    url = new URL(URL);
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//                //Open a connection to that URL.
//
//                try {
//                    ucon = (HttpURLConnection) url.openConnection();
//                    responseCode = ucon.getResponseCode();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                //this timeout affects how long it takes for the app to realize there's a connection problem
//                ucon.setReadTimeout(TIMEOUT_CONNECTION);
//                ucon.setConnectTimeout(TIMEOUT_SOCKET);
//
//                // always check HTTP response code first
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    String disposition = ucon.getHeaderField("Content-Disposition");
//                    String contentType = ucon.getContentType();
//                    int contentLength = ucon.getContentLength();
//
//                    if (disposition != null) {
//                        // extracts file name from header field
//                        int index = disposition.indexOf("filename=");
//                        if (index > 0) {
//                            fileName = disposition.substring(index + 10,
//                                    disposition.length() - 1);
//                        }
//
//                    } else {
//                        return null;
//                    }
//                }else{
//                    return null;
//                }
//
//
//                final File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
//
//
//                String filePath = outputDir.getAbsolutePath() +"/"+fileName;
//
//                final File outputFile = new File(filePath);
//
//
//                if(!outputFile.exists()){
//                    //Define InputStreams to read from the URLConnection.
//                    // uses 3KB download buffer
//                    InputStream is = null;
//                    try {
//                        is = ucon.getInputStream();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
//                    FileOutputStream outStream = null;
//                    try {
//                        outStream = new FileOutputStream(outputFile);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    byte[] buff = new byte[5 * 1024];
//
//                    //Read bytes (and store them) until there is nothing more to read(-1)
//                    int len;
//                    try {
//                        while ((len = inStream.read(buff)) != -1) {
//                            outStream.write(buff, 0, len);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    //clean up
//                    try {
//                        outStream.flush();
//                        outStream.close();
//                        inStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }else{
//                    ucon.disconnect();
//                }
//
//                return outputFile;
//            }
//
//            @Override
//            protected void onPostExecute(File file) {
//                dialog.dismiss();
//                if(file != null && file.exists()){
//                    serverResponse.setStatusCode(200);
//                    serverResponse.setPayload(file.getAbsolutePath());
//                }else{
//                    serverResponse.setStatusCode(responseCode);
//                }
//                serverResponseListener.getResponse(serverResponse,type);
//
//            }
//        };
//        task.execute();
//    }


}
