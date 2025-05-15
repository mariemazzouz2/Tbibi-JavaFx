package service;

import models.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {
    private final Connection connection;

    public UtilisateurDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void ajouterUtilisateur(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, password, roles, telephone, adresse, date_naissance, sexe, taille, poids, specialite, diplome,image,status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, user.getNom());
        stmt.setString(2, user.getPrenom());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPassword());
        stmt.setString(5, user.getRoles());
        stmt.setInt(6, user.getTelephone());
        stmt.setString(7, user.getAdresse());
        stmt.setDate(8, java.sql.Date.valueOf(user.getDateNaissance()));
        stmt.setString(9, user.getSexe());
        stmt.setObject(10, user.getTaille()); // peut être null
        stmt.setObject(11, user.getPoids());  // peut être null
        stmt.setString(12, user.getSpecialite());
        stmt.setString(13, user.getDiplome());
        stmt.setString(14, user.getImage());
        stmt.setInt(15, user.getStatus() != null ? user.getStatus() : 0);

        stmt.executeUpdate();
        System.out.println("Utilisateur inséré avec succès !");
    }
    public Utilisateur getUserByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRoles(rs.getString("roles"));
                u.setTelephone(rs.getInt("telephone"));
                u.setAdresse(rs.getString("adresse"));
                u.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
                u.setSexe(rs.getString("sexe"));
                u.setTaille(rs.getObject("taille") != null ? rs.getDouble("taille") : null);
                u.setPoids(rs.getObject("poids") != null ? rs.getInt("poids") : null);
                u.setSpecialite(rs.getString("specialite"));
                u.setDiplome(rs.getString("diplome"));
                u.setImage(rs.getString("image"));
                u.setStatus(rs.getInt("status")); // ✅ ici
                return u;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Utilisateur> getUtilisateursAvecStatus(int status) throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE status = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, status);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Utilisateur u = new Utilisateur();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setSpecialite(rs.getString("specialite"));
            u.setDiplome(rs.getString("diplome")); // Ajouté ici
            list.add(u);
        }
        return list;
    }
    public List<Utilisateur> getAllUsers() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";

        try (PreparedStatement stmt = MyDataBase.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setPrenom(rs.getString("prenom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setPassword(rs.getString("password"));
                utilisateur.setRoles(rs.getString("roles"));
                utilisateur.setTelephone(rs.getInt("telephone"));
                utilisateur.setAdresse(rs.getString("adresse"));
                utilisateur.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
                utilisateur.setSexe(rs.getString("sexe"));
                utilisateur.setTaille(rs.getObject("taille") != null ? rs.getDouble("taille") : null);
                utilisateur.setPoids(rs.getObject("poids") != null ? rs.getInt("poids") : null);
                utilisateur.setSpecialite(rs.getString("specialite"));
                utilisateur.setDiplome(rs.getString("diplome"));
                utilisateur.setImage(rs.getString("image"));
                utilisateur.setStatus(rs.getInt("status"));
                utilisateurs.add(utilisateur);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return utilisateurs;
    }

    public void updateUtilisateur(Utilisateur user) throws SQLException {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, password = ?, roles = ?, telephone = ?, adresse = ?, date_naissance = ?, sexe = ?, taille = ?, poids = ?, specialite = ?, diplome = ?, image = ?, status = ? WHERE id = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, user.getNom());
        stmt.setString(2, user.getPrenom());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPassword());
        stmt.setString(5, formatRolesAsJson(user.getRoles()));
        stmt.setInt(6, user.getTelephone());
        stmt.setString(7, user.getAdresse());
        stmt.setDate(8, java.sql.Date.valueOf(user.getDateNaissance()));
        stmt.setString(9, user.getSexe());
        stmt.setObject(10, user.getTaille());
        stmt.setObject(11, user.getPoids());
        stmt.setString(12, user.getSpecialite());
        stmt.setString(13, user.getDiplome());
        stmt.setString(14, user.getImage());
        stmt.setInt(15, user.getStatus() != null ? user.getStatus() : 0);
        stmt.setInt(16, user.getId());

        stmt.executeUpdate();
        System.out.println("Utilisateur modifié avec succès !");
    }
    public void supprimerUtilisateur(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void accepterUtilisateur(int id) {
        String sql = "UPDATE utilisateur SET status = 1 WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Utilisateur accepté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private String formatRolesAsJson(String roles) {
        // Si ce n'est pas déjà un tableau JSON, on l'entoure
        if (!roles.trim().startsWith("[") && !roles.trim().endsWith("]")) {
            return "[\"" + roles + "\"]";
        }
        return roles;
    }
    public void updateUserPassword(Utilisateur user) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            String query = "UPDATE utilisateur SET password = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getPassword());
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierUtilisateur(Utilisateur user) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, roles = ?, telephone = ?, adresse = ?, date_naissance = ?, sexe = ?, taille = ?, poids = ?, specialite = ?, diplome = ?, image = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRoles());
            stmt.setInt(5, user.getTelephone());
            stmt.setString(6, user.getAdresse());
            stmt.setDate(7, java.sql.Date.valueOf(user.getDateNaissance()));
            stmt.setString(8, user.getSexe());
            stmt.setObject(9, user.getTaille());
            stmt.setObject(10, user.getPoids());
            stmt.setString(11, user.getSpecialite());
            stmt.setString(12, user.getDiplome());
            stmt.setString(13, user.getImage());
            stmt.setInt(14, user.getStatus() != null ? user.getStatus() : 0);
            stmt.setInt(15, user.getId());

            stmt.executeUpdate();
            System.out.println("Utilisateur mis à jour avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
