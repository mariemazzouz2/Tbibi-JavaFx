package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    final String URL = "jdbc:mysql://127.0.0.1:3306/tbibi_integration";
    final String USERNAME = "root";
    final String PASSWORD = ""; // Pas de mot de passe selon votre URL
    private Connection connection;
    private static MyDataBase instance;

    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion établie à MySQL");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}