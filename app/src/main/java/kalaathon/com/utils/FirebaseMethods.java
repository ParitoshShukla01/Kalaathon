package kalaathon.com.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import kalaathon.com.R;
import kalaathon.com.home.homeactivity;
import kalaathon.com.login.register;
import kalaathon.com.models.Photo;
import kalaathon.com.models.Trending;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.models.UserSettings;
import kalaathon.com.models.Video;

public class FirebaseMethods {

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;
    public final String FIREBASE_IMAGE_STORAGE = "photos/users/";
    public final String FIREBASE_VIDEO_STORAGE = "videos/users/";
    private Context mContext;
    private double mPhotoUploadProgress = 0;
    private String time;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        mContext = context;
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    //-----------------------------------------------Image------------------------------------------------

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds: dataSnapshot
                .child("user_photos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;
    }

    public int getVideoCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds: dataSnapshot
                .child("user_videos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;
    }

    private String getTimestamp(){
        CustomTimeStamp obj=new CustomTimeStamp();
        try {
            this.time=obj.printTime(mContext);
        } catch (IOException e) {
            e.printStackTrace();
            Calendar c=Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'_'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            Date date=c.getTime();
            this.time= sdf.format(date);
        }
        return time;
    }

    private void addPhotoToDatabase(String caption, String url,String category,String contest_type){
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        String timestamp=getTimestamp();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(timestamp);
        photo.setImage_path(url);
        photo.setCategory(category);
        photo.setContest(contest_type);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        Trending trending=new Trending();
        trending.setTime(timestamp);
        trending.setLike(0);
        trending.setType(contest_type);
        trending.setCategory(category);
        myRef.child("trend").child("photo").child(newPhotoKey).setValue(trending);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

        Intent intent = new Intent(mContext, homeactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    private void addVideoToDatabase(String caption, String url,String category,String contest_type,String thumbnail){
        String newVideoKey = myRef.child(mContext.getString(R.string.dbname_video)).push().getKey();
        String timestamp=getTimestamp();
        Video video=new Video();
        video.setCaption(caption);
        video.setDate_created(timestamp);
        video.setVideo_path(url);
        video.setCategory(category);
        video.setContest(contest_type);
        video.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        video.setVideo_id(newVideoKey);
        video.setThumbnail(thumbnail);


        Trending trending=new Trending();
        trending.setLike(0);
        trending.setTime(timestamp);
        trending.setType(contest_type);
        trending.setCategory(category);
        myRef.child("trend").child("video").child(newVideoKey).setValue(trending);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_videos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child(newVideoKey).setValue(video);
        myRef.child(mContext.getString(R.string.dbname_video)).child(newVideoKey).setValue(video);

        Toast.makeText(mContext, "Successfuly uploaded", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mContext, homeactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }
    //---------------------------------------this upload both image & video ------------------------------------
    public void uploadNewPhoto(String photoType, final String caption,final long count, final String imgUrl, Bitmap bm,
                               final ProgressBar progressBar, final String category, final String contest_type)
    {
        if(photoType.equals(mContext.getString(R.string.new_photo)))
        {
            final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference=mStorageReference
                    .child(FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo"+(count+1));

            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 80);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageReference.child(FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo"+(count+1))
                            .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String firebaseUrl=task.getResult().toString();
                            addPhotoToDatabase(caption, firebaseUrl,category,contest_type);
                        }
                    });
                    Toast.makeText(mContext, "Successfuly uploaded", Toast.LENGTH_SHORT).show();

                    //add the new photo to 'photos' node and 'user_photos' node
                    //addPhotoToDatabase(caption, firebaseUrl.toString());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress((int)progress);
                }
            });
        }
        else if(photoType.equals(mContext.getString(R.string.profile_photo)))
        {
            final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference=mStorageReference
                    .child(FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 80);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    mStorageReference.child(FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo")
                            .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String firebaseUrl= Objects.requireNonNull(task.getResult()).toString();
                            setProfilePhoto(firebaseUrl);

                            UserAccountSettings settings=new UserAccountSettings();
                            settings.setProfile_photo(firebaseUrl);

                            if(category.equals("register"))
                                new register().complete(mContext);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    // double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            progressBar.setVisibility(View.GONE);
        }
        else if(photoType.equals(mContext.getString(R.string.dbname_video))) {
            final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (imgUrl != null) {
                final String[] thumbnail = {null};
                if(bm != null) {
                    StorageReference storageReference = mStorageReference
                            .child(FIREBASE_VIDEO_STORAGE + "/" + user_id + "/photo" + (count + 1));
                    byte[] bytes = ImageManager.getBytesFromBitmap(bm, 80);

                    UploadTask uploadTask = null;
                    uploadTask = storageReference.putBytes(bytes);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mStorageReference.child(FIREBASE_VIDEO_STORAGE + "/" + user_id + "/photo" + (count + 1))
                                    .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    thumbnail[0] = Objects.requireNonNull(task.getResult()).toString();
                                }
                            });
                        }
                    });
                }
                StorageReference storageReference = mStorageReference
                        .child(FIREBASE_VIDEO_STORAGE + "/" + user_id + "/video" + (count + 1)+".mp4");

                UploadTask uploadTask = storageReference.putFile(Uri.parse(imgUrl));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mStorageReference.child(FIREBASE_VIDEO_STORAGE + "/" + user_id + "/video" + (count + 1)+".mp4")
                                .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String firebaseUrl = Objects.requireNonNull(task.getResult()).toString();
                                addVideoToDatabase(caption, firebaseUrl, category, contest_type,thumbnail[0]);
                            }
                        });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress((int)progress);
                    }
                });
                progressBar.setVisibility(View.GONE);
            }
        }

    }

    private void setProfilePhoto(String url){
        try{
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(mContext.getString(R.string.profile_photo))
                    .setValue(url);
        } catch (Exception e) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child(mContext.getString(R.string.profile_photo))
                    .setValue(url);
        }
    }

    //-------------------------------------------------------user details-----------------------------------
    public void updateUserAccountSettings(String displayName, String description, String email){


        if(displayName != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if(description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(email!=null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_email))
                    .setValue(email);
        }
    }

    public void updateUsername(String username){

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

}