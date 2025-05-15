package service;

import entities.CategorieEv;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CategorieEvService {
    private Connection connection;

    public CategorieEvService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // Ajouter une nouvelle catégorie
    public void ajouter(CategorieEv categorie) throws SQLException {
        String query = "INSERT INTO categorie_ev (nom, description) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, categorie.getNom());
        preparedStatement.setString(2, categorie.getDescription());
        preparedStatement.executeUpdate();
    }

    // Modifier une catégorie existante
    public void modifier(CategorieEv categorie) throws SQLException {
        String query = "UPDATE categorie_ev SET nom = ?, description = ? WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, categorie.getNom());
        preparedStatement.setString(2, categorie.getDescription());
        preparedStatement.setInt(3, categorie.getId());
        preparedStatement.executeUpdate();
    }

    // Supprimer une catégorie par son id
    public void supprimer(int id) throws SQLException {
        // La suppression en cascade est gérée par la base de données (ON DELETE CASCADE),
        // donc pas besoin de vérifier si la catégorie est utilisée dans des événements
        String deleteQuery = "DELETE FROM evenement WHERE categorie_id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, id);
            deleteStmt.executeUpdate();
        }

        // Ensuite, supprimer la catégorie
        String queryDeleteCategory = "DELETE FROM categorie_ev WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(queryDeleteCategory)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }

    }


    // Afficher toutes les catégories
    public List<CategorieEv> afficher() throws SQLException {
        List<CategorieEv> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie_ev";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                CategorieEv category = new CategorieEv(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
                categories.add(category);
            }
        }
        return categories;
    }

    // Récupérer une catégorie par son id
    public CategorieEv getById(int id) throws SQLException {
        String query = "SELECT * FROM categorie_ev WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new CategorieEv(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public List<CategorieEv> recuperer() throws SQLException {
        List<CategorieEv> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie_ev";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            CategorieEv categorie = new CategorieEv();
            categorie.setId(resultSet.getInt("id"));
            categorie.setNom(resultSet.getString("nom"));
            categories.add(categorie);
        }

        return categories;
    }

    public int getEventCountForCategory(int categoryId) throws SQLException {
        String query = "SELECT COUNT(*) FROM evenement WHERE categorie_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}