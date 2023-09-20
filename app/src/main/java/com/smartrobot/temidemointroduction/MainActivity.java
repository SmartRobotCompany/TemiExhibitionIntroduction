package com.smartrobot.temidemointroduction;

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

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.smartrobot.temidemointroduction.constant.TemiConstant;
import com.smartrobot.temidemointroduction.constant.VideoConstant;
import com.smartrobot.temidemointroduction.fragment.GifTaskFragment;
import com.smartrobot.temidemointroduction.fragment.MainTaskFragment;
import com.smartrobot.temidemointroduction.fragment.VideoTaskFragment;
import com.smartrobot.temidemointroduction.listener.OnGoogleTtsStatusListener;
import com.smartrobot.temidemointroduction.listener.OnIntroductionButtonClickListener;
import com.smartrobot.temidemointroduction.listener.OnVideoPlayStatusListener;
import com.smartrobot.temidemointroduction.utilities.GoogleTtsUtilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements
        OnGoToLocationStatusChangedListener,
        OnRobotReadyListener,
        OnGoogleTtsStatusListener,
        OnVideoPlayStatusListener,
        OnIntroductionButtonClickListener {

    static final String TAG = "Debug_" + MainActivity.class.getSimpleName();

    private Robot robot;
    private String aimLocation = "";
    private List<String> temiLocations;
    private List<String> randomGoToLocations = new ArrayList<>();
    public FragmentManager fragmentManager;
    private int REQUEST_STORAGE_PERMISSION = 100;
    private GoogleTtsUtilities googleTtsUtilities;
    private String currentUriString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        robot = Robot.getInstance();
        getSupportActionBar().hide();

        checkStoragePermission();

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
            boolean hasMainFragmentOrNot = false;
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments){
                if (fragment instanceof MainTaskFragment){
                    hasMainFragmentOrNot = true;
                    break;
                }
            }

            if (hasMainFragmentOrNot == false){
                MainTaskFragment mainTaskFragment = new MainTaskFragment(MainActivity.this);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container,mainTaskFragment)
                        .commit();
            }
        }
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

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        VideoTaskFragment videoTaskFragment =
                                new VideoTaskFragment(MainActivity.this, currentUriString);
                        getSupportFragmentManager().beginTransaction()
                                .addToBackStack(MainTaskFragment.class.getSimpleName())
                                .replace(R.id.fragment_container,videoTaskFragment)
                                .commit();
                    }
                },1000);

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

                    default:
                        MainTaskFragment mainTaskFragment = new MainTaskFragment(MainActivity.this);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,mainTaskFragment)
                                .commit();
                        break;
                }
                break;

            case TemiConstant.GOOGLE_TTS_START:
                if (!text.equals(TemiConstant.FINISH_INITIAL)){
                    GifTaskFragment gifTaskFragment = new GifTaskFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,gifTaskFragment)
                            .commit();
                }
                break;

            case TemiConstant.GOOGLE_TTS_ERROR:
                break;
        }
    }

    @Override
    public void onVideoPlayEnded(String uriString) {
        MainTaskFragment mainTaskFragment = new MainTaskFragment(MainActivity.this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,mainTaskFragment)
                .commit();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                robotSpeak("產品介紹影片已經播放完畢，對產品有興趣的話，請洽詢現場的工作人員，謝謝");
            }
        },500);

    }

    @Override
    public void introductionButtonClick(String uriString) {
        currentUriString = uriString;
        temiRandomPatrol();
    }
}