package ensa.application01.projetocr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import ensa.application01.projetocr.adapters.MainCategoryAdapter;
import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;

/**
 * Classe MainActivity qui gère l'écran principal de l'application.
 * Elle inclut les fonctionnalités suivantes :
 * - Lancement de la caméra pour capturer des images.
 * - Importation d'images depuis la galerie.
 * - Gestion des catégories (ajout, mise à jour et affichage des catégories).
 * - Navigation via la barre de navigation inférieure (Accueil, Scan, Profil).
 */

public class MainActivity extends AppCompatActivity {

    private CategoryService categoryService;

    // Lanceur pour l'activité galerie permettant de sélectionner une image
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Vérifie si l'utilisateur a sélectionné une image avec succès
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Récupère l'URI de l'image sélectionnée
                    Uri selectedImageUri = result.getData().getData();

                    if (selectedImageUri != null) {
                        // Crée une intention pour lancer l'activité CameraActivity
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);

                        // Passe l'URI de l'image sélectionnée en tant qu'extra
                        intent.putExtra("importedImageUri", selectedImageUri.toString());

                        // Démarre CameraActivity
                        startActivity(intent);
                    }
                } else {
                    // Message d'erreur si aucune image n'est sélectionnée
                    Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation du service de catégories
        categoryService = CategoryService.getInstance(this);

        // Configuration du RecyclerView pour les catégories
        setupCategoriesRecyclerView();

        // Initialisation des boutons
        ImageView btnCamera = findViewById(R.id.btnCamera);
        ImageView btnImportPicture = findViewById(R.id.btnImportPicture);
        ImageView btnCategories = findViewById(R.id.btnCategories);

        // Listener pour le bouton de la caméra
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Listener pour le bouton d'importation d'image
        btnImportPicture.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        // Listener pour le bouton de gestion des catégories
        btnCategories.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            startActivityForResult(intent, 2); // Code de requête 2
        });

        // Initialisation du bouton pour afficher toutes les catégories
        Button btnShowAllCategories = findViewById(R.id.btnShowAllCategories);
        btnShowAllCategories.setOnClickListener(v -> {
            // Lancement de l'activité pour afficher toutes les catégories
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            startActivityForResult(intent, 3); // Code de requête pour afficher toutes les catégories
        });

        // Configuration de la navigation inférieure
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Gestion des actions selon l'élément sélectionné
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

        // Mettre à jour l'adaptateur avec les 4 principales catégories
        RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
        MainCategoryAdapter adapter = (MainCategoryAdapter) recyclerView.getAdapter();

        // Vérifier si l'adaptateur est non nul avant de le mettre à jour
        if (adapter != null) {
            adapter.updateTopCategories(updatedCategories);
        }
    }

    /**
     * Configure le RecyclerView pour afficher les catégories.
     */
    private void setupCategoriesRecyclerView() {
        // Obtenir la référence du RecyclerView à partir de la vue
        RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);

        // Définir une disposition en grille avec 2 colonnes
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Récupérer toutes les catégories via le service
        List<Category> categories = categoryService.getCategories();

        // Initialiser l'adaptateur avec une liste de catégories et les actions associées
        MainCategoryAdapter adapter = new MainCategoryAdapter(this, categories,
                new MainCategoryAdapter.OnCategoryClickListener() {
                    @Override
                    public void onCategoryClick(Category category) {
                        // Action à effectuer lorsqu'une catégorie est sélectionnée
                        Intent intent = new Intent(MainActivity.this, CategoryDetailActivity.class);
                        intent.putExtra("categoryId", category.getId()); // Passer l'ID de la catégorie
                        startActivity(intent); // Lancer l'activité des détails de la catégorie
                    }

                    @Override
                    public void onShowMoreClick() {
                        // Action pour afficher toutes les catégories
                        Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
                        startActivity(intent); // Lancer l'activité de gestion des catégories
                    }
                }
        );

        // Assigner l'adaptateur au RecyclerView
        recyclerView.setAdapter(adapter);

        // Mettre à jour les 4 principales catégories dans l'adaptateur
        adapter.updateTopCategories(categories);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérifier si le code de requête est 3 et que le résultat est OK
        if (requestCode == 3 && resultCode == RESULT_OK) { // Code pour gérer les mises à jour
            // Récupérer les catégories mises à jour via le service
            List<Category> updatedCategories = categoryService.getCategories();

            // Mettre à jour uniquement les 4 principales catégories affichées
            RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
            MainCategoryAdapter adapter = (MainCategoryAdapter) recyclerView.getAdapter();

            if (adapter != null) {
                adapter.updateTopCategories(updatedCategories); // Mettre à jour les catégories
            }
        }
    }

}