package ysrtp.party.app.viewarticle;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;

public class ViewArticleViewModel extends AndroidViewModel implements ServerResponseListener {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<ViewArticleModel> viewArticleLiveData = new MutableLiveData<>();


    public ViewArticleViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    void getArticleDetails(int articleId) {
        RequestModel articleDetailsModel = new RequestModel();
        articleDetailsModel.setURL(Constants.getInstance().getPostDetailsUrl().replace(":id",articleId+""));
        articleDetailsModel.setRequestType(articleDetailsModel.GET);

        ServerRequest otpRequest = new ServerRequest(context);
        otpRequest.sendRequest(articleDetailsModel,this,Constants.POST_DETAILS);

    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess() && responseModel.getStatusCode() == 200) {
            ViewArticleModel viewArticleModel = new Gson().fromJson(responseModel.getPayload(),ViewArticleModel.class);
            viewArticleLiveData.postValue(viewArticleModel);
        }
    }

    MutableLiveData<ViewArticleModel> getViewArticleLiveData() {
        return viewArticleLiveData;
    }
}
