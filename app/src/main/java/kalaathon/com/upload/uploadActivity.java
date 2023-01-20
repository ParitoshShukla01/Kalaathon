package kalaathon.com.upload;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import kalaathon.com.R;
import kalaathon.com.utils.Permissions;
import kalaathon.com.utils.SectionsPagerAdapter;

public class uploadActivity extends AppCompatActivity {
    private Context mContext=uploadActivity.this;
    private static final int ACTIVITY_NUM=2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contest);

        if(!checkPermissionsArray(Permissions.PERMISSIONS)){
            verifyPermissions(Permissions.PERMISSIONS);
            Toast.makeText(mContext, "Allow Permissions", Toast.LENGTH_LONG).show();
        }
        setupViewPager();
    }

    public void verifyPermissions(String[] permissions){
        ActivityCompat.requestPermissions(
                uploadActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
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

        int permissionRequest = ActivityCompat.checkSelfPermission(uploadActivity.this, permission);

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

    private void setupViewPager(){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new freeFragment());
        adapter.addFragment(new paidFragment());

        mViewPager = (ViewPager) findViewById(R.id.view_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("Share Your Talent");
        tabLayout.getTabAt(1).setText("Participate in Contest");
    }

    protected void onResume()
    {
        super.onResume();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onPause();
    }

}
