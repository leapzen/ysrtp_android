package ysrtp.party.app.membersregister;

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

public class MembersRegisterViewModel extends AndroidViewModel implements ServerResponseListener {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<MasterListsModel> masterListsLiveData = new MutableLiveData<>();
    private MutableLiveData<MembersRegisterStatus> loginStatusLiveData = new MutableLiveData<>();

    public MembersRegisterViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        getMastersList();
    }

    private void getMastersList() {
        RequestModel mastersListModel = new RequestModel();
        mastersListModel.setURL(Constants.getInstance().getMasterListsUrl());
        mastersListModel.setRequestType(mastersListModel.GET);

        ServerRequest mastersListRequest = new ServerRequest(context);
        mastersListRequest.sendRequest(mastersListModel,this,Constants.MASTER_LISTS);
    }


    void requestOtp(int selectedPanchayatId, int selectedBlockId, String wardId, String name,String mobile) {
        RequestModel requestOtpModel = new RequestModel();
        requestOtpModel.setURL(Constants.getInstance().getRequestOtpUrl().replace(":grampanchayat_id",selectedPanchayatId+"")
                .replace(":ward_id",wardId)
                .replace(":block_id",selectedBlockId+"")
        .replace(":name",name).replace(":mobile",mobile).replace(" ","%20"));
        requestOtpModel.setRequestType(requestOtpModel.POST);

        ServerRequest otpRequest = new ServerRequest(context);
        otpRequest.sendRequest(requestOtpModel,this,Constants.REQUEST_OTP);
    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess() && responseModel.getStatusCode() == 200){
            if(type == Constants.MASTER_LISTS){
                MasterListsModel masterListsModel = new Gson().fromJson(responseModel.getPayload(),MasterListsModel.class);
                masterListsLiveData.postValue(masterListsModel);
            }else if(type == Constants.REQUEST_OTP){
                MembersRegisterStatus membersRegisterStatus = new Gson().fromJson(responseModel.getPayload(), MembersRegisterStatus.class);
                loginStatusLiveData.postValue(membersRegisterStatus);
            }
        }else{
            loginStatusLiveData.postValue(null);
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_SHORT).show();
        }

    }

    MutableLiveData<MasterListsModel> getMasterListsLiveData() {
        return masterListsLiveData;
    }

    MutableLiveData<MembersRegisterStatus> getLoginStatusLiveData() {
        return loginStatusLiveData;
    }
}
