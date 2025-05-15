package controller;

import entities.Analyse;
import services.ServiceAnalyse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AnalyseListController implements Initializable {

    @FXML private GridPane analyseGrid;

    private ServiceAnalyse serviceAnalyse;
    private Analyse selectedAnalyse; // Pour suivre l'analyse sélectionnée

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            serviceAnalyse = new ServiceAnalyse();
            System.out.println("ServiceAnalyse initialisé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de ServiceAnalyse : " + e.getMessage());
            e.printStackTrace();
        }

        loadAnalyses();
    }

    public void loadAnalyses() {
        try {
            List<Analyse> analyses = serviceAnalyse.afficher();
            populateGrid(analyses);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des analyses : " + e.getMessage());
        }
    }

    public void filterByDossierId(int dossierId) {
        try {
            List<Analyse> analyses = serviceAnalyse.getByDossierId(dossierId);
            populateGrid(analyses);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des analyses pour le dossier : " + e.getMessage());
        }
    }

    private void populateGrid(List<Analyse> analyses) {
        // Effacer le contenu existant du GridPane
        analyseGrid.getChildren().clear();
        selectedAnalyse = null; // Réinitialiser la sélection

        // Ajouter les cartes au GridPane
        int row = 0;
        int col = 0;
        for (Analyse analyse : analyses) {
            // Créer une carte pour chaque analyse
            VBox card = new VBox(5);
            card.getStyleClass().add("analyse-card");

            Label titleLabel = new Label("Analyse #" + analyse.getId());
            titleLabel.getStyleClass().add("title");

            Label typeLabel = new Label("Type: " + analyse.getType());
            typeLabel.getStyleClass().add("label");

            Label dateLabel = new Label("Date: " + analyse.getDateAnalyse().toString());
            dateLabel.getStyleClass().add("label");

            Label donneesLabel = new Label("Données: " + analyse.getDonneesAnalyse());
            donneesLabel.getStyleClass().add("label");

            Label diagnosticLabel = new Label("Diagnostic: " + analyse.getDiagnostic());
            diagnosticLabel.getStyleClass().add("label");

            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().add("details-button");
            detailsButton.setOnAction(event -> showDetails(analyse));

            card.getChildren().addAll(titleLabel,typeLabel, dateLabel, donneesLabel, diagnosticLabel, detailsButton);

            // Ajouter un gestionnaire de clic pour sélectionner la carte
            card.setOnMouseClicked(event -> {
                selectedAnalyse = analyse;
                // Ajouter une indication visuelle de sélection
                analyseGrid.getChildren().forEach(node -> node.setStyle("-fx-border-color: transparent;"));
                card.setStyle("-fx-border-color: #6C5CE7; -fx-border-width: 2; -fx-border-radius: 8;");
            });

            // Ajouter la carte au GridPane
            analyseGrid.add(card, col, row);

            // Passer à la colonne suivante
            col++;
            if (col == 4) { // 3 cartes par ligne
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void addAnalyse() {
        showAnalyseForm("Ajouter Analyse", null);
    }

    @FXML
    private void editAnalyse() {
        if (selectedAnalyse != null) {
            showAnalyseForm("Modifier Analyse", selectedAnalyse);
        } else {
            showAlert("Erreur", "Veuillez sélectionner une analyse à modifier.");
        }
    }

    @FXML
    private void deleteAnalyse() {
        if (selectedAnalyse != null) {
            try {
                serviceAnalyse.supprimer(selectedAnalyse.getId());
                loadAnalyses();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression : " + e.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une analyse à supprimer.");
        }
    }

    private void showAnalyseForm(String title, Analyse analyse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/FormAnalyse.fxml"));
            Parent root = loader.load();
            AnalyseFormController controller = loader.getController();
            controller.setAnalyse(analyse);
            controller.setListController(this);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            loadAnalyses();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire : " + e.getMessage());
        }
    }

    private void showDetails(Analyse analyse) {
        try {
            String fxmlPath = "/fxml/Patient/AnalyseDetails.fxml";
            if (getClass().getResource("/fxml/Doctor/AnalyseListDoctor.fxml") != null) {
                fxmlPath = "/fxml/Doctor/AnalyseDetailsDoctor.fxml";
            } else if (getClass().getResource("/fxml/Admin/AnalyseListAdmin.fxml") != null) {
                fxmlPath = "/fxml/Admin/AnalyseDetailsAdmin.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/AnalyseDetails.fxml"));
            Parent root = loader.load();
            AnalyseDetailsController controller = loader.getController();
            controller.setAnalyse(analyse);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'Analyse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les détails : " + e.getMessage());
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