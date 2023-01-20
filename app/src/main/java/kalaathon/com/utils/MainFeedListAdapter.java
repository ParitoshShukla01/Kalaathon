package kalaathon.com.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import kalaathon.com.R;
import kalaathon.com.SendNotification;
import kalaathon.com.home.homeactivity;
import kalaathon.com.models.Like;
import kalaathon.com.models.Photo;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.profile.profileActivity;
import kalaathon.com.trending.TrendingActivity;


public class MainFeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "MainFeedListAdapter";

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    public String currentUsername = "";
    private int act;
    private String currentuser="";

    public MainFeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects,int act) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
        currentuser= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.act=act;
    }

    static class ViewHolder{
        CircleImageView mprofileImage;
        String likesString=null;
        TextView username, timeDetla, caption, likes, category;
        SquareImageView image;
        ImageView heartRed, heartWhite,share,report,mFollow,mUnfollow;

        UserAccountSettings settings = new UserAccountSettings();
        User user  = new User();
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
        Set<String> mStringSet;
        long count;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        // if(convertView == null){
        convertView =mInflater.inflate(mLayoutResource, parent, false);
        holder = new ViewHolder();

        holder.username = (TextView) convertView.findViewById(R.id.username);
        holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
        holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
        holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
        holder.share = (ImageView) convertView.findViewById(R.id.sharefeedphoto);
        holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
        holder.category = (TextView) convertView.findViewById(R.id.category);
        holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
        holder.timeDetla = (TextView) convertView.findViewById(R.id.image_time_posted);
        holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
        holder.report=(ImageView) convertView.findViewById(R.id.reportfeedphoto);
        holder.mFollow=(ImageView)convertView.findViewById(R.id.follow);
        holder.mUnfollow=(ImageView)convertView.findViewById(R.id.followed);

        convertView.setTag(holder);
       /* }
        else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        holder.photo = getItem(position);
        holder.detector = new GestureDetector(mContext, new GestureListener(holder));
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);

        //get the current users username (need for checking likes string)
        getCurrentUsername();

        if(act==1) {
            isFollowing(holder);

            holder.mFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_following))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(holder.photo.getUser_id())
                            .child(mContext.getString(R.string.field_user_id))
                            .setValue(holder.photo.getUser_id());

                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_followers))
                            .child(holder.photo.getUser_id())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(mContext.getString(R.string.field_user_id))
                            .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    new SendNotification().sendNotification("New Follower",currentUsername+" started following you."
                            ,holder.photo.getUser_id(),null,null,mContext);

                    setFollowing(holder);
                }
            });
            holder.mUnfollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_following))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(holder.photo.getUser_id())
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_followers))
                            .child(holder.photo.getUser_id())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .removeValue();
                    setUnfollowing(holder);
                }
            });
        }
        //get likes string
        getLikesString(holder);

        //set the caption
        if(getItem(position).getCaption().isEmpty())
            holder.caption.setVisibility(View.GONE);
        else {
            String text=getItem(position).getCaption().trim();
            SpannableString hashText = new SpannableString(text);
            Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
            while (matcher.find()) {
                hashText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),R.color.hashtag)), matcher.start(), matcher.end(), 0);
            }
            holder.caption.setText(hashText);
        }
        holder.category.setText(getItem(position).getCategory());

        //set the time it was posted
        String timestamp = getTimestamp(getItem(position));
        holder.timeDetla.setText(timestamp);

        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Pair pairs = new Pair<View, String>(holder.mprofileImage, "profile_transition");
                            if(act==0) {
                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((homeactivity) mContext, pairs);
                                Intent intent = new Intent(mContext, profileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                                mContext.startActivity(intent, activityOptions.toBundle());
                            }
                            else if(act==1)
                            {
                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((TrendingActivity) mContext, pairs);
                                Intent intent = new Intent(mContext, profileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                                mContext.startActivity(intent, activityOptions.toBundle());
                            }
                        }
                    });
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mprofileImage);
                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Pair pairs = new Pair<View, String>(holder.mprofileImage, "profile_transition");
                            if(act==0) {
                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((homeactivity) mContext, pairs);
                                Intent intent = new Intent(mContext, profileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                                mContext.startActivity(intent, activityOptions.toBundle());
                            }
                            else if(act==1)
                            {
                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((TrendingActivity) mContext, pairs);
                                Intent intent = new Intent(mContext, profileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                                mContext.startActivity(intent, activityOptions.toBundle());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the user object
        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String sharelinktext  = "https://kalaathon.page.link/?"+
                        "link=https://www.kalaathon.com/"+holder.photo.getPhoto_id()+"/"+holder.photo.getUser_id()+"1"+
                        "&afl=https://play.google.com/store/apps/details?id="+getContext().getPackageName()+
                        "&ofl=https://www.kalaathon.com/"+
                        "&apn="+getContext().getPackageName()+
                        "&st="+"Kalaathon"+
                        "&sd="+"Check it out!"+
                        "&si="+"https://kalaathon.com/assets/images/left-image.png";

                if(act==0)
                    ((homeactivity)mContext).share(sharelinktext);
                else if(act==1)
                    ((TrendingActivity)mContext).share(sharelinktext);
            }
        });

        holder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query1 = mReference.child("report")
                        .child(holder.photo.getPhoto_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            Toast.makeText(mContext, "Already reported.", Toast.LENGTH_SHORT).show();
                        else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
                            builder.setTitle("Report Post")
                                    .setMessage("Are you sure you want to report this post ?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            mReference.child("report")
                                                    .child(holder.photo.getPhoto_id())
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(1);

                                            Toast.makeText(mContext, "This Post has been reported!", Toast.LENGTH_SHORT).show();

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

        if(reachedEndOfList(position)){
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position){
        return position == getCount() - 1;
    }

    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        }catch (ClassCastException e){

        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){

        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {



            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        /*String keyID = singleSnapshot.getKey();*/

                        //case1: Then user already liked the photo
                        if(mHolder.likeByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            new MyAsyncTask(mHolder,-1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .removeValue();

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(final ViewHolder holder){

        new MyAsyncTask(holder,+1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        /*String newLikeID = mReference.push().getKey();*/
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
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
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLikesString(final ViewHolder holder){

        ArrayList mUserList=new ArrayList<>();
        holder.mStringSet=new HashSet<>();
        try{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        holder.mStringSet.add(singleSnapshot.getValue(User.class).getUser_id());
                    }
                    holder.likes.setVisibility(View.VISIBLE);
                    holder.count=holder.mStringSet.size();
                    if (holder.count<1000)
                    {
                        if (holder.count==1)
                            holder.likes.setText((holder.mStringSet.size())+" Like");
                        else
                            holder.likes.setText((holder.mStringSet.size())+" Likes");
                    }
                    else
                        holder.likes.setText(new CustomTimeStamp().coolFormat(holder.mStringSet.size(),0)+" Likes");

                    if(holder.mStringSet.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        holder.likeByCurrentUser = true;
                    }else{
                        holder.likeByCurrentUser = false;
                    }
                    setupLikesString(holder);
                    if(!dataSnapshot.exists()){
                        holder.likesString = "";
                        holder.likes.setVisibility(View.GONE);
                        holder.likeByCurrentUser = false;
                        setupLikesString(holder);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            holder.likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserList.clear();
                    mUserList.addAll(holder.mStringSet);
                    if(act==0)
                    {
                        ((homeactivity)mContext).hideLayout();
                        ((homeactivity)mContext).onLikesSelected(mUserList,
                                mContext.getString(R.string.home_activity));
                    }
                    else if(act==1)
                    {
                        ((TrendingActivity)mContext).hideLayout();
                        ((TrendingActivity)mContext).onLikesSelected(mUserList,
                                mContext.getString(R.string.trend_activity));
                    }
                }
            });
        }catch (NullPointerException e){

            holder.likesString = "";
            holder.likes.setVisibility(View.GONE);
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder);
        }
    }

    private void setupLikesString(final ViewHolder holder){
        if(holder.likeByCurrentUser){

            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }else{

            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
       /* if(likesString.length()>0) {
            holder.likes.setVisibility(View.VISIBLE);
            holder.likes.setText(likesString);
        }
        else
            holder.likes.setVisibility(View.GONE);*/
    }

    private String getTimestamp(Photo photo) {
        final String time = photo.getDate_created();
        String[] arr = time.split("_");
        return arr[0];

    }
    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        ViewHolder holder;
        int add;
        public MyAsyncTask(ViewHolder holder,int add) {
            this.holder=holder;
            this.add=add;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            Date old= null;
            try {
                if(act==0)
                {
                    if(add==1 && !holder.photo.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        ((homeactivity)mContext).notification(currentUsername,holder.user.getUser_id(),holder.photo.getPhoto_id(),"1");

                    old = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(holder.photo.getDate_created());
                    if(((homeactivity)mContext).withinSeven(old))
                    {
                        mReference.child("trend")
                                .child("photo")
                                .child(holder.photo.getPhoto_id())
                                .child("like")
                                .setValue(-1*(holder.count+add));
                        this.add=0;
                    }
                }
                else if(act==1) {
                    if(add==1 && !holder.photo.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))

                        ((TrendingActivity)mContext).notification(currentUsername,holder.user.getUser_id(),holder.photo.getPhoto_id(),"1");

                    old = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(holder.photo.getDate_created());
                    if(((TrendingActivity)mContext).withinSeven(old))
                    {
                        mReference.child("trend")
                                .child("photo")
                                .child(holder.photo.getPhoto_id())
                                .child("like")
                                .setValue(-1*(holder.count+add));
                        this.add=0;
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private void isFollowing(ViewHolder holder){
        try {
            if (mContext!=null) {
                setUnfollowing(holder);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference.child(mContext.getString(R.string.dbname_following))
                        .child(currentuser)
                        .orderByChild(mContext.getString(R.string.field_user_id)).equalTo(holder.photo.getUser_id());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            setFollowing(holder);
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

    private void setFollowing(ViewHolder holder){
        holder.mFollow.setVisibility(View.GONE);
        holder.mUnfollow.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing(ViewHolder holder){
        holder.mFollow.setVisibility(View.VISIBLE);
        holder.mUnfollow.setVisibility(View.GONE);
    }
}