package ysrtp.party.app.home.sharedialog;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SharedArticleStatus {
    @SerializedName("response_code")
    @Expose
    private int response_code;
    @SerializedName("message")
    @Expose
    private String message;
    private String selectedPlatform;

    public int getResponse_code() {
        return response_code;
    }

    public String getMessage() {
        return message;
    }

    public String getSelectedPlatform() {
        return selectedPlatform;
    }

    public void setSelectedPlatform(String selectedPlatform) {
        this.selectedPlatform = selectedPlatform;
    }
}
