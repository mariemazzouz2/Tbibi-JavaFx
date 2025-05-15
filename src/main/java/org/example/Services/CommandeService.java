package org.example.Services;

import org.example.Entities.Commande;
import org.example.Entities.Produit;
import org.example.Entities.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing Commande entities
 */
public class CommandeService {
    private Connection connection;

    public CommandeService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // 1️⃣ Add an Order
    public void addCommande(Commande commande) throws SQLException {
        String query = "INSERT INTO commande (montant_total, date_commande, statut, user_id) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        statement.setFloat(1, commande.getMontantTotal());
        statement.setDate(2, Date.valueOf(commande.getDateCommande()));
        statement.setString(3, commande.getStatut());

        if (commande.getUser() != null) {
            statement.setInt(4, commande.getUser().getId());
        } else {
            statement.setInt(4, 1);
        }

        statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            commande.setId(rs.getInt(1));
        }

        // Save the associated products if any
        for (Produit produit : commande.getProduits()) {
            produit.setCommande(commande);
            ProduitService produitService = new ProduitService();

            // For new products
            if (produit.getId() == 0) {
                produitService.addProduit(produit);
            } else {
                // For existing products
                produitService.updateProduit(produit);
            }
        }
    }

    // 2️⃣ Retrieve All Orders
    public List<Commande> getAllCommandes() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Commande commande = new Commande();
                commande.setId(resultSet.getInt("id"));
                commande.setMontantTotal(resultSet.getFloat("montant_total"));
                commande.setDateCommande(resultSet.getDate("date_commande").toLocalDate());
                commande.setStatut(resultSet.getString("statut"));

                // Load user if exists
                int userId = resultSet.getInt("user_id");
                if (!resultSet.wasNull()) {
                    UtilisateurService utilisateurService = new UtilisateurService();
                    Utilisateur user = utilisateurService.getUtilisateurById(userId);
                    commande.setUser(user);
                }

                // Load related products
                ProduitService produitService = new ProduitService();
                List<Produit> produits = produitService.getProduitsByCommandeId(commande.getId());
                commande.setProduits(produits);

                commandes.add(commande);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commandes;
    }

    // 3️⃣ Get Order by ID
    public Commande getCommandeById(int id) {
        Commande commande = null;
        String query = "SELECT * FROM commande WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    commande = new Commande();
                    commande.setId(resultSet.getInt("id"));
                    commande.setMontantTotal(resultSet.getFloat("montant_total"));
                    commande.setDateCommande(resultSet.getDate("date_commande").toLocalDate());
                    commande.setStatut(resultSet.getString("statut"));

                    // Load user if exists
                    int userId = resultSet.getInt("user_id");
                    if (!resultSet.wasNull()) {
                        UtilisateurService utilisateurService = new UtilisateurService();
                        Utilisateur user = utilisateurService.getUtilisateurById(userId);
                        commande.setUser(user);
                    }

                    // Load related products
                    ProduitService produitService = new ProduitService();
                    List<Produit> produits = produitService.getProduitsByCommandeId(commande.getId());
                    commande.setProduits(produits);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commande;
    }

    // 4️⃣ Update an Order
    public void updateCommande(Commande commande) throws SQLException {
        String query = "UPDATE commande SET montant_total = ?, date_commande = ?, statut = ?, user_id = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setFloat(1, commande.getMontantTotal());
        statement.setDate(2, Date.valueOf(commande.getDateCommande()));
        statement.setString(3, commande.getStatut());

        if (commande.getUser() != null) {
            statement.setInt(4, commande.getUser().getId());
        } else {
            statement.setNull(4, Types.INTEGER);
        }

        statement.setInt(5, commande.getId());

        statement.executeUpdate();

        // Update the associated products
        ProduitService produitService = new ProduitService();
        List<Produit> existingProduits = produitService.getProduitsByCommandeId(commande.getId());

        // Remove products that are no longer associated with this order
        for (Produit existingProduit : existingProduits) {
            boolean found = false;
            for (Produit newProduit : commande.getProduits()) {
                if (existingProduit.getId() == newProduit.getId()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                existingProduit.setCommande(null);
                produitService.updateProduit(existingProduit);
            }
        }

        // Add or update products associated with this order
        for (Produit produit : commande.getProduits()) {
            produit.setCommande(commande);

            if (produit.getId() == 0) {
                produitService.addProduit(produit);
            } else {
                produitService.updateProduit(produit);
            }
        }
    }

    // 5️⃣ Delete an Order
    public void deleteCommande(int id) throws SQLException {
        // First, update all related products to remove the commande reference
        ProduitService produitService = new ProduitService();
        List<Produit> produits = produitService.getProduitsByCommandeId(id);

        for (Produit produit : produits) {
            produit.setCommande(null);
            produitService.updateProduit(produit);
        }

        // Then delete the order
        String query = "DELETE FROM commande WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    // 6️⃣ Count Orders
    public int countCommandes() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM commande";

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

    // 7️⃣ Search Orders by Status
    public List<Commande> searchCommandesByStatus(String status) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande WHERE statut = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, status);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Commande commande = new Commande();
                    commande.setId(resultSet.getInt("id"));
                    commande.setMontantTotal(resultSet.getFloat("montant_total"));
                    commande.setDateCommande(resultSet.getDate("date_commande").toLocalDate());
                    commande.setStatut(resultSet.getString("statut"));

                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commandes;
    }

    // 8️⃣ Get Orders by User
    public List<Commande> getCommandesByUserId(int userId) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Commande commande = new Commande();
                    commande.setId(resultSet.getInt("id"));
                    commande.setMontantTotal(resultSet.getFloat("montant_total"));
                    commande.setDateCommande(resultSet.getDate("date_commande").toLocalDate());
                    commande.setStatut(resultSet.getString("statut"));

                    // Load user
                    UtilisateurService utilisateurService = new UtilisateurService();
                    Utilisateur user = utilisateurService.getUtilisateurById(userId);
                    commande.setUser(user);

                    // Load related products
                    ProduitService produitService = new ProduitService();
                    List<Produit> produits = produitService.getProduitsByCommandeId(commande.getId());
                    commande.setProduits(produits);

                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commandes;
    }

    // 9️⃣ Get Orders by Date Range
    public List<Commande> getCommandesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande WHERE date_commande BETWEEN ? AND ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Commande commande = new Commande();
                    commande.setId(resultSet.getInt("id"));
                    commande.setMontantTotal(resultSet.getFloat("montant_total"));
                    commande.setDateCommande(resultSet.getDate("date_commande").toLocalDate());
                    commande.setStatut(resultSet.getString("statut"));

                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commandes;
    }

    // 🔟 Calculate Total Sales
    public float calculateTotalSales() {
        float total = 0;
        String query = "SELECT SUM(montant_total) FROM commande WHERE statut != 'Annulée'";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                total = resultSet.getFloat(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }
}