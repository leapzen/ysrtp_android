package ysrtp.party.app.home;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SingleArticle implements Parcelable {

        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("article_type")
        @Expose
        private String articleType;
        @SerializedName("social_url")
        @Expose
        private String socialUrl;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("content")
        @Expose
        private String content = "";
        @SerializedName("published_at")
        @Expose
        private String published_at;
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
        @SerializedName("favourite")
        @Expose
        private boolean favourite;
        @SerializedName("new_post")
        @Expose
        private int isNewPost;
        @SerializedName("youtube_id")
        @Expose
        private String youtubeId;
        @SerializedName("likes_count")
        @Expose
        private long likesCount;
        @SerializedName("shares_count")
        @Expose
        private long sharesCount;
        @SerializedName("shared_on_whatsapp")
        @Expose
        private int sharedOnWhatsapp;
        @SerializedName("shared_on_facebook")
        @Expose
        private int sharedOnFacebook;
        @SerializedName("shared_on_twitter")
        @Expose
        private int sharedOnTwitter;
        @SerializedName("article_images")
        @Expose
        private ArrayList<ArticleImages> articleImagesList = new ArrayList<ArticleImages>();
        private boolean isBannerImageAdded;
//        @SerializedName("shared_on_instagram")
//        @Expose
//        private int sharedOnInstagram;
//        @SerializedName("shared_on_sharechat")
//        @Expose
//        private int sharedOnSharechat;

        protected SingleArticle(Parcel in) {
                id = in.readInt();
                articleType = in.readString();
                socialUrl = in.readString();
                title = in.readString();
                content = in.readString();
                published_at = in.readString();
                original = in.readString();
                large = in.readString();
                medium = in.readString();
                small = in.readString();
                favourite = in.readByte() != 0;
                isNewPost = in.readInt();
                youtubeId = in.readString();
                likesCount = in.readLong();
                sharesCount = in.readLong();
                sharedOnWhatsapp = in.readInt();
                sharedOnFacebook = in.readInt();
                sharedOnTwitter = in.readInt();
                articleImagesList = in.createTypedArrayList(ArticleImages.CREATOR);
                isBannerImageAdded = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(id);
                dest.writeString(articleType);
                dest.writeString(socialUrl);
                dest.writeString(title);
                dest.writeString(content);
                dest.writeString(published_at);
                dest.writeString(original);
                dest.writeString(large);
                dest.writeString(medium);
                dest.writeString(small);
                dest.writeByte((byte) (favourite ? 1 : 0));
                dest.writeInt(isNewPost);
                dest.writeString(youtubeId);
                dest.writeLong(likesCount);
                dest.writeLong(sharesCount);
                dest.writeInt(sharedOnWhatsapp);
                dest.writeInt(sharedOnFacebook);
                dest.writeInt(sharedOnTwitter);
                dest.writeTypedList(articleImagesList);
                dest.writeByte((byte) (isBannerImageAdded ? 1 : 0));
        }

        @Override
        public int describeContents() {
                return 0;
        }

        public static final Creator<SingleArticle> CREATOR = new Creator<SingleArticle>() {
                @Override
                public SingleArticle createFromParcel(Parcel in) {
                        return new SingleArticle(in);
                }

                @Override
                public SingleArticle[] newArray(int size) {
                        return new SingleArticle[size];
                }
        };

        public int getId() {
            return id;
        }

        public String getArticleType() {
                return articleType;
        }

        public String getSocialUrl() {
                return socialUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
                return content;
        }

        public String getPublished_at() {
            return published_at;
        }

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

        public boolean isFavourite() {
            return favourite;
        }

        public void setFavourite(boolean favourite) {
            this.favourite = favourite;
        }

        public String getYoutubeId() {
                return youtubeId;
        }

        public long getLikesCount() {
                return likesCount;
        }

        public long getSharesCount() {
                return sharesCount;
        }

        public void setLikesCount(long likesCount) {
                this.likesCount = likesCount;
        }

        public void setSharesCount(long sharesCount) {
                this.sharesCount = sharesCount;
        }

        public boolean isSharedOnWhatsapp() {
                return sharedOnWhatsapp == 1 ;
        }

        public boolean isSharedOnFacebook() {
                return sharedOnFacebook == 1 ;
        }

        public boolean isSharedOnTwitter() {
                return sharedOnTwitter == 1 ;
        }

        public boolean isNewPost() {
                return isNewPost == 1;
        }

        public void setBannerImageAdded(boolean firstImageAdded) {
                isBannerImageAdded = firstImageAdded;
        }

        //        public boolean isSharedOnInstagram() {
//                return sharedOnInstagram == 1 ;
//        }
//
//        public boolean isSharedOnSharechat() {
//                return sharedOnSharechat == 1 ;
//        }

        public void setSharedOnWhatsapp(int sharedOnWhatsapp) {
                this.sharedOnWhatsapp = sharedOnWhatsapp;
        }

        public void setSharedOnFacebook(int sharedOnFacebook) {
                this.sharedOnFacebook = sharedOnFacebook;
        }

        public void setSharedOnTwitter(int sharedOnTwitter) {
                this.sharedOnTwitter = sharedOnTwitter;
        }

        public ArrayList<ArticleImages> getArticleImagesList() {
                return articleImagesList;
        }

        public boolean isBannerImageAdded() {
                return isBannerImageAdded;
        }
//        public void setSharedOnInstagram(int sharedOnInstagram) {
//                this.sharedOnInstagram = sharedOnInstagram;
//        }
//
//        public void setSharedOnSharechat(int sharedOnSharechat) {
//                this.sharedOnSharechat = sharedOnSharechat;
//        }

        public SingleArticle() {
        }

}
