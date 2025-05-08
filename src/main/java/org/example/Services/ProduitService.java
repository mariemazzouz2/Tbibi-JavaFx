package org.example.Services;

import org.example.Entities.Produit;
import org.example.Entities.Commande;
import org.example.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing Produit entities
 */
public class ProduitService {
    private Connection connection;

    public ProduitService() {
        connection = DataSource.getInstance().getCnx();
    }

    // 1️⃣ Add a Product
    public void addProduit(Produit produit) throws SQLException {
        String query = "INSERT INTO produit (prix, type, image, nom, description, commande_id) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        statement.setFloat(1, produit.getPrix());
        statement.setString(2, produit.getType());
        statement.setString(3, produit.getImage());
        statement.setString(4, produit.getNom());
        statement.setString(5, produit.getDescription());

        if (produit.getCommande() != null) {
            statement.setInt(6, produit.getCommande().getId());
        } else {
            statement.setNull(6, Types.INTEGER);
        }

        statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            produit.setId(rs.getInt(1));
        }
    }

    // 2️⃣ Retrieve All Products
    public List<Produit> getAllProduits() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Produit produit = new Produit();
                produit.setId(resultSet.getInt("id"));
                produit.setPrix(resultSet.getFloat("prix"));
                produit.setType(resultSet.getString("type"));
                produit.setImage(resultSet.getString("image"));
                produit.setNom(resultSet.getString("nom"));
                produit.setDescription(resultSet.getString("description"));

                // Load related commande if exists
                int commandeId = resultSet.getInt("commande_id");
                if (!resultSet.wasNull()) {
                    CommandeService commandeService = new CommandeService();
                    Commande commande = commandeService.getCommandeById(commandeId);
                    produit.setCommande(commande);
                }

                produits.add(produit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    // 3️⃣ Get Product by ID
    public Produit getProduitById(int id) {
        Produit produit = null;
        String query = "SELECT * FROM produit WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    produit = new Produit();
                    produit.setId(resultSet.getInt("id"));
                    produit.setPrix(resultSet.getFloat("prix"));
                    produit.setType(resultSet.getString("type"));
                    produit.setImage(resultSet.getString("image"));
                    produit.setNom(resultSet.getString("nom"));
                    produit.setDescription(resultSet.getString("description"));

                    // Load related commande if exists
                    int commandeId = resultSet.getInt("commande_id");
                    if (!resultSet.wasNull()) {
                        CommandeService commandeService = new CommandeService();
                        Commande commande = commandeService.getCommandeById(commandeId);
                        produit.setCommande(commande);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produit;
    }

    // 4️⃣ Update a Product
    public void updateProduit(Produit produit) throws SQLException {
        String query = "UPDATE produit SET prix = ?, type = ?, image = ?, nom = ?, description = ?, commande_id = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setFloat(1, produit.getPrix());
        statement.setString(2, produit.getType());
        statement.setString(3, produit.getImage());
        statement.setString(4, produit.getNom());
        statement.setString(5, produit.getDescription());

        if (produit.getCommande() != null) {
            statement.setInt(6, produit.getCommande().getId());
        } else {
            statement.setNull(6, Types.INTEGER);
        }

        statement.setInt(7, produit.getId());

        statement.executeUpdate();
    }

    // 5️⃣ Delete a Product
    public void deleteProduit(int id) throws SQLException {
        String query = "DELETE FROM produit WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    // 6️⃣ Count Products
    public int countProduits() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM produit";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    // 7️⃣ Search Products by Name
    public List<Produit> searchProduitsByName(String keyword) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE nom LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + keyword + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Produit produit = new Produit();
                    produit.setId(resultSet.getInt("id"));
                    produit.setPrix(resultSet.getFloat("prix"));
                    produit.setType(resultSet.getString("type"));
                    produit.setImage(resultSet.getString("image"));
                    produit.setNom(resultSet.getString("nom"));
                    produit.setDescription(resultSet.getString("description"));

                    produits.add(produit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // 8️⃣ Get Products by Type
    public List<Produit> getProduitsByType(String type) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE type = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, type);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Produit produit = new Produit();
                    produit.setId(resultSet.getInt("id"));
                    produit.setPrix(resultSet.getFloat("prix"));
                    produit.setType(resultSet.getString("type"));
                    produit.setImage(resultSet.getString("image"));
                    produit.setNom(resultSet.getString("nom"));
                    produit.setDescription(resultSet.getString("description"));

                    produits.add(produit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // 9️⃣ Get Products by Commande
    public List<Produit> getProduitsByCommandeId(int commandeId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE commande_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, commandeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Produit produit = new Produit();
                    produit.setId(resultSet.getInt("id"));
                    produit.setPrix(resultSet.getFloat("prix"));
                    produit.setType(resultSet.getString("type"));
                    produit.setImage(resultSet.getString("image"));
                    produit.setNom(resultSet.getString("nom"));
                    produit.setDescription(resultSet.getString("description"));

                    produits.add(produit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }
}