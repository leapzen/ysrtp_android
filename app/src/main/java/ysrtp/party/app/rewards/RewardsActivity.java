package ysrtp.party.app.rewards;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.databinding.ActivityRewardBinding;
import ysrtp.party.app.splash.SplashActivity;

public class RewardsActivity extends PartyActivity {

    private ActivityRewardBinding ActivityRewardBinding;
    private FirebaseRemoteConfig firebaseRemoteConfig =FirebaseRemoteConfig.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRewardBinding = DataBindingUtil.setContentView(RewardsActivity.this,R.layout.activity_reward);
        ActivityRewardBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        ActivityRewardBinding.layoutTableUnshared.setVisibility(View.GONE);
        ActivityRewardBinding.holderNewArticle.setVisibility(View.GONE);


        ActivityRewardBinding.tvShareNowFb.setText(Html.fromHtml(firebaseRemoteConfig.getString("share_now")));
        ActivityRewardBinding.tvShareNowTwitter.setText(Html.fromHtml(firebaseRemoteConfig.getString("share_now")));
        ActivityRewardBinding.tvShareNowWhatsapp.setText(Html.fromHtml(firebaseRemoteConfig.getString("share_now")));
        ActivityRewardBinding.tvShareNowNewArticle.setText(Html.fromHtml(firebaseRemoteConfig.getString("share_now")));

        setSupportActionBar(ActivityRewardBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RewardsViewModel rewardsViewModel = new ViewModelProvider(RewardsActivity.this).get(RewardsViewModel.class);
        rewardsViewModel.getRewardsLiveData().observe(RewardsActivity.this, new Observer<RewardsModel>() {
            @Override
            public void onChanged(final @Nullable RewardsModel rewardsModel) {
                ActivityRewardBinding.layoutLoading.getRoot().setVisibility(View.GONE);
                ActivityRewardBinding.layoutTableUnshared.setVisibility(View.VISIBLE);
                ActivityRewardBinding.holderNewArticle.setVisibility(View.VISIBLE);

                if(rewardsModel != null){
                    ActivityRewardBinding.setRewards(rewardsModel);
                    ActivityRewardBinding.tvRewardsInfo.setText(Html.fromHtml(rewardsModel.getRewardsInfo()));
                    ActivityRewardBinding.webView.loadData(rewardsModel.getRewardsInfo(),"text/html", "UTF-8");
                    ActivityRewardBinding.tvShareNowFb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("type","facebook");
                            setResult(RESULT_OK,returnIntent);
                            RewardsActivity.this.finish();
                        }
                    });

                    ActivityRewardBinding.tvShareNowTwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("type","twitter");
                            setResult(RESULT_OK,returnIntent);
                            RewardsActivity.this.finish();
                        }
                    });

                    ActivityRewardBinding.tvShareNowWhatsapp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("type","whatsapp");
                            setResult(RESULT_OK,returnIntent);
                            RewardsActivity.this.finish();
                        }
                    });

                    ActivityRewardBinding.holderNewArticle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("type","new");
                            setResult(RESULT_OK,returnIntent);
                            RewardsActivity.this.finish();
                        }
                    });
                }else{
                    Intent splashIntent = new Intent(RewardsActivity.this, SplashActivity.class);
                    startActivity(splashIntent);
                    RewardsActivity.this.finishAffinity();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            RewardsActivity.this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
