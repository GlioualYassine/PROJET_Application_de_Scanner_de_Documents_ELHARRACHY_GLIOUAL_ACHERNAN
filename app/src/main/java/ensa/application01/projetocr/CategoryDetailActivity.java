package ensa.application01.projetocr;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.adapters.CategoryPhotoAdapter;
import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;

public class CategoryDetailActivity extends AppCompatActivity {

    private CategoryService categoryService;
    private CategoryPhotoAdapter photoAdapter;
    private TextView categoryNameTitle;
    private Category currentCategory;
    private List<String> validPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Initialiser les vues
        categoryNameTitle = findViewById(R.id.categoryNameTitle);
        RecyclerView photosRecyclerView = findViewById(R.id.photosRecyclerView);

        // Configurer le RecyclerView
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photoAdapter = new CategoryPhotoAdapter(this);
        photosRecyclerView.setAdapter(photoAdapter);

        // Récupérer l'ID de la catégorie depuis l'intent
        int categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId != -1) {
            loadCategory(categoryId);
        } else {
            Toast.makeText(this, "Erreur : catégorie non trouvée", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCategory(int categoryId) {
        categoryService = CategoryService.getInstance(this);
        currentCategory = categoryService.getCategoryById(categoryId);

        if (currentCategory != null) {
            // Afficher le nom de la catégorie
            categoryNameTitle.setText(currentCategory.getName());

            // Charger les photos associées à la catégorie
            List<String> photos = categoryService.getCategoryPhotos(categoryId);
            validPhotos = new ArrayList<>();

            for (String photoUri : photos) {
                if (isUriAccessible(photoUri)) {
                    validPhotos.add(photoUri);
                }
            }

            if (validPhotos.isEmpty()) {
                Toast.makeText(this, "Aucune image disponible pour cette catégorie.", Toast.LENGTH_SHORT).show();
            }

            // Mettre à jour l'adaptateur avec les photos valides
            photoAdapter.setPhotos(validPhotos);
        } else {
            Toast.makeText(this, "Erreur : catégorie introuvable", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isUriAccessible(String uriString) {
        try {
            Uri uri = Uri.parse(uriString);
            getContentResolver().openInputStream(uri).close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
