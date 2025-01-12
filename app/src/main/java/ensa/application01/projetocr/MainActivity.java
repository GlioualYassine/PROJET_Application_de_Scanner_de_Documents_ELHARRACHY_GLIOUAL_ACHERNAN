package ensa.application01.projetocr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import ensa.application01.projetocr.adapters.CategoryAdapter;
import ensa.application01.projetocr.adapters.MainCategoryAdapter;
import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;

public class MainActivity extends AppCompatActivity {

    private CategoryService categoryService;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        intent.putExtra("importedImageUri", selectedImageUri.toString());
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser le service des catégories
        categoryService = CategoryService.getInstance(this);


        setupCategoriesRecyclerView();


        // Initialiser les boutons
        ImageView btnCamera = findViewById(R.id.btnCamera);
        ImageView btnImportPicture = findViewById(R.id.btnImportPicture);
        ImageView btnCategories = findViewById(R.id.btnCategories);

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        btnImportPicture.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btnCategories.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            //startActivity(intent);
            startActivityForResult(intent, 2); // Code de requête 2
        });
        Button btnShowAllCategories = findViewById(R.id.btnShowAllCategories);
        btnShowAllCategories.setOnClickListener(v -> {
            // Lancer une activité pour afficher toutes les catégories
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            startActivityForResult(intent, 3); // Code de requête pour afficher toutes les catégories
        });



        // Configurer la navigation en bas
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Accueil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_scan) {
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profil (fonctionnalité à implémenter)", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Récupérer les catégories mises à jour
        List<Category> updatedCategories = categoryService.getCategories();

        // Mettre à jour l'adaptateur avec les 4 premières catégories
        RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
        MainCategoryAdapter adapter = (MainCategoryAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateTopCategories(updatedCategories);
        }
    }

    private void setupCategoriesRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Grille à 2 colonnes

        List<Category> categories = categoryService.getCategories(); // Récupérer toutes les catégories
        MainCategoryAdapter adapter = new MainCategoryAdapter(this, categories,
                new MainCategoryAdapter.OnCategoryClickListener() {
                    @Override
                    public void onCategoryClick(Category category) {
                        // Action lorsque l'utilisateur clique sur une catégorie
                        Intent intent = new Intent(MainActivity.this, CategoryDetailActivity.class);
                        intent.putExtra("categoryId", category.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onShowMoreClick() {
                        // Action pour afficher toutes les catégories
                        Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
                        startActivity(intent);
                    }
                });

        recyclerView.setAdapter(adapter);

        // Mettre à jour les 4 premières catégories
        adapter.updateTopCategories(categories);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3 && resultCode == RESULT_OK) { // Code de requête pour gérer les modifications
            // Récupérer les catégories mises à jour
            List<Category> updatedCategories = categoryService.getCategories();

            // Mettre à jour uniquement les 4 premières catégories
            RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
            MainCategoryAdapter adapter = (MainCategoryAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.updateTopCategories(updatedCategories);
            }
        }
    }


}
