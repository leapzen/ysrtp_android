package ysrtp.party.app.profile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;
import ysrtp.party.app.verifyotp.VerifyOtpStatus;

public class ProfileViewModel extends AndroidViewModel implements ServerResponseListener {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<MembersRegisterStatus> logoutStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<UserDetailsModel> userDetailsLiveData = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    void updateProfile(String name) {
        RequestModel userProfileModel = new RequestModel();
        userProfileModel.setURL(Constants.getInstance().getUpdateProfileUrl()
                .replace(":name",name)
                .replace(" ","%20"));
        userProfileModel.setRequestType(userProfileModel.POST);

        ServerRequest userProfileRequest = new ServerRequest(context);
        userProfileRequest.sendRequest(userProfileModel,this,Constants.UPDATE_PROFILE);
    }

    void updateProfilePicture(String imagePath) {
        new ServerRequest(context).sendMultiPartRequest(this,imagePath,Constants.UPDATE_PROFILE_PIC);
    }

    void sendLogout() {
        RequestModel logoutModel = new RequestModel();
        logoutModel.setURL(Constants.getInstance().getLogoutUrl().replace(":fcm_token",new SessionManager(context).getFcmToken()));
        logoutModel.setRequestType(logoutModel.POST);

        ServerRequest logoutRequest = new ServerRequest(context);
        logoutRequest.sendRequest(logoutModel,this,Constants.LOGOUT);
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess()){
            if(type == Constants.UPDATE_PROFILE && responseModel.getStatusCode() == 200){
                VerifyOtpStatus verifyOtpStatus = new Gson().fromJson(responseModel.getPayload(),VerifyOtpStatus.class);
                if(verifyOtpStatus.getResponseCode() == 1){
                    UserDetailsModel.setInstance(verifyOtpStatus.getUserDetailsModel());
                    userDetailsLiveData.postValue(UserDetailsModel.getInstance());
                }
                Toast.makeText(context, verifyOtpStatus.getMessage(), Toast.LENGTH_SHORT).show();
            }else if(type == Constants.UPDATE_PROFILE_PIC && responseModel.getStatusCode() == 200){
                final UpdatePicModel updatePicModel = new Gson().fromJson(responseModel.getPayload(),UpdatePicModel.class);
                if(updatePicModel.getResponse_code() == 1){
                    UserDetailsModel.getInstance().setPicLarge(updatePicModel.getPicLarge());
                    UserDetailsModel.getInstance().setPicMedium(updatePicModel.getPicMedium());
                    UserDetailsModel.getInstance().setPicSmall(updatePicModel.getPicSmall());
                    userDetailsLiveData.postValue(UserDetailsModel.getInstance());
                }
                Toast.makeText(context, updatePicModel.getMessage(), Toast.LENGTH_SHORT).show();
            }else if(type == Constants.LOGOUT && responseModel.getStatusCode() == 200 ){
                MembersRegisterStatus membersRegisterStatus = new Gson().fromJson(responseModel.getPayload(),MembersRegisterStatus.class);
                logoutStatusLiveData.postValue(membersRegisterStatus);
            }
        }else{
            logoutStatusLiveData.postValue(null);
        }
    }

     MutableLiveData<MembersRegisterStatus> getLogoutStatusLiveData() {
        return logoutStatusLiveData;
    }

    MutableLiveData<UserDetailsModel> getUserDetailsLiveData() {
        return userDetailsLiveData;
    }
}
