package ru.eltech.studproject.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Kino";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "0000";

    private static Connection connection;
    private static boolean driverLoaded = false;

    static {
        loadDriver();
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            driverLoaded = true;
            System.out.println("PostgreSQL драйвер загружен!");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver не найден!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("PostgreSQL драйвер не загружен");
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public static void initDatabase() {
        if (!driverLoaded) return;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Создаем таблицу только с названием фильма
            String sql = """
                CREATE TABLE IF NOT EXISTS films (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL UNIQUE
                )
                """;
            stmt.executeUpdate(sql);
            System.out.println("Таблица films создана/проверена");

        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }
}