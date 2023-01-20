package kalaathon.com.profile;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.models.UserSettings;
import kalaathon.com.utils.BottomNavigationViewHelper;
import kalaathon.com.utils.FirebaseMethods;
import kalaathon.com.utils.SectionsPagerAdapter;
import kalaathon.com.utils.UniversalImageLoader;

public class ProfileFragment extends Fragment  {

    private final int ACTIVITY_NUM = 4;
    private  final int NUM_GRID_COLUMNS = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mDescription;
    private CircleImageView mProfilePhoto;
    private Toolbar toolbar;
    private ImageView profileMenu,badge;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;
    private LinearLayout followers,following;

    //vars
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;
    private ArrayList<String> followinglist;
    private ArrayList<String> followerslist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_profile,container,false);

        mDisplayName = (TextView) view.findViewById(R.id.profile_name_underimage);
        mUsername = (TextView) view.findViewById(R.id.profileUserametoolbar);
        mDescription = (TextView) view.findViewById(R.id.profile_description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_image_username);
        mPosts = (TextView) view.findViewById(R.id.tvpost);
        mFollowers = (TextView) view.findViewById(R.id.tvfollower);
        mFollowing = (TextView) view.findViewById(R.id.tvfollowing);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolbar);
        profileMenu = (ImageView) view.findViewById(R.id.profile_menu_dot);
        followers=view.findViewById(R.id.centre_layout);
        following=view.findViewById(R.id.lin_following);
        badge=view.findViewById(R.id.badge);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomnavigationviewbar);
        mContext = getContext();
        mFirebaseMethods = new FirebaseMethods(mContext);
        profileMenu.setVisibility(View.GONE);
        badge.setVisibility(View.GONE);

        setupViewPager(view);
        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        getFollowersCount();
        getPostsCount();
        getFollowingCount();
        getbadge();

        return view;
    }

    private void getbadge()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference.child("zbadge").orderByChild("user_id").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
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

    private void getFollowersCount(){
        mFollowersCount = 0;
        followerslist=new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
                        if(!mFollowers.getText().toString().equals("0")) {
                            ((profileActivity) getContext()).hideLayout();
                            ((profileActivity) getContext()).onfollowerslist(followerslist,
                                    getContext().getString(R.string.profile_activity));
                        }
                        else Toast.makeText(mContext, "No Followers Yet!", Toast.LENGTH_SHORT).show();
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
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
                        if(!mFollowing.getText().toString().equals("0")) {
                            ((profileActivity) Objects.requireNonNull(getContext())).hideLayout();
                            ((profileActivity) getContext()).onfollowinglist(followinglist,
                                    getContext().getString(R.string.profile_activity));
                        }
                        else Toast.makeText(mContext, "You aren't following anyone!", Toast.LENGTH_SHORT).show();

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
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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

        Query query1=reference.child(getString(R.string.dbname_user_videos)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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

    private void setProfileWidgets(UserSettings msettings)
    {
        UserAccountSettings settings=msettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescription());
        profileMenu.setVisibility(View.VISIBLE);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,AccountSettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("username",settings.getUsername());
                startActivity(intent);
            }
        });
    }

    private void setupToolbar(){
        ((profileActivity)getContext()).setSupportActionBar(toolbar);
    }

    //Botom Navigation Menu
    public void setupBottomNavigationView()
    {
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationView,5);
        Menu menu=bottomNavigationView.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(R.drawable.ic_profiledark);

    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    private void setupFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in

                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }
                // ...
            }
        };

        Query query1 = myRef.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mAuth.getCurrentUser().getUid());
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
        adapter.addFragment(new photofrag());
        adapter.addFragment(new videofrag());

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.container_imgvdo);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("Photos");
        tabLayout.getTabAt(1).setText("Videos");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toolbar.removeAllViews();
        onDestroyView();
        onDetach();
    }
}
