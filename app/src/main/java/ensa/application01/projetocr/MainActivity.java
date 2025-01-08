package ensa.application01.projetocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private ImageView imageViewPreview;
    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vérifier les permissions
        checkAndRequestPermissions();

        // Lier les éléments du layout
        Button btnScanDocument = findViewById(R.id.btnScanDocument);
        imageViewPreview = findViewById(R.id.imageViewPreview);

        // Bouton pour ouvrir la caméra
        btnScanDocument.setOnClickListener(v -> openCamera());
    }

    // Vérifier et demander la permission de la caméra
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    // Résultat de la demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission de caméra accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission de caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Méthode pour ouvrir la caméra
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Vérifier si une application caméra est disponible
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Créer un fichier temporaire pour enregistrer l'image
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, "ensa.application01.projetocr.fileprovider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors de la création du fichier.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Aucune application de caméra détectée.", Toast.LENGTH_SHORT).show();
        }
    }

    // Créer un fichier temporaire pour l'image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Récupérer le résultat de la capture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Charger l'image à partir du fichier temporaire
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageViewPreview.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Capture annulée ou échouée.", Toast.LENGTH_SHORT).show();
        }
    }
}
