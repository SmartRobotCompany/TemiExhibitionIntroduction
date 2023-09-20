package com.smartrobot.temidemointroduction.fragment;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.smartrobot.temidemointroduction.constant.IntroductionContent;
import com.smartrobot.temidemointroduction.R;
import com.smartrobot.temidemointroduction.constant.VideoConstant;
import com.smartrobot.temidemointroduction.listener.OnVideoPlayStatusListener;


import java.io.File;

public class VideoTaskFragment extends Fragment {
    private static final String TAG = "Debug_" + VideoTaskFragment.class.getSimpleName();
    private Context context;
    private String videoPath;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private OnVideoPlayStatusListener onVideoPlayStatusListener;

    public VideoTaskFragment(Context context,
                             String videoPath){
        this.context = context;
        this.videoPath = videoPath;
        this.onVideoPlayStatusListener = (OnVideoPlayStatusListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.video_task_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        playerView = view.findViewById(R.id.video_view);
        exoPlayer = new ExoPlayer.Builder(getActivity()).build();
        playerView.setPlayer(exoPlayer);
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                switch (playbackState){
                    case VideoConstant.PLAYER_STATE_IDLE:
                        Log.d(TAG, "onPlaybackStateChanged: player state is idle");
                        break;

                    case VideoConstant.PLAYER_STATE_BUFFERING:
                        Log.d(TAG, "onPlaybackStateChanged: player state is buffering");
                        playerView.hideController();
                        break;

                    case VideoConstant.PLAYER_STATE_READY:
                        Log.d(TAG, "onPlaybackStateChanged: player state is ready");
                        break;

                    case VideoConstant.PLAYER_STATE_ENDED:
                        Log.d(TAG, "onPlaybackStateChanged: player state is ended");
                        onVideoPlayStatusListener.onVideoPlayEnded(videoPath);
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        videoShow(videoPath);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void videoShow(String videoPath){
        Uri uri = Uri.parse(videoPath);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

    }
}
