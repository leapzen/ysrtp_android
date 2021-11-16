package ysrtp.party.app.verifyotp;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.databinding.ActivityVerifyOtpBinding;
import ysrtp.party.app.home.HomeActivity;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.membersregister.UserDetailsModel;

import java.util.ArrayList;
import java.util.List;

public class VerifyOtpActivity extends PartyActivity   {
    private ActivityVerifyOtpBinding activityVerifyOtpBinding;
    private VerifyOtpViewModel verifyOtpViewModel;
    private String userMobile="";
    private String userName="";
    private String selectedWardId ="";
    private int selectedPanchayatId;
    private int selectedBlockId;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
//    private SmsReceiver smsReceiver = new SmsReceiver();
    private int finalI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        activityVerifyOtpBinding = DataBindingUtil.setContentView(VerifyOtpActivity.this,R.layout.activity_verify_otp);
        verifyOtpViewModel = new ViewModelProvider(VerifyOtpActivity.this).get(VerifyOtpViewModel.class);
        activityVerifyOtpBinding.layoutLoading.getRoot().setVisibility(View.GONE);

        userMobile = getIntent().getStringExtra("mobile");
        userName = getIntent().getStringExtra("name");
        selectedPanchayatId = getIntent().getIntExtra("selectedPanchayatId",0);
        selectedBlockId = getIntent().getIntExtra("selectedBlockId",0);
        selectedWardId = getIntent().getStringExtra("selectedWardId");
        userMobile = getIntent().getStringExtra("mobile");
        activityVerifyOtpBinding.tvOtpTitle.setText(activityVerifyOtpBinding.tvOtpTitle.getText().toString()
                .replace(":mobile", userMobile));

        verifyOtpViewModel.getVerifyOtpStatusLiveData().observe(VerifyOtpActivity.this, new Observer<VerifyOtpStatus>() {
            @Override
            public void onChanged(@Nullable VerifyOtpStatus verifyOtpStatus) {
                activityVerifyOtpBinding.layoutLoading.getRoot().setVisibility(View.GONE);
                if(verifyOtpStatus != null){
                    if(verifyOtpStatus.getResponseCode() == 1){
                        try{
//                            subscribeToTopics(verifyOtpStatus);
                            startHomeActivity(verifyOtpStatus);
                        }catch (Exception e){
                            e.printStackTrace();
                            startHomeActivity(verifyOtpStatus);
                        }
                    }else{
                        new Utils(VerifyOtpActivity.this).showSnackBar(verifyOtpStatus.getMessage());
                    }
                }else{
                    new Utils(VerifyOtpActivity.this).showSnackBar(mFirebaseRemoteConfig.getString("error_occured"));
                }
            }
        });
        verifyOtpViewModel.getRegisterStatusMutableLiveData().observe(VerifyOtpActivity.this, new Observer<MembersRegisterStatus>() {
            @Override
            public void onChanged(@Nullable MembersRegisterStatus membersRegisterStatus) {
                activityVerifyOtpBinding.layoutLoading.getRoot().setVisibility(View.GONE);
            }
        });
    }

    private void startHomeActivity(VerifyOtpStatus verifyOtpStatus) {
        activityVerifyOtpBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        new Utils(VerifyOtpActivity.this).showSnackBar(verifyOtpStatus.getMessage());

        UserDetailsModel.setInstance(verifyOtpStatus.getUserDetailsModel());
        new SessionManager(VerifyOtpActivity.this).createSession(verifyOtpStatus.getUserDetailsModel().getUniqueId());
        Intent homeIntent = new Intent(VerifyOtpActivity.this,HomeActivity.class);
        startActivity(homeIntent);
        VerifyOtpActivity.this.finishAffinity();
    }

    private void subscribeToTopics(VerifyOtpStatus verifyOtpStatus) {
        final List<String> topicNames = new ArrayList<>();
        topicNames.add(getIntent().getStringExtra("district_name"));
        topicNames.add(getIntent().getStringExtra("constituency_name"));
        if(getIntent().getStringExtra("panchayat_name").length() > 0){
            topicNames.add(getIntent().getStringExtra("panchayat_name"));
        }
        topicNames.add(getIntent().getStringExtra("block_name"));


        for (int i = 0; i < topicNames.size(); i++) {
            new SessionManager(VerifyOtpActivity.this).setSubscribedTopics(topicNames.get(i));
            FirebaseMessaging.getInstance().subscribeToTopic(topicNames.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        new SessionManager(VerifyOtpActivity.this).setSubscribedTopics(topicNames.get(finalI));
                    }
                }
            });
        }
    }

    public void submitOTP(View view) {
        if(TextUtils.isEmpty(activityVerifyOtpBinding.etOtp.getText())){
            activityVerifyOtpBinding.etOtp.setError(mFirebaseRemoteConfig.getString("otp_required"));
            activityVerifyOtpBinding.etOtp.requestFocus();
            return;
        }
        new Utils(VerifyOtpActivity.this).closeKeyboard(view);
        activityVerifyOtpBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        verifyOtpViewModel.submitOtp(userMobile,activityVerifyOtpBinding.etOtp.getText().toString());
    }

    public void resendOTP(View view) {
        new Utils(VerifyOtpActivity.this).closeKeyboard(view);
        activityVerifyOtpBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        verifyOtpViewModel.resendOTP(selectedPanchayatId,selectedBlockId,selectedWardId,userName,userMobile);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        smsReceiver.setOnSmsReceivedListener(this);
//        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
//        intentFilter.setPriority(79997);
//        this.registerReceiver(smsReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        this.unregisterReceiver(smsReceiver);
    }

//    @Override
//    public void onSmsReceived(String message) {
//        activityVerifyOtpBinding.etOtp.setText(message);
//        activityVerifyOtpBinding.etOtp.requestFocus();
//        activityVerifyOtpBinding.etOtp.setSelection(message.length());
//    }
}
