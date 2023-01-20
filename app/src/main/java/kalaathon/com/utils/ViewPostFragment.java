package kalaathon.com.utils;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kalaathon.com.R;
import kalaathon.com.start.OpenLink;
import kalaathon.com.models.Like;
import kalaathon.com.models.Photo;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.profile.profileActivity;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment(){
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
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes,mCategory;
    private ImageView mBackArrow, mHeartRed, mHeartWhite, mProfileImage,share,dots,report;
    private ProgressBar mProgressBar;


    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
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
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomnavigationviewbar);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);
        mLikes = (TextView) view.findViewById(R.id.image_likes);
        mCategory=(TextView)view.findViewById(R.id.category);
        dots=(ImageView)view.findViewById(R.id.ivThreeDots);
        share=view.findViewById(R.id.share_img);
        report=view.findViewById(R.id.report_photo);
        mProgressBar=view.findViewById(R.id.post_del_progress);
        if(getActivityNumFromBundle()==0)
            mBackArrow.setVisibility(View.GONE);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        setupFirebaseAuth();
        setupBottomNavigationView();
        if (mAuth.getUid().equals(getPhotoFromBundle().getUser_id()))
            dots.setVisibility(View.VISIBLE);
        else
            dots.setVisibility(View.GONE);

        dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getUid().equals(getPhotoFromBundle().getUser_id())) {
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
                                    StorageReference storageReference = firebaseStorage.getReferenceFromUrl(getPhotoFromBundle().getImage_path());
                                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            if(getContext()!=null) {
                                                myRef.child(getResources().getString(R.string.dbname_user_photos))
                                                        .child(FirebaseAuth.getInstance().getCurrentUser()
                                                                .getUid()).child(getPhotoFromBundle().getPhoto_id()).removeValue();
                                                myRef.child(getResources().getString(R.string.dbname_photos))
                                                        .child(getPhotoFromBundle().getPhoto_id()).removeValue();
                                                myRef.child("trend").child("photo").child(getPhotoFromBundle().getPhoto_id()).removeValue();
                                                //add back to stack
                                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                                getActivity().onBackPressed();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            try {
                                                if(getContext()!=null) {
                                                    myRef.child(getResources().getString(R.string.dbname_user_photos))
                                                            .child(FirebaseAuth.getInstance().getCurrentUser()
                                                                    .getUid()).child(getPhotoFromBundle().getPhoto_id()).removeValue();
                                                    myRef.child(getResources().getString(R.string.dbname_photos))
                                                            .child(getPhotoFromBundle().getPhoto_id()).removeValue();
                                                    myRef.child("trend").child("photo").child(getPhotoFromBundle().getPhoto_id()).removeValue();
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

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sharelinktext  = "https://kalaathon.page.link/?"+
                        "link=https://www.kalaathon.com/"+getPhotoFromBundle().getPhoto_id()+"/"+getPhotoFromBundle().getUser_id()+"1"+
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
                        .child(getPhotoFromBundle().getPhoto_id())
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
                                                    .child(getPhotoFromBundle().getPhoto_id())
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

        return view;
    }

    private void init(){
        try{
            //mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        try {
                            newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                            newPhoto.setCategory(objectMap.get(getString(R.string.field_category)).toString());
                            newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                            newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                            mPhoto = newPhoto;

                            getCurrentUser();
                            getPhotoDetails();
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
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getContext().getString(R.string.dbname_photos))
                    .child(getPhotoFromBundle().getPhoto_id())
                    .child(getContext().getString(R.string.field_likes));
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
                        ((OpenLink)getContext()).hideLayout();
                        ((OpenLink)getContext()).onLikesSelected(mUserList,"Open Link");
                    }
                    else
                    {
                        ((profileActivity)getContext()).hideLayout();
                        ((profileActivity)getContext()).onLikesSelected(mUserList,
                                getContext().getString(R.string.profile_activity));
                    }
                }
            });
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
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        //case1: Then user already liked the photo
                        if(mLikedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            new MyAsyncTask(-1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
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

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void getPhotoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
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
        String timestampDiff = getTimestamp(mPhoto);
        mTimestamp.setText(timestampDiff);
        if(mUserAccountSettings!=null) {
            UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
            mUsername.setText(mUserAccountSettings.getUsername());
        }
        mCategory.setText(mPhoto.getCategory());
        if(mPhoto.getCaption().trim().equals(""))
            mCaption.setVisibility(View.GONE);
        else{
            try {
                mCaption.setVisibility(View.VISIBLE);
                String text = mPhoto.getCaption().trim();
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

    private String getTimestamp(Photo photo) {
        final String time = photo.getDate_created();
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
    private Photo getPhotoFromBundle(){


        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getParcelable("PHOTO");
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
                if (mActivityNumber==0)
                {
                    if (add == 1 && !getPhotoFromBundle().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        ((OpenLink)getContext()).notification(mCurrentUser.getUsername(),getPhotoFromBundle().getUser_id()
                                ,getPhotoFromBundle().getPhoto_id(),"1");

                    old = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(getPhotoFromBundle().getDate_created());
                    if (((OpenLink) getContext()).withinSeven(old)) {
                        myRef.child("trend")
                                .child("photo")
                                .child(getPhotoFromBundle().getPhoto_id())
                                .child("like")
                                .setValue(-1 * (count + add));

                        this.add = 0;
                    }
                }
                else {
                    if (add == 1 && !getPhotoFromBundle().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        ((profileActivity)getContext()).notification(mCurrentUser.getUsername(),getPhotoFromBundle().getUser_id()
                                ,getPhotoFromBundle().getPhoto_id(),"1");

                    old = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(getPhotoFromBundle().getDate_created());
                    if (((profileActivity) getContext()).withinSeven(old)) {
                        myRef.child("trend")
                                .child("photo")
                                .child(getPhotoFromBundle().getPhoto_id())
                                .child("like")
                                .setValue(-1 * (count + add));
                        this.add=0;
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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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
}

