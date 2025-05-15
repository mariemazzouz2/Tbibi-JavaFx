package services;

import entities.Analyse;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnalyse implements IService<Analyse> {
    private Connection connection;

    public ServiceAnalyse() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Analyse analyse) throws SQLException {
        String query = "INSERT INTO analyse (dossier_id, type, dateanalyse, donnees_analyse, diagnostic) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, analyse.getDossierId());
            stmt.setString(2, analyse.getType());
            stmt.setDate(3, Date.valueOf(analyse.getDateAnalyse()));
            stmt.setString(4, analyse.getDonneesAnalyse());
            stmt.setString(5, analyse.getDiagnostic());
            stmt.executeUpdate();
        }
    }

    @Override
    public void modifier(Analyse analyse) throws SQLException {
        String query = "UPDATE analyse SET dossier_id = ?, type = ?, dateanalyse = ?, donnees_analyse = ?, diagnostic = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, analyse.getDossierId());
            stmt.setString(2, analyse.getType());
            stmt.setDate(3, Date.valueOf(analyse.getDateAnalyse()));
            stmt.setString(4, analyse.getDonneesAnalyse());
            stmt.setString(5, analyse.getDiagnostic());
            stmt.setInt(6, analyse.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM analyse WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Analyse> afficher() throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Analyse analyse = new Analyse();
                analyse.setId(rs.getInt("id"));
                analyse.setDossierId(rs.getInt("dossier_id"));
                analyse.setType(rs.getString("type"));
                analyse.setDateAnalyse(rs.getDate("dateanalyse").toLocalDate());
                analyse.setDonneesAnalyse(rs.getString("donnees_analyse"));
                analyse.setDiagnostic(rs.getString("diagnostic"));
                analyses.add(analyse);
            }
        }
        return analyses;
    }

    public List<Analyse> getByDossierId(int dossierId) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE dossier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Analyse analyse = new Analyse();
                    analyse.setId(rs.getInt("id"));
                    analyse.setDossierId(rs.getInt("dossier_id"));
                    analyse.setType(rs.getString("type"));
                    analyse.setDateAnalyse(rs.getDate("dateanalyse").toLocalDate());
                    analyse.setDonneesAnalyse(rs.getString("donnees_analyse"));
                    analyse.setDiagnostic(rs.getString("diagnostic"));
                    analyses.add(analyse);
                }
            }
        }
        return analyses;
    }
}