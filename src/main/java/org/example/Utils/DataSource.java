package org.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for managing database connections
 */
public class DataSource {
    private String url = "jdbc:mysql://localhost:3306/tbibi2";
    private String username = "root";
    private String password = "";
    private Connection cnx;
    private static DataSource instance;

    /**
     * Private constructor to prevent instantiation
     * Establishes a connection to the database
     */
    private DataSource() {
        try {
            cnx = DriverManager.getConnection(url, username, password);
            System.out.println("connexion établie");
        } catch (SQLException ex) {
            Logger.getLogger(DataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the singleton instance of DataSource
     * If the instance doesn't exist, it creates one
     * @return DataSource instance
     */
    public static DataSource getInstance() {
        if (instance == null)
            instance = new DataSource();
        return instance;
    }

    /**
     * Returns the database connection
     * @return Connection object
     */
    public Connection getCnx() {
        return cnx;
    }
}