package kalaathon.com.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kalaathon.com.R;
import kalaathon.com.models.Video;
import kalaathon.com.utils.MainFeedListVideoAdapter;

public class Fragfeed_video extends Fragment {

    String TAG="Fragfeed video";

    private ArrayList<Video> mVideos;
    private ArrayList<Video> mPaginatedVideos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainFeedListVideoAdapter mAdapter;
    private int mResults;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayout mLinearLayout;
    private TextView head,text;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_feed_video,container,false);

        mListView = (ListView) view.findViewById(R.id.listVideo);
        mFollowing = new ArrayList<>();
        mVideos = new ArrayList<>();
        mLinearLayout=view.findViewById(R.id.empty_vfeed);
        head=view.findViewById(R.id.empty_vfeed_head);
        text=view.findViewById(R.id.empty_vfeed_text);
        mLinearLayout.setVisibility(View.GONE);

        mRefreshLayout=view.findViewById(R.id.swipe_video_feed);
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListView.setAdapter(null);
                mVideos.clear();
                mFollowing.clear();
                getFollowing();
            }
        });

        getFollowing();

        return view;
    }


    private void getFollowing(){


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if(isAdded())
                        mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get the videos
                try {
                    getVideos();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getVideos(){
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            for (int i = 0; i < mFollowing.size(); i++) {
                final int count = i;
                Query query = reference
                        .child(getString(R.string.dbname_user_videos))
                        .child(mFollowing.get(i))
                        .orderByChild(getString(R.string.field_user_id))
                        .equalTo(mFollowing.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            if(isAdded()) {
                                Video video = new Video();
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                video.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                video.setCategory(objectMap.get(getString(R.string.field_category)).toString());
                                video.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                                video.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                video.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                try {
                                    video.setThumbnail(objectMap.get(getString(R.string.field_video_thumbnail)).toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                video.setVideo_path(objectMap.get(getString(R.string.field_video_path)).toString());
                                mVideos.add(video);
                            }
                        }
                        if (count >= mFollowing.size() - 1 && isAdded()) {
                            displayVideos();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
            if(mVideos.size()==0)
                mRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayVideos(){
        mRefreshLayout.setRefreshing(false);
        mPaginatedVideos = new ArrayList<>();
        if(mVideos.size()==0) {
            try {
                head.setText(Objects.requireNonNull(getContext()).getString(R.string.main_feed_head));
                text.setText(Objects.requireNonNull(getContext()).getString(R.string.main_feed_text));
                mLinearLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(mVideos != null){
            try{
                Collections.sort(mVideos, new Comparator<Video>() {
                    @Override
                    public int compare(Video o1, Video o2) {

                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mVideos.size();

                if(iterations > 3){
                    iterations = 3;
                }

                mResults = 3;
                for(int i = 0; i < iterations; i++){
                    mPaginatedVideos.add(mVideos.get(i));
                }

                mAdapter = new MainFeedListVideoAdapter(getActivity(), R.layout.fragment_listfeedvideo, mPaginatedVideos,0);
                mListView.setAdapter(mAdapter);

            }catch (NullPointerException e){

            }catch (IndexOutOfBoundsException e){

            }
        }
    }

    public void displayMoreVideos() {
        try {

            if (mVideos.size() > mResults && mVideos.size() > 0) {

                int iterations;
                if (mVideos.size() > (mResults + 3)) {

                    iterations = 3;
                } else {

                    iterations = mVideos.size() - mResults;
                }

                //add the new photos to the paginated results
                for (int i = mResults; i < mResults + iterations; i++) {
                    mPaginatedVideos.add(mVideos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {

        } catch (IndexOutOfBoundsException e) {
        }
    }
}
