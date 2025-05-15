package service;
import entities.Evenement;
import models.Question;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EvenementService implements IService1<Evenement> {

    private final Connection connection;
    private static final Logger logger = Logger.getLogger(EvenementService.class.getName());

    public EvenementService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Evenement evenement) throws SQLException {
        String sql = "INSERT INTO evenement (categorie_id, titre, description, date_debut, date_fin, lieu, statut, image, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenement.getCategorieId());
            ps.setString(2, evenement.getTitre());
            ps.setString(3, evenement.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(evenement.getDateDebut().atStartOfDay()));
            ps.setTimestamp(5, Timestamp.valueOf(evenement.getDateFin().atStartOfDay()));
            ps.setString(6, evenement.getLieu());
            ps.setString(7, evenement.getStatut());
            ps.setString(8, evenement.getImage());
            ps.setDouble(9, evenement.getLatitude());
            ps.setDouble(10, evenement.getLongitude());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ajout de l'événement: " + evenement.getTitre(), e);
            throw e;
        }
    }

    @Override
    public boolean modifier(Evenement evenement) throws SQLException {
        String sql = "UPDATE evenement SET categorie_id=?, titre=?, description=?, date_debut=?, date_fin=?, lieu=?, statut=?, image=?, latitude=?, longitude=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenement.getCategorieId());
            ps.setString(2, evenement.getTitre());
            ps.setString(3, evenement.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(evenement.getDateDebut().atStartOfDay()));
            ps.setTimestamp(5, Timestamp.valueOf(evenement.getDateFin().atStartOfDay()));
            ps.setString(6, evenement.getLieu());
            ps.setString(7, evenement.getStatut());
            ps.setString(8, evenement.getImage());
            ps.setDouble(9, evenement.getLatitude());
            ps.setDouble(10, evenement.getLongitude());
            ps.setLong(11, evenement.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la modification de l'événement: " + evenement.getTitre(), e);
            throw e;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String sql = "DELETE FROM evenement WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la suppression de l evento avec id: " + id, e);
            throw e;
        }
    }

    public boolean supprimerParCategorie(int categorieId) throws SQLException {
        String sql = "DELETE FROM evenement WHERE categorie_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, categorieId);
            int rowsAffected = stmt.executeUpdate();
            logger.log(Level.INFO, "Nombre d'événements supprimés pour la catégorie " + categorieId + ": " + rowsAffected);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la suppression des événements de la catégorie " + categorieId, e);
            throw e;
        }
    }

    @Override
    public List<Evenement> recuperer() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            logger.log(Level.INFO, "Début de la récupération des événements");

            while (rs.next()) {
                try {
                    Evenement evenement = new Evenement();
                    evenement.setId(rs.getInt("id"));
                    evenement.setCategorieId(rs.getInt("categorie_id"));
                    evenement.setTitre(rs.getString("titre"));
                    evenement.setDescription(rs.getString("description"));
                    evenement.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime().toLocalDate());
                    evenement.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime().toLocalDate());
                    evenement.setLieu(rs.getString("lieu"));
                    evenement.setStatut(rs.getString("statut"));
                    evenement.setImage(rs.getString("image"));
                    evenement.setLatitude(rs.getDouble("latitude"));
                    evenement.setLongitude(rs.getDouble("longitude"));

                    list.add(evenement);
                    logger.log(Level.INFO, "Événement récupéré: " + evenement.getTitre());
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Erreur lors de la récupération d'un événement", e);
                }
            }

            logger.log(Level.INFO, "Nombre d'événements récupérés: " + list.size());
            return list;
        }
    }

    public Evenement getById(int id) throws SQLException {
        String sql = "SELECT * FROM evenement WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setCategorieId(rs.getInt("categorie_id"));
                evenement.setTitre(rs.getString("titre"));
                evenement.setDescription(rs.getString("description"));
                evenement.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime().toLocalDate());
                evenement.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime().toLocalDate());
                evenement.setLieu(rs.getString("lieu"));
                evenement.setStatut(rs.getString("statut"));
                evenement.setImage(rs.getString("image"));
                evenement.setLatitude(rs.getDouble("latitude"));
                evenement.setLongitude(rs.getDouble("longitude"));
                return evenement;
            }
            return null;
        }
    }
}