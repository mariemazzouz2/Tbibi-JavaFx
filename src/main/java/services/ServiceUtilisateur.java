// src/main/java/services/ServiceUtilisateur.java
package services;

import entities.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ServiceUtilisateur implements IService<Utilisateur> {
    private Connection connection;

    public ServiceUtilisateur() throws SQLException {
        try {
            connection = MyDataBase.getInstance().getConnection();
            System.out.println("ServiceUtilisateur instancié");
        } catch (RuntimeException e) {
            System.err.println("Erreur lors de l'initialisation de ServiceUtilisateur: " + e.getMessage());
            throw new SQLException("Erreur de connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(Utilisateur utilisateur) throws SQLException {
        String query = "INSERT INTO utilisateur (nom, prenom, email, roles, password, telephone, adresse, date_naissance, sexe, taille, poids, image, status, diplome, specialite) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getRoles());
            stmt.setString(5, utilisateur.getPassword());
            stmt.setInt(6, utilisateur.getTelephone());
            stmt.setString(7, utilisateur.getAdresse());
            stmt.setObject(8, utilisateur.getDateNaissance());
            stmt.setString(9, utilisateur.getSexe());
            stmt.setObject(10, utilisateur.getTaille());
            stmt.setObject(11, utilisateur.getPoids());
            stmt.setString(12, utilisateur.getImage());
            stmt.setObject(13, utilisateur.getStatus());
            stmt.setString(14, utilisateur.getDiplome());
            stmt.setString(15, utilisateur.getSpecialite());

            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    utilisateur.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Utilisateur utilisateur) throws SQLException {
        String query = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, roles = ?, password = ?, telephone = ?, adresse = ?, date_naissance = ?, sexe = ?, taille = ?, poids = ?, image = ?, status = ?, diplome = ?, specialite = ?, face_encoding = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getRoles());
            stmt.setString(5, utilisateur.getPassword());
            stmt.setInt(6, utilisateur.getTelephone());
            stmt.setString(7, utilisateur.getAdresse());
            stmt.setObject(8, utilisateur.getDateNaissance());
            stmt.setString(9, utilisateur.getSexe());
            stmt.setObject(10, utilisateur.getTaille());
            stmt.setObject(11, utilisateur.getPoids());
            stmt.setString(12, utilisateur.getImage());
            stmt.setObject(13, utilisateur.getStatus());
            stmt.setString(14, utilisateur.getDiplome());
            stmt.setString(15, utilisateur.getSpecialite());
            stmt.setString(16, utilisateur.getFaceEncoding());
            stmt.setInt(17, utilisateur.getId());
            
            stmt.executeUpdate();
        }
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
    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Utilisateur> afficher() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                utilisateurs.add(extractUtilisateurFromResultSet(rs));
            }
        }
        System.out.println(utilisateurs);
        return utilisateurs;
    }
    
    public Utilisateur getById(int id) throws SQLException {
        String query = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUtilisateurFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public Utilisateur getByEmail(String email) throws SQLException {
        String query = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUtilisateurFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    // Méthode pour récupérer la liste des médecins (utilisateurs avec rôle ROLE_MEDECIN)
    public List<Utilisateur> getMedecinsList() throws SQLException {
        List<Utilisateur> medecins = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE roles LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%ROLE_MEDECIN%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medecins.add(extractUtilisateurFromResultSet(rs));
                }
            }
        }
        return medecins;
    }

    private Utilisateur extractUtilisateurFromResultSet(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setRoles(rs.getString("roles"));
        utilisateur.setPassword(rs.getString("password"));
        utilisateur.setTelephone(rs.getInt("telephone"));
        utilisateur.setAdresse(rs.getString("adresse"));
        
        Object dateObj = rs.getObject("date_naissance");
        if (dateObj != null) {
            utilisateur.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
        }
        
        utilisateur.setSexe(rs.getString("sexe"));
        
        Double taille = rs.getObject("taille") != null ? rs.getDouble("taille") : null;
        utilisateur.setTaille(taille);
        
        Integer poids = rs.getObject("poids") != null ? rs.getInt("poids") : null;
        utilisateur.setPoids(poids);
        
        utilisateur.setImage(rs.getString("image"));
        
        Integer status = rs.getObject("status") != null ? rs.getInt("status") : null;
        utilisateur.setStatus(status);
        
        utilisateur.setDiplome(rs.getString("diplome"));
        utilisateur.setSpecialite(rs.getString("specialite"));

        return utilisateur;
    }
    
    // Getter pour la connexion (utilisé par certains services)
    public Connection getConnection() {
        return connection;
    }
}