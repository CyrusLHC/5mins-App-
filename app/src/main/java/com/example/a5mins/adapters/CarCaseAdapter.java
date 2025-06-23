package com.example.a5mins.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a5mins.R;
import com.example.a5mins.models.CarCase;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class CarCaseAdapter extends RecyclerView.Adapter<CarCaseAdapter.CarCaseViewHolder> {
    private List<CarCase> carCases;
    private FirebaseDatabase database;

    public CarCaseAdapter(List<CarCase> carCases) {
        this.carCases = carCases;
        this.database = FirebaseDatabase.getInstance();
    }

    public void updateData(List<CarCase> newCarCases) {
        this.carCases = newCarCases;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CarCaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car_case, parent, false);
        return new CarCaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarCaseViewHolder holder, int position) {
        CarCase carCase = carCases.get(position);
        holder.locationTextView.setText(carCase.getLocation());
        holder.statusTextView.setText(carCase.getStatus());
        
        // 設置狀態文字顏色
        switch (carCase.getStatus()) {
            case "未接單":
                holder.statusTextView.setTextColor(Color.parseColor("#FFA500")); // 橙黃色
                break;
            case "進行中":
                holder.statusTextView.setTextColor(Color.parseColor("#8B0000")); // 深紅色
                break;
            case "已完成":
                holder.statusTextView.setTextColor(Color.parseColor("#006400")); // 深綠色
                break;
            default:
                holder.statusTextView.setTextColor(Color.BLACK);
                break;
        }
        
        holder.noteTextView.setText(carCase.getNote().isEmpty() ? "無備註" : "備註：" + carCase.getNote());
        holder.timeTextView.setText(carCase.getTime());

        holder.deleteButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle("確認刪除")
                .setMessage("確定要刪除此車案嗎？")
                .setPositiveButton("確定", (dialog, which) -> {
                    database.getReference("cases").child(carCase.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // 删除成功，数据会自动更新
                        })
                        .addOnFailureListener(e -> {
                            // 删除失败，显示错误信息
                            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                                .setTitle("刪除失敗")
                                .setMessage("無法刪除車案：" + e.getMessage())
                                .setPositiveButton("確定", null)
                                .show();
                        });
                })
                .setNegativeButton("取消", null)
                .show();
        });
    }

    @Override
    public int getItemCount() {
        return carCases.size();
    }

    static class CarCaseViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        TextView statusTextView;
        TextView noteTextView;
        TextView timeTextView;
        ImageButton deleteButton;

        CarCaseViewHolder(View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
} 