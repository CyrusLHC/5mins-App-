package com.example.a5mins;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CaseDetailActivity extends AppCompatActivity {
    private static final String TAG = "CaseDetailActivity";
    private TextView locationTextView;
    private TextView noteTextView;
    private TextView timeTextView;
    private TextView statusTextView;
    private TextView driverTextView;
    private Button completeButton;
    private Button navigateButton;
    private String caseId;
    private String driver;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);

        // 初始化視圖
        locationTextView = findViewById(R.id.locationTextView);
        noteTextView = findViewById(R.id.noteTextView);
        timeTextView = findViewById(R.id.timeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        driverTextView = findViewById(R.id.driverTextView);
        completeButton = findViewById(R.id.completeButton);
        navigateButton = findViewById(R.id.navigateButton);

        // 獲取傳遞過來的數據
        location = getIntent().getStringExtra("location");
        String note = getIntent().getStringExtra("note");
        String time = getIntent().getStringExtra("time");
        String status = getIntent().getStringExtra("status");
        driver = getIntent().getStringExtra("driver");
        caseId = getIntent().getStringExtra("caseId");

        // 設置文本
        locationTextView.setText("地點：" + location);
        noteTextView.setText("備註：" + note);
        timeTextView.setText("時間：" + time);
        statusTextView.setText("狀態：" + status);
        driverTextView.setText("司機：" + driver);

        // 設置導航按鈕
        navigateButton.setOnClickListener(v -> {
            if (location != null && !location.isEmpty()) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(location));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "請安裝 Google Maps", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "無法獲取地點信息", Toast.LENGTH_SHORT).show();
            }
        });

        // 設置完成按鈕點擊事件
        completeButton.setOnClickListener(v -> {
            if (caseId == null || caseId.isEmpty()) {
                Toast.makeText(this, "無法獲取車案信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (driver == null || driver.isEmpty()) {
                Toast.makeText(this, "無法獲取司機信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新車案狀態為"已完成"
            DatabaseReference caseRef = FirebaseDatabase.getInstance().getReference("cases")
                    .child(caseId);
            
            // 更新用戶狀態為"接單中"
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(driver);
            
            caseRef.child("status").setValue("已完成")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "車案狀態更新成功");
                    // 更新用戶狀態
                    userRef.child("status").setValue("接單中")
                        .addOnSuccessListener(aVoid2 -> {
                            Log.d(TAG, "用戶狀態更新成功");
                            Toast.makeText(CaseDetailActivity.this, "車案已完成", Toast.LENGTH_SHORT).show();
                            // 返回主頁面
                            Intent intent = new Intent(CaseDetailActivity.this, MainActivity.class);
                            intent.putExtra("username", driver);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "更新用戶狀態失敗", e);
                            Toast.makeText(CaseDetailActivity.this, "更新用戶狀態失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "更新車案狀態失敗", e);
                    Toast.makeText(CaseDetailActivity.this, "更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }
} 