package com.smartrobot.temidemointroduction.utilities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

public class CommonUtilities {
    private final static String TAG = "Debug_" +  CommonUtilities.class.getSimpleName();

    private Activity activity;

    public CommonUtilities(Activity activity){
        this.activity = activity;
    }

    public void cancelHandler(Handler handler){
        if (handler != null){
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG, "cancelHandler: 取消" + handler.getClass().getSimpleName());
            handler = null;
        }
    }

}
