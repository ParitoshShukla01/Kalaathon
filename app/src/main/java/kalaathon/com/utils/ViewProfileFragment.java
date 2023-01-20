package kalaathon.com.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import kalaathon.com.R;
import kalaathon.com.SendNotification;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.models.UserSettings;
import kalaathon.com.profile.Viewphoto;
import kalaathon.com.profile.Viewvideo;
import kalaathon.com.profile.profileActivity;

//mpoints and grid view.
public class ViewProfileFragment extends Fragment  {

    private  final int ACTIVITY_NUM = 4;
    private  final int NUM_GRID_COLUMNS = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mFollow,mUnfollow, mDescription;
    private CircleImageView mProfilePhoto;
    private ImageView badge;
    private BottomNavigationViewEx bottomNavigationView;
    private  Context mContext;
    private LinearLayout followers,following;

    //vars
    private User mUser;
    public String currentUsername;
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;
    private ArrayList<String> followinglist;
    private ArrayList<String> followerslist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_seeprofile,container,false);
        mDisplayName = (TextView) view.findViewById(R.id.profile_name_underimage);
        mUsername = (TextView) view.findViewById(R.id.profileUserametoolbarsee);
        mFollow=view.findViewById(R.id.followuser);
        mUnfollow=view.findViewById(R.id.unfollowuser);
        mDescription = (TextView) view.findViewById(R.id.profile_description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_image_username);
        mPosts = (TextView) view.findViewById(R.id.tvpost);
        mFollowers = (TextView) view.findViewById(R.id.tvfollower);
        mFollowing = (TextView) view.findViewById(R.id.tvfollowing);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomnavigationviewbar);
        mContext = Objects.requireNonNull(getContext()).getApplicationContext();
        followers=view.findViewById(R.id.centre_layout);
        following=view.findViewById(R.id.lin_following);
        badge= view.findViewById(R.id.badge);
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        badge.setVisibility(View.GONE);
        mFirebaseMethods = new FirebaseMethods(mContext);
        getCurrentUsername();

        try{
            mUser = getUserFromBundle();
            init();
        }catch (NullPointerException e){
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                new SendNotification().sendNotification("New Follower",currentUsername+" started following you."
                        ,mUser.getUser_id(),null,null,mContext);

                setFollowing();
            }
        });


        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnfollowing();
            }
        });

        setupViewPager(view);
        setupBottomNavigationView();
        setupFirebaseAuth();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();
        getbadge();
        return view;
    }

    private void getbadge()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference.child("zbadge").orderByChild("user_id").equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    badge.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init(){
        //set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    UserSettings settings = new UserSettings();
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private User getUserFromBundle(){
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.intent_user));
        }else{
            return null;
        }
    }

    private void setProfileWidgets(UserSettings userSettings){
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        // mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());

    }

    private void isFollowing(){
        try {
            if (isAdded() && getContext()!=null) {
                setUnfollowing();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference.child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            setFollowing();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFollowersCount(){
        mFollowersCount = 0;
        followerslist=new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    followerslist.add(singleSnapshot.getValue(User.class).getUser_id());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
                followers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mUnfollow.getVisibility() == View.VISIBLE) {
                            if (!mFollowers.getText().toString().equals("0")) {
                                ((profileActivity) Objects.requireNonNull(getContext())).hideLayout();
                                ((profileActivity) getContext()).onfollowerslist(followerslist,
                                        getContext().getString(R.string.profile_activity));
                            } else
                                Toast.makeText(mContext, "No Followers Yet!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        mFollowingCount = 0;
        followinglist=new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                    followinglist.add(singleSnapshot.getValue(User.class).getUser_id());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));

                following.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mUnfollow.getVisibility() == View.VISIBLE) {
                            if (!mFollowing.getText().toString().equals("0")) {
                                ((profileActivity) Objects.requireNonNull(getContext())).hideLayout();
                                ((profileActivity) getContext()).onfollowinglist(followinglist,
                                        getContext().getString(R.string.profile_activity));
                            } else
                                Toast.makeText(mContext, "Not following anyone!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount(){
        mPostsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query query1=reference.child(getString(R.string.dbname_user_videos)).child(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing(){
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing(){
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
    }

    //Botom Navigation Menu
    private void setupBottomNavigationView()
    {
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationView,0);
        Menu menu=bottomNavigationView.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(R.drawable.ic_profiledark);

    }

    /*
   ------------------------------------ Firebase ---------------------------------------------
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

    private void setupViewPager(View view){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new Viewphoto(getUserFromBundle()));
        adapter.addFragment(new Viewvideo(getUserFromBundle()));

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.container_imgvdo);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("Photos");
        tabLayout.getTabAt(1).setText("Videos");

    }

    private void getCurrentUsername(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    isFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
