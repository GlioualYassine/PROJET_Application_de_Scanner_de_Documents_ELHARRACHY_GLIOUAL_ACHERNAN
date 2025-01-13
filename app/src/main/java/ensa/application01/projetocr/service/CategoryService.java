package ensa.application01.projetocr.services;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.models.Category;

public class CategoryService {
    private static final String FILE_NAME = "categories.json";
    private final File storageFile;
    private final Gson gson;

    public CategoryService(Context context) {
        this.storageFile = new File(context.getFilesDir(), FILE_NAME);
        this.gson = new Gson();
    }
    private static CategoryService instance;

    public static synchronized CategoryService getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryService(context);
        }
        return instance;
    }

    // Récupérer toutes les catégories
    public List<Category> getCategories() {
        if (!storageFile.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(storageFile)) {
            Type listType = new TypeToken<List<Category>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public List<String> getCategoryPhotos(int categoryId) {
        // Récupérer la catégorie par son ID
        Category category = getCategoryById(categoryId);

        // Si la catégorie existe et a des images, retourner la liste des images
        if (category != null && category.getImages() != null) {
            return category.getImages();
        }

        // Si la catégorie n'existe pas ou n'a pas d'images, retourner une liste vide
        return new ArrayList<>();
    }
    // Ajouter une catégorie
    public void addCategory(Category category) {
        List<Category> categories = getCategories();
        categories.add(category);
        saveCategories(categories);
    }

    // Mettre à jour une catégorie
    public void updateCategory(int id, String newName, List<String> newImages) {
        List<Category> categories = getCategories();
        for (Category category : categories) {
            if (category.getId() == id) {
                category.setName(newName);
                category.setImages(newImages);
                break;
            }
        }
        saveCategories(categories);
    }

    // Supprimer une catégorie
    public void deleteCategory(int id) {
        List<Category> categories = getCategories();
        categories.removeIf(category -> category.getId() == id); // Supprime la catégorie par son ID
        saveCategories(categories); // Enregistre les modifications dans le fichier JSON
    }

    //get category by id
    public Category getCategoryById(int id) {
        List<Category> categories = getCategories();
        for (Category category : categories) {
            if (category.getId() == id) {
                return category;
            }
        }
        return null;
    }

    // Verifier si le nom existe
    public boolean isCategoryNameExists(String name) {
        List<Category> categories = getCategories();
        for (Category category : categories) {
            // Compare names case-insensitively and ignore leading/trailing spaces
            if (category.getName().trim().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    // Sauvegarder les catégories dans le fichier
    private void saveCategories(List<Category> categories) {
        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(categories, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
