package ensa.application01.projetocr;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


/**
 * Classe CameraActivity qui gère les fonctionnalités liées à l'utilisation de la caméra.
 * Elle inclut les fonctionnalités suivantes :
 * - Vérification des permissions pour accéder à la caméra.
 * - Capture d'images via la caméra et enregistrement des fichiers.
 * - Affichage d'un aperçu de l'image capturée.
 * - Navigation entre les différentes pages de l'application via un menu de navigation.
 * - Conversion d'images en PDF ou application de l'OCR (reconnaissance optique des caractères).
 * - Gestion de l'orientation des images pour un affichage correct.
 */
public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100; // Code de permission pour la caméra
    private static final int CAMERA_REQUEST_CODE = 101;    // Code pour la demande d'ouverture de la caméra

    private ImageView imageViewPreview; // ImageView pour afficher l'image capturée
    private Uri photoUri;               // URI de l'image capturée
    private File photoFile;             // Fichier de l'image capturée

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Initialisation des vues
        imageViewPreview = findViewById(R.id.imageViewPreview);
        Button btnCaptureImage = findViewById(R.id.btnCaptureImage);
        Button btnToPDF = findViewById(R.id.btnToPDF);
        Button btnToOCR = findViewById(R.id.btnOCR);
        Button btnToCategory = findViewById(R.id.btnToCategories);

        // Vérifie si une image a été importée depuis la galerie
        Intent intent = getIntent();
        String importedImageUri = intent.getStringExtra("importedImageUri");
        if (importedImageUri != null) {
            photoUri = Uri.parse(importedImageUri); // Charger l'image importée
            imageViewPreview.setImageURI(photoUri);
        }

        // Vérifie et demande les permissions nécessaires pour utiliser la caméra
        checkCameraPermission();

        // Initialisation du menu de navigation en bas de l'écran
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_scan); // Sélectionne par défaut l'onglet "Scan"

        // Gérer la navigation entre les éléments du menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Logique pour la page d'accueil
                Intent homeIntent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.nav_scan) {
                // Rester dans l'activité actuelle
                Toast.makeText(this, "Déjà dans Scanner", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent ProfileIntent = new Intent(this, ProfileActivity.class);
                startActivity(ProfileIntent);
                return true;
            } else {
                return false;
            }
        });


        /// Capture d'image via la caméra
        btnCaptureImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission caméra non accordée.", Toast.LENGTH_SHORT).show();
                checkCameraPermission();
            }
        });

        // Convertir en PDF
        btnToPDF.setOnClickListener(v -> {
            if (photoUri != null) {
                Intent pdfIntent = new Intent(CameraActivity.this, PDFActivity.class);
                pdfIntent.putExtra("photoUri", photoUri.toString());
                startActivity(pdfIntent);
            } else {
                Toast.makeText(this, "Aucune image disponible pour la conversion en PDF.", Toast.LENGTH_SHORT).show();
            }
        });

        // Appliquer l'OCR
        btnToOCR.setOnClickListener(v -> {
            if (photoUri != null) {
                Intent ocrIntent = new Intent(CameraActivity.this, OCRActivity.class);
                ocrIntent.putExtra("photoUri", photoUri.toString());
                startActivity(ocrIntent);
            } else {
                Toast.makeText(this, "Aucune image disponible pour l'OCR.", Toast.LENGTH_SHORT).show();
            }
        });


        // Ajouter a une catégorie:
        btnToCategory.setOnClickListener(v -> {
            if (photoUri != null) {
                // Create an intent to launch the CategoryManagementActivity
                Intent addToCategoryIntent = new Intent(this, AddToCategoryActivity.class);
                addToCategoryIntent.putExtra("photoUri", photoUri.toString());
                addToCategoryIntent.putExtra("isSelectMode", true); // Indicate selection mode
                startActivity(addToCategoryIntent); // Start the activity
            } else {
                Toast.makeText(this, "Aucune image disponible pour l'ajout à une catégorie.", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, "ensa.application01.projetocr.fileprovider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Erreur lors de la création du fichier d'image.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur : impossible de créer le fichier d'image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Aucune application caméra trouvée.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCapturedImages");
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // Create directory if it does not exist
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (photoFile == null || !photoFile.exists()) {
                Toast.makeText(this, "Erreur : fichier d'image introuvable.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            Bitmap rotatedBitmap = correctImageOrientation(photoFile.getAbsolutePath());
            imageViewPreview.setImageBitmap(rotatedBitmap);

            // Save the captured image path to SharedPreferences
            saveImagePath(photoFile.getAbsolutePath());

            // Notify the user
            Toast.makeText(this, "Image sauvegardée et ajoutée à la liste principale.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Capture annulée ou échouée.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap correctImageOrientation(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap rotatedBitmap;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateBitmap(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateBitmap(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateBitmap(bitmap, 270);
                    break;
                default:
                    rotatedBitmap = bitmap;
            }
            return rotatedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return BitmapFactory.decodeFile(imagePath);
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void saveImagePath(String imagePath) {
        SharedPreferences sharedPreferences = getSharedPreferences("ensa.application01.projetocr", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the current list of image paths
        Set<String> imagePaths = sharedPreferences.getStringSet("captured_images", new HashSet<>());
        imagePaths.add(imagePath);

        // Save the updated list
        editor.putStringSet("captured_images", imagePaths);
        editor.apply();
    }
}
