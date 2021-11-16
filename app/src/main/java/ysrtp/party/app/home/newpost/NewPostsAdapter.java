package ysrtp.party.app.home.newpost;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import ysrtp.party.app.R;
import ysrtp.party.app.common.CountFormat;
import ysrtp.party.app.common.interfaces.MyViewClickListener;
import ysrtp.party.app.databinding.ItemNewPostsBinding;
import ysrtp.party.app.home.SingleArticle;

import java.util.List;

class NewPostsAdapter extends RecyclerView.Adapter<NewPostsAdapter.NewPostsHolder>{

    private MyViewClickListener myViewClickListener;
    private List<SingleArticle> articlesList;


    NewPostsAdapter(MyViewClickListener myViewClickListener, List<SingleArticle> articlesList) {
        this.myViewClickListener = myViewClickListener;
        this.articlesList = articlesList;
    }

    @NonNull
    @Override
    public NewPostsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        ItemNewPostsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_new_posts, viewGroup, false);
        return new NewPostsHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NewPostsHolder viewHolder, int i) {
        SingleArticle newPostsModel = articlesList.get(i);
        viewHolder.itemNewPostsBinding.setNewPosts(newPostsModel);

        viewHolder.itemNewPostsBinding.tvShare.setText(String.format("%s Shares", CountFormat.format(newPostsModel.getSharesCount())));
        viewHolder.itemNewPostsBinding.tvLike.setText(String.format("%s Likes", CountFormat.format(newPostsModel.getLikesCount())));
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(2000);
        anim.setRepeatMode(Animation.INFINITE);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }

    class NewPostsHolder extends RecyclerView.ViewHolder {

        ItemNewPostsBinding itemNewPostsBinding;
        NewPostsHolder(final ItemNewPostsBinding itemNewPostsBinding) {
            super(itemNewPostsBinding.getRoot());
            this.itemNewPostsBinding = itemNewPostsBinding;

            itemNewPostsBinding.ivPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.POST,getAdapterPosition());
                }
            });

            itemNewPostsBinding.tvShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myViewClickListener.onViewClick(MyViewClickListener.SHARE,getAdapterPosition());
                }
            });

            itemNewPostsBinding.tvLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!articlesList.get(getAdapterPosition()).isFavourite()){
                        itemNewPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_favorite,0,0);
                        articlesList.get(getAdapterPosition()).setFavourite(true);
                        articlesList.get(getAdapterPosition()).setLikesCount(articlesList.get(getAdapterPosition()).getLikesCount()+1);
                        myViewClickListener.onViewClick(MyViewClickListener.LIKE,getAdapterPosition());
                        notifyDataSetChanged();
                    }else{
                        itemNewPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_not_favorite,0,0);
                        articlesList.get(getAdapterPosition()).setFavourite(false);
                        articlesList.get(getAdapterPosition()).setLikesCount(articlesList.get(getAdapterPosition()).getLikesCount()-1);
                        myViewClickListener.onViewClick(MyViewClickListener.DISLIKE,getAdapterPosition());
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
