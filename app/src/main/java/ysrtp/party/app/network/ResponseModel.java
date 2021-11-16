package ysrtp.party.app.network;

/**
 * Created by mahesh on 26/3/18.
 */

public class ResponseModel {

    private String payload;
    private boolean success;
    private int statusCode;

    ResponseModel() {
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
