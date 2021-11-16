package ysrtp.party.app.home.sharedialog;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.R;
import ysrtp.party.app.databinding.FragmentShareDialogBinding;
import ysrtp.party.app.home.SingleArticle;

public class ShareDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private MyDialogDismissListener onDismissListener;
    private FragmentShareDialogBinding shareDialogBinding;
    private SingleArticle selectedArticle;

    public ShareDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            selectedArticle = getArguments().getParcelable("selectedArticle");
            String viewType = getArguments().getString("type");
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        shareDialogBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.fragment_share_dialog, container, false);
        return shareDialogBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("onViewCreated: ", "onViewCreated");

        if(selectedArticle == null){
            dismiss();
            return;
        }
        updateView(selectedArticle);

//        if(shareDialogBinding.tvFacebook.getVisibility() == View.GONE &&
//                shareDialogBinding.tvWhatsapp.getVisibility() == View.GONE &&
//                shareDialogBinding.tvTwitter.getVisibility() == View.GONE &&
//                shareDialogBinding.tvInstagram.getVisibility() == View.GONE &&
//                shareDialogBinding.tvShareChat.getVisibility() == View.GONE ){
//            onDismissListener.onDismiss("other.app");
//            dismiss();
//        }

        shareDialogBinding.tvFacebookShared.setOnClickListener(this);
        shareDialogBinding.tvWhatsappShared.setOnClickListener(this);
        shareDialogBinding.tvTwitterShared.setOnClickListener(this);
        shareDialogBinding.tvInstagramShared.setOnClickListener(this);
        shareDialogBinding.tvShareChatShared.setOnClickListener(this);
        shareDialogBinding.tvFacebook.setOnClickListener(this);
        shareDialogBinding.tvWhatsapp.setOnClickListener(this);
        shareDialogBinding.tvTwitter.setOnClickListener(this);
        shareDialogBinding.tvInstagram.setOnClickListener(this);
        shareDialogBinding.tvShareChat.setOnClickListener(this);
        shareDialogBinding.tvMore.setOnClickListener(this);
        shareDialogBinding.tvClose.setOnClickListener(this);

    }

    public void updateView(SingleArticle selectedArticle){
        Log.e("updateView: "+isPackageInstalled("com.facebook.katana"), "+"+selectedArticle.isSharedOnFacebook());

        shareDialogBinding.tvTitleSharedAlready.setVisibility(View.GONE);
        shareDialogBinding.flexSharedLayout.setVisibility(View.GONE);
        shareDialogBinding.divider.setVisibility(View.GONE);



        //TOP VIEW
        if(selectedArticle.isSharedOnFacebook() ||
                selectedArticle.isSharedOnTwitter() ||
                selectedArticle.isSharedOnWhatsapp()){

            shareDialogBinding.tvTitleSharedAlready.setVisibility(View.VISIBLE);
            shareDialogBinding.flexSharedLayout.setVisibility(View.VISIBLE);
            shareDialogBinding.divider.setVisibility(View.VISIBLE);

            shareDialogBinding.tvTitleSharedAlready.setText(FirebaseRemoteConfig.getInstance().getString("already_shared_on"));
            if(selectedArticle.isSharedOnFacebook() && isPackageInstalled("com.facebook.katana")){
                shareDialogBinding.tvFacebookShared.setVisibility(View.VISIBLE);
            }else{
                shareDialogBinding.tvFacebookShared.setVisibility(View.GONE);
            }

            if(selectedArticle.isSharedOnWhatsapp() && isPackageInstalled("com.whatsapp")){
                shareDialogBinding.tvWhatsappShared.setVisibility(View.VISIBLE);
            }else{
                shareDialogBinding.tvWhatsappShared.setVisibility(View.GONE);
            }

            if(selectedArticle.isSharedOnTwitter() && isPackageInstalled("com.twitter.android")){
                shareDialogBinding.tvTwitterShared.setVisibility(View.VISIBLE);
            }else{
                shareDialogBinding.tvTwitterShared.setVisibility(View.GONE);
            }

//            if(selectedArticle.isSharedOnInstagram() && isPackageInstalled("com.instagram.android")){
//                shareDialogBinding.tvInstagramShared.setVisibility(View.VISIBLE);
//            }else{
//                shareDialogBinding.tvInstagramShared.setVisibility(View.GONE);
//            }
//
//            if(selectedArticle.isSharedOnSharechat() && isPackageInstalled("in.mohalla.sharechat")){
//                shareDialogBinding.tvShareChatShared.setVisibility(View.VISIBLE);
//            }else{
//                shareDialogBinding.tvShareChatShared.setVisibility(View.GONE);
//            }
        }else{
            shareDialogBinding.tvTitleSharedAlready.setVisibility(View.GONE);
        }


        //BOTTOM VIEW

        shareDialogBinding.tvTitleShare.setText(FirebaseRemoteConfig.getInstance().getString("share_on"));

        if(!isPackageInstalled("com.facebook.katana") || selectedArticle.isSharedOnFacebook()){
            shareDialogBinding.tvFacebook.setVisibility(View.GONE);
        }

        if(!isPackageInstalled("com.whatsapp") || selectedArticle.isSharedOnWhatsapp()){
            shareDialogBinding.tvWhatsapp.setVisibility(View.GONE);
        }

        if(!isPackageInstalled("com.twitter.android") || selectedArticle.isSharedOnTwitter()){
            shareDialogBinding.tvTwitter.setVisibility(View.GONE);
        }

//        if(!isPackageInstalled("com.instagram.android") || selectedArticle.isSharedOnInstagram()){
//            shareDialogBinding.tvInstagram.setVisibility(View.GONE);
//        }
//
//        if(!isPackageInstalled("in.mohalla.sharechat") || selectedArticle.isSharedOnSharechat()){
//            shareDialogBinding.tvShareChat.setVisibility(View.GONE);
//        }
    }



    public void setOnDismissListener(MyDialogDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_facebook) {
            onDismissListener.onDismiss("com.facebook.katana");
        } else if (id == R.id.tv_whatsapp) {
            onDismissListener.onDismiss("com.whatsapp");
        } else if (id == R.id.tv_twitter) {
            onDismissListener.onDismiss("com.twitter.android");
        } else if (id == R.id.tv_instagram) {
            onDismissListener.onDismiss("com.instagram.android");
        } else if (id == R.id.tv_share_chat) {
            onDismissListener.onDismiss("in.mohalla.sharechat");
        } else if (id == R.id.tv_more) {
            onDismissListener.onDismiss("other.app");
        } else if (id == R.id.tv_close) {
            onDismissListener.onDismiss("exit");
            dismiss();
        } else if (id == R.id.tv_facebook_shared) {
            onDismissListener.onDismiss("com.facebook.katana");
        } else if (id == R.id.tv_whatsapp_shared) {
            onDismissListener.onDismiss("com.whatsapp");
        } else if (id == R.id.tv_twitter_shared) {
            onDismissListener.onDismiss("com.twitter.android");
        } else if (id == R.id.tv_instagram_shared) {
            onDismissListener.onDismiss("com.instagram.android");
        } else if (id == R.id.tv_share_chat_shared) {
            onDismissListener.onDismiss("in.mohalla.sharechat");
        }
    }

    private boolean isPackageInstalled(String packagename) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
