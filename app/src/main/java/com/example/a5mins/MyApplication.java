package com.example.a5mins;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
} 