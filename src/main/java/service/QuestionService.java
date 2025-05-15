package service;

import models.Question;
import models.Utilisateur;
import enums.Specialite;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements IService<Question> {
    private final Connection connection;

    public QuestionService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Question question) throws SQLException {
        String sql = "INSERT INTO question (titre, contenu, specialite, image, visible, date_creation, patient_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, question.getTitre());
            ps.setString(2, question.getContenu());
            ps.setString(3, question.getSpecialite().name()); // Conversion enum -> String
            ps.setString(4, question.getImage());
            ps.setBoolean(5, question.isVisible());
            ps.setTimestamp(6, Timestamp.valueOf(question.getDateCreation()));
            ps.setInt(7, question.getPatient().getId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    question.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Question question) throws SQLException {
        String sql = "UPDATE question SET titre = ?, contenu = ?, specialite = ?, image = ?, visible = ? " +
                "WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, question.getTitre());
            ps.setString(2, question.getContenu());
            ps.setString(3, question.getSpecialite().name());
            ps.setString(4, question.getImage());
            ps.setBoolean(5, question.isVisible());
            ps.setInt(6, question.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM question WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Question> afficher() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, u.nom as patient_nom, u.prenom as patient_prenom " +
                "FROM question q " +
                "JOIN utilisateur u ON q.patient_id = u.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setTitre(rs.getString("titre"));
                question.setContenu(rs.getString("contenu"));

                // Conversion String -> enum
                Specialite specialite = Specialite.valueOf(rs.getString("specialite"));
                question.setSpecialite(specialite);

                question.setImage(rs.getString("image"));
                question.setVisible(rs.getBoolean("visible"));
                question.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

                Utilisateur patient = new Utilisateur();
                patient.setId(rs.getInt("patient_id"));
                patient.setNom(rs.getString("patient_nom"));
                patient.setPrenom(rs.getString("patient_prenom"));
                question.setPatient(patient);

                questions.add(question);
            }
        }
        return questions;
    }

    // Méthode supplémentaire pour récupérer une question par son ID
    public Question getById(int id) throws SQLException {
        String sql = "SELECT q.*, u.nom as patient_nom, u.prenom as patient_prenom " +
                "FROM question q " +
                "JOIN utilisateur u ON q.patient_id = u.id " +
                "WHERE q.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Question question = new Question();
                    question.setId(rs.getInt("id"));
                    question.setTitre(rs.getString("titre"));
                    question.setContenu(rs.getString("contenu"));

                    Specialite specialite = Specialite.valueOf(rs.getString("specialite"));
                    question.setSpecialite(specialite);

                    question.setImage(rs.getString("image"));
                    question.setVisible(rs.getBoolean("visible"));
                    question.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

                    Utilisateur patient = new Utilisateur();
                    patient.setId(rs.getInt("patient_id"));
                    patient.setNom(rs.getString("patient_nom"));
                    patient.setPrenom(rs.getString("patient_prenom"));
                    question.setPatient(patient);

                    return question;
                }
            }
        }
        return null;
    }

    // Nouvelle méthode pour récupérer les questions par patient ID
    public List<Question> getByPatientId(int patientId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, u.nom as patient_nom, u.prenom as patient_prenom " +
                "FROM question q " +
                "JOIN utilisateur u ON q.patient_id = u.id " +
                "WHERE q.patient_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setId(rs.getInt("id"));
                    question.setTitre(rs.getString("titre"));
                    question.setContenu(rs.getString("contenu"));

                    Specialite specialite = Specialite.valueOf(rs.getString("specialite"));
                    question.setSpecialite(specialite);

                    question.setImage(rs.getString("image"));
                    question.setVisible(rs.getBoolean("visible"));
                    question.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

                    Utilisateur patient = new Utilisateur();
                    patient.setId(rs.getInt("patient_id"));
                    patient.setNom(rs.getString("patient_nom"));
                    patient.setPrenom(rs.getString("patient_prenom"));
                    question.setPatient(patient);

                    questions.add(question);
                }
            }
        }
        return questions;
    }
}
