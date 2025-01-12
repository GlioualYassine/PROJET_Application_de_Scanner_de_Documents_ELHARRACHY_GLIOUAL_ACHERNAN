package ensa.application01.projetocr.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ensa.application01.projetocr.CategoryDetailActivity;
import ensa.application01.projetocr.EditCategoryActivity;
import ensa.application01.projetocr.R;
import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;
    private final Context context;
    private  final CategoryService categoryService;
    public CategoryAdapter(List<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
        categoryService = CategoryService.getInstance(context);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_line, parent, false);
        return new CategoryViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getName());

        // Écouteur de clic sur la catégorie
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CategoryDetailActivity.class);
            intent.putExtra("categoryId", category.getId());
            context.startActivity(intent);
        });

        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.menuButton);
            popupMenu.inflate(R.menu.card_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit) {
                    // Naviguer vers la page d'édition
                    Intent intent = new Intent(context, EditCategoryActivity.class);
                    intent.putExtra("categoryId", category.getId());
                    context.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.delete) {
                    // Supprimer la catégorie
                    categoryService.deleteCategory(categories.get(position).getId());
                    deleteCategory(position);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }


    public void updateTopCategories(List<Category> topCategories) {
        this.categories.clear();
        this.categories.addAll(topCategories.subList(0, Math.min(topCategories.size(), 4))); // Limiter aux 4 premières catégories
        notifyDataSetChanged(); // Rafraîchir la vue
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged(); // Rafraîchit la vue
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void addCategory(Category category) {
        categories.add(category);
        notifyItemInserted(categories.size() - 1);
    }

    private void deleteCategory(int position) {
        categories.remove(position);
        notifyItemRemoved(position);
    }

    private void editCategory(Category category) {
        // Logique pour éditer la catégorie (par exemple, ouvrir un dialog)
    }

    public List<Category> getCategories() {
        return categories; // Retourne directement la référence à la liste utilisée
    }


    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView menuButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}
