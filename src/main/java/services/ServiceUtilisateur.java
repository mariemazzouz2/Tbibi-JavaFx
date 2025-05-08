// src/main/java/services/ServiceUtilisateur.java
package services;

import entities.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements IService<Utilisateur> {
    private Connection connection;

    public ServiceUtilisateur() {
        connection = MyDataBase.getInstance().getConnection();
        if (connection == null) {
            System.err.println("La connexion à la base de données est null dans ServiceUtilisateur");
        }
    }

    @Override
    public void ajouter(Utilisateur utilisateur) throws SQLException {
        String req = "INSERT INTO utilisateur (nom, prenom, email, roles, password, telephone, adresse, date_naissance, sexe) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, utilisateur.getNom());
        ps.setString(2, utilisateur.getPrenom());
        ps.setString(3, utilisateur.getEmail());
        ps.setString(4, utilisateur.getRoles()); // JSON ou texte selon votre besoin
        ps.setString(5, utilisateur.getPassword());
        ps.setInt(6, utilisateur.getTelephone());
        ps.setString(7, utilisateur.getAdresse());
        ps.setDate(8, Date.valueOf(utilisateur.getDateNaissance()));
        ps.setString(9, utilisateur.getSexe());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            utilisateur.setId(rs.getInt(1));
        }
        System.out.println("Utilisateur ajouté avec ID : " + utilisateur.getId());
    }

    @Override
    public void modifier(Utilisateur utilisateur) throws SQLException {
        String req = "UPDATE utilisateur SET nom=?, prenom=?, email=?, roles=?, password=?, telephone=?, adresse=?, date_naissance=?, sexe=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, utilisateur.getNom());
        ps.setString(2, utilisateur.getPrenom());
        ps.setString(3, utilisateur.getEmail());
        ps.setString(4, utilisateur.getRoles());
        ps.setString(5, utilisateur.getPassword());
        ps.setInt(6, utilisateur.getTelephone());
        ps.setString(7, utilisateur.getAdresse());
        ps.setDate(8, Date.valueOf(utilisateur.getDateNaissance()));
        ps.setString(9, utilisateur.getSexe());
        ps.setInt(10, utilisateur.getId());
        ps.executeUpdate();
        System.out.println("Utilisateur modifié");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Utilisateur supprimé");
    }

    @Override
    public List<Utilisateur> afficher() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        while (rs.next()) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(rs.getInt("id"));
            utilisateur.setNom(rs.getString("nom"));
            utilisateur.setPrenom(rs.getString("prenom"));
            utilisateur.setEmail(rs.getString("email"));
            utilisateur.setRoles(rs.getString("roles"));
            utilisateur.setPassword(rs.getString("password"));
            utilisateur.setTelephone(rs.getInt("telephone"));
            utilisateur.setAdresse(rs.getString("adresse"));
            utilisateur.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
            utilisateur.setSexe(rs.getString("sexe"));
            utilisateurs.add(utilisateur);
        }
        return utilisateurs;
    }

    // Ajout de la méthode getById
    public Utilisateur getById(int id) throws SQLException {
        String query = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId(rs.getInt("id"));
                    utilisateur.setNom(rs.getString("nom"));
                    utilisateur.setPrenom(rs.getString("prenom"));
                    utilisateur.setEmail(rs.getString("email"));
                    utilisateur.setRoles(rs.getString("roles"));
                    utilisateur.setTelephone(rs.getInt("telephone"));
                    utilisateur.setAdresse(rs.getString("adresse"));
                    utilisateur.setDateNaissance(rs.getDate("date_naissance") != null ? rs.getDate("date_naissance").toLocalDate() : null);
                    utilisateur.setSexe(rs.getString("sexe"));
                    return utilisateur;
                }
            }
        }
        return null;
    }
    // Fetch all users who do not have a dossier medical record
    public List<Utilisateur> getUsersWithoutDossierMedical() throws SQLException {
        List<Utilisateur> users = new ArrayList<>();
        String query = """
            SELECT u.*
            FROM utilisateur u
            LEFT JOIN dossier_medical dm ON u.id = dm.utilisateur_id
            WHERE dm.id IS NULL
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                // Set other fields if needed (e.g., nom, prenom)
                users.add(user);
            }
        }
        return users;
    }
}