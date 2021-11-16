package ysrtp.party.app.home.sharedialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.EXTRA_CHOSEN_COMPONENT;

public class ShareCallBackListener extends BroadcastReceiver {

    private static ShareCallBack shareCallBack;

    public interface ShareCallBack {
        void onShareSuccess(String selectedPlatform);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String selectedAppPackage = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            selectedAppPackage = String.valueOf(intent.getExtras().get(EXTRA_CHOSEN_COMPONENT));
            selectedAppPackage = selectedAppPackage.substring(selectedAppPackage.indexOf("{")+1,selectedAppPackage.indexOf("/"));
            ShareCallBackListener.shareCallBack.onShareSuccess(selectedAppPackage);

        }
    }

    public static void registerCallback(ShareCallBack shareCallBack) {
        ShareCallBackListener.shareCallBack = shareCallBack;
    }
}
