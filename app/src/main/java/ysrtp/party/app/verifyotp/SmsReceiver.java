package ysrtp.party.app.verifyotp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;


public class SmsReceiver extends BroadcastReceiver {
    private OnSmsReceivedListener listener;
    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                if (pdusObj != null) {
                    for (Object aPdusObj : pdusObj) {
                        try {
                            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                            String senderAddress = currentMessage.getDisplayOriginatingAddress().trim();
                            String message = currentMessage.getDisplayMessageBody().trim();

                            if (senderAddress.contains(FirebaseRemoteConfig.getInstance().getString("sms_header"))) {
                                String verificationCode = message.replaceAll("[^0-9]", "");

                                if (listener != null) {
                                    listener.onSmsReceived(verificationCode);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnSmsReceivedListener(Context context) {
        this.listener = (OnSmsReceivedListener) context;
    }

    public interface OnSmsReceivedListener {
        void onSmsReceived(String message);
    }
}
