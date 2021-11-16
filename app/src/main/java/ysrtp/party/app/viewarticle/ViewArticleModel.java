package ysrtp.party.app.viewarticle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ysrtp.party.app.home.SingleArticle;

public class ViewArticleModel {
    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("article")
    @Expose
    private SingleArticle article;

    public int getResponseCode() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }

    public SingleArticle getArticle() {
        return article;
    }
}
