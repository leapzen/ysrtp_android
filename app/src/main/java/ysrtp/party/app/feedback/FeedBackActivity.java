package ysrtp.party.app.feedback;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.databinding.ActivityFeedBackBinding;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.splash.SplashActivity;

public class FeedBackActivity extends PartyActivity {
private ActivityFeedBackBinding feedBackBinding;
private FeedBackViewModel feedBackViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedBackBinding = DataBindingUtil.setContentView(FeedBackActivity.this,R.layout.activity_feed_back);
        feedBackViewModel = new ViewModelProvider(this).get(FeedBackViewModel.class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        feedBackBinding.etName.setText(UserDetailsModel.getInstance().getName());
        feedBackBinding.layoutLoading.getRoot().setVisibility(View.GONE);

        feedBackViewModel.getMembersRegisterStatusLiveData().observe(FeedBackActivity.this, new Observer<MembersRegisterStatus>() {
            @Override
            public void onChanged(@Nullable MembersRegisterStatus membersRegisterStatus) {
                feedBackBinding.layoutLoading.getRoot().setVisibility(View.GONE);
                if(membersRegisterStatus != null){
                    Toast.makeText(FeedBackActivity.this, membersRegisterStatus.getMessage(), Toast.LENGTH_SHORT).show();
                    FeedBackActivity.this.finish();
                }else{
                    Intent splashIntent = new Intent(FeedBackActivity.this, SplashActivity.class);
                    startActivity(splashIntent);
                    FeedBackActivity.this.finishAffinity();
                }
            }
        });
    }

    private void submitFeedBack() {
        if(TextUtils.isEmpty(feedBackBinding.etComments.getText().toString().trim())){
            feedBackBinding.etComments.setError(FirebaseRemoteConfig.getInstance().getString("comments_required"));
            feedBackBinding.etComments.requestFocus();
            return;
        }else if(feedBackBinding.etComments.getText().toString().length() < 2){
            feedBackBinding.etComments.setError(FirebaseRemoteConfig.getInstance().getString("min_comments_required"));
            feedBackBinding.etComments.requestFocus();
        }
        feedBackBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        feedBackViewModel.submitFeedBack(feedBackBinding.etComments.getText().toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single, menu);
        MenuItem getItem = menu.findItem(R.id.action_single_item);
        if (getItem != null) {
            getItem.setVisible(true);
            TextView tvLogout = (TextView) getItem.getActionView();
            tvLogout.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
            tvLogout.setText(R.string.submit);
            tvLogout.setPadding(0,0,20,0);
            tvLogout.setTextSize(18f);
            tvLogout.setTypeface(Typeface.DEFAULT_BOLD);
            tvLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Utils(FeedBackActivity.this).closeKeyboard(view);
                    submitFeedBack();
                }
            });
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            FeedBackActivity.this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }


}
