package ysrtp.party.app.home;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ArticleImages implements Parcelable {
    @SerializedName("original")
    @Expose
    private String original;
    @SerializedName("large")
    @Expose
    private String large;
    @SerializedName("medium")
    @Expose
    private String medium;
    @SerializedName("small")
    @Expose
    private String small;

    public String getOriginal() {
        return original;
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

    public void setOriginal(String original) {
        this.original = original;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public void setSmall(String small) {
        this.small = small;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.original);
        dest.writeString(this.large);
        dest.writeString(this.medium);
        dest.writeString(this.small);
    }

    public ArticleImages() {
    }

    public ArticleImages(String original, String large, String medium, String small) {
        this.original = original;
        this.large = large;
        this.medium = medium;
        this.small = small;
    }

    protected ArticleImages(Parcel in) {
        this.original = in.readString();
        this.large = in.readString();
        this.medium = in.readString();
        this.small = in.readString();
    }

    public static final Creator<ArticleImages> CREATOR = new Creator<ArticleImages>() {
        @Override
        public ArticleImages createFromParcel(Parcel source) {
            return new ArticleImages(source);
        }

        @Override
        public ArticleImages[] newArray(int size) {
            return new ArticleImages[size];
        }
    };
}
