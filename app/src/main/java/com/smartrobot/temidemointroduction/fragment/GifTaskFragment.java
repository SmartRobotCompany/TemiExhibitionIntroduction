package com.smartrobot.temidemointroduction.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartrobot.temidemointroduction.R;
import com.smartrobot.temidemointroduction.listener.OnGifTaskFragmentActionListener;

//import org.jetbrains.annotations.NotNull;

import pl.droidsonroids.gif.GifImageView;

public class GifTaskFragment extends Fragment {
    private static final String TAG = "Debug_" + GifTaskFragment.class.getSimpleName();
    private Context context;
    private OnGifTaskFragmentActionListener onGifTaskFragmentActionListener;
    private GifImageView temi_face_gif;

    public GifTaskFragment(Context context){
        this.context = context;
        this.onGifTaskFragmentActionListener = (OnGifTaskFragmentActionListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.gif_task_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        temi_face_gif = view.findViewById(R.id.temi_face_gif);
    }

    @Override
    public void onResume() {
        super.onResume();
        onGifTaskFragmentActionListener.gifTaskFragmentLoadFinish();
    }
}
