package service;

// Importations nécessaires
import models.Vote;
import models.Utilisateur;
import models.Reponse;
import utils.MyDataBase;
import enums.TypeVote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VoteService implements IService<Vote> {
    private Connection connection; // Connexion à la base de données

    // Constructeur initialisant la connexion à la DB
    public VoteService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    // Méthode pour ajouter un vote dans la base de données
    @Override
    public void ajouter(Vote vote) throws SQLException {
        // Requête SQL d'insertion
        String sql = "INSERT INTO vote (medecin_id, reponse_id, valeur) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Paramétrage des valeurs
            stmt.setInt(1, vote.getMedecin().getId());       // ID du médecin
            stmt.setInt(2, vote.getReponse().getId());        // ID de la réponse
            stmt.setString(3, vote.getValeur().name());       // Type de vote (Like/Dislike)
            stmt.executeUpdate();                             // Exécution de la requête
        }
    }

    // Méthode pour modifier un vote existant
    @Override
    public void modifier(Vote vote) throws SQLException {
        // Requête SQL de mise à jour
        String sql = "UPDATE vote SET medecin_id = ?, reponse_id = ?, valeur = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Paramétrage des valeurs
            stmt.setInt(1, vote.getMedecin().getId());
            stmt.setInt(2, vote.getReponse().getId());
            stmt.setString(3, vote.getValeur().name());
            stmt.setInt(4, vote.getId()); // ID du vote à modifier
            stmt.executeUpdate();
        }
    }

    // Méthode pour supprimer un vote
    @Override
    public void supprimer(int id) throws SQLException {
        // Requête SQL de suppression
        String sql = "DELETE FROM vote WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id); // ID du vote à supprimer
            stmt.executeUpdate();
        }
    }

    // Méthode pour récupérer tous les votes
    @Override
    public List<Vote> afficher() throws SQLException {
        List<Vote> votes = new ArrayList<>();
        // Requête SQL avec jointures pour récupérer les infos complètes
        String sql = "SELECT v.id, v.medecin_id, v.reponse_id, v.valeur, " +
                "u.nom, u.prenom, r.contenu " +
                "FROM vote v " +
                "JOIN utilisateur u ON v.medecin_id = u.id " +
                "JOIN reponse r ON v.reponse_id = r.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Parcours des résultats
            while (rs.next()) {
                Vote vote = new Vote();
                vote.setId(rs.getInt("id"));

                // Création de l'objet Médecin
                Utilisateur medecin = new Utilisateur();
                medecin.setId(rs.getInt("medecin_id"));
                medecin.setNom(rs.getString("nom"));
                medecin.setPrenom(rs.getString("prenom"));
                vote.setMedecin(medecin);

                // Création de l'objet Réponse
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("reponse_id"));
                reponse.setContenu(rs.getString("contenu"));
                vote.setReponse(reponse);

                // Conversion de la valeur String en enum TypeVote
                vote.setValeur(TypeVote.valueOf(rs.getString("valeur")));
                votes.add(vote);
            }
        }
        return votes;
    }

    // Méthode pour récupérer un vote spécifique d'un utilisateur sur une réponse
    public Vote getVoteByUserAndResponse(int medecinId, int reponseId) throws SQLException {
        // Requête SQL avec jointures et conditions
        String sql = "SELECT v.id, v.medecin_id, v.reponse_id, v.valeur, " +
                "u.nom, u.prenom, r.contenu " +
                "FROM vote v " +
                "JOIN utilisateur u ON v.medecin_id = u.id " +
                "JOIN reponse r ON v.reponse_id = r.id " +
                "WHERE v.medecin_id = ? AND v.reponse_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);    // ID du médecin
            stmt.setInt(2, reponseId);    // ID de la réponse

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { // Si un vote est trouvé
                    Vote vote = new Vote();
                    vote.setId(rs.getInt("id"));

                    Utilisateur medecin = new Utilisateur();
                    medecin.setId(rs.getInt("medecin_id"));
                    medecin.setNom(rs.getString("nom"));
                    medecin.setPrenom(rs.getString("prenom"));
                    vote.setMedecin(medecin);

                    Reponse reponse = new Reponse();
                    reponse.setId(rs.getInt("reponse_id"));
                    reponse.setContenu(rs.getString("contenu"));
                    vote.setReponse(reponse);

                    vote.setValeur(TypeVote.valueOf(rs.getString("valeur")));
                    return vote;
                }
            }
        }
        return null; // Aucun vote trouvé
    }

    // Méthode pour compter les "Like" d'une réponse
    public long getLikeCount(int reponseId) throws SQLException {
        // Requête de comptage avec condition sur le type de vote
        String sql = "SELECT COUNT(*) FROM vote WHERE reponse_id = ? AND valeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reponseId);                     // ID de la réponse
            stmt.setString(2, TypeVote.Like.name());       // Type Like
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1); // Retourne le nombre de Like
                }
            }
        }
        return 0; // Retourne 0 si erreur ou aucun Like
    }

    // Méthode pour compter les "Dislike" d'une réponse
    public long getDislikeCount(int reponseId) throws SQLException {
        // Même principe que getLikeCount mais pour les Dislike
        String sql = "SELECT COUNT(*) FROM vote WHERE reponse_id = ? AND valeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reponseId);
            stmt.setString(2, TypeVote.Dislike.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }
}