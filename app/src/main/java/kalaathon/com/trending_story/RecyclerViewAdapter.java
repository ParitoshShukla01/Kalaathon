package kalaathon.com.trending_story;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kalaathon.com.R;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;
import kalaathon.com.profile.profileActivity;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    //vars
    private ArrayList<UserAccountSettings> mUsers=new ArrayList<>();
    private Context mContext;
    private Pair pair;
    public RecyclerViewAdapter(Context context, ArrayList<UserAccountSettings> user) {
        mUsers = user;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_story_element, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mUsers.get(position).getProfile_photo(),holder.image);

        holder.name.setText(mUsers.get(position).getUsername());

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pair = new Pair<View, String>(holder.image, "profile_transition");
                getUser(mUsers.get(position).getUser_id());
            }
        });
    }
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
        }
    }

    public void getUser(String user_id)
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getResources().getString(R.string.dbname_users))
                .orderByChild(mContext.getResources().getString(R.string.field_user_id))
                .equalTo(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Intent intent = new Intent(mContext, profileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(mContext.getString(R.string.calling_activity),
                            mContext.getString(R.string.home_activity));
                    intent.putExtra(mContext.getString(R.string.intent_user), snapshot.getValue(User.class));
                    mContext.startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
