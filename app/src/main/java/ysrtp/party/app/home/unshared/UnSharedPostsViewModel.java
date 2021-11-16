package ysrtp.party.app.home.unshared;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import ysrtp.party.app.home.FavArticleStatus;
import ysrtp.party.app.home.SingleArticle;
import ysrtp.party.app.home.sharedialog.SharedArticleStatus;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;

public class UnSharedPostsViewModel extends AndroidViewModel implements ServerResponseListener {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<UnSharedPostsModel> unSharedPostsLiveData = new MutableLiveData<>();
    private MutableLiveData<FavArticleStatus> favArticleStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<SharedArticleStatus> sharedArticleStatusMutableLiveData = new MutableLiveData<>();
    private String selectedPlatform;


    public UnSharedPostsViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        getUnsharedPosts();
    }

    private void getUnsharedPosts() {
        RequestModel unSharedPostsModel = new RequestModel();
        unSharedPostsModel.setURL(Constants.getInstance().getUnsharedPostsUrl());
        unSharedPostsModel.setRequestType(unSharedPostsModel.GET);

        ServerRequest favPostsRequest = new ServerRequest(context);
        favPostsRequest.sendRequest(unSharedPostsModel,this,Constants.UNSHARED_POSTS);
    }

    void sendLikedPost(int articleId) {
        RequestModel likedPostModel = new RequestModel();
        likedPostModel.setURL(Constants.getInstance().getSendLikeUrl().replace(":article_id",articleId+""));
        likedPostModel.setRequestType(likedPostModel.POST);

        ServerRequest sendLikeRequest = new ServerRequest(context);
        sendLikeRequest.sendRequest(likedPostModel,this,Constants.SEND_LIKE);
    }


    void removeLikedPost(int articleId) {
        RequestModel dislikePostModel = new RequestModel();
        dislikePostModel.setURL(Constants.getInstance().getRemoveLikeUrl().replace(":article_id",articleId+""));
        dislikePostModel.setRequestType(dislikePostModel.DELETE);

        ServerRequest removeLikeRequest = new ServerRequest(context);
        removeLikeRequest.sendRequest(dislikePostModel,this,Constants.REMOVE_LIKE);
    }

    void sendSharedPost(int article_ids,String source) {
        selectedPlatform = source;
        RequestModel sharedPostModel = new RequestModel();
        try {
            sharedPostModel.setURL(Constants.getInstance().getMarkSharedUrl().replace(":article_ids",article_ids+"")
                    .replace(":source",source)
                    .replace(":application",URLEncoder.encode(getApplicationName(source), "UTF-8"))
                    .replace(" ","%20"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sharedPostModel.setRequestType(sharedPostModel.POST);

        ServerRequest sharedPostRequest = new ServerRequest(context);
        sharedPostRequest.sendRequest(sharedPostModel,this,Constants.MARK_AS_SHARED);
    }



    private String getApplicationName(String packageName){
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess()){
            if (type == Constants.UNSHARED_POSTS && responseModel.getStatusCode() == 200){
                UnSharedPostsModel unSharedPostsModel = new Gson().fromJson(responseModel.getPayload(),UnSharedPostsModel.class);
                UnSharedPostsModel.setUnSharedPostsModel(unSharedPostsModel);
                unSharedPostsLiveData.postValue(UnSharedPostsModel.getUnSharedPostsModel());
            }else if(type == Constants.SEND_LIKE && responseModel.getStatusCode() == 200){
                FavArticleStatus favArticleStatus = new Gson().fromJson(responseModel.getPayload(),FavArticleStatus.class);
                favArticleStatusLiveData.postValue(favArticleStatus);
            }else if(type == Constants.REMOVE_LIKE && responseModel.getStatusCode() == 200){
                FavArticleStatus favArticleStatus = new Gson().fromJson(responseModel.getPayload(),FavArticleStatus.class);
                favArticleStatusLiveData.postValue(favArticleStatus);
            }else if(type == Constants.MARK_AS_SHARED && responseModel.getStatusCode() == 200){
                SharedArticleStatus sharedArticleStatus = new Gson().fromJson(responseModel.getPayload(),SharedArticleStatus.class);
                sharedArticleStatus.setSelectedPlatform(selectedPlatform);
                sharedArticleStatusMutableLiveData.postValue(sharedArticleStatus);
            }
        }else {
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_SHORT).show();
        }
    }

    MutableLiveData<UnSharedPostsModel> getUnSharedPostsLiveData() {
        return unSharedPostsLiveData;
    }

    MutableLiveData<FavArticleStatus> getFavArticleStatusLiveData() {
        return favArticleStatusLiveData;
    }


    MutableLiveData<SharedArticleStatus> getSharedArticleStatusLiveData() {
        return sharedArticleStatusMutableLiveData;
    }

    void sortPosts(String type) {
        Collections.sort(UnSharedPostsModel.getUnSharedPostsModel().getArticlesList(), new Comparator<SingleArticle>() {
            @Override
            public int compare(SingleArticle o1, SingleArticle o2) {
                int a1 = 1,a2 = 1;
                if(type.equalsIgnoreCase("facebook")){
                    a1 = o1.isSharedOnFacebook() ? 1 : 0;
                    a2 = o2.isSharedOnFacebook() ? 1 : 0;
                }else if(type.equalsIgnoreCase("twitter")){
                    a1 = o1.isSharedOnTwitter() ? 1 : 0;
                    a2 = o2.isSharedOnTwitter() ? 1 : 0;
                }else if(type.equalsIgnoreCase("whatsapp")){
                    a1 = o1.isSharedOnWhatsapp() ? 1 : 0;
                    a2 = o2.isSharedOnWhatsapp() ? 1 : 0;
                }
                return a1 - a2;
            }
        });
        unSharedPostsLiveData.postValue(UnSharedPostsModel.getUnSharedPostsModel());
    }
}