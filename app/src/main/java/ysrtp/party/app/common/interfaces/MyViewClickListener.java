package ysrtp.party.app.common.interfaces;

public interface MyViewClickListener {
        int POST = 0,SHARE = 1,LIKE = 2,DISLIKE = 3,FACEBOOK = 4,WHATSAPP = 5,TWITTER = 6;
        void onViewClick(int viewType,int position);
}
