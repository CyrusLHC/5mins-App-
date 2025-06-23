package com.example.a5mins.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a5mins.CaseDetailActivity;
import com.example.a5mins.R;
import com.example.a5mins.models.Case;
import java.util.List;

public class MyCaseAdapter extends RecyclerView.Adapter<MyCaseAdapter.CaseViewHolder> {
    private List<Case> caseList;
    private String username;

    public MyCaseAdapter(List<Case> caseList, String username) {
        this.caseList = caseList;
        this.username = username;
    }

    @NonNull
    @Override
    public CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_case, parent, false);
        return new CaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaseViewHolder holder, int position) {
        Case caseItem = caseList.get(position);
        holder.locationTextView.setText("地點：" + caseItem.getLocation());
        holder.noteTextView.setText("備註：" + caseItem.getNote());
        holder.timeTextView.setText("時間：" + caseItem.getTime());
        holder.statusTextView.setText("狀態：" + caseItem.getStatus());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CaseDetailActivity.class);
            intent.putExtra("caseId", caseItem.getId());
            intent.putExtra("location", caseItem.getLocation());
            intent.putExtra("note", caseItem.getNote());
            intent.putExtra("time", caseItem.getTime());
            intent.putExtra("status", caseItem.getStatus());
            intent.putExtra("driver", caseItem.getDriver());
            intent.putExtra("spot", caseItem.getSpot());
            intent.putExtra("username", username);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return caseList.size();
    }

    static class CaseViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        TextView noteTextView;
        TextView timeTextView;
        TextView statusTextView;

        CaseViewHolder(View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
} 