package ysrtp.party.app.rewards;

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

public class RewardsViewModel extends AndroidViewModel implements ServerResponseListener {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private MutableLiveData<RewardsModel> rewardsModelMutableLiveData = new MutableLiveData<>();

    public RewardsViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        getRewards();
    }

    private void getRewards() {
        RequestModel myRewardsModel = new RequestModel();
        myRewardsModel.setURL(Constants.getInstance().getRewardsUrl());
        myRewardsModel.setRequestType(myRewardsModel.GET);

        ServerRequest myRewardsRequest = new ServerRequest(context);
        myRewardsRequest.sendRequest(myRewardsModel,this,Constants.REWARDS);

    }

    @Override
    public void getResponse(ResponseModel responseModel, int type) {
        if(responseModel.isSuccess()){
            if(type == Constants.REWARDS && responseModel.getStatusCode() == 200){
                RewardsModel rewardsModel = new Gson().fromJson(responseModel.getPayload(),RewardsModel.class);
                if(rewardsModel.getResponse_code() == 1){
                    rewardsModelMutableLiveData.postValue(rewardsModel);
                }
            }
        }else {
            Toast.makeText(context, responseModel.getPayload(), Toast.LENGTH_SHORT).show();
        }
    }

    MutableLiveData<RewardsModel> getRewardsLiveData() {
        return rewardsModelMutableLiveData;
    }
}
