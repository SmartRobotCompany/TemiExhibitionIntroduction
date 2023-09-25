package com.smartrobot.temidemointroduction.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.smartrobot.temidemointroduction.listener.OnLoadingPageFragmentNeedShowCloseListener;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeechRecognizeUtilities {
    private final static String TAG = "Debug_" + SpeechRecognizeUtilities.class.getSimpleName();
    private Context context;
    private OnLoadingPageFragmentNeedShowCloseListener onLoadingPageFragmentNeedShowCloseListener;
    private SpeechService speechService;
    private Model model;

    public SpeechRecognizeUtilities(Context context){
        this.context = context;
        this.onLoadingPageFragmentNeedShowCloseListener = (OnLoadingPageFragmentNeedShowCloseListener) context;
//        LibVosk.setLogLevel(LogLevel.INFO);s
        initModel();
    }

    private void  initModel(){
        StorageService.unpack(context,"model-en-us","model",
                (model) -> {this.model = model;},
                (exception) -> Log.d(TAG, "initiModel: Failed to unpack the model" + exception.getMessage()));
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoadingPageFragmentNeedShowCloseListener.loadingPageFragmentNeedClose();
                Toast.makeText(context,"OK",Toast.LENGTH_SHORT).show();
            }
        },8000);
    }

    public void startSpeakRecognize(){
        if (speechService != null){
            speechService.stop();
            speechService = null;
        }else {
            try {
                Recognizer recognizer = new Recognizer(model,16000.0f);
                speechService = new SpeechService(recognizer,16000.0f);
                speechService.startListening((RecognitionListener) context);
            }catch (IOException e){
                Log.d(TAG, "startSpeakRecognize: ");
                e.printStackTrace();
            }
        }
    }

    public void stopSpeakRecognize(){
        if (speechService != null){
            speechService.stop();
            speechService = null;
        }
    }
}
