package ysrtp.party.app.home;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FavArticleStatus {

    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("is_favourite")
    @Expose
    private boolean isFavourite;

    public int getResponseCode() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFavourite() {
        return isFavourite;
    }
}
