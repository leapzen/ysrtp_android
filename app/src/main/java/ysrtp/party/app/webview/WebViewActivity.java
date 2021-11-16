package ysrtp.party.app.webview;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Objects;

import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.databinding.ActivityWebViewBinding;

public class WebViewActivity extends PartyActivity {
private ActivityWebViewBinding webViewBinding;
    public ValueCallback<Uri[]> uploadMessage;
    private final int OPEN_FILE_CHOOSER = 101,STORAGE_PERMISSIONS = 202;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);

        webViewBinding = DataBindingUtil.setContentView(WebViewActivity.this, R.layout.activity_web_view);
        webViewBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);

        if(getIntent() != null){
            if(getIntent().getStringExtra("type").equalsIgnoreCase("tnc")){
                getSupportActionBar().setTitle(FirebaseRemoteConfig.getInstance().getString("title_privacy"));
            }else if(getIntent().getStringExtra("type").equalsIgnoreCase("mla_posting")){
//                getSupportActionBar().hide();
                getSupportActionBar().setTitle(FirebaseRemoteConfig.getInstance().getString("new_mla_postings"));
            }else{
                getSupportActionBar().setTitle(FirebaseRemoteConfig.getInstance().getString("title_rewards"));
            }
            webViewBinding.wvTerms.loadUrl(getIntent().getStringExtra("url"),getCustomHeaders());

        }else{
           WebViewActivity.this.finish();
        }
        webViewBinding.wvTerms.getSettings().setJavaScriptEnabled(true);
        webViewBinding.wvTerms.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webViewBinding.wvTerms.getSettings().setDomStorageEnabled(true);
        webViewBinding.wvTerms.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void performClick()
            {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                WebViewActivity.this.finish();
            }
        },"close");

        webViewBinding.wvTerms.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                WebViewActivity.this.finish();
            }

            public void onPageFinished(WebView view, String url) {
                webViewBinding.layoutLoading.getRoot().setVisibility(View.GONE);
            }
        });

        webViewBinding.wvTerms.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams
                    fileChooserParams) {
                if (ActivityCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(WebViewActivity.this,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    }, STORAGE_PERMISSIONS);
                    return false;
                }

                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);

                try {
                    startActivityForResult(intent, OPEN_FILE_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(WebViewActivity.this, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_FILE_CHOOSER && resultCode == Activity.RESULT_OK) {

            Uri[] results = null;
            String stringData;
            ClipData clipData;

            try {
                clipData = data.getClipData();
                stringData = data.getDataString();
            }catch (Exception e){
                clipData = null;
                stringData = null;
            }


            if (null != clipData) { // checking if multiple files selected or not
                final int numSelectedFiles = clipData.getItemCount();
                results = new Uri[numSelectedFiles];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    results[i] = clipData.getItemAt(i).getUri();
                }
            } else {
                results = new Uri[]{Uri.parse(stringData)};
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        }else{
            uploadMessage.onReceiveValue(new Uri[]{});
            uploadMessage = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(WebViewActivity.this, "Thanks for providing storage permission. Choose files for upload", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(WebViewActivity.this, "Storage permission required", Toast.LENGTH_SHORT).show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private HashMap<String, String> getCustomHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put("UNIQUE-ID", new SessionManager(this).getAccessToken());
        return headers;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            WebViewActivity.this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
