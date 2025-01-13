package ensa.application01.projetocr;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;

public class AddToCategoryActivity extends AppCompatActivity {

    private static final String TAG = "AddToCategoryActivity";

    private Uri photoUri;
    private ListView categoryListView;
    private CategoryService categoryService;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_category);

        // Initialize views
        categoryListView = findViewById(R.id.categoryListView);

        // Get photo URI from the intent
        String uriString = getIntent().getStringExtra("photoUri");
        if (uriString != null) {
            photoUri = Uri.parse(uriString);
        } else {
            Toast.makeText(this, "Aucune image à afficher.", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if no image URI is provided
            return;
        }

        // Initialize category service
        categoryService = new CategoryService(this);

        // Load categories
        loadCategories();
    }

    /**
     * Loads the list of categories and displays them in a ListView.
     */
    private void loadCategories() {
        try {
            categoryList = categoryService.getCategories();

            if (categoryList.isEmpty()) {
                Toast.makeText(this, "Aucune catégorie disponible.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract category names for display
            List<String> categoryNames = new ArrayList<>();
            for (Category category : categoryList) {
                categoryNames.add(category.getName());
            }

            // Set up the ListView with an ArrayAdapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryNames);
            categoryListView.setAdapter(adapter);

            // Handle item clicks
            categoryListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                Category selectedCategory = categoryList.get(position);
                addPhotoToCategory(selectedCategory.getId());
            });

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des catégories.", e);
            Toast.makeText(this, "Erreur lors du chargement des catégories.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Adds the photo to the selected category.
     *
     * @param categoryId The ID of the selected category.
     */
    private void addPhotoToCategory(int categoryId) {
        try {
            // Retrieve the selected category
            Category selectedCategory = categoryService.getCategoryById(categoryId);
            if (selectedCategory == null) {
                Toast.makeText(this, "Catégorie introuvable.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add the photo to the category's image list
            List<String> newImageList = selectedCategory.getImages();
            newImageList.add(photoUri.toString());

            // Update the category
            categoryService.updateCategory(selectedCategory.getId(), selectedCategory.getName(), newImageList);

            // Notify the user
            Toast.makeText(this, "Photo ajoutée à la catégorie: " + selectedCategory.getName(), Toast.LENGTH_SHORT).show();
            finish(); // Close the activity after successful addition
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'ajout de la photo à la catégorie.", e);
            Toast.makeText(this, "Erreur lors de l'ajout de la photo à la catégorie.", Toast.LENGTH_LONG).show();
        }
    }
}
