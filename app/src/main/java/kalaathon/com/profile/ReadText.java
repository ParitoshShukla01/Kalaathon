package kalaathon.com.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import kalaathon.com.R;

public class ReadText extends Fragment {

    TextView mTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.layout_read,container,false);
        mTextView=view.findViewById(R.id.txt);
        String str=getArguments().getString("file");
        read(str);
        return view;
    }

    private void read(String file) {
        String text = "";
        try{
            InputStream inputStream = Objects.requireNonNull(getContext()).getAssets().open(file);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTextView.setText(text);
    }

}
