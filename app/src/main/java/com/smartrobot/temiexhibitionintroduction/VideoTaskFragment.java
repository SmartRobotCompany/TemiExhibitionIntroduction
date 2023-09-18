package com.smartrobot.temiexhibitionintroduction;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;

public class VideoTaskFragment extends Fragment {
    private static final String TAG = "Debug_" + VideoTaskFragment.class.getSimpleName();
    private static VideoTaskFragment instance;

    private VideoView videoView;
    private String mlocation;

    public VideoTaskFragment(String location){
        mlocation = location;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.video_task_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        videoView = view.findViewById(R.id.video_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        switch (mlocation){
            case IntroductionContent.mg400_router_location:
                videoShow(IntroductionContent.mg400Mp4Path);
                break;

            case IntroductionContent.mg400_scale_location:
                videoShow(IntroductionContent.mg400Mp4Path);
                break;

            case IntroductionContent.mg400_tablet_location:
                videoShow(IntroductionContent.mg400Mp4Path);
                break;

            case IntroductionContent.cr5_location:
                videoShow(IntroductionContent.cr5Mp4Path);
                break;

            case IntroductionContent.thouzer_location:
                videoShow(IntroductionContent.thouzerMp4Path);
                break;

            case IntroductionContent.temi_location:
                videoShow(IntroductionContent.temiMp4Path);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        videoView.stopPlayback();
    }

    private void videoShow(String videoPath){
        Uri uri = Uri.fromFile(new File(videoPath));
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController(getActivity());

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaController.hide();
                mp.setVolume(0f,0f);
                mp.setLooping(true);

            }
        });
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });
    }
}
