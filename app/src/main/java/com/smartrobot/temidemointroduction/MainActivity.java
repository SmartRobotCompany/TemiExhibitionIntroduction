package com.smartrobot.temidemointroduction;

import androidx.annotation.CheckResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.robotemi.sdk.permission.Permission;
import com.smartrobot.temidemointroduction.constant.IntroductionContent;
import com.smartrobot.temidemointroduction.constant.TemiConstant;
import com.smartrobot.temidemointroduction.constant.VideoConstant;
import com.smartrobot.temidemointroduction.fragment.GifTaskFragment;
import com.smartrobot.temidemointroduction.fragment.MainTaskFragment;
import com.smartrobot.temidemointroduction.fragment.VideoTaskFragment;
import com.smartrobot.temidemointroduction.listener.OnGifTaskFragmentActionListener;
import com.smartrobot.temidemointroduction.listener.OnGoHomeBaseButtonClickListener;
import com.smartrobot.temidemointroduction.listener.OnGoogleTtsStatusListener;
import com.smartrobot.temidemointroduction.listener.OnIntroductionButtonClickListener;
import com.smartrobot.temidemointroduction.listener.OnVideoPlayStatusListener;
import com.smartrobot.temidemointroduction.utilities.GoogleTtsUtilities;

import org.jetbrains.annotations.NotNull;
import org.vosk.android.RecognitionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements
        OnGoToLocationStatusChangedListener,
        OnRobotReadyListener,
        OnGoogleTtsStatusListener,
        OnVideoPlayStatusListener,
        OnIntroductionButtonClickListener,
        OnGifTaskFragmentActionListener,
        OnGoHomeBaseButtonClickListener,
        RecognitionListener {

    static final String TAG = "Debug_" + MainActivity.class.getSimpleName();

    private Robot robot;
    private String aimLocation = "";
    private List<String> temiLocations;
    private List<String> randomGoToLocations = new ArrayList<>();
    public FragmentManager fragmentManager;
    private int REQUEST_STORAGE_PERMISSION = 2001;
    private int REQUEST_RECORD_AUDIO = 2002;
    private GoogleTtsUtilities googleTtsUtilities;
    private String currentUriString;
    private MainTaskFragment mainTaskFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        robot = Robot.getInstance();
        getSupportActionBar().hide();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                robotSpeak(TemiConstant.FINISH_INITIAL);
            }
        },1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        robot.addOnRobotReadyListener(this);
        robot.addOnGoToLocationStatusChangedListener(this);

        fragmentManager = getSupportFragmentManager();
        checkStoragePermission();
        checkRecordAudioPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        robot.removeOnRobotReadyListener(this);
        robot.removeOnGoToLocationStatusChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private void removeFragment(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments){
            if (fragment instanceof GifTaskFragment ||
                    fragment instanceof VideoTaskFragment){
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                Log.d(TAG, "removeFragment: remove");
                break;
            }
        }
    }
    private void locationInitial(){
        temiLocations = robot.getLocations();
        if (temiLocations.contains(TemiConstant.HOME_BASE)){
            temiLocations.remove(TemiConstant.HOME_BASE);
        }
    }

    private List<String> reGetLocationData(){
        List<String> list = new ArrayList<>(temiLocations);
        return list;
    }

    private String getRandomLocationToGo(List<String> list){
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    private void temiRandomPatrol(){
        if (randomGoToLocations.size() == 0){
            if (reGetLocationData().size() != 0 && reGetLocationData().size() > 1){
                randomGoToLocations = reGetLocationData();
                robotGoTo(SpeedLevel.HIGH, getRandomLocationToGo(randomGoToLocations));
            }
        }else if (randomGoToLocations.size() > 0){
            robotGoTo(SpeedLevel.HIGH, getRandomLocationToGo(randomGoToLocations));
        }
    }

    private void robotGoTo(SpeedLevel speedLevel, String location){
        robot.setGoToSpeed(speedLevel);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                aimLocation = location;
                robot.goTo(location);
            }
        },500);
    }

    private void robotSpeak(String text){
        if (!text.equals(TemiConstant.FINISH_INITIAL)){
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            boolean hasGifOrNot = false;

            for (Fragment fragment : fragments){
                if (fragment instanceof GifTaskFragment){
                    hasGifOrNot = true;
                    break;
                }
            }

            if (hasGifOrNot == false){
                GifTaskFragment gifTaskFragment = new GifTaskFragment(MainActivity.this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.display_fragment_container,gifTaskFragment)
                        .commit();
            }
        }
        googleTtsUtilities = new GoogleTtsUtilities(MainActivity.this, Locale.CHINA,
                Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, MainActivity.this);
        googleTtsUtilities.textSpeak(text);
    }

    private void handlerCancel(Handler handler){
        if (handler != null){
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG, "handlerCancel: cancel handler");
        }
    }

    private void checkStoragePermission(){
        int hasGone = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasGone != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
            , REQUEST_STORAGE_PERMISSION);
        }
    }

    private void checkRecordAudioPermission(){
        int hasGone = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (hasGone != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    //隱藏temi的TopBar
    private void refreshTemiUi() {
        try {
            ActivityInfo activityInfo = getPackageManager()
                    .getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            Robot.getInstance().onStart(activityInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus == true){
            if (requestPermissionIfNeeded(Permission.SETTINGS,TemiConstant.REQUEST_CODE_TEMI_SETTING)){
                return;
            }else {
                boolean hasMainFragmentOrNot = false;
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments){
                    if (fragment instanceof MainTaskFragment){
                        hasMainFragmentOrNot = true;
                        break;
                    }
                }

                if (hasMainFragmentOrNot == false){
                    mainTaskFragment = new MainTaskFragment(MainActivity.this);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, mainTaskFragment)
                            .commit();
                }
            }

        }
    }

    @CheckResult
    private boolean requestPermissionIfNeeded(Permission permission, int requestCode){

        if (robot.checkSelfPermission(permission) == Permission.GRANTED){
            //權限已經開啟
            //如果有進到if執行這一行，則執行完這一行後就會跳出requestPermissionIfNeeded()這個方法，並不會執行if{}外的程式
            return false;
        }

        //若權限沒有開啟，則請求開啟權限
        robot.requestPermissions(Collections.singletonList(permission),requestCode);
        return true;
    }

    @Override
    public void onRobotReady(boolean b) {
        if (b == true){
            refreshTemiUi();
            locationInitial();
        }
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String s, @NotNull String s1, int i, @NotNull String s2) {
        switch (s1){
            case OnGoToLocationStatusChangedListener.COMPLETE:
                robot.tiltAngle(45);

                if (randomGoToLocations.contains(s)){
                    randomGoToLocations.remove(s);
                }

                if (!s.equals(TemiConstant.HOME_BASE)){
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            robot.setVolume(5);
                            VideoTaskFragment videoTaskFragment =
                                    new VideoTaskFragment(MainActivity.this, currentUriString);
                            getSupportFragmentManager().beginTransaction()
                                    .addToBackStack(MainTaskFragment.class.getSimpleName())
                                    .replace(R.id.display_fragment_container,videoTaskFragment)
                                    .commit();
                        }
                    },1000);
                }
                break;

            case OnGoToLocationStatusChangedListener.ABORT:

                break;

            case OnGoToLocationStatusChangedListener.START:

                break;

            case OnGoToLocationStatusChangedListener.REPOSING:

                break;

            case OnGoToLocationStatusChangedListener.GOING:

                break;
        }
    }

    @Override
    public void onGoogleTtsStatus(String status, String text) {
        switch (status){
            case TemiConstant.GOOGLE_TTS_FINISH:
                switch (text){
                    case TemiConstant.FINISH_INITIAL:
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"語音功能初始化完成",Toast.LENGTH_SHORT).show();
                            }
                        },100);
                        break;
                }
                removeFragment();
                break;

            case TemiConstant.GOOGLE_TTS_START:
                if (!text.equals(TemiConstant.FINISH_INITIAL)){
                    robot.setVolume(3);
                }else {
                    robot.setVolume(0);
                }
                break;

            case TemiConstant.GOOGLE_TTS_ERROR:
                break;
        }
    }

    @Override
    public void onVideoPlayEnded(String uriString) {
//        GifTaskFragment gifTaskFragment = new GifTaskFragment(MainActivity.this);
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container,gifTaskFragment)
//                .commit();
        robotSpeak(IntroductionContent.videoFinishWord);
    }

    @Override
    public void introductionButtonClick(String uriString) {
        currentUriString = uriString;
        temiRandomPatrol();
    }

    @Override
    public void gifTaskFragmentLoadFinish() {

    }

    @Override
    public void homeBaseButtonClick() {
        robotGoTo(SpeedLevel.HIGH,TemiConstant.HOME_BASE);
    }

    @Override
    public void onPartialResult(String hypothesis) {
        JsonObject jsonObject = JsonParser
                .parseString(hypothesis.replace(" ","").replace("\n","")).getAsJsonObject();
        String text = jsonObject.get("partial").getAsString();
        Log.d(TAG, "onPartialResult: result is " + text);
        mainTaskFragment.setSpeechRecognizeResultInTextView(text);
    }

    @Override
    public void onResult(String hypothesis) {
        JsonObject jsonObject = JsonParser
                .parseString(hypothesis).getAsJsonObject();
        String text = jsonObject.get("text").getAsString();
        Log.d(TAG, "onResult: result is " + text);
        mainTaskFragment.setSpeechRecognizeResultInTextView(text);
        
        if (hypothesis.contains("charging") || 
                hypothesis.contains("charge") || 
                hypothesis.contains("charged")){
            robotGoTo(SpeedLevel.HIGH,TemiConstant.HOME_BASE);
            mainTaskFragment.stopSpeechRecognizeDetect();
        }else if (hypothesis.contains("mike")){
            robotSpeak("您好，我是邁克的服務型機器人");
            mainTaskFragment.stopSpeechRecognizeDetect();
        }else if (hypothesis.contains("time")){
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            String[] timeParts = simpleDateFormat.format(date).split(":");
            String hour = timeParts[0];
            String minute = timeParts[1];
            String speakWord = "現在是" + hour + "點" + minute + "分";
            robotSpeak(speakWord);
            mainTaskFragment.stopSpeechRecognizeDetect();
        }else if (hypothesis.contains("hello")){
            robotSpeak("您好，歡迎命令我替您做介紹");
            mainTaskFragment.stopSpeechRecognizeDetect();
        } else if (hypothesis.contains("merry christmas")) {
            robotSpeak("聖誕快樂");
            mainTaskFragment.stopSpeechRecognizeDetect();
        }

//        if (hypothesis.contains("充电")){
//            robotGoTo(SpeedLevel.HIGH,TemiConstant.HOME_BASE);
//            mainTaskFragment.stopSpeechRecognizeDetect();
//        }else if (hypothesis.contains("麦克")){
//            robotSpeak("您好，我是邁克的服務型機器人");
//            mainTaskFragment.stopSpeechRecognizeDetect();
//        } else if (hypothesis.contains("几点")) {
//            Calendar calendar = Calendar.getInstance();
//            Date date = calendar.getTime();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
//            String[] timeParts = simpleDateFormat.format(date).split(":");
//            String hour = timeParts[0];
//            String minute = timeParts[1];
//            String speakWord = "現在是" + hour + "點" + minute + "分";
//            robotSpeak(speakWord);
//            mainTaskFragment.stopSpeechRecognizeDetect();
//        } else if (hypothesis.contains("您好") || hypothesis.contains("你好")){
//            robotSpeak("您好，歡迎命令我替您做介紹");
//            mainTaskFragment.stopSpeechRecognizeDetect();
//        } else if (hypothesis.contains("发财") || hypothesis.contains("新年快乐")){
//            robotSpeak("新年快樂，恭喜發財");
//            mainTaskFragment.stopSpeechRecognizeDetect();
//        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        Log.d(TAG, "onFinalResult: result is " + hypothesis);
    }

    @Override
    public void onError(Exception exception) {
        Log.d(TAG, "onError: ");
        exception.printStackTrace();
    }

    @Override
    public void onTimeout() {

    }
}