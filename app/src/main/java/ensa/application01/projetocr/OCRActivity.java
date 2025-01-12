package ensa.application01.projetocr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Classe OCRActivity qui gère les fonctionnalités liées à la reconnaissance de texte (OCR).
 * Elle inclut les fonctionnalités suivantes :
 * - Sélection d'images pour effectuer la reconnaissance de texte.
 * - Appel à une API OCR externe pour extraire le texte des images.
 * - Affichage du texte extrait dans l'interface utilisateur.
 * - Gestion des erreurs en cas de problème avec l'API ou la sélection d'image.
 */

public class OCRActivity extends AppCompatActivity {

    // Pour le log
    private EditText editTextResult;

    private Bitmap capturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocractivity);

        // Initialisation des vues
        ImageView imageViewPreview = findViewById(R.id.imageViewPreview);
        editTextResult = findViewById(R.id.editTextResult);
        Button btnCopyText = findViewById(R.id.btnCopyText);
        Button btnPerformOCR = findViewById(R.id.btnPerformOCR);

        // Récupération de l'image capturée depuis CameraActivity
        Intent intent         = getIntent();
        String photoUriString = intent.getStringExtra("photoUri");

        if (photoUriString != null) {
            Uri photoUri = Uri.parse(photoUriString);
            try {
                // Corriger l'orientation de l'image et l'afficher
                capturedBitmap = correctImageOrientation(photoUri);
                imageViewPreview.setImageBitmap(capturedBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image.", Toast.LENGTH_SHORT).show();
            }
        }

        // Bouton pour effectuer l'OCR
        btnPerformOCR.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                // Analyse de l'image via OCR
                performOCR(capturedBitmap);
            } else {
                Toast.makeText(this, "Aucune image à analyser.", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton pour copier le texte reconnu
        btnCopyText.setOnClickListener(v -> {
            String text = editTextResult.getText().toString();
            if (!text.isEmpty()) {
                // Copier le texte dans le presse-papier
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip               = ClipData.newPlainText("OCR Text", text);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, "Texte copié dans le presse-papier.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Aucun texte à copier.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private Bitmap correctImageOrientation(Uri photoUri) {
        try {
            // Décodage de l'image à partir de l'URI
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));

            // Obtention des métadonnées Exif pour déterminer l'orientation de l'image
            ExifInterface exif = new ExifInterface(Objects.requireNonNull(getContentResolver().openInputStream(photoUri)));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // Corriger l'orientation de l'image en fonction des métadonnées
            return switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270);
                default -> bitmap; // Retourner l'image telle quelle si l'orientation est correcte
            };

        } catch (Exception e) {
            // Gestion des erreurs lors du traitement de l'image
            e.printStackTrace();
            return null; // Retourner null en cas d'erreur
        }
    }


    /**
     * Effectue une rotation sur une image en fonction de l'angle spécifié.
     *
     * @param source L'image source (Bitmap).
     * @param angle L'angle de rotation (en degrés).
     * @return Un nouveau Bitmap avec la rotation appliquée.
     */
    private Bitmap rotateImage(Bitmap source, float angle) {
        // Créer une matrice pour appliquer la rotation
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // Créer un nouveau Bitmap avec la rotation appliquée
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Effectue une reconnaissance de texte (OCR) sur un Bitmap donné.
     *
     * @param bitmap L'image Bitmap à analyser.
     */
    private void performOCR(Bitmap bitmap) {
        // Exécuter l'OCR dans un thread séparé pour éviter de bloquer l'interface utilisateur
        new Thread(() -> {
            try {
                // Créer un fichier temporaire pour stocker l'image
                File tempFile = File.createTempFile("ocr_image", ".jpg", getCacheDir());

                // Convertir le Bitmap en tableau de bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapData = bos.toByteArray();

                // Écrire les données dans le fichier temporaire
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();

                // Appeler l'API OCR avec le fichier temporaire
                callOCRApi(tempFile);

            } catch (IOException e) {
                // Gérer les erreurs lors de la préparation de l'image
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Erreur lors de la préparation de l'image.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    /**
     * Appelle une API OCR pour extraire du texte d'une image.
     *
     * @param imageFile Le fichier image à analyser.
     */
    private void callOCRApi(File imageFile) {
        // URL de l'API OCR
        String url = "https://api.edenai.run/v2/ocr/ocr";

        // Clé API pour l'autorisation
        String apiKey = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZTkwYWQyMDAtYzUxYS00MWI3LThkM2ItMGM3MGY1MWRhMTVjIiwidHlwZSI6ImFwaV90b2tlbiJ9.c_Jbg7KA5On3LdaUY_eTbi85qnzApVgZ6noa4Xg_iKA";

        // Client HTTP pour effectuer les appels API
        OkHttpClient client = new OkHttpClient();

        // Construction du corps de la requête (multipart/form-data)
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg"))) // Image à analyser
                .addFormDataPart("providers", "google") // Fournisseur OCR (Google)
                .addFormDataPart("language", "en") // Langue pour l'analyse
                .build();

        // Création de la requête HTTP
        Request request = new Request.Builder()
                .url(url) // URL de l'API
                .addHeader("Authorization", apiKey) // Clé API pour l'autorisation
                .post(requestBody) // Corps de la requête
                .build();

        // Exécuter l'appel API dans un thread séparé
        new Thread(() -> {
            try {
                // Envoyer la requête et récupérer la réponse
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    // Récupérer le corps de la réponse (JSON)
                    assert response.body() != null;
                    String responseBody = response.body().string();

                    // Convertir la réponse en objet JSON
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // Extraire le texte reconnu depuis le JSON
                    String recognizedText = jsonResponse.getJSONObject("google").getString("text");

                    // Mettre à jour l'interface utilisateur avec le texte reconnu
                    runOnUiThread(() -> editTextResult.setText(recognizedText));

                } else {
                    // Gestion des erreurs de l'API
                    runOnUiThread(() ->
                            Toast.makeText(OCRActivity.this, "Erreur OCR : " + response.code(), Toast.LENGTH_SHORT).show());
                    assert response.body() != null;
                    Log.e("OCRActivity", "Erreur API : " + response.body().string());
                }

            } catch (Exception e) {
                // Gestion des exceptions
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(OCRActivity.this, "Erreur lors de l'appel à l'API OCR.", Toast.LENGTH_SHORT).show());
            }
        }).start(); // Démarrer le thread
    }

}
