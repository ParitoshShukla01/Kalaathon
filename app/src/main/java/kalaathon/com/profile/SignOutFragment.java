package kalaathon.com.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kalaathon.com.R;
import kalaathon.com.login.phone;

public class SignOutFragment extends Fragment {


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signout, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBarsignout);
        final Button btnConfirmSignout = (Button) view.findViewById(R.id.btnConfirmSignout);

        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();

        btnConfirmSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConfirmSignout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                mAuth.signOut();
                getActivity().finishAffinity();
            }
        });

        return view;
    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    private void setupFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                    Intent intent = new Intent(getActivity(), phone.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onAttach(context);
    }
}
