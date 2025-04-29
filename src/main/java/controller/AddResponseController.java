package controller;


import models.Question;
import models.Reponse;
import models.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import service.ReponseService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AddResponseController {
    @FXML private Label questionTitleLabel;
    @FXML private TextArea contentField;

    private ReponseService responseService = new ReponseService();
    private Question question;
    private ManageResponsesController parentController;
    private final int currentMedecinId = 2; // Hardcoded for demo; replace with session

    public void setQuestion(Question question) {
        this.question = question;
        questionTitleLabel.setText("Question: " + question.getTitre());
    }

    public void setParentController(ManageResponsesController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSave() {
        String contenu = contentField.getText().trim();

        if (contenu.isEmpty()) {
            showAlert("Erreur", "Validation", "Le contenu de la réponse ne peut pas être vide.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Reponse reponse = new Reponse();
            reponse.setContenu(contenu);
            reponse.setDateReponse(LocalDateTime.now());
            reponse.setQuestion(question);

            Utilisateur medecin = new Utilisateur();
            medecin.setId(currentMedecinId);
            reponse.setMedecin(medecin);

            responseService.ajouter(reponse);

            // Close window
            contentField.getScene().getWindow().hide();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        contentField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}