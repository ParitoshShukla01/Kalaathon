package kalaathon.com.upload;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Date;

import kalaathon.com.R;
import kalaathon.com.image_Filters.Edit_Activity;
import kalaathon.com.login.phone;
import kalaathon.com.utils.CheckVideoFormat;
import kalaathon.com.utils.CustomAdapter;
import kalaathon.com.utils.FirebaseMethods;
import kalaathon.com.utils.Permissions;

public class shareActivity extends AppCompatActivity {
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private Context mContext=shareActivity.this;

    private Spinner mSpinner;
    private ImageView mImageView,mCross,edit;
    private EditText mEditText,luckycode;
    private TextView mShare,getluckycode,reset;
    private int isimage=1;
    private String path;
    ProgressBar mProgressBar,share_progress;
    private VideoView mVideoView;
    private String specialCode="false";
    private LinearLayout mLayout;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private String category;
    private int isCategorySelected=-1;
    private static final int imggallery_code = 1000;
    private static final int imgcamera_code = 1001;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE=1002;

    private MediaPlayer mMediaPlayer = null;
    private long duration=0;
    private Uri imguri;
    FFmpeg ffmpeg;
    String outputFileAbsolutePath;
    Bitmap thumbnail;
    private String contest,edit_type,edit_path;
    private CustomAdapter customAdapter;

    private String[] arr = {"Category :", "Art", "Dance", "Express Yourself", "Memes", "Music", "Photography", "Writing"};
    private int []icons={0,
            R.drawable.ic_art,
            R.drawable.ic_dance,
            R.drawable.ic_express,
            R.drawable.ic_meme,
            R.drawable.ic_music,
            R.drawable.ic_photography,
            R.drawable.ic_writing};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        contest=getIntent().getExtras().getString("contest");
        luckycode=findViewById(R.id.set_lucky_code);
        getluckycode=findViewById(R.id.get_lucky_code);
        mLayout=findViewById(R.id.get_lucky_code_layout);
        reset=findViewById(R.id.reset);
        reset.setVisibility(View.INVISIBLE);
        luckycode.setVisibility(View.GONE);
        mLayout.setVisibility(View.GONE);

        init();
        mFirebaseMethods=new FirebaseMethods(mContext);
        setupFirebaseAuth();
        getsdcardinfo();

