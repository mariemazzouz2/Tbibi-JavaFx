package org.example.Services;

import org.example.Entities.Utilisateur;
import org.example.Entities.Commande;
import org.example.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing Utilisateur entities
 */
public class UtilisateurService {
    private Connection connection;

    public UtilisateurService() {
        connection = DataSource.getInstance().getCnx();
    }

    // 1️⃣ Add a User
    public void addUtilisateur(Utilisateur utilisateur) throws SQLException {
        String query = "INSERT INTO utilisateur (nom, email) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, utilisateur.getName());
        statement.setString(2, utilisateur.getEmail());

        statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            utilisateur.setId(rs.getInt(1));
        }
    }

    // 2️⃣ Retrieve All Users
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(resultSet.getInt("id"));
                utilisateur.setName(resultSet.getString("nom"));
                utilisateur.setEmail(resultSet.getString("email"));

                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }

    // 3️⃣ Get User by ID
    public Utilisateur getUtilisateurById(int id) {
        Utilisateur utilisateur = null;
        String query = "SELECT * FROM utilisateur WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId(resultSet.getInt("id"));
                    utilisateur.setName(resultSet.getString("nom"));
                    utilisateur.setEmail(resultSet.getString("email"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateur;
    }

    // 4️⃣ Update a User
    public void updateUtilisateur(Utilisateur utilisateur) throws SQLException {
        String query = "UPDATE utilisateur SET nom = ?, email = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, utilisateur.getName());
        statement.setString(2, utilisateur.getEmail());
        statement.setInt(3, utilisateur.getId());

        statement.executeUpdate();
    }

    // 5️⃣ Delete a User
    public void deleteUtilisateur(int id) throws SQLException {
        // First, remove all related commandes or set their user_id to null
        String updateCommandeQuery = "UPDATE commande SET user_id = NULL WHERE user_id = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateCommandeQuery)) {
            updateStatement.setInt(1, id);
            updateStatement.executeUpdate();
        }

        // Then delete the user
        String query = "DELETE FROM utilisateur WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    // 6️⃣ Count Users
    public int countUtilisateurs() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM utilisateur";

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

    // 7️⃣ Search Users by Name
    public List<Utilisateur> searchUtilisateursByName(String keyword) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE nom LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + keyword + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId(resultSet.getInt("id"));
                    utilisateur.setName(resultSet.getString("nom"));
                    utilisateur.setEmail(resultSet.getString("email"));

                    utilisateurs.add(utilisateur);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return utilisateurs;
    }

    // 8️⃣ Get User's Orders
    public List<Commande> getUserCommandes(int userId) {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande WHERE user_id = ?";
        CommandeService commandeService = new CommandeService();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int commandeId = resultSet.getInt("id");
                    Commande commande = commandeService.getCommandeById(commandeId);
                    if (commande != null) {
                        commandes.add(commande);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commandes;
    }
}