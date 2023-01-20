package kalaathon.com.upload;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import kalaathon.com.R;

public class VideoCapture extends Activity {

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";
    private Uri fileUri; // file url to store image/video

    private VideoView videoPreview;
    private Button  btnRecordVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);

        videoPreview = (VideoView) findViewById(R.id.videoPreview);
        btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);


        btnRecordVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // record video
             /*   */
                recordVideo();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /*
     * Recording video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = Uri.fromFile(new File(getsdcardinfo()));
        intent.putExtra("android.intent.extra.durationLimit", 5);
        intent.putExtra("EXTRA_VIDEO_QUALITY", 0);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Receiving activity result method
     * will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // video successfully recorded
                // preview the recorded video
                previewVideo(data);
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    /*
     * Previewing recorded video
     */
    private void previewVideo(Intent data) {
        try {
            // hide image preview

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setZOrderOnTop(true);
            videoPreview.setBackgroundColor(Color.TRANSPARENT);
            videoPreview.setVideoURI(data.getData());
            videoPreview.requestFocus();
            videoPreview.start();
            videoPreview.setMediaController(new MediaController(VideoCapture.this));
            // start playing
            videoPreview.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ------------ Helper Methods ----------------------
     * */

    /*
     * returning image / video
     */
    public String getsdcardinfo()
    {
        File downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if(downloadFolder==null)
            downloadFolder.mkdir();
        return downloadFolder+"/output.mp4";
    }
}