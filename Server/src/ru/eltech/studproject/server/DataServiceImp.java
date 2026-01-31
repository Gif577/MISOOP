package ru.eltech.studproject.server;

import ru.eltech.studproject.api.DataService;
import ru.eltech.studproject.api.Films;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataServiceImp implements DataService {

    private static final Logger LOGGER = Logger.getLogger(DataServiceImp.class.getName());

    static {
        Database.initDatabase();
    }

    @Override
    public void ping() {
        LOGGER.info("Колесо фортуны: сервер активен");
    }

    @Override
    public boolean addFilm(String title) {
        if (title == null || title.trim().isEmpty()) {
            LOGGER.warning("Попытка добавления пустого названия фильма");
            return false;
        }

        String sql = "INSERT INTO films (title) VALUES (?)";
        String trimmedTitle = title.trim();

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trimmedTitle);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Фильм добавлен: " + trimmedTitle);
                return true;
            } else {
                LOGGER.warning("Фильм не был добавлен: " + trimmedTitle);
                return false;
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                LOGGER.info("Фильм уже существует: " + trimmedTitle);
            } else {
                LOGGER.log(Level.SEVERE, "Ошибка при добавлении фильма: " + trimmedTitle, e);
            }
            return false;
        }
    }

    @Override
    public boolean removeFilm(int id) {
        if (id <= 0) {
            LOGGER.warning("Попытка удаления фильма с невалидным ID: " + id);
            return false;
        }

        String sql = "DELETE FROM films WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.info("Фильм удален ID: " + id);
                return true;
            } else {
                LOGGER.warning("Фильм с ID " + id + " не найден для удаления");
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при удалении фильма ID: " + id, e);
            return false;
        }
    }

    @Override
    public List<Films> getAllFilms() {
        List<Films> films = new ArrayList<>();
        String sql = "SELECT id, title FROM films ORDER BY id";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                films.add(new Films(id, title));
            }

            LOGGER.info("Загружено фильмов: " + films.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при получении фильмов", e);
        }
        return films;
    }

    @Override
    public void clearAllFilms() {
        String sql = "DELETE FROM films";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            int deletedCount = stmt.executeUpdate(sql);
            LOGGER.info("Все фильмы удалены. Удалено записей: " + deletedCount);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при очистке фильмов", e);
            throw new RuntimeException("Ошибка при очистке фильмов", e);
        }
    }

    @Override
    public int getFilmsCount() {
        String sql = "SELECT COUNT(*) as count FROM films";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int count = rs.getInt("count");
                LOGGER.fine("Текущее количество фильмов: " + count);
                return count;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при подсчете фильмов", e);
        }
        return 0;
    }

    // Дополнительный метод для поиска фильма по названию
    public Films findFilmByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, title FROM films WHERE title = ?";
        String trimmedTitle = title.trim();

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trimmedTitle);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Films(rs.getInt("id"), rs.getString("title"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при поиске фильма: " + trimmedTitle, e);
        }

        return null;
    }
}