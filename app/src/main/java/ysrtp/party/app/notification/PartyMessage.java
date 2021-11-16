
package ysrtp.party.app.notification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PartyMessage {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("large")
    @Expose
    private String large;
    @SerializedName("medium")
    @Expose
    private String medium;
    @SerializedName("small")
    @Expose
    private String small;
    @SerializedName("original")
    @Expose
    private String original;
    @SerializedName("published_at")
    @Expose
    private String publishedAt;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("content")
    @Expose
    private String content;

    public int getId() {
        return id;
    }

    public String getLarge() {
        return large;
    }

    public String getMedium() {
        return medium;
    }

    public String getSmall() {
        return small;
    }

    public String getOriginal() {
        return original;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
