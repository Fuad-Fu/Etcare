package com.et.etcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.et.etcare.R;
import com.et.etcare.fragments.Article;

public class ArticleDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        Article article = null;
        if (getArguments() != null) {
            article = getArguments().getParcelable("article");
        }

        if (article != null) {
            TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
            tvTitle.setText(article.getTitle());

            TextView tvEmoji = view.findViewById(R.id.tvDetailEmoji);
            tvEmoji.setText(article.getEmoji() != null ? article.getEmoji() : "📄");

            TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
            tvCategory.setText(article.getCategory());

            TextView tvReadTime = view.findViewById(R.id.tvDetailReadTime);
            tvReadTime.setText(article.getReadTimeMinutes() + " min read");

            TextView tvBody = view.findViewById(R.id.tvDetailBody);
            tvBody.setText(article.getBody());
        }

        return view;
    }
}