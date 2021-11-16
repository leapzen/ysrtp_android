package ysrtp.party.app.membersregister;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MembersRegisterStatus {
    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;

    public int getResponseCode() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }
}
