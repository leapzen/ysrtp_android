package ysrtp.party.app.membersregister;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetailsModel implements Parcelable {
    private static UserDetailsModel userDetailsModelInstance = new UserDetailsModel();
    @SerializedName("unique_id")
    @Expose
    private String unique_id;
    @SerializedName("name")
    @Expose
    private String name="";
    @SerializedName("mobile")
    @Expose
    private String mobile="";
    @SerializedName("user_type")
    @Expose
    String userType="";
    @SerializedName("date_of_birth")
    @Expose
    private String dob="";
    @SerializedName("gender")
    @Expose
    private String gender="";
    @SerializedName("grampanchayat")
    @Expose
    private String grampanchayat="";
    @SerializedName("ward_id")
    @Expose
    private int wardNumber;
    @SerializedName("block")
    @Expose
    private String block="";
    @SerializedName("assembly_constituency")
    @Expose
    private String Constituency="";
    @SerializedName("district")
    @Expose
    private String district="";
    @SerializedName("grampanchayat_normalized_name")
    @Expose
    private String grampanchayatNormalName="";
    @SerializedName("block_normalized_name")
    @Expose
    private String blockNormalName="";
    @SerializedName("assembly_constituency_normalized_name")
    @Expose
    private String ConstituencyNormalName="";
    @SerializedName("district_normalized_name")
    @Expose
    private String districtNormalName="";
    @SerializedName("pic_large")
    @Expose
    private String picLarge="";
    @SerializedName("pic_medium")
    @Expose
    private String picMedium="";
    @SerializedName("pic_small")
    @Expose
    private String picSmall="";
    @SerializedName("new_articles_count")
    @Expose
    private int newArticlesCount;
    @SerializedName("shared_articles_count")
    @Expose
    private int sharedArticlesCount;
    @SerializedName("favourite_articles_count")
    @Expose
    private int favouriteArticlesCount;
    @SerializedName("partially_shared_articles_count")
    @Expose
    private int partialSharedCount;
    @SerializedName("new_messages_count")
    @Expose
    private int newMessagesCount;
    @SerializedName("can_show_mla_postings")
    @Expose
    private boolean canShowPostings;
    @SerializedName("mla_posting_url")
    @Expose
    private String mlaPostingUrl;

    private UserDetailsModel() {
    }

    public static UserDetailsModel getInstance() {
        return userDetailsModelInstance;
    }

    public static void setInstance(UserDetailsModel instance) {
        userDetailsModelInstance = instance;
    }

    public String getUniqueId() {
        return unique_id;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUserType() {
        return userType;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public String getGrampanchayat() {
        return grampanchayat;
    }

    public int getWardNumber() {
        return wardNumber;
    }

    public String getBlock() {
        return block;
    }

    public String getConstituency() {
        return Constituency;
    }

    public String getDistrict() {
        return district;
    }

    public String getPicLarge() {
        return picLarge;
    }

    public String getPicMedium() {
        return picMedium;
    }

    public String getPicSmall() {
        return picSmall;
    }

    public void setPicLarge(String picLarge) {
        this.picLarge = picLarge;
    }

    public void setPicMedium(String picMedium) {
        this.picMedium = picMedium;
    }

    public void setPicSmall(String picSmall) {
        this.picSmall = picSmall;
    }

    public int getNewArticlesCount() {
        return newArticlesCount;
    }

    public void setNewArticlesCount(int newArticlesCount) {
        this.newArticlesCount = newArticlesCount;
    }

    public int getSharedArticlesCount() {
        return sharedArticlesCount;
    }

    public void setSharedArticlesCount(int sharedArticlesCount) {
        this.sharedArticlesCount = sharedArticlesCount;
    }

    public int getFavouriteArticlesCount() {
        return favouriteArticlesCount;
    }

    public void setFavouriteArticlesCount(int favouriteArticlesCount) {
        this.favouriteArticlesCount = favouriteArticlesCount;
    }

    public int getPartialSharedCount() {
        return partialSharedCount;
    }

    public void setPartialSharedCount(int partialSharedCount) {
        this.partialSharedCount = partialSharedCount;
    }

    public String getGrampanchayatNormalName() {
        return grampanchayatNormalName;
    }

    public String getBlockNormalName() {
        return blockNormalName;
    }

    public String getConstituencyNormalName() {
        return ConstituencyNormalName;
    }

    public String getDistrictNormalName() {
        return districtNormalName;
    }

    public int getNewMessagesCount() {
        return newMessagesCount;
    }

    public void setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }

    public boolean isCanShowPostings() {
        return canShowPostings;
    }

    public String getMlaPostingUrl() {
        return mlaPostingUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.unique_id);
        dest.writeString(this.name);
        dest.writeString(this.mobile);
        dest.writeString(this.userType);
        dest.writeString(this.dob);
        dest.writeString(this.gender);
        dest.writeString(this.grampanchayat);
        dest.writeInt(this.wardNumber);
        dest.writeString(this.block);
        dest.writeString(this.Constituency);
        dest.writeString(this.district);
        dest.writeString(this.grampanchayatNormalName);
        dest.writeString(this.blockNormalName);
        dest.writeString(this.ConstituencyNormalName);
        dest.writeString(this.districtNormalName);
        dest.writeString(this.picLarge);
        dest.writeString(this.picMedium);
        dest.writeString(this.picSmall);
        dest.writeInt(this.newArticlesCount);
        dest.writeInt(this.sharedArticlesCount);
        dest.writeInt(this.favouriteArticlesCount);
        dest.writeInt(this.partialSharedCount);
        dest.writeInt(this.newMessagesCount);
        dest.writeByte(this.canShowPostings ? (byte) 1 : (byte) 0);
        dest.writeString(this.mlaPostingUrl);

    }

    protected UserDetailsModel(Parcel in) {
        this.unique_id = in.readString();
        this.name = in.readString();
        this.mobile = in.readString();
        this.userType = in.readString();
        this.dob = in.readString();
        this.gender = in.readString();
        this.grampanchayat = in.readString();
        this.wardNumber = in.readInt();
        this.block = in.readString();
        this.Constituency = in.readString();
        this.district = in.readString();
        this.grampanchayatNormalName = in.readString();
        this.blockNormalName = in.readString();
        this.ConstituencyNormalName = in.readString();
        this.districtNormalName = in.readString();
        this.picLarge = in.readString();
        this.picMedium = in.readString();
        this.picSmall = in.readString();
        this.newArticlesCount = in.readInt();
        this.sharedArticlesCount = in.readInt();
        this.favouriteArticlesCount = in.readInt();
        this.partialSharedCount = in.readInt();
        this.newMessagesCount = in.readInt();
        this.canShowPostings = in.readByte() != 0;
        this.mlaPostingUrl = in.readString();

    }

    public static final Creator<UserDetailsModel> CREATOR = new Creator<UserDetailsModel>() {
        @Override
        public UserDetailsModel createFromParcel(Parcel source) {
            return new UserDetailsModel(source);
        }

        @Override
        public UserDetailsModel[] newArray(int size) {
            return new UserDetailsModel[size];
        }
    };
}
