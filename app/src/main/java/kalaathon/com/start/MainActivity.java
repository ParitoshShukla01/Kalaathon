package kalaathon.com.start;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.nostra13.universalimageloader.core.ImageLoader;

import kalaathon.com.R;
import kalaathon.com.home.homeactivity;
import kalaathon.com.login.phone;
import kalaathon.com.utils.UniversalImageLoader;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    boolean link;
    int delay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()==null)
            delay=2000;
        else
            delay=0;

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getApplicationContext());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mAuth.getCurrentUser() != null) {
                    String check=mAuth.getCurrentUser().getUid();
                    Query query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("user_id").equalTo(check);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists())
                            {
                                Intent i = new Intent(MainActivity.this,
                                        phone.class);
                                startActivity(i);
                                finish();
                            }
                            else
                            {
                                if (!dynamic()) {

                                    if (getIntent().hasExtra("type")){
                                        Intent intent = new Intent(MainActivity.this,OpenLink.class);
                                        intent.putExtra("user_id",getIntent().getStringExtra("user_id"));
                                        intent.putExtra("id",getIntent().getStringExtra("media_id"));
                                        intent.putExtra("type",getIntent().getStringExtra("type"));
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {

                                        Intent i = new Intent(MainActivity.this,
                                                homeactivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {

                    Intent i = new Intent(MainActivity.this,
                            phone.class);
                    startActivity(i);
                    finish();
                }
            }

        }, delay);
    }

    public boolean dynamic()
    {
        link=false;
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                            @Override
                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                // Get deep link from result (may be null if no link is found)
                                Uri deepLink = null;
                                if (pendingDynamicLinkData != null) {
                                    link=true;
                                    deepLink = pendingDynamicLinkData.getLink();
                                    String str=deepLink.toString();
                                    String id=str.substring(26,str.lastIndexOf("/"));
                                    String user_id=str.substring(str.lastIndexOf("/")+1,str.length()-1);
                                    String type=""+str.charAt(str.length()-1);

                                    Intent i=new Intent(MainActivity.this,OpenLink.class);
                                    i.putExtra("id",id);
                                    i.putExtra("type",type);
                                    i.putExtra("user_id",user_id);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        }
                )
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        link=false;
                    }
                });
        return link;
    }
}
