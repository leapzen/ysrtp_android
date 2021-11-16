package ysrtp.party.app.firebase;

import android.app.Notification;
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
import android.util.Patterns;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ysrtp.party.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class Notifications {
    private static int Unique_Integer_Number;
    private Context mContext;
    private Uri sound;

    Notifications(Context mContext) {
        this.mContext = mContext;
        sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + mContext.getPackageName() + "/"+R.raw.pitta);
    }

    void showNotificationMessage(final String title, final String description, final String subText, boolean setSound, Intent intent, String imageUrl) {
        Unique_Integer_Number = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        // Check for empty push message
        if (description == null)
            return;
        // notification icon

        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        Unique_Integer_Number,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext,"PARTY_CHANNEL");

        if (imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
            Bitmap bitmap = getBitmapFromURL(imageUrl);
            if (bitmap != null) {
                showBigNotification(bitmap,mBuilder,title,description,subText,resultPendingIntent );
            } else {
                showSmallNotification(mBuilder,title,description,subText,resultPendingIntent );
            }
        } else {
            showSmallNotification(mBuilder,title,description,subText,resultPendingIntent);
        }

        if(setSound && NotificationManagerCompat.from(mContext).areNotificationsEnabled()){
            playNotificationSound();
        }
    }

    private void showSmallNotification(NotificationCompat.Builder mBuilder, String title, String description, String subText, PendingIntent resultPendingIntent) {

        Notification notification;
        notification = mBuilder.setTicker(title).setWhen(0)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(description)
                .setSubText(subText)
                .setSound(null)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
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
        notificationManager.notify(Unique_Integer_Number, notification);
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, String title,String subText, String description, PendingIntent resultPendingIntent) {
        Notification notification = mBuilder
                .setTicker(title)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setSound(null)
                .setContentText(description)
                .setSubText(subText)
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),  R.mipmap.ic_launcher))/*Notification icon image*/
                .build();


        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("PARTY_CHANNEL",
                    "PARTY",
                    NotificationManager.IMPORTANCE_DEFAULT);

            // Creating an Audio Attribute
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            channel.setSound(null,audioAttributes);
            channel.enableLights(true);
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(Unique_Integer_Number, notification);


//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//
//            try{
//            List<NotificationChannel> channelList = null;
//                channelList = notificationManager.getNotificationChannels();
//
//            for (int i = 0; channelList != null && i < channelList.size(); i++) {
//
//                notificationManager.deleteNotificationChannel(channelList.get(i).getId());
//            }
//        }catch (Exception ew){
//            ew.printStackTrace();
//        }
//        }

    }

    private void playNotificationSound() {
        try {
            Ringtone r = RingtoneManager.getRingtone(mContext, sound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }

//    /**
//     * Method checks if the app is in background or not
//     */
//    public static boolean isAppIsInBackground(Context context) {
//        boolean isInBackground = true;
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
//                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    for (String activeProcess : processInfo.pkgList) {
//                        if (activeProcess.equals(context.getPackageName())) {
//                            isInBackground = false;
//                        }
//                    }
//                }
//            }
//        } else {
//            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//            ComponentName componentInfo = taskInfo.get(0).topActivity;
//            if (componentInfo.getPackageName().equals(context.getPackageName())) {
//                isInBackground = false;
//            }
//        }
//
//        return isInBackground;
//    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Unique_Integer_Number);
    }

//    public static long getTimeMilliSec(String timeStamp) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
//        try {
//            Date date = format.parse(timeStamp);
//            return date.getTime();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }
}
