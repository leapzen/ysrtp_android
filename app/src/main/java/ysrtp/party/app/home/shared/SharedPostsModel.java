package ysrtp.party.app.home.shared;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ysrtp.party.app.home.SingleArticle;

import java.util.ArrayList;
import java.util.List;

public class SharedPostsModel {
    private static SharedPostsModel sharedPostsInstance = new SharedPostsModel();
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

    public static SharedPostsModel getSharedPostsInstance() {
        return sharedPostsInstance;
    }

    public static void setSharedPostsInstance(SharedPostsModel sharedPostsInstance) {
        SharedPostsModel.sharedPostsInstance = sharedPostsInstance;
    }

    private SharedPostsModel() {
    }
}