        Query query=myRef.child("contest").child("code");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class).equals("false")){
                    luckycode.setVisibility(View.GONE);
                    mLayout.setVisibility(View.GONE);
                }
                else{
                    if(contest.equals("free")) {
                        luckycode.setVisibility(View.VISIBLE);
                        mLayout.setVisibility(View.VISIBLE);
                        specialCode = dataSnapshot.getValue(String.class);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getluckycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=instagramProfile(getPackageManager(),"https://www.instagram.com/kalaathon/");
                startActivity(intent);
            }
        });

        customAdapter=new CustomAdapter(getApplicationContext(),icons,arr)
        {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        mSpinner.setAdapter(customAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                isCategorySelected=adapterView.getSelectedItemPosition();
                category=arr[isCategorySelected];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                isCategorySelected=-1;
            }
        });

        if(getIntent().hasExtra("path"))
        {
            edit_path=getIntent().getStringExtra("path");
            edit_type=getIntent().getStringExtra("desc");
            int spnr=getIntent().getIntExtra("pos",-1);
            if(spnr>0) {
                mSpinner.setSelection(spnr);
                isCategorySelected=spnr;
                category=arr[isCategorySelected];
            }
            try {
                path = getPath(Uri.parse(edit_path));
                mEditText.setText(edit_type);
                mImageView.setImageURI(Uri.parse(edit_path));
                isimage=1;
            } catch (Exception e) {
                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        if(!checkPermissionsArray(Permissions.PERMISSIONS)){
            Intent i=new Intent(mContext,uploadActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Toast.makeText(mContext, "Allow Permissions", Toast.LENGTH_LONG).show();
        }
        else
        {
            //----------------------------------------onClickListner--------------------------------------------

            mCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String[] items = {"Click Picture","Record Video","Upload image","Upload Video"};
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("Upload from :");
                    dialog.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (i == 0)
                                pickcamera();
                            if(i==1)
                                recordVideo();
                            if (i == 2)
                                pickgalleryimage();
                            if (i==3)
                                pickgalleryvideo();
                        }
                    });
                    dialog.create().show();
                }
            });
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    edit.setVisibility(View.GONE);
                    mVideoView.setVideoURI(null);
                    mVideoView.setVisibility(View.GONE);
                    mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            return true;
                        }
                    });
                    if(ffmpeg!=null) {
                        if (ffmpeg.isFFmpegCommandRunning())
                            ffmpeg.killRunningProcesses();
                        ffmpeg=null;
                    }
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setVisibility(View.GONE);
                    mImageView.setVisibility(View.VISIBLE);
                    mImageView.setImageResource(R.drawable.upload_gallery);
                    mShare.setVisibility(View.VISIBLE);
                    share_progress.setVisibility(View.GONE);
                    reset.setVisibility(View.INVISIBLE);
                }
            });

            mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(isCategorySelected<1)
                        Toast.makeText(mContext, "Choose a Valid Category", Toast.LENGTH_SHORT).show();
                    else if(path==null)
                        Toast.makeText(mContext, "No Media Found !", Toast.LENGTH_SHORT).show();
                    else
                    {
                        String luck=luckycode.getText().toString().trim().toLowerCase();
                        if (luck.isEmpty())
                        {
                            String category=arr[isCategorySelected];
                            String caption=mEditText.getText().toString().trim();
                            mShare.setVisibility(View.GONE);
                            share_progress.setVisibility(View.VISIBLE);
                            reset.setVisibility(View.GONE);
                            mCross.setVisibility(View.GONE);
                            edit.setVisibility(View.GONE);
                            if(isimage==1)
                            {
                                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo)
                                        ,caption,new Date().getTime(),path,null,mProgressBar,category,contest);
                            }
                            if(isimage==0)
                            {
                                mProgressBar.setVisibility(View.VISIBLE);
                                Uri uri=Uri.fromFile(new File(outputFileAbsolutePath));

                                mFirebaseMethods.uploadNewPhoto(getString(R.string.dbname_video),caption
                                        ,new Date().getTime(),uri.toString(),thumbnail,mProgressBar,category,contest);
                            }
                        }
                        else {
                            if(luck.equals(specialCode))
                            {
                                String category=arr[isCategorySelected];
                                String caption=mEditText.getText().toString().trim();
                                mShare.setVisibility(View.GONE);
                                share_progress.setVisibility(View.VISIBLE);
                                reset.setVisibility(View.GONE);
                                mCross.setVisibility(View.GONE);
                                edit.setVisibility(View.GONE);

                                myRef.child("insta_code").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(1);

                                if(isimage==1)
                                {
                                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo)
                                            ,caption,new Date().getTime(),path,null,mProgressBar,category,contest);
                                }
                                if(isimage==0)
                                {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    Uri uri=Uri.fromFile(new File(outputFileAbsolutePath));

                                    mFirebaseMethods.uploadNewPhoto(getString(R.string.dbname_video),caption
                                            ,new Date().getTime(),uri.toString(),thumbnail,mProgressBar,category,contest);
                                }
                            }
                            else Toast.makeText(mContext, "Not a lucky code.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
    }

    private void init()
    {
        mSpinner=findViewById(R.id.categorySpinner);
        mVideoView=findViewById(R.id.shareVideo);
        mCross=findViewById(R.id.ivCloseShare);
        mShare=findViewById(R.id.tvShare);
        mImageView=findViewById(R.id.shareimage);
        edit=findViewById(R.id.edit);
        edit.setVisibility(View.GONE);
        mEditText=findViewById(R.id.description_share);
        mProgressBar=findViewById(R.id.progress_share);
        share_progress=findViewById(R.id.share_progress);

    }

//-------------------------------------camera and gallery-------------------------------------------

    private void recordVideo() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri fileUri = Uri.fromFile(new File(getsdrecord()));
        intent.putExtra("android.intent.extra.durationLimit", 60);
        intent.putExtra("EXTRA_VIDEO_QUALITY",0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    private void pickcamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Kalaathon");
        imguri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imguri);
        startActivityForResult(cameraIntent, imgcamera_code);
    }

    private void pickgalleryvideo() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        // photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        startActivityForResult(photoPickerIntent,imggallery_code);
    }

    private void pickgalleryimage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        startActivityForResult(photoPickerIntent,imggallery_code);
    }

    public void loadlibrary()
    {
        if(ffmpeg ==null)
        {
            ffmpeg = FFmpeg.getInstance(mContext);
            try {
                ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                    @Override
                    public void onStart() {}

                    @Override
                    public void onFailure() {}

                    @Override
                    public void onSuccess() {
                        mShare.setVisibility(View.GONE);
                        outputFileAbsolutePath=getsdcardinfo();
                        String[] command = {"-y", "-i",path,"-r", "25","-b:v","2000k","-b:a","40000","-movflags","+faststart","-ac","2","-ar","22050","-preset", "superfast", outputFileAbsolutePath};
                        executecommand(command);
                    }

                    @Override
                    public void onFinish() {}
                });
            } catch (FFmpegNotSupportedException e) {
                // Handle if FFmpeg is not supported by device
                outputFileAbsolutePath=path;
            }
        }
    }

    public void executecommand(final String[] command)
    {
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setIndeterminate(true);
                }
                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setVisibility(View.GONE);
                    mShare.setVisibility(View.VISIBLE);
                    outputFileAbsolutePath=path;
                }

                @Override
                public void onSuccess(String message) {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setVisibility(View.GONE);
                    mShare.setVisibility(View.VISIBLE);}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.GONE);
            mShare.setVisibility(View.VISIBLE);
            outputFileAbsolutePath=path;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            edit.setVisibility(View.VISIBLE);
            reset.setVisibility(View.VISIBLE);
            if (requestCode == imggallery_code) {

                Uri contentURI = data.getData();

                if(CheckVideoFormat.accept(getPath(contentURI)))
                {
                    edit.setVisibility(View.GONE);
                    try {
                        path = getPath(contentURI);
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setDataSource(mContext, data.getData());
                        mMediaPlayer.prepare();
                        duration = mMediaPlayer.getDuration();
                        if(duration<=65000) {
                            mVideoView.setVisibility(View.VISIBLE);
                            mImageView.setVisibility(View.GONE);
                            mVideoView.setZOrderOnTop(true);
                            mVideoView.setBackgroundColor(Color.TRANSPARENT);
                            mVideoView.setVideoURI(data.getData());
                            mVideoView.requestFocus();
                            mVideoView.start();
                            mVideoView.setMediaController(new MediaController(shareActivity.this));
                            File file = new File(path);
                            long length = file.length();
                            length = length/1000;
                            if (length<=2500)
                                outputFileAbsolutePath=path;
                            else
                                loadlibrary();
                            thumbnail= ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                            isimage=0;
                        }else
                            Toast.makeText(mContext, "Video length exceeds 1 minute !", Toast.LENGTH_LONG).show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(mContext, "Unknown Format!", Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        if (mMediaPlayer != null) {
                            mMediaPlayer.reset();
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                        }
                    }
                }
                else {
                    path = getPath(data.getData());
                    /*mImageView.setImageURI(data.getData());*/
                    Glide.with(this).load(data.getData()).into(mImageView);
                    isimage=1;
                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(shareActivity.this, Edit_Activity.class);
                            intent.putExtra("path",path);
                            intent.putExtra("contest",contest);
                            intent.putExtra("pos",isCategorySelected);
                            intent.putExtra("desc",mEditText.getText().toString());
                            startActivity(intent);
                        }
                    });
                }
            }
            if (requestCode == imgcamera_code) {
                isimage=1;
                path=getPathcamera(imguri);
                /*mImageView.setImageURI(imguri);*/
                Glide.with(this).load(imguri).into(mImageView);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(shareActivity.this,Edit_Activity.class);
                        intent.putExtra("path",path);
                        intent.putExtra("contest",contest);
                        intent.putExtra("pos",isCategorySelected);
                        intent.putExtra("desc",mEditText.getText().toString());
                        startActivity(intent);

                    }
                });
            }
            if(requestCode==CAMERA_CAPTURE_VIDEO_REQUEST_CODE)
            {
                edit.setVisibility(View.GONE);
                try {
                    path=getsdrecord();
                    mVideoView.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.GONE);
                    mVideoView.setZOrderOnTop(true);
                    mVideoView.setBackgroundColor(Color.TRANSPARENT);
                    mVideoView.setVideoURI(Uri.fromFile(new File(path)));
                    mVideoView.requestFocus();
                    mVideoView.start();
                    mVideoView.setMediaController(new MediaController(shareActivity.this));
                    loadlibrary();
                    thumbnail= ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                    isimage=0;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(mContext, "Unknown Format!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getPathcamera(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null,null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public boolean checkPermissionsArray(String[] permissions){

        for (String check : permissions) {
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission){

        int permissionRequest = ActivityCompat.checkSelfPermission(shareActivity.this, permission);

        if (permissionRequest == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
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
                    Intent intent=new Intent(shareActivity.this, phone.class);
                    startActivity(intent);
                    finish();
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

    public String getsdcardinfo()
    {
        File downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if(downloadFolder==null)
            downloadFolder.mkdir();
        return downloadFolder+"/output.mp4";
    }
    public String getsdrecord()
    {
        File downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if(downloadFolder==null)
            downloadFolder.mkdir();
        return downloadFolder+"/record.mp4";
    }

    @Override
    protected void onDestroy() {
        if(ffmpeg!=null) {
            if (ffmpeg.isFFmpegCommandRunning())
                ffmpeg.killRunningProcesses();
            mProgressBar.setVisibility(View.GONE);
            ffmpeg=null;
        }
        super.onDestroy();
    }

    public  Intent instagramProfile(PackageManager pm, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            if (pm.getPackageInfo("com.instagram.android", 0) != null) {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                final String username = url.substring(url.lastIndexOf("/") + 1);
                intent.setData(Uri.parse("http://instagram.com/_u/" + username));
                intent.setPackage("com.instagram.android");
                return intent;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        intent.setData(Uri.parse(url));
        return intent;
    }
}
