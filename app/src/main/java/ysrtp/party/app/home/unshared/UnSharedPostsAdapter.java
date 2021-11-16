package ysrtp.party.app.home.unshared;

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
import ysrtp.party.app.databinding.ItemUnsharedPostsBinding;
import ysrtp.party.app.home.SingleArticle;

import java.util.List;

public class UnSharedPostsAdapter extends RecyclerView.Adapter<UnSharedPostsAdapter.UnSharedPostsHolder> {
    private List<SingleArticle> unSharedArticleList;
    private MyViewClickListener myViewClickListener;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    UnSharedPostsAdapter(MyViewClickListener myViewClickListener, List<SingleArticle> articlesList) {
        this.myViewClickListener = myViewClickListener;
        this.unSharedArticleList = articlesList;
    }

    @NonNull
    @Override
    public UnSharedPostsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        ItemUnsharedPostsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_unshared_posts, viewGroup, false);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return new UnSharedPostsHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UnSharedPostsHolder viewHolder, int i) {
        SingleArticle unSharedArticle = unSharedArticleList.get(i);
        viewHolder.itemUnsharedPostsBinding.setUnsharedPosts(unSharedArticle);
        viewHolder.itemUnsharedPostsBinding.tvShare.setText(String.format("%s Shares", CountFormat.format(unSharedArticle.getSharesCount())));
        viewHolder.itemUnsharedPostsBinding.tvLike.setText(String.format("%s Likes", CountFormat.format(unSharedArticle.getLikesCount())));

        if(unSharedArticle.isSharedOnTwitter() &&
                unSharedArticle.isSharedOnWhatsapp()&&
                unSharedArticle.isSharedOnFacebook()){
            viewHolder.itemUnsharedPostsBinding.tvSharedOn.setText(mFirebaseRemoteConfig.getString("title_share_on_all"));
        }else{
            viewHolder.itemUnsharedPostsBinding.tvSharedOn.setText(mFirebaseRemoteConfig.getString("title_share_on"));
        }
    }

    @Override
    public int getItemCount() {
        return unSharedArticleList.size();
    }

    class UnSharedPostsHolder extends RecyclerView.ViewHolder {

        ItemUnsharedPostsBinding itemUnsharedPostsBinding;
        UnSharedPostsHolder(final ItemUnsharedPostsBinding ItemUnsharedPostsBinding) {
            super(ItemUnsharedPostsBinding.getRoot());
            this.itemUnsharedPostsBinding = ItemUnsharedPostsBinding;



            itemUnsharedPostsBinding.ivFacebookShared.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.FACEBOOK,getAdapterPosition());
                }
            });

            itemUnsharedPostsBinding.ivTwitterShared.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.TWITTER,getAdapterPosition());
                }
            });

            itemUnsharedPostsBinding.ivWhatsappShared.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.WHATSAPP,getAdapterPosition());
                }
            });


            ItemUnsharedPostsBinding.ivPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.POST,getAdapterPosition());
                }
            });

            ItemUnsharedPostsBinding.tvShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.SHARE,getAdapterPosition());
                }
            });

            ItemUnsharedPostsBinding.tvLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!unSharedArticleList.get(getAdapterPosition()).isFavourite()){
                        ItemUnsharedPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_favorite,0,0);
                        unSharedArticleList.get(getAdapterPosition()).setFavourite(true);
                        unSharedArticleList.get(getAdapterPosition()).setLikesCount(unSharedArticleList.get(getAdapterPosition()).getLikesCount()+1);
                        myViewClickListener.onViewClick(MyViewClickListener.LIKE,getAdapterPosition());
                        notifyDataSetChanged();
                    }else{
                        ItemUnsharedPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_not_favorite,0,0);
                        unSharedArticleList.get(getAdapterPosition()).setFavourite(false);
                        unSharedArticleList.get(getAdapterPosition()).setLikesCount(unSharedArticleList.get(getAdapterPosition()).getLikesCount()-1);
                        myViewClickListener.onViewClick(MyViewClickListener.DISLIKE,getAdapterPosition());
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
