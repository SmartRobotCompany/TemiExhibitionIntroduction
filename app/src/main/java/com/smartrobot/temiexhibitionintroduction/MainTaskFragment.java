package com.smartrobot.temiexhibitionintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public class MainTaskFragment extends Fragment {
    private static final String TAG = "Debug_" + MainTaskFragment.class.getSimpleName();
    private static MainTaskFragment instance;

    private Button mg400_tablet_button;
    private Button mg400_scale_button;
    private Button mg400_router_button;
    private Button cr5_button;
    private Button thouzer_button;
    private Button temi_button;

    public static MainTaskFragment getInstance() {
        if (instance == null){
            instance = new MainTaskFragment();
        }
        return instance;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.main_task_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        mg400_tablet_button = view.findViewById(R.id.mg400_tablet_button);
        mg400_scale_button = view.findViewById(R.id.mg400_scale_button);
        mg400_router_button = view.findViewById(R.id.mg400_router_button);
        cr5_button = view.findViewById(R.id.cr5_button);
        thouzer_button = view.findViewById(R.id.thouzer_button);
        temi_button = view.findViewById(R.id.temi_button);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        introductionButtonSetting(mg400_router_button);
        introductionButtonSetting(mg400_tablet_button);
        introductionButtonSetting(mg400_scale_button);
        introductionButtonSetting(cr5_button);
        introductionButtonSetting(thouzer_button);
        introductionButtonSetting(temi_button);
    }

    private void introductionButtonSetting(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (button.getId()){
                    case R.id.mg400_router_button:
                        Log.d(TAG, "onClick: router");
                        locationBroadcastIntent(BroadcastConstant.GO_LOCATION_ROUTER);
                        break;

                    case R.id.mg400_scale_button:
                        Log.d(TAG, "onClick: scale");
                        locationBroadcastIntent(BroadcastConstant.GO_LOCATION_SCALE);
                        break;

                    case R.id.mg400_tablet_button:
                        Log.d(TAG, "onClick: tablet");
                        locationBroadcastIntent(BroadcastConstant.GO_LOCATION_TABLET);
                        break;

                    case R.id.cr5_button:
                        Log.d(TAG, "onClick: cr5");
                        locationBroadcastIntent(BroadcastConstant.GO_LOCATION_CR);
                        break;

                    case R.id.thouzer_button:
                        Log.d(TAG, "onClick: thouzer");
                        locationBroadcastIntent(BroadcastConstant.GO_LOCATION_THOUZER);
                        break;

                    case R.id.temi_button:
                        Log.d(TAG, "onClick: temi");
                        locationBroadcastIntent(BroadcastConstant.GO_LOCATION_TEMI);
                        break;

                }
            }
        });
    }

    public void locationBroadcastIntent(String message){
        Intent intent = new Intent();
        intent.setAction(BroadcastConstant.GO_LOCATION_ACTION);
        intent.putExtra(BroadcastConstant.GO_LOCATION_TASK,message);
        getActivity().sendBroadcast(intent);
    }
}
