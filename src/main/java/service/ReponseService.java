package service;


import com.sun.mail.iap.Response;
import models.Question;
import models.Reponse;
import models.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService implements IService<Reponse> {
    private final Connection connection;

    public ReponseService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reponse reponse) throws SQLException {
        String sql = "INSERT INTO reponse (question_id, medecin_id, contenu, date_reponse) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reponse.getQuestion().getId());
            ps.setInt(2, reponse.getMedecin().getId());
            ps.setString(3, reponse.getContenu());
            ps.setTimestamp(4, Timestamp.valueOf(reponse.getDateReponse()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    reponse.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Reponse reponse) throws SQLException {
        String sql = "UPDATE reponse SET contenu = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reponse.getContenu());
            ps.setInt(2, reponse.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reponse WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    public List<Reponse> afficherByQuestion(int questionId) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT r.*, u.nom as medecin_nom, u.prenom as medecin_prenom " +
                "FROM reponse r " +
                "JOIN utilisateur u ON r.medecin_id = u.id " +
                "WHERE r.question_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setContenu(rs.getString("contenu"));
                reponse.setDateReponse(rs.getTimestamp("date_reponse").toLocalDateTime());

                // Création de l'objet Médecin
                Utilisateur medecin = new Utilisateur();
                medecin.setId(rs.getInt("medecin_id"));
                medecin.setNom(rs.getString("medecin_prenom"));      // Ajout du nom
                medecin.setPrenom(rs.getString("medecin_prenom"));
                reponse.setMedecin(medecin);

                // Création de l'objet Question (juste l'ID)
                Question question = new Question();
                question.setId(questionId);
                reponse.setQuestion(question);

                reponses.add(reponse);
            }
        }
        return reponses;
    }
    @Override
    public List<Reponse> afficher() throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT r.*, u.nom as medecin_nom, u.prenom as medecin_prenom, q.titre as question_titre " +
                "FROM reponse r " +
                "JOIN utilisateur u ON r.medecin_id = u.id " +
                "JOIN question q ON r.question_id = q.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Reponse reponse = mapResultSetToReponse(rs);
                reponses.add(reponse);
            }
        }
        return reponses;
    }

    // Méthode pour récupérer les réponses d'une question spécifique
    public List<Reponse> getReponsesByQuestion(int questionId) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT r.*, u.nom as medecin_nom, u.prenom as medecin_prenom " +
                "FROM reponse r " +
                "JOIN utilisateur u ON r.medecin_id = u.id " +
                "WHERE r.question_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reponse reponse = mapResultSetToReponse(rs);
                    reponses.add(reponse);
                }
            }
        }
        return reponses;
    }

    // Méthode pour récupérer une réponse par son ID
    public Reponse getById(int id) throws SQLException {
        String sql = "SELECT r.*, u.nom as medecin_nom, u.prenom as medecin_prenom, q.titre as question_titre " +
                "FROM reponse r " +
                "JOIN utilisateur u ON r.medecin_id = u.id " +
                "JOIN question q ON r.question_id = q.id " +
                "WHERE r.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReponse(rs);
                }
            }
        }
        return null;
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet Reponse
    private Reponse mapResultSetToReponse(ResultSet rs) throws SQLException {
        Reponse reponse = new Reponse();
        reponse.setId(rs.getInt("id"));

        // Création de l'objet Question
        Question question = new Question();
        question.setId(rs.getInt("question_id"));
        question.setTitre(rs.getString("question_titre"));
        reponse.setQuestion(question);

        // Création de l'objet Utilisateur (médecin)
        Utilisateur medecin = new Utilisateur();
        medecin.setId(rs.getInt("medecin_id"));
        medecin.setNom(rs.getString("medecin_nom"));
        medecin.setPrenom(rs.getString("medecin_prenom"));
        reponse.setMedecin(medecin);

        reponse.setContenu(rs.getString("contenu"));
        reponse.setDateReponse(rs.getTimestamp("date_reponse").toLocalDateTime());

        return reponse;
    }
}



