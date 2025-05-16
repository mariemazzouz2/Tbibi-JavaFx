package services;

import entities.Consultation;
import entities.Ordonnance;
import entities.Utilisateur;
import exceptions.ValidationException;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceConsultation implements IService<Consultation> {
    private Connection connection;

    public ServiceConsultation() throws SQLException {
        connection = MyDataBase.getInstance().getConnection();
        System.out.println("ServiceConsultation instancié");
    }

    @Override
    public void ajouter(Consultation consultation) throws SQLException, ValidationException {
        // Check for scheduling conflicts
        if (hasSchedulingConflict(consultation)) {
            throw new ValidationException("Le médecin a déjà une consultation programmée à cette date et heure.");
        }

        String query = "INSERT INTO consultation (type, status, commentaire, date_c, meet_link, medecin_id, patient_id) VALUES (UPPER(?), ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, consultation.getType().toString());
            stmt.setString(2, consultation.getStatus());
            stmt.setString(3, consultation.getCommentaire());
            stmt.setTimestamp(4, Timestamp.valueOf(consultation.getDateC()));
            stmt.setString(5, consultation.getMeetLink());
            stmt.setInt(6, consultation.getMedecin().getId());
            stmt.setInt(7, consultation.getPatient().getId());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    consultation.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Consultation consultation) throws SQLException, ValidationException {
        // Check for scheduling conflicts
        if (hasSchedulingConflict(consultation)) {
            throw new ValidationException("Le médecin a déjà une consultation programmée à cette date et heure.");
        }

        String query = "UPDATE consultation SET type = UPPER(?), status = ?, commentaire = ?, date_c = ?, meet_link = ?, medecin_id = ?, patient_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, consultation.getType().toString());
            stmt.setString(2, consultation.getStatus());
            stmt.setString(3, consultation.getCommentaire());
            stmt.setTimestamp(4, Timestamp.valueOf(consultation.getDateC()));
            stmt.setString(5, consultation.getMeetLink());
            stmt.setInt(6, consultation.getMedecin().getId());
            stmt.setInt(7, consultation.getPatient().getId());
            stmt.setInt(8, consultation.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Vérifier d'abord s'il y a une ordonnance associée
        ServiceOrdonnance serviceOrdonnance = new ServiceOrdonnance();
        Ordonnance ordonnance = serviceOrdonnance.getByConsultationId(id);
        if (ordonnance != null) {
            // Supprimer l'ordonnance associée
            serviceOrdonnance.supprimer(ordonnance.getId());
        }

        // Supprimer la consultation
        String query = "DELETE FROM consultation WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Consultation> afficher() throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                consultations.add(extractConsultationFromResultSet(rs));
            }
        }
        return consultations;
    }

    public List<Consultation> afficherConsultation() throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                consultations.add(extractConsultationFromResultSet(rs));
            }
        }
        return consultations;
    }

    public Consultation getById(int id) throws SQLException {
        String query = "SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id " +
                "WHERE c.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractConsultationFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Consultation> getByPatientId(int patientId) throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id " +
                "WHERE c.patient_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultations.add(extractConsultationFromResultSet(rs));
                }
            }
        }
        return consultations;
    }

    public List<Consultation> getByMedecinId(int medecinId) throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id " +
                "WHERE c.medecin_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, medecinId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultations.add(extractConsultationFromResultSet(rs));
                }
            }
        }
        return consultations;
    }

    public List<Consultation> getByStatus(String status) throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id " +
                "WHERE c.status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultations.add(extractConsultationFromResultSet(rs));
                }
            }
        }
        return consultations;
    }

    private Consultation extractConsultationFromResultSet(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setId(rs.getInt("id"));

        // Handle legacy type values from database
        String typeStr = rs.getString("type").toUpperCase();

        consultation.setStatus(rs.getString("status"));
        consultation.setCommentaire(rs.getString("commentaire"));
        consultation.setDateC(rs.getTimestamp("date_c").toLocalDateTime());
        consultation.setMeetLink(rs.getString("meet_link"));

        // Create and set the doctor user
        Utilisateur medecin = new Utilisateur();
        medecin.setId(rs.getInt("medecin_id"));
        medecin.setNom(rs.getString("medecin_nom"));
        medecin.setPrenom(rs.getString("medecin_prenom"));
        consultation.setMedecin(medecin);

        // Create and set the patient user
        Utilisateur patient = new Utilisateur();
        patient.setId(rs.getInt("patient_id"));
        patient.setNom(rs.getString("patient_nom"));
        patient.setPrenom(rs.getString("patient_prenom"));
        consultation.setPatient(patient);

        // Check if an ordonnance is associated
        try {
            ServiceOrdonnance serviceOrdonnance = new ServiceOrdonnance();
            Ordonnance ordonnance = serviceOrdonnance.getByConsultationId(consultation.getId());
            if (ordonnance != null) {
                consultation.setOrdonnance(ordonnance);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'ordonnance : " + e.getMessage());
        }

        return consultation;
    }

    // Méthode pour filtrer et paginer les consultations
    // Méthode pour filtrer et paginer les consultations
    public List<Consultation> filterConsultations(String status, String type, LocalDateTime date, String searchText, String patientId, int page, int pageSize) throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT c.*, u1.id as medecin_id, u1.nom as medecin_nom, u1.prenom as medecin_prenom, " +
                "u2.id as patient_id, u2.nom as patient_nom, u2.prenom as patient_prenom " +
                "FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id " +
                "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // Add patient ID filter
        if (patientId != null && !patientId.isEmpty()) {
            query.append(" AND c.patient_id = ?");
            params.add(Integer.parseInt(patientId));
        }

        if (status != null && !status.isEmpty()) {
            query.append(" AND c.status = ?");
            params.add(status);
        }

        // Fix for type filter - same as in countConsultations
        if (type != null && !type.isEmpty()) {
            // Handle the special case for VIRTUELLE/EN LIGNE
            if (type.equalsIgnoreCase("VIRTUELLE")) {
                query.append(" AND (UPPER(c.type) = ? OR UPPER(c.type) = ?)");
                params.add("VIRTUELLE");
                params.add("EN LIGNE");
            } else {
                query.append(" AND UPPER(c.type) = UPPER(?)");
                params.add(type);
            }
        }

        if (date != null) {
            query.append(" AND DATE(c.date_c) = ?");
            params.add(Date.valueOf(date.toLocalDate()));
        }
        if (searchText != null && !searchText.isEmpty()) {
            query.append(" AND (CAST(c.id AS CHAR) LIKE ? OR c.commentaire LIKE ? OR u1.nom LIKE ? OR u1.prenom LIKE ?)");
            String searchPattern = "%" + searchText + "%";
            params.add(searchPattern); // pour id
            params.add(searchPattern); // pour commentaire
            params.add(searchPattern); // pour nom médecin
            params.add(searchPattern); // pour prénom médecin
        }

        // Ajouter la pagination
        query.append(" ORDER BY c.date_c DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Remplir les paramètres
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultations.add(extractConsultationFromResultSet(rs));
                }
            }
        }
        return consultations;
    }

    // Also update the count method to match the same filtering logic
    public int countConsultations(String status, String type, LocalDateTime date, String searchText, String doctorId) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM consultation c " +
                "JOIN utilisateur u1 ON c.medecin_id = u1.id " +
                "JOIN utilisateur u2 ON c.patient_id = u2.id " +
                "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Add doctor ID filter if provided
        if (doctorId != null && !doctorId.isEmpty()) {
            query.append(" AND c.medecin_id = ?");
            params.add(Integer.parseInt(doctorId));
        }

        // Add patient ID filter
        if (searchText != null && !searchText.isEmpty()) {
            query.append(" AND (CAST(c.id AS CHAR) LIKE ? OR c.commentaire LIKE ? OR u1.nom LIKE ? OR u1.prenom LIKE ?)");
            String searchPattern = "%" + searchText + "%";
            params.add(searchPattern); // pour id
            params.add(searchPattern); // pour commentaire
            params.add(searchPattern); // pour nom médecin
            params.add(searchPattern); // pour prénom médecin
        }

        if (status != null && !status.isEmpty()) {
            query.append(" AND c.status = ?");
            params.add(status);
        }

        // Fix for type filter - same as in filterConsultations
        if (type != null && !type.isEmpty()) {
            // Handle the special case for VIRTUELLE/EN LIGNE
            if (type.equalsIgnoreCase("VIRTUELLE")) {
                query.append(" AND (UPPER(c.type) = ? OR UPPER(c.type) = ?)");
                params.add("VIRTUELLE");
                params.add("EN LIGNE");
            } else {
                query.append(" AND UPPER(c.type) = UPPER(?)");
                params.add(type);
            }
        }

        if (date != null) {
            query.append(" AND DATE(c.date_c) = ?");
            params.add(Date.valueOf(date.toLocalDate()));
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Remplir les paramètres
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Méthode pour récupérer les statuts distincts (pour remplir le ChoiceBox)
    public List<String> getDistinctStatuses() throws SQLException {
        List<String> statuses = new ArrayList<>();
        String query = "SELECT DISTINCT status FROM consultation WHERE status IS NOT NULL";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                statuses.add(rs.getString("status"));
            }
        }
        return statuses;
    }

    // Méthode pour récupérer les types distincts (pour remplir le ChoiceBox)
    public List<String> getDistinctTypes() throws SQLException {
        List<String> types = new ArrayList<>();
        String query = "SELECT DISTINCT UPPER(type) as type FROM consultation WHERE type IS NOT NULL";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        }
        return types;
    }

    public List<Consultation> getByPatient(int patientId) throws SQLException {
        return getByPatientId(patientId);
    }

    private boolean hasSchedulingConflict(Consultation newConsultation) throws SQLException, ValidationException {
        // Validate required fields
        if (newConsultation == null) {
            throw new ValidationException("La consultation ne peut pas être null");
        }
        if (newConsultation.getMedecin() == null) {
            throw new ValidationException("Le médecin doit être spécifié");
        }
        if (newConsultation.getDateC() == null) {
            throw new ValidationException("La date de consultation doit être spécifiée");
        }

        // Query to check for overlapping consultations
        String query = "SELECT COUNT(*) FROM consultation " +
                      "WHERE medecin_id = ? " +
                      "AND DATE(date_c) = DATE(?) " +
                      "AND TIME(date_c) = TIME(?) ";
        
        // Only add ID check for existing consultations
        Integer consultationId = newConsultation.getId();
        if (consultationId != null) {
            query += "AND id != ?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newConsultation.getMedecin().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(newConsultation.getDateC()));
            stmt.setTimestamp(3, Timestamp.valueOf(newConsultation.getDateC()));
            
            // Set ID parameter only if it exists
            if (consultationId != null) {
                stmt.setInt(4, consultationId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
