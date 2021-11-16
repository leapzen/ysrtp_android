package ysrtp.party.app.home.unshared;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ysrtp.party.app.home.SingleArticle;

import java.util.ArrayList;
import java.util.List;

public class UnSharedPostsModel {
    private static UnSharedPostsModel unSharedPostsModel = new UnSharedPostsModel();
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

    public static UnSharedPostsModel getUnSharedPostsModel() {
        return unSharedPostsModel;
    }

    public static void setUnSharedPostsModel(UnSharedPostsModel unSharedPostsModel) {
        UnSharedPostsModel.unSharedPostsModel = unSharedPostsModel;
    }

    private UnSharedPostsModel() {
    }
}
