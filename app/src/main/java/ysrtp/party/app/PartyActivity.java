package ysrtp.party.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.common.model.RedirectLogin;
import ysrtp.party.app.network.NetworkConnection;
import ysrtp.party.app.splash.SplashActivity;


public class PartyActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiver ;
    private int firstTime;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "testing");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                firstTime++;
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null
                        && activeNetwork.isConnectedOrConnecting();
                if (firstTime > 1) {
                    EventBus.getDefault().post(new NetworkConnection(isConnected));
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mReceiver, intentFilter);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onNetworkConnectionChanged(NetworkConnection networkConnection) {
        if (networkConnection.isNetworkConnected()) {
            new Utils(PartyActivity.this).showSnackBar(FirebaseRemoteConfig.getInstance().getString("network_connected"));
        } else {
            new Utils(PartyActivity.this).showSnackBar(FirebaseRemoteConfig.getInstance().getString("network_not_connected"));
        }
    }

    @Subscribe
    public void onAuthFailure(RedirectLogin redirectLogin) {
        if(redirectLogin.isRedirectToLogin()){
            Toast.makeText(this, FirebaseRemoteConfig.getInstance().getString("session_expired"), Toast.LENGTH_SHORT).show();
            new SessionManager(PartyActivity.this).clearSession();
            Intent startIntent = new Intent(PartyActivity.this, SplashActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
            PartyActivity.this.finishAffinity();

        }
    }
}