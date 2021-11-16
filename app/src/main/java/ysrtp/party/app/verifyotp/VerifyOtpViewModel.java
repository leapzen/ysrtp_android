package ysrtp.party.app.verifyotp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.gson.Gson;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;

public class VerifyOtpViewModel extends AndroidViewModel implements ServerResponseListener {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<MembersRegisterStatus> registerStatusMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<VerifyOtpStatus> verifyOtpStatusLiveData = new MutableLiveData<>();


    public VerifyOtpViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        checkFCMToken();
    }

    private void checkFCMToken() {
        if(new SessionManager(context).getFcmToken().length() < 5){
            FirebaseInstallations.getInstance().getToken(true)
                    .addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                            if (task.isSuccessful()) {
                                new SessionManager(context).storeFcmToken(task.getResult().getToken());
                            }
                        }
                    });
        }
    }

    void resendOTP(int selectedPanchayatId, int selectedBlockId, String wardId, String name, String mobile) {
        RequestModel requestOtpModel = new RequestModel();
        requestOtpModel.setURL(Constants.getInstance().getRequestOtpUrl().replace(":grampanchayat_id",selectedPanchayatId+"")
                .replace(":ward_id",wardId)
                .replace(":block_id",selectedBlockId+"")
                .replace(":name",name).replace(":mobile",mobile).replace(" ","%20"));
        requestOtpModel.setRequestType(requestOtpModel.POST);

        ServerRequest otpRequest = new ServerRequest(context);
        otpRequest.sendRequest(requestOtpModel,this,Constants.REQUEST_OTP);
    }

    void submitOtp(String userMobileNo, String otpCode) {
        Log.e("submitOtp: ", otpCode+"+"+userMobileNo);
        RequestModel requestOtpModel = new RequestModel();
        requestOtpModel.setURL(Constants.getInstance().getVerifyOtpUrl()
                .replace(":otp",otpCode).replace(":mobile",userMobileNo)
                .replace(":fcm_token",new SessionManager(context).getFcmToken())
                .replace(":topic",new SessionManager(context).getSubscribedTopics()));
//        if(AppPreferencesModel.getAppPreferencesInstance().getAppPrefs().isSubscribedToTopic()){
//            requestOtpModel.setURL(requestOtpModel.getURL().replace(":topic",
//                    AppPreferencesModel.getAppPreferencesInstance().getAppPrefs().getNotificationTopic()));
//        }else{
//            requestOtpModel.setURL(requestOtpModel.getURL().replace(":topic","NA"));
//        }
        requestOtpModel.setURL(requestOtpModel.getURL().replace(" ","%20"));

        requestOtpModel.setRequestType(requestOtpModel.PUT);

        ServerRequest otpRequest = new ServerRequest(context);
        otpRequest.sendRequest(requestOtpModel,this,Constants.VERIFY_OTP);
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess() && responseModel.getStatusCode() == 200){
            if(type == Constants.REQUEST_OTP){
                MembersRegisterStatus membersRegisterStatus = new Gson().fromJson(responseModel.getPayload(),MembersRegisterStatus.class);
                registerStatusMutableLiveData.postValue(membersRegisterStatus);
                Toast.makeText(context, membersRegisterStatus.getMessage(), Toast.LENGTH_SHORT).show();
            }else if(type == Constants.VERIFY_OTP){
                VerifyOtpStatus verifyOtpStatus = new Gson().fromJson(responseModel.getPayload(),VerifyOtpStatus.class);
                verifyOtpStatusLiveData.postValue(verifyOtpStatus);
            }
        }else{
            verifyOtpStatusLiveData.postValue(null);
        }

    }

    MutableLiveData<VerifyOtpStatus> getVerifyOtpStatusLiveData() {
        return verifyOtpStatusLiveData;
    }

    MutableLiveData<MembersRegisterStatus> getRegisterStatusMutableLiveData() {
        return registerStatusMutableLiveData;
    }
}

