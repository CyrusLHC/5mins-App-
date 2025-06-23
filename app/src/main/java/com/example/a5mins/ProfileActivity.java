package com.example.a5mins;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private Button logoutButton;
    private TextView titleTextView;
    private TextView usernameTextView;
    private TextView statusTextView;
    private String username;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        logoutButton = findViewById(R.id.logoutButton);
        titleTextView = findViewById(R.id.titleTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        statusTextView = findViewById(R.id.statusTextView);
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);

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

        // 初始化 Firebase 引用
        userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        // 設置標題
        titleTextView.setText("個人資料 - " + username);

        // 設置用戶名
        usernameTextView.setText("用戶名：" + username);

        // 監聽狀態變化
        userRef.child("status").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null) {
                    statusTextView.setText("狀態：" + status);
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "獲取狀態失敗：" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 設置底部導航
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_cases) {
                startActivity(new Intent(ProfileActivity.this, CasesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // 已經在會員中心，不需要做任何事
                return true;
            }
            return false;
        });

        // 設置登出按鈕點擊事件
        logoutButton.setOnClickListener(v -> {
            // 更新狀態為"已下綫"並清空位置
            userRef.child("status").setValue("已下綫")
                .addOnSuccessListener(aVoid -> {
                    // 清空位置信息
                    userRef.child("location").setValue("")
                        .addOnSuccessListener(aVoid2 -> {
                            // 清除 SharedPreferences 中的用戶信息
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    .edit()
                                    .clear()
                                    .apply();
                            
                            // 返回登錄頁面
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProfileActivity.this, "清空位置信息失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "狀態更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }
} 