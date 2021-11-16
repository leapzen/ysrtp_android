package ysrtp.party.app.network;

public class Constants {
    private static final Constants constantsInstance = new Constants();

    private Constants() {
    }

    public static Constants getInstance() {
        return constantsInstance;
    }

    /*Development */
//    private String SERVER_URL;
//    public static final String SERVER_INFO = "Development";

    /*Production */
    private String SERVER_URL = "https://cms.teamyssr.com/api/";
    public static final String SERVER_INFO = "Production";


    public void setServerUrl(String SERVER_URL) {
        this.SERVER_URL = SERVER_URL;
    }

    public String getCrashLogUrl() {
        return SERVER_URL+"v1/errors";
    }

    public String getAppPrefsUrl() {
        return SERVER_URL+"v1/application_preferences";
    }

    public String getMasterListsUrl() {
        return SERVER_URL+"v1/master_lists";
    }

    public String getRequestOtpUrl() {
        return SERVER_URL+"v1/sessions/request_otp?block_id=:block_id&grampanchayat_id=:grampanchayat_id&ward_id=:ward_id&name=:name&mobile=:mobile&referred_mobile=:referred_mobile";
    }

    public String getVerifyOtpUrl() {
        return SERVER_URL+"v1/sessions/verify_otp?otp=:otp&mobile=:mobile&fcm_token=:fcm_token&topic=:topic";
    }

    public String getGetProfileUrl() {
        return SERVER_URL+"v1/users/update_profile";
    }

    public String getNewPostsUrl() {
        return SERVER_URL+"v1/articles/new_articles";
    }

    public String getSharedPostsUrl() {
        return SERVER_URL+"v1/articles/fully_shared";
    }

    public String getUnsharedPostsUrl() {
        return SERVER_URL+"v1/articles/partially_shared";
    }

    public String getLikedPostsUrl() {
        return SERVER_URL+"v1/articles/favourites";
    }

    public String getSendLikeUrl() {
        return SERVER_URL+"v1/articles/add_to_favourites?id=:article_id";
    }

    public String getRemoveLikeUrl() {
        return SERVER_URL+"v1/articles/remove_from_favourites?id=:article_id";
    }

    public String getMarkSharedUrl() {
        return SERVER_URL+"v1/articles/mark_articles_as_shared?article_ids=:article_ids&source=:source&application=:application";
    }

    public String getUpdateProfilePicUrl() {
        return SERVER_URL+"v1/users/update_profile_picture";
    }

    public String getUpdateProfileUrl() {
        return SERVER_URL+"v1/users/update_profile?name=:name";
    }

    public String getFeedbackUrl() {
        return SERVER_URL+"v1/users/feedback?message=:message";
    }

    public String getRewardsUrl() {
        return SERVER_URL+"v1/users/my_points";
    }

    public String getPostDetailsUrl() {
        return SERVER_URL+"v1/articles/view?id=:id";
    }

    public String getLogoutUrl() {
        return SERVER_URL+"v1/users/logout?fcm_token=:fcm_token";
    }

    public String getPushNotificationClickedUrl() {
        return SERVER_URL+"v1/users/clicked_notification?notification_type=:notification_type&article_id=:article_id&biju_message_id=:biju_message_id";
    }

    public String getYsrtpMessagesUrl() {
        return SERVER_URL+"v1/biju_messages/new_biju_messages";
    }

    public String getVerifyMobileUrl() {
        return SERVER_URL+"v1/sessions/verify_mobile?mobile=:mobile";
    }

    public String getMlaPostsUrl() {
        return SERVER_URL+"v1/articles/my_mla_postings";
    }

    public String getMlaCreatePostsUrl() {
        return SERVER_URL.replace("api/","")+"articles/new_mla_posting";
    }


    public static final int APP_PREFS = 1;
    public static final int MASTER_LISTS = 2;
    public static final int REQUEST_OTP = 3;
    public static final int VERIFY_OTP = 4;
    public static final int GET_PROFILE = 5;
    public static final int NEW_POSTS = 6;
    public static final int SHARED_POSTS = 7;
    public static final int UNSHARED_POSTS = 8;
    public static final int LIKED_POSTS = 8;
    public static final int SEND_LIKE = 9;
    public static final int REMOVE_LIKE = 10;
    public static final int MARK_AS_SHARED = 11;
    public static final int UPDATE_PROFILE_PIC = 12;
    public static final int UPDATE_PROFILE = 13;
    public static final int FEEDBACK = 14;
    public static final int REWARDS = 15;
    public static final int POST_DETAILS = 16;
    public static final int LOGOUT = 17;
    public static final int PUSH_NOTIFICATION_CLICKED = 18;
    public static final int YSRTP_MESSAGES = 19;
    public static final int CRASH_LOG = 20;
    public static final int VERIFY_MOBILE = 21;
    public static final int MLA_POSTS = 22;




}
