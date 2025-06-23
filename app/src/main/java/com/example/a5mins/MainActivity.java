package com.example.a5mins;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.a5mins.adapters.CaseAdapter;
import com.example.a5mins.models.Case;
import com.example.a5mins.services.LocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button workStatusButton;
    private TextView statusTextView;
    private TextView messageTextView;
    private String username;
    private DatabaseReference userRef;
    private boolean isWorking = false;
    private BottomNavigationView bottomNavigation;
    private RecyclerView caseListRecyclerView;
    private CaseAdapter caseAdapter;
    private List<Case> caseList;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private Handler autoRefreshHandler = new Handler();
    private Runnable autoRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化位置服務
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 檢查位置權限
        checkLocationPermission();

        // 獲取用戶名
        username = getIntent().getStringExtra("username");
        if (username == null) {
            // 如果沒有從 Intent 獲取到用戶名，嘗試從 SharedPreferences 獲取
            username = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getString("username", null);
        }
        
        if (username == null) {
            // 如果還是沒有用戶名，返回登錄頁面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 保存用戶名到 SharedPreferences
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .apply();

        // 初始化 Firebase 引用
        userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        // 初始化視圖
        workStatusButton = findViewById(R.id.workStatusButton);
        statusTextView = findViewById(R.id.statusTextView);
        messageTextView = findViewById(R.id.messageTextView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        caseListRecyclerView = findViewById(R.id.caseListRecyclerView);

        // 初始化 RecyclerView
        caseList = new ArrayList<>();
        // 先創建一個默認位置，之後會更新
        currentLocation = new LatLng(0, 0);
        caseAdapter = new CaseAdapter(caseList, username, currentLocation);
        caseListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        caseListRecyclerView.setAdapter(caseAdapter);

        // 獲取當前位置
        updateCurrentLocation();

        // 設置工作狀態按鈕點擊事件
        workStatusButton.setOnClickListener(v -> {
            String buttonText = workStatusButton.getText().toString();
            String newStatus;
            
            if (buttonText.equals("點我上班")) {
                newStatus = "接單中";
            } else {
                newStatus = "休息中";
            }
            
            updateStatus(newStatus);
        });

        // 監聽狀態變化
        userRef.child("status").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null) {
                    statusTextView.setText("我的狀態：" + status);
                    isWorking = status.equals("接單中");
                    updateButtonState();
                    updateCaseListVisibility();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(MainActivity.this, "狀態更新失敗：" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 監聽未接單的車案
        DatabaseReference casesRef = FirebaseDatabase.getInstance().getReference("cases");
        casesRef.orderByChild("status").equalTo("未接單").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                caseList.clear();
                long now = System.currentTimeMillis();
                for (com.google.firebase.database.DataSnapshot caseSnapshot : snapshot.getChildren()) {
                    Case caseItem = caseSnapshot.getValue(Case.class);
                    if (caseItem != null) {
                        String priorityUser = caseItem.getPriorityUser();
                        long createdAt = caseItem.getCreatedAt();
                        if ((priorityUser != null && priorityUser.equals(username) && now - createdAt < 10000)
                            || (now - createdAt >= 10000)) {
                            caseList.add(caseItem);
                        }
                    }
                }
                caseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(MainActivity.this, "獲取車案失敗：" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 啟動自動刷新
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                caseAdapter.notifyDataSetChanged();
                autoRefreshHandler.postDelayed(this, 1000); // 每秒刷新
            }
        };
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 1000);

        // 設置底部導航
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // 已經在主頁，不需要做任何事
                return true;
            } else if (itemId == R.id.navigation_cases) {
                Intent intent = new Intent(MainActivity.this, CasesActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 權限被授予
                Toast.makeText(this, "位置權限已授予", Toast.LENGTH_SHORT).show();
            } else {
                // 權限被拒絕
                Toast.makeText(this, "需要位置權限才能使用此功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateStatus(String newStatus) {
        userRef.child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> {
                updateButtonState();
                messageTextView.setText(isWorking ? "您現在正在等待派單!!!" : "您現在正在休息~");
                updateCaseListVisibility();
                
                // 根據狀態啟動或停止位置服務
                Intent serviceIntent = new Intent(this, LocationService.class);
                serviceIntent.putExtra("username", username);
                
                if (newStatus.equals("接單中") || newStatus.equals("載客中")) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        startService(serviceIntent);
                    } else {
                        Toast.makeText(this, "需要位置權限才能開始工作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    stopService(serviceIntent);
                    // 清空位置信息
                    userRef.child("location").setValue("");
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "狀態更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateButtonState() {
        if (isWorking) {
            workStatusButton.setText("點我下班");
            workStatusButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        } else {
            workStatusButton.setText("點我上班");
            workStatusButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
        }
    }

    private void updateCaseListVisibility() {
        if (isWorking) {
            caseListRecyclerView.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);
        } else {
            caseListRecyclerView.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);
        }
    }

    private void updateCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            // 更新適配器中的位置
                            if (caseAdapter != null) {
                                caseAdapter.updateLocation(currentLocation);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }
} 