package ensa.application01.projetocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class PDFActivity extends AppCompatActivity {

    private ImageView imageViewPreview;
    private ProgressBar progressBar;
    private Button btnConvertToPDF, btnDownloadPDF;
    private Bitmap imageBitmap;

    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfactivity);

        imageViewPreview = findViewById(R.id.imageViewPreview);
        progressBar = findViewById(R.id.progressBar);
        btnConvertToPDF = findViewById(R.id.btnConvertToPDF);
        btnDownloadPDF = findViewById(R.id.btnDownloadPDF);

        // Charger l'image
        Intent intent = getIntent();
        String photoUriString = intent.getStringExtra("photoUri");
        if (photoUriString != null) {
            Uri photoUri = Uri.parse(photoUriString);
            try {
                imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                imageBitmap = correctImageOrientation(photoUri); // Corrige l'orientation
                imageViewPreview.setImageBitmap(imageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image.", Toast.LENGTH_SHORT).show();
            }
        }

        // Convertir en PDF
        btnConvertToPDF.setOnClickListener(v -> {
            if (imageBitmap != null) {
                createPdfDocument();
            } else {
                Toast.makeText(this, "Aucune image à convertir.", Toast.LENGTH_SHORT).show();
            }
        });

        // Télécharger le PDF
        btnDownloadPDF.setOnClickListener(v -> {
            if (pdfUri != null) {
                openPDF(pdfUri);
            } else {
                Toast.makeText(this, "Aucun fichier PDF disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPdfDocument() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "CapturedImage.pdf");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                pdfUri = data.getData();
                savePdfToUri(pdfUri);
            }
        }
    }

    private void savePdfToUri(Uri uri) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        new Thread(() -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                ImageData imageData = ImageDataFactory.create(stream.toByteArray());

                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);

                // Définir les dimensions exactes de la page PDF
                float pdfWidth = pdfDocument.getDefaultPageSize().getWidth();
                float pdfHeight = pdfDocument.getDefaultPageSize().getHeight();

                // Créer une image qui occupe toute la page
                Image image = new Image(imageData);
                image.scaleAbsolute(pdfWidth, pdfHeight); // Adapter exactement aux dimensions de la page
                image.setFixedPosition(0, 0); // Positionner à (0,0) pour couvrir toute la zone

                document.add(image);
                document.close();

                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "PDF créé avec succès !", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Erreur lors de la création du PDF : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void openPDF(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Aucune application disponible pour ouvrir le PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap correctImageOrientation(Uri photoUri) {
        try {
            String filePath = photoUri.getPath();
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(photoUri));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(imageBitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(imageBitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(imageBitmap, 270);
                default:
                    return imageBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return imageBitmap;
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
