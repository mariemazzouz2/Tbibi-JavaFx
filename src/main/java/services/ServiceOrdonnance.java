package services;

import entities.Consultation;
import entities.Ordonnance;
import entities.Utilisateur;
import exceptions.ValidationException;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrdonnance implements IService<Ordonnance> {
    private Connection connection;

    public ServiceOrdonnance() throws SQLException {
        connection = MyDataBase.getInstance().getConnection();
        System.out.println("ServiceOrdonnance instancié");
    }

    @Override
    public void ajouter(Ordonnance ordonnance) throws SQLException {
        String query = "INSERT INTO ordonnance (description, signature, consultation_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ordonnance.getDescription());
            stmt.setString(2, ordonnance.getSignature());
            stmt.setInt(3, ordonnance.getConsultation().getId());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ordonnance.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Ordonnance ordonnance) throws SQLException {
        String query = "UPDATE ordonnance SET description = ?, signature = ?, consultation_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, ordonnance.getDescription());
            stmt.setString(2, ordonnance.getSignature());
            stmt.setInt(3, ordonnance.getConsultation().getId());
            stmt.setInt(4, ordonnance.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM ordonnance WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Ordonnance> afficher() throws SQLException {
        List<Ordonnance> ordonnances = new ArrayList<>();
        String query = "SELECT o.*, c.id as consultation_id, c.type, c.status, c.date_c, c.medecin_id, c.patient_id " +
                      "FROM ordonnance o " +
                      "JOIN consultation c ON o.consultation_id = c.id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println(rs);
                ordonnances.add(extractOrdonnanceFromResultSet(rs));
            }
        }
        return ordonnances;
    }
    
    public Ordonnance getById(int id) throws SQLException {
        String query = "SELECT o.*, c.id as consultation_id, c.type, c.status, c.date_c, c.medecin_id, c.patient_id " +
                      "FROM ordonnance o " +
                      "JOIN consultation c ON o.consultation_id = c.id " +
                      "WHERE o.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractOrdonnanceFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public Ordonnance getByConsultationId(int consultationId) throws SQLException {
        String query = "SELECT o.*, c.id as consultation_id, c.type, c.status, c.date_c, c.medecin_id, c.patient_id " +
                      "FROM ordonnance o " +
                      "JOIN consultation c ON o.consultation_id = c.id " +
                      "WHERE o.consultation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, consultationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return extractOrdonnanceFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    private Ordonnance extractOrdonnanceFromResultSet(ResultSet rs) throws SQLException {
        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setId(rs.getInt("id"));
        ordonnance.setDescription(rs.getString("description"));
        ordonnance.setSignature(rs.getString("signature"));

        // Create and set the consultation
        Consultation consultation = new Consultation();
        consultation.setId(rs.getInt("consultation_id"));
        consultation.setDateC(rs.getTimestamp("date_c").toLocalDateTime());

        // Set patient
        Utilisateur patient = new Utilisateur();
        ServiceUtilisateur su = new ServiceUtilisateur();

        patient  = su.getById(rs.getInt("patient_id"));
       // patient.setNom(rs.getString("patient_nom"));
        patient.setNom("dummy");
        patient.setPrenom("last name");

        //patient.setPrenom(rs.getString("patient_prenom"));
        consultation.setPatient(patient);

        // Set medecin
        Utilisateur medecin = new Utilisateur();
        medecin = su.getById(rs.getInt("medecin_id"));
        consultation.setMedecin(medecin);

        ordonnance.setConsultation(consultation);
        return ordonnance;
    }
    
    // Méthode pour filtrer et paginer les ordonnances
    public List<Ordonnance> filterOrdonnances(String searchText, String patientId, int page, int pageSize) throws SQLException {
        List<Ordonnance> ordonnances = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT o.*, c.date_c, c.patient_id, c.medecin_id, " +
            "u1.nom as patient_nom, u1.prenom as patient_prenom, " +
            "u2.nom as medecin_nom, u2.prenom as medecin_prenom " +
            "FROM ordonnance o " +
            "JOIN consultation c ON o.consultation_id = c.id " +
            "JOIN utilisateur u1 ON c.patient_id = u1.id " +
            "JOIN utilisateur u2 ON c.medecin_id = u2.id " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // Add patient ID filter
        if (patientId != null && !patientId.isEmpty()) {
            query.append(" AND c.patient_id = ?");
            params.add(Integer.parseInt(patientId));
        }

        // Add search text filter
        if (searchText != null && !searchText.isEmpty()) {
            query.append(" AND (o.description LIKE ? OR o.signature LIKE ? OR CAST(o.id AS CHAR) LIKE ?)");
            String searchPattern = "%" + searchText + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Add pagination
        query.append(" ORDER BY o.id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ordonnances.add(extractOrdonnanceFromResultSet(rs));
                }
            }
        }

        return ordonnances;
    }

    // Méthode pour compter le nombre total d'ordonnances (pour la pagination)
    public int countOrdonnances(String searchText, String patientId) throws SQLException {
        StringBuilder query = new StringBuilder(
            "SELECT COUNT(*) FROM ordonnance o " +
            "JOIN consultation c ON o.consultation_id = c.id " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // Add patient ID filter
        if (patientId != null && !patientId.isEmpty()) {
            query.append(" AND c.patient_id = ?");
            params.add(Integer.parseInt(patientId));
        }

        // Add search text filter
        if (searchText != null && !searchText.isEmpty()) {
            query.append(" AND (o.description LIKE ? OR o.signature LIKE ? OR CAST(o.id AS CHAR) LIKE ?)");
            String searchPattern = "%" + searchText + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Set parameters
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
}