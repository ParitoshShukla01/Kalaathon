package kalaathon.com.trending;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kalaathon.com.R;
import kalaathon.com.models.Video;
import kalaathon.com.utils.MainFeedListVideoAdapter;

public class Frag_trend_video extends Fragment {

    String TAG="Frag trend video";

    private ArrayList<Video> mVideos;
    private ArrayList<Video> mPaginatedVideos;
    private ArrayList<String> mtrending;
    private ListView mListView;
    private MainFeedListVideoAdapter mAdapter;
    private int mResults;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayout mLayout;
    private TextView text,head;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_feed_video,container,false);

        mListView = (ListView) view.findViewById(R.id.listVideo);
        mtrending = new ArrayList<>();
        mVideos = new ArrayList<>();
        mLayout=view.findViewById(R.id.empty_vfeed);
        head=view.findViewById(R.id.empty_vfeed_head);
        text=view.findViewById(R.id.empty_vfeed_text);
        mLayout.setVisibility(View.GONE);

        mRefreshLayout=view.findViewById(R.id.swipe_video_feed);
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListView.setAdapter(null);
                mVideos.clear();
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
                .child("video")
                .orderByChild("like");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                {
                    mtrending.add(dataSnapshot1.getKey());
                }
                if (isAdded())
                    getVideos();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getVideos(){
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
                    .child(getString(R.string.dbname_video))
                    .child(mtrending.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try {
                        Video video = new Video();
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();

                        video.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        video.setCategory(objectMap.get(getString(R.string.field_category)).toString());
                        video.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                        video.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        video.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        video.setThumbnail(objectMap.get(getString(R.string.field_video_thumbnail)).toString());
                        video.setVideo_path(objectMap.get(getString(R.string.field_video_path)).toString());
                        mVideos.add(video);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(count >= mtrending.size() -1){
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
    }

    private void displayVideos(){
        mRefreshLayout.setRefreshing(false);
        mPaginatedVideos = new ArrayList<>();
        if(mVideos != null){
            try{
                int iterations = mVideos.size();

                if(iterations > 3){
                    iterations = 3;
                }

                mResults = 3;
                for(int i = 0; i < iterations; i++){
                    mPaginatedVideos.add(mVideos.get(i));
                }

                mAdapter = new MainFeedListVideoAdapter(getActivity(), R.layout.fragment_listfeedvideo, mPaginatedVideos,1);
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
