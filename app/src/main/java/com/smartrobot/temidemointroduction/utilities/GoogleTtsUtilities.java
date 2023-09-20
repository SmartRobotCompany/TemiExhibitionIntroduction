package com.smartrobot.temidemointroduction.utilities;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smartrobot.temidemointroduction.constant.TemiConstant;
import com.smartrobot.temidemointroduction.listener.OnGoogleTtsStatusListener;

import java.util.HashMap;
import java.util.Locale;

public class GoogleTtsUtilities extends AppCompatActivity {
    static final String TAG = "Debug_" + GoogleTtsUtilities.class.getSimpleName();
    private Context context;
    private TextToSpeech textToSpeech;
    private Locale locale;
    private int voiceQuality;
    private int voiceLatency;
    private HashMap<String, String> map = new HashMap<String, String>();
    private OnGoogleTtsStatusListener onGoogleTtsStatusListener;
    private String speakText = "";

    public GoogleTtsUtilities(Context context,
                              Locale locale,
                              int voiceQuality,
                              int voiceLatency,
                              OnGoogleTtsStatusListener onGoogleTtsStatusListener){
        this.context = context;
        this.locale = locale;
        this.voiceQuality = voiceQuality;
        this.voiceLatency = voiceLatency;
        this.onGoogleTtsStatusListener = onGoogleTtsStatusListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TemiConstant.GOOGLE_TTS_ID);
    }

    public void textSpeak(String text){
        speakText = text;
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS){
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Log.d(TAG, "tts onStart text is : " + speakText);
                            onGoogleTtsStatusListener.onGoogleTtsStatus(TemiConstant.GOOGLE_TTS_START,speakText);
                        }

                        @Override
                        public void onDone(String s) {
                            Log.d(TAG, "tts onDone text is : " + speakText);
                            onGoogleTtsStatusListener.onGoogleTtsStatus(TemiConstant.GOOGLE_TTS_FINISH,speakText);

                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "tts onError text is : " + speakText);
                            onGoogleTtsStatusListener.onGoogleTtsStatus(TemiConstant.GOOGLE_TTS_ERROR,speakText);
                        }
                    });

                    int result = textToSpeech.isLanguageAvailable(locale);
                    if (result == TextToSpeech.LANG_AVAILABLE) {
                        Log.d(TAG, "onInit: available");
                        // Create the voice
                        Voice voice = new Voice("speak",
                                locale, voiceQuality, voiceLatency, true, null);
                        // Set the voice
                        textToSpeech.setVoice(voice);
                        textToSpeech.setPitch(1f);
                        textToSpeech.setSpeechRate(1f);
                        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, TemiConstant.GOOGLE_TTS_ID);
                    } else {
                        // The language is not available, use the default language of the device
                        Log.d(TAG, "onInit: non available");
                        textToSpeech.setPitch(1f);
                        textToSpeech.setSpeechRate(1f);
                        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, TemiConstant.GOOGLE_TTS_ID);
                    }
                }
            }
        },"com.google.android.tts");
    }

    public void cancelTtsIntroduction(){
        if (textToSpeech != null){
            textToSpeech.stop();
        }
    }

    public void reTtsIntroduction(){
        textToSpeech.speak(speakText, TextToSpeech.QUEUE_ADD, null, TemiConstant.GOOGLE_TTS_ID);
    }
}
