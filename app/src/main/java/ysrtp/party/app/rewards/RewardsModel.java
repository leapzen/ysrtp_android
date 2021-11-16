package ysrtp.party.app.rewards;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RewardsModel {

    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("shared_info")
    @Expose
    private String shared_info;
    @SerializedName("likes_info")
    @Expose
    private String likes_info;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("rewards_info")
    @Expose
    private String rewardsInfo;
    @SerializedName("new_articles_count")
    @Expose
    private String newArticlesCount;
    @SerializedName("not_shared_facebook_count")
    @Expose
    private int notSharedFacebookCount;
    @SerializedName("not_shared_twitter_count")
    @Expose
    private int notSharedTwitterCount;
    @SerializedName("not_shared_whatsapp_count")
    @Expose
    private int notSharedWhatsappCount;
    @SerializedName("reward_conditions_link")
    @Expose
    private String rewardConditionsLink;

    public int getResponse_code() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }

    public String getShared_info() {
        return shared_info;
    }

    public String getLikes_info() {
        return likes_info;
    }

    public String getSummary() {
        return summary;
    }

    public String getNotSharedFacebookCount() {
        return String.valueOf(notSharedFacebookCount);
    }

    public String getNotSharedTwitterCount() {
        return String.valueOf(notSharedTwitterCount);
    }

    public String getNotSharedWhatsappCount() {
        return String.valueOf(notSharedWhatsappCount);
    }

    public String getNewArticlesCount() {
        return newArticlesCount;
    }

    public String getRewardsInfo() {
        return rewardsInfo;
    }

    public String getRewardConditionsLink() {
        return rewardConditionsLink;
    }
}
