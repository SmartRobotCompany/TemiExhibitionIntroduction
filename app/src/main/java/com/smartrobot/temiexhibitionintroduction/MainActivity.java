package com.smartrobot.temiexhibitionintroduction;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.listener.OnReposeStatusChangedListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        OnGoToLocationStatusChangedListener,
        OnReposeStatusChangedListener {

    static final String TAG = "Debug_" + MainActivity.class.getSimpleName();

    private Robot robot;
    private String aimLocation = "";

    private Button stop_movement_button;

    private TextToSpeech textToSpeech;

    private Handler goBackHomeHandler;
    private Handler introductionFinishHandler;
    private Handler goAgainHandler;
    private Handler reposeAgainHandler;

    //是否要停止temi的導航
    private boolean isNeedToStopTemiMoveOrNot;

    //temi是否要執行重新定位
    private boolean isNeedToReposeOrNot;

    //temi是否是自動觸發重新定位
    private boolean isTemiAutoRepose;

    private int delayGoHomeTime = 10000;

    private int delayFinishIntroductionTime = 2000;

    //temi受干擾後，幾秒後要重啟導航
    private int reStartGoToTime = 10000;

    //temi定位失敗後，幾秒後要重新定位
    private int reposeStartTime = 2000;

    private TemiBroadcastReceiveFunction temiBroadcastReceiveFunction = new TemiBroadcastReceiveFunction();

    //Fragment變數
    private boolean isShowFragmentOrNot = false;
    public FragmentManager fragmentManager;

    private int REQUEST_STORAGE_PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        robot = Robot.getInstance();
        getSupportActionBar().hide();

        checkStoragePermission();

        //過濾廣播訊息
        IntentFilter intentFilter = new IntentFilter();
        //管理多個Action
        intentFilter.addAction(BroadcastConstant.GO_LOCATION_ACTION);
        registerReceiver(temiBroadcastReceiveFunction,intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        robot.addOnRobotReadyListener(this);
        robot.addOnGoToLocationStatusChangedListener(this);
        robot.addOnReposeStatusChangedListener(this);

        stop_movement_button = findViewById(R.id.stop_movement_button);
        stop_movement_button.setBackgroundColor(Color.TRANSPARENT);

        stop_movement_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTemiMovement();
            }
        });

        fragmentManager = getSupportFragmentManager();

        if (isShowFragmentOrNot == false){
            loadMainTaskFragment();
            isShowFragmentOrNot = true;
        }

        //google語音初始化
        googleTtsInitial();
        
        //google語音狀態監聽器
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                Log.d(TAG, "onStart: TTS Start " + s);
                switch (s){
                    case IntroductionContent.introduction_finish:
                        loadGifTaskFragment();
                        break;
                }
            }

            @Override
            public void onDone(String s) {
                Log.d(TAG, "onDone: TTS Complete " + s);

                if (!(s.equals(IntroductionContent.mg400_basic) || s.equals(IntroductionContent.introduction_finish))){
                    introductionFinishHandler = new Handler(Looper.getMainLooper());
                    introductionFinishHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            convertTextToSpeech(IntroductionContent.introduction_finish,TextToSpeech.QUEUE_FLUSH);
                        }
                    },delayFinishIntroductionTime);
                }else if (s.equals(IntroductionContent.introduction_finish)){
                    loadMainTaskFragment();
                    goBackHomeHandler = new Handler(Looper.getMainLooper());
                    goBackHomeHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            robot.goTo(IntroductionContent.home_location);
                        }
                    },delayGoHomeTime);
                }
            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "onError: TTS Error " + s);
                convertTextToSpeech(s,TextToSpeech.QUEUE_FLUSH);
            }
        });
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
        robot.removeOnReposeStatusChangedListener(this);
        textToSpeech.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private void googleTtsInitial(){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int languageSetting = textToSpeech.setLanguage(Locale.TAIWAN);
                    if (languageSetting == TextToSpeech.LANG_MISSING_DATA
                            || languageSetting == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d(TAG, "onInit: google TTS語言功能設定失敗");
                    }else {
                        textToSpeech.setPitch(1);
                    }
                }
            }
        });
    }

    private void convertTextToSpeech(String text,int type){
        if (text == null || text.equals("")){
            Log.d(TAG, "ConvertTextToSpeech: 沒有字串資料");
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                textToSpeech.speak(text,type,null,text);
            }else{
                textToSpeech.speak(text,type,null);
            }
        }
    }

    private void loadMainTaskFragment(){
        MainTaskFragment mainTaskFragment = new MainTaskFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container,mainTaskFragment,"mainTaskFragment")
                .commit();
    }

    private void loadGifTaskFragment(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GifTaskFragment gifTaskFragment = new GifTaskFragment();
                fragmentManager.beginTransaction()
                        .addToBackStack(MainTaskFragment.class.getSimpleName())
                        .replace(R.id.fragment_container,gifTaskFragment)
                        .commit();
            }
        });
    }

    private void loadVideoTaskFragment(String locaiton){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VideoTaskFragment videoTaskFragment = new VideoTaskFragment(locaiton);
                fragmentManager.beginTransaction()
                        .addToBackStack(MainTaskFragment.class.getSimpleName())
                        .replace(R.id.fragment_container,videoTaskFragment)
                        .commit();
            }
        });
    }

    private void stopTemiMovement(){
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (!(currentFragment instanceof MainTaskFragment)){
            loadMainTaskFragment();
            Log.d(TAG, "stopTemiMovement: change");
        }else {
            Log.d(TAG, "stopTemiMovement: keep");
        }
        isNeedToStopTemiMoveOrNot = true;
        robot.stopMovement();
        textToSpeech.stop();
        handlerCancel(goAgainHandler);
        handlerCancel(reposeAgainHandler);
        handlerCancel(introductionFinishHandler);
        Toast.makeText(this,"停止機器人的行為",Toast.LENGTH_SHORT);
        Log.d(TAG, "stopTemiMovement: 用stopMovement()來停止temi的動作");
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
    public void onRobotReady(boolean b) {
        if (b == true){
            refreshTemiUi();
        }
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String s, @NotNull String s1, int i, @NotNull String s2) {
        switch (s1){
            case OnGoToLocationStatusChangedListener.COMPLETE:
                if (!(s.equals(IntroductionContent.home_location))){
                    loadVideoTaskFragment(s);
                }
                if (s.equals(IntroductionContent.mg400_router_location)
                        || s.equals(IntroductionContent.mg400_scale_location)
                        || s.equals(IntroductionContent.mg400_tablet_location)){
                    convertTextToSpeech(IntroductionContent.mg400_basic,TextToSpeech.QUEUE_FLUSH);

                    switch (s){
                        case IntroductionContent.mg400_router_location:
                            convertTextToSpeech(IntroductionContent.mg400_router,TextToSpeech.QUEUE_ADD);
                            break;

                        case IntroductionContent.mg400_scale_location:
                            convertTextToSpeech(IntroductionContent.mg400_scale,TextToSpeech.QUEUE_ADD);
                            break;

                        case IntroductionContent.mg400_tablet_location:
                            convertTextToSpeech(IntroductionContent.mg400_tablet,TextToSpeech.QUEUE_ADD);
                            break;
                    }

                }else {
                    switch (s){
                        case IntroductionContent.cr5_location:
                            convertTextToSpeech(IntroductionContent.cr5_basic,TextToSpeech.QUEUE_FLUSH);
                            break;

                        case IntroductionContent.thouzer_location:
                            convertTextToSpeech(IntroductionContent.thouzer_basic,TextToSpeech.QUEUE_FLUSH);
                            break;

                        case IntroductionContent.temi_location:
                            convertTextToSpeech(IntroductionContent.temi_basic,TextToSpeech.QUEUE_FLUSH);
                            break;

                        case IntroductionContent.home_location:

                            break;
                    }
                }
                break;

            case OnGoToLocationStatusChangedListener.ABORT:
                Log.d(TAG, "onGoToLocationStatusChanged: Abort");

                if (isNeedToStopTemiMoveOrNot == false
                        && isNeedToReposeOrNot == false
                        && isTemiAutoRepose == false){
                    goAgainHandler = new Handler();
                    goAgainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            aimLocation = s;
                            Log.d(TAG, "OnGoToLocationStatusChangedListener.ABORT: Abort後即將再次執行導航");
                            robot.goTo(s);
                            Log.d(TAG, "OnGoToLocationStatusChangedListener.ABORT: Abort後再次執行導航");
                        }
                    },reStartGoToTime);
                    Log.d(TAG, "OnGoToLocationStatusChangedListener.ABORT: Abort後執行goAgainHandler");
                }else if (isNeedToStopTemiMoveOrNot == true || isNeedToReposeOrNot == true){
                    Log.d(TAG, "OnGoToLocationStatusChangedListener.ABORT: Abort後沒有執行goAgainHandler");
                    Log.d(TAG, "OnGoToLocationStatusChangedListener.ABORT: isNeedToReposeOrNot的狀態為 " + isNeedToReposeOrNot);
                    Log.d(TAG, "OnGoToLocationStatusChangedListener.ABORT: isNeedToStopTemiMoveOrNot的狀態為 " + isNeedToStopTemiMoveOrNot);
                }

                break;

            case OnGoToLocationStatusChangedListener.START:
                Log.d(TAG, "onGoToLocationStatusChanged: Start");
                isNeedToStopTemiMoveOrNot = false;
                isNeedToReposeOrNot = false;
                isTemiAutoRepose = false;
                handlerCancel(goBackHomeHandler);
                handlerCancel(goAgainHandler);
                handlerCancel(reposeAgainHandler);
                handlerCancel(introductionFinishHandler);
                aimLocation = s;
                break;

            case OnGoToLocationStatusChangedListener.REPOSING:
                Log.d(TAG, "onGoToLocationStatusChanged: reposing");
                //temi在導航過程中自主觸發重新定位的任務時才會進到這個階段
                Log.d(TAG, "OnGoToLocationStatusChangedListener.REPOSING: temi開始自主重新定位");
                isTemiAutoRepose = true;
                Log.d(TAG, "OnGoToLocationStatusChangedListener.REPOSING: isTemiAutoRepose的狀態為" + isTemiAutoRepose);
                isNeedToReposeOrNot = true;
                Log.d(TAG, "OnGoToLocationStatusChangedListener.REPOSING: isNeedToReposeOrNot的狀態為" + isNeedToReposeOrNot);
                //紀錄尚未完成導航任務的目標地點，以便當temi定位失敗時，用robot.repose再次定位後，定位成功時可以命令temi繼續執行導航任務
                aimLocation = s;
                break;

            case OnGoToLocationStatusChangedListener.GOING:
                Log.d(TAG, "onGoToLocationStatusChanged: going");
                break;
        }
    }

    @Override
    public void onReposeStatusChanged(int i, @NotNull String s) {
        switch (i){
            case OnReposeStatusChangedListener.REPOSING_COMPLETE:
                Log.d(TAG,TAG + ": temi重新定位完成，狀態描述為: " + s);

                isNeedToReposeOrNot = false;
                Log.d(TAG,TAG + ": REPOSING_COMPLETE, isNeedToReposeOrNot的狀態為" + isNeedToReposeOrNot);

                if ((!aimLocation.equals("")) && (isTemiAutoRepose == false)){
                    //重新定位完成後(非temi自主觸發重新定位)，temi繼續完成尚未完成的導航任務
                    robot.goTo(aimLocation);
                }else if (isTemiAutoRepose == true){
                    isTemiAutoRepose = false;
                    Log.d(TAG,TAG + ": REPOSING_COMPLETE, isTemiAutoRepose的狀態為" + isTemiAutoRepose);
                }
                break;

            case OnReposeStatusChangedListener.REPOSING_START:
                Log.d(TAG,TAG + ": temi開始重新定位，狀態描述為: " + s);
                break;
            case OnReposeStatusChangedListener.REPOSE_REQUIRED:
                Log.d(TAG,TAG + ": temi準備重新定位，狀態描述為: " + s);
                break;
            case OnReposeStatusChangedListener.REPOSING_ABORT:

                Log.d(TAG,TAG + ": temi重新定位受到中斷，狀態描述為: " + s);

                isTemiAutoRepose = false;
                Log.d(TAG,TAG + ": REPOSING_ABORT, isTemiAutoRepose的狀態為" + isTemiAutoRepose);
                isNeedToReposeOrNot = true;
                Log.d(TAG,TAG + ": REPOSING_ABORT, isNeedToReposeOrNot的狀態為" + isNeedToReposeOrNot);

                if (isNeedToStopTemiMoveOrNot == false){
                    reposeAgainHandler = new Handler();
                    reposeAgainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            robot.repose();
                        }
                    },reposeStartTime);
                    Log.d(TAG,TAG + ": Abort後執行reposeAgainHandler");
                    break;

                }else if (isNeedToStopTemiMoveOrNot == true){
                    Log.d(TAG,TAG + ": Abort後沒有執行reposeAgainHandler");
                }
                break;

            case OnReposeStatusChangedListener.REPOSING_OBSTACLE_DETECTED:
                Log.d(TAG,TAG + ": temi在定位時偵測到障礙物，狀態描述為: " + s);
                break;
        }
    }

    private class TemiBroadcastReceiveFunction extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String currentAction = intent.getAction();
            assert currentAction != null;
            Log.d(TAG, "onReceive: current action is " + currentAction);

            switch (currentAction){
                case BroadcastConstant.GO_LOCATION_ACTION:

                    switch (intent.getStringExtra(BroadcastConstant.GO_LOCATION_TASK)){
                        case BroadcastConstant.GO_LOCATION_ROUTER:
                            robot.goTo(IntroductionContent.mg400_router_location);
                            break;

                        case BroadcastConstant.GO_LOCATION_SCALE:
                            robot.goTo(IntroductionContent.mg400_scale_location);
                            break;

                        case BroadcastConstant.GO_LOCATION_TABLET:
                            robot.goTo(IntroductionContent.mg400_tablet_location);
                            break;

                        case BroadcastConstant.GO_LOCATION_CR:
                            robot.goTo(IntroductionContent.cr5_location);
                            break;

                        case BroadcastConstant.GO_LOCATION_THOUZER:
                            robot.goTo(IntroductionContent.thouzer_location);
                            break;

                        case BroadcastConstant.GO_LOCATION_TEMI:
                            robot.goTo(IntroductionContent.temi_location);
                            break;
                    }

                    break;
            }
        }
    }
}