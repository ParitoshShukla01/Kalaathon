package kalaathon.com.trending;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kalaathon.com.R;
import kalaathon.com.SendNotification;
import kalaathon.com.list.LikesList;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.models.Video;
import kalaathon.com.trending_story.RecyclerViewAdapter;
import kalaathon.com.utils.BottomNavigationViewHelper;
import kalaathon.com.utils.CustomTimeStamp;
import kalaathon.com.utils.MainFeedListAdapter;
import kalaathon.com.utils.MainFeedListVideoAdapter;
import kalaathon.com.utils.SectionsPagerAdapter;
import kalaathon.com.utils.UniversalImageLoader;

public class TrendingActivity extends AppCompatActivity implements MainFeedListAdapter.OnLoadMoreItemsListener,
        MainFeedListVideoAdapter.OnLoadMoreVideoListener  {

    private Context mContext= TrendingActivity.this;
    private static final int ACTIVITY_NUM=1;
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private LinearLayout mRelativeLayout;
    private Date currenttime=new Date();
    private RelativeLayout recyclerlayout;
    private RecyclerView mRecyclerView;
    private Boolean winners;
    private ImageView clear;

    @Override
    public void onLoadMoreVideos() {
        if(mViewPager.getCurrentItem()==1) {
            Frag_trend_video fragment = (Frag_trend_video) getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.view_container + ":" + mViewPager.getCurrentItem());
            if (fragment != null) {
                fragment.displayMoreVideos();
            }
        }

    }

    @Override
    public void onLoadMoreItems() {
        if(mViewPager.getCurrentItem()==0) {
            Frag_trend_photo fragment = (Frag_trend_photo) getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.view_container + ":" + mViewPager.getCurrentItem());
            if (fragment != null) {
                fragment.displayMorePhotos();
            }
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trendactivity);
        mFrameLayout = (FrameLayout) findViewById(R.id.frame_container);
        mRelativeLayout = (LinearLayout) findViewById(R.id.rellayoutparent);
        recyclerlayout=findViewById(R.id.rel_recycler);
        mRecyclerView=findViewById(R.id.story_recycler_view);
        recyclerlayout.setVisibility(View.GONE);
        clear=findViewById(R.id.story_clear);
        winners=false;

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideLeft(recyclerlayout);
            }
        });

        if(!ImageLoader.getInstance().isInited())
            initImageLoader();

        loadwinners();

        setupViewPager();
        setupBottomNavigationView();
    }

    public void hideLayout() {
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout() {
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }
    }


    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    //Bottom navigation view
    public void setupBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomnavigationviewbar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx,2);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(R.drawable.ic_trenddark);
    }

    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Frag_trend_photo());
        adapter.addFragment(new Frag_trend_video());

        mViewPager = (ViewPager) findViewById(R.id.view_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("Photos");
        tabLayout.getTabAt(1).setText("Videos");
    }

    public void onVideoThreadSelected(Video video, String callingActivity) {

        ViewNextTrendingVideo fragment = new ViewNextTrendingVideo();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.dbname_video), video);
        args.putString(getString(R.string.trend_activity), getString(R.string.trend_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack("View Video");
        transaction.commit();
    }

    public void onLikesSelected(ArrayList<String> user, String callingActivity)
    {
        LikesList frag=new LikesList();
        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.dbname_photos), user);
        args.putString("Activity",callingActivity);
        frag.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, frag);
        transaction.addToBackStack("Likes List");
        transaction.commit();
    }

    public void share(String sharelinktext)
    {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(sharelinktext))  // manually
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(intent);
                        } else {
                            // Error
                            Toast.makeText(mContext, "Something went wrong !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void notification(String username,String user_id,String media_id,String type)
    {
        new SendNotification().sendNotification("New Like ",username+" liked your post.",user_id,media_id,type,mContext);
    }

    public boolean withinSeven(Date old){
        long diff=currenttime.getTime()-old.getTime();
        long days=diff/(60*60*24*1000);
        return days<= 7;
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String current =new CustomTimeStamp().printTime(mContext);
                currenttime=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(current);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void loadwinners()
    {
        ArrayList<String> mUserList=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child("winners").child("show");
        Query query1=reference.child("winners").child("user_id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class).equals("true"))
                {
                    winners=true;
                    recyclerlayout.setVisibility(View.VISIBLE);
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot:dataSnapshot.getChildren())
                            {
                                mUserList.add(snapshot.getKey().trim());
                            }
                            getuserlist(mUserList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    recyclerlayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getuserlist(List<String> mUser)
    {
        ArrayList<UserAccountSettings> senduser=new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i=0;i<mUser.size();i++)
        {
            Query query = reference
                    .child(getResources().getString(R.string.dbname_user_account_settings))
                    .orderByChild(getResources().getString(R.string.field_user_id))
                    .equalTo(mUser.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        senduser.add(dataSnapshot1.getValue(UserAccountSettings.class));
                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(TrendingActivity.this, LinearLayoutManager.HORIZONTAL, false);
                    mRecyclerView.setLayoutManager(layoutManager);
                    RecyclerViewAdapter adapter = new RecyclerViewAdapter(TrendingActivity.this,senduser);
                    mRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        new MyAsyncTask().cancel(true);
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onPause();
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onResume();
    }
    public void slideLeft(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                -5000,
                0,
                0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerlayout.setAnimation(null);
                animate.cancel();
                recyclerlayout.setVisibility(View.GONE);
            }
        },500);
    }


}