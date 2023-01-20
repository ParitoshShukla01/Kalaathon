package kalaathon.com.utils;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kalaathon.com.R;
import kalaathon.com.start.OpenLink;
import kalaathon.com.models.Like;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.models.Video;
import kalaathon.com.profile.profileActivity;

public class ViewVideoFragment extends Fragment{

    private static final String TAG = "VideoVideoFragment";


    public ViewVideoFragment(){
        super();
        setArguments(new Bundle());
    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    //widgets
    // private VideoView mVideoView;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes,mCategory;
    private ImageView mBackArrow, mHeartRed, mHeartWhite, mProfileImage,share,delete,report;
    private PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;
    ProgressBar mVideoprogress;
    private ProgressBar mProgressBar;



    //vars
    private Video mVideo;
    private int mActivityNumber = 0;
    private String videoUsername = "";
    private String thumbnailUrl = "";
    private String videoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";
    private User mCurrentUser;
    Set<String> mStringSet;
    long count=0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_video, container, false);
        //mVideoView = (VideoView) view.findViewById(R.id.post_video);
        mPlayerView=view.findViewById(R.id.post_videoexo);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomnavigationviewbar);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption_vdo);
        mUsername = (TextView) view.findViewById(R.id.username_vdo);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted_vdo);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red_vdo);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart_vdo);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo_vdo);
        mLikes = (TextView) view.findViewById(R.id.image_likes_vdo);
        mHeart = new Heart(mHeartWhite, mHeartRed);
        mCategory=(TextView)view.findViewById(R.id.categoryvdo);
        mVideoprogress=view.findViewById(R.id.vdoprogress);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        share=view.findViewById(R.id.sharevdo);
        delete=view.findViewById(R.id.ivEllipses);
        report=view.findViewById(R.id.report);
        mProgressBar=view.findViewById(R.id.video_del_progress);

        if(getActivityNumFromBundle()==0)
            mBackArrow.setVisibility(View.GONE);
        setupFirebaseAuth();
        if (mAuth.getUid().equals(getVideoFromBundle().getUser_id()))
            delete.setVisibility(View.VISIBLE);
        else
            delete.setVisibility(View.GONE);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setupBottomNavigationView();
        getPlayer();

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sharelinktext  = "https://kalaathon.page.link/?"+
                        "link=https://www.kalaathon.com/"+getVideoFromBundle().getVideo_id()+"/"+getVideoFromBundle().getUser_id()+"2"+
                        "&afl=https://play.google.com/store/apps/details?id="+getContext().getPackageName()+
                        "&ofl=https://www.kalaathon.com/"+
                        "&apn="+getContext().getPackageName()+
                        "&st="+"Kalaathon"+
                        "&sd="+"Check it out!"+
                        "&si="+"https://kalaathon.com/assets/images/left-image.png";

                if(mActivityNumber==0)
                    ((OpenLink)getContext()).share(sharelinktext);
                else
                    ((profileActivity)getContext()).share(sharelinktext);
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query1 = myRef.child("report")
                        .child(getVideoFromBundle().getVideo_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            Toast.makeText(getContext(), "Already reported.", Toast.LENGTH_SHORT).show();
                        else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
                            builder.setTitle("Report Post")
                                    .setMessage("Are you sure you want to report this post ?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            myRef.child("report")
                                                    .child(getVideoFromBundle().getVideo_id())
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(1);

                                            Toast.makeText(getContext(), "This Post has been reported!", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    }).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getUid().equals(getVideoFromBundle().getUser_id())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
                    builder.setTitle("Delete Post !")
                            .setMessage("Are you sure you want to delete this post ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance().getReference().getStorage();
                                    StorageReference storageReference = firebaseStorage.getReferenceFromUrl(getVideoFromBundle().getVideo_path());
                                    StorageReference storageReference2=firebaseStorage.getReferenceFromUrl(getVideoFromBundle().getThumbnail());
                                    storageReference .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            if(getContext()!=null)
                                            {
                                                storageReference2.delete();
                                                myRef.child(getResources().getString(R.string.dbname_user_videos))
                                                        .child(FirebaseAuth.getInstance().getCurrentUser()
                                                                .getUid()).child(getVideoFromBundle().getVideo_id()).removeValue();
                                                myRef.child(getResources().getString(R.string.dbname_video))
                                                        .child(getVideoFromBundle().getVideo_id()).removeValue();
                                                myRef.child("trend").child("video").child(getVideoFromBundle().getVideo_id()).removeValue();
                                                //add back to stack
                                                mProgressBar.setVisibility(View.GONE);
                                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                                getActivity().onBackPressed();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            try {
                                                if(getContext()!=null) {
                                                    myRef.child(getResources().getString(R.string.dbname_user_videos))
                                                            .child(FirebaseAuth.getInstance().getCurrentUser()
                                                                    .getUid()).child(getVideoFromBundle().getVideo_id()).removeValue();
                                                    myRef.child(getResources().getString(R.string.dbname_video))
                                                            .child(getVideoFromBundle().getVideo_id()).removeValue();
                                                    myRef.child("trend").child("video").child(getVideoFromBundle().getVideo_id()).removeValue();

                                                    mProgressBar.setVisibility(View.GONE);
                                                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    getActivity().onBackPressed();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            })
                            .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                }
            }
        });

        return view;
    }

    private void getPlayer() {
        // URL of the video to stream
        String videourllink = getVideoFromBundle().getVideo_path();
        mActivityNumber = getActivityNumFromBundle();
        String video_id = getVideoFromBundle().getVideo_id();

        // Handler for the video player
        Handler mainHandler = new Handler();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // Create the player with previously created TrackSelector
        mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        // Load the default controller
        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();

        // Load the SimpleExoPlayerView with the created player
        mPlayerView.setPlayer(mPlayer);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(),
                Util.getUserAgent(getContext(), "Kalaathon"),
                defaultBandwidthMeter);
        DashMediaSource.Factory mediasourcefactory = new DashMediaSource.Factory(dataSourceFactory);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(
                Uri.parse(videourllink),
                dataSourceFactory,
                extractorsFactory,
                null,
                null);

        // Prepare the player with the source.
        mPlayer.prepare(videoSource);

        // Autoplay the video when the player is ready
        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                if(isLoading) {
                    mVideoprogress.setVisibility(View.VISIBLE);
                }
                else
                    mVideoprogress.setVisibility(View.GONE);

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(playbackState==Player.STATE_BUFFERING)
                {
                    mVideoprogress.setVisibility(View.VISIBLE);
                }
                else
                    mVideoprogress.setVisibility(View.GONE);
            }
        });
    }

    private void init(){
        try{

            mActivityNumber = getActivityNumFromBundle();
            String video_id =getVideoFromBundle().getVideo_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_videos))
                    .orderByChild(getString(R.string.field_video_id))
                    .equalTo(video_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Video newVideo = new Video();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        try {


                            newVideo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                            newVideo.setCategory(objectMap.get(getString(R.string.field_category)).toString());
                            newVideo.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                            newVideo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            newVideo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            newVideo.setVideo_path(objectMap.get(getString(R.string.field_video_path)).toString());

                            mVideo = newVideo;

                            getCurrentUser();
                            getVideoDetails();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }catch (NullPointerException e){

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()){
            init();
        }
    }

    private void getLikesString(){

        ArrayList mUserList=new ArrayList<>();
        mStringSet=new HashSet<>();
        try{
            if(isAdded() && getContext()!=null)
            {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference
                        .child(getResources().getString(R.string.dbname_videos))
                        .child(mVideo.getVideo_id())
                        .child(getResources().getString(R.string.field_likes));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            mStringSet.add(singleSnapshot.getValue(User.class).getUser_id());
                        }
                        mLikes.setVisibility(View.VISIBLE);
                        count=mStringSet.size();
                        if (count<1000)
                        {
                            if(count==1)
                                mLikes.setText((mStringSet.size())+" Like");
                            else
                                mLikes.setText((mStringSet.size())+" Likes");
                        }
                        else
                            mLikes.setText(new CustomTimeStamp().coolFormat(mStringSet.size(),0)+" Likes");

                        if(mStringSet.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            mLikedByCurrentUser = true;
                        }else{
                            mLikedByCurrentUser = false;
                        }
                        setupWidgets();
                        if(!dataSnapshot.exists()){
                            mLikesString = "";
                            mLikedByCurrentUser = false;
                            setupWidgets();
                            mLikes.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUserList.clear();
                        mUserList.addAll(mStringSet);

                        if(mActivityNumber==0)
                        {
                            mPlayer.setPlayWhenReady(false);
                            mPlayer.getPlaybackState();
                            ((OpenLink)getContext()).hideLayout();
                            ((OpenLink)getContext()).onLikesSelected(mUserList,"Open Link");
                        }
                        else
                        {
                            ((profileActivity)getContext()).hideLayout();
                            ((profileActivity)getContext()).onLikesSelected(mUserList,
                                    getContext().getString(R.string.profile_activity));
                            mPlayer.setPlayWhenReady(false);
                            mPlayer.getPlaybackState();
                        }
                    }
                });
            }
        }catch (NullPointerException e){

            mLikesString = "";
            mLikedByCurrentUser = false;
            setupWidgets();
            mLikes.setVisibility(View.GONE);

        }
    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_videos))
                    .child(mVideo.getVideo_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        /*String keyID = singleSnapshot.getKey();*/
                        //case1: Then user already liked the photo
                        if(mLikedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            new MyAsyncTask(-1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            myRef.child(getString(R.string.dbname_videos))
                                    .child(mVideo.getVideo_id())
                                    .child(getString(R.string.field_likes))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_videos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mVideo.getVideo_id())
                                    .child(getString(R.string.field_likes))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }
                        //case2: The user has not liked the photo
                        else if(!mLikedByCurrentUser){
                            //add new like
                            addNewLike();
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(){

        new MyAsyncTask(1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        /*String newLikeID = myRef.push().getKey();*/
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_videos))
                .child(mVideo.getVideo_id())
                .child(getString(R.string.field_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_videos))
                .child(mVideo.getUser_id())
                .child(mVideo.getVideo_id())
                .child(getString(R.string.field_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void getVideoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mVideo.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                //setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupWidgets() throws NullPointerException{

        String timestampDiff = getTimestamp(mVideo);
        mTimestamp.setText(timestampDiff);

        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mCategory.setText(mVideo.getCategory());
        if(mVideo.getCaption().trim().equals(""))
            mCaption.setVisibility(View.GONE);
        else{
            try {

                mCaption.setVisibility(View.VISIBLE);
                String text = mVideo.getCaption().trim();
                SpannableString hashText = new SpannableString(text);
                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                while (matcher.find()) {
                    hashText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),R.color.hashtag)), matcher.start(), matcher.end(), 0);
                }
                mCaption.setText(hashText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
        else{
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }


    }

    private String getTimestamp(Video video) {
        final String time = video.getDate_created();
        String[] arr = time.split("_");
        return arr[0];

    }

    /**
     * retrieve the activity number from the incoming bundle from profileActivity interface
     * @return
     */
    private int getActivityNumFromBundle(){


        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        }else{
            return 0;
        }
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Video getVideoFromBundle(){
        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getParcelable("VIDEO");
        }else{
            return null;
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        int add;
        public MyAsyncTask(int add) {
            this.add=add;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            Date old= null;
            try {
                if(mActivityNumber==0)
                {
                    if (add == 1 && !getVideoFromBundle().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        ((OpenLink)getContext()).notification(mCurrentUser.getUsername(),getVideoFromBundle().getUser_id()
                                ,getVideoFromBundle().getVideo_id(),"2");

                    old = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",Locale.US).parse(Objects.requireNonNull(getVideoFromBundle()).getDate_created());
                    if (((OpenLink) getContext()).withinSeven(Objects.requireNonNull(old))) {
                        myRef.child("trend")
                                .child("video")
                                .child(getVideoFromBundle().getVideo_id())
                                .child("like")
                                .setValue(-1 * (count + add));

                        this.add = 0;

                    }
                }
                else {
                    if (add == 1 && !getVideoFromBundle().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        ((profileActivity)getContext()).notification(mCurrentUser.getUsername(),getVideoFromBundle().getUser_id()
                                ,getVideoFromBundle().getVideo_id(),"2");

                    old = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",Locale.US).parse(Objects.requireNonNull(getVideoFromBundle()).getDate_created());
                    if (((profileActivity) getContext()).withinSeven(Objects.requireNonNull(old))) {
                        myRef.child("trend")
                                .child("video")
                                .child(getVideoFromBundle().getVideo_id())
                                .child("like")
                                .setValue(-1 * (count + add));
                        this.add = 0;

                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private void setupBottomNavigationView(){

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(),bottomNavigationView,0);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

       /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){


        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in

                } else {
                    // User is signed out

                }
                // ...
            }
        };


    }

   /* @Override
    public void onAttach(@NonNull Context context) {
        getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        super.onDetach();
    }*/

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }


    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPause() {
        releasePlayer();
        super.onPause();
    }

}