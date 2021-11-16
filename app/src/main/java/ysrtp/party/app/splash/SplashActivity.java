package ysrtp.party.app.splash;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.BuildConfig;
import ysrtp.party.app.R;
import ysrtp.party.app.common.CrashLog;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.databinding.ActivitySplashBinding;
import ysrtp.party.app.firebase.Notifications;
import ysrtp.party.app.home.HomeActivity;
import ysrtp.party.app.membersregister.MembersRegisterActivity;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.network.Constants;
import ysrtp.party.app.verifyotp.VerifyOtpActivity;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends PartyActivity {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SplashViewModel splashViewModel;
    private int versionCode;
    ActivitySplashBinding activitySplashBinding;
    private boolean networkCallSuccess;
    private long backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        activitySplashBinding.progressBar.setVisibility(View.GONE);
        activitySplashBinding.tilOne.setVisibility(View.GONE);
        activitySplashBinding.btnGetOtp.setVisibility(View.GONE);

        if(Constants.SERVER_INFO.contains("Development")){
            checkServerUrl();
        }else{
            afterServerUrlChecked();
        }
    }

    private void afterServerUrlChecked() {
        splashViewModel = new ViewModelProvider(SplashActivity.this).get(SplashViewModel.class);

        Animation animFadein = AnimationUtils.loadAnimation(this,
                R.anim.fade_in);
        activitySplashBinding.appLogo.setAnimation(animFadein);

        Notifications.clearNotifications(SplashActivity.this);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.default_config);

        splashViewModel.getAppPreferencesLiveData().observe(SplashActivity.this, appPreferencesModel -> {
            if(appPreferencesModel != null){
                if(checkVersion(appPreferencesModel)){
                    subscribeToGlobalTopic();
                    try {
                        fetchFirebaseForRemoteConfig();
                    }catch (Exception e){
                        e.printStackTrace();
                        continueActivity();
                    }
                }
            }
        });

        splashViewModel.getUserDetailsLiveData().observe(SplashActivity.this, userDetailsModel -> {
            if (userDetailsModel != null) {
                UserDetailsModel.setInstance(userDetailsModel);
                Intent homeIntent = new Intent(SplashActivity.this, HomeActivity.class);

                if(getIntent().getStringExtra("from_activity") != null){
                    if(getIntent().getStringExtra("from_activity").equalsIgnoreCase("pushNotification")){
                        homeIntent.putExtra("from_activity", getIntent().getStringExtra("from_activity"));
                        homeIntent.putExtra("article_id", getIntent().getIntExtra("article_id", 0));
                        homeIntent.putExtra("party_message_id", getIntent().getIntExtra("party_message_id", 0));
                        homeIntent.putExtra("article_type", getIntent().getStringExtra("article_type"));
                        homeIntent.putExtra("social_url", getIntent().getStringExtra("social_url"));
                        homeIntent.putExtra("notification_type", getIntent().getStringExtra("notification_type"));
                    }
                }
                startActivity(homeIntent);
                SplashActivity.this.finish();
            }else{
                new SessionManager(SplashActivity.this).clearSession();
                continueActivity();
            }
        });

        splashViewModel.getVerifyMobileData().observe(SplashActivity.this, verifyMobileModel -> {
            activitySplashBinding.progressBar.setVisibility(View.GONE);
            Toast.makeText(SplashActivity.this, verifyMobileModel.getMessage(), Toast.LENGTH_SHORT).show();
            if(verifyMobileModel.getResponseCode() == 1){
                if(verifyMobileModel.getUserType().equalsIgnoreCase("MLA")){
                    Intent verifyOtpIntent = new Intent(SplashActivity.this, VerifyOtpActivity.class);
                    verifyOtpIntent.putExtra("mobile", activitySplashBinding.etLoginMobile.getText().toString());
                    verifyOtpIntent.putExtra("name", verifyMobileModel.getName());
                    verifyOtpIntent.putExtra("selectedPanchayatId", verifyMobileModel.getGrampanchayatId());
                    verifyOtpIntent.putExtra("selectedWardId", verifyMobileModel.getWardId());
                    verifyOtpIntent.putExtra("selectedBlockId", verifyMobileModel.getBlockId());
                    startActivity(verifyOtpIntent);
                }else if(verifyMobileModel.getUserType().equalsIgnoreCase("new_user")){
                    Intent memberRegisterIntent = new Intent(SplashActivity.this, MembersRegisterActivity.class);
                    memberRegisterIntent.putExtra("mobile", activitySplashBinding.etLoginMobile.getText().toString());
                    startActivity(memberRegisterIntent);
                }else if(verifyMobileModel.getUserType().equalsIgnoreCase("Member")){
                    Intent verifyOtpIntent = new Intent(SplashActivity.this, VerifyOtpActivity.class);
                    verifyOtpIntent.putExtra("mobile", activitySplashBinding.etLoginMobile.getText().toString());
                    verifyOtpIntent.putExtra("name", verifyMobileModel.getName());
                    verifyOtpIntent.putExtra("selectedPanchayatId", verifyMobileModel.getGrampanchayatId());
                    verifyOtpIntent.putExtra("selectedWardId", verifyMobileModel.getWardId());
                    verifyOtpIntent.putExtra("selectedBlockId", verifyMobileModel.getBlockId());
                    startActivity(verifyOtpIntent);
                }
            }
        });

        if(getIntent().getStringExtra("from_activity") != null &&
                getIntent().getStringExtra("from_activity").equalsIgnoreCase("app_crash")){

            CrashLog crashLog = new CrashLog(""+BuildConfig.VERSION_CODE,getDeviceDetails(),
                    getIntent().getStringExtra("STACK_TRACE"),getIntent().getStringExtra("LOGCAT"));

            splashViewModel.sendErrorDetails(crashLog);

            showCrashDialog();
        }else{
            splashViewModel.getAppPrefs();
            showProgress();
        }
    }


    public static String getDeviceDetails() {
        StringBuilder deviceDetails = new StringBuilder();
        try{
            deviceDetails.append(Build.BRAND).append("::").append(Build.MODEL)
                    .append("::").append(Build.HARDWARE).append("::").append(Build.VERSION.RELEASE);
        }catch (Exception e){
            e.printStackTrace();
        }
        return deviceDetails.toString();
    }


    private void subscribeToGlobalTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(AppPreferencesModel.getAppPreferencesInstance().getAppPrefs().getNotificationTopic())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        new SessionManager(SplashActivity.this).setSubscribedTopics(AppPreferencesModel.getAppPreferencesInstance().getAppPrefs().getNotificationTopic());
                    }
                });
    }

    private boolean checkVersion(AppPreferencesModel appPreferencesModel) {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(appPreferencesModel.getAppPrefs().getStableVersion() > versionCode){
            showUpdateDialog(appPreferencesModel.getAppPrefs().isForceUpdate());
            return false;
        }
        return true;
    }

    private void showCrashDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setCancelable(false);
        builder.setTitle(FirebaseRemoteConfig.getInstance().getString("crash_info"));
        builder.setMessage(FirebaseRemoteConfig.getInstance().getString("crash_message"));
        builder.setNegativeButton("Ok", (dialog, which) -> {
            splashViewModel.getAppPrefs();
            showProgress();
            dialog.dismiss();

        });
        AlertDialog ad = builder.create();
        ad.show();
        Button negativeButton = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        negativeButton.setTextColor(Color.parseColor("#007567"));
    }

    private void showProgress() {
        new Handler().postDelayed(() -> {
            if(!networkCallSuccess)
                activitySplashBinding.progressBar.setVisibility(View.VISIBLE);
        }, 3000);
    }

    private void showUpdateDialog(boolean isForceUpdate) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SplashActivity.this);
        dialogBuilder.setTitle(mFirebaseRemoteConfig.getString("new_update_title"))
                .setMessage(mFirebaseRemoteConfig.getString("new_update_required"))
                .setPositiveButton(mFirebaseRemoteConfig.getString("update"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("playStore_redirect_url"))));
                        SplashActivity.this.finish();
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.ic_info)
                .setCancelable(!isForceUpdate);
        if(!isForceUpdate){
            dialogBuilder.setNegativeButton("cancel", (dialog, which) -> continueActivity());
        }

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        positiveButton.setTextColor(Color.parseColor("#007567"));
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        negativeButton.setTextColor(Color.parseColor("#007567"));
    }

    private void checkServerUrl() {
        SessionManager sessionManager = new SessionManager(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("URL Checkpoint");

        LayoutInflater inflater = LayoutInflater.from(SplashActivity.this);
        View customView = inflater.inflate(R.layout.dialog_editbox, null);

        ((TextInputLayout) customView.findViewById(R.id.til_user_input)).setHint("Change the URL to working server");

        TextInputEditText mainUrl = (TextInputEditText) customView.findViewById(R.id.et_user_input);
        mainUrl.setText(sessionManager.getServerUrl());

        builder.setView(customView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            sessionManager.saveServerUrl(mainUrl.getText().toString());
            dialog.dismiss();
            afterServerUrlChecked();
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    private void continueActivity() {
        if(new SessionManager(SplashActivity.this).checkSession()){
            splashViewModel.getUserProfile();
        }else{
//            Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
//            startActivity(loginIntent);
//            SplashActivity.this.finish();
            networkCallSuccess = true;
            activitySplashBinding.tilOne.setVisibility(View.VISIBLE);
            activitySplashBinding.btnGetOtp.setVisibility(View.VISIBLE);
            activitySplashBinding.progressBar.setVisibility(View.GONE);
        }
    }

    private void fetchFirebaseForRemoteConfig() {
        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mFirebaseRemoteConfig.activate();
                    }
                    continueActivity();
                });
    }

    public void verifyMobile(View view) {
        if(TextUtils.isEmpty(activitySplashBinding.etLoginMobile.getText())){
            activitySplashBinding.etLoginMobile.setError(mFirebaseRemoteConfig.getString("mobile_required"));
            activitySplashBinding.etLoginMobile.requestFocus();
        }else if(TextUtils.getTrimmedLength(activitySplashBinding.etLoginMobile.getText()) < 10){
            activitySplashBinding.etLoginMobile.setError(mFirebaseRemoteConfig.getString("mobile_digits"));
            activitySplashBinding.etLoginMobile.requestFocus();
        }
        else if(!isValidMobile(activitySplashBinding.etLoginMobile.getText().toString())){
            activitySplashBinding.etLoginMobile.setError(mFirebaseRemoteConfig.getString("mobile_invalid"));
            activitySplashBinding.etLoginMobile.requestFocus();
        }else if(!TextUtils.isDigitsOnly(activitySplashBinding.etLoginMobile.getText())){
            activitySplashBinding.etLoginMobile.setError(mFirebaseRemoteConfig.getString("mobile_digits"));
            activitySplashBinding.etLoginMobile.requestFocus();
        }
        else{
            activitySplashBinding.progressBar.setVisibility(View.VISIBLE);
            splashViewModel.verifyMobile(activitySplashBinding.etLoginMobile.getText().toString());
        }
    }

    private boolean isValidMobile(String phone) {
        int i = Character.getNumericValue(phone.charAt(0));
        return i >= 6;
    }

    @Override
    public void onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        }else {
            Toast.makeText(getBaseContext(), mFirebaseRemoteConfig.getString("press_back_to_exit"), Toast.LENGTH_LONG).show();
            backPressed = System.currentTimeMillis();
        }
    }
}
