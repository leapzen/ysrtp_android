package ysrtp.party.app.membersregister;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.databinding.ActivityRegisterBinding;
import ysrtp.party.app.splash.AppPreferencesModel;
import ysrtp.party.app.verifyotp.VerifyOtpActivity;
import ysrtp.party.app.webview.WebViewActivity;


public class MembersRegisterActivity extends PartyActivity implements View.OnTouchListener {
    private static final int SMS_PERMISSION = 555;
    private ActivityRegisterBinding activityRegisterBinding;
    private MembersRegisterViewModel membersRegisterViewModel;
    private MasterListsModel masterListsModel;
    private int districtPosition;
    private int constituencyPosition;
    private int blockPosition;
    private int selectedPanchayatId = 0;
    private int selectedBlockId;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String selectedDistrict= "";
    private String selectedConstituency= "";
    private String selectedBlock= "";
    private String selectedPanchayat= "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        activityRegisterBinding = DataBindingUtil.setContentView(MembersRegisterActivity.this, R.layout.activity_register);
        activityRegisterBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        activityRegisterBinding.tvTnc.setText(Html.fromHtml(mFirebaseRemoteConfig.getString("tv_tnc")));
        activityRegisterBinding.tilSeven.setHint(FirebaseRemoteConfig.getInstance().getString("referred_hint"));
        activityRegisterBinding.tilSeven.setBoxBackgroundColor(Color.parseColor(mFirebaseRemoteConfig.getString("referred_by_color")));
        activityRegisterBinding.tvReferrerTitle.setText(mFirebaseRemoteConfig.getString("referred_title"));
        
        activityRegisterBinding.tvTnc.setOnClickListener(v -> {
            Intent webView = new Intent(MembersRegisterActivity.this, WebViewActivity.class);
            webView.putExtra("type","tnc");
            webView.putExtra("url",AppPreferencesModel.getAppPreferencesInstance().getAppPrefs().getTncLink());
            startActivity(webView);
        });

        membersRegisterViewModel = new ViewModelProvider(MembersRegisterActivity.this).get(MembersRegisterViewModel.class);

        membersRegisterViewModel.getMasterListsLiveData().observe(MembersRegisterActivity.this, masterListsModel -> {
            MembersRegisterActivity.this.masterListsModel = masterListsModel;
            activityRegisterBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        });

        membersRegisterViewModel.getLoginStatusLiveData().observe(MembersRegisterActivity.this, membersRegisterStatus -> {
            activityRegisterBinding.layoutLoading.getRoot().setVisibility(View.GONE);
            if (membersRegisterStatus != null) {
                new Utils(MembersRegisterActivity.this).showSnackBar(membersRegisterStatus.getMessage());
                if (membersRegisterStatus.getResponseCode() == 1) {
                    Intent otpIntent = new Intent(MembersRegisterActivity.this, VerifyOtpActivity.class);
                    otpIntent.putExtra("mobile", getIntent().getStringExtra("mobile"));
                    otpIntent.putExtra("name", activityRegisterBinding.etName.getText().toString());
                    otpIntent.putExtra("selectedPanchayatId", selectedPanchayatId);
                    otpIntent.putExtra("selectedWardId", activityRegisterBinding.etWard.getText().toString());
                    otpIntent.putExtra("selectedBlockId", selectedBlockId);
                    otpIntent.putExtra("district_name", selectedDistrict);
                    otpIntent.putExtra("constituency_name", selectedConstituency);
                    otpIntent.putExtra("panchayat_name", selectedPanchayat);
                    otpIntent.putExtra("block_name", selectedBlock);
                    startActivity(otpIntent);
                }
            }
        });
        activityRegisterBinding.etDistrict.setOnTouchListener(this);
        activityRegisterBinding.etConstituency.setOnTouchListener(this);
        activityRegisterBinding.etBlock.setOnTouchListener(this);
        activityRegisterBinding.etPanchayat.setOnTouchListener(this);
        activityRegisterBinding.btnRegister.setOnTouchListener(this);

