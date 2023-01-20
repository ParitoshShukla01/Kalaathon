package kalaathon.com.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import kalaathon.com.models.Photo;
import kalaathon.com.models.User;
import kalaathon.com.utils.gridImageAdapter;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Viewphoto extends Fragment {

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int NUM_GRID_COLUMNS = 3;
    private static final int ACTIVITY_NUM = 4;
    private GridView gridView;
    private User mUser;
    private LinearLayout mLayout;
    private TextView head,text;

    public Viewphoto(User user) {
        mUser = user;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_layout_photo, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        mLayout=(LinearLayout)view.findViewById(R.id.empty_pgrid);
        head=view.findViewById(R.id.empty_pgrid_head);
        text=view.findViewById(R.id.empty_pgrid_text);
        mLayout.setVisibility(View.GONE);
        setupGridView();
        return view;
    }

    private void setupGridView(){
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    try {
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setCategory(objectMap.get("category").toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        photo.setLikes(likesList);
                        photos.add(photo);
                    } catch (Exception e) {

                    }
                }
                Collections.reverse(photos);
                try {
                    //setup our image grid
                    int gridWidth = getResources().getDisplayMetrics().widthPixels;
                    int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                    gridView.setColumnWidth(imageWidth);
                    ArrayList<String> imgUrls = new ArrayList<String>();
                    for (int i = 0; i < photos.size(); i++) {
                        imgUrls.add(photos.get(i).getImage_path());
                    }
                    if(imgUrls.size()==0)
                    {
                        String s=mUser.getUsername()+" "+getContext().getString(R.string.view_profile_text_addtop);
                        head.setText(getContext().getString(R.string.view_profile_head));
                        text.setText(s);
                        mLayout.setVisibility(View.VISIBLE);
                    }
                    gridImageAdapter adapter = new gridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                            "", imgUrls);
                    gridView.setAdapter(adapter);

                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mOnGridImageSelectedListener.onGridImageSelected(photos.get(i), ACTIVITY_NUM);
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
            mOnGridImageSelectedListener = (Viewphoto.OnGridImageSelectedListener)getActivity();
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