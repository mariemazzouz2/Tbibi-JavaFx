package controller.Patient;

import entities.DossierMedical;
import entities.Prediction;
import entities.Utilisateur;
import services.ServiceDossierMedical;
import services.ServicePrediction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import controller.AnalyseListController;
import java.awt.Desktop;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;

public class DossierMedicalDetailsPatientController {
    @FXML private Label idLabel; // Ajouté pour correspondre au FXML
    @FXML private Label utilisateurEmailLabel;
    @FXML private Label dateLabel;
    @FXML private Label uniteLabel;
    @FXML private Label mesureLabel;
    @FXML private Label diabeteLabel;

    private Stage stage;
    private DossierMedical dossier;
    private ServiceDossierMedical serviceDossierMedical;

    public DossierMedicalDetailsPatientController() {
        try {
            serviceDossierMedical = new ServiceDossierMedical();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void previewFile() {
        if (dossier == null || dossier.getFichier() == null || dossier.getFichier().isEmpty()) {
            showAlert("Erreur", "Aucun fichier associé à ce dossier médical.");
            return;
        }

        try {
            // Récupérer le chemin du fichier
            String filePath = dossier.getFichier();
            File file = new File(filePath);

            // Vérifier si le fichier existe
            if (!file.exists()) {
                showAlert("Erreur", "Le fichier spécifié n'existe pas : " + filePath);
                return;
            }

            // Vérifier si Desktop est supporté
            if (!Desktop.isDesktopSupported()) {
                showAlert("Erreur", "L'ouverture de fichiers n'est pas supportée sur ce système.");
                return;
            }

            // Ouvrir le fichier avec l'application par défaut
            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le fichier : " + e.getMessage());
        }
    }
    public void setDossier(DossierMedical dossier) throws SQLException {
        this.dossier = dossier;

        // Vérification des labels
        if (idLabel == null || utilisateurEmailLabel == null || dateLabel == null  ||
                uniteLabel == null || mesureLabel == null || diabeteLabel == null) {
            showAlert("Erreur FXML", "Un ou plusieurs labels n'ont pas été correctement injectés depuis le FXML.");
            return;
        }

        // Remplissage des labels avec les données du dossier
        idLabel.setText(" "+String.valueOf(dossier.getId()));
        // Récupérer l'utilisateur via son ID
        Utilisateur utilisateur = serviceDossierMedical.getUtilisateurByDossierId(dossier.getUtilisateurId());
        utilisateurEmailLabel.setText("Email: " + utilisateur.getEmail());        dateLabel.setText("Date: " + dossier.getDate().toString());
        uniteLabel.setText("Unité: " + dossier.getUnite());
        mesureLabel.setText("Mesure: " + dossier.getMesure());

        try {
            ServicePrediction servicePrediction = new ServicePrediction();
            Prediction prediction = servicePrediction.getByDossierId(dossier.getId()).stream().findFirst().orElse(null);
            if (prediction != null) {
                diabeteLabel.setText("Risque de Diabète: " + (prediction.isDiabete() ? "Oui" : "Non"));
            } else {
                diabeteLabel.setText("Risque de Diabète: Aucune prédiction");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la récupération de la prédiction : " + e.getMessage());
        }
    }

    @FXML
    private void modifyDossier() {
        try {
            String fxmlPath = "/fxml/Admin/FormDossierMedicalAdmin.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("Impossible de trouver le fichier FXML : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            controller.Admin.DossierMedicalFormController controller = loader.getController();
            controller.setDossier(dossier);

            Stage modifyStage = new Stage();
            modifyStage.setTitle("Modifier le Dossier Médical");
            modifyStage.setScene(new Scene(root, 800, 600)); // Taille augmentée pour un grand format
            modifyStage.setResizable(true);
            modifyStage.showAndWait();

            try {
                DossierMedical updatedDossier = serviceDossierMedical.getById(dossier.getId());
                if (updatedDossier != null) {
                    setDossier(updatedDossier);
                } else {
                    showAlert("Erreur", "Le dossier médical n'a pas pu être trouvé après modification.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du rechargement du dossier : " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }

    @FXML
    private void showPredictionList() {
        try {
            String fxmlPath = "/fxml/Patient/PredictionListPatient.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("Impossible de trouver le fichier FXML : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            controller.Patient.PredictionListPatientController controller = loader.getController();
            controller.setDossierId(dossier.getId());

            Stage stage = new Stage();
            stage.setTitle("Liste des Prédictions");
            stage.setScene(new Scene(root, 800, 600)); // Taille augmentée pour un grand format
            stage.setResizable(true);
            stage.showAndWait();

            setDossier(dossier);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des prédictions : " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void showAnalyseList() {
        try {
            String fxmlPath = "/fxml/Patient/AnalyseListPatient.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("Impossible de trouver le fichier FXML : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            AnalyseListController controller = loader.getController();
            controller.filterByDossierId(dossier.getId());

            Stage analyseStage = new Stage();
            analyseStage.setTitle("Liste des Analyses pour le Dossier " + dossier.getId());
            analyseStage.setScene(new Scene(root, 800, 600)); // Taille augmentée pour un grand format
            analyseStage.setResizable(true);
            analyseStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des analyses : " + e.getMessage());
        }
    }

    @FXML
    private void closeDetails() {
        stage = (Stage) utilisateurEmailLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (getClass().getResource("/MedicalStyle.css") != null) {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/MedicalStyle.css").toExternalForm());
        }
        alert.showAndWait();
    }
}