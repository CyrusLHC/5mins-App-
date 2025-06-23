package com.example.a5mins;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.a5mins.adapters.CarCaseAdapter;
import com.example.a5mins.models.CarCase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CarCaseListActivity extends AppCompatActivity {
    private static final String TAG = "CarCaseListActivity";
    private Button backButton;
    private ImageButton filterButton;
    private TextView currentFilterTextView;
    private RecyclerView carCaseRecyclerView;
    private CarCaseAdapter adapter;
    private DatabaseReference database;
    private List<CarCase> allCarCases;
    private String currentFilter = "全部";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_case_list);

        // 初始化视图
        backButton = findViewById(R.id.backButton);
        filterButton = findViewById(R.id.filterButton);
        currentFilterTextView = findViewById(R.id.currentFilterTextView);
        carCaseRecyclerView = findViewById(R.id.carCaseRecyclerView);
        
        // 初始化Firebase
        database = FirebaseDatabase.getInstance().getReference();
        allCarCases = new ArrayList<>();
        
        // 设置RecyclerView
        adapter = new CarCaseAdapter(new ArrayList<>());
        carCaseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        carCaseRecyclerView.setAdapter(adapter);

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> {
            finish();
        });

        // 过滤按钮点击事件
        filterButton.setOnClickListener(v -> {
            showFilterDialog();
        });

        // 加载车案数据
        loadCarCases();
    }

    private void showFilterDialog() {
        String[] filters = {"全部", "未接單", "進行中", "已完成"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("選擇過濾條件");
        builder.setItems(filters, (dialog, which) -> {
            currentFilter = filters[which];
            currentFilterTextView.setText(currentFilter);
            filterCarCases();
        });
        builder.show();
    }

    private void filterCarCases() {
        List<CarCase> filteredList = new ArrayList<>();
        for (CarCase carCase : allCarCases) {
            switch (currentFilter) {
                case "全部":
                    filteredList.add(carCase);
                    break;
                case "未接單":
                    if ("未接單".equals(carCase.getStatus())) {
                        filteredList.add(carCase);
                    }
                    break;
                case "進行中":
                    if ("進行中".equals(carCase.getStatus())) {
                        filteredList.add(carCase);
                    }
                    break;
                case "已完成":
                    if ("已完成".equals(carCase.getStatus())) {
                        filteredList.add(carCase);
                    }
                    break;
            }
        }
        adapter.updateData(filteredList);
    }

    private void loadCarCases() {
        Log.d(TAG, "开始加载车案数据");
        
        database.child("cases")
            .orderByChild("createdAt")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "获取到数据更新");
                    allCarCases.clear();
                    
                    for (DataSnapshot caseSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Log.d(TAG, "处理文档: " + caseSnapshot.getKey());
                            CarCase carCase = caseSnapshot.getValue(CarCase.class);
                            if (carCase != null) {
                                carCase.setId(caseSnapshot.getKey());
                                allCarCases.add(carCase);
                                Log.d(TAG, "添加车案: " + carCase.getLocation());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理文档时出错: " + caseSnapshot.getKey(), e);
                            Log.d(TAG, "原始数据: " + caseSnapshot.getValue());
                        }
                    }
                    
                    filterCarCases();
                    Log.d(TAG, "数据更新完成，列表大小: " + allCarCases.size());
                    
                    // 显示加载结果
                    if (allCarCases.isEmpty()) {
                        Toast.makeText(CarCaseListActivity.this, "暫無車案數據", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CarCaseListActivity.this, "成功加載 " + allCarCases.size() + " 個車案", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "加载数据失败", databaseError.toException());
                    Toast.makeText(CarCaseListActivity.this, "加載數據失敗：" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
} 