package kalaathon.com.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kalaathon.com.R;
import kalaathon.com.SendNotification;
import kalaathon.com.list.FollowersList;
import kalaathon.com.list.FollowingList;
import kalaathon.com.list.LikesList;
import kalaathon.com.models.Photo;
import kalaathon.com.models.User;
import kalaathon.com.models.Video;
import kalaathon.com.utils.CustomTimeStamp;
import kalaathon.com.utils.UniversalImageLoader;
import kalaathon.com.utils.ViewPostFragment;
import kalaathon.com.utils.ViewProfileFragment;
import kalaathon.com.utils.ViewVideoFragment;

public class profileActivity extends AppCompatActivity implements photofrag.OnGridImageSelectedListener, videofrag.OnGridItemSelectedListener
        , Viewphoto.OnGridImageSelectedListener , Viewvideo.OnGridItemSelectedListener {

    private Context mContext;
    private static final int ACTIVITY_NUM=4;
    private Date currenttime=new Date();
    RelativeLayout mRelativeLayout;
    FrameLayout mFrameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        setContentView(R.layout.activity_profile);
        mRelativeLayout=findViewById(R.id.rellayout1);
        mFrameLayout=findViewById(R.id.frame_container);
        mContext=profileActivity.this;
        if(!ImageLoader.getInstance().isInited())
            initImageLoader();
        init();
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
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }
    }

    private void init(){
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.calling_activity))){
            if(intent.hasExtra(getString(R.string.intent_user))){
                User user = intent.getParcelableExtra(getString(R.string.intent_user));
                if(!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    //transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }else{
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = profileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    // transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }
            }else{
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = profileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment,"profilefragment");
            //  transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable("PHOTO", photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    public void onGridItemSelected(Video video, int activityNumber) {

        ViewVideoFragment fragment = new ViewVideoFragment();
        Bundle args = new Bundle();
        args.putParcelable("VIDEO", video);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_video_fragment));
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

    public void onfollowerslist(ArrayList<String> user, String callingActivity)
    {
        FollowersList frag=new FollowersList();
        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.dbname_photos), user);
        args.putString("Activity",callingActivity);
        frag.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, frag,"followerslist");
        transaction.addToBackStack("followerslist");
        transaction.commit();
    }

    public void onfollowinglist(ArrayList<String> user, String callingActivity)
    {
        FollowingList frag=new FollowingList();
        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.dbname_photos), user);
        args.putString("Activity",callingActivity);
        frag.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, frag);
        transaction.addToBackStack("Following List");
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
        mFrameLayout.removeAllViews();
        mRelativeLayout.removeAllViews();
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
    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(profileActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
}
