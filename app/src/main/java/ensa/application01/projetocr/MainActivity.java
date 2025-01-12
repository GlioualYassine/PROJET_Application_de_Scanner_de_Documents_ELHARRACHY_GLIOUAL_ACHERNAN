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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Lanceur d'activité pour gérer le résultat de l'importation d'une image depuis la galerie
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Vérifie si une image a été sélectionnée avec succès
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData(); // URI de l'image sélectionnée
                    if (selectedImageUri != null) {
                        // Démarre CameraActivity et transfère l'URI de l'image importée
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        intent.putExtra("importedImageUri", selectedImageUri.toString());
                        startActivity(intent);
                    }
                } else {
                    // Affiche un message si aucune image n'a été sélectionnée
                    Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des boutons pour les différentes fonctionnalités
        ImageView btnCamera = findViewById(R.id.btnCamera); // Bouton pour ouvrir la caméra
        ImageView btnImportPicture = findViewById(R.id.btnImportPicture); // Bouton pour importer une image
        ImageView btnCategories = findViewById(R.id.btnCategories); // Bouton pour les catégories (non implémenté)

        // Configuration du RecyclerView pour afficher les fichiers récents
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Chargement des fichiers récents et configuration de l'adaptateur
        List<File> recentFiles = loadCapturedImages(); // Récupère la liste des fichiers d'images
        ImageAdapter imageAdapter = new ImageAdapter(recentFiles, this);
        recyclerView.setAdapter(imageAdapter);

        // Gestion des clics sur le bouton pour ouvrir la caméra
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent); // Démarre CameraActivity
        });

        // Gestion des clics sur le bouton pour importer une image
        btnImportPicture.setOnClickListener(v -> {
            // Lance une intention pour sélectionner une image dans la galerie
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent); // Démarre le processus de sélection d'image
        });

        // Gestion des clics sur le bouton des catégories (fonctionnalité à implémenter)
        btnCategories.setOnClickListener(v ->
                Toast.makeText(this, "Catégories (fonctionnalité à implémenter)", Toast.LENGTH_SHORT).show()
        );

        // Initialisation du menu de navigation en bas de l'écran
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Identifiant de l'élément sélectionné

            if (itemId == R.id.nav_home) {
                // Affiche un message pour la page d'accueil
                Toast.makeText(this, "Accueil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_scan) {
                // Ouvre CameraActivity pour scanner
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Affiche un message pour la page de profil (non implémentée)
                Toast.makeText(this, "Profil (fonctionnalité à implémenter)", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Charge les images capturées à partir du répertoire local défini pour l'application.
     *
     * @return Une liste contenant les fichiers d'images au format JPG.
     */
    private List<File> loadCapturedImages() {
        // Détermine le répertoire local où les images capturées sont stockées
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCapturedImages");
        List<File> images = new ArrayList<>();

        // Vérifie si le répertoire existe et contient des fichiers
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles(); // Récupère la liste des fichiers dans le répertoire
            if (files != null) {
                for (File file : files) {
                    // Ajoute uniquement les fichiers au format JPG à la liste
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        images.add(file);
                    }
                }
            }
        }
        return images; // Retourne la liste des fichiers d'images
    }
}
