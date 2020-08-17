package com.kiatech.kia;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class MyService extends AccessibilityService {

    WindowManager windowManager;
    ImageView back,home,notification,minimize;
    WindowManager.LayoutParams params;
    AccessibilityService service;

    int mDebugDepth = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Status", "Service Created");

    }

    void printAllViews(AccessibilityNodeInfo mNodeInfo) {
        if (mNodeInfo == null) return;
        String log ="";
        for (int i = 0; i < mDebugDepth; i++) {
            log += ".";
        }
        log+="("+mNodeInfo.getText() +" <-- "+
                mNodeInfo.getViewIdResourceName()+")";
        Log.d("Component", log);
        if (mNodeInfo.getChildCount() < 1) return;
        mDebugDepth++;

        for (int i = 0; i < mNodeInfo.getChildCount(); i++) {
            printAllViews(mNodeInfo.getChild(i));
        }
        mDebugDepth--;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("Status", "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("Status", "Event Occurred");
        mDebugDepth = 0;
        printAllViews(event.getSource());
    }

    @Override
    public void onInterrupt() {

    }
}
