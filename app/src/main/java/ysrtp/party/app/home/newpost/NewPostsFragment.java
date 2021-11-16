package ysrtp.party.app.home.newpost;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.R;
import ysrtp.party.app.common.DownloadImage;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.common.interfaces.DownloadListener;
import ysrtp.party.app.common.interfaces.MyViewClickListener;
import ysrtp.party.app.databinding.FragmentNewPostsBinding;
import ysrtp.party.app.home.FavArticleStatus;
import ysrtp.party.app.home.HomeActivity;
import ysrtp.party.app.home.SingleArticle;
import ysrtp.party.app.home.shared.SharedPostsModel;
import ysrtp.party.app.home.sharedialog.MyDialogDismissListener;
import ysrtp.party.app.home.sharedialog.ShareCallBackListener;
import ysrtp.party.app.home.sharedialog.ShareDialog;
import ysrtp.party.app.home.sharedialog.SharedArticleStatus;
import ysrtp.party.app.home.unshared.UnSharedPostsModel;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.network.Connectivity;
import ysrtp.party.app.viewarticle.ShareFromNotification;
import ysrtp.party.app.viewarticle.ViewArticleActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static androidx.core.content.FileProvider.getUriForFile;


public class NewPostsFragment extends Fragment implements MyViewClickListener,DownloadListener,ShareCallBackListener.ShareCallBack {
    private NewPostsViewModel newPostsViewModel;
    private FragmentNewPostsBinding newPostsFragmentBinding;
    private int viewTypeTemp,positionTemp;
    private String selectedPlatform = "other.app",shareMessage = "";
    private SingleArticle selectedArticle;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private ShareDialog shareDialog;
    private boolean isSharingSuccess,sharedOnThreeSM,notificationArticleShared,isRefreshed;
    private int pushNotificationArticleId;
    private int previousArticlesCount;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        newPostsViewModel = new ViewModelProvider(this).get(NewPostsViewModel.class);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        newPostsFragmentBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_new_posts, container, false);
        newPostsFragmentBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        return newPostsFragmentBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newPostsFragmentBinding.fabMoveUp.hide();
//        newPostsFragmentBinding.refreshLayout.setColorSchemeResources(
//                R.color.colorPrimary,
//                R.color.colorPrimaryDark);
        newPostsViewModel.getNewPostsModelLiveData().observe(getViewLifecycleOwner(), new Observer<NewPostsModel>() {
            @Override
            public void onChanged(@Nullable NewPostsModel newPostsModel) {
                if(newPostsModel != null){
                    if(isRefreshed){
                        isRefreshed = false;
                        int latestArticlesCount = newPostsModel.getArticlesList().size() - previousArticlesCount;
                        if(latestArticlesCount !=0){
                            Toast.makeText(getActivity(), latestArticlesCount + mFirebaseRemoteConfig.getString("latest_articles_count"), Toast.LENGTH_SHORT).show();
                        }
                    }
                    previousArticlesCount = newPostsModel.getArticlesList().size();
                    UserDetailsModel.getInstance().setNewArticlesCount(newPostsModel.getArticlesList().size());
                    ((HomeActivity)getActivity()).homeTabsAdapter.updateTitles(true);
                    if(newPostsModel.getArticlesList().size() > 0){
                        newPostsFragmentBinding.tvNoContent.setVisibility(View.GONE);
                    }else{
                        newPostsFragmentBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_new_posts"));
                        newPostsFragmentBinding.tvNoContent.setVisibility(View.VISIBLE);
                        ((HomeActivity)getActivity()).moveTabs(1);
                    }

                    newPostsFragmentBinding.rvNewPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
                    newPostsFragmentBinding.rvNewPosts.setNestedScrollingEnabled(false);
                    newPostsFragmentBinding.rvNewPosts.setHasFixedSize(true);
                    newPostsFragmentBinding.rvNewPosts.setItemViewCacheSize(20);
                    newPostsFragmentBinding.rvNewPosts.setDrawingCacheEnabled(true);
                    newPostsFragmentBinding.rvNewPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    newPostsFragmentBinding.rvNewPosts.setAdapter(new NewPostsAdapter(NewPostsFragment.this,newPostsModel.getArticlesList()));
                    newPostsFragmentBinding.rvNewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition() == 0) {
                                newPostsFragmentBinding.fabMoveUp.hide();
//                                newPostsFragmentBinding.refreshLayout.setEnabled(true);
                            }else{
//                                newPostsFragmentBinding.refreshLayout.setEnabled(false);
                            }
                            Log.e(((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition()+"onScrolled: ", dy+"dy--->     dx---> "+dx);
                            if (dy > 0) {
                                // Scrolling up
                                newPostsFragmentBinding.fabMoveUp.hide();
                            } else if (dy < -50) {
                                // Scrolling down
                                newPostsFragmentBinding.fabMoveUp.show();
                            }
                        }
                    });

                    newPostsFragmentBinding.fabMoveUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            newPostsFragmentBinding.rvNewPosts.smoothScrollToPosition(0);
                            newPostsFragmentBinding.fabMoveUp.hide();
                        }
                    });
                    markNotificationArticleAsShared();
                }else{
                    newPostsFragmentBinding.tvNoContent.setText(mFirebaseRemoteConfig.getString("no_new_posts"));
                    newPostsFragmentBinding.tvNoContent.setVisibility(View.VISIBLE);
                }
                newPostsFragmentBinding.layoutLoading.getRoot().setVisibility(View.GONE);
