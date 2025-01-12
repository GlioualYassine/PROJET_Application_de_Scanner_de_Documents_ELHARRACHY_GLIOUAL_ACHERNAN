package ensa.application01.projetocr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ensa.application01.projetocr.adapters.CategoryAdapter;
import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;

public class CategoryManagementActivity extends AppCompatActivity {

    private CategoryService categoryService;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // Initialisation du service des catégories
        categoryService = new CategoryService(this);

        // Récupérer la liste des catégories
        List<Category> categories = categoryService.getCategories();

        // Configurer le RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Utiliser l'adaptateur
        categoryAdapter = new CategoryAdapter(categories, this);
        recyclerView.setAdapter(categoryAdapter);

        // Bouton pour ajouter une catégorie
        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        btnAddCategory.setOnClickListener(v -> addCategory());
    }

    private void addCategory() {
        // Ajouter une nouvelle catégorie (exemple)
        int newId = categoryAdapter.getItemCount() + 1;
        Category newCategory = new Category(newId, "Nouvelle Catégorie", null);
        categoryService.addCategory(newCategory);
        categoryAdapter.addCategory(newCategory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        categoryAdapter.notifyDataSetChanged(); // Notifie des modifications à l'adaptateur
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            int categoryId = data.getIntExtra("categoryId", -1);
            String newName = data.getStringExtra("categoryName");

            if (categoryId != -1 && newName != null) {
                // Mise à jour dans la liste locale
                for (Category category : categoryService.getCategories()) {
                    if (category.getId() == categoryId) {
                        category.setName(newName);
                        break;
                    }
                }

                // Rafraîchir l'adaptateur
                categoryAdapter.updateCategories(categoryService.getCategories());

                // Renvoyer le résultat à MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isUpdated", true); // Indiquer que des modifications ont été faites
                setResult(RESULT_OK, resultIntent);
            }
        }
    }

}
