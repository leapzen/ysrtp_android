package ysrtp.party.app.splash;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import ysrtp.party.app.common.CrashLog;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.network.RequestModel;
import ysrtp.party.app.network.ResponseModel;
import ysrtp.party.app.network.ServerRequest;
import ysrtp.party.app.network.ServerResponseListener;
import ysrtp.party.app.verifyotp.VerifyOtpStatus;

public class SplashViewModel extends AndroidViewModel implements ServerResponseListener {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<AppPreferencesModel> appPreferencesLiveData = new MutableLiveData<>();
    private MutableLiveData<UserDetailsModel> userDetailsLiveData = new MutableLiveData<>();
    private MutableLiveData<VerifyMobileModel> verifyMobileLiveData = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }


    public void getAppPrefs() {
        RequestModel appPrefsModel = new RequestModel();
        appPrefsModel.setURL(Constants.getInstance().getAppPrefsUrl());
        appPrefsModel.setRequestType(appPrefsModel.GET);

        ServerRequest appPrefsRequest = new ServerRequest(context);
        appPrefsRequest.sendRequest(appPrefsModel,this,Constants.APP_PREFS);
    }

    public void getUserProfile() {
        RequestModel userProfileModel = new RequestModel();
        userProfileModel.setURL(Constants.getInstance().getGetProfileUrl());
        userProfileModel.setRequestType(userProfileModel.POST);

        ServerRequest userProfileRequest = new ServerRequest(context);
        userProfileRequest.sendRequest(userProfileModel,this,Constants.GET_PROFILE);
    }


    public void verifyMobile(String mobile) {
        RequestModel verifyMobileModel = new RequestModel();
        verifyMobileModel.setURL(Constants.getInstance().getVerifyMobileUrl().replace(":mobile",mobile));
        verifyMobileModel.setRequestType(verifyMobileModel.POST);

        ServerRequest verifyMobileRequest = new ServerRequest(context);
        verifyMobileRequest.sendRequest(verifyMobileModel,this,Constants.VERIFY_MOBILE);
    }

    public void sendErrorDetails(CrashLog crashLog) {
        RequestModel crashModel = new RequestModel();
        crashModel.setURL(Constants.getInstance().getCrashLogUrl());
        crashModel.setRequestType(crashModel.POST);
        crashModel.setPayload(new Gson().toJson(crashLog));

        ServerRequest appPrefsRequest = new ServerRequest(context);
        appPrefsRequest.sendRequest(crashModel,this,Constants.CRASH_LOG);

    }


    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess()){
            if(type == Constants.APP_PREFS && responseModel.getStatusCode() == 200){
                AppPreferencesModel appPreferencesModel = new Gson().fromJson(responseModel.getPayload(),AppPreferencesModel.class);
                AppPreferencesModel.setAppPreferencesInstance(appPreferencesModel);
                appPreferencesLiveData.postValue(appPreferencesModel);
            }else if(type == Constants.GET_PROFILE){
                if(responseModel.getStatusCode() == 200){
                    VerifyOtpStatus verifyOtpStatus = new Gson().fromJson(responseModel.getPayload(),VerifyOtpStatus.class);
                    if(verifyOtpStatus.getResponseCode() == 1){
                        userDetailsLiveData.postValue(verifyOtpStatus.getUserDetailsModel());
                    }else{
                        userDetailsLiveData.postValue(null);
                    }
                }else{
                    userDetailsLiveData.postValue(null);
                }
            }else if(type == Constants.VERIFY_MOBILE){
                if(responseModel.getStatusCode() == 200){
                    VerifyMobileModel verifyMobileModel = new Gson().fromJson(responseModel.getPayload(), VerifyMobileModel.class);
                    verifyMobileLiveData.postValue(verifyMobileModel);
                }
            }else{
                appPreferencesLiveData.postValue(null);
            }

        }else{
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_LONG).show();
        }

    }

    public MutableLiveData<AppPreferencesModel> getAppPreferencesLiveData() {
        return appPreferencesLiveData;
    }

    public MutableLiveData<UserDetailsModel> getUserDetailsLiveData() {
        return userDetailsLiveData;
    }

    public MutableLiveData<VerifyMobileModel> getVerifyMobileData() {
        return verifyMobileLiveData;
    }
}