//                newPostsFragmentBinding.refreshLayout.setRefreshing(false);
            }
        });

        newPostsViewModel.getFavArticleStatusLiveData().observe(getViewLifecycleOwner(), new Observer<FavArticleStatus>() {
            @Override
            public void onChanged(@Nullable FavArticleStatus favArticleStatus) {
                if(favArticleStatus.getMessage().length() > 1)
                    new Utils(getActivity()).showSnackBar(favArticleStatus.getMessage());
            }
        });

        newPostsViewModel.getSharedArticleStatusLiveData().observe(getViewLifecycleOwner(), new Observer<SharedArticleStatus>() {
            @Override
            public void onChanged(@Nullable SharedArticleStatus sharedArticleStatus) {
                if(sharedArticleStatus != null && selectedArticle != null){
                    new Utils(getActivity()).showSnackBar(sharedArticleStatus.getMessage());
                    if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.whatsapp") || sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.whatsapp.w4b")){
                        selectedArticle.setSharedOnWhatsapp(1);
                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.facebook.katana")){
                        selectedArticle.setSharedOnFacebook(1);
                    }else if(sharedArticleStatus.getSelectedPlatform().equalsIgnoreCase("com.twitter.android")){
                        selectedArticle.setSharedOnTwitter(1);
                    }

                    sharedOnThreeSM = selectedArticle.isSharedOnTwitter() && selectedArticle.isSharedOnFacebook() && selectedArticle.isSharedOnWhatsapp();

                    if(shareDialog != null){
                        shareDialog.updateView(selectedArticle);
                    }else{
                        UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().add(0,selectedArticle);
                        ((HomeActivity)getActivity()).moveTabs(1);
                        UserDetailsModel.getInstance().setPartialSharedCount(UserDetailsModel.getInstance().getPartialSharedCount() + 1);
                        newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList().remove(selectedArticle);
                        newPostsFragmentBinding.rvNewPosts.getAdapter().notifyDataSetChanged();
                        newPostsFragmentBinding.rvNewPosts.smoothScrollToPosition(0);
                        UserDetailsModel.getInstance().setNewArticlesCount(UserDetailsModel.getInstance().getNewArticlesCount() - 1);
                        ((HomeActivity)getActivity()).homeTabsAdapter.updateTitles(true);
                    }

                    selectedArticle.setSharesCount(selectedArticle.getSharesCount()+1);

                }
            }
        });
