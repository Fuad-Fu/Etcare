package com.et.etcare.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.et.etcare.fragments.Article;
import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> articles = new ArrayList<>();
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    public ArticleAdapter(OnArticleClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Article> newList) {
        this.articles = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.tvTitle.setText(article.getTitle());
        holder.tvSnippet.setText(article.getSnippet());
        holder.tvMeta.setText(article.getReadTimeMinutes() + " min read • " + article.getCategory());
        holder.tvEmoji.setText(article.getEmoji() != null ? article.getEmoji() : "📄");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onArticleClick(article);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvTitle, tvSnippet, tvMeta;
        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvArticleEmoji);
            tvTitle = itemView.findViewById(R.id.tvArticleTitle);
            tvSnippet = itemView.findViewById(R.id.tvArticleSnippet);
            tvMeta = itemView.findViewById(R.id.tvArticleMeta);
        }
    }
}