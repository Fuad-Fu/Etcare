package com.et.etcare.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.Appointment;
import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onChatClick(Appointment appointment);
        void onItemClick(Appointment appointment);
        void onCancelClick(Appointment appointment);
    }

    public AppointmentAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment app = appointmentList.get(position);

        holder.tvDoctorName.setText(app.getDoctorName());
        holder.tvDate.setText(app.getDateTimeDisplay());

        if (app.getEmoji() != null && !app.getEmoji().isEmpty()) {
            holder.tvDoctorEmoji.setText(app.getEmoji());
        } else {
            holder.tvDoctorEmoji.setText("👩‍⚕️");
        }

        String status = app.getStatus();
        holder.btnCancel.setVisibility(View.GONE);
        
        switch (status) {
            case "upcoming":
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("Upcoming");
                holder.tvStatus.setBackgroundResource(R.drawable.badge_upcoming);
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_primary));
                holder.btnChat.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;

            case "active":
                holder.tvStatus.setVisibility(View.GONE);
                holder.btnChat.setVisibility(View.VISIBLE);
                holder.btnChat.setEnabled(true);
                break;

            case "completed":
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("Completed");
                holder.tvStatus.setBackgroundResource(R.drawable.badge_completed);
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.completed_green));
                holder.btnChat.setVisibility(View.GONE);
                break;
                
            case "cancelled":
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("Cancelled");
                holder.tvStatus.setBackgroundResource(R.drawable.badge_completed); // Using same shape but maybe different color
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
                holder.btnChat.setVisibility(View.GONE);
                break;
        }

        holder.btnChat.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(app);
        });

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClick(app);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(app);
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDate, tvStatus, tvDoctorEmoji;
        Button btnChat, btnCancel;

        ViewHolder(View itemView) {
            super(itemView);
            tvDoctorEmoji = itemView.findViewById(R.id.tvDoctorProfile);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDate = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}