package com.et.etcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.ChatListAdapter;
import com.et.etcare.fragments.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChatList;
    private LinearLayout emptyState;
    private TextView tvEmptyMessage;
    private Button btnFindDoctor;
    private TextView tabActive, tabCompleted;
    private View indicator;
    private ProgressBar pbLoading;

    private ChatListAdapter adapter;
    private List<ChatListItem> activeChatItems = new ArrayList<>();
    private List<ChatListItem> completedChatItems = new ArrayList<>();
    
    // Maintain lists of Appointment objects to pass to detail view
    private List<Appointment> activeAppointments = new ArrayList<>();
    private List<Appointment> completedAppointments = new ArrayList<>();
    
    private int currentTab = 0; // 0 = active, 1 = completed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        rvChatList = view.findViewById(R.id.rvChatList);
        emptyState = view.findViewById(R.id.emptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        btnFindDoctor = view.findViewById(R.id.btnFindDoctor);
        tabActive = view.findViewById(R.id.tabActive);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        indicator = view.findViewById(R.id.indicator);
        pbLoading = view.findViewById(R.id.pbLoading);

        adapter = new ChatListAdapter(item -> {
            openChatDetail(item.getAppointmentId());
        });
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatList.setAdapter(adapter);

        tabActive.setOnClickListener(v -> switchTab(0));
        tabCompleted.setOnClickListener(v -> switchTab(1));

        btnFindDoctor.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new DoctorListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        loadChatsFromFirestore();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatsFromFirestore();
    }

    private void loadChatsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        // Don't hide the list immediately to avoid flicker if cache is fast
        // rvChatList.setVisibility(View.GONE); 
        emptyState.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .whereEqualTo("patientId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    
                    activeChatItems.clear();
                    completedChatItems.clear();
                    activeAppointments.clear();
                    completedAppointments.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String doctorName = doc.getString("doctorName");
                        String doctorEmoji = doc.getString("doctorEmoji");
                        String status = doc.getString("status");
                        String slot = doc.getString("selectedSlot");
                        int id = doc.getId().hashCode();

                        Appointment app = new Appointment(
                                id,
                                doctorName,
                                slot,
                                0, // We don't necessarily need the millis here for chat list
                                "Chat",
                                status,
                                doctorEmoji
                        );
                        app.setFirestoreId(doc.getId());

                        String lastMsg = "Tap to start consultation...";
                        String time = "Now";

                        ChatListItem item = new ChatListItem(id, doctorName, doctorEmoji, lastMsg, time, status);

                        if ("active".equalsIgnoreCase(status)) {
                            activeChatItems.add(item);
                            activeAppointments.add(app);
                        } else if ("completed".equalsIgnoreCase(status)) {
                            completedChatItems.add(item);
                            completedAppointments.add(app);
                        }
                    }
                    
                    switchTab(currentTab);
                })
                .addOnFailureListener(e -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
                    switchTab(currentTab);
                });
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;
        if (tabIndex == 0) {
            tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
            tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_inactive));
            moveIndicator(tabActive);
        } else {
            tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
            tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_inactive));
            moveIndicator(tabCompleted);
        }

        List<ChatListItem> listToShow = (tabIndex == 0) ? activeChatItems : completedChatItems;
        if (listToShow.isEmpty()) {
            rvChatList.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(tabIndex == 0 ? "No active consultations" : "No completed consultations");
        } else {
            rvChatList.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.updateList(listToShow);
        }
    }

    private void moveIndicator(View targetTab) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        params.startToStart = targetTab.getId();
        params.endToEnd = targetTab.getId();
        indicator.setLayoutParams(params);
    }

    private void openChatDetail(int appointmentId) {
        Appointment appointment = null;
        
        // Search in our local lists
        for (Appointment a : activeAppointments) {
            if (a.getId() == appointmentId) {
                appointment = a;
                break;
            }
        }
        if (appointment == null) {
            for (Appointment a : completedAppointments) {
                if (a.getId() == appointmentId) {
                    appointment = a;
                    break;
                }
            }
        }

        if (appointment != null) {
            ChatDetailFragment fragment = new ChatDetailFragment();
            Bundle args = new Bundle();
            args.putParcelable("appointment", appointment);
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(getContext(), "Chat details unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}