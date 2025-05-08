package controller;
import controller.Patient.DossierMedicalDetailsPatientController;
import entities.DossierMedical;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Utilisateur;
import services.ServiceDossierMedical;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class FrontPatientController {

    @FXML
    private Label labelNomUtilisateur;
    @FXML
    private Button suiviMedicalButton;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText( currentUser.getNom());
        }

    }


    @FXML
    private void goToDossierList() {
        try {
            // Vérifier si un utilisateur_id a été fourni
            /*if (utilisateurId == null || utilisateurId.isEmpty()) {
                showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter.");
                return;
            }*/

            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            int userId;
            try {
                //userId = Integer.parseInt(utilisateurId);
                userId = currentUser.getId();
            } catch (NumberFormatException e) {
                return;
            }

            // Récupérer le dossier médical de l'utilisateur
            ServiceDossierMedical service = new ServiceDossierMedical();
            DossierMedical dossier = service.getByUtilisateurId(userId);

            if (dossier == null) {
                showAlert("Erreur", "Aucun dossier médical trouvé pour l'utilisateur avec l'ID " + userId);
                return;
            }

            // Charger DossierMedicalDetails.fxml pour afficher les détails du dossier
            String fxmlPath = "/fxml/Patient/DossierMedicalDetails.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                System.err.println("Chemin absolu des ressources : " + getClass().getResource("/").getPath());
                throw new RuntimeException("Impossible de trouver " + fxmlPath + " dans les ressources");
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Passer le dossier au contrôleur
            DossierMedicalDetailsPatientController controller = loader.getController();
            controller.setDossier(dossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical (Patient)");
            stage.setScene(new Scene(root, 800, 600)); // Taille augmentée pour un grand format
            stage.setResizable(true);
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) suiviMedicalButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du dossier : " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la récupération du dossier : " + e.getMessage());
        }
    }
    public void logout(javafx.event.ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
