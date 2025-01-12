package ensa.application01.projetocr.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.R;

public class CategoryPhotoAdapter extends RecyclerView.Adapter<CategoryPhotoAdapter.ViewHolder> {
    private List<String> photoUrls;
    private final Context context;

    public CategoryPhotoAdapter(Context context) {
        this.context = context;
        this.photoUrls = new ArrayList<>(); // Initialize with empty list
    }

    public void setPhotos(List<String> photos) {
        this.photoUrls = photos != null ? photos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);
        // Utilisez Glide ou Picasso pour charger l'image
        // Glide.with(context).load(photoUrl).into(holder.imageView);
        holder.imageView.setImageURI(Uri.parse(photoUrl));
    }

    @Override
    public int getItemCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
        }
    }
}