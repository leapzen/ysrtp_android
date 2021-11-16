package ysrtp.party.app.common;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import ysrtp.party.app.R;
import ysrtp.party.app.network.Connectivity;

public class BindingAdapters {

    private static String imageUrl;


    @BindingAdapter({"android:largeImage","android:smallImage"})
    public static void loadImage(ImageView view, String largeImage,String smallImage) {
        CircularProgressDrawable cProgress = new CircularProgressDrawable(view.getContext());
        cProgress.setCenterRadius(50f);
        cProgress.setStrokeWidth(10f);
        cProgress.start();


        RequestOptions requestOptions = new RequestOptions().placeholder(cProgress).override(500,500).error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        String imageUrl;
        if(Connectivity.isConnectionFast(view.getContext())){
            imageUrl = largeImage;
        }else{
            imageUrl = smallImage;
        }
        Glide.with(view.getContext())
                .load(imageUrl)
                .thumbnail(0.2f)
                .apply(requestOptions)
                .into(view);
    }


    @BindingAdapter("android:image")
    public static void loadImage(ImageView view, String imageUrl) {
        RequestOptions requestOptions = new RequestOptions().override(75,75)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(view.getContext())
                .load(imageUrl)
                .apply(requestOptions)
                .thumbnail(0.2f)
                .into(view);
    }

    @BindingAdapter("android:profile")
    public static void loadProfileImage(ImageView view, String imageUrl) {
        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_person)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(view.getContext())
                .load(imageUrl)
                .apply(requestOptions)
                .thumbnail(0.2f)
                .into(view);
    }

    @BindingAdapter("android:background")
    public static void setBackground(ImageView view, String articleType) {
       if(articleType.equalsIgnoreCase("youtube")){
           view.setImageResource(R.drawable.ic_youtube);
       }else if(articleType.equalsIgnoreCase("twitter")){
           view.setImageResource(R.drawable.ic_twitter);
       }else if(articleType.equalsIgnoreCase("facebook")){
           view.setImageResource(R.drawable.ic_facebook);
       }
    }

}
