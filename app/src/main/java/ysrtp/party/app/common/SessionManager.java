package ysrtp.party.app.common;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessaging;

import ysrtp.party.app.network.Constants;


public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "Party-Pref";
    private String ACCESS_TOKEN = "access_token";
    private String FCM_TOKEN = "fcm_token";
    private String NEW_POSTS_ALERT = "new_posts_alert";
    private String SUBSCRIBED_TOPICS = "subscribed_topics";
    private final String SERVER_URL_FROM_USER = "server_url_from_user";

    // Constructor
    public SessionManager(Context context){
        pref = context.getSharedPreferences(PREF_NAME, 0);
    }

    public void createSession(String token){
        editor = pref.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.apply();
    }

    public String getAccessToken() {
        return pref.getString(ACCESS_TOKEN, null);
    }

    public boolean checkSession(){
        return pref.getString(ACCESS_TOKEN, "").length() > 3;
    }

    public void clearSession(){
        try {
            unsubscribeTopics(getSubscribedTopics());
        }catch (Exception e){
            e.printStackTrace();
        }
        editor = pref.edit();
        editor.remove(ACCESS_TOKEN);
        editor.remove(NEW_POSTS_ALERT);
        editor.remove(SUBSCRIBED_TOPICS);
        editor.apply();
    }

    public void setSubscribedTopics(String topic){
        if(getSubscribedTopics().contains(topic)){
            return;
        }
        editor = pref.edit();
        if(getSubscribedTopics().length() == 0){
            editor.putString(SUBSCRIBED_TOPICS,topic);
        }else{
            editor.putString(SUBSCRIBED_TOPICS, getSubscribedTopics()+","+topic);
        }
        editor.apply();
    }

    public String getSubscribedTopics(){
        return pref.getString(SUBSCRIBED_TOPICS, "");
    }

    private void unsubscribeTopics(String subscribedTopics) {
        String[] topics = subscribedTopics.split(",");

        for (String topic : topics) {
            if(!topic.equalsIgnoreCase("global")){
                if( topic.length() > 2){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                }
            }
        }

    }

    public void storeFcmToken(String fcm_token) {
        editor = pref.edit();
        editor.putString(FCM_TOKEN, fcm_token);
        editor.apply();
    }

    public String getFcmToken(){
        return pref.getString(FCM_TOKEN, "na");
    }

    public boolean getNewPostsAlert(){
        return pref.getBoolean(NEW_POSTS_ALERT,false);
    }

    public void setNewPostsAlert(){
        editor = pref.edit();
        editor.putBoolean(NEW_POSTS_ALERT, true);
        editor.apply();
    }

    public void saveServerUrl(String serverUrl) {
        editor = pref.edit();
        editor.putString(SERVER_URL_FROM_USER, serverUrl);
        editor.apply();
        Constants.getInstance().setServerUrl(serverUrl);
    }

    public String getServerUrl() {
        return pref.getString(SERVER_URL_FROM_USER, "https://cms.teamyssr.com/api/");
    }

}
