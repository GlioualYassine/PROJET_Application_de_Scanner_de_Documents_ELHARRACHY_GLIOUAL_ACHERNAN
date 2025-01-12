package ensa.application01.projetocr.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.R;

public class CategoryPhotoAdapter extends RecyclerView.Adapter<CategoryPhotoAdapter.ViewHolder> {

    private List<String> photoUrls; // Liste des URLs des images
    private final Context context;

    public CategoryPhotoAdapter(Context context) {
        this.context = context;
        this.photoUrls = new ArrayList<>(); // Initialisation de la liste vide
    }

    // Méthode pour définir les photos dans l'adaptateur
    public void setPhotos(List<String> photos) {
        this.photoUrls = photos != null ? photos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Liez le layout `item_card.xml` pour chaque élément
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);

        // Charger l'image dans l'ImageView en utilisant Glide
        Glide.with(context)
                .load(Uri.parse(photoUrl)) // Charger depuis l'URI
                .placeholder(R.drawable.placeholder_image) // Image de remplacement si non disponible
                .error(R.drawable.error_image) // Image en cas d'erreur
                .into(holder.photoImageView);

        // Définir une description (facultatif)
        holder.photoDescription.setText("Image " + (position + 1));
    }

    @Override
    public int getItemCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView; // Référence vers l'ImageView
        TextView photoDescription; // Référence vers le TextView pour la description

        ViewHolder(View itemView) {
            super(itemView);
            // Récupérer les vues depuis `item_card.xml`
            photoImageView = itemView.findViewById(R.id.photoImageView);
            photoDescription = itemView.findViewById(R.id.photoDescription);
        }
    }
}
