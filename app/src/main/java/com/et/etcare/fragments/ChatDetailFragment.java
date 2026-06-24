package com.et.etcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatDetailFragment extends Fragment {

    private Appointment appointment;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private MessageAdapter adapter;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration messageListener;
    private CollectionReference messagesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_detail, container, false);

        if (getArguments() != null) {
            appointment = getArguments().getParcelable("appointment");
        }
        
        if (appointment == null || appointment.getFirestoreId() == null) {
            Toast.makeText(getContext(), "Error: Invalid Appointment", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return view;
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        messagesRef = db.collection("appointments")
                .document(appointment.getFirestoreId())
                .collection("messages");

        setupHeader(view);
        setupRecyclerView(view);
        setupInput(view);
        startRealtimeListener();

        return view;
    }

    private void setupHeader(View view) {
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        TextView tvDoctorName = view.findViewById(R.id.tvChatDoctorName);
        tvDoctorName.setText(appointment.getDoctorName());

        view.findViewById(R.id.btnEndConsultation).setOnClickListener(v -> endConsultation());
    }

    private void setupRecyclerView(View view) {
        rvMessages = view.findViewById(R.id.rvMessages);
        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void setupInput(View view) {
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> sendMessage());
        
        // Hide input if consultation is completed
        if ("completed".equalsIgnoreCase(appointment.getStatus())) {
            view.findViewById(R.id.inputArea).setVisibility(View.GONE);
            view.findViewById(R.id.btnEndConsultation).setVisibility(View.GONE);
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";
        String time = getCurrentTime();

        Message message = new Message(text, Message.TYPE_PATIENT, userId, time);
        
        etMessage.setText("");

        messagesRef.add(message)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void startRealtimeListener() {
        messageListener = messagesRef.orderBy("serverTimestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message msg = dc.getDocument().toObject(Message.class);
                                adapter.addMessage(msg);
                                rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                            }
                        }
                    }
                });
    }

    private void endConsultation() {
        db.collection("appointments")
                .document(appointment.getFirestoreId())
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Consultation Ended", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}