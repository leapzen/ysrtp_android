package ysrtp.party.app.notification;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;

public class NotificationViewModel extends AndroidViewModel implements ServerResponseListener {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<NotificationModel> notificationLiveData = new MutableLiveData<>();

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        getNotifications();
    }

    private void getNotifications() {
        RequestModel ysrtpMessageModel = new RequestModel();
        ysrtpMessageModel.setURL(Constants.getInstance().getYsrtpMessagesUrl());
        ysrtpMessageModel.setRequestType(ysrtpMessageModel.GET);

        ServerRequest ysrtpMsgRequest = new ServerRequest(context);
        ysrtpMsgRequest.sendRequest(ysrtpMessageModel,this,Constants.YSRTP_MESSAGES);
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess()) {
            NotificationModel notificationModel = new Gson().fromJson(responseModel.getPayload(),NotificationModel.class);
            notificationLiveData.postValue(notificationModel);
        }else{
            notificationLiveData.postValue(null);
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    MutableLiveData<NotificationModel> getNotificationLiveData() {
        return notificationLiveData;
    }
}
