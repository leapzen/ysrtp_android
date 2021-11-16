package ysrtp.party.app.feedback;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FeedBackViewModel extends AndroidViewModel implements ServerResponseListener {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<MembersRegisterStatus> MembersRegisterStatusLiveData = new MutableLiveData<>();

    public FeedBackViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    void submitFeedBack(String feedback) {
        RequestModel feedbackModel = new RequestModel();
        String encodedMessage;
        try {
            encodedMessage = URLEncoder.encode(feedback, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedMessage = "UnsupportedEncodingException";
            e.printStackTrace();
        }
        feedbackModel.setURL(Constants.getInstance().getFeedbackUrl().replace(":message", encodedMessage));
        feedbackModel.setRequestType(feedbackModel.POST);

        ServerRequest feedbackRequest = new ServerRequest(context);
        feedbackRequest.sendRequest(feedbackModel,this,Constants.FEEDBACK);
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess()){
            MembersRegisterStatus membersRegisterStatus = new Gson().fromJson(responseModel.getPayload(),MembersRegisterStatus.class);
            MembersRegisterStatusLiveData.postValue(membersRegisterStatus);
        }else {
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_SHORT).show();
        }
    }

    MutableLiveData<MembersRegisterStatus> getMembersRegisterStatusLiveData() {
        return MembersRegisterStatusLiveData;
    }
}
