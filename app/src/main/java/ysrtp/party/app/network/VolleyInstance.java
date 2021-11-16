package ysrtp.party.app.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by maheshpujala on 24,August,2018
 */
class VolleyInstance {

    private static RequestQueue mRequestQueue;

    private VolleyInstance() {
    }

    public static synchronized void addToRequestQueue(JsonObjectRequest jsObjRequest, Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        jsObjRequest.setShouldCache(false);
        jsObjRequest.setTag("Party");
        mRequestQueue.add(jsObjRequest);
    }

    public static void clearQueue() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll("Party");
        }
    }
}
