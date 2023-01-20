package kalaathon.com.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kalaathon.com.R;
import kalaathon.com.models.Video;

public class ViewNextFragVideo extends Fragment {

    private PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;
    ProgressBar mVideoprogress;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //vars
    private Video mVideo;
    private Context mContext;
    ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.frag_feed_view_video, container, false);
        mPlayerView=view.findViewById(R.id.feed_videoexo);
        mVideoprogress=view.findViewById(R.id.vdoprogress);
        mImageView=view.findViewById(R.id.backArrow);
        mContext=getContext();
        setupFirebaseAuth();

        try{
            mVideo = getVideoFromBundle();
            setupFirebaseAuth();

        }catch (NullPointerException e){
            Toast.makeText(mContext, "Something went wrong !", Toast.LENGTH_SHORT).show();
        }
        String url=mVideo.getVideo_path();
        getPlayer(url);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                ((homeactivity)getActivity()).showLayout();
            }
        });

        return view;
    }

    private void getPlayer(String url) {

        // URL of the video to stream

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // Create the player with previously created TrackSelector
        mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        // Load the default controller
        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();

        // Load the SimpleExoPlayerView with the created player
        mPlayerView.setPlayer(mPlayer);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(),
                Util.getUserAgent(getContext(), "Kalaathon"),
                defaultBandwidthMeter);
        DashMediaSource.Factory mediasourcefactory = new DashMediaSource.Factory(dataSourceFactory);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(
                Uri.parse(url),
                dataSourceFactory,
                extractorsFactory,
                null,
                null);

        // Prepare the player with the source.
        mPlayer.prepare(videoSource);

        // Autoplay the video when the player is ready
        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                if(isLoading) {
                    mVideoprogress.setVisibility(View.VISIBLE);
                }
                else
                    mVideoprogress.setVisibility(View.GONE);

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(playbackState==Player.STATE_BUFFERING)
                {
                    mVideoprogress.setVisibility(View.VISIBLE);
                }
                else
                    mVideoprogress.setVisibility(View.GONE);
            }
        });
    }

    private Video getVideoFromBundle(){

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getParcelable(getString(R.string.dbname_video));
        }else{
            return null;
        }
    }

    private void setupFirebaseAuth() {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }
                // ...
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
        releasePlayer();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }


    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPause() {
        releasePlayer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}