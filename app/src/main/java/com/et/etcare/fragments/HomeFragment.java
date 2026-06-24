package com.et.etcare.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.et.etcare.MainActivity;
import com.et.etcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private Article featuredArticle;
    private TextView tvArticleTitle, tvGreeting, tvOfflineIndicator;
    private ImageView ivArticlePhoto;
    private CardView articleCard;
    private ProgressBar pbHome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvOfflineIndicator = view.findViewById(R.id.tvOfflineIndicator);
        pbHome = view.findViewById(R.id.pbHome);
        tvArticleTitle = view.findViewById(R.id.tvArticleTitle);
        ivArticlePhoto = view.findViewById(R.id.ivArticlePhoto);
        articleCard = view.findViewById(R.id.articleCard);

        setupUserGreeting();

        view.findViewById(R.id.profileIcon).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectProfileTab();
            }
        });

        loadDailyTip(view.findViewById(R.id.tvHeroTitle), view.findViewById(R.id.tvHeroDescription));
        loadFeaturedArticle();
        checkConnectivity();

        articleCard.setOnClickListener(v -> {
            if (featuredArticle != null) {
                ArticleDetailFragment detailFragment = new ArticleDetailFragment();
                Bundle args = new Bundle();
                args.putParcelable("article", featuredArticle);
                detailFragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                navigateTo(new ArticleListFragment());
            }
        });

        return view;
    }

    private void setupUserGreeting() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = currentUser.getEmail();
                name = (email != null) ? email.split("@")[0] : "User";
            } else {
                name = name.split("\\s+")[0];
            }
            String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            tvGreeting.setText("Hello, " + capitalized);
        } else {
            tvGreeting.setText("Hello");
        }
    }

    private void checkConnectivity() {
        if (getContext() == null) return;
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (tvOfflineIndicator != null) {
            tvOfflineIndicator.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        }
    }

    // Returns the correct Firestore collection name for articles based on language
    private String getArticlesCollection() {
        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");
        if ("am".equals(lang)) return "articles_am";
        if ("om".equals(lang)) return "articles_om";
        return "articles";   // or return "articles" if your English collection is named "articles"
    }

    private void loadFeaturedArticle() {
        if (pbHome != null) pbHome.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getArticlesCollection())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (pbHome != null) pbHome.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                        int index = dayOfYear % queryDocumentSnapshots.size();
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(index);

                        featuredArticle = new Article(
                                doc.getId().hashCode(),
                                doc.getString("title"),
                                doc.getString("snippet"),
                                doc.getString("body"),
                                doc.getString("category"),
                                doc.getString("emoji"),
                                doc.getLong("readTimeMinutes") != null ? doc.getLong("readTimeMinutes").intValue() : 5
                        );
                        tvArticleTitle.setText(featuredArticle.getTitle());
                        ivArticlePhoto.setImageResource(R.drawable.placeholder);
                    }
                })
                .addOnFailureListener(e -> {
                    if (pbHome != null) pbHome.setVisibility(View.GONE);
                    checkConnectivity();
                });
    }

    private void loadDailyTip(TextView titleView, TextView descView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("wellness_tips")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                        int index = dayOfYear % queryDocumentSnapshots.size();
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(index);
                        titleView.setText(doc.getString("title"));
                        descView.setText(doc.getString("description"));
                    }
                })
                .addOnFailureListener(e -> {
                    titleView.setText("Daily Wellness");
                    descView.setText("Take a moment for yourself today.");
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.chipConsult).setOnClickListener(v -> navigateTo(new DoctorListFragment()));
        view.findViewById(R.id.chipWellnessPlan).setOnClickListener(v -> navigateTo(new WellnessPlanFragment()));
        view.findViewById(R.id.chipLibrary).setOnClickListener(v -> navigateTo(new ArticleListFragment()));
        view.findViewById(R.id.tvViewAllArticles).setOnClickListener(v -> navigateTo(new ArticleListFragment()));
        view.findViewById(R.id.readMore).setOnClickListener(v -> {
            if (featuredArticle != null) {
                ArticleDetailFragment detailFragment = new ArticleDetailFragment();
                Bundle args = new Bundle();
                args.putParcelable("article", featuredArticle);
                detailFragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                navigateTo(new ArticleListFragment());
            }
        });
    }

    private void navigateTo(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}