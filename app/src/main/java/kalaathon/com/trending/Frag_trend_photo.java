package kalaathon.com.trending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kalaathon.com.R;
import kalaathon.com.models.Photo;
import kalaathon.com.utils.CustomTimeStamp;
import kalaathon.com.utils.MainFeedListAdapter;

public class Frag_trend_photo extends Fragment {

    String TAG="Frag trend photo";


    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mtrending;
    private ListView mListView;
    private MainFeedListAdapter mAdapter;
    private int mResults;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayout mLayout;
    private TextView head,text;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_feed_photo,container,false);

        mListView = (ListView) view.findViewById(R.id.listPhoto);
        mtrending = new ArrayList<>();
        mLayout=view.findViewById(R.id.empty_pfeed);
        head=view.findViewById(R.id.empty_pfeed_head);
        text=view.findViewById(R.id.empty_pfeed_text);
        mLayout.setVisibility(View.GONE);

        mPhotos = new ArrayList<>();

        mRefreshLayout=view.findViewById(R.id.scroll_photo_feed);
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListView.setAdapter(null);
                mPhotos.clear();
                mtrending.clear();
                gettrending();
            }
        });
        gettrending();
        return view;
    }

    private  void gettrending()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query=reference.child("trend")
                .child("photo")
                .orderByChild("like");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                {
                    mtrending.add(dataSnapshot1.getKey());
                }
                if (isAdded())
                    getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getPhotos(){
        if(mtrending.size()==0) {
            try {
                head.setText(Objects.requireNonNull(getContext()).getString(R.string.trend_head));
                text.setText(Objects.requireNonNull(getContext()).getString(R.string.trend_text));
                mLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mtrending.size(); i++){
            final int count = i;
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mtrending.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setCategory(objectMap.get(getString(R.string.field_category)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                        mPhotos.add(photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(count >= mtrending.size() -1){
                        displayPhotos();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if(mPhotos.size()==0)
            mRefreshLayout.setRefreshing(false);
    }

    private void displayPhotos(){
        mRefreshLayout.setRefreshing(false);
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null){
            try{
                int iterations = mPhotos.size();

                if(iterations > 3){
                    iterations = 3;
                }

                mResults = 3;
                for(int i = 0; i < iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mAdapter = new MainFeedListAdapter(getActivity(), R.layout.fragment_listfeedphoto, mPaginatedPhotos,1);
                mListView.setAdapter(mAdapter);

            }catch (NullPointerException e){

            }catch (IndexOutOfBoundsException e){

            }
        }
    }

    public void displayMorePhotos() {


        try {
            if (mPhotos.size() > mResults && mPhotos.size() > 0) {

                int iterations;
                if (mPhotos.size() > (mResults + 3)) {

                    iterations = 3;
                } else {

                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated results
                for (int i = mResults; i < mResults + iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {

        } catch (IndexOutOfBoundsException e) {
        }
    }



}
