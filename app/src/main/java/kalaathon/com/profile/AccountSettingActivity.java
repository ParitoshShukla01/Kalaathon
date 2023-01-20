package kalaathon.com.profile;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import kalaathon.com.BuildConfig;
import kalaathon.com.R;
import kalaathon.com.upload.VideoCapture;
import kalaathon.com.utils.BottomNavigationViewHelper;

public class AccountSettingActivity extends AppCompatActivity {
    private Context mContext;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;
    private static final int ACTIVITY_NUM=4;
    private LinearLayout edit,invite,notification,help,about,rate;
    private Button signout;
    private String username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        mContext = AccountSettingActivity.this;
        mViewPager = (ViewPager) findViewById(R.id.view_container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayout1);
        edit=(LinearLayout) findViewById(R.id.editprofile);
        invite=(LinearLayout) findViewById(R.id.invite);
        notification=(LinearLayout) findViewById(R.id.notification);
        help=(LinearLayout) findViewById(R.id.help);
        about=(LinearLayout) findViewById(R.id.about);
        rate=(LinearLayout) findViewById(R.id.rateus);
        signout=(Button)findViewById(R.id.signout);
        setupBottomNavigationView();
        ImageView backArrow = (ImageView) findViewById(R.id.back_arrow);
        username=getIntent().getStringExtra("username");

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SignOutFragment frag=new SignOutFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack("Sign Out");
                transaction.commit();
            }
        });

        otherOptions();
    }

    private void otherOptions() {

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(AccountSettingActivity.this,Edit_Profile_Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kalaathon");
                    String shareMessage= "\nHello! Join me on Kalaathon!!! \uD83D\uDC47\n" +
                            "\n" +
                            "Download from the link below to share your talent, participate in contests and win exciting prizes.Use Referral Code:"+username+" ⬇️ \n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share from :"));
                } catch(Exception e) {
                    Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName());
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", mContext.getPackageName());
                    intent.putExtra("app_uid", mContext.getApplicationInfo().uid);
                } else {
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                }
                mContext.startActivity(intent);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@kalaathon.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT,"@"+username );
                intent.putExtra(Intent.EXTRA_TEXT,"This is to report an issue regarding ");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }*/
                Toast.makeText(mContext, "Mail us at: support@kalaathon.com", Toast.LENGTH_LONG).show();
                Intent sendMail = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("support@kalaathon.com") +
                        "?subject=" + Uri.encode("@"+username) +
                        "&body=" + Uri.encode("This is to report an issue regarding ");
                Uri uri = Uri.parse(uriText);

                sendMail.setData(uri);
                startActivity(Intent.createChooser(sendMail, "Send from..."));
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // to taken back to our application
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
                }
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aboutapp frag=new Aboutapp();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack("Aboutapp");
                transaction.commit();
            }
        });
    }

    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx)findViewById(R.id.bottomnavigationviewbar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx,0);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(R.drawable.ic_profiledark);
    }

}