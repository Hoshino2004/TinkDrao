package com.example.tinkdrao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.R;

import java.util.List;

public class StatusOrderAdapter extends RecyclerView.Adapter<StatusOrderAdapter.StatusViewHolder> {
    private Context context;
    private List<String> statusList;
    private OnStatusClickListener listener;
    private int selectedPosition = -1; // Theo dõi item được chọn

    // Interface để xử lý sự kiện nhấn
    public interface OnStatusClickListener {
        void onStatusClick(String status);
    }

    public StatusOrderAdapter(Context context, List<String> statusList, OnStatusClickListener listener) {
        this.context = context;
        this.statusList = statusList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status_order, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        String status = statusList.get(position);
        holder.txtStatus.setText(status);

        // Thay đổi giao diện khi item được chọn
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.background_button_selected); // Background khi chọn
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.background_button_unselected); // Background mặc định
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.white));
        }

        // Xử lý sự kiện nhấn
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position; // Cập nhật vị trí được chọn
            notifyDataSetChanged(); // Cập nhật giao diện RecyclerView
            if (listener != null) {
                listener.onStatusClick(status); // Thông báo trạng thái được chọn
            }
        });
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        TextView txtStatus;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}