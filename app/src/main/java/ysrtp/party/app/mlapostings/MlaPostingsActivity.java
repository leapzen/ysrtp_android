package ysrtp.party.app.mlapostings;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.DownloadArticles;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.common.interfaces.DownloadArticlesListener;
import ysrtp.party.app.common.interfaces.MyViewClickListener;
import ysrtp.party.app.databinding.ActivityMlaPostingsBinding;
import ysrtp.party.app.home.ArticleImages;
import ysrtp.party.app.home.FavArticleStatus;
import ysrtp.party.app.home.SingleArticle;
import ysrtp.party.app.home.sharedialog.MyDialogDismissListener;
import ysrtp.party.app.home.sharedialog.ShareCallBackListener;
import ysrtp.party.app.home.sharedialog.ShareDialog;
import ysrtp.party.app.home.sharedialog.SharedArticleStatus;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.network.Connectivity;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.viewarticle.ViewArticleActivity;
import ysrtp.party.app.webview.WebViewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static androidx.core.content.FileProvider.getUriForFile;

public class MlaPostingsActivity extends PartyActivity implements MyViewClickListener, DownloadArticlesListener,ShareCallBackListener.ShareCallBack {
    ActivityMlaPostingsBinding mlaPostingsBinding;
    MlaPostingsViewModel mlaPostingsViewModel;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean isRefreshed;
    private int previousArticlesCount;
    private ArrayList<File> downloadedFilesList;
    private ShareDialog shareDialog;
    private static final int SHARE_CALLBACK = 2002,PROCEED_FOR_POSTING=3003;
    private final int STORAGE_PERMISSION = 1004;
    private int viewTypeTemp, selectedArticlePosition;
    private String selectedPlatform = "other.app",shareMessage = "";
    private SingleArticle selectedArticle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mlaPostingsBinding = DataBindingUtil.setContentView(MlaPostingsActivity.this, R.layout.activity_mla_postings);
        mlaPostingsViewModel = new ViewModelProvider(this).get(MlaPostingsViewModel.class);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mlaPostingsBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        mlaPostingsBinding.refreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimaryDark);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mlaPostingsViewModel.getMlaPostings();

        mlaPostingsViewModel.getMlaPostingsLiveData().observe(this, mlaPostingsModel -> {
            if(mlaPostingsModel != null){
                if(isRefreshed){
                    isRefreshed = false;
                    int latestArticlesCount = mlaPostingsModel.getArticlesList().size() - previousArticlesCount;
                    if(latestArticlesCount !=0){
                        Toast.makeText(MlaPostingsActivity.this, latestArticlesCount + mFirebaseRemoteConfig.getString("latest_articles_count"), Toast.LENGTH_SHORT).show();
                    }
                }
                previousArticlesCount = mlaPostingsModel.getArticlesList().size();

                if(mlaPostingsModel.getArticlesList().size() > 0){
                    mlaPostingsBinding.tvNoContent.setVisibility(View.GONE);
                }else{
                    mlaPostingsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_new_mla_posts"));
                    mlaPostingsBinding.tvNoContent.setVisibility(View.VISIBLE);
                }

                mlaPostingsBinding.rvMlaPosts.setLayoutManager(new LinearLayoutManager(MlaPostingsActivity.this));
                mlaPostingsBinding.rvMlaPosts.setNestedScrollingEnabled(false);
                mlaPostingsBinding.rvMlaPosts.setHasFixedSize(true);
                mlaPostingsBinding.rvMlaPosts.setItemViewCacheSize(20);
                mlaPostingsBinding.rvMlaPosts.setDrawingCacheEnabled(true);
                mlaPostingsBinding.rvMlaPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                mlaPostingsBinding.rvMlaPosts.setAdapter(new MlaPostsAdapter(MlaPostingsActivity.this,mlaPostingsModel.getArticlesList()));
                mlaPostingsBinding.rvMlaPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        mlaPostingsBinding.refreshLayout.setEnabled(((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstCompletelyVisibleItemPosition() == 0);
                    }
                });

                mlaPostingsBinding.fabCreatePost.setOnClickListener(v -> {
                    Intent webView = new Intent(MlaPostingsActivity.this, WebViewActivity.class);
                    webView.putExtra("type","mla_posting");
                    webView.putExtra("url", UserDetailsModel.getInstance().getMlaPostingUrl());
                    startActivityForResult(webView,PROCEED_FOR_POSTING);
                });
            }else{
                mlaPostingsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_new_mla_posts"));
                mlaPostingsBinding.tvNoContent.setVisibility(View.VISIBLE);
            }
            mlaPostingsBinding.layoutLoading.getRoot().setVisibility(View.GONE);
            mlaPostingsBinding.refreshLayout.setRefreshing(false);
        });


        mlaPostingsViewModel.getFavArticleStatusLiveData().observe(MlaPostingsActivity.this, new Observer<FavArticleStatus>() {
            @Override
            public void onChanged(@Nullable FavArticleStatus favArticleStatus) {
                assert favArticleStatus != null;
                if(favArticleStatus.getMessage().length() > 1)
                    new Utils(MlaPostingsActivity.this).showSnackBar(favArticleStatus.getMessage());
            }
        });

        mlaPostingsViewModel.getSharedArticleStatusLiveData().observe(MlaPostingsActivity.this, new Observer<SharedArticleStatus>() {
            @Override
            public void onChanged(@Nullable SharedArticleStatus sharedArticleStatus) {
                if(sharedArticleStatus != null && selectedArticle != null){
                    new Utils(MlaPostingsActivity.this).showSnackBar(sharedArticleStatus.getMessage());
                    if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.whatsapp") || sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.whatsapp.w4b")){
                        selectedArticle.setSharedOnWhatsapp(1);
                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.facebook.katana")){
                        selectedArticle.setSharedOnFacebook(1);
                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.twitter.android")){
                        selectedArticle.setSharedOnTwitter(1);
                    }

                    if(shareDialog != null){
                        shareDialog.updateView(selectedArticle);
                    }

                    selectedArticle.setSharesCount(selectedArticle.getSharesCount()+1);
                    mlaPostingsBinding.rvMlaPosts.getAdapter().notifyItemChanged(selectedArticlePosition,selectedArticle);
                }
            }
        });
        mlaPostingsBinding.refreshLayout.setOnRefreshListener(() -> {
            isRefreshed = true;
            mlaPostingsViewModel.getMlaPostings();
        });
    }

    @Override
    public void onViewClick(int viewType,int position) {
        viewTypeTemp = viewType;
        selectedArticlePosition = position;
        selectedArticle = mlaPostingsViewModel.getMlaPostingsLiveData().getValue().getArticlesList()
                .get(position);
        downloadedFilesList = new ArrayList<>();
        addBannerImageToList();
        if(viewType == MyViewClickListener.SHARE){
            if (ActivityCompat.checkSelfPermission(MlaPostingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },STORAGE_PERMISSION);
                return;
            }

            String[] imageNames = new String[selectedArticle.getArticleImagesList().size()];
            String[] imageUrls = new String[selectedArticle.getArticleImagesList().size()];

            for (int i = 0; i < selectedArticle.getArticleImagesList().size() ; i++) {
                if (Connectivity.isConnectionFast(MlaPostingsActivity.this)) {
                    String imageName =  selectedArticle.getArticleImagesList().get(i).getOriginal();
                    imageNames[i] = imageName.substring(imageName.lastIndexOf("/")+1,imageName.lastIndexOf("."));
                    imageUrls[i] = selectedArticle.getArticleImagesList().get(i).getOriginal();
                } else {
                    String imageName =  selectedArticle.getArticleImagesList().get(i).getMedium();
                    imageNames[i] = imageName.substring(imageName.lastIndexOf("/")+1,imageName.lastIndexOf("."));
                    imageUrls[i] = selectedArticle.getArticleImagesList().get(i).getMedium();
                }
            }

            File pictureFolder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appFolder = new File(pictureFolder, getString(R.string.app_name));
            ArrayList<String> imagesToDownloadList = new ArrayList<>();

            for (int i = 0; i < imageNames.length; i++) {
                File imageFile = new File(appFolder,
                        imageNames[i]+".jpg");
                if(!imageFile.exists()){
                    imagesToDownloadList.add(imageUrls[i]);
                }
                downloadedFilesList.add(imageFile);
            }

            if(imagesToDownloadList.size() == 0){
                sharePost(downloadedFilesList);
            }else{
                mlaPostingsBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);

                new DownloadArticles(this)
                        .execute(imagesToDownloadList.toArray(new String[0]));
            }

        }else if(viewType == MyViewClickListener.LIKE){
            mlaPostingsViewModel.sendLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.DISLIKE){
            mlaPostingsViewModel.removeLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.POST){
            if(selectedArticle.getArticleType().equalsIgnoreCase("facebook")){
                mlaPostingsViewModel.sendSharedPost(selectedArticle.getId(),"com.facebook.katana");
            }else if(selectedArticle.getArticleType().equalsIgnoreCase("twitter")){
                mlaPostingsViewModel.sendSharedPost(selectedArticle.getId(),"com.twitter.android");
            }
            Intent viewArticleIntent = new Intent(MlaPostingsActivity.this, ViewArticleActivity.class);
            viewArticleIntent.putExtra("selected_article",selectedArticle);
            viewArticleIntent.putExtra("from_activity","new_posts");
            viewArticleIntent.putExtra("article_id",selectedArticle.getId());
            startActivity(viewArticleIntent);
        }
    }


    private void addBannerImageToList() {
        if(!selectedArticle.isBannerImageAdded()){
            selectedArticle.getArticleImagesList().add(0,new ArticleImages(selectedArticle.getOriginal(),
                    selectedArticle.getLarge(),
                    selectedArticle.getMedium(),
                    selectedArticle.getSmall()));
            selectedArticle.setBannerImageAdded(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onViewClick(viewTypeTemp, selectedArticlePosition);
        } else {
            Toast.makeText(MlaPostingsActivity.this, mFirebaseRemoteConfig.getString("storage_permission"), Toast.LENGTH_SHORT).show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDownloadArticleImage(boolean isSuccess) {
        mlaPostingsBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        if(isSuccess){
            sharePost(downloadedFilesList);
        }else{
            new Utils(MlaPostingsActivity.this).showSnackBar(mFirebaseRemoteConfig.getString("error_occured"));
        }
    }

    private void sharePost(final ArrayList<File> imagesToShareList) {
        if(!selectedArticle.getArticleType().equalsIgnoreCase("normal")){
            shareMessage = selectedArticle.getTitle()+"\n"+selectedArticle.getSocialUrl();
        }else{
            shareMessage = selectedArticle.getTitle();
        }
        shareDialog = new ShareDialog();
        Bundle shareBundle = new Bundle();
        shareBundle.putString("type","new_post");
        shareBundle.putParcelable("selectedArticle",selectedArticle);
        shareDialog.setArguments(shareBundle);
        shareDialog.setCancelable(false);
        shareDialog.show(MlaPostingsActivity.this.getSupportFragmentManager(), shareDialog.getTag());
        shareDialog.setOnDismissListener(new MyDialogDismissListener() {
            @Override
            public void onDismiss(String selectedPlatform) {
                if(!selectedPlatform.equalsIgnoreCase("exit")){
                    MlaPostingsActivity.this.selectedPlatform = selectedPlatform;
                    if(imagesToShareList != null && imagesToShareList.size() > 0){
                        ArrayList<Uri> multipleFileUris = new ArrayList<>();
                        for (int i = 0; i < imagesToShareList.size(); i++) {
                            Uri imageUri = getUriForFile(MlaPostingsActivity.this, "ysrtp.party.app.fileprovider", imagesToShareList.get(i));
                            multipleFileUris.add(imageUri);
                        }
                        Intent sendIntent = new Intent();
                        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        if(selectedPlatform.contains("twitter")){
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_STREAM,getUriForFile(MlaPostingsActivity.this, "ysrtp.party.app.fileprovider", imagesToShareList.get(0)));
                        }else{
                            sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,multipleFileUris);
                        }
                        if(selectedArticle.getArticleType().equalsIgnoreCase("normal")){
                            sendIntent.setType("image/*");
                        }else{
                            sendIntent.setType("text/plain");
                        }

                        if(!selectedPlatform.equalsIgnoreCase("other.app")){
                            sendIntent.setPackage(selectedPlatform);
                        }

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                            Intent receiver = new Intent(MlaPostingsActivity.this, ShareCallBackListener.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(MlaPostingsActivity.this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                            ShareCallBackListener.registerCallback(MlaPostingsActivity.this);
                            startActivity(Intent.createChooser(sendIntent, mFirebaseRemoteConfig.getString("send_to"), pendingIntent.getIntentSender()));
                        }else{
                            startActivityForResult(Intent.createChooser(sendIntent, mFirebaseRemoteConfig.getString("send_to")),SHARE_CALLBACK);
                        }

                    }else{
                        Toast.makeText(MlaPostingsActivity.this, mFirebaseRemoteConfig.getString("error_occured"), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SHARE_CALLBACK){
            mlaPostingsViewModel.sendSharedPost(selectedArticle.getId(),MlaPostingsActivity.this.selectedPlatform);
        }else if(requestCode == PROCEED_FOR_POSTING && resultCode == RESULT_OK){
//            Toast.makeText(this, mFirebaseRemoteConfig.getString("mla_post_created"), Toast.LENGTH_SHORT).show();
            mlaPostingsViewModel.getMlaPostings();
        }
    }

    @Override
    public void onShareSuccess(String selectedPlatform) {
        if(selectedPlatform != null){
            mlaPostingsViewModel.sendSharedPost(selectedArticle.getId(),selectedPlatform);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            MlaPostingsActivity.this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }


}
