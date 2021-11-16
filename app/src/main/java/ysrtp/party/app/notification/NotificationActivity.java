package ysrtp.party.app.notification;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.databinding.ActivityNotificationBinding;

public class NotificationActivity extends PartyActivity {
NotificationViewModel notificationViewModel;
ActivityNotificationBinding notificationBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationBinding = DataBindingUtil.setContentView(this,R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        notificationBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        notificationViewModel.getNotificationLiveData().observe(this, new Observer<NotificationModel>() {
            @Override
            public void onChanged(@Nullable NotificationModel notificationModel) {
                notificationBinding.layoutLoading.getRoot().setVisibility(View.GONE);
                if(notificationModel != null){
                    if(notificationModel.getResponseCode() == 1 && notificationModel.getTotalCount() > 0){
                        notificationBinding.tvNoContent.setVisibility(View.GONE);
                        notificationBinding.rvNotifications.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
                        notificationBinding.rvNotifications.setAdapter(
                                new NotificationsAdapter(notificationModel.getPartyMessages()));
                    }else{
                        notificationBinding.tvNoContent.setVisibility(View.VISIBLE);
                        notificationBinding.tvNoContent.setText(FirebaseRemoteConfig.getInstance().getString("no_notifications"));
                    }
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
            NotificationActivity.this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
