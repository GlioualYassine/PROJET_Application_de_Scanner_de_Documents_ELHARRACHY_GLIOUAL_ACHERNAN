package ensa.application01.projetocr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<File> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les boutons
        ImageView btnCamera = findViewById(R.id.btnCamera);
        ImageView btnPdfTools = findViewById(R.id.btnPdfTools);
        ImageView btnImportPicture = findViewById(R.id.btnImportPicture);
        ImageView btnImportFile = findViewById(R.id.btnImportFile);
        Button btnRefresh = findViewById(R.id.btnRefresh); // Nouveau bouton "Actualiser"

        // RecyclerView pour afficher les images capturées
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Charger les images capturées
        imageList = loadCapturedImages();
        imageAdapter = new ImageAdapter(imageList, this);
        recyclerView.setAdapter(imageAdapter);

        // Bouton pour ouvrir la page de la caméra
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Bouton pour les outils PDF
        btnPdfTools.setOnClickListener(v -> {
            Toast.makeText(this, "Outils PDF non implémentés", Toast.LENGTH_SHORT).show();
        });

        // Bouton pour importer une image
        btnImportPicture.setOnClickListener(v -> {
            Toast.makeText(this, "Importer une image non implémenté", Toast.LENGTH_SHORT).show();
        });

        // Bouton pour importer un fichier
        btnImportFile.setOnClickListener(v -> {
            Toast.makeText(this, "Importer un fichier non implémenté", Toast.LENGTH_SHORT).show();
        });

        // Bouton "Actualiser" pour recharger la liste
        btnRefresh.setOnClickListener(v -> {
            refreshImageList();
        });
    }

    /**
     * Méthode pour actualiser la liste des images.
     */
    private void refreshImageList() {
        imageList.clear();
        imageList.addAll(loadCapturedImages());
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
