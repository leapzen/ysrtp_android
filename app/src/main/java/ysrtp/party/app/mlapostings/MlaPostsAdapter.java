package ysrtp.party.app.mlapostings;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import ysrtp.party.app.R;
import ysrtp.party.app.common.CountFormat;
import ysrtp.party.app.common.interfaces.MyViewClickListener;
import ysrtp.party.app.databinding.ItemMlaPostsBinding;
import ysrtp.party.app.home.SingleArticle;

import java.util.List;

class MlaPostsAdapter extends RecyclerView.Adapter<MlaPostsAdapter.MlaPostsHolder>{

    private MyViewClickListener myViewClickListener;
    private List<SingleArticle> articlesList;


    MlaPostsAdapter(MyViewClickListener myViewClickListener, List<SingleArticle> articlesList) {
        this.myViewClickListener = myViewClickListener;
        this.articlesList = articlesList;
    }

    @NonNull
    @Override
    public MlaPostsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        ItemMlaPostsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_mla_posts, viewGroup, false);
        return new MlaPostsHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MlaPostsHolder viewHolder, int i) {
        SingleArticle mlaSinglePost = articlesList.get(i);
        viewHolder.itemMlaPostsBinding.setMlaPosts(mlaSinglePost);

        viewHolder.itemMlaPostsBinding.tvShare.setText(String.format("%s Shares", CountFormat.format(mlaSinglePost.getSharesCount())));
        viewHolder.itemMlaPostsBinding.tvLike.setText(String.format("%s Likes", CountFormat.format(mlaSinglePost.getLikesCount())));
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }

    class MlaPostsHolder extends RecyclerView.ViewHolder {

        ItemMlaPostsBinding itemMlaPostsBinding;
        MlaPostsHolder(final ItemMlaPostsBinding itemMlaPostsBinding) {
            super(itemMlaPostsBinding.getRoot());
            this.itemMlaPostsBinding = itemMlaPostsBinding;

            itemMlaPostsBinding.ivPost.setOnClickListener(v ->
                    myViewClickListener.onViewClick(MyViewClickListener.POST,getAdapterPosition()));

            itemMlaPostsBinding.tvShare.setOnClickListener(v ->
                    myViewClickListener.onViewClick(MyViewClickListener.SHARE,getAdapterPosition()));

            itemMlaPostsBinding.tvLike.setOnClickListener(v -> {
                if(!articlesList.get(getAdapterPosition()).isFavourite()){
                    itemMlaPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_favorite,0,0);
                    articlesList.get(getAdapterPosition()).setFavourite(true);
                    articlesList.get(getAdapterPosition()).setLikesCount(articlesList.get(getAdapterPosition()).getLikesCount()+1);
                    myViewClickListener.onViewClick(MyViewClickListener.LIKE,getAdapterPosition());
                    notifyItemChanged(getAdapterPosition(),articlesList.get(getAdapterPosition()));
                }else{
                    itemMlaPostsBinding.tvLike.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_not_favorite,0,0);
                    articlesList.get(getAdapterPosition()).setFavourite(false);
                    articlesList.get(getAdapterPosition()).setLikesCount(articlesList.get(getAdapterPosition()).getLikesCount()-1);
                    myViewClickListener.onViewClick(MyViewClickListener.DISLIKE,getAdapterPosition());
                    notifyItemChanged(getAdapterPosition(),articlesList.get(getAdapterPosition()));
                }
            });
        }
    }
}
