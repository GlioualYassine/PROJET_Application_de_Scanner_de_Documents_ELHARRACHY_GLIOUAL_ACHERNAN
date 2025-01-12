package ensa.application01.projetocr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.R;
import ensa.application01.projetocr.models.Category;

public class MainCategoryAdapter extends RecyclerView.Adapter<MainCategoryAdapter.ViewHolder> {
    private final List<Category> categories; // Liste principale des catégories
    private final Context context;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onShowMoreClick();
    }

    public MainCategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>(categories);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Afficher une catégorie
        if (position < categories.size()) {
            Category category = categories.get(position);
            holder.categoryName.setText(category.getName());
            holder.cardView.setVisibility(View.VISIBLE); // Rendre visible
            holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        } else {
            holder.cardView.setVisibility(View.GONE); // Masquer les cases inutilisées
        }
    }

    @Override
    public int getItemCount() {
        return categories.size(); // Retourne la taille réelle de la liste
    }

    // Méthode existante pour mettre à jour toutes les catégories (utilisée dans un cadre spécial)
    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    // Nouvelle méthode pour mettre à jour uniquement les 4 premières catégories
    public void updateTopCategories(List<Category> topCategories) {

        this.categories.clear();
        this.categories.addAll(topCategories.subList(0, Math.min(topCategories.size(), 4))); // Limiter aux 4 premières
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            cardView = (CardView) itemView;
        }
    }
}
