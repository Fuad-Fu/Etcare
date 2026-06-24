package com.et.etcare.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import java.util.ArrayList;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> {

    private List<String> slots = new ArrayList<>();
    private int selectedPosition = -1;
    private OnSlotSelectedListener listener;

    public interface OnSlotSelectedListener {
        void onSlotSelected(String slot);
    }

    public SlotAdapter(OnSlotSelectedListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<String> slots) {
        this.slots = slots;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slot_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String slot = slots.get(position);
        holder.tvSlot.setText(slot);
        if (position == selectedPosition) {
            holder.tvSlot.setBackgroundResource(R.drawable.slot_chip_bg_selected);
            holder.tvSlot.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.tvSlot.setBackgroundResource(R.drawable.slot_chip_bg);
            holder.tvSlot.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
        }
        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (previous != -1) notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSlotSelected(slots.get(selectedPosition));
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlot;
        ViewHolder(View itemView) {
            super(itemView);
            tvSlot = itemView.findViewById(R.id.tvSlot);
        }
    }
}