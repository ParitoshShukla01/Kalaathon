package kalaathon.com.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kalaathon.com.R;
import kalaathon.com.SendNotification;
import kalaathon.com.list.LikesList;
import kalaathon.com.models.Video;
import kalaathon.com.utils.BottomNavigationViewHelper;
import kalaathon.com.utils.CustomTimeStamp;
import kalaathon.com.utils.MainFeedListAdapter;
import kalaathon.com.utils.MainFeedListVideoAdapter;
import kalaathon.com.utils.SectionsPagerAdapter;
import kalaathon.com.utils.UniversalImageLoader;

public class homeactivity extends AppCompatActivity implements MainFeedListAdapter.OnLoadMoreItemsListener,
        MainFeedListVideoAdapter.OnLoadMoreVideoListener {

    private Context mContext = homeactivity.this;
    private static final int ACTIVITY_NUM = 0;
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private LinearLayout mRelativeLayout;
    private Date currenttime=new Date();
    private static long back_pressed;
    private AppUpdateManager appUpdateManager;
    @Override
    public void onLoadMoreVideos() {

        Fragfeed_video fragment = (Fragfeed_video) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.view_container + ":" + mViewPager.getCurrentItem());
        if (fragment != null) {
            fragment.displayMoreVideos();
        }
    }

    @Override
    public void onLoadMoreItems() {
        try {
            Fragfeed_photo fragment = (Fragfeed_photo) getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.view_container + ":" + mViewPager.getCurrentItem());
            if (fragment != null) {
                fragment.displayMorePhotos();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeactivity);
        mFrameLayout = (FrameLayout) findViewById(R.id.frame_container);
        mRelativeLayout = (LinearLayout) findViewById(R.id.rellayoutparent);

        if(!ImageLoader.getInstance().isInited())
            initImageLoader();

        setupViewPager();
        setupBottomNavigationView();
        checkForUpdates();
    }

    private void checkForUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(mContext);

        com.google.android.play.core.tasks.
                Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    /*&& appUpdateInfo.clientVersionStalenessDays() != null
                    && appUpdateInfo.clientVersionStalenessDays() > 1*/) {
                Log.e("Custom", "update available: " );
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            99);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else Log.e("Custom", "not available: " );
        });
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
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
            showLayout();
        }
        else {
            if (back_pressed + 2000 > System.currentTimeMillis()) finishAffinity();
            else Toast.makeText(getBaseContext(), "Press back again to exit!", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
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
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx,1);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(R.drawable.ic_homedark);
    }

    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragfeed_photo());
        adapter.addFragment(new Fragfeed_video());

        mViewPager = (ViewPager) findViewById(R.id.view_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("Photos");
        tabLayout.getTabAt(1).setText("Videos");

    }

    public void onVideoThreadSelected(Video video, String callingActivity) {

        ViewNextFragVideo fragment = new ViewNextFragVideo();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.dbname_video), video);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
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
        // shorten the link
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
        Log.e("Custom", "onResume: " );
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(
                appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    this,
                                    99);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 99)
        {
            Log.e("Custom", "onActivityResult: "+resultCode );
        }
    }
}