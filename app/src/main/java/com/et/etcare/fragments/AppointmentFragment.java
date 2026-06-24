package com.et.etcare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentFragment extends Fragment {

    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> upcomingList = new ArrayList<>();
    private List<Appointment> activeList = new ArrayList<>();
    private List<Appointment> completedList = new ArrayList<>();

    private TextView tabUpcoming, tabActive, tabCompleted;
    private View indicator;
    private ProgressBar pbLoading;
    private TextView tvOfflineIndicator;

    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;

    private static final int TAB_UPCOMING = 0;
    private static final int TAB_ACTIVE = 1;
    private static final int TAB_COMPLETED = 2;
    private int currentTab = TAB_UPCOMING;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        rvAppointments = view.findViewById(R.id.rvAppointments);
        tabUpcoming = view.findViewById(R.id.tabUpcoming);
        tabActive = view.findViewById(R.id.tabActive);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        indicator = view.findViewById(R.id.indicator);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvOfflineIndicator = view.findViewById(R.id.tvOfflineIndicator);

        // Set up adapter before loading data to avoid null adapter
        adapter = new AppointmentAdapter(new AppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onChatClick(Appointment appointment) {
                openChatDetail(appointment);
            }

            @Override
            public void onItemClick(Appointment appointment) {
                // Handle item click if needed
            }

            @Override
            public void onCancelClick(Appointment appointment) {
                showCancelConfirmation(appointment);
            }
        });

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppointments.setAdapter(adapter);

        // Load appointments from Firestore
        loadAppointmentsFromFirestore();

        tabUpcoming.setOnClickListener(v -> switchTab(TAB_UPCOMING));
        tabActive.setOnClickListener(v -> switchTab(TAB_ACTIVE));
        tabCompleted.setOnClickListener(v -> switchTab(TAB_COMPLETED));

        switchTab(TAB_UPCOMING);
        startPeriodicRefresh();

        return view;
    }

    private void showCancelConfirmation(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel your appointment with " + appointment.getDoctorName() + "?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelAppointment(appointment))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment(Appointment appointment) {
        if (appointment.getFirestoreId() == null) return;

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // 1. Update appointment status to "cancelled"
        db.collection("appointments")
                .document(appointment.getFirestoreId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    // 2. Add the slot back to the doctor's available slots
                    // We need the doctorId. If we don't have it, we can query by name (less ideal) 
                    // but for this implementation we'll try to find the doctor doc if doctorId was saved.
                    
                    if (appointment.getDoctorId() != null) {
                        db.collection("doctors")
                                .document(appointment.getDoctorId())
                                .update("availableSlots", FieldValue.arrayUnion(appointment.getDateTimeDisplay()));
                    } else {
                        // Fallback: search doctor by name to add slot back
                        db.collection("doctors")
                                .whereEqualTo("name", appointment.getDoctorName())
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        querySnapshot.getDocuments().get(0).getReference()
                                                .update("availableSlots", FieldValue.arrayUnion(appointment.getDateTimeDisplay()));
                                    }
                                });
                    }

                    Toast.makeText(getContext(), "Appointment cancelled", Toast.LENGTH_SHORT).show();
                    loadAppointmentsFromFirestore(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAppointmentsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        if (pbLoading != null && !pbLoading.isShown()) pbLoading.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .whereEqualTo("patientId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    
                    if (tvOfflineIndicator != null) {
                        if (queryDocumentSnapshots.getMetadata().isFromCache()) {
                            tvOfflineIndicator.setVisibility(View.VISIBLE);
                        } else {
                            tvOfflineIndicator.setVisibility(View.GONE);
                        }
                    }

                    allAppointments.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String doctorName = doc.getString("doctorName");
                        String doctorEmoji = doc.getString("doctorEmoji");
                        String slot = doc.getString("selectedSlot");
                        String status = doc.getString("status");
                        String doctorId = doc.getString("doctorId");

                        long slotTimestamp = parseSlotToMillis(slot);

                        Appointment app = new Appointment(
                                doc.getId().hashCode(),
                                doctorName,
                                slot,            // display text
                                slotTimestamp,   // REAL appointment time
                                "Chat",
                                status != null ? status : "upcoming",
                                doctorEmoji
                        );
                        app.setFirestoreId(doc.getId());
                        app.setDoctorId(doctorId);
                        allAppointments.add(app);
                    }
                    classifyAppointments();
                    switchTab(currentTab);   // refresh the current tab
                })
                .addOnFailureListener(e -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    if (tvOfflineIndicator != null) tvOfflineIndicator.setVisibility(View.VISIBLE);
                });
    }
    private long parseSlotToMillis(String slot) {
        if (slot == null) {
            Log.w("Appointment", "parseSlotToMillis: slot is null");
            return System.currentTimeMillis() + 3600000;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, h:mm a", Locale.ENGLISH);
            // Use today's year to complete the date
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(slot));
            cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            long result = cal.getTimeInMillis();
            return result;
        } catch (Exception e) {
            return System.currentTimeMillis() + 3600000;   // fallback
        }
    }

    private void classifyAppointments() {
        long now = System.currentTimeMillis();
        upcomingList.clear();
        activeList.clear();
        completedList.clear();

        for (Appointment app : allAppointments) {
            if ("cancelled".equals(app.getStatus())) {
                // For now, we don't show cancelled ones or we could put them in completed
                continue;
            }
            
            if ("completed".equals(app.getStatus())) {
                completedList.add(app);
                continue;
            }

            // If the scheduled time has passed
            if (app.getDateTimeMillis() <= now) {
                // Mark it as active locally (so the adapter shows the Chat button)
                if (!"active".equals(app.getStatus())) {
                    app.setStatus("active");
                    updateAppointmentStatusInFirestore(app, "active");
                }
                activeList.add(app);
            } else {
                // Future appointment -> upcoming
                upcomingList.add(app);
            }
        }
    }

    /**
     * Updates the status of an appointment in Firestore.
     */
    private void updateAppointmentStatusInFirestore(Appointment appointment, String newStatus) {
        if (appointment.getFirestoreId() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .document(appointment.getFirestoreId())
                .update("status", newStatus);
    }

    private void switchTab(int tab) {
        currentTab = tab;
        // Ensure classification is up‑to‑date before showing a tab
        classifyAppointments();

        // Reset all tab colors
        tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_inactive));
        tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_inactive));
        tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_inactive));

        View targetTab = null;
        List<Appointment> targetList = null;

        switch (tab) {
            case TAB_UPCOMING:
                targetTab = tabUpcoming;
                targetList = upcomingList;
                tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
                break;
            case TAB_ACTIVE:
                targetTab = tabActive;
                targetList = activeList;
                tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
                break;
            case TAB_COMPLETED:
                targetTab = tabCompleted;
                targetList = completedList;
                tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
                break;
        }

        adapter.updateList(targetList);

        // Move the indicator
        if (targetTab != null) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) indicator.getLayoutParams();
            params.startToStart = targetTab.getId();
            params.endToEnd = targetTab.getId();
            indicator.setLayoutParams(params);
        }
    }

    private void startPeriodicRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadAppointmentsFromFirestore(); // re‑fetch and re‑classify
                refreshHandler.postDelayed(this, 30_000); // every 30 seconds
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 30_000);
    }

    private void openChatDetail(Appointment appointment) {
        ChatDetailFragment fragment = new ChatDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("appointment", appointment);
        fragment.setArguments(args);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}