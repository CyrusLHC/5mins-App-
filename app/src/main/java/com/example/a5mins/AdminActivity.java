package com.example.a5mins;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminActivity extends AppCompatActivity {
    private Button logoutButton;
    private Button createAccountButton;
    private Button newCaseButton;
    private Button carCaseListButton;
    private TextView titleTextView;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // 獲取傳遞過來的用戶名
        username = getIntent().getStringExtra("username");
        if (username == null) {
            username = "未知用戶";
        }

        logoutButton = findViewById(R.id.logoutButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        newCaseButton = findViewById(R.id.newCaseButton);
        carCaseListButton = findViewById(R.id.carCaseListButton);
        titleTextView = findViewById(R.id.titleTextView);

        // 設置標題
        titleTextView.setText("管理員頁面-" + username);

        logoutButton.setOnClickListener(v -> {
            // 更新狀態為"已下綫"並清空位置
            final String currentUsername = username;  // 創建一個 final 變量
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUsername);
            userRef.child("status").setValue("已下綫")
                .addOnSuccessListener(aVoid -> {
                    // 清空位置信息
                    userRef.child("location").setValue("")
                        .addOnSuccessListener(aVoid2 -> {
                            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdminActivity.this, "清空位置信息失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "狀態更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        createAccountButton.setOnClickListener(v -> {
            // 跳轉到創建賬號頁面
            Intent intent = new Intent(AdminActivity.this, AdminCreateAccountActivity.class);
            startActivity(intent);
        });

        newCaseButton.setOnClickListener(v -> {
            // 跳轉到新增車案頁面
            Intent intent = new Intent(AdminActivity.this, NewCaseActivity.class);
            startActivity(intent);
        });

        carCaseListButton.setOnClickListener(v -> {
            // 跳轉到車案列表頁面
            Intent intent = new Intent(AdminActivity.this, CarCaseListActivity.class);
            startActivity(intent);
        });
    }
} 