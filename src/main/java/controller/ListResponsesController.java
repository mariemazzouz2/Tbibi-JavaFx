package controller;


import models.*;
import models.Reponse;

import models.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.ReponseService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class ListResponsesController {
    @FXML private Label titleLabel;
    @FXML private TableView<Reponse> responsesTable;
    @FXML private TableColumn<Reponse, String> contentColumn;
    @FXML private TableColumn<Reponse, LocalDateTime> dateColumn;
    @FXML private TableColumn<Reponse, String> responderColumn; // Changé en String

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
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReponse")); // Changé de dateCreation à dateReponse
        responderColumn.setCellValueFactory(cellData -> {
            Utilisateur medecin = cellData.getValue().getMedecin();
            return new SimpleStringProperty(medecin != null ?
                    medecin.getNom() : "Inconnu");
        });

        // Configure Date column
        setupDateColumn();

        // Set items
        responsesTable.setItems(responsesList);
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

    private void loadResponses() {
        try {
            responsesList.clear();
            responsesList.addAll(responseService.afficherByQuestion(question.getId()));
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du chargement des réponses");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        responsesTable.getScene().getWindow().hide();
    }
}
