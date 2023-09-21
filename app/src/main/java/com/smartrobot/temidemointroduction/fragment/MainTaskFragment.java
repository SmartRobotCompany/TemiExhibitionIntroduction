package com.smartrobot.temidemointroduction.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.smartrobot.temidemointroduction.R;
import com.smartrobot.temidemointroduction.constant.VideoConstant;
import com.smartrobot.temidemointroduction.listener.OnGoHomeBaseButtonClickListener;
import com.smartrobot.temidemointroduction.listener.OnIntroductionButtonClickListener;
import com.smartrobot.temidemointroduction.utilities.SpeechRecognizeUtilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainTaskFragment extends Fragment {
    private static final String TAG = "Debug_" + MainTaskFragment.class.getSimpleName();
    private Button go_homebase_button;
    private Button temi_introduction_button;
    private Button thouzer_introduction_button;
    private Button nova5_introduction_button;
    private Button Mg400_introduction_button;
    private Button voice_command_button;
    private ImageButton uri_permission_setting_imagebutton;
    private TextView current_speech_recognize_textview;
    private Context context;
    private OnIntroductionButtonClickListener onIntroductionButtonClickListener;
    private OnGoHomeBaseButtonClickListener onGoHomeBaseButtonClickListener;
    private SpeechRecognizeUtilities speechRecognizeUtilities;


    public MainTaskFragment(Context context){
        this.context = context;
        this.onIntroductionButtonClickListener = (OnIntroductionButtonClickListener) context;
        this.onGoHomeBaseButtonClickListener = (OnGoHomeBaseButtonClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.main_task_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        go_homebase_button = view.findViewById(R.id.go_homebase_button);
        temi_introduction_button = view.findViewById(R.id.temi_introduction_button);
        thouzer_introduction_button = view.findViewById(R.id.thouzer_introduction_button);
        nova5_introduction_button = view.findViewById(R.id.nova5_introduction_button);
        Mg400_introduction_button = view.findViewById(R.id.Mg400_introduction_button);
        voice_command_button = view.findViewById(R.id.voice_command_button);
        uri_permission_setting_imagebutton = view.findViewById(R.id.uri_permission_setting_imagebutton);
        current_speech_recognize_textview = view.findViewById(R.id.current_speech_recognize_textview);

        current_speech_recognize_textview.setVisibility(View.INVISIBLE);

        uri_permission_setting_imagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String action[] = {"video/*"};
                videoFilePickerLauncher.launch(action);
            }
        });

        buttonOnClickActionSetting(go_homebase_button);
        buttonOnClickActionSetting(temi_introduction_button);
        buttonOnClickActionSetting(thouzer_introduction_button);
        buttonOnClickActionSetting(nova5_introduction_button);
        buttonOnClickActionSetting(Mg400_introduction_button);
        buttonOnClickActionSetting(voice_command_button);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        speechRecognizeUtilities = new SpeechRecognizeUtilities(context);
    }

    private final ActivityResultLauncher<String[]> videoFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null){
                        Log.d(TAG, "onActivityResult: uri is " + result.toString());
                        context.getContentResolver().takePersistableUriPermission(result,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
            });

    private void buttonOnClickActionSetting(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uriString = "";
                switch (button.getId()){
                    case R.id.go_homebase_button:
                        onGoHomeBaseButtonClickListener.homeBaseButtonClick();
                        break;

                    case R.id.temi_introduction_button:
                        uriString = VideoConstant.TEMI_VIDEO_URI_STRING;
                        onIntroductionButtonClickListener.introductionButtonClick(uriString);
                        break;

                    case R.id.thouzer_introduction_button:
                        uriString = VideoConstant.THOUZER_VIDEO_URI_STRING;
                        onIntroductionButtonClickListener.introductionButtonClick(uriString);
                        break;

                    case R.id.nova5_introduction_button:
                        uriString = VideoConstant.NOVA5_VIDEO_URI_STRING;
                        onIntroductionButtonClickListener.introductionButtonClick(uriString);
                        break;

                    case R.id.Mg400_introduction_button:
                        uriString = VideoConstant.MG400_VIDEO_URI_STRING;
                        onIntroductionButtonClickListener.introductionButtonClick(uriString);
                        break;

                    case R.id.voice_command_button:
                        current_speech_recognize_textview.setVisibility(View.VISIBLE);
                        speechRecognizeUtilities.startSpeakRecognize();
//                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                speechRecognizeUtilities.stopSpeakRecognize();
//                            }
//                        },8000);
                        break;

                }
            }
        });
    }

    public void stopSpeechRecognizeDetect(){
        speechRecognizeUtilities.stopSpeakRecognize();
        current_speech_recognize_textview.setVisibility(View.INVISIBLE);
    }

    public void setSpeechRecognizeResultInTextView(String text){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                current_speech_recognize_textview.setText(text);
            }
        });

    }
}
