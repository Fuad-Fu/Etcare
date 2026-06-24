package com.et.etcare.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.LoginActivity;
import com.et.etcare.MainActivity;
import com.et.etcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private RecyclerView rvPastAppointments;
    private TextView langEnglish, langAmharic, langOromo;
    private TextView tvUserName, tvUserEmail, tvTotalConsults, tvEmptyHistory, tvViewAllPast, tvHealthScore;
    private ImageView btnLogout;
    private List<Appointment> completedAppointments = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI Elements
        rvPastAppointments = view.findViewById(R.id.rvPastAppointments);
        langEnglish = view.findViewById(R.id.langEnglish);
        langAmharic = view.findViewById(R.id.langAmharic);
        langOromo = view.findViewById(R.id.langOromo);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvTotalConsults = view.findViewById(R.id.tvTotalConsults);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        tvViewAllPast = view.findViewById(R.id.tvViewAllPast);
        tvHealthScore = view.findViewById(R.id.tvHealthScore);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupUserInfo();
        setupLanguageSelection();
        loadCompletedConsultations();

        // Listeners
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }


        return view;
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            if (tvUserName != null) tvUserName.setText(name != null && !name.isEmpty() ? name : getString(R.string.user_name_placeholder));
            if (tvUserEmail != null) tvUserEmail.setText(email != null ? email : getString(R.string.user_email_placeholder));
        }
    }

    private void setupLanguageSelection() {
        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String currentLang = prefs.getString("My_Lang", "en");

        // Update button states based on current preference
        updateLanguageButtons(currentLang);

        langEnglish.setOnClickListener(v -> setLocale("en"));
        langAmharic.setOnClickListener(v -> setLocale("am"));
        langOromo.setOnClickListener(v -> setLocale("om"));
    }

    private void updateLanguageButtons(String lang) {
        // Reset all
        TextView[] langs = {langEnglish, langAmharic, langOromo};
        for (TextView tv : langs) {
            if (tv != null) {
                tv.setBackgroundResource(R.drawable.lang_unselected);
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_inactive));
            }
        }

        // Highlight selected
        TextView selected = langEnglish;
        if ("am".equals(lang)) selected = langAmharic;
        else if ("om".equals(lang)) selected = langOromo;

        if (selected != null) {
            selected.setBackgroundResource(R.drawable.lang_selected);
            selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private void setLocale(String lang) {
        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String currentLang = prefs.getString("My_Lang", "");
        
        if (lang.equals(currentLang)) return;

        // Save preference
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("My_Lang", lang);
        editor.apply();

        // Update Locale
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        // Restart Activity to apply changes
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    private void loadCompletedConsultations() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .whereEqualTo("patientId", user.getUid())
                .whereEqualTo("status", "completed")
                .orderBy("dateTimeMillis", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    completedAppointments.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        completedAppointments.add(new Appointment(
                                doc.getId().hashCode(),
                                doc.getString("doctorName"),
                                doc.getString("selectedSlot"),
                                doc.getLong("dateTimeMillis") != null ? doc.getLong("dateTimeMillis") : 0,
                                "Chat",
                                "completed",
                                doc.getString("doctorEmoji")
                        ));
                    }
                    updateHistoryUI();
                })
                .addOnFailureListener(e -> {
                    loadConsultationsFallback(db, user.getUid());
                });
    }

    private void loadConsultationsFallback(FirebaseFirestore db, String userId) {
        db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .whereEqualTo("status", "completed")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    completedAppointments.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        completedAppointments.add(new Appointment(
                                doc.getId().hashCode(),
                                doc.getString("doctorName"),
                                doc.getString("selectedSlot"),
                                doc.getLong("dateTimeMillis") != null ? doc.getLong("dateTimeMillis") : 0,
                                "Chat",
                                "completed",
                                doc.getString("doctorEmoji")
                        ));
                    }
                    updateHistoryUI();
                });
    }

    private void updateHistoryUI() {
        if (!isAdded()) return;

        if (completedAppointments.isEmpty()) {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.VISIBLE);
            if (rvPastAppointments != null) rvPastAppointments.setVisibility(View.GONE);
            if (tvTotalConsults != null) tvTotalConsults.setText(getString(R.string.zero));
            if (tvHealthScore != null) tvHealthScore.setText(getString(R.string.double_dash));
        } else {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.GONE);
            if (rvPastAppointments != null) {
                rvPastAppointments.setVisibility(View.VISIBLE);
                PastAppointmentAdapter adapter = new PastAppointmentAdapter(completedAppointments);
                rvPastAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
                rvPastAppointments.setAdapter(adapter);
            }
            if (tvTotalConsults != null) tvTotalConsults.setText(String.valueOf(completedAppointments.size()));
            if (tvHealthScore != null) tvHealthScore.setText("92%"); 
        }
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getContext(), getString(R.string.logout), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}