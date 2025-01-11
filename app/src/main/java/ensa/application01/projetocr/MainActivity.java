package ensa.application01.projetocr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<File> recentFiles;

    // Gestion des résultats pour l'importation d'image
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Transférer l'image à CameraActivity
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

        // Initialiser les boutons
        ImageView btnCamera = findViewById(R.id.btnCamera);
        ImageView btnImportPicture = findViewById(R.id.btnImportPicture);
        ImageView btnCategories = findViewById(R.id.btnCategories);

        // RecyclerView pour afficher les fichiers récents
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Charger les fichiers récents
        recentFiles = loadCapturedImages();
        imageAdapter = new ImageAdapter(recentFiles, this);
        recyclerView.setAdapter(imageAdapter);

        // Logique des boutons
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        btnImportPicture.setOnClickListener(v -> {
            // Lancer l'intention pour sélectionner une image
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btnCategories.setOnClickListener(v -> {
            Toast.makeText(this, "Catégories (fonctionnalité à implémenter)", Toast.LENGTH_SHORT).show();
        });

        // Initialiser le menu de navigation en bas
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Logique pour la page d'accueil
                Toast.makeText(this, "Accueil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_scan) {
                // Ouvrir l'activité de la caméra
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Logique pour la page du profil
                Toast.makeText(this, "Profil (fonctionnalité à implémenter)", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Méthode pour actualiser la liste des images.
     */
    private void refreshImageList() {
        imageAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Liste des images actualisée", Toast.LENGTH_SHORT).show();
    }

    /**
     * Charge les images capturées à partir du répertoire local.
     */
    private List<File> loadCapturedImages() {
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCapturedImages");
        List<File> images = new ArrayList<>();
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        images.add(file);
                    }
                }
            }
        }
        return images;
    }
}
