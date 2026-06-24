package com.et.etcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.SlotAdapter;
import com.et.etcare.fragments.Doctor;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.FlexWrap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorDetailFragment extends Fragment {

    private Doctor doctor;
    private RecyclerView rvSlots;
    private Button btnConfirm;
    private SlotAdapter slotAdapter;
    private String selectedSlot = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_detail, container, false);

        // Get doctor from arguments
        if (getArguments() != null) {
            doctor = getArguments().getParcelable("doctor");
        }
        if (doctor == null) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return view;
        }

        // Back button
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Doctor profile
        TextView tvProfileEmoji = view.findViewById(R.id.tvProfileEmoji);
        tvProfileEmoji.setText(doctor.getEmoji() != null ? doctor.getEmoji() : "👩‍⚕️");

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileName.setText(doctor.getName());

        TextView tvProfileSpecialty = view.findViewById(R.id.tvProfileSpecialty);
        tvProfileSpecialty.setText(doctor.getSpecialty());

        LinearLayout languagesContainer = view.findViewById(R.id.languagesContainer);
        if (doctor.getLanguages() != null) {
            for (String lang : doctor.getLanguages()) {
                TextView tag = new TextView(getContext());
                tag.setText(lang);
                tag.setTextSize(11);
                tag.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
                tag.setBackgroundResource(R.drawable.lang_tag_bg);
                tag.setPadding(8, 4, 8, 4);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 8, 0);
                tag.setLayoutParams(params);
                languagesContainer.addView(tag);
            }
        }

        TextView tvBio = view.findViewById(R.id.tvBio);
        if (doctor.getBio() != null && !doctor.getBio().isEmpty()) {
            tvBio.setText(doctor.getBio());
        } else {
            tvBio.setVisibility(View.GONE);
        }

        // Time slots with Flexbox
        rvSlots = view.findViewById(R.id.rvSlots);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        rvSlots.setLayoutManager(layoutManager);

        List<String> slotTimes = doctor.getAvailableSlots();

        if (slotTimes == null || slotTimes.isEmpty()) {
            slotTimes = Arrays.asList("Today, 3:30 PM", "Tomorrow, 9:00 AM");
        }

        slotAdapter = new SlotAdapter(selected -> {
            selectedSlot = selected;
            btnConfirm.setEnabled(true);
            btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.purple_primary));
        });
        slotAdapter.setSlots(slotTimes);
        rvSlots.setAdapter(slotAdapter);

        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            if (selectedSlot == null) {
                Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Booking...");

            Map<String, Object> appointment = new HashMap<>();
            appointment.put("doctorName", doctor.getName());
            appointment.put("doctorEmoji", doctor.getEmoji());
            appointment.put("doctorId", doctor.getFirestoreId()); // Save doctorId for easier cancellation
            appointment.put("patientId", currentUser.getUid());
            appointment.put("patientName", currentUser.getDisplayName());
            appointment.put("selectedSlot", selectedSlot);
            appointment.put("status", "upcoming");
            appointment.put("createdAt", System.currentTimeMillis());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("appointments")
                    .add(appointment)
                    .addOnSuccessListener(ref -> {
                        // Remove the slot from the doctor's availableSlots
                        db.collection("doctors")
                                .document(doctor.getFirestoreId())
                                .update("availableSlots", FieldValue.arrayRemove(selectedSlot));
                        Toast.makeText(getContext(), "Booked with " + doctor.getName(), Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Confirm Booking");
                        Toast.makeText(getContext(), "Booking failed", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }
}