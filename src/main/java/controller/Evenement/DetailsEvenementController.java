package controller.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

public class DetailsEvenementController {
    @FXML
    private Label titreLabel;
    @FXML
    private TextArea descriptionText;
    @FXML
    private Label dateDebutLabel;
    @FXML
    private Label dateFinLabel;
    @FXML
    private Label lieuLabel;
    @FXML
    private Label statutLabel;

    private Evenement evenement;

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        afficherDetails();
    }

    private void afficherDetails() {
        titreLabel.setText(evenement.getTitre());
        descriptionText.setText(evenement.getDescription());
        dateDebutLabel.setText(evenement.getDateDebut().toString());
        dateFinLabel.setText(evenement.getDateFin().toString());
        lieuLabel.setText(evenement.getLieu());
        statutLabel.setText(evenement.getStatut());
    }

    @FXML
    private void handleRetour() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }
} 