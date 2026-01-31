package ru.eltech.studproject.api;

import java.io.Serializable;

public class Films implements Serializable {
    private int id;
    private String title;

    public Films() {}

    public Films(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}