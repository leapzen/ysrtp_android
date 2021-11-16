package ysrtp.party.app.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import ysrtp.party.app.R;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.splash.SplashActivity;

import org.json.JSONObject;

public class PushMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    private Uri sound;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
        new SessionManager(this).storeFcmToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                handleDataMessage(json);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getBody());
        }

    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);

        sound =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/"+R.raw.pitta);
        try {
            Ringtone r = RingtoneManager.getRingtone(this, sound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"PARTY_CHANNEL")
                .setLargeIcon(bitmap)
                .setContentTitle("PARTY")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(null)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("PARTY_CHANNEL",
                    "PARTY",
                    NotificationManager.IMPORTANCE_DEFAULT);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(null,attributes);
            channel.enableLights(true);
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


//  image: "#{image}", title: "#{title}", content: "#{content}" ,
//            description: "" , article_id: "#{article_id}", play_sound: "#{play_sound}", youtube_id: "#{youtube_id}",
//            notification_type: "article" } }.to_json

    private void handleDataMessage(JSONObject json) {
        try {
            String title = json.optString("title","PARTY APP");
            String imageUrl = json.optString("image","");
            String description = json.optString("description","");
            String subText = json.optString("sub_text","");
            boolean setSound = json.optBoolean("play_sound",true);
            String articleId = json.optString("article_id","0");
            String ysrtpMessageId = json.optString("ysrtp_message_id","0");
            String articleType = json.optString("article_type","");
            String socialUrl = json.optString("social_url","");

            String notificationType = json.optString("notification_type","normal");

            Intent resultIntent = new Intent();
            resultIntent.setClass(getApplicationContext(), SplashActivity.class);
            resultIntent.putExtra("from_activity", "pushNotification");
            if(notificationType.equalsIgnoreCase("article")){
                resultIntent.putExtra("article_id", Integer.parseInt(articleId));
                resultIntent.putExtra("article_type", articleType);
                resultIntent.putExtra("social_url", socialUrl);
            }

            resultIntent.putExtra("ysrtp_message_id", Integer.parseInt(ysrtpMessageId));
            resultIntent.putExtra("notification_type", notificationType);

            showNotificationMessage(getApplicationContext(), title, description,subText,setSound,resultIntent,imageUrl);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotificationMessage(Context context, String title, String description,String subText,boolean setSound,Intent intent, String imageUrl) {
        Notifications notificationUtils = new Notifications(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, description,subText,setSound,intent,imageUrl);
    }
}
