package ysrtp.party.app.home.newpost;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ysrtp.party.app.home.SingleArticle;

import java.util.ArrayList;
import java.util.List;

public class NewPostsModel {

    private static NewPostsModel newPostsInstance = new NewPostsModel();
    @SerializedName("total_count")
    @Expose
    private int totalCount;
    @SerializedName("articles")
    @Expose
    private List<SingleArticle> articlesList = new ArrayList<>();

    public List<SingleArticle> getArticlesList() {
        return articlesList;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public static NewPostsModel getNewPostsInstance() {
        return newPostsInstance;
    }

    public static void setNewPostsInstance(NewPostsModel newPostsInstance) {
        NewPostsModel.newPostsInstance = newPostsInstance;
    }

    private NewPostsModel() {
    }
}