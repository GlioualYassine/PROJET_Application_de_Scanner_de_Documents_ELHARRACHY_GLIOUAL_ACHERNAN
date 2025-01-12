package ensa.application01.projetocr;

import android.os.Bundle;

import androidx.activity.EdgeToEdge; // Active l'affichage en plein écran (edge-to-edge)
import androidx.appcompat.app.AppCompatActivity; // Classe de base pour les activités avec ActionBar
import androidx.core.graphics.Insets; // Représente les marges liées aux barres système
import androidx.core.view.ViewCompat; // Utilitaire pour les fonctionnalités de compatibilité des vues
import androidx.core.view.WindowInsetsCompat; // Fournit des informations sur les marges de la fenêtre

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Active le mode affichage "edge-to-edge" pour un affichage immersif
        EdgeToEdge.enable(this);

        // Définit le layout de l'activité
        setContentView(R.layout.activity_categories);

        // Ajuste le padding pour la vue principale en fonction des marges des barres système (ex : barre de statut et de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Récupère les dimensions des barres système (haut, bas, gauche, droite)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Applique un padding à la vue pour compenser l'espace occupé par les barres système
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Retourne les insets pour indiquer qu'ils ont été consommés
            return insets;
        });
    }
}
