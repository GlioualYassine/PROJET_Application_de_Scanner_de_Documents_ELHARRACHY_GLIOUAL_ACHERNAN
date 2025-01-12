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
import java.util.Objects;

public class PDFActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Bitmap imageBitmap;

    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfactivity);

        // Initialisation des composants de l'interface utilisateur
        ImageView imageViewPreview = findViewById(R.id.imageViewPreview);
        progressBar = findViewById(R.id.progressBar);
        Button btnConvertToPDF = findViewById(R.id.btnConvertToPDF);
        Button btnDownloadPDF = findViewById(R.id.btnDownloadPDF);

        // Charger l'image à partir de l'Intent
        Intent intent = getIntent();
        String photoUriString = intent.getStringExtra("photoUri"); // Récupère l'URI de l'image transmise
        if (photoUriString != null) {
            Uri photoUri = Uri.parse(photoUriString);
            try {
                // Décoder l'image depuis son URI et corriger son orientation
                imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                imageBitmap = correctImageOrientation(photoUri);
                imageViewPreview.setImageBitmap(imageBitmap); // Affiche l'image dans le composant ImageView
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image.", Toast.LENGTH_SHORT).show();
            }
        }

        // Action du bouton "Convertir en PDF"
        btnConvertToPDF.setOnClickListener(v -> {
            if (imageBitmap != null) {
                createPdfDocument(); // Méthode pour créer le fichier PDF
            } else {
                Toast.makeText(this, "Aucune image à convertir.", Toast.LENGTH_SHORT).show();
            }
        });

        // Action du bouton "Télécharger le PDF"
        btnDownloadPDF.setOnClickListener(v -> {
            if (pdfUri != null) {
                openPDF(pdfUri); // Ouvre le fichier PDF dans une visionneuse
            } else {
                Toast.makeText(this, "Aucun fichier PDF disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Méthode pour créer un fichier PDF.
     * Cette méthode ouvre un sélecteur de documents pour permettre à l'utilisateur
     * de choisir où enregistrer le fichier PDF.
     */
    private void createPdfDocument() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT); // Intent pour créer un nouveau fichier
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Rend le fichier accessible
        intent.setType("application/pdf"); // Spécifie le type MIME du fichier
        intent.putExtra(Intent.EXTRA_TITLE, "CapturedImage.pdf"); // Définit le nom par défaut du fichier
        startActivityForResult(intent, 101); // Lance l'activité avec un code de requête spécifique
    }

    /**
     * Gestion du résultat de l'activité `createPdfDocument`.
     * Cette méthode est appelée après que l'utilisateur a choisi un emplacement pour le fichier PDF.
     *
     * @param requestCode Code de la requête envoyé à l'activité
     * @param resultCode  Code indiquant si l'opération a réussi ou non
     * @param data        Données renvoyées par l'activité (ici, l'URI du fichier)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérifie que le code de requête correspond et que l'opération s'est terminée avec succès
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                pdfUri = data.getData(); // Récupère l'URI du fichier PDF sélectionné
                savePdfToUri(pdfUri); // Sauvegarde le contenu dans le fichier PDF
            }
        }
    }


    private void savePdfToUri(Uri uri) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        new Thread(() -> {
            try {
                // Convertir l'image en flux de données
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                ImageData imageData = ImageDataFactory.create(stream.toByteArray());

                // Initialiser les composants PDF
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);

                // Adapter l'image pour qu'elle occupe toute la page PDF
                float pdfWidth = pdfDocument.getDefaultPageSize().getWidth();
                float pdfHeight = pdfDocument.getDefaultPageSize().getHeight();
                Image image = new Image(imageData);
                image.scaleAbsolute(pdfWidth, pdfHeight);
                image.setFixedPosition(0, 0);

                document.add(image);
                document.close();

                // Mise à jour de l'interface utilisateur
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "PDF créé avec succès !", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                // Gérer les erreurs
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Erreur lors de la création du PDF : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }


    private void openPDF(Uri uri) {
        // Intent pour afficher le PDF
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            // Tenter d'ouvrir le fichier PDF
            startActivity(intent);
        } catch (Exception e) {
            // Gérer les erreurs si aucune application n'est disponible
            e.printStackTrace();
            Toast.makeText(this, "Aucune application disponible pour ouvrir le PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap correctImageOrientation(Uri photoUri) {
        try {
            // Obtenir les métadonnées d'orientation de l'image
            ExifInterface exif = new ExifInterface(Objects.requireNonNull(getContentResolver().openInputStream(photoUri)));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // Corriger l'orientation en fonction des données EXIF
            return switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(imageBitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(imageBitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(imageBitmap, 270);
                default -> imageBitmap; // Aucune rotation nécessaire
            };
        } catch (Exception e) {
            // Gérer les exceptions en retournant l'image d'origine
            e.printStackTrace();
            return imageBitmap;
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        // Créer une matrice pour effectuer la rotation
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // Retourner l'image tournée
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
