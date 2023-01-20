package kalaathon.com.search;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kalaathon.com.R;
import kalaathon.com.models.User;
import kalaathon.com.profile.profileActivity;
import kalaathon.com.utils.BottomNavigationViewHelper;
import kalaathon.com.utils.UniversalImageLoader;
import kalaathon.com.utils.UserListAdapter;

public class searchActivity extends AppCompatActivity {
    private Context mContext;
    private static final int ACTIVITY_NUM=3;
    //widgets
    private EditText mSearchParam;
    private ListView mListView;
    private ImageView back;
    private RelativeLayout mRelativeLayout;
    private ProgressBar mBar;
    private TextView mTextView;

    //vars
    private List<User> mUserList;
    private UserListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListView = (ListView) findViewById(R.id.listView);
        back=(ImageView)findViewById(R.id.srchback);
        mRelativeLayout=(RelativeLayout)findViewById(R.id.layoutsrch);
        mBar=(ProgressBar)findViewById(R.id.srchprogress);
        mTextView=findViewById(R.id.search_empty);
        mTextView.setVisibility(View.GONE);
        mContext=getApplicationContext();
        if(!ImageLoader.getInstance().isInited())
            initImageLoader();

        hideSoftKeyboard();
        setupBottomNavigationView();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSearchParam.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    mBar.setVisibility(View.VISIBLE);
                    mUserList = new ArrayList<>();
                    mListView.setAdapter(null);
                    mUserList.clear();
                    String text = mSearchParam.getText().toString();
                    searchForMatch(text);
                    return true;
                }
                return false;
            }
        });

        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBar.setVisibility(View.VISIBLE);
                mUserList = new ArrayList<>();
                mListView.setAdapter(null);
                mUserList.clear();
                String text = mSearchParam.getText().toString().trim();
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword){
        //update the users list view
        if(keyword.length() ==0){
            mBar.setVisibility(View.GONE);
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username)).startAt(keyword).endAt(keyword+"\uf8ff").limitToFirst(30);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount()==0) {
                        mBar.setVisibility(View.GONE);
                        mTextView.setVisibility(View.VISIBLE);
                    }
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        mUserList.add(singleSnapshot.getValue(User.class));
                        //update the users list view
                        mTextView.setVisibility(View.GONE);
                        notifyAdapter();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void updateUsersList(){
        mAdapter = new UserListAdapter(searchActivity.this, R.layout.layout_user_listitem, mUserList);
        mListView.setAdapter(mAdapter);
        mBar.setVisibility(View.GONE);
        hideSoftKeyboard();
        mAdapter.notifyDataSetChanged();
        try {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    /*ImageView imageView=findViewById(R.id.profile_image);*/
                    ImageView imageView=view.findViewById(R.id.profile_image);
                    Pair pairs=new Pair<View,String>(imageView,"profile_transition");
                    ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(searchActivity.this,pairs);

                    Intent intent = new Intent(searchActivity.this, profileActivity.class);
                    intent.putExtra(getString(R.string.calling_activity), "Search Activity");
                    intent.putExtra(getString(R.string.intent_user), mUserList.get(i));
                    startActivity(intent,activityOptions.toBundle());
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "errror", Toast.LENGTH_SHORT).show();
            mBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void setupBottomNavigationView()
    {
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx)findViewById(R.id.bottomnavigationviewbar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx,4);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(R.drawable.ic_searchdark);
    }

    private void notifyAdapter()  {
        searchActivity.this.runOnUiThread(new Runnable()  {
            public void run() {
                updateUsersList();
            }
        });
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

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(searchActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
}