//        newPostsFragmentBinding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                isRefreshed = true;
//                newPostsViewModel.getNewPosts();
//            }
//        });
    }

    private void markNotificationArticleAsShared() {
        if(pushNotificationArticleId != 0 && !notificationArticleShared &&
                newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList().size() > 0){
            for (int i = 0; i < newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList().size(); i++) {
                if(pushNotificationArticleId == newPostsViewModel.getNewPostsModelLiveData().getValue()
                        .getArticlesList().get(i).getId()){
                    selectedArticle = newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList().get(i);
                    newPostsViewModel.sendSharedPost(pushNotificationArticleId,selectedPlatform);
                    notificationArticleShared = true;
                }
            }
        }
    }

    public void scrollRecyclerView() {
        newPostsFragmentBinding.fabMoveUp.hide();
        newPostsFragmentBinding.rvNewPosts.smoothScrollToPosition(0);
        newPostsFragmentBinding.fabMoveUp.hide();
    }

    @Override
    public void onViewClick(int viewType,int position) {
        viewTypeTemp = viewType;
        positionTemp = position;
        selectedArticle = newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList()
                .get(position);
        if(viewType == MyViewClickListener.SHARE){


            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                mPermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
            File pictureFolder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appFolder = new File(pictureFolder, getString(R.string.app_name));
            File imageFile = new File(appFolder,
                    "YSRTP-Image_"+selectedArticle.getId()+".jpg");

            if(imageFile.exists()){
                sharePost(imageFile);
            }else{
                newPostsFragmentBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
                String imageUrl;
                if(Connectivity.isConnectionFast(getActivity())){
                    imageUrl = selectedArticle.getOriginal();
                }else{
                    imageUrl = selectedArticle.getMedium();
                }
                new DownloadImage(this)
                        .execute(imageUrl);
            }

        }else if(viewType == MyViewClickListener.LIKE){
            newPostsViewModel.sendLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.DISLIKE){
            newPostsViewModel.removeLikedPost(selectedArticle.getId());
        }else if(viewType == MyViewClickListener.POST){
            if(selectedArticle.getArticleType().equalsIgnoreCase("facebook")){
                newPostsViewModel.sendSharedPost(selectedArticle.getId(),"com.facebook.katana");
            }else if(selectedArticle.getArticleType().equalsIgnoreCase("twitter")){
                newPostsViewModel.sendSharedPost(selectedArticle.getId(),"com.twitter.android");
            }

            Intent viewArticleIntent = new Intent(getActivity(),ViewArticleActivity.class);
            viewArticleIntent.putExtra("article_id",selectedArticle.getId());
            startActivity(viewArticleIntent);
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
        newPostsFragmentBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        if(isSuccess){
            sharePost(getLocalBitmapUri(bitmap));
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

    private void sharePost(final File imageFile) {
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
        shareDialog.show(getActivity().getSupportFragmentManager(), shareDialog.getTag());
        shareDialog.setOnDismissListener(new MyDialogDismissListener() {
            @Override
            public void onDismiss(String selectedPlatform) {
                if(selectedPlatform.equalsIgnoreCase("exit") && isSharingSuccess){
                    isSharingSuccess = false;

                    if(sharedOnThreeSM){
                        SharedPostsModel.getSharedPostsInstance().getArticlesList().add(0,selectedArticle);
                        ((HomeActivity)getActivity()).moveTabs(2);
                        UserDetailsModel.getInstance().setSharedArticlesCount(UserDetailsModel.getInstance().getSharedArticlesCount() + 1);
                    }else{
                        UnSharedPostsModel.getUnSharedPostsModel().getArticlesList().add(0,selectedArticle);
                        ((HomeActivity)getActivity()).moveTabs(1);
                        UserDetailsModel.getInstance().setPartialSharedCount(UserDetailsModel.getInstance().getPartialSharedCount() + 1);
                    }

                    newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList().remove(selectedArticle);
                    newPostsFragmentBinding.rvNewPosts.getAdapter().notifyDataSetChanged();
                    newPostsFragmentBinding.rvNewPosts.smoothScrollToPosition(0);
                    UserDetailsModel.getInstance().setNewArticlesCount(UserDetailsModel.getInstance().getNewArticlesCount() - 1);
                    ((HomeActivity)getActivity()).homeTabsAdapter.updateTitles(true);
                }else if(!selectedPlatform.equalsIgnoreCase("exit")){
                    NewPostsFragment.this.selectedPlatform = selectedPlatform;
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
                        ShareCallBackListener.registerCallback(NewPostsFragment.this);
                        startActivity(Intent.createChooser(sendIntent, mFirebaseRemoteConfig.getString("send_to"), pendingIntent.getIntentSender()));

                    }else{
                        Toast.makeText(getActivity(), mFirebaseRemoteConfig.getString("error_occured"), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onShareSuccess(String selectedPlatform) {
        if(selectedPlatform != null){
            isSharingSuccess = true;
            newPostsViewModel.sendSharedPost(selectedArticle.getId(),selectedPlatform);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(newPostsFragmentBinding != null &&
                newPostsFragmentBinding.rvNewPosts.getAdapter() != null){
            newPostsFragmentBinding.rvNewPosts.getAdapter().notifyDataSetChanged();

            if(newPostsViewModel.getNewPostsModelLiveData().getValue().getArticlesList().size() > 0){
                newPostsFragmentBinding.tvNoContent.setVisibility(View.GONE);
            }else{
                newPostsFragmentBinding.tvNoContent.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void pushNotificationArticle(ShareFromNotification shareFromNotification) {
        pushNotificationArticleId = shareFromNotification.getArticleId();
        selectedPlatform = shareFromNotification.getPlatform();
        markNotificationArticleAsShared();
    }

    @Subscribe
    public void refreshNewPosts(RefreshNewPosts refreshNewPosts) {
        newPostsViewModel.getNewPosts();
    }

    public static class RefreshNewPosts {
        public RefreshNewPosts() {
        }
    }
}
