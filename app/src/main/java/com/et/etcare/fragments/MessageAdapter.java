package com.et.etcare.fragments;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.tvMessage.setText(msg.getText());
        
        // Use the display timestamp (set locally on send or from server later)
        String time = msg.getTimestampDisplay();
        holder.tvTimestamp.setText(time != null ? time : "");

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.bubbleWrapper.getLayoutParams();
        if (msg.getSenderType() == Message.TYPE_PATIENT) {
            // Patient: right aligned, purple bubble
            params.gravity = Gravity.END;
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_patient);
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            // Doctor: left aligned, light purple bubble
            params.gravity = Gravity.START;
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_doctor);
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
        }
        holder.bubbleWrapper.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;
        LinearLayout bubbleWrapper;
        ViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            bubbleWrapper = itemView.findViewById(R.id.bubbleWrapper);
        }
    }
}