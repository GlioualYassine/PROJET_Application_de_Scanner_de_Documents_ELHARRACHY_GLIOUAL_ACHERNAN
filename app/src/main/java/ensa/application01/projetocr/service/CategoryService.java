package ensa.application01.projetocr.service;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ensa.application01.projetocr.models.Category;

public class CategoryService {
    private static final String FILE_NAME = "categories.json";
    private final File categoryFile;

    public CategoryService(Context context) {
        categoryFile = new File(context.getFilesDir(), FILE_NAME);
    }

    // Lire les catégories depuis le fichier JSON
    public List<Category> getCategories() {
        if (!categoryFile.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(categoryFile)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Category>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Sauvegarder les catégories dans le fichier JSON
    private void saveCategories(List<Category> categories) {
        try (FileWriter writer = new FileWriter(categoryFile)) {
            Gson gson = new Gson();
            gson.toJson(categories, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Ajouter une catégorie
    public void addCategory(Category category) {
        List<Category> categories = getCategories();
        categories.add(category);
        saveCategories(categories);
    }

    // Supprimer une catégorie
    public void deleteCategory(Category category) {
        List<Category> categories = getCategories();
        categories.removeIf(c -> c.getId() == category.getId());
        saveCategories(categories);
    }

    // Mettre à jour une catégorie
    public void updateCategory(Category updatedCategory) {
        List<Category> categories = getCategories();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == updatedCategory.getId()) {
                categories.set(i, updatedCategory);
                break;
            }
        }
        saveCategories(categories);
    }
}
