package ensa.application01.projetocr;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;
import ensa.application01.projetocr.adapters.ImageAdapter;

public class CategoryDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Initialiser les vues
        TextView categoryNameTitle = findViewById(R.id.categoryNameTitle);
        RecyclerView photosRecyclerView = findViewById(R.id.photosRecyclerView);

        // Configurer le RecyclerView
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 colonnes

        // Initialiser le service des catégories
        CategoryService categoryService = CategoryService.getInstance(this);

        // Récupérer l'ID de la catégorie depuis l'intent
        int categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId == -1) {
            Toast.makeText(this, "Erreur : ID de catégorie invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Charger la catégorie
        Category currentCategory = categoryService.getCategoryById(categoryId);
        if (currentCategory == null) {
            Toast.makeText(this, "Erreur : Catégorie introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Afficher le nom de la catégorie
        categoryNameTitle.setText(currentCategory.getName());

        // Charger les images
        List<String> images = new ArrayList<>(currentCategory.getImages());
        // Action pour supprimer une image si nécessaire
        ImageAdapter imageAdapter = new ImageAdapter(images, imagePath -> {
            // Action pour supprimer une image si nécessaire
        });
        photosRecyclerView.setAdapter(imageAdapter);

        // Vérifier si aucune image n'est disponible
        if (images.isEmpty()) {
            Toast.makeText(this, "Aucune image disponible pour cette catégorie.", Toast.LENGTH_SHORT).show();
        }
    }

}
