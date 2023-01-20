package kalaathon.com.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import kalaathon.com.R;
import kalaathon.com.trending.TrendingActivity;
import kalaathon.com.home.homeactivity;
import kalaathon.com.profile.profileActivity;
import kalaathon.com.search.searchActivity;
import kalaathon.com.upload.uploadActivity;

public class BottomNavigationViewHelper {
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx)
    {
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context,BottomNavigationViewEx viewEx,int i)
    {
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.house:
                        if(i!=1) {
                            Intent intent1 = new Intent(context, homeactivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent1);
                        }
                        break;
                    case R.id.category:
                        if(i!=2) {
                            Intent intent2 = new Intent(context, TrendingActivity.class);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent2);
                        }
                        break;
                    case R.id.upload:
                        if(i!=3) {
                            Intent intent3 = new Intent(context, uploadActivity.class);
                            intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent3);
                        }
                        break;
                    case R.id.search:
                        if(i!=4) {
                            Intent intent4 = new Intent(context, searchActivity.class);
                            intent4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent4);
                        }
                        break;
                    case R.id.profile:
                        if(i!=5) {
                            Intent intent5 = new Intent(context, profileActivity.class);
                            intent5.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent5);
                        }
                        break;
                }
                return false;
            }
        });
    }
}
