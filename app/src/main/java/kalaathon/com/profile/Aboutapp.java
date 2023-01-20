package kalaathon.com.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import kalaathon.com.R;

public class Aboutapp extends Fragment {

    private RelativeLayout privacy,terms,community,open;
    private ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_aboutapp,container,false);
        mImageView=view.findViewById(R.id.back_arrow);
        privacy=view.findViewById(R.id.privacy);
        terms=view.findViewById(R.id.terms);
        community=view.findViewById(R.id.community);
        open=view.findViewById(R.id.open);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        setup();
        return view;
    }

    private void setup() {
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "privacy_policy.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "terms_and_condition.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "community_guidelines.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openlib frag=new openlib();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

    }
}
