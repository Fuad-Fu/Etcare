package com.et.etcare.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.et.etcare.R;
import com.et.etcare.fragments.ArticleAdapter;
import com.et.etcare.fragments.Article;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ArticleListFragment extends Fragment {

    private RecyclerView rvArticles;
    private EditText etSearch;
    private ArticleAdapter adapter;
    private List<Article> allArticles = new ArrayList<>();
    private List<Article> filteredArticles = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_list, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        rvArticles = view.findViewById(R.id.rvArticles);
        etSearch = view.findViewById(R.id.etSearch);

        adapter = new ArticleAdapter(article -> {
            ArticleDetailFragment detailFragment = new ArticleDetailFragment();
            Bundle args = new Bundle();
            args.putParcelable("article", article);
            detailFragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvArticles.setLayoutManager(new LinearLayoutManager(getContext()));
        rvArticles.setAdapter(adapter);

        loadArticlesFromFirestore();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterArticles(s.toString().trim().toLowerCase());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    // Returns the correct Firestore collection name for articles based on language
    private String getArticlesCollection() {
        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");
        if ("am".equals(lang)) return "articles_am";
        if ("om".equals(lang)) return "articles_om";
        return "articles";   // or "articles" if your English collection is named that
    }

    private void loadArticlesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getArticlesCollection())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        int id = doc.getId().hashCode();
                        String title = doc.getString("title");
                        String snippet = doc.getString("snippet");
                        String body = doc.getString("body");
                        String category = doc.getString("category");
                        String emoji = doc.getString("emoji");
                        int readTime = doc.getLong("readTimeMinutes") != null
                                ? doc.getLong("readTimeMinutes").intValue() : 5;

                        Article article = new Article(id, title, snippet, body, category, emoji, readTime);
                        allArticles.add(article);
                    }
                    filterArticles(etSearch.getText().toString().trim().toLowerCase());
                });
    }

    private void filterArticles(String query) {
        filteredArticles.clear();
        if (query.isEmpty()) {
            filteredArticles.addAll(allArticles);
        } else {
            for (Article article : allArticles) {
                if (article.getTitle().toLowerCase().contains(query) ||
                        article.getCategory().toLowerCase().contains(query)) {
                    filteredArticles.add(article);
                }
            }
        }
        adapter.updateList(filteredArticles);
    }
}