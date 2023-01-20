package kalaathon.com.login;

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
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import de.hdodenhof.circleimageview.CircleImageView;
import kalaathon.com.R;
import kalaathon.com.home.homeactivity;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.utils.FirebaseMethods;
import kalaathon.com.utils.Permissions;

public class register extends AppCompatActivity {

    private Context mContext;
    private EditText mEmail, mName, mDescription, mUsername,refer;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private String username, name, description, email;
    private ImageView mImageView;
    private CircleImageView mCircleImageView;
    private TextView mTextView;

    private FirebaseMethods firebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    private String userID;

    private Uri imguri;
    private String path=null;
    private boolean check=false;
    private static final int imggallery_code = 1000;
    private static final int imgcamera_code = 1001;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();
        initWidgets();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        final String phone_number = intent.getStringExtra("phone_number");


        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }

        mTextView.setOnClickListener(new View.OnClickListener() {
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

        mImageView.setOnClickListener(new View.OnClickListener() {
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

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = String.valueOf(mUsername.getText()).trim().toLowerCase();
                name = String.valueOf(mName.getText());
                description = String.valueOf(mDescription.getText());
                email = String.valueOf(mEmail.getText());
                if (!checkInputs(username, name, description, email)) {
                    Toast.makeText(mContext, "All fields must be filled out.", Toast.LENGTH_SHORT).show();
                } else if (!isValidEmail(email)) {
                    Toast.makeText(mContext, "Invalid Email-ID", Toast.LENGTH_SHORT).show();
                }
                else if(path==null)
                {
                    Toast.makeText(mContext, "Upload Profile Picture!", Toast.LENGTH_SHORT).show();
                }
                else {
                    btnRegister.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    String code=refer.getText().toString().trim();
                    if(code.isEmpty())
                    {
                        Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("username").equalTo(username);
                        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount() > 0) {
                                    Toast.makeText(mContext, "Username already exists.", Toast.LENGTH_SHORT).show();
                                    mUsername.setError("Enter a different Username.");
                                    mProgressBar.setVisibility(View.GONE);
                                    btnRegister.setVisibility(View.VISIBLE);
                                } else {
                                    UserAccountSettings settings = new UserAccountSettings(
                                            description,
                                            name,
                                            "https://firebasestorage.googleapis.com/v0/b/kalaathon-parita.appspot.com/o/photos%2Fusers%2Fdefault%2Fwarning.png?alt=media&token=e2eec1cb-0974-4973-9440-b1f6b597a823",
                                            username,
                                            userID,
                                            email
                                    );

                                    myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                                            .child(userID)
                                            .setValue(settings);

                                    User user = new User(userID, phone_number, username);

                                    myRef.child(mContext.getString(R.string.dbname_users))
                                            .child(userID)
                                            .setValue(user);

                                    FirebaseMessaging.getInstance().subscribeToTopic(userID);
                                    check=true;
                                    firebaseMethods.uploadNewPhoto(mContext.getString(R.string.profile_photo), null
                                            , 0, path, null, mProgressBar, "register", null);

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(mContext, "Invalid request!", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                                btnRegister.setVisibility(View.VISIBLE);

                            }
                        });
                    }
                    else {
                        Query query = myRef.child("users").orderByChild("username").equalTo(code);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String ruid = "";
                                if (dataSnapshot.getChildrenCount() > 0) {
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        ruid = dataSnapshot1.child("user_id").getValue(String.class);
                                    }

                                    if (ruid != null) {
                                        myRef.child("refer").child(ruid).child(userID).setValue(0);
                                    }

                                    Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("username").equalTo(username);
                                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() > 0) {
                                                Toast.makeText(mContext, "Username already exists.", Toast.LENGTH_SHORT).show();
                                                mUsername.setError("Enter a different Username.");
                                                mProgressBar.setVisibility(View.GONE);
                                                btnRegister.setVisibility(View.VISIBLE);
                                            } else {
                                                UserAccountSettings settings = new UserAccountSettings(
                                                        description,
                                                        name,
                                                        "https://firebasestorage.googleapis.com/v0/b/kalaathon-parita.appspot.com/o/photos%2Fusers%2Fdefault%2Fwarning.png?alt=media&token=e2eec1cb-0974-4973-9440-b1f6b597a823",
                                                        username,
                                                        userID,
                                                        email
                                                );

                                                myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                                                        .child(userID)
                                                        .setValue(settings);

                                                User user = new User(userID, phone_number, username);

                                                myRef.child(mContext.getString(R.string.dbname_users))
                                                        .child(userID)
                                                        .setValue(user);

                                                FirebaseMessaging.getInstance().subscribeToTopic(userID);
                                                check=true;

                                                firebaseMethods.uploadNewPhoto(mContext.getString(R.string.profile_photo), null
                                                        , 0, path, null, mProgressBar, "register", null);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(mContext, "Invalid request!", Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                            btnRegister.setVisibility(View.VISIBLE);

                                        }
                                    });

                                }
                                else {
                                    Toast.makeText(mContext, "Invalid refer code!", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    btnRegister.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                mProgressBar.setVisibility(View.GONE);
                                btnRegister.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == imggallery_code) {
                path = getPath(data.getData());
                mImageView.setVisibility(View.GONE);
                mCircleImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(data.getData()).into(mCircleImageView);
                /*mCircleImageView.setImageURI(data.getData());*/
            }
            if(requestCode==imgcamera_code)
            {
                path=getPathcamera(imguri);
                mImageView.setVisibility(View.GONE);
                mCircleImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(imguri).into(mCircleImageView);
                /*mCircleImageView.setImageURI(imguri);*/
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

    private void pickcamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Kalaathon");
        imguri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imguri);
        startActivityForResult(cameraIntent, imgcamera_code);
    }

    private void pickgallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent,imggallery_code);
    }

    private boolean checkInputs(String username, String name, String description, String email) {
        return username.trim().length() > 1 && name.trim().length() > 1 && description.trim().length() > 1 && email.trim().length() > 1;
    }

    private void initWidgets() {
        mUsername = (EditText) findViewById(R.id.username_reg);
        mName = (EditText) findViewById(R.id.name_reg);
        mDescription = (EditText) findViewById(R.id.description_reg);
        mEmail = (EditText) findViewById(R.id.email_reg);
        btnRegister = (Button) findViewById(R.id.regbtn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.register_progress);
        mContext = register.this;
        refer=findViewById(R.id.refer);
        mImageView=findViewById(R.id.regprofilepic);
        mCircleImageView=findViewById(R.id.regprofilepic_circular);
        mTextView=findViewById(R.id.txtuploadphoto);
        firebaseMethods=new FirebaseMethods(mContext);
    }

    private static boolean isValidEmail(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
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

        int permissionRequest = ActivityCompat.checkSelfPermission(register.this, permission);

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
            // this is just an example
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
                register.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }
    public void complete(Context context)
    {
        Intent i=new Intent(context,homeactivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        register.this.finish();
    }

    @Override
    public void onBackPressed() {
        mAuth.signOut();
        super.onBackPressed();
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