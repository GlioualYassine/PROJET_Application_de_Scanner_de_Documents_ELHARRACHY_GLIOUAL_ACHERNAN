package ensa.application01.projetocr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.adapters.ImageAdapter;
import ensa.application01.projetocr.models.Category;
import ensa.application01.projetocr.services.CategoryService;

public class EditCategoryActivity extends AppCompatActivity {

    private CategoryService categoryService;
    private Category category;
    private EditText categoryName;
    private List<String> images;
    private ImageAdapter imageAdapter;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        images.add(selectedImageUri.toString());
                        imageAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

        categoryService = new CategoryService(this);

        int categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId == -1) {
            Toast.makeText(this, "Erreur : ID de catégorie invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            Toast.makeText(this, "Erreur : Catégorie introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        categoryName = findViewById(R.id.editCategoryName);
        categoryName.setText(category.getName());

        images = new ArrayList<>(category.getImages());
        RecyclerView recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(images, imagePath -> {
            images.remove(imagePath);
            imageAdapter.notifyDataSetChanged();
        });
        recyclerView.setAdapter(imageAdapter);

        Button saveButton = findViewById(R.id.saveCategoryButton);
        saveButton.setOnClickListener(v -> {
            String newName = categoryName.getText().toString().trim();
            if (!newName.isEmpty()) {
                categoryService.updateCategory(category.getId(), newName, images);

                // Renvoie les données modifiées à l'activité principale
                Intent resultIntent = new Intent();
                resultIntent.putExtra("categoryId", category.getId());
                resultIntent.putExtra("categoryName", newName);
                setResult(RESULT_OK, resultIntent);

                finish();
            } else {
                Toast.makeText(this, "Le nom de la catégorie ne peut pas être vide", Toast.LENGTH_SHORT).show();
            }
        });



        Button addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });
    }


}
