package kalaathon.com.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import kalaathon.com.R;
import kalaathon.com.models.User;
import kalaathon.com.models.UserAccountSettings;

public class UserListAdapter extends ArrayAdapter<User>  {
    private static final String TAG = "UserListAdapter";

    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutResource;
    private Context mContext;
    private String mString;

    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<User> objects)throws NullPointerException {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUsers = objects;
    }

    private static class ViewHolder{
        TextView username, name;
        CircleImageView profileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        final ViewHolder holder;

        //if(convertView == null){
        convertView = mInflater.inflate(layoutResource, parent, false);
        holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        holder.name = (TextView) convertView.findViewById(R.id.name);
        holder.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);
        convertView.setTag(holder);
    /*    }else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        holder.username.setText(getItem(position).getUsername());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    holder.name.setText(singleSnapshot.getValue(UserAccountSettings.class).getDisplay_name());
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}
