package com.example.a5mins;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminCreateAccountActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText confirmUsernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Switch isAdminSwitch;
    private Button confirmButton;
    private Button backButton;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_ac);

        // 初始化 Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 初始化視圖
        usernameEditText = findViewById(R.id.usernameEditText);
        confirmUsernameEditText = findViewById(R.id.confirmUsernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        isAdminSwitch = findViewById(R.id.isAdminSwitch);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);

        // 設置返回按鈕
        backButton.setOnClickListener(v -> finish());

        // 設置確認按鈕
        confirmButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String confirmUsername = confirmUsernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            boolean isAdmin = isAdminSwitch.isChecked();

            // 驗證輸入
            if (username.isEmpty() || confirmUsername.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!username.equals(confirmUsername)) {
                Toast.makeText(this, "兩次輸入的賬號不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "兩次輸入的密碼不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 顯示確認對話框
            showConfirmDialog(username, password, isAdmin);
        });
    }

    private void showConfirmDialog(String username, String password, boolean isAdmin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_account, null);
        
        TextView usernameTextView = dialogView.findViewById(R.id.usernameTextView);
        TextView passwordTextView = dialogView.findViewById(R.id.passwordTextView);
        TextView isAdminTextView = dialogView.findViewById(R.id.isAdminTextView);
        Button createButton = dialogView.findViewById(R.id.createButton);

        usernameTextView.setText("賬號：" + username);
        passwordTextView.setText("密碼：" + password);
        isAdminTextView.setText("是否管理員賬號：" + (isAdmin ? "是" : "不是"));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        createButton.setOnClickListener(v -> {
            // 創建用戶數據
            mDatabase.child("users").child(username).setValue(new User(password, isAdmin))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "賬號創建成功", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "創建失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
        });

        dialog.show();
    }

    // 用戶數據類
    private static class User {
        public String password;
        public boolean isAdmin;
        public String status;

        public User() {
            // 需要空的構造函數用於 Firebase
        }

        public User(String password, boolean isAdmin) {
            this.password = password;
            this.isAdmin = isAdmin;
            this.status = "已下綫";  // 設置默認狀態
        }
    }
} 