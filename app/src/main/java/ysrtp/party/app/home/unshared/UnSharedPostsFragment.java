package ysrtp.party.app.home.unshared;

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
import ysrtp.party.app.databinding.FragmentUnsharedPostsBinding;
import ysrtp.party.app.home.FavArticleStatus;
import ysrtp.party.app.home.HomeActivity;
import ysrtp.party.app.home.SingleArticle;
import ysrtp.party.app.home.shared.SharedPostsModel;
import ysrtp.party.app.home.sharedialog.ShareCallBackListener;
import ysrtp.party.app.home.sharedialog.ShareDialog;
import ysrtp.party.app.home.sharedialog.SharedArticleStatus;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.network.Connectivity;
import ysrtp.party.app.viewarticle.ViewArticleActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static androidx.core.content.FileProvider.getUriForFile;

public class UnSharedPostsFragment extends Fragment implements MyViewClickListener,DownloadListener,ShareCallBackListener.ShareCallBack {

    private UnSharedPostsViewModel unSharedPostsViewModel;
    private FragmentUnsharedPostsBinding unsharedPostsBinding;
    private int viewTypeTemp,positionTemp;
    private String selectedPlatform = "other.app";
    private SingleArticle selectedArticle;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private ShareDialog shareDialog;
    private boolean isSharingSuccess,isDirectShare;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        unSharedPostsViewModel = new ViewModelProvider(this).get(UnSharedPostsViewModel.class);

        unsharedPostsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_unshared_posts, container, false);
        return  unsharedPostsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        unsharedPostsBinding.fabMoveUp.hide();
        unSharedPostsViewModel.getUnSharedPostsLiveData().observe(getViewLifecycleOwner(), new Observer<UnSharedPostsModel>() {
            @Override
            public void onChanged(@Nullable UnSharedPostsModel unSharedPostsModel) {
                if(unSharedPostsModel != null){
                    if(unSharedPostsModel.getArticlesList().size() > 0){
                        unsharedPostsBinding.tvNoContent.setVisibility(View.GONE);
                    }else{
                        unsharedPostsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_unshared_posts"));
                        unsharedPostsBinding.tvNoContent.setVisibility(View.VISIBLE);
                        ((HomeActivity)getActivity()).moveTabs(2);
                    }
                    unsharedPostsBinding.rvUnsharedPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
                    unsharedPostsBinding.rvUnsharedPosts.setNestedScrollingEnabled(false);
                    unsharedPostsBinding.rvUnsharedPosts.setHasFixedSize(true);
                    unsharedPostsBinding.rvUnsharedPosts.setItemViewCacheSize(20);
                    unsharedPostsBinding.rvUnsharedPosts.setDrawingCacheEnabled(true);
                    unsharedPostsBinding.rvUnsharedPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    unsharedPostsBinding.rvUnsharedPosts.setAdapter(new UnSharedPostsAdapter(UnSharedPostsFragment.this, unSharedPostsModel.getArticlesList()));
                    unsharedPostsBinding.rvUnsharedPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition() == 0) {
                                unsharedPostsBinding.fabMoveUp.hide();
                            }
                            if (dy > 0) {
                                // Scrolling up
                                unsharedPostsBinding.fabMoveUp.hide();
                            } else if (dy < -50) {
                                // Scrolling down
                                unsharedPostsBinding.fabMoveUp.show();
                            }
                        }
                    });

                    unsharedPostsBinding.fabMoveUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            unsharedPostsBinding.rvUnsharedPosts.smoothScrollToPosition(0);
                            unsharedPostsBinding.fabMoveUp.hide();
                        }
                    });
                }else{
                    unsharedPostsBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_unshared_posts"));
                    unsharedPostsBinding.tvNoContent.setVisibility(View.VISIBLE);
                }
            }
        });

        unSharedPostsViewModel.getFavArticleStatusLiveData().observe(getViewLifecycleOwner(), new Observer<FavArticleStatus>() {
            @Override
            public void onChanged(@Nullable FavArticleStatus favArticleStatus) {
                if(favArticleStatus.getMessage().length() > 1)
                new Utils(getActivity()).showSnackBar(favArticleStatus.getMessage());
            }
        });

        unSharedPostsViewModel.getSharedArticleStatusLiveData().observe(getViewLifecycleOwner(), new Observer<SharedArticleStatus>() {
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
//                    else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.instagram.android")){
//                        selectedArticle.setSharedOnInstagram(1);
//                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("in.mohalla.sharechat")){
//                        selectedArticle.setSharedOnSharechat(1);
//                    }
                    if(selectedArticle.isSharedOnWhatsapp() &&
                            selectedArticle.isSharedOnFacebook() &&
                            selectedArticle.isSharedOnTwitter()){
                        UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().remove(positionTemp);
                        SharedPostsModel.getSharedPostsInstance().getArticlesList().add(0,selectedArticle);
                        unsharedPostsBinding.rvUnsharedPosts.getAdapter().notifyItemRemoved(positionTemp);
                        UserDetailsModel.getInstance().setPartialSharedCount(UserDetailsModel.getInstance().getPartialSharedCount() - 1);
                        UserDetailsModel.getInstance().setSharedArticlesCount(UserDetailsModel.getInstance().getSharedArticlesCount() + 1);
                        ((HomeActivity)getActivity()).homeTabsAdapter.updateTitles(true);
                    }else{
                        unsharedPostsBinding.rvUnsharedPosts.getAdapter().notifyItemChanged(positionTemp,selectedArticle);
                    }

                    if(!isDirectShare){
                        shareDialog.updateView(selectedArticle);
                    }
                }
            }
        });
    }

    public void scrollRecyclerView() {
        unsharedPostsBinding.fabMoveUp.hide();
        unsharedPostsBinding.rvUnsharedPosts.smoothScrollToPosition(0);
        unsharedPostsBinding.fabMoveUp.hide();
    }

    @Override
    public void onViewClick(int viewType,int position) {
        viewTypeTemp = viewType;
        positionTemp = position;

        selectedArticle = unSharedPostsViewModel.getUnSharedPostsLiveData().getValue().getArticlesList().get(position);
        if(viewType == MyViewClickListener.SHARE){
            isDirectShare = false;
        verifyPermissionsAndShare();

        }else if(viewType == MyViewClickListener.FACEBOOK){
            if(!isPackageInstalled("com.facebook.katana")){
                new Utils(getActivity()).showSnackBar(mFirebaseRemoteConfig.getString("install_facebook"));
            }else{
                isDirectShare = true;
                selectedPlatform = "com.facebook.katana";
                verifyPermissionsAndShare();
            }
        }else if(viewType == MyViewClickListener.TWITTER){
            if(!isPackageInstalled("com.twitter.android")){
                new Utils(getActivity()).showSnackBar(mFirebaseRemoteConfig.getString("install_twitter"));
            }else{
                selectedPlatform = "com.twitter.android";
                isDirectShare = true;
                verifyPermissionsAndShare();
            }
        }else if(viewType == MyViewClickListener.WHATSAPP){
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
            unSharedPostsViewModel.sendLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.DISLIKE){
            unSharedPostsViewModel.removeLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.POST){
            isDirectShare = true;
            if(selectedArticle.getArticleType().equalsIgnoreCase("facebook")){
                unSharedPostsViewModel.sendSharedPost(selectedArticle.getId(),"com.facebook.katana");
            }else if(selectedArticle.getArticleType().equalsIgnoreCase("twitter")){
                unSharedPostsViewModel.sendSharedPost(selectedArticle.getId(),"com.twitter.android");
            }

            Intent viewArticleIntent = new Intent(getActivity(),ViewArticleActivity.class);
            viewArticleIntent.putExtra("article_id",selectedArticle.getId());
            startActivity(viewArticleIntent);
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
            unsharedPostsBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
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

    private final ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
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


    private boolean isPackageInstalled(String packagename) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDownloadBitmap(boolean isSuccess,Bitmap bitmap) {
        unsharedPostsBinding.layoutLoading.getRoot().setVisibility(View.GONE);
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
            imageFile = new File(appFolder, "YSRTP-Image_"+ unSharedPostsViewModel.getUnSharedPostsLiveData().getValue().getArticlesList().get(positionTemp).getId()+".jpg");


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
            shareDialog.setOnDismissListener(selectedPlatform -> readyForShare(selectedPlatform,imageFile));
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
            UnSharedPostsFragment.this.selectedPlatform = selectedPlatform;
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
                ShareCallBackListener.registerCallback(UnSharedPostsFragment.this);
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
            unSharedPostsViewModel.sendSharedPost(selectedArticle.getId(),selectedPlatform);
        }
    }


    public void sortPosts(String type) {
        unSharedPostsViewModel.sortPosts(type);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(unsharedPostsBinding != null &&
                unsharedPostsBinding.rvUnsharedPosts.getAdapter() != null){
            Set<SingleArticle> withoutDuplicates = new LinkedHashSet<>(UnSharedPostsModel.getUnSharedPostsModel().getArticlesList());
            UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().clear();
            UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().addAll(withoutDuplicates);
            unsharedPostsBinding.rvUnsharedPosts.getAdapter().notifyDataSetChanged();

            if(unSharedPostsViewModel.getUnSharedPostsLiveData().getValue().getArticlesList().size() > 0){
                unsharedPostsBinding.tvNoContent.setVisibility(View.GONE);
            }else{
                unsharedPostsBinding.tvNoContent.setVisibility(View.VISIBLE);
            }

        }
    }
}

