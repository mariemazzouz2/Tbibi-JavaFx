package services;

import entities.Prediction;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePrediction implements IService<Prediction> {
    private Connection connection;

    public ServicePrediction() throws SQLException {
        connection = MyDataBase.getInstance().getConnection();
        if (connection == null) {
            throw new SQLException("Échec de la connexion à la base de données");
        }
        System.out.println("Connexion à la base de données établie avec succès dans ServicePrediction");
    }

    @Override
    public void ajouter(Prediction prediction) throws SQLException {
        String query = "INSERT INTO prediction (dossier_id, hypertension, heart_disease, smoking_history, bmi, hb_a1c_level, blood_glucose_level, diabete) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, prediction.getDossierId());
            stmt.setBoolean(2, prediction.isHypertension());
            stmt.setBoolean(3, prediction.isheart_disease());
            stmt.setString(4, prediction.getsmoking_history());
            stmt.setFloat(5, prediction.getBmi());
            stmt.setFloat(6, prediction.gethbA1c_level());
            stmt.setFloat(7, prediction.getBloodGlucoseLevel());
            stmt.setBoolean(8, prediction.isDiabete());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Prédiction ajoutée, lignes affectées : " + rowsAffected);

            // Récupérer l'ID généré
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    prediction.setId(rs.getInt(1));
                    System.out.println("ID généré pour la prédiction : " + prediction.getId());
                }
            }
        }
    }

    @Override
    public void modifier(Prediction prediction) throws SQLException {
        String query = "UPDATE prediction SET dossier_id = ?, hypertension = ?, heart_disease = ?, smoking_history = ?, " +
                "bmi = ?, hb_a1c_level = ?, blood_glucose_level = ?, diabete = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, prediction.getDossierId());
            stmt.setBoolean(2, prediction.isHypertension());
            stmt.setBoolean(3, prediction.isheart_disease());
            stmt.setString(4, prediction.getsmoking_history());
            stmt.setFloat(5, prediction.getBmi());
            stmt.setFloat(6, prediction.gethbA1c_level());
            stmt.setFloat(7, prediction.getBloodGlucoseLevel());
            stmt.setBoolean(8, prediction.isDiabete());
            stmt.setInt(9, prediction.getId());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Prédiction modifiée, lignes affectées : " + rowsAffected);
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM prediction WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Prédiction supprimée (ID: " + id + "), lignes affectées : " + rowsAffected);
        }
    }

    @Override
    public List<Prediction> afficher() throws SQLException {
        List<Prediction> predictions = new ArrayList<>();
        String query = "SELECT * FROM prediction";
        System.out.println("Exécution de la requête pour récupérer toutes les prédictions...");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int count = 0;
            while (rs.next()) {
                Prediction prediction = new Prediction();
                prediction.setId(rs.getInt("id"));
                prediction.setDossierId(rs.getInt("dossier_id"));
                prediction.setHypertension(rs.getBoolean("hypertension"));
                prediction.setheart_disease(rs.getBoolean("heart_disease"));
                prediction.setsmoking_history(rs.getString("smoking_history"));
                prediction.setBmi(rs.getFloat("bmi"));
                prediction.sethbA1c_level(rs.getFloat("hb_a1c_level")); // Corrigé ici
                prediction.setBloodGlucoseLevel(rs.getFloat("blood_glucose_level"));
                prediction.setDiabete(rs.getBoolean("diabete"));
                predictions.add(prediction);
                count++;
            }
            System.out.println("Nombre total de prédictions récupérées (afficher) : " + count);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des prédictions (afficher) : " + e.getMessage());
            throw e;
        }
        return predictions;
    }

    public List<Prediction> getAll() throws SQLException {
        System.out.println("Appel de getAll(), redirection vers afficher()...");
        return afficher();
    }

    public Prediction getById(int id) throws SQLException {
        String query = "SELECT * FROM prediction WHERE id = ?";
        System.out.println("Récupération de la prédiction avec ID : " + id);
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prediction prediction = new Prediction();
                    prediction.setId(rs.getInt("id"));
                    prediction.setDossierId(rs.getInt("dossier_id"));
                    prediction.setHypertension(rs.getBoolean("hypertension"));
                    prediction.setheart_disease(rs.getBoolean("heart_disease"));
                    prediction.setsmoking_history(rs.getString("smoking_history"));
                    prediction.setBmi(rs.getFloat("bmi"));
                    prediction.sethbA1c_level(rs.getFloat("hb_a1c_level")); // Corrigé ici
                    prediction.setBloodGlucoseLevel(rs.getFloat("blood_glucose_level"));
                    prediction.setDiabete(rs.getBoolean("diabete"));
                    System.out.println("Prédiction trouvée : " + prediction);
                    return prediction;
                } else {
                    System.out.println("Aucune prédiction trouvée pour l'ID : " + id);
                }
            }
        }
        return null;
    }

    public List<Prediction> getByDossierId(int dossierId) throws SQLException {
        List<Prediction> predictions = new ArrayList<>();
        String query = "SELECT * FROM prediction WHERE dossier_id = ?";
        System.out.println("Récupération des prédictions pour le dossier ID : " + dossierId);
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierId);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Prediction prediction = new Prediction();
                    prediction.setId(rs.getInt("id"));
                    prediction.setDossierId(rs.getInt("dossier_id"));
                    prediction.setHypertension(rs.getBoolean("hypertension"));
                    prediction.setheart_disease(rs.getBoolean("heart_disease"));
                    prediction.setsmoking_history(rs.getString("smoking_history"));
                    prediction.setBmi(rs.getFloat("bmi"));
                    prediction.sethbA1c_level(rs.getFloat("hb_a1c_level")); // Corrigé ici
                    prediction.setBloodGlucoseLevel(rs.getFloat("blood_glucose_level"));
                    prediction.setDiabete(rs.getBoolean("diabete"));
                    predictions.add(prediction);
                    count++;
                }
                System.out.println("Nombre de prédictions trouvées pour le dossier ID " + dossierId + " : " + count);
            }
        }
        return predictions;
    }
}