package com.example.a5mins.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a5mins.CaseDetailActivity;
import com.example.a5mins.R;
import com.example.a5mins.models.Case;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.Arrays;

public class CaseAdapter extends RecyclerView.Adapter<CaseAdapter.CaseViewHolder> {
    private static final String TAG = "CaseAdapter";
    private static final String GEO_API_KEY = "your google api key"; // 佔位字串，請自行填入
    private List<Case> caseList;
    private String username;
    private LatLng currentLocation;
    private GeoApiContext geoApiContext;

    public CaseAdapter(List<Case> caseList, String username, LatLng currentLocation) {
        this.caseList = caseList;
        this.username = username;
        this.currentLocation = currentLocation;
        
        // 初始化 Google Maps API
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(GEO_API_KEY)
                .build();
    }

    @NonNull
    @Override
    public CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_case, parent, false);
        return new CaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaseViewHolder holder, int position) {
        Case caseItem = caseList.get(position);
        holder.locationTextView.setText("地點：" + caseItem.getLocation());
        holder.noteTextView.setText("備註：" + caseItem.getNote());
        holder.timeTextView.setText("時間：" + caseItem.getTime());

        // 計算距離和時間
        if (currentLocation != null) {
            calculateDistanceAndTime(holder, caseItem.getLocation());
        }

        holder.acceptButton.setOnClickListener(v -> {
            try {
                Log.d(TAG, "開始處理接單請求");
                Log.d(TAG, "當前用戶: " + username);

                if (username == null || username.isEmpty()) {
                    Log.e(TAG, "用戶名為空");
                    Toast.makeText(v.getContext(), "無法獲取用戶信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 更新車案狀態為"進行中"並設置司機
                DatabaseReference caseRef = FirebaseDatabase.getInstance().getReference("cases")
                        .child(caseItem.getId());
                
                // 更新用戶狀態為"載客中"
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(username);
                
                caseRef.child("status").setValue("進行中")
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "車案狀態更新成功");
                        // 更新司機信息
                        caseRef.child("driver").setValue(username)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "司機信息更新成功");
                                // 更新用戶狀態
                                userRef.child("status").setValue("載客中")
                                    .addOnSuccessListener(aVoid3 -> {
                                        Log.d(TAG, "用戶狀態更新成功");
                                        // 跳轉到車案詳情頁面
                                        Intent intent = new Intent(v.getContext(), CaseDetailActivity.class);
                                        intent.putExtra("location", caseItem.getLocation());
                                        intent.putExtra("note", caseItem.getNote());
                                        intent.putExtra("time", caseItem.getTime());
                                        intent.putExtra("status", "進行中");
                                        intent.putExtra("driver", username);
                                        intent.putExtra("caseId", caseItem.getId());
                                        v.getContext().startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "更新用戶狀態失敗", e);
                                        Toast.makeText(v.getContext(), "更新用戶狀態失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "更新司機信息失敗", e);
                                Toast.makeText(v.getContext(), "更新司機信息失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "接單失敗", e);
                        Toast.makeText(v.getContext(), "接單失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            } catch (Exception e) {
                Log.e(TAG, "處理接單時發生錯誤", e);
                Toast.makeText(v.getContext(), "發生錯誤：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateDistanceAndTime(CaseViewHolder holder, String destinationAddress) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(new com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude))
                    .destination(destinationAddress)
                    .mode(TravelMode.DRIVING)
                    .await();

            if (result.routes != null && result.routes.length > 0) {
                String distance = result.routes[0].legs[0].distance.humanReadable;
                String duration = result.routes[0].legs[0].duration.humanReadable;
                holder.distanceTextView.setText(String.format("距離：%s，預計時間：%s", distance, duration));
            }
        } catch (Exception e) {
            Log.e(TAG, "計算距離時發生錯誤", e);
            holder.distanceTextView.setText("無法計算距離");
        }
    }

    @Override
    public int getItemCount() {
        return caseList.size();
    }

    public void updateLocation(LatLng newLocation) {
        this.currentLocation = newLocation;
        notifyDataSetChanged();  // 重新計算所有項目的距離
    }

    static class CaseViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        TextView noteTextView;
        TextView timeTextView;
        TextView distanceTextView;
        Button acceptButton;

        CaseViewHolder(View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }
    }
} 