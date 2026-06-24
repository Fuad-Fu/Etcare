package com.et.etcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.DoctorAdapter;
import com.et.etcare.fragments.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoctorListFragment extends Fragment {

    private RecyclerView rvDoctors;
    private EditText etSearch;
    private DoctorAdapter adapter;
    private List<Doctor> allDoctors = new ArrayList<>();
    private ProgressBar pbLoading;
    private TextView tvOfflineIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_list, container, false);

        // Back button
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Go back to Home (pop this fragment)
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        rvDoctors = view.findViewById(R.id.rvDoctors);
        etSearch = view.findViewById(R.id.etSearch);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvOfflineIndicator = view.findViewById(R.id.tvOfflineIndicator);

        loadDoctorsFromFirestore();

        adapter = new DoctorAdapter(new DoctorAdapter.OnDoctorActionListener() {
            @Override
            public void onBookClick(Doctor doctor) {
                openDoctorDetail(doctor);
            }

            @Override
            public void onItemClick(Doctor doctor) {
                openDoctorDetail(doctor);
            }
        });

        rvDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDoctors.setAdapter(adapter);
        adapter.updateList(allDoctors);

        // Simple search filter (by name or specialty)
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        return view;
    }

    private void loadDoctorsFromFirestore() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("doctors")
                .whereEqualTo("isActive", true)
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

                    allDoctors.clear();
                    int counter = 1;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String specialty = document.getString("specialty");
                        List<String> languages = (List<String>) document.get("languages");
                        String emoji = document.getString("emoji");
                        double rating = document.getDouble("rating") != null ? document.getDouble("rating") : 0.0;
                        int reviewCount = document.getLong("reviewCount") != null ? document.getLong("reviewCount").intValue() : 0;
                        List<String> availableSlots = (List<String>) document.get("availableSlots");
                        String bio = document.getString("bio");

                        Doctor doctor = new Doctor(
                                counter,
                                name,
                                specialty,
                                languages != null ? languages : new ArrayList<>(),
                                emoji,
                                rating,
                                reviewCount,
                                availableSlots != null ? availableSlots : new ArrayList<>(),
                                bio != null ? bio : ""
                        );
                        doctor.setFirestoreId(document.getId());
                        allDoctors.add(doctor);
                        counter++;
                    }
                    adapter.updateList(allDoctors);
                })
                .addOnFailureListener(e -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    if (tvOfflineIndicator != null) tvOfflineIndicator.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Failed to load doctors", Toast.LENGTH_SHORT).show();
                });
    }

    private void openDoctorDetail(Doctor doctor) {
        DoctorDetailFragment fragment = new DoctorDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("doctor", doctor);
        fragment.setArguments(args);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void filterDoctors(String query) {
        if (query.trim().isEmpty()) {
            adapter.updateList(allDoctors);
            return;
        }
        List<Doctor> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Doctor d : allDoctors) {
            if (d.getName().toLowerCase().contains(lowerQuery) ||
                    d.getSpecialty().toLowerCase().contains(lowerQuery)) {
                filtered.add(d);
            }
        }
        adapter.updateList(filtered);
    }
}