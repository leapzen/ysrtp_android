package ysrtp.party.app.network;

import com.android.volley.Request;

/**
 * Created by mahesh on 26/3/18.
 */


public class RequestModel {

    private String URL;
    private String payload = "{}";
    private int requestType;
    public int GET = Request.Method.GET,
        POST = Request.Method.POST,
        PUT = Request.Method.PUT,
        DELETE = Request.Method.DELETE;

    public RequestModel() {
    }


    public RequestModel(String URL) {
        this.URL = URL;
    }

    public RequestModel(String URL, String payload,int requestType) {
        this.URL = URL;
        this.payload = payload;
        this.requestType = requestType;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }


    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }


}
