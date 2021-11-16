package ysrtp.party.app.home.shared;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import ysrtp.party.app.R;
import ysrtp.party.app.common.CountFormat;
import ysrtp.party.app.common.interfaces.MyViewClickListener;
import ysrtp.party.app.databinding.ItemSharedPostsBinding;
import ysrtp.party.app.home.SingleArticle;

import java.util.List;

public class SharedPostsAdapter extends RecyclerView.Adapter<SharedPostsAdapter.SharedPostsHolder> {
    private List<SingleArticle> sharedArticlesList;
    private MyViewClickListener myViewClickListener;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    SharedPostsAdapter(MyViewClickListener myViewClickListener,List<SingleArticle> articlesList) {
        this.myViewClickListener = myViewClickListener;
        this.sharedArticlesList = articlesList;
    }

    @NonNull
    @Override
    public SharedPostsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        ItemSharedPostsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_shared_posts, viewGroup, false);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return new SharedPostsHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedPostsHolder viewHolder, int i) {
        SingleArticle sharedArticle = sharedArticlesList.get(i);
        viewHolder.itemSharedPostsBinding.setSharedPosts(sharedArticle);
        viewHolder.itemSharedPostsBinding.tvShare.setText(String.format("%s Shares", CountFormat.format(sharedArticle.getSharesCount())));
        viewHolder.itemSharedPostsBinding.tvLike.setText(String.format("%s Likes", CountFormat.format(sharedArticle.getLikesCount())));

//        if(sharedArticle.isSharedOnTwitter() &&
//                sharedArticle.isSharedOnWhatsapp()&&
//                sharedArticle.isSharedOnFacebook()){
//            viewHolder.itemSharedPostsBinding.tvSharedOn.setText(mFirebaseRemoteConfig.getString("title_shared_all"));
//        }else{
//            viewHolder.itemSharedPostsBinding.tvSharedOn.setText(mFirebaseRemoteConfig.getString("title_share_on"));
//        }
        viewHolder.itemSharedPostsBinding.tvSharedOn.setText(mFirebaseRemoteConfig.getString("title_all_shared"));

    }

    @Override
    public int getItemCount() {
        return sharedArticlesList.size();
    }

    class SharedPostsHolder extends RecyclerView.ViewHolder {

        ItemSharedPostsBinding itemSharedPostsBinding;
        SharedPostsHolder(final ItemSharedPostsBinding itemSharedPostsBinding) {
            super(itemSharedPostsBinding.getRoot());
            this.itemSharedPostsBinding = itemSharedPostsBinding;

            itemSharedPostsBinding.ivPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.POST,getAdapterPosition());
                }
            });

            itemSharedPostsBinding.tvShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.SHARE,getAdapterPosition());
                }
            });

            itemSharedPostsBinding.tvLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!sharedArticlesList.get(getAdapterPosition()).isFavourite()){
                        itemSharedPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_favorite,0,0);
                        sharedArticlesList.get(getAdapterPosition()).setFavourite(true);
                        sharedArticlesList.get(getAdapterPosition()).setLikesCount(sharedArticlesList.get(getAdapterPosition()).getLikesCount()+1);
                        myViewClickListener.onViewClick(MyViewClickListener.LIKE,getAdapterPosition());
                        notifyDataSetChanged();                    }else{
                        itemSharedPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_not_favorite,0,0);
                        sharedArticlesList.get(getAdapterPosition()).setFavourite(false);
                        sharedArticlesList.get(getAdapterPosition()).setLikesCount(sharedArticlesList.get(getAdapterPosition()).getLikesCount()-1);
                        myViewClickListener.onViewClick(MyViewClickListener.DISLIKE,getAdapterPosition());
                        notifyDataSetChanged();                    }
                }
            });
        }
    }
}
