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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OCRActivity extends AppCompatActivity {

    private static final String TAG = "OCRActivity"; // Pour le log
    private ImageView imageViewPreview;
    private EditText editTextResult;
    private Button btnCopyText, btnPerformOCR;

    private Bitmap capturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocractivity);

        // Initialisation des vues
        imageViewPreview = findViewById(R.id.imageViewPreview);
        editTextResult = findViewById(R.id.editTextResult);
        btnCopyText = findViewById(R.id.btnCopyText);
        btnPerformOCR = findViewById(R.id.btnPerformOCR);

        // Récupération de l'image capturée depuis CameraActivity
        Intent intent = getIntent();
        String photoUriString = intent.getStringExtra("photoUri");
        if (photoUriString != null) {
            Uri photoUri = Uri.parse(photoUriString);
            try {
                File imageFile = new File(photoUri.getPath());
                capturedBitmap = correctImageOrientation(imageFile.getAbsolutePath());
                imageViewPreview.setImageBitmap(capturedBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image.", Toast.LENGTH_SHORT).show();
            }
        }

        // Bouton pour effectuer l'OCR
        btnPerformOCR.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                performOCR(capturedBitmap);
            } else {
                Toast.makeText(this, "Aucune image à analyser.", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton pour copier le texte reconnu
        btnCopyText.setOnClickListener(v -> {
            String text = editTextResult.getText().toString();
            if (!text.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("OCR Text", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Texte copié dans le presse-papier.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Aucun texte à copier.", Toast.LENGTH_SHORT).show();
            }
        });
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
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void performOCR(Bitmap bitmap) {
        new Thread(() -> {
            try {
                // Créer un fichier temporaire à partir de l'image
                File tempFile = File.createTempFile("ocr_image", ".jpg", getCacheDir());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapData = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();

                // Appeler l'API OCR
                callOCRApi(tempFile);

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erreur lors de la préparation de l'image.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void callOCRApi(File imageFile) {
        String url = "https://api.edenai.run/v2/ocr/ocr";
        String apiKey = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZTkwYWQyMDAtYzUxYS00MWI3LThkM2ItMGM3MGY1MWRhMTVjIiwidHlwZSI6ImFwaV90b2tlbiJ9.c_Jbg7KA5On3LdaUY_eTbi85qnzApVgZ6noa4Xg_iKA";

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .addFormDataPart("providers", "google")
                .addFormDataPart("language", "en")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .post(requestBody)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // Extraire le texte reconnu
                    String recognizedText = jsonResponse.getJSONObject("google").getString("text");

                    // Nettoyer le texte (exemple : suppression des caractères indésirables)
                    String cleanedText = cleanText(recognizedText);

                    runOnUiThread(() -> editTextResult.setText(cleanedText));
                } else {
                    runOnUiThread(() -> Toast.makeText(OCRActivity.this, "Erreur OCR : " + response.code(), Toast.LENGTH_SHORT).show());
                    Log.e("OCRActivity", "Erreur API : " + response.body().string());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(OCRActivity.this, "Erreur lors de l'appel à l'API OCR.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String cleanText(String text) {
        String[] lines = text.split("\n");
        StringBuilder cleanedText = new StringBuilder();

        for (String line : lines) {
            if (!line.contains("<") && !line.contains("public class") && !line.contains(".xml")) {
                cleanedText.append(line.trim()).append("\n");
            }
        }

        return cleanedText.toString().trim();
    }
}
