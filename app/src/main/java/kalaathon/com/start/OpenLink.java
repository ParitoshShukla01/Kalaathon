package kalaathon.com.start;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kalaathon.com.R;
import kalaathon.com.SendNotification;
import kalaathon.com.home.homeactivity;
import kalaathon.com.list.LikesList;
import kalaathon.com.models.Like;
import kalaathon.com.models.Photo;
import kalaathon.com.models.Video;
import kalaathon.com.utils.CustomTimeStamp;
import kalaathon.com.utils.UniversalImageLoader;
import kalaathon.com.utils.ViewPostFragment;
import kalaathon.com.utils.ViewVideoFragment;

public class OpenLink extends AppCompatActivity {

    private String id,type,user_id;
    Photo photo=new Photo();
    Video video=new Video();
    private Date currenttime=new Date();
    RelativeLayout mRelativeLayout;
    FrameLayout mFrameLayout;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_link);
        mRelativeLayout=findViewById(R.id.openrel);
        mFrameLayout=findViewById(R.id.openframe);
        mContext= OpenLink.this;

        if(!ImageLoader.getInstance().isInited())
            initImageLoader();

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null) {
            this.id = bundle.getString("id");
            this.type = bundle.getString("type");
            this.user_id=bundle.getString("user_id");
        }
        else
        {
            back();
        }

        if(type.equals("1"))
        {
            getPhoto();

        }
        else if(type.equals("2"))
        {
            getVideo();
        }
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(OpenLink.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getVideo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_videos))
                .child(user_id)
                .child(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();

                try {
                    video.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    video.setCategory(objectMap.get("category").toString());
                    video.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                    video.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    video.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    video.setVideo_path(objectMap.get(getString(R.string.field_video_path)).toString());

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : dataSnapshot
                            .child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    video.setLikes(likesList);
                } catch (Exception e) {
                    Toast.makeText(OpenLink.this, "Post doesn't exists !", Toast.LENGTH_SHORT).show();

                }

                ViewVideoFragment fragment = new ViewVideoFragment();
                Bundle args = new Bundle();
                args.putParcelable("VIDEO", video);
                args.putInt(getString(R.string.activity_number), 0);
                fragment.setArguments(args);
                FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.commitAllowingStateLoss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError){
                Toast.makeText(OpenLink.this, "Post doesn't exists !", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getPhoto() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(user_id)
                .child(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();

                try {
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setCategory(objectMap.get("category").toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : dataSnapshot
                            .child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Post doesn't exists !", Toast.LENGTH_SHORT).show();

                }
                ViewPostFragment fragment = new ViewPostFragment();
                Bundle args = new Bundle();
                args.putParcelable("PHOTO", photo);
                args.putInt(getString(R.string.activity_number), 0);
                fragment.setArguments(args);
                FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.commitAllowingStateLoss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError){
                Toast.makeText(mContext, "Post doesn't exists !", Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void notification(String username,String user_id,String media_id,String type)
    {
        new SendNotification().sendNotification("New Like ",username+" liked your post.",user_id,media_id,type,mContext);
    }

    public void onLikesSelected(ArrayList<String> user, String callingActivity)
    {
        LikesList frag=new LikesList();
        Bundle args = new Bundle();
        args.putStringArrayList(getString(R.string.dbname_photos), user);
        args.putString("Activity",callingActivity);
        frag.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.openframe, frag);
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
                            // share app dialog
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(intent);
                        } else {
                            // Error
                            Toast.makeText(OpenLink.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }else
            finish();
    }
    private void back()
    {
        Intent i=new Intent(this, homeactivity.class);
        startActivity(i);
        finish();

    }

    public void hideLayout() {
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout() {
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
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
    }
}
