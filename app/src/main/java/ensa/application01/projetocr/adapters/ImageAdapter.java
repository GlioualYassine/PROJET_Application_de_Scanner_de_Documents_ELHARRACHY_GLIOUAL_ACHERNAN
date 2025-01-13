package ensa.application01.projetocr.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ensa.application01.projetocr.CameraActivity;
import ensa.application01.projetocr.OCRActivity;
import ensa.application01.projetocr.PDFActivity;
import ensa.application01.projetocr.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imagePaths;
    private final Context context;
    private final OnDeleteImageListener onDeleteImageListener;

    public interface OnDeleteImageListener {
        void onDelete(String imagePath);
    }

    public ImageAdapter(Context context, List<String> imagePaths, OnDeleteImageListener onDeleteImageListener) {
        this.context = context;
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

        // Load the image into the ImageView using Glide
        Glide.with(context)
                .load(imagePath)
                .into(holder.imageView);

        // Set up the dropdown menu (PopupMenu)
        holder.dropdownMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.categories_menu, popupMenu.getMenu());

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.option_convert_pdf) {
                    Intent pdfIntent = new Intent(context, PDFActivity.class);
                    pdfIntent.putExtra("photoUri", imagePath);
                    context.startActivity(pdfIntent);
                    return true;

                } else if (itemId == R.id.option_ocr) {
                    Intent ocrIntent = new Intent(context, OCRActivity.class);
                    ocrIntent.putExtra("photoUri", imagePath);
                    context.startActivity(ocrIntent);
                    return true;

                } else if (itemId == R.id.option_cloud) {
                    // Retrieve SharedPreferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

                    // Get the existing SyncedImages list
                    Set<String> syncedImages = sharedPreferences.getStringSet("SyncedImages", new HashSet<>());

                    // Add the current image to the list
                    syncedImages.add(imagePath);

                    // Save the updated list back to SharedPreferences
                    sharedPreferences.edit().putStringSet("SyncedImages", syncedImages).apply();

                    // Show a toast confirming the action
                    Toast.makeText(context, "Image synchronisée avec le cloud.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (itemId == R.id.option_delete) {
                    if (onDeleteImageListener != null) {
                        onDeleteImageListener.onDelete(imagePath);
                    }
                    Toast.makeText(context, "Image deleted.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView dropdownMenuButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCategoryView);
            dropdownMenuButton = itemView.findViewById(R.id.dropdownMenuButton);
        }
    }
}
