package kalaathon.com.list;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kalaathon.com.R;
import kalaathon.com.models.User;
import kalaathon.com.profile.profileActivity;
import kalaathon.com.utils.UserListAdapter;

public class FollowersList extends Fragment {

    private List<String> mUser;
    private UserListAdapter mAdapter;
    private ListView mListView;
    private TextView mTextView;
    private ImageView mImageView;
    private ProgressBar listprogress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.frag_list,container,false);
        listprogress=view.findViewById(R.id.listprogress);
        listprogress.setVisibility(View.VISIBLE);
        this.mUser=getUserFromBundle();
        mListView=view.findViewById(R.id.list_list);
        mTextView=view.findViewById(R.id.custom_top_text);
        mImageView=view.findViewById(R.id.backArrow);
        mTextView.setText(getString(R.string.Followers));
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getCallingActivity().equals(getString(R.string.profile_activity)))
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                    ((profileActivity) getActivity()).showLayout();
                }
            }
        });
        if(isAdded() && getContext()!=null)
            getuserlist();
        return view;
    }

    private List<String> getUserFromBundle(){
        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getStringArrayList(getString(R.string.dbname_photos));
        }else{
            return null;
        }
    }

    private String getCallingActivity(){
        Bundle bundle=this.getArguments();
        if(bundle != null) {
            return bundle.getString("Activity");
        }else{
            return null;
        }
    }

    private void updateUsersList(List<User> senduser){

        try {
            mAdapter = new UserListAdapter(Objects.requireNonNull(getActivity()), R.layout.layout_user_listitem, senduser);
            mListView.setAdapter(mAdapter);
            listprogress.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageView imageView=view.findViewById(R.id.profile_image);
                Pair pairs=new Pair<View,String>(imageView,"profile_transition");
                ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(getActivity(),pairs);

                Intent intent =  new Intent(getContext(), profileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(getString(R.string.calling_activity), "Search Activity");
                intent.putExtra(getString(R.string.intent_user), senduser.get(i));
                startActivity(intent,activityOptions.toBundle());
            }
        });
    }

    private void getuserlist()
    {
        List<User> senduser=new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i=0;i<mUser.size();i++)
        {
            Query query = reference
                    .child(getContext().getString(R.string.dbname_users))
                    .orderByChild(getContext().getString(R.string.field_user_id))
                    .equalTo(mUser.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        senduser.add(dataSnapshot1.getValue(User.class));
                    }
                    updateUsersList(senduser);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
