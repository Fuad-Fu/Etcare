package com.et.etcare.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.Appointment;
import java.util.List;

public class PastAppointmentAdapter extends RecyclerView.Adapter<PastAppointmentAdapter.ViewHolder> {

    private List<Appointment> pastAppointments;

    public PastAppointmentAdapter(List<Appointment> pastAppointments) {
        this.pastAppointments = pastAppointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment app = pastAppointments.get(position);
        holder.tvDoctorName.setText(app.getDoctorName());
        holder.tvDate.setText(app.getDateTimeDisplay());
        holder.tvAvatar.setText(app.getEmoji());
    }

    @Override
    public int getItemCount() {
        return pastAppointments != null ? pastAppointments.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvDoctorName, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvPastAvatar);
            tvDoctorName = itemView.findViewById(R.id.tvPastDoctorName);
            tvDate = itemView.findViewById(R.id.tvPastDate);
        }
    }
}