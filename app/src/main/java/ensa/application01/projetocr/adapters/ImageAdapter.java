package ensa.application01.projetocr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ensa.application01.projetocr.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imagePaths; // Liste des chemins des images
    private final OnDeleteImageListener onDeleteImageListener;

    public interface OnDeleteImageListener {
        void onDelete(String imagePath);
    }

    public ImageAdapter(List<String> imagePaths, OnDeleteImageListener onDeleteImageListener) {
        this.imagePaths = imagePaths;
        this.onDeleteImageListener = onDeleteImageListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        // Charger l'image dans l'ImageView
        Glide.with(holder.itemView.getContext())
                .load(imagePath)
                .into(holder.imageView);

        // Ajouter un listener au bouton de suppression
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteImageListener != null) {
                onDeleteImageListener.onDelete(imagePath);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCategoryView);
            deleteButton = itemView.findViewById(R.id.deleteCategoryImageButton);
        }
    }
}
