package ensa.application01.projetocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PDFActivity extends AppCompatActivity {

    private ImageView imageViewPreview;
    private ProgressBar progressBar;
    private Button btnConvertToPDF, btnDownloadPDF;
    private Bitmap testBitmap;
    private Uri savedPdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfactivity);

        imageViewPreview = findViewById(R.id.imageViewPreview);
        progressBar = findViewById(R.id.progressBar);
        btnConvertToPDF = findViewById(R.id.btnConvertToPDF);
        btnDownloadPDF = findViewById(R.id.btnDownloadPDF);

        // Charger une image de test
        testBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        if (testBitmap != null) {
            imageViewPreview.setImageBitmap(testBitmap);
        } else {
            Toast.makeText(this, "Erreur : Impossible de charger l'image de test.", Toast.LENGTH_SHORT).show();
        }

        // Bouton pour convertir en PDF
        btnConvertToPDF.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                createPdfUsingScopedStorage();
            } else {
                savePdfToLegacyStorage(testBitmap);
            }
        });

        // Bouton pour afficher l'URI du fichier enregistré
        btnDownloadPDF.setOnClickListener(v -> {
            if (savedPdfUri != null) {
                Toast.makeText(this, "PDF enregistré à l'emplacement : " + savedPdfUri.toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Aucun PDF disponible à télécharger.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Créer un PDF à l'aide de Scoped Storage (Android 10+)
    private void createPdfUsingScopedStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "ImageToPDF.pdf");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("Documents"));
        savePdfLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> savePdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    savedPdfUri = result.getData().getData();
                    savePdfToUri(savedPdfUri, testBitmap);
                } else {
                    Toast.makeText(this, "PDF non enregistré.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Enregistrer le PDF dans une URI spécifique
    private void savePdfToUri(Uri uri, Bitmap bitmap) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        new Thread(() -> {
            try {
                PDDocument document = new PDDocument();
                PDPage page = new PDPage(new PDRectangle(bitmap.getWidth(), bitmap.getHeight()));
                document.addPage(page);

                File tempFile = File.createTempFile("tempImage", ".jpg", getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdfImage =
                        JPEGFactory.createFromStream(document, new java.io.FileInputStream(tempFile));
                contentStream.drawImage(pdfImage, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                contentStream.close();

                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        document.save(outputStream);
                    }
                }
                document.close();

                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "PDF enregistré avec succès !", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Erreur lors de la création du PDF.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Pour Android 9 ou inférieur : enregistrer dans Téléchargements
    private void savePdfToLegacyStorage(Bitmap bitmap) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        new Thread(() -> {
            try {
                PDDocument document = new PDDocument();
                PDPage page = new PDPage(new PDRectangle(bitmap.getWidth(), bitmap.getHeight()));
                document.addPage(page);

                File tempFile = File.createTempFile("tempImage", ".jpg", getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdfImage =
                        JPEGFactory.createFromStream(document, new java.io.FileInputStream(tempFile));
                contentStream.drawImage(pdfImage, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                contentStream.close();

                File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ImageToPDF.pdf");
                document.save(pdfFile);
                document.close();

                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "PDF enregistré dans : " + pdfFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Erreur lors de la création du PDF.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
