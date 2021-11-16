package ysrtp.party.app;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.multidex.MultiDexApplication;


import java.util.Objects;


public class BaseApp extends MultiDexApplication {
    Context context;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {

            if(isUIThread()) {
                invokeSplashActivity(e);
            }else{
                new Handler(Looper.getMainLooper()).post(() -> invokeSplashActivity(e));
            }
        });
    }

    public boolean isUIThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private void invokeSplashActivity(Throwable e) {
        Intent intent = new Intent ();
        intent.putExtra("from_activity","app_crash");
        intent.putExtra("STACK_TRACE", Log.getStackTraceString(e));
        intent.putExtra("LOGCAT", Objects.requireNonNull(e.getCause()).getMessage());
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction ("ysrtp.party.app.splash.SplashActivity");
        startActivity(intent);
        System.exit(1);
    }
}
