package com.et.etcare.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.ChatListItem;
import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private List<ChatListItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChatListItem item);
    }

    public ChatListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<ChatListItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatListItem item = items.get(position);
        holder.tvDoctorName.setText(item.getDoctorName());
        holder.tvLastMessage.setText(item.getLastMessage());
        holder.tvAvatar.setText(item.getEmoji() != null ? item.getEmoji() : "👩‍⚕️");
        if ("active".equals(item.getStatus())) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setBackgroundResource(R.drawable.badge_active);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setBackgroundResource(R.drawable.badge_completed);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.completed_green));
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvDoctorName, tvLastMessage, tvStatus;
        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}