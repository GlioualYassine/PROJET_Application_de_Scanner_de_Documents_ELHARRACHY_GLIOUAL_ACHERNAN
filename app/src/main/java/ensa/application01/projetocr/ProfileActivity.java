package ensa.application01.projetocr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ensa.application01.projetocr.adapters.ImageAdapter;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView headerTextView;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_SYNCED_IMAGES = "SyncedImages";
    private RecyclerView savedImagesRecyclerView;

    private String getOrGenerateUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, null);

        if (savedUsername == null) {
            // Generate a random username
            savedUsername = "USERNAME#" + (10000 + new Random().nextInt(90000));

            // Save the username to SharedPreferences
            sharedPreferences.edit().putString(KEY_USERNAME, savedUsername).apply();
        }

        return savedUsername;
    }

    private void initializeSyncedImages() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> syncedImages = sharedPreferences.getStringSet(KEY_SYNCED_IMAGES, null);

        if (syncedImages == null) {
            // Initialize as an empty set
            sharedPreferences.edit().putStringSet(KEY_SYNCED_IMAGES, new HashSet<>()).apply();
        }
    }

    public void addImageToSyncedImages(String imagePath) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> syncedImages = sharedPreferences.getStringSet(KEY_SYNCED_IMAGES, new HashSet<>());
        syncedImages.add(imagePath);
        sharedPreferences.edit().putStringSet(KEY_SYNCED_IMAGES, syncedImages).apply();
    }

    public List<String> getSyncedImages() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> syncedImages = sharedPreferences.getStringSet(KEY_SYNCED_IMAGES, new HashSet<>());
        return new ArrayList<>(syncedImages); // Convert to a List for RecyclerView
    }

    private void removeImageFromSyncedImages(String imagePath) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> syncedImages = sharedPreferences.getStringSet(KEY_SYNCED_IMAGES, new HashSet<>());

        if (syncedImages != null) {
            syncedImages.remove(imagePath);
            sharedPreferences.edit().putStringSet(KEY_SYNCED_IMAGES, syncedImages).apply();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        usernameTextView = findViewById(R.id.usernameTextView);
        headerTextView = findViewById(R.id.headerTextView);

        // Generate or retrieve the username
        String username = getOrGenerateUsername();
        usernameTextView.setText(username);

        // Retrieve SyncedImages
        List<String> syncedImages = getSyncedImages();

        // Set up RecyclerView
        if (syncedImages.isEmpty()) {
            headerTextView.append("Les Fichiers Synchronisés\n\n(Aucun fichier synchronisé)");
        } else {
            headerTextView.append("Les Fichiers synchronisés\n");

            // Find the LinearLayout container for synced images
            LinearLayout syncedImagesContainer = findViewById(R.id.syncedImagesContainer);
            syncedImagesContainer.removeAllViews(); // Clear existing views

            for (String imagePath : syncedImages) {
                // Create a new ImageView for each image
                ImageView imageView = new ImageView(this);

                // Set layout parameters for small-sized images
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpToPx(100), // Width: 100dp
                        dpToPx(100)  // Height: 100dp
                );
                params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8)); // Add margins for spacing
                imageView.setLayoutParams(params);

                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Crop the image to fit the size

                // Load the image using Glide or any other library
                Glide.with(this)
                        .load(imagePath)
                        .into(imageView);

                // Add the ImageView to the LinearLayout container
                syncedImagesContainer.addView(imageView);
            }
        }





        // Configuration de la navigation inférieure
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Gestion des actions selon l'élément sélectionné
            if (itemId == R.id.nav_home) {
                Intent cameraIntent = new Intent(this, MainActivity.class);
                startActivity(cameraIntent);
                return true;

            } else if (itemId == R.id.nav_scan) {
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                startActivity(cameraIntent);
                return true;

            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                return true;

            } else {
                return false;
            }
        });

    }
}
