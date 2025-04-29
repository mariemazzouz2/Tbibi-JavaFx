package controller;



import models.Question;
import models.Reponse;
import models.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import service.ReponseService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManageResponsesController {
    @FXML private Label titleLabel;
    @FXML private TableView<Reponse> responsesTable;
    @FXML private TableColumn<Reponse, String> contentColumn;
    @FXML private TableColumn<Reponse, LocalDateTime> dateColumn;
    @FXML private TableColumn<Reponse, Utilisateur> responderColumn;

    private ReponseService responseService = new ReponseService();
    private ObservableList<Reponse> responsesList = FXCollections.observableArrayList();
    private Question question;

    public void setQuestion(Question question) {
        this.question = question;
        titleLabel.setText("Gérer les réponses: " + question.getTitre());
        loadResponses();
    }

    @FXML
    public void initialize() {
        // Configure columns
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReponse"));
        responderColumn.setCellValueFactory(new PropertyValueFactory<>("medecin"));

        // Configure Date column
        setupDateColumn();

        // Configure Responder column
        setupResponderColumn();

        // Set items
        responsesTable.setItems(responsesList);

        // Placeholder for empty table
        responsesTable.setPlaceholder(new Label("Aucune réponse disponible"));
    }

    private void setupDateColumn() {
        dateColumn.setCellFactory(column -> new TableCell<Reponse, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });
    }

    private void setupResponderColumn() {
        responderColumn.setCellFactory(column -> new TableCell<Reponse, Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur medecin, boolean empty) {
                super.updateItem(medecin, empty);
                if (empty || medecin == null) {
                    setText(null);
                } else {
                    String displayName = (medecin.getPrenom() != null ? medecin.getPrenom() + " " : "") +
                            (medecin.getNom() != null ? medecin.getNom() : "");
                    setText(displayName.isEmpty() ? "Médecin " + medecin.getId() : displayName);
                }
            }
        });
    }

    private void loadResponses() {
        try {
            responsesList.clear();
            responsesList.addAll(responseService.afficherByQuestion(question.getId()));
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réponses", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddResponse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/AddResponse.fxml"));
            Parent root = loader.load();

            AddResponseController controller = loader.getController();
            controller.setQuestion(question);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une réponse");
            stage.showAndWait();

            // Refresh responses after adding
            loadResponses();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClose() {
        responsesTable.getScene().getWindow().hide();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}