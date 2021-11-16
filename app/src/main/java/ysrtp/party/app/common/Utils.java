package ysrtp.party.app.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;


/**
 * Created by maheshpujala on 24,August,2018
 */
public class Utils {
    private Activity activity;

    public Utils(Activity activity) {
        this.activity = activity;
    }

    public void closeKeyboard(View view){
        try {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

}