        activityRegisterBinding.etName.setOnEditorActionListener((v, actionId, event) -> {
            new Utils(MembersRegisterActivity.this).closeKeyboard(v);
            openDistrictDialog();
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        new Utils(MembersRegisterActivity.this).closeKeyboard(v);
        if(event.getAction() == KeyEvent.ACTION_UP){
            switch (v.getId()){
                case R.id.et_district:
                    openDistrictDialog();
                    break;
                case R.id.et_constituency:
                    openConstituencyDialog();
                    break;
                case R.id.et_block:
                    openBlockDialog();
                    break;
                case R.id.et_panchayat:
                    openPanchayatDialog();
                    break;
                case R.id.btn_register:
                    registerUser();
                    break;
            }
        }
        return true;
    }

    private void openPanchayatDialog() {
        if(TextUtils.isEmpty(activityRegisterBinding.etDistrict.getText())){
            activityRegisterBinding.etDistrict.requestFocus();
            activityRegisterBinding.etDistrict.setError(mFirebaseRemoteConfig.getString("district_required"));
        }else if( TextUtils.isEmpty(activityRegisterBinding.etConstituency.getText())){
            activityRegisterBinding.etConstituency.requestFocus();
            activityRegisterBinding.etConstituency.setError(mFirebaseRemoteConfig.getString("constituency_required"));
        }else if( TextUtils.isEmpty(activityRegisterBinding.etBlock.getText())){
            activityRegisterBinding.etBlock.requestFocus();
            activityRegisterBinding.etBlock.setError(mFirebaseRemoteConfig.getString("block_required"));
        }else{
            int size = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                    .get(constituencyPosition).getBlocksList().get(blockPosition).getGrampanchayatList().size();

            String[] panchayatNames = new String[size];
            for(int i = 0;i < size;i++){
                panchayatNames[i] = masterListsModel.getDistrictsList().get(districtPosition)
                        .getConstituencyList().get(constituencyPosition).getBlocksList()
                        .get(blockPosition).getGrampanchayatList().get(i).getName();
            }

            final ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(MembersRegisterActivity.this, android.R.layout.select_dialog_item);
            arrayAdapter.addAll(panchayatNames);

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MembersRegisterActivity.this);
            builderSingle.setTitle(mFirebaseRemoteConfig.getString("select_panchayat"));

            builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                String strName = arrayAdapter.getItem(which);
                activityRegisterBinding.etPanchayat.setText(strName);
                activityRegisterBinding.etPanchayat.setError(null);
                activityRegisterBinding.etPanchayat.requestFocus();
                selectedPanchayatId = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                        .get(constituencyPosition).getBlocksList().get(blockPosition).getGrampanchayatList().get(which).getId();

                selectedPanchayat = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                        .get(constituencyPosition).getBlocksList().get(blockPosition).getGrampanchayatList().get(which).getNormalizedName();
            });
            builderSingle.show();
        }
    }

    private void openBlockDialog() {
        if(TextUtils.isEmpty(activityRegisterBinding.etDistrict.getText())){
            activityRegisterBinding.etDistrict.requestFocus();
            activityRegisterBinding.etDistrict.setError(mFirebaseRemoteConfig.getString("district_required"));
        }else if( TextUtils.isEmpty(activityRegisterBinding.etConstituency.getText())){
            activityRegisterBinding.etConstituency.requestFocus();
            activityRegisterBinding.etConstituency.setError(mFirebaseRemoteConfig.getString("constituency_required"));
        }else{
            int size = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                    .get(constituencyPosition).getBlocksList().size();

            String[] blockNames = new String[size];
            for(int i = 0;i < size;i++){
                blockNames[i] = masterListsModel.getDistrictsList().get(districtPosition)
                        .getConstituencyList().get(constituencyPosition).getBlocksList().get(i).getName();
            }

            final ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(MembersRegisterActivity.this, android.R.layout.select_dialog_item);
            arrayAdapter.addAll(blockNames);

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MembersRegisterActivity.this);
            builderSingle.setTitle(mFirebaseRemoteConfig.getString("select_block"));

            builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                String strName = arrayAdapter.getItem(which);
                blockPosition = which;
                activityRegisterBinding.etBlock.setText(strName);
                activityRegisterBinding.etBlock.setError(null);
                activityRegisterBinding.etBlock.requestFocus();
                activityRegisterBinding.etPanchayat.setText(null);

                if(masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                        .get(constituencyPosition).getBlocksList().get(blockPosition).getGrampanchayatList().size() < 1){
                    activityRegisterBinding.tilFive.setVisibility(View.GONE);
                    activityRegisterBinding.tilSix.setVisibility(View.GONE);
                }else{
                    activityRegisterBinding.tilFive.setVisibility(View.VISIBLE);
                    activityRegisterBinding.tilSix.setVisibility(View.VISIBLE);
                }

                selectedBlockId = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                        .get(constituencyPosition).getBlocksList().get(blockPosition).getId();

                selectedBlock = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList()
                        .get(constituencyPosition).getBlocksList().get(blockPosition).getNormalizedName();

            });
            builderSingle.show();
        }
    }

    private void openConstituencyDialog() {
        if(TextUtils.isEmpty(activityRegisterBinding.etDistrict.getText())){
            activityRegisterBinding.etDistrict.requestFocus();
            activityRegisterBinding.etDistrict.setError(mFirebaseRemoteConfig.getString("district_required"));
        }else{
            int size = masterListsModel.getDistrictsList().get(districtPosition).getConstituencyList().size();
            String[] constituencyNames = new String[size];
            for(int i = 0;i < size;i++){
                constituencyNames[i] = masterListsModel.getDistrictsList().get(districtPosition)
                        .getConstituencyList().get(i).getName();
            }

            final ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(MembersRegisterActivity.this, android.R.layout.select_dialog_item);
            arrayAdapter.addAll(constituencyNames);

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MembersRegisterActivity.this);
            builderSingle.setTitle(mFirebaseRemoteConfig.getString("select_constituency"));

            builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                String strName = arrayAdapter.getItem(which);
                constituencyPosition = which;
                activityRegisterBinding.etConstituency.setText(strName);
                activityRegisterBinding.etConstituency.setError(null);
                activityRegisterBinding.etConstituency.requestFocus();
                activityRegisterBinding.etBlock.setText(null);
                activityRegisterBinding.etPanchayat.setText(null);

                selectedConstituency = masterListsModel.getDistrictsList().get(districtPosition)
                        .getConstituencyList().get(which).getNormalizedName();

            });
            builderSingle.show();
        }
    }

    private void openDistrictDialog() {
        String[] districtNames = new String[masterListsModel.getDistrictsList().size()];
        for(int i = 0;i < masterListsModel.getDistrictsList().size();i++){
            districtNames[i] = masterListsModel.getDistrictsList().get(i).getName();
        }

        final ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(MembersRegisterActivity.this, android.R.layout.select_dialog_item);
        arrayAdapter.addAll(districtNames);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MembersRegisterActivity.this);
        builderSingle.setTitle(mFirebaseRemoteConfig.getString("select_district"));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                districtPosition = which;
                activityRegisterBinding.etDistrict.setText(strName);
                activityRegisterBinding.etDistrict.setError(null);
                activityRegisterBinding.etDistrict.requestFocus();
                activityRegisterBinding.etConstituency.setText(null);
                activityRegisterBinding.etBlock.setText(null);
                activityRegisterBinding.etPanchayat.setText(null);
                selectedDistrict = masterListsModel.getDistrictsList().get(which).getNormalizedName();
            }
        });
        builderSingle.show();
    }

    private void registerUser() {
        if(TextUtils.isEmpty(activityRegisterBinding.etName.getText())){
            activityRegisterBinding.etName.setError(mFirebaseRemoteConfig.getString("name_required"));
            activityRegisterBinding.etName.requestFocus();
        }else if(TextUtils.getTrimmedLength(activityRegisterBinding.etName.getText()) < 3){
            activityRegisterBinding.etName.setError(mFirebaseRemoteConfig.getString("name_min_char"));
            activityRegisterBinding.etName.requestFocus();
        }else if(TextUtils.isEmpty(activityRegisterBinding.etDistrict.getText())){
            activityRegisterBinding.etDistrict.setError(mFirebaseRemoteConfig.getString("district_required"));
            activityRegisterBinding.etDistrict.requestFocus();
        }else if(TextUtils.isEmpty(activityRegisterBinding.etConstituency.getText())){
            activityRegisterBinding.etConstituency.setError(mFirebaseRemoteConfig.getString("constituency_required"));
            activityRegisterBinding.etConstituency.requestFocus();
        }else if(TextUtils.isEmpty(activityRegisterBinding.etBlock.getText())){
            activityRegisterBinding.etBlock.setError(mFirebaseRemoteConfig.getString("block_required"));
            activityRegisterBinding.etBlock.requestFocus();
        }else if(activityRegisterBinding.tilSix.getVisibility() == View.VISIBLE &&
                TextUtils.isEmpty(activityRegisterBinding.etPanchayat.getText())){
            activityRegisterBinding.etPanchayat.setError(mFirebaseRemoteConfig.getString("panchayat_required"));
            activityRegisterBinding.etPanchayat.requestFocus();
        }else if(activityRegisterBinding.etReferredBy.getText().length() > 1
                && activityRegisterBinding.etReferredBy.getText().length() < 10 ){
            activityRegisterBinding.etReferredBy.setError(mFirebaseRemoteConfig.getString("referred_required"));
            activityRegisterBinding.etReferredBy.requestFocus();
        }else if(activityRegisterBinding.etReferredBy.getText().length() > 1
                && !isValidMobile(activityRegisterBinding.etReferredBy.getText().toString())){
            activityRegisterBinding.etReferredBy.setError(mFirebaseRemoteConfig.getString("mobile_invalid"));
            activityRegisterBinding.etReferredBy.requestFocus();
        }else if(activityRegisterBinding.etWard.getText().toString().equalsIgnoreCase("0")||
                activityRegisterBinding.etWard.getText().toString().equalsIgnoreCase("00")){
            activityRegisterBinding.etWard.setError(mFirebaseRemoteConfig.getString("ward_required"));
            activityRegisterBinding.etWard.requestFocus();
        }else{
            continueToVerifyOtp();
//            int permissionCheck = ContextCompat.checkSelfPermission(MembersRegisterActivity.this,
//                    Manifest.permission.RECEIVE_SMS);
//            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
//                askSmsPermission();
//            }else{
//                continueToVerifyOtp();
//            }
        }
    }

    private void askSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION);
    }

    private boolean isValidMobile(String phone) {
        int i = Character.getNumericValue(phone.charAt(0));
        return i >= 6;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION) {
            continueToVerifyOtp();
        }
    }

    private void continueToVerifyOtp() {
        activityRegisterBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        membersRegisterViewModel.requestOtp(selectedPanchayatId,selectedBlockId, activityRegisterBinding.etWard.getText().toString(),
                activityRegisterBinding.etName.getText().toString(),
                getIntent().getStringExtra("mobile"));
    }

    @Override
    protected void onPause() {
        activityRegisterBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        super.onPause();
    }


}