package ysrtp.party.app.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ysrtp.party.app.common.interfaces.DownloadListener;

public class DownloadImage extends AsyncTask<String,Integer,Bitmap> {
    private DownloadListener downloadListener;

    public DownloadImage(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        InputStream in = null;

        try {
            Log.e("doInBackground: ", "++"+urls[0]);
            URL url = new URL(urls[0]);
            URLConnection urlConn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.connect();

            in = httpConn.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return BitmapFactory.decodeStream(in);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        try {
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        downloadListener.onDownloadBitmap(bitmap != null,bitmap);
    }
}
