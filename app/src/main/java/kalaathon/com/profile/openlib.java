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

public class openlib extends Fragment {

    private RelativeLayout circle,commons,exoplayer,ffmpeg,ittianyu,universal,volley,zomato, glide;
    private ImageView mImageView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_open,container,false);
        mImageView=view.findViewById(R.id.back_arrow);
        circle=view.findViewById(R.id.circleimageview);
        commons=view.findViewById(R.id.commons);
        exoplayer=view.findViewById(R.id.exoplayer);
        ffmpeg=view.findViewById(R.id.ffmpeg);
        ittianyu=view.findViewById(R.id.ittiyau);
        universal=view.findViewById(R.id.universal);
        volley=view.findViewById(R.id.volley);
        zomato=view.findViewById(R.id.zomato);
        glide =view.findViewById(R.id.glide);

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
        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "circle_image_view.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        commons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "commons net.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        exoplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "exoplayer.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        ffmpeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "ffmpeg.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        ittianyu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "ittianyu.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        universal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "universal_image_loader.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        volley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "volley.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        glide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "glide.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        zomato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle=new Bundle();
                bundle.putString("file", "zomato.txt");
                ReadText frag=new ReadText();
                frag.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.relLayout1, frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}
