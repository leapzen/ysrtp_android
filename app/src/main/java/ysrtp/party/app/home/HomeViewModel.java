package ysrtp.party.app.home;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;


public class HomeViewModel extends AndroidViewModel implements ServerResponseListener {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    public HomeViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    void clickedPushNotification(String notificationType, int articleId, int ysrtpMsgId) {
        RequestModel pushNotifyModel = new RequestModel();
        pushNotifyModel.setURL(Constants.getInstance().getPushNotificationClickedUrl()
                .replace(":article_id",articleId+"")
                .replace(":ysrtp_message_id", ysrtpMsgId +"")
        .replace(":notification_type",notificationType));
        pushNotifyModel.setRequestType(pushNotifyModel.POST);

        ServerRequest pushNotifyRequest = new ServerRequest(context);
        pushNotifyRequest.sendRequest(pushNotifyModel,this,Constants.PUSH_NOTIFICATION_CLICKED);
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
    }
}
