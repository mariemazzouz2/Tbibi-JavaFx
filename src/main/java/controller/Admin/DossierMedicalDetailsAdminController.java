package controller.Admin;


import controller.AnalyseListController;
import entities.DossierMedical;
import entities.Prediction;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceDossierMedical;
import services.ServicePrediction;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import java.sql.SQLException;

public class DossierMedicalDetailsAdminController {
    @FXML private Label utilisateurEmailLabel;
    @FXML private Label dateLabel;
    @FXML private Label uniteLabel;
    @FXML private Label mesureLabel;
    @FXML private Label diabeteLabel;
    @FXML private HBox header;
    @FXML private Label headerTitle;
    @FXML private VBox card;
    private Stage stage;
    private DossierMedical dossier;
    ServiceDossierMedical serviceDossierMedical = new ServiceDossierMedical();

    public DossierMedicalDetailsAdminController() throws SQLException {
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
        if (utilisateurEmailLabel == null || dateLabel == null || uniteLabel == null || mesureLabel == null || diabeteLabel == null) {
            showAlert("Erreur FXML", "Un ou plusieurs labels n'ont pas été correctement injectés depuis le FXML.");
            return;
        }

        // Récupérer l'utilisateur via son ID
        Utilisateur utilisateur = serviceDossierMedical.getUtilisateurByDossierId(dossier.getUtilisateurId());
        utilisateurEmailLabel.setText("Email: " + utilisateur.getEmail());
        dateLabel.setText("Date: " + dossier.getDate().toString());
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
        // Ajouter la classe "visible" pour déclencher les animations
        if (header != null) {
            header.getStyleClass().add("visible");
        }
        if (headerTitle != null) {
            headerTitle.getStyleClass().add("visible");
        }
        if (card != null) {
            card.getStyleClass().add("visible");
        }
    }

    @FXML
    private void showPredictionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/PredictionListAdmin.fxml"));
            Parent root = loader.load();

            controller.Admin.PredictionListAdminController controller = loader.getController();
            controller.setDossierId(dossier.getId());

            Stage stage = new Stage();
            stage.setTitle("Liste des Prédictions");
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(true);
            stage.showAndWait();

            // Rafraîchir les prédictions affichées après la gestion
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/AnalyseListAdmin.fxml"));
            Parent root = loader.load();
            AnalyseListController controller = loader.getController();
            controller.filterByDossierId(dossier.getId());

            Stage analyseStage = new Stage();
            analyseStage.setTitle("Liste des Analyses pour le Dossier " + dossier.getId());
            analyseStage.setScene(new Scene(root, 600, 400));
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
        alert.showAndWait();
    }
}