package ysrtp.party.app.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class UpdatePicModel {

    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("pic_large")
    @Expose
    private String picLarge;
    @SerializedName("pic_medium")
    @Expose
    private String picMedium;
    @SerializedName("pic_small")
    @Expose
    private String picSmall;
    @SerializedName("pic_thumb")
    @Expose
    private String picThumb;

    int getResponse_code() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }

    String getPicLarge() {
        return picLarge;
    }

    String getPicMedium() {
        return picMedium;
    }

    String getPicSmall() {
        return picSmall;
    }

    public String getPicThumb() {
        return picThumb;
    }
}
