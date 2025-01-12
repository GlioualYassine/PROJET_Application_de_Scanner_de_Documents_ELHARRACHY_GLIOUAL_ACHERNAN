package ensa.application01.projetocr;

import android.Manifest;
import android.annotation.SuppressLint;
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

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;

    private ImageView imageViewPreview;
    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera); // Définir la vue de l'activité sur le layout associé à l'activité de la caméra.

        imageViewPreview = findViewById(R.id.imageViewPreview); // Récupérer l'élément d'aperçu de l'image dans l'interface.
        Button btnCaptureImage = findViewById(R.id.btnCaptureImage); // Bouton pour capturer une image depuis la caméra.
        Button btnToPDF = findViewById(R.id.btnToPDF); // Bouton pour convertir une image en PDF.
        Button btnToOCR = findViewById(R.id.btnOCR); // Bouton pour appliquer l'OCR sur une image.

        // Vérifier si une image a été importée depuis la galerie.
        Intent intent = getIntent();
        String importedImageUri = intent.getStringExtra("importedImageUri"); // Récupérer l'URI de l'image importée si elle existe.
        if (importedImageUri != null) {
            photoUri = Uri.parse(importedImageUri); // Convertir l'URI en un objet `Uri`.
            imageViewPreview.setImageURI(photoUri); // Afficher l'image importée dans l'aperçu.
        }

        // Vérifier les permissions pour l'accès à la caméra.
        checkCameraPermission();

        // Initialiser le menu de navigation en bas de l'écran.
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_scan); // Forcer la sélection de l'élément "Scan" par défaut.

        // Gérer la navigation entre les éléments du menu de navigation.
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Naviguer vers la page d'accueil.
                Intent homeIntent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.nav_scan) {
                // Afficher un message car l'utilisateur est déjà dans la page "Scanner".
                Toast.makeText(this, "Déjà dans Scanner", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Afficher un message pour indiquer une fonctionnalité non encore implémentée.
                Toast.makeText(this, "Profil (fonctionnalité à implémenter)", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false; // Aucune action si l'élément sélectionné ne correspond pas.
            }
        });

        // Gestion de l'événement lorsque l'utilisateur clique sur le bouton "Capture Image".
        btnCaptureImage.setOnClickListener(v -> {
            // Vérifier si la permission d'utiliser la caméra est accordée.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // Ouvrir la caméra pour capturer une image.
            } else {
                // Afficher un message d'erreur et vérifier à nouveau les permissions.
                Toast.makeText(this, "Permission caméra non accordée.", Toast.LENGTH_SHORT).show();
                checkCameraPermission();
            }
        });

        // Gestion de l'événement lorsque l'utilisateur clique sur le bouton "Convertir en PDF".
        btnToPDF.setOnClickListener(v -> {
            // Vérifier si une image a été capturée ou importée.
            if (photoUri != null) {
                Intent pdfIntent = new Intent(CameraActivity.this, PDFActivity.class); // Lancer une nouvelle activité pour convertir l'image en PDF.
                pdfIntent.putExtra("photoUri", photoUri.toString()); // Passer l'URI de l'image à la nouvelle activité.
                startActivity(pdfIntent);
            } else {
                // Afficher un message d'erreur si aucune image n'est disponible.
                Toast.makeText(this, "Aucune image disponible pour la conversion en PDF.", Toast.LENGTH_SHORT).show();
            }
        });

        // Gestion de l'événement lorsque l'utilisateur clique sur le bouton "OCR".
        btnToOCR.setOnClickListener(v -> {
            // Vérifier si une image est disponible pour l'OCR.
            if (photoUri != null) {
                Intent ocrIntent = new Intent(CameraActivity.this, OCRActivity.class); // Lancer une nouvelle activité pour appliquer l'OCR.
                ocrIntent.putExtra("photoUri", photoUri.toString()); // Passer l'URI de l'image à la nouvelle activité.
                startActivity(ocrIntent);
            } else {
                // Afficher un message d'erreur si aucune image n'est disponible.
                Toast.makeText(this, "Aucune image disponible pour l'OCR.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Méthode pour vérifier si l'application dispose de la permission d'utiliser la caméra.
    private void checkCameraPermission() {
        // Vérifie si la permission de la caméra n'a pas été accordée.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Si la permission n'est pas accordée, demande la permission à l'utilisateur.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        // Si la permission est déjà accordée, rien ne se passe ici.
    }

    // Méthode appelée automatiquement après que l'utilisateur ait répondu à une demande de permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Vérifie si la demande de permission concerne l'accès à la caméra.
        if (requestCode == CAMERA_PERMISSION_CODE) {
            // Vérifie si la permission a été accordée ou refusée.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si la permission est accordée, affiche un message de confirmation.
                Toast.makeText(this, "Camera permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Si la permission est refusée, affiche un message d'avertissement.
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("QueryPermissionsNeeded") // Annotation pour éviter un avertissement sur l'utilisation de `resolveActivity`.
    private void openCamera() {
        // Crée une intention pour capturer une image à l'aide de la caméra.
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Vérifie si une application capable de gérer l'intention est installée.
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Tente de créer un fichier temporaire pour stocker l'image capturée.
                photoFile = createImageFile();

                // Génère une URI pour ce fichier, en utilisant le FileProvider pour un accès sécurisé.
                photoUri = FileProvider.getUriForFile(this,
                        "ensa.application01.projetocr.fileprovider", // Remplacez par votre autorité FileProvider.
                        photoFile);

                // Ajoute l'URI comme emplacement de sortie pour l'image capturée.
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                // Lance l'intention pour ouvrir l'application de la caméra.
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } catch (IOException e) {
                // Si une erreur se produit lors de la création du fichier, elle est capturée ici.
                e.printStackTrace(); // Affiche la pile d'erreurs dans la console pour le débogage.
                Toast.makeText(this, "Erreur : impossible de créer le fichier d'image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Si aucune application caméra n'est trouvée, affiche un message à l'utilisateur.
            Toast.makeText(this, "Aucune application caméra trouvée.", Toast.LENGTH_SHORT).show();
        }
    }


    private File createImageFile() throws IOException {
        // Génère un nom unique basé sur la date et l'heure
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Répertoire pour stocker les images capturées
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCapturedImages");

        // Crée le répertoire s'il n'existe pas encore
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Crée et retourne un fichier temporaire pour l'image
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérifie si l'image a été capturée avec succès
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (photoFile == null || !photoFile.exists()) {
                Toast.makeText(this, "Erreur : fichier d'image introuvable.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Décodage et correction de l'orientation de l'image
            Bitmap rotatedBitmap = correctImageOrientation(photoFile.getAbsolutePath());
            imageViewPreview.setImageBitmap(rotatedBitmap);

            // Sauvegarde du chemin de l'image dans les préférences
            saveImagePath(photoFile.getAbsolutePath());

            // Notifie l'utilisateur
            Toast.makeText(this, "Image sauvegardée et ajoutée à la liste principale.", Toast.LENGTH_SHORT).show();
        } else {
            // Notifie en cas d'échec ou d'annulation
            Toast.makeText(this, "Capture annulée ou échouée.", Toast.LENGTH_SHORT).show();
        }
    }


    private Bitmap correctImageOrientation(String imagePath) {
        try {
            // Récupération des métadonnées EXIF pour l'orientation
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // Décodage de l'image
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            // Ajustement de l'orientation selon les métadonnées
            return switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270);
                default -> bitmap; // Aucune rotation nécessaire
            };
        } catch (IOException e) {
            e.printStackTrace();
            // Retourne l'image originale en cas d'erreur
            return BitmapFactory.decodeFile(imagePath);
        }
    }


    // Effectue une rotation d'un Bitmap d'un certain angle
    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degrees); // Applique la rotation
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Sauvegarde le chemin d'une image dans les SharedPreferences
    @SuppressLint("MutatingSharedPrefs")
    private void saveImagePath(String imagePath) {
        SharedPreferences sharedPreferences = getSharedPreferences("ensa.application01.projetocr", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Récupère les chemins d'images déjà sauvegardés ou initialise un nouvel ensemble
        Set<String> imagePaths = sharedPreferences.getStringSet("captured_images", new HashSet<>());
        imagePaths.add(imagePath); // Ajoute le nouveau chemin d'image

        // Sauvegarde la liste mise à jour
        editor.putStringSet("captured_images", imagePaths);
        editor.apply(); // Applique les changements
    }

}
