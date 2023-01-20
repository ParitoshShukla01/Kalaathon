package kalaathon.com.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kalaathon.com.R;
import kalaathon.com.models.Like;
import kalaathon.com.models.Video;
import kalaathon.com.utils.gridImageAdapter;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class videofrag extends Fragment {

    public interface OnGridItemSelectedListener{
        void onGridItemSelected(Video video, int activityNumber);
    }
    OnGridItemSelectedListener mOnGridItemSelectedListener;

    private static final int NUM_GRID_COLUMNS = 3;
    private static final int ACTIVITY_NUM = 4;
    private GridView gridView;
    private LinearLayout mLayout;
    private TextView head,text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_layout_video_grid, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        mLayout=(LinearLayout)view.findViewById(R.id.empty_vgrid);
        head=view.findViewById(R.id.empty_vgrid_head);
        text=view.findViewById(R.id.empty_vgrid_text);
        mLayout.setVisibility(View.GONE);
        setupGridView();
        return view;
    }

    private void setupGridView(){
        final ArrayList<Video> videos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_videos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    //videos.add(singleSnapshot.getValue(Video.class));
                    Video video=new Video();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    try {
                        video.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        video.setCategory(objectMap.get("category").toString());
                        video.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                        video.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        video.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        video.setVideo_path(objectMap.get(getString(R.string.field_video_path)).toString());
                        video.setThumbnail(objectMap.get(getString(R.string.field_video_thumbnail)).toString());

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        video.setLikes(likesList);
                        videos.add(video);
                    } catch (Exception e) {

                    }
                }
                Collections.reverse(videos);
                try {
                    //setup our image grid
                    int gridWidth = getResources().getDisplayMetrics().widthPixels;
                    int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                    gridView.setColumnWidth(imageWidth);

                    final ArrayList<String> imgUrls = new ArrayList<String>();
                    for (int i = 0; i < videos.size(); i++) {
                        imgUrls.add(videos.get(i).getThumbnail());
                    }
                    if(imgUrls.size()==0)
                    {
                        head.setText(getContext().getString(R.string.own_profile_head));
                        text.setText(getContext().getString(R.string.own_profile_text));
                        mLayout.setVisibility(View.VISIBLE);
                    }

                    gridImageAdapter adapter = new gridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                            "", imgUrls);
                    gridView.setAdapter(adapter);

                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            try {
                                mOnGridItemSelectedListener.onGridItemSelected(videos.get(i), ACTIVITY_NUM);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnGridItemSelectedListener = (videofrag.OnGridItemSelectedListener) getActivity();
        }catch (ClassCastException e){
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        if(gridView!=null)
        gridView.removeAllViewsInLayout();
        super.onDetach();
    }

}





