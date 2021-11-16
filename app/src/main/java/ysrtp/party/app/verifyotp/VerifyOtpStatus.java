package ysrtp.party.app.verifyotp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ysrtp.party.app.membersregister.UserDetailsModel;

public class VerifyOtpStatus {
    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("user_details")
    @Expose
    private UserDetailsModel userDetailsModel;

    public int getResponseCode() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }

    public UserDetailsModel getUserDetailsModel() {
        return userDetailsModel;
    }
}
