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
 * MainActivity class that handles the main screen of the application.
 * It includes functionality for launching the camera, importing pictures,
 * managing categories, and navigating through the bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {

    private CategoryService categoryService;

    // Launcher for the gallery activity to pick an image
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

        // Initialize the category service
        categoryService = CategoryService.getInstance(this);

        // Setup the categories RecyclerView
        setupCategoriesRecyclerView();

        // Initialize buttons
        ImageView btnCamera = findViewById(R.id.btnCamera);
        ImageView btnImportPicture = findViewById(R.id.btnImportPicture);
        ImageView btnCategories = findViewById(R.id.btnCategories);

        // Set onClick listener for the camera button
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Set onClick listener for the import picture button
        btnImportPicture.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        // Set onClick listener for the categories button
        btnCategories.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            startActivityForResult(intent, 2); // Request code 2
        });

        // Initialize the show all categories button
        Button btnShowAllCategories = findViewById(R.id.btnShowAllCategories);
        btnShowAllCategories.setOnClickListener(v -> {
            // Launch an activity to show all categories
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            startActivityForResult(intent, 3); // Request code to show all categories
        });

        // Configure the bottom navigation
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

        // Retrieve updated categories
        List<Category> updatedCategories = categoryService.getCategories();

        // Update the adapter with the top 4 categories
        RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
        MainCategoryAdapter adapter = (MainCategoryAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateTopCategories(updatedCategories);
        }
    }

    /**
     * Setup the RecyclerView for displaying categories.
     */
    private void setupCategoriesRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2-column grid layout

        List<Category> categories = categoryService.getCategories(); // Retrieve all categories
        MainCategoryAdapter adapter = new MainCategoryAdapter(this, categories,
                new MainCategoryAdapter.OnCategoryClickListener() {
                    @Override
                    public void onCategoryClick(Category category) {
                        // Action when a category is clicked
                        Intent intent = new Intent(MainActivity.this, CategoryDetailActivity.class);
                        intent.putExtra("categoryId", category.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onShowMoreClick() {
                        // Action to show all categories
                        Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
                        startActivity(intent);
                    }
                });

        recyclerView.setAdapter(adapter);

        // Update the top 4 categories
        adapter.updateTopCategories(categories);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3 && resultCode == RESULT_OK) { // Request code to handle updates
            // Retrieve updated categories
            List<Category> updatedCategories = categoryService.getCategories();

            // Update only the top 4 categories
            RecyclerView recyclerView = findViewById(R.id.categoriesRecyclerView);
            MainCategoryAdapter adapter = (MainCategoryAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.updateTopCategories(updatedCategories);
            }
        }
    }
}