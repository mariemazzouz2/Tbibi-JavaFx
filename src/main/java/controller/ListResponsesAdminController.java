package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;



import models.Question;
import models.Reponse;
import models.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import service.ReponseService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ListResponsesAdminController {
    @FXML
    private Label titleLabel;
    @FXML private TableView<Reponse> responsesTable;
    @FXML private TableColumn<Reponse, String> contentColumn;
    @FXML private TableColumn<Reponse, LocalDateTime> dateColumn;
    @FXML private TableColumn<Reponse, Utilisateur> responderColumn;
    @FXML private TableColumn<Reponse, Void> actionsColumn;

    private ReponseService responseService = new ReponseService();
    private ObservableList<Reponse> responsesList = FXCollections.observableArrayList();
    private Question question;

    public void setQuestion(Question question) {
        this.question = question;
        titleLabel.setText("Réponses à: " + question.getTitre());
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

        // Configure Actions column
        setupActionsColumn();

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

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");
            private final HBox hBox = new HBox(10, deleteButton);

            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                deleteButton.setOnAction(event -> {
                    Reponse reponse = getTableView().getItems().get(getIndex());
                    handleDeleteResponse(reponse);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hBox);
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

    private void handleDeleteResponse(Reponse reponse) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la réponse");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                responseService.supprimer(reponse.getId());
                loadResponses();
                showAlert("Succès", "Réponse supprimée", "La réponse a été supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression", e.getMessage(), Alert.AlertType.ERROR);
            }
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