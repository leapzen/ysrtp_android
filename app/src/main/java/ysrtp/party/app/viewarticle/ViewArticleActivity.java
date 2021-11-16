package ysrtp.party.app.viewarticle;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.DownloadImage;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.common.interfaces.DownloadListener;
import ysrtp.party.app.databinding.ActivityViewArticleBinding;
import ysrtp.party.app.network.Connectivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ViewArticleActivity extends PartyActivity implements DownloadListener {

    private ActivityViewArticleBinding viewArticleBinding;
    private int STORAGE_PERMISSION = 1004;
    private int articleId ;
    private ViewArticleModel viewArticleModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        viewArticleBinding = DataBindingUtil.setContentView(ViewArticleActivity.this, R.layout.activity_view_article);
        viewArticleBinding.progressBar.setVisibility(View.VISIBLE);
        ViewArticleViewModel viewArticleViewModel = new ViewModelProvider(ViewArticleActivity.this).get(ViewArticleViewModel.class);
        articleId = getIntent().getIntExtra("article_id",0);
        viewArticleViewModel.getArticleDetails(articleId);

        viewArticleViewModel.getViewArticleLiveData().observe(ViewArticleActivity.this, new Observer<ViewArticleModel>() {
            @Override
            public void onChanged(@Nullable ViewArticleModel viewArticleModel) {
                if(viewArticleModel != null &&
                        viewArticleModel.getResponseCode() == 1){
                    ViewArticleActivity.this.viewArticleModel = viewArticleModel;

                    if(viewArticleModel.getArticle().getArticleType().equalsIgnoreCase("normal")){
                        viewArticleBinding.tvTitle.setText(viewArticleModel.getArticle().getTitle());
                        if(viewArticleModel.getArticle().getContent().length() > 2){
                            viewArticleBinding.tvContent.setText(Html.fromHtml(viewArticleModel.getArticle().getContent()));
                        }else {
                            viewArticleBinding.tvContent.setVisibility(View.GONE);
                        }
                        RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.ic_launcher)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

                        Glide.with(viewArticleBinding.ivFullImage).load(viewArticleModel.getArticle().getOriginal()).apply(requestOptions)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        ViewArticleActivity.this.finish();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        viewArticleBinding.progressBar.setVisibility(View.GONE);
                                        viewArticleBinding.ivFullImage.setImageDrawable(resource);
                                        viewArticleBinding.ivFullImage.setZoom(1.0f);
                                        return false;
                                    }
                                }).into(viewArticleBinding.ivFullImage);
                    }else{
                        if(getIntent().getStringExtra("from_activity") != null &&
                                getIntent().getStringExtra("from_activity").equalsIgnoreCase("pushNotification")){
                            if(viewArticleModel.getArticle().getArticleType().equalsIgnoreCase("facebook") &&
                                    !viewArticleModel.getArticle().isSharedOnFacebook()) {
                                EventBus.getDefault().post(new ShareFromNotification(articleId,"com.facebook.katana"));
                            }
                            if(viewArticleModel.getArticle().getArticleType().equalsIgnoreCase("twitter") &&
                                    !viewArticleModel.getArticle().isSharedOnTwitter()) {
                                EventBus.getDefault().post(new ShareFromNotification(articleId,"com.twitter.android"));
                            }
                        }
                        try{
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(viewArticleModel.getArticle().getSocialUrl())));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        ViewArticleActivity.this.finish();
                    }

                }else{
                    ViewArticleActivity.this.finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ViewArticleActivity.this.finish();
    }

    public void backClicked(View view) {
        ViewArticleActivity.this.finish();
    }

    public void downloadImage(View view) {
        if (ActivityCompat.checkSelfPermission(ViewArticleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, STORAGE_PERMISSION);
            return;
        }
        File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appFolder = new File(pictureFolder, getString(R.string.app_name));
        File imageFile = new File(appFolder,
                "YSRTP-Image_" + articleId + ".jpg");

        if (imageFile.exists()) {
            showImageLocation(imageFile);
        } else {
            viewArticleBinding.progressBar.setVisibility(View.VISIBLE);
            String imageUrl;
            if (Connectivity.isConnectionFast(ViewArticleActivity.this)) {
                imageUrl = viewArticleModel.getArticle().getOriginal();
            } else {
                imageUrl = viewArticleModel.getArticle().getMedium();
            }
            new DownloadImage(this)
                    .execute(imageUrl);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImage(viewArticleBinding.imBtnDownload);
        } else {
            Toast.makeText(ViewArticleActivity.this, FirebaseRemoteConfig.getInstance().getString("storage_permission"), Toast.LENGTH_SHORT).show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDownloadBitmap(boolean isSuccess,Bitmap bitmap) {
        showImageLocation(getLocalBitmapUri(bitmap));
        if(isSuccess){
            showImageLocation(getLocalBitmapUri(bitmap));
        }else{
            new Utils(ViewArticleActivity.this).showSnackBar(FirebaseRemoteConfig.getInstance().getString("error_occured"));
        }
    }

    private void showImageLocation(final File localBitmapUri) {
        final Snackbar snackbar = Snackbar.make(viewArticleBinding.viewImageHolder, "Image downloaded successfully to "+
                localBitmapUri.getAbsolutePath(), Snackbar.LENGTH_INDEFINITE).setActionTextColor(Color.WHITE);
        snackbar.setAction("OPEN", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewImageIntent = new Intent(Intent.ACTION_VIEW);
                viewImageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Uri fileUri = FileProvider.getUriForFile(
                        ViewArticleActivity.this,
                        "ysrtp.party.app.fileprovider", localBitmapUri);
                String mimetype = getMimeType(localBitmapUri.getPath());

                viewImageIntent.setDataAndType(fileUri, mimetype);
                viewImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(viewImageIntent);
                }
                catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Snackbar.make(viewArticleBinding.viewImageHolder, "No suitable app found to open the file", Snackbar.LENGTH_LONG).show();
                }
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private File getLocalBitmapUri(Bitmap bmp) {
        File imageFile = null;
        try {
            File pictureFolder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appFolder = new File(pictureFolder, getString(R.string.app_name));
            if (!appFolder.exists()) {
                appFolder.mkdirs();
            }
            imageFile = new File(appFolder, "YSRTP-Image_"+articleId+".jpg");


            FileOutputStream out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private String getMimeType(String path) {
        path = path.replace(" ", "");
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
