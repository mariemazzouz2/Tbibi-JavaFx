package controller.Patient;

import entities.Prediction;
import services.ServicePrediction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PredictionListPatientController {

    @FXML private Label titleLabel;
    @FXML private Label dossierIdLabel;
    @FXML private GridPane predictionGrid;

    private ObservableList<Prediction> predictionList;
    private Integer dossierId;
    private Prediction selectedPrediction;

    private final ServicePrediction servicePrediction = new ServicePrediction();

    public PredictionListPatientController() throws SQLException {
    }

    public void setDossierId(Integer dossierId) {
        this.dossierId = dossierId;
        dossierIdLabel.setText("Dossier ID: " + dossierId);
        loadPredictions();
    }

    private void loadPredictions() {
        try {
            List<Prediction> predictions = servicePrediction.getByDossierId(dossierId);
            predictionList = FXCollections.observableArrayList(predictions);
            populateGrid(predictionList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des prédictions : " + e.getMessage());
        }
    }

    private void populateGrid(ObservableList<Prediction> predictions) {
        predictionGrid.getChildren().clear();
        selectedPrediction = null;

        int row = 0;
        int col = 0;
        for (Prediction prediction : predictions) {
            VBox card = new VBox(5);
            card.getStyleClass().add("prediction-card");

            Label titleLabel = new Label("Prédiction #" + prediction.getId());
            titleLabel.getStyleClass().add("title");

            Label hypertensionLabel = new Label("Hypertension: " + (prediction.isHypertension() ? "Oui" : "Non"));
            hypertensionLabel.getStyleClass().add("label");

            Label heartDiseaseLabel = new Label("Maladie Cardiaque: " + (prediction.isheart_disease() ? "Oui" : "Non"));
            heartDiseaseLabel.getStyleClass().add("label");

            Label smokingLabel = new Label("Tabagisme: " + prediction.getsmoking_history());
            smokingLabel.getStyleClass().add("label");

            Label bmiLabel = new Label("IMC: " + prediction.getBmi());
            bmiLabel.getStyleClass().add("label");

            Label hba1cLabel = new Label("Niveau HbA1c: " + prediction.gethbA1c_level());
            hba1cLabel.getStyleClass().add("label");

            Label glucoseLabel = new Label("Glucose Sanguin: " + prediction.getBloodGlucoseLevel());
            glucoseLabel.getStyleClass().add("label");

            Label diabeteLabel = new Label("Diabète: " + (prediction.isDiabete() ? "Oui" : "Non"));
            diabeteLabel.getStyleClass().add("label");

            card.getChildren().addAll(titleLabel, hypertensionLabel, heartDiseaseLabel, smokingLabel, bmiLabel, hba1cLabel, glucoseLabel, diabeteLabel);

            card.setOnMouseClicked(event -> {
                selectedPrediction = prediction;
                predictionGrid.getChildren().forEach(node -> node.setStyle("-fx-border-color: transparent;"));
                card.setStyle("-fx-border-color: #1E90FF; -fx-border-width: 2; -fx-border-radius: 8;");
            });

            predictionGrid.add(card, col, row);

            col++;
            if (col == 3) { // Changed from 3 to 2 cards per row
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void supprimerPrediction() {
        if (selectedPrediction != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette prédiction ?");
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        servicePrediction.supprimer(selectedPrediction.getId());
                        predictionList.remove(selectedPrediction);
                        selectedPrediction = null;
                        loadPredictions();
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Prédiction supprimée avec succès !");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la prédiction : " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/MedicalStyle.css").toExternalForm());
        alert.showAndWait();
    }

    public void refreshPredictions() {
        loadPredictions();
    }
}