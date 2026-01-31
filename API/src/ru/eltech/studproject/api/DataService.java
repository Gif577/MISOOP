package ru.eltech.studproject.api;

import java.util.List;

public interface DataService {
    void ping();

    // Методы для работы с фильмами
    boolean addFilm(String title);
    boolean removeFilm(int id);
    List<Films> getAllFilms();
    void clearAllFilms();
    int getFilmsCount();
}