package ysrtp.party.app.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.R;
import ysrtp.party.app.databinding.FragmentNewPostsAlertDialogBinding;
import ysrtp.party.app.home.sharedialog.MyDialogDismissListener;

public class NewPostsAlertDialog extends BottomSheetDialogFragment {
    private MyDialogDismissListener onDismissListener;
    private FragmentNewPostsAlertDialogBinding newPostsAlertDialogBinding;


    public NewPostsAlertDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        newPostsAlertDialogBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.fragment_new_posts_alert_dialog, container, false);
        return newPostsAlertDialogBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newPostsAlertDialogBinding.tvAlertTitle.setText(Html.fromHtml(FirebaseRemoteConfig.getInstance().getString("alert_title")));
        newPostsAlertDialogBinding.tvAlertInfo.setText(FirebaseRemoteConfig.getInstance().getString("alert_info"));
        newPostsAlertDialogBinding.tvAlertInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDismissListener.onDismiss("rewards");
                dismiss();
            }
        });
        newPostsAlertDialogBinding.tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setOnDismissListener(MyDialogDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(onDismissListener != null)
            onDismissListener.onDismiss("");
        super.onDismiss(dialog);

    }
}