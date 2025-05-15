package tests.Main;

import entities.DossierMedical;
import services.ServiceDossierMedical;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
//import tests.Patient.DossierMedicalDetailsPatientController;

import java.io.IOException;
import java.sql.SQLException;

public class MainController {
    @FXML private Button forumButton;
    @FXML private Button produitButton;
    @FXML private Button evenementButton;
    @FXML private Button suiviMedicalButton;
    @FXML private Button consultationButton;
    @FXML private Button connexionButton;
    @FXML private ImageView backgroundImageView;

    private String utilisateurId; // Stocker l'ID de l'utilisateur

    // Méthode pour définir l'ID de l'utilisateur
    public void setUtilisateurId(String utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    @FXML
    private void goToDossierList() {
        try {
            // Vérifier si un utilisateur_id a été fourni
            if (utilisateurId == null || utilisateurId.isEmpty()) {
                showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter.");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(utilisateurId);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "L'ID de l'utilisateur doit être un nombre entier : " + utilisateurId);
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
      //      DossierMedicalDetailsPatientController controller = loader.getController();
            //    controller.setDossier(dossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical (Patient)");
            stage.setScene(new Scene(root, 400, 300));
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

    @FXML
    private void goToForum() {
        System.out.println("Bouton Forum cliqué - À implémenter");
        // TODO: Charger le FXML correspondant au Forum
    }

    @FXML
    private void goToProduit() {
        System.out.println("Bouton Produit cliqué - À implémenter");
        // TODO: Charger le FXML correspondant aux Produits
    }

    @FXML
    private void goToEvenement() {
        System.out.println("Bouton Événement cliqué - À implémenter");
        // TODO: Charger le FXML correspondant aux Événements
    }

    @FXML
    private void goToConsultation() {
        System.out.println("Bouton Consultation cliqué - À implémenter");
        // TODO: Charger le FXML correspondant à la Consultation
    }

    @FXML
    private void goToConnexion() {
        System.out.println("Bouton Connexion cliqué - À implémenter");
        // TODO: Charger le FXML correspondant à la Connexion
    }

    @FXML
    public void initialize() {
        if (backgroundImageView.getImage() == null) {
            System.out.println("Erreur : l'image de fond n'a pas été chargée.");
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