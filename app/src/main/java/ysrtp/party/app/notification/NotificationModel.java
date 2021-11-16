
package ysrtp.party.app.notification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NotificationModel {

    @SerializedName("biju_messages")
    @Expose
    private ArrayList<PartyMessage> partyMessages = new ArrayList<>();
    @SerializedName("response_code")
    @Expose
    private int responseCode;
    @SerializedName("total_count")
    @Expose
    private int totalCount;

    public ArrayList<PartyMessage> getPartyMessages() {
        return partyMessages;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
