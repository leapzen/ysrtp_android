package ysrtp.party.app.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.greenrobot.eventbus.EventBus;

import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.databinding.ActivityHomeBinding;
import ysrtp.party.app.databinding.NavHeaderHomeBinding;
import ysrtp.party.app.feedback.FeedBackActivity;
import ysrtp.party.app.home.newpost.NewPostsFragment;
import ysrtp.party.app.home.shared.SharedPostsFragment;
import ysrtp.party.app.home.unshared.UnSharedPostsFragment;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.mlapostings.MlaPostingsActivity;
import ysrtp.party.app.notification.NotificationActivity;
import ysrtp.party.app.profile.ProfileActivity;
import ysrtp.party.app.rewards.RewardsActivity;
import ysrtp.party.app.splash.AppPreferencesModel;
import ysrtp.party.app.viewarticle.ViewArticleActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends PartyActivity {
    private static final int MY_POINTS_CALLLBACK = 9009;
    private ActivityHomeBinding activityHomeBinding;
    private long back_pressed;
    private NavHeaderHomeBinding navHeaderHomeBinding;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public HomeTabsAdapter homeTabsAdapter;
    private boolean showAlert = true;
    private HomeViewModel homeViewModel;
    private UserDetailsModel userDetailsModel;
    private final List<String> mFragmentTitleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = DataBindingUtil.setContentView(this,R.layout.activity_home);
        homeViewModel = new ViewModelProvider(HomeActivity.this).get(HomeViewModel.class);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, activityHomeBinding.drawerLayout, activityHomeBinding.appBar.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        activityHomeBinding.drawerLayout.addDrawerListener(toggle);
        userDetailsModel = UserDetailsModel.getInstance();

        toggle.syncState();

        setupTabsWithViewPager();

        checkActivityType();

        activityHomeBinding.appBar.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("onTabSelected: ", "++");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.e("onTabUnselected: ", "++");

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.e("onTabReselected: ", "++");
                if(tab.getPosition() == 0)
                EventBus.getDefault().post(new NewPostsFragment.RefreshNewPosts());

            }
        });

        navHeaderHomeBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.nav_header_home, activityHomeBinding
                .navView, false);
        activityHomeBinding.navView.addHeaderView(navHeaderHomeBinding.getRoot());

        activityHomeBinding.navView.getHeaderView(0).setOnClickListener(v -> {
            Intent profileIntent = new Intent(HomeActivity.this,ProfileActivity.class);
            startActivity(profileIntent);
        });
        activityHomeBinding.appBar.tvShareTitle.setText(mFirebaseRemoteConfig.getString("share_app"));
        activityHomeBinding.appBar.tvShareTitle.setOnClickListener(v -> activityHomeBinding.appBar.ivShareImage.performClick());
        activityHomeBinding.appBar.ivShareImage.setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, AppPreferencesModel.getAppPreferencesInstance().getAppPrefs()
                    .getPlayStoreLink());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, mFirebaseRemoteConfig.getString("send_to")));
        });

        activityHomeBinding.appBar.tvRewardTitle.setText(Html.fromHtml(mFirebaseRemoteConfig.getString("reward")));
        activityHomeBinding.appBar.tvRewardTitle.setOnClickListener(v -> activityHomeBinding.appBar.ivRewardImage.performClick());
        activityHomeBinding.appBar.ivRewardImage.setOnClickListener(v -> {
            Intent myPointsIntent = new Intent(HomeActivity.this,RewardsActivity.class);
            startActivityForResult(myPointsIntent,MY_POINTS_CALLLBACK);
        });

        activityHomeBinding.tvNotification.setOnClickListener(v -> {
            Intent notificationIntent = new Intent(HomeActivity.this,NotificationActivity.class);
            startActivity(notificationIntent);
            UserDetailsModel.getInstance().setNewMessagesCount(0);
        });

        activityHomeBinding.appBar.fabCreatePost.setOnClickListener(v->{
            if(userDetailsModel.getUserType().equalsIgnoreCase("MLA")){
                Intent mlaPostingIntent = new Intent(HomeActivity.this, MlaPostingsActivity.class);
                startActivity(mlaPostingIntent);
            }
        });

        activityHomeBinding.tvFeedback.setOnClickListener(v -> {
            Intent feedbackIntent = new Intent(HomeActivity.this, FeedBackActivity.class);
            startActivity(feedbackIntent);
            activityHomeBinding.drawerLayout.closeDrawer(GravityCompat.START);
        });


        if(userDetailsModel.getUserType().equalsIgnoreCase("MLA")){
            if(userDetailsModel.isCanShowPostings()){
                activityHomeBinding.appBar.fabCreatePost.show();
            }else{
                activityHomeBinding.appBar.fabCreatePost.hide();
            }
        }else{
            activityHomeBinding.appBar.fabCreatePost.hide();
        }
    }


    private void showNewPostsAlert() {
        NewPostsAlertDialog newPostsAlertDialog = new NewPostsAlertDialog();
        newPostsAlertDialog.show(getSupportFragmentManager(), newPostsAlertDialog.getTag());
        newPostsAlertDialog.setCancelable(false);
        newPostsAlertDialog.setOnDismissListener(selectedPlatform -> {
            if(selectedPlatform.equalsIgnoreCase("rewards")){
                Intent myPointsIntent = new Intent(HomeActivity.this,RewardsActivity.class);
                startActivityForResult(myPointsIntent,MY_POINTS_CALLLBACK);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navHeaderHomeBinding.setUser(UserDetailsModel.getInstance());
        if(UserDetailsModel.getInstance().getNewMessagesCount() == 0){
            activityHomeBinding.tvNotification.setText(getString(R.string.notifications));
        }else{
            activityHomeBinding.tvNotification.setText(Html.fromHtml(getString(R.string.notificaiton_count,UserDetailsModel.getInstance().getNewMessagesCount())));
        }

    }

    private void checkActivityType() {

        if(getIntent().getStringExtra("from_activity") != null &&
                getIntent().getStringExtra("from_activity").equalsIgnoreCase("pushNotification")){
            homeViewModel.clickedPushNotification(getIntent().getStringExtra("notification_type"),
                    getIntent().getIntExtra("article_id",
                            0),getIntent().getIntExtra("ysrtp_message_id",0));
            if(getIntent().getStringExtra("notification_type").equalsIgnoreCase("article")){
                Intent viewArticleIntent = new Intent(this,ViewArticleActivity.class);
                viewArticleIntent.putExtra("from_activity",getIntent().getStringExtra("from_activity"));
                viewArticleIntent.putExtra("article_id",getIntent().getIntExtra("article_id",0));
                startActivity(viewArticleIntent);
                showAlert = false;
            }else if(getIntent().getStringExtra("notification_type").equalsIgnoreCase("un-shared")){
                moveTabs(1);
                showAlert = false;
            }else if(getIntent().getStringExtra("notification_type").equalsIgnoreCase("reward")){
                Intent myPointsIntent = new Intent(HomeActivity.this,RewardsActivity.class);
                startActivityForResult(myPointsIntent,MY_POINTS_CALLLBACK);
                showAlert = false;
            }
            else if(getIntent().getStringExtra("notification_type").equalsIgnoreCase("ysrtp_message")){
                Intent notificationIntent = new Intent(HomeActivity.this,NotificationActivity.class);
                startActivity(notificationIntent);
                showAlert = false;
                UserDetailsModel.getInstance().setNewMessagesCount(0);
            }
        }

        if(showAlert){
            showNewPostsAlert();
        }
    }


    public void redirectToLeaderSocialLinkOne(View view) {
        Intent leaderSocialLink1 = new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("leader_social_link_1")));
        startActivity(leaderSocialLink1);
    }

    public void redirectToLeaderSocialLinkTwo(View view) {
        Intent leaderSocialLink2 = new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("leader_social_link_2")));
        startActivity(leaderSocialLink2);
    }

    public void redirectToLeaderSocialLinkThree(View view) {
        Intent leaderSocialLink3 = new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("leader_social_link_3")));
        startActivity(leaderSocialLink3);
    }

    public void redirectToPartySocialLinkOne(View view) {
        Intent partySocialLink1 = new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("party_social_link_1")));
        startActivity(partySocialLink1);
    }

    public void redirectToPartySocialLinkTwo(View view) {
        Intent partySocialLink2 = new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("party_social_link_2")));
        startActivity(partySocialLink2);
    }

    public void redirectToPartySocialLinkThree(View view) {
        Intent partySocialLink3 = new Intent(Intent.ACTION_VIEW, Uri.parse(mFirebaseRemoteConfig.getString("party_social_link_3")));
        startActivity(partySocialLink3);
    }


    public void moveTabs(int position){
        activityHomeBinding.appBar.viewpager.setCurrentItem(position,true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_POINTS_CALLLBACK && resultCode == RESULT_OK && data != null){
            if(data.getStringExtra("type").equalsIgnoreCase("new")){
                moveTabs(0);
            }else{
                moveTabs(1);
            }
        }
    }


    private void setupTabsWithViewPager() {
        homeTabsAdapter = new HomeTabsAdapter(getSupportFragmentManager(),this.getLifecycle());
        activityHomeBinding.appBar.viewpager.setAdapter(homeTabsAdapter);

        new TabLayoutMediator(activityHomeBinding.appBar.tabLayout, activityHomeBinding.appBar.viewpager,
                (tab, position) -> tab.setText(mFragmentTitleList.get(position))).attach();
    }

    public class HomeTabsAdapter extends FragmentStateAdapter {

        HomeTabsAdapter(FragmentManager manager, Lifecycle lifecycle) {
            super(manager,lifecycle);
            updateTitles(false);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if(position == 0){
                return new NewPostsFragment();
            }else if(position == 1){
                return new UnSharedPostsFragment();
            }else if(position == 2){
                return new SharedPostsFragment();
            }else{
                return new NewPostsFragment();
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        public void updateTitles(boolean doNotify){
            int newPostsCount = UserDetailsModel.getInstance().getNewArticlesCount();
            int sharedPostsCount = UserDetailsModel.getInstance().getSharedArticlesCount();
            int unSharedPostsCount = UserDetailsModel.getInstance().getPartialSharedCount();
            if(newPostsCount < 0){
                newPostsCount = 0;
            }
            if(sharedPostsCount < 0){
                sharedPostsCount = 0;
            }
            if(unSharedPostsCount < 0){
                unSharedPostsCount = 0;
            }
            //TODO
            mFragmentTitleList.clear();
            mFragmentTitleList.add(0,mFirebaseRemoteConfig
                    .getString("new_posts")+"\n("+newPostsCount+")");
            mFragmentTitleList.add(1,mFirebaseRemoteConfig
                    .getString("unshared_posts")+"\n("+unSharedPostsCount+")");
            mFragmentTitleList.add(2,mFirebaseRemoteConfig
                    .getString("shared_posts")+"\n("+sharedPostsCount+")");

            if(doNotify){
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            if(UserDetailsModel.getInstance().getUserType().equalsIgnoreCase("MLA")){
                return 1;
            }else{
                return 3;

            }
        }
    }

    @Override
    public void onBackPressed() {
        if (activityHomeBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            activityHomeBinding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        }else {
            Toast.makeText(getBaseContext(), mFirebaseRemoteConfig.getString("press_back_to_exit"),
                    Toast.LENGTH_LONG).show();
            back_pressed = System.currentTimeMillis();
        }
    }
}
