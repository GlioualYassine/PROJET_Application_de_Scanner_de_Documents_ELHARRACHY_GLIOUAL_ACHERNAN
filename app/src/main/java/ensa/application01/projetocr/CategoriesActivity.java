package ensa.application01.projetocr;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Classe CategoriesActivity qui gère l'affichage et la gestion des catégories dans l'application.
 * Elle inclut les fonctionnalités suivantes :
 * - Configuration de l'interface utilisateur pour une compatibilité avec les marges système.
 * - Support pour un affichage fluide, prenant en compte les barres système (status bar, navigation bar).
 * - Adaptation de l'interface utilisateur pour une expérience utilisateur moderne.
 */

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Active la gestion des marges système pour une meilleure compatibilité visuelle.
        EdgeToEdge.enable(this);

        // Définit la mise en page de l'activité à partir de la ressource XML associée.
        setContentView(R.layout.activity_categories);

        // Configure le comportement des marges système (barres de navigation et d'état).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Récupère les marges système (barres de navigation et d'état).
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Applique un padding au composant principal de l'interface pour éviter un chevauchement
            // avec les barres système.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Retourne les marges pour poursuivre leur propagation si nécessaire.
            return insets;
        });
    }
}
