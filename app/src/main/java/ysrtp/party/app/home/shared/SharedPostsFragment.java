package ysrtp.party.app.home.shared;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.R;
import ysrtp.party.app.common.DownloadImage;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.common.interfaces.DownloadListener;
import ysrtp.party.app.common.interfaces.MyViewClickListener;
import ysrtp.party.app.databinding.FragmentSharedPostsBinding;
import ysrtp.party.app.home.FavArticleStatus;
import ysrtp.party.app.home.SingleArticle;
import ysrtp.party.app.home.sharedialog.MyDialogDismissListener;
import ysrtp.party.app.home.sharedialog.ShareCallBackListener;
import ysrtp.party.app.home.sharedialog.ShareDialog;
import ysrtp.party.app.home.sharedialog.SharedArticleStatus;
import ysrtp.party.app.home.unshared.UnSharedPostsModel;
import ysrtp.party.app.network.Connectivity;
import ysrtp.party.app.viewarticle.ViewArticleActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static androidx.core.content.FileProvider.getUriForFile;

public class SharedPostsFragment extends Fragment implements MyViewClickListener,DownloadListener,ShareCallBackListener.ShareCallBack {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPostsViewModel sharedPostsViewModel;
    private FragmentSharedPostsBinding sharedPostsBinding;
    private int viewTypeTemp,positionTemp;
    private SingleArticle selectedArticle;
    private String selectedPlatform = "other.app";
    private ShareDialog shareDialog;
    private boolean isSharingSuccess,isDirectShare;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        sharedPostsViewModel = new ViewModelProvider(this).get(SharedPostsViewModel.class);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        sharedPostsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_shared_posts, container, false);
        return sharedPostsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPostsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_shared_posts"));
        sharedPostsBinding.fabMoveUp.hide();
        sharedPostsViewModel.getSharedPostsLiveData().observe(getViewLifecycleOwner(), new Observer<SharedPostsModel>() {
            @Override
            public void onChanged(@Nullable SharedPostsModel sharedPostsModel) {
                if(sharedPostsModel != null){
                    if(sharedPostsModel.getArticlesList().size() > 0){
                        sharedPostsBinding.tvNoContent.setVisibility(View.GONE);
                    }else{
                        sharedPostsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_shared_posts"));
                        sharedPostsBinding.tvNoContent.setVisibility(View.VISIBLE);
                    }
                    sharedPostsBinding.rvSharedPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
                    sharedPostsBinding.rvSharedPosts.setNestedScrollingEnabled(false);
                    sharedPostsBinding.rvSharedPosts.setHasFixedSize(true);
                    sharedPostsBinding.rvSharedPosts.setItemViewCacheSize(20);
                    sharedPostsBinding.rvSharedPosts.setDrawingCacheEnabled(true);
                    sharedPostsBinding.rvSharedPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    sharedPostsBinding.rvSharedPosts.setAdapter(
                            new SharedPostsAdapter(SharedPostsFragment.this,sharedPostsModel.getArticlesList()));
                    sharedPostsBinding.rvSharedPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition() == 0) {
                                sharedPostsBinding.fabMoveUp.hide();
                            }
                            if (dy > 0) {
                                // Scrolling up
                                sharedPostsBinding.fabMoveUp.hide();
                            } else if (dy < -50) {
                                // Scrolling down
                                sharedPostsBinding.fabMoveUp.show();
                            }
                        }
                    });

                    sharedPostsBinding.fabMoveUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sharedPostsBinding.rvSharedPosts.smoothScrollToPosition(0);
                            sharedPostsBinding.fabMoveUp.hide();
                        }
                    });
                }else{
                    sharedPostsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_shared_posts"));
                    sharedPostsBinding.tvNoContent.setVisibility(View.VISIBLE);
                }
            }
        });

        sharedPostsViewModel.getFavArticleStatusLiveData().observe(getViewLifecycleOwner(),new Observer<FavArticleStatus>() {
            @Override
            public void onChanged(@Nullable FavArticleStatus favArticleStatus) {
                if(favArticleStatus.getMessage().length() > 1)
                    new Utils(getActivity()).showSnackBar(favArticleStatus.getMessage());
            }
        });


        sharedPostsViewModel.getSharedArticleStatusLiveData().observe(getViewLifecycleOwner(), new Observer<SharedArticleStatus>() {
            @Override
            public void onChanged(@Nullable SharedArticleStatus sharedArticleStatus) {
                if(sharedArticleStatus != null){
                    if(sharedArticleStatus.getMessage().length() > 1)
                    new Utils(getActivity()).showSnackBar(sharedArticleStatus.getMessage());
                    selectedArticle.setSharesCount(selectedArticle.getSharesCount()+1);

                    if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.whatsapp") || sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.whatsapp.w4b")){
                        selectedArticle.setSharedOnWhatsapp(1);
                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.facebook.katana")){
                        selectedArticle.setSharedOnFacebook(1);
                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.twitter.android")){
                        selectedArticle.setSharedOnTwitter(1);
                    }

                    sharedPostsBinding.rvSharedPosts.getAdapter().notifyItemChanged(positionTemp,selectedArticle);
                    if(!isDirectShare){
                        shareDialog.updateView(selectedArticle);
                    }
                }
            }
        });

    }

    public void scrollRecyclerView() {
        sharedPostsBinding.fabMoveUp.hide();
        sharedPostsBinding.rvSharedPosts.smoothScrollToPosition(0);
        sharedPostsBinding.fabMoveUp.hide();
    }


    @Override
    public void onViewClick(int viewType,int position) {
        viewTypeTemp = viewType;
        positionTemp = position;
        selectedArticle = sharedPostsViewModel.getSharedPostsLiveData().getValue().getArticlesList().get(position);
        if(viewType == MyViewClickListener.SHARE){
            isDirectShare = false;
            verifyPermissionsAndShare();
        }if(viewType == MyViewClickListener.FACEBOOK){
            if(!isPackageInstalled("com.facebook.katana")){
                new Utils(getActivity()).showSnackBar(mFirebaseRemoteConfig.getString("install_facebook"));
            }else{
                isDirectShare = true;
                selectedPlatform = "com.facebook.katana";
                verifyPermissionsAndShare();
            }
        }if(viewType == MyViewClickListener.TWITTER){
            if(!isPackageInstalled("com.twitter.android")){
                new Utils(getActivity()).showSnackBar(mFirebaseRemoteConfig.getString("install_twitter"));
            }else{
                selectedPlatform = "com.twitter.android";
                isDirectShare = true;
                verifyPermissionsAndShare();
            }
        }if(viewType == MyViewClickListener.WHATSAPP){
            if(isPackageInstalled("com.whatsapp")){
                selectedPlatform = "com.whatsapp";
            }else if(isPackageInstalled("com.whatsapp.w4b")){
                selectedPlatform = "com.whatsapp.w4b";
            }else {
                new Utils(getActivity()).showSnackBar(mFirebaseRemoteConfig.getString("install_whatsapp"));
                return;
            }
            isDirectShare = true;
            verifyPermissionsAndShare();
        }else if(viewType == MyViewClickListener.LIKE){
            sharedPostsViewModel.sendLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.DISLIKE){
            sharedPostsViewModel.removeLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.POST){
            Intent viewArticleIntent = new Intent(getActivity(),ViewArticleActivity.class);
            viewArticleIntent.putExtra("article_id",selectedArticle.getId());
            startActivity(viewArticleIntent);
        }
    }

    private boolean isPackageInstalled(String packagename) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void verifyPermissionsAndShare() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mPermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        File pictureFolder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appFolder = new File(pictureFolder, getString(R.string.app_name));
        File imageFile = new File(appFolder,
                "YSRTP-Image_"+selectedArticle.getId()+".jpg");

        if(imageFile.exists()){
            sharePost(imageFile,isDirectShare);
        }else{
            sharedPostsBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            String imageUrl;

            if(Connectivity.isConnectionFast(getActivity())){
                imageUrl = selectedArticle.getOriginal();
            }else{
                imageUrl = selectedArticle.getMedium();
            }
            new DownloadImage(this)
                    .execute(imageUrl);
        }
    }

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result) {
                        onViewClick(viewTypeTemp,positionTemp);
                    } else {
                        Toast.makeText(getActivity(), mFirebaseRemoteConfig.getString("storage_permission"), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onDownloadBitmap(boolean isSuccess,Bitmap bitmap) {
        sharedPostsBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        if(isSuccess){
            sharePost(getLocalBitmapUri(bitmap),isDirectShare);
        }else{
            new Utils(getActivity()).showSnackBar(mFirebaseRemoteConfig.getString("error_occured"));
        }
    }

    private File getLocalBitmapUri(Bitmap bmp) {
        File imageFile = null;
        try {
            File pictureFolder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appFolder = new File(pictureFolder, getString(R.string.app_name));
            if (!appFolder.exists()) {
                appFolder.mkdirs();
            }
            imageFile = new File(appFolder, "YSRTP-Image_"+selectedArticle.getId()+".jpg");


            FileOutputStream out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private void sharePost(final File imageFile,boolean isDirectShare) {

        if(isDirectShare){
            readyForShare(selectedPlatform,imageFile);
        }else{
            shareDialog = new ShareDialog();
            Bundle shareBundle = new Bundle();
            shareBundle.putString("type","shared_post");
            shareBundle.putParcelable("selectedArticle",selectedArticle);
            shareDialog.setArguments(shareBundle);
            shareDialog.setCancelable(false);
            shareDialog.show(getActivity().getSupportFragmentManager(), shareDialog.getTag());
            shareDialog.setOnDismissListener(new MyDialogDismissListener() {

                @Override
                public void onDismiss(String selectedPlatform) {
                    readyForShare(selectedPlatform,imageFile);
                }
            });
        }
    }

    private void readyForShare(String selectedPlatform,final File imageFile) {
        String shareMessage = "";
        if(!selectedArticle.getArticleType().equalsIgnoreCase("normal")){
            shareMessage = selectedArticle.getTitle()+"\n"+selectedArticle.getSocialUrl();
        }else{
            shareMessage = selectedArticle.getTitle();
        }

        if(selectedPlatform.equalsIgnoreCase("exit") && isSharingSuccess){
            isSharingSuccess = false;
            for(int i = 0; i < UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().size(); i++){
                if(UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().get(i).getId() ==
                        selectedArticle.getId()){
                    UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().remove(i);
                    UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().add(i,selectedArticle);
                    break;
                }
            }

        }else if(!selectedPlatform.equalsIgnoreCase("exit")){
            SharedPostsFragment.this.selectedPlatform = selectedPlatform;
            if(imageFile != null && imageFile.exists()){
                Uri imageUri = getUriForFile(getActivity(), "ysrtp.party.app.fileprovider", imageFile);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                sendIntent.putExtra(Intent.EXTRA_STREAM,imageUri);
                if(selectedArticle.getArticleType().equalsIgnoreCase("normal")){
                    sendIntent.setType("image/*");
                }else{
                    sendIntent.setType("text/plain");
                }

                if(!selectedPlatform.equalsIgnoreCase("other.app")){
                    sendIntent.setPackage(selectedPlatform);
                    isSharingSuccess = true;
                }

                Intent receiver = new Intent(getActivity(), ShareCallBackListener.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                ShareCallBackListener.registerCallback(SharedPostsFragment.this);
                startActivity(Intent.createChooser(sendIntent, mFirebaseRemoteConfig.getString("send_to"), pendingIntent.getIntentSender()));

            }else{
                Toast.makeText(getActivity(), mFirebaseRemoteConfig.getString("error_occured"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onShareSuccess(String selectedPlatform) {
        if(selectedPlatform != null){
            isSharingSuccess = true;
            sharedPostsViewModel.sendSharedPost(selectedArticle.getId(),selectedPlatform);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(sharedPostsBinding != null &&
                sharedPostsBinding.rvSharedPosts.getAdapter() != null){

            Set<SingleArticle> withoutDuplicates = new LinkedHashSet<>(SharedPostsModel.getSharedPostsInstance().getArticlesList());
            SharedPostsModel.getSharedPostsInstance().getArticlesList().clear();
            SharedPostsModel.getSharedPostsInstance().getArticlesList().addAll(withoutDuplicates);

            sharedPostsBinding.rvSharedPosts.getAdapter().notifyDataSetChanged();

            if(sharedPostsViewModel.getSharedPostsLiveData().getValue().getArticlesList().size() > 0){
                sharedPostsBinding.tvNoContent.setVisibility(View.GONE);
            }else{
                sharedPostsBinding.tvNoContent.setVisibility(View.VISIBLE);
            }
        }
    }
}
