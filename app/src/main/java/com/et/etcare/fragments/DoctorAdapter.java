package com.et.etcare.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.Doctor;
import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<Doctor> doctors = new ArrayList<>();
    private OnDoctorActionListener listener;

    public interface OnDoctorActionListener {
        void onBookClick(Doctor doctor);
        void onItemClick(Doctor doctor);
    }

    public DoctorAdapter(OnDoctorActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Doctor> newList) {
        this.doctors = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doc = doctors.get(position);

        holder.tvDoctorName.setText(doc.getName());
        holder.tvSpecialty.setText(doc.getSpecialty());
        holder.tvDoctorEmoji.setText(doc.getEmoji() != null ? doc.getEmoji() : "👩‍⚕️");

        // Add language tags dynamically
        holder.languagesContainer.removeAllViews();
        if (doc.getLanguages() != null) {
            for (String lang : doc.getLanguages()) {
                TextView tag = new TextView(holder.itemView.getContext());
                tag.setText(lang);
                tag.setTextSize(10);
                tag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_primary));
                tag.setBackgroundResource(R.drawable.lang_tag_bg);
                tag.setPadding(8, 2, 8, 2);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 6, 0);
                tag.setLayoutParams(params);
                holder.languagesContainer.addView(tag);
            }
        }

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) listener.onBookClick(doc);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(doc);
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorEmoji, tvDoctorName, tvSpecialty;
        LinearLayout languagesContainer;
        Button btnBook;

        ViewHolder(View itemView) {
            super(itemView);
            tvDoctorEmoji = itemView.findViewById(R.id.tvDoctorEmoji);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            languagesContainer = itemView.findViewById(R.id.languagesContainer);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}