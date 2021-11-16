package ysrtp.party.app.mlapostings;


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
import ysrtp.party.app.home.sharedialog.SharedArticleStatus;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class MlaPostingsViewModel extends AndroidViewModel implements ServerResponseListener {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<MlaPostingsModel> mlaPostingsLiveData = new MutableLiveData<>();
    private MutableLiveData<FavArticleStatus> favArticleStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<SharedArticleStatus> sharedArticleStatusLiveData = new MutableLiveData<>();
    private String selectedPlatform;

    public MlaPostingsViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    void getMlaPostings() {
        RequestModel mlaPostsModel = new RequestModel();
        mlaPostsModel.setURL(Constants.getInstance().getMlaPostsUrl());
        mlaPostsModel.setRequestType(mlaPostsModel.GET);

        ServerRequest mlaPostsRequest = new ServerRequest(context);
        mlaPostsRequest.sendRequest(mlaPostsModel,this,Constants.MLA_POSTS);
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
                    .replace(":application", URLEncoder.encode(getApplicationName(source), "UTF-8"))
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
            if(type == Constants.MLA_POSTS && responseModel.getStatusCode() == 200){
                MlaPostingsModel mlaPostingsModel = new Gson().fromJson(responseModel.getPayload(), MlaPostingsModel.class);
                mlaPostingsLiveData.postValue(mlaPostingsModel);
            }else if(type == Constants.SEND_LIKE && responseModel.getStatusCode() == 200){
                FavArticleStatus favArticleStatus = new Gson().fromJson(responseModel.getPayload(),FavArticleStatus.class);
                favArticleStatusLiveData.postValue(favArticleStatus);
            }else if(type == Constants.REMOVE_LIKE && responseModel.getStatusCode() == 200){
                FavArticleStatus favArticleStatus = new Gson().fromJson(responseModel.getPayload(),FavArticleStatus.class);
                favArticleStatusLiveData.postValue(favArticleStatus);
            }else if(type == Constants.MARK_AS_SHARED && responseModel.getStatusCode() == 200){
                SharedArticleStatus sharedArticleStatus = new Gson().fromJson(responseModel.getPayload(),SharedArticleStatus.class);
                sharedArticleStatus.setSelectedPlatform(selectedPlatform);
                sharedArticleStatusLiveData.postValue(sharedArticleStatus);
            }
        }else {
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_SHORT).show();
        }
    }

    MutableLiveData<MlaPostingsModel> getMlaPostingsLiveData() {
        return mlaPostingsLiveData;
    }

    MutableLiveData<FavArticleStatus> getFavArticleStatusLiveData() {
        return favArticleStatusLiveData;
    }

    MutableLiveData<SharedArticleStatus> getSharedArticleStatusLiveData() {
        return sharedArticleStatusLiveData;
    }
}

