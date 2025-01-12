package ensa.application01.projetocr.models;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private int id;
    private String name;
    private List<String> images;

    public Category(int id, String name, List<String> images) {
        this.id = id;
        this.name = name;
        this.images = images;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getImages() {
        return images != null ? images : new ArrayList<>();
    }
    public void setImages(List<String> images) { this.images = images; }
}
