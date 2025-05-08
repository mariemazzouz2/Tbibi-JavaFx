package controller.Doctor;

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
import services.ServiceUtilisateur;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class DossierMedicalDetailsDoctorController {

    @FXML private Label utilisateurIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label uniteLabel;
    @FXML private Label mesureLabel;
    @FXML private Label diabeteLabel;
    @FXML private HBox header; // Ajouté pour accéder à l'élément .header
    @FXML private Label headerTitle; // Ajouté pour accéder à l'élément .header-title
    @FXML private VBox card; // Ajouté pour accéder à l'élément .card

    private Stage stage;
    private DossierMedical dossier;
    private ServiceDossierMedical serviceDossierMedical = new ServiceDossierMedical();
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    public DossierMedicalDetailsDoctorController() throws SQLException {
    }

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;

        if (utilisateurIdLabel == null || dateLabel == null ||
                uniteLabel == null || mesureLabel == null || diabeteLabel == null) {
            showAlert("Erreur FXML", "Un ou plusieurs labels n'ont pas été correctement injectés depuis le FXML.");
            return;
        }

        utilisateurIdLabel.setText("Utilisateur ID: " + dossier.getUtilisateurId());
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
    private void previewFilePatient() throws SQLException {
        Utilisateur user = serviceUtilisateur.getById(dossier.getUtilisateurId());
        if (dossier == null || user.getEmail() == null) {
            showAlert("Erreur", "Aucune information sur le patient associée à ce dossier médical.");
            return;
        }

        try {
            String email = user.getEmail();
            String fileName = "Fichier Patient de " + email + ".txt";
            fileName = fileName.replaceAll("[^a-zA-Z0-9.@ ]", "_");

            String filePath = "src/main/resources/fichier/" + fileName;
            File file = new File(filePath);

            if (!file.exists()) {
                showAlert("Erreur", "Le fichier des mesures du patient n'existe pas : " + filePath);
                return;
            }

            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            Stage previewStage = new Stage();
            previewStage.setTitle("Mesures du Patient - " + email);

            VBox vbox = new VBox(10);
            vbox.setPadding(new javafx.geometry.Insets(10));

            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(300);
            textArea.setPrefWidth(400);

            Button closeButton = new Button("Fermer");
            closeButton.setOnAction(e -> previewStage.close());

            vbox.getChildren().addAll(new Label("Mesures du Patient :"), textArea, closeButton);

            Scene scene = new Scene(vbox);
            previewStage.setScene(scene);
            previewStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de lire le fichier des mesures : " + e.getMessage());
        }
    }

    @FXML
    private void previewFile() {
        if (dossier == null || dossier.getFichier() == null || dossier.getFichier().isEmpty()) {
            showAlert("Erreur", "Aucun fichier associé à ce dossier médical.");
            return;
        }

        try {
            String filePath = dossier.getFichier();
            File file = new File(filePath);

            if (!file.exists()) {
                showAlert("Erreur", "Le fichier spécifié n'existe pas : " + filePath);
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                showAlert("Erreur", "L'ouverture de fichiers n'est pas supportée sur ce système.");
                return;
            }

            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le fichier : " + e.getMessage());
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
            modifyStage.setScene(new Scene(root));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/PredictionListDoctor.fxml"));
            Parent root = loader.load();

            PredictionListDoctorController controller = loader.getController();
            controller.setDossierId(dossier.getId());

            Stage stage = new Stage();
            stage.setTitle("Liste des Prédictions");
            stage.setScene(new Scene(root, 1000, 600));
            stage.setResizable(true);
            stage.showAndWait();

            setDossier(dossier);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des prédictions : " + e.getMessage());
        }
    }

    @FXML
    private void showAnalyseList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/AnalyseListDoctor.fxml"));
            Parent root = loader.load();
            controller.AnalyseListController controller = loader.getController();
            controller.filterByDossierId(dossier.getId());

            Stage analyseStage = new Stage();
            analyseStage.setTitle("Liste des Analyses pour le Dossier " + dossier.getId());
            analyseStage.setScene(new Scene(root, 1000, 600));
            analyseStage.setResizable(true);
            analyseStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des analyses : " + e.getMessage());
        }
    }

    @FXML
    private void closeDetails() {
        stage = (Stage) utilisateurIdLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}