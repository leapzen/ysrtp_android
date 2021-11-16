package ysrtp.party.app.splash;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VerifyMobileModel {
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("user_type")
    @Expose
    String userType ;
    @SerializedName("response_code")
    @Expose
    int responseCode;
    @SerializedName("mobile")
    @Expose
    String mobile ;
    @SerializedName("name")
    @Expose
    String name ;
    @SerializedName("ward_id")
    @Expose
    String wardId ;
    @SerializedName("grampanchayat_id")
    @Expose
    String grampanchayatId ;
    @SerializedName("block_id")
    @Expose
    String blockId ;


    public String getName() {
        return name;
    }

    public String getWardId() {
        return wardId;
    }

    public String getGrampanchayatId() {
        return grampanchayatId;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getMessage() {
        return message;
    }

    public String getUserType() {
        return userType;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getMobile() {
        return mobile;
    }
}
