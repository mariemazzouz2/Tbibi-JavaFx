package controller;

import entities.Analyse;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AnalyseDetailsController {
    @FXML private Label dossierIdLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateAnalyseLabel;
    @FXML private Label donneesAnalyseLabel;
    @FXML private Label diagnosticLabel;
    private Analyse analyse;
    private Stage stage;

    public void setAnalyse(Analyse analyse) {
        // Vérification pour s'assurer que tous les Labels sont initialisés
        if (dossierIdLabel == null || typeLabel == null || dateAnalyseLabel == null ||
                donneesAnalyseLabel == null || diagnosticLabel == null) {
            showAlert("Erreur", "Un ou plusieurs composants FXML ne sont pas initialisés.");
            return;
        }

        this.analyse = analyse;
        if (analyse != null) {
            dossierIdLabel.setText("Dossier ID: " + analyse.getDossierId());
            typeLabel.setText("Type: " + analyse.getType());
            dateAnalyseLabel.setText("Date Analyse: " + analyse.getDateAnalyse().toString());
            donneesAnalyseLabel.setText("Données Analyse: " + analyse.getDonneesAnalyse());
            diagnosticLabel.setText("Diagnostic: " + analyse.getDiagnostic());
        } else {
            showAlert("Erreur", "Aucune analyse fournie pour affichage.");
        }
    }

    // Ajout d'une méthode pour définir le Stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void previewFile() {
        if (analyse == null) {
            showAlert("Erreur", "Aucune analyse sélectionnée.");
            return;
        }

        if (analyse.getDonneesAnalyse() == null || analyse.getDonneesAnalyse().isEmpty()) {
            showAlert("Erreur", "Aucun fichier associé à cette analyse.");
            return;
        }

        try {
            String filePath = analyse.getDonneesAnalyse();
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
    private void closeDetails() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}