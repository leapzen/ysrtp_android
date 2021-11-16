package ysrtp.party.app.mlapostings;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ysrtp.party.app.home.SingleArticle;

import java.util.ArrayList;
import java.util.List;

class MlaPostingsModel implements Parcelable {
    @SerializedName("total_count")
    @Expose
    private int totalCount;
    @SerializedName("articles")
    @Expose
    private List<SingleArticle> articlesList = new ArrayList<>();

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<SingleArticle> getArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<SingleArticle> articlesList) {
        this.articlesList = articlesList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.totalCount);
        dest.writeTypedList(this.articlesList);
    }

    public MlaPostingsModel() {
    }

    protected MlaPostingsModel(Parcel in) {
        this.totalCount = in.readInt();
        this.articlesList = in.createTypedArrayList(SingleArticle.CREATOR);
    }

    public static final Creator<MlaPostingsModel> CREATOR = new Creator<MlaPostingsModel>() {
        @Override
        public MlaPostingsModel createFromParcel(Parcel source) {
            return new MlaPostingsModel(source);
        }

        @Override
        public MlaPostingsModel[] newArray(int size) {
            return new MlaPostingsModel[size];
        }
    };
}
