package ysrtp.party.app.splash;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppPreferencesModel {
    private static AppPreferencesModel appPreferencesInstance = new AppPreferencesModel();
    @SerializedName("app_preferences")
    @Expose
    private AppPrefs appPrefs;

    public AppPrefs getAppPrefs() {
        return appPrefs;
    }

    private AppPreferencesModel() {
    }

    public static AppPreferencesModel getAppPreferencesInstance() {
        return appPreferencesInstance;
    }

    static void setAppPreferencesInstance(AppPreferencesModel appPreferencesInstance) {
        AppPreferencesModel.appPreferencesInstance = appPreferencesInstance;
    }

    public class AppPrefs {

        @SerializedName("stable_version")
        @Expose
        int stableVersion;
        @SerializedName("force_update")
        @Expose
        boolean forceUpdate;
        @SerializedName("notification_topic")
        @Expose
        String notificationTopic;
        @SerializedName("playstore_link")
        @Expose
        String playStoreLink ;
        @SerializedName("tnc_link")
        @Expose
        String tncLink;

        public int getStableVersion() {
            return stableVersion;
        }

        public boolean isForceUpdate() {
            return forceUpdate;
        }

        public String getNotificationTopic() {
            return notificationTopic;
        }

        public String getPlayStoreLink() {
            return playStoreLink;
        }

        public String getTncLink() {
            return tncLink;
        }
    }
}
