package ysrtp.party.app.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import ysrtp.party.app.common.interfaces.DownloadArticlesListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadArticles extends AsyncTask<String,Integer,Boolean> {
    private DownloadArticlesListener downloadArticlesListener;
    private String imageName="";
    public DownloadArticles(DownloadArticlesListener downloadArticlesListener) {
        this.downloadArticlesListener = downloadArticlesListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        InputStream in = null;
        for (int i = 0; i < urls.length; i++) {
            try {
                URL url = new URL(urls[i]);
                imageName = url.toString();
                URLConnection urlConn = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                httpConn.connect();

                in = httpConn.getInputStream();
                storeBitmapInLocalStorage(BitmapFactory.decodeStream(in));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        downloadArticlesListener.onDownloadArticleImage(isSuccess);
    }

    private File storeBitmapInLocalStorage(Bitmap bmp) {
        imageName = imageName.substring(imageName.lastIndexOf("/")+1,imageName.lastIndexOf("."));

        File imageFile = null;
        try {
            File pictureFolder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appFolder = new File(pictureFolder, "YSRTP");
            if (!appFolder.exists()) {
                appFolder.mkdirs();
            }
            imageFile = new File(appFolder,imageName+".jpg");


            FileOutputStream out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }
}
