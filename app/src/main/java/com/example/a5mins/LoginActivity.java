package com.example.a5mins;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText phoneNumberEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 獲取 Firebase 引用
        mDatabase = FirebaseDatabase.getInstance().getReference();

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String username = phoneNumberEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "請填寫完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 從 Firebase 驗證用戶
            mDatabase.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // 用戶存在,檢查密碼
                        Object passwordObj = dataSnapshot.child("password").getValue();
                        String storedPassword = passwordObj != null ? passwordObj.toString() : null;
                        Object isAdminObj = dataSnapshot.child("isAdmin").getValue();
                        Boolean isAdmin = isAdminObj != null ? Boolean.parseBoolean(isAdminObj.toString()) : false;

                        if (storedPassword != null && password.equals(storedPassword)) {
                            // 密碼正確
                            handleLoginSuccess(username, isAdmin);
                        } else {
                            Toast.makeText(LoginActivity.this, "密碼錯誤", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "用戶不存在", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, "數據庫錯誤: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void handleLoginSuccess(String username, boolean isAdmin) {
        // 更新用戶狀態為"休息中"
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
        userRef.child("status").setValue("休息中")
            .addOnSuccessListener(aVoid -> {
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                }
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(LoginActivity.this, "狀態更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 