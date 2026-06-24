package com.et.etcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.ConversationItem;
import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<ConversationItem> conversations = new ArrayList<>();
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(ConversationItem item);
    }

    public ConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<ConversationItem> newList) {
        this.conversations = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationItem item = conversations.get(position);
        holder.tvDoctorName.setText(item.getDoctorName());
        holder.tvLastMessage.setText(item.getLastMessage());
        holder.tvAvatar.setText(item.getEmoji() != null ? item.getEmoji() : "👩‍⚕️");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConversationClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvDoctorName, tvLastMessage;

        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}