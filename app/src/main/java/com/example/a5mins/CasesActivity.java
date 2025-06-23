package com.example.a5mins;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.a5mins.adapters.MyCaseAdapter;
import com.example.a5mins.models.Case;
import java.util.ArrayList;
import java.util.List;

public class CasesActivity extends AppCompatActivity {
    private TextView titleTextView;
    private RecyclerView caseListRecyclerView;
    private BottomNavigationView bottomNavigation;
    private String username;
    private MyCaseAdapter caseAdapter;
    private List<Case> caseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cases);

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

        // 初始化視圖
        titleTextView = findViewById(R.id.titleTextView);
        caseListRecyclerView = findViewById(R.id.caseListRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // 設置標題
        titleTextView.setText("我的車案 - " + username);

        // 初始化 RecyclerView
        caseList = new ArrayList<>();
        caseAdapter = new MyCaseAdapter(caseList, username);
        caseListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        caseListRecyclerView.setAdapter(caseAdapter);

        // 設置底部導航
        bottomNavigation.setSelectedItemId(R.id.navigation_cases);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(CasesActivity.this, MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_cases) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent intent = new Intent(CasesActivity.this, ProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // 監聽當前用戶的車案
        DatabaseReference casesRef = FirebaseDatabase.getInstance().getReference("cases");
        casesRef.orderByChild("driver").equalTo(username).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                caseList.clear();
                for (com.google.firebase.database.DataSnapshot caseSnapshot : snapshot.getChildren()) {
                    Case caseItem = caseSnapshot.getValue(Case.class);
                    if (caseItem != null) {
                        caseList.add(caseItem);
                    }
                }
                caseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(CasesActivity.this, "獲取車案失敗：" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 