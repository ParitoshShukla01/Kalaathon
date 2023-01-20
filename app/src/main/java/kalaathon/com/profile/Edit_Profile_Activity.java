package kalaathon.com.profile;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.hdodenhof.circleimageview.CircleImageView;
import kalaathon.com.R;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.models.UserSettings;
import kalaathon.com.utils.FirebaseMethods;
import kalaathon.com.utils.Permissions;
import kalaathon.com.utils.UniversalImageLoader;

public class Edit_Profile_Activity extends AppCompatActivity {


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private EditText mUsername,mName,mMail,mDescription;
    private TextView mchangePhoto;
    private ProgressBar mProgressBar;
    private UserSettings mUserSettings;
    private ImageView mSaveep;
    private CircleImageView mProfilePhoto;
    private int check;

    private Uri imguri;
    private String path=null;
    private static final int imggallery_code = 1000;
    private static final int imgcamera_code = 1001;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profilelayout);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        mContext=Edit_Profile_Activity.this;
        mProfilePhoto=findViewById(R.id.profile_photo_edit);
        mUsername=findViewById(R.id.username_edit);
        mName=findViewById(R.id.display_name_edit);
        mDescription=findViewById(R.id.description_edit);
        mMail=findViewById(R.id.email_edit);
        mchangePhoto=findViewById(R.id.changeProfilePhoto_edit);
        mProgressBar=findViewById(R.id.progress_edit);
        mFirebaseMethods=new FirebaseMethods(mContext);
        mSaveep=findViewById(R.id.save_changes_ep);
        setupFirebaseAuth();
        ImageView backarrow=findViewById(R.id.back_arrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(mContext,profileActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });

        mSaveep.setVisibility(View.GONE);
        mSaveep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                check=0;
                saveProfileSettings();
            }
        });


        mchangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkPermissionsArray(Permissions.PERMISSIONS)){
                    Toast.makeText(mContext, "Allow Permissions", Toast.LENGTH_LONG).show();
                    verifyPermissions(Permissions.PERMISSIONS);
                }
                else {

                    String[] items = {"Camera", "Gallery"};
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("Upload from :");
                    dialog.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (i == 0)
                                pickcamera();
                            if (i == 1)
                                pickgallery();
                        }
                    });
                    dialog.create().show();
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == imggallery_code) {
                path = getPath(data.getData());
                /*mProfilePhoto.setImageURI(data.getData());*/
                Glide.with(this).load(data.getData()).into(mProfilePhoto);
            }
            if(requestCode==imgcamera_code)
            {
                path=getPathcamera(imguri);
                Glide.with(this).load(imguri).into(mProfilePhoto);
                /*mProfilePhoto.setImageURI(imguri);*/
            }
        }
    }

    private String getPathcamera(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null,null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
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

    private void pickcamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Kalaathon");
        imguri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imguri);
        startActivityForResult(cameraIntent, imgcamera_code);
    }

    private void pickgallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent,imggallery_code);
    }

    private void setProfileWidgets(UserSettings userSettings)
    {
        UserAccountSettings settings=userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");
        mName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescription());
        mMail.setText(settings.getEmail_id());
        mProgressBar.setVisibility(View.GONE);
        mUserSettings = userSettings;
        mSaveep.setVisibility(View.VISIBLE);
    }

    private void saveProfileSettings(){
        final String displayName = mName.getText().toString();
        final String username = mUsername.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mMail.getText().toString();

        //case1: if the user made a change to their username
        if(!mUserSettings.getSettings().getUsername().equals(username)){
            if(username.toLowerCase().trim().length()>1)
            checkIfUsernameExists(username.toLowerCase().trim());
            else
                Toast.makeText(mContext, "Username too short!", Toast.LENGTH_SHORT).show();
        }
        if(!mUserSettings.getSettings().getEmail_id().equals(email)){
            isValidEmail(email);
        }
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            //update displayname
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null);
            check=1;
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            //update description
            mFirebaseMethods.updateUserAccountSettings(null, description, null);
            check=1;
        }
        if((!mUserSettings.getSettings().getProfile_photo().equals(path)) && path!=null)
        {
            Toast.makeText(mContext, ""+path, Toast.LENGTH_SHORT).show();
            mFirebaseMethods.uploadNewPhoto(mContext.getString(R.string.profile_photo),null
                    ,0,path,null,mProgressBar,"register",null);
            check=2;
        }
        if(check==1) {
            Toast.makeText(mContext, "Changes updated ", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
        }
        else if(check==2)
            Toast.makeText(mContext, "Changing Profile photo", Toast.LENGTH_SHORT).show();
        else
            mProgressBar.setVisibility(View.GONE);
    }

    private void checkIfUsernameExists(final String username) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    //add the username
                    Toast.makeText(mContext, "Changes updated ", Toast.LENGTH_SHORT).show();
                    mFirebaseMethods.updateUsername(username);
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Toast.makeText(mContext, "That username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private  void isValidEmail(String target) {
        if (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
            mFirebaseMethods.updateUserAccountSettings(null, null, target);
            check=1;
        }
        else
            Toast.makeText(mContext, "Invalid E-mail", Toast.LENGTH_SHORT).show();
    }

    /*--------------------------------------------------firebase----------------------------------------------*/

    private void setupFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
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

    public boolean checkPermissionsArray(String[] permissions){

        for (String check : permissions) {
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission){

        int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);

        if (permissionRequest == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == VERIFY_PERMISSIONS_REQUEST) {
            // for each permission check if the user granted/denied them
            // you may want to group the rationale in a single dialog,
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale( permission );
                    if (! showRationale) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, VERIFY_PERMISSIONS_REQUEST);
                        return;
                    }
                }
            }
        }
    }
    public void verifyPermissions(String[] permissions){
        ActivityCompat.requestPermissions(
                Edit_Profile_Activity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
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
