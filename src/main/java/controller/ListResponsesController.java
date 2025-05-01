package controller;

import javafx.geometry.Insets;
import javafx.scene.Node;
import models.Question;
import models.Reponse;
import models.Utilisateur;
import models.Vote;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import service.GeminiRapport;
import service.ReponseService;
import service.VoteService;

import java.net.http.HttpClient;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ListResponsesController {
    @FXML private Label titleLabel;
    @FXML private ScrollPane responsesScrollPane;
    @FXML private Pagination pagination;

    private ReponseService responseService = new ReponseService();
    private VoteService voteService = new VoteService();
    private ObservableList<Reponse> responsesList = FXCollections.observableArrayList();
    private Question question;
    private GeminiRapport geminiRapport;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int COLUMNS_PER_ROW = 3;

    public void setQuestion(Question question) {
        this.question = question;
        titleLabel.setText("Réponses à: " + question.getTitre());
        loadResponses();
    }

    @FXML
    public void initialize() {
        // Initialize translation service
        geminiRapport = new GeminiRapport(HttpClient.newHttpClient(), null);

        // Configure Pagination
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        GridPane pageGrid = new GridPane();
        pageGrid.setHgap(20);
        pageGrid.setVgap(20);
        pageGrid.setPadding(new Insets(15));
        pageGrid.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, responsesList.size());

        if (fromIndex >= responsesList.size() || fromIndex < 0) {
            Label placeholder = new Label("Aucune réponse à afficher.");
            placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #607d8b;");
            return placeholder;
        }

        // Configure columns
        for (int i = 0; i < COLUMNS_PER_ROW; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / COLUMNS_PER_ROW);
            column.setHgrow(Priority.SOMETIMES);
            pageGrid.getColumnConstraints().add(column);
        }

        // Add response cards
        int row = 0;
        int col = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            Reponse reponse = responsesList.get(i);
            VBox card = createResponseCard(reponse);
            pageGrid.add(card, col, row);

            col++;
            if (col >= COLUMNS_PER_ROW) {
                col = 0;
                row++;
                RowConstraints rowConstraint = new RowConstraints();
                rowConstraint.setVgrow(Priority.SOMETIMES);
                pageGrid.getRowConstraints().add(rowConstraint);
            }
        }

        return pageGrid;
    }

    private VBox createResponseCard(Reponse reponse) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPrefWidth(250);
        card.setPrefHeight(220);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.2), 8, 0, 0, 2);");

        Circle placeholder = new Circle(25, javafx.scene.paint.Color.LIGHTGRAY);
        placeholder.setStyle("-fx-fill: #e0e4e8;");

        Label dateLabel = new Label(reponse.getDateReponse() != null ?
                reponse.getDateReponse().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #78909c; -fx-font-family: 'Segoe UI';");

        Label contentLabel = new Label(reponse.getContenu() != null ? reponse.getContenu() : "");
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-wrap-text: true; -fx-font-family: 'Arial';");
        contentLabel.setMaxWidth(230);
        contentLabel.setMaxHeight(60);

        String responderName = "Inconnu";
        if (reponse.getMedecin() != null) {
            responderName = (reponse.getMedecin().getPrenom() != null ? reponse.getMedecin().getPrenom() + " " : "") +
                    (reponse.getMedecin().getNom() != null ? reponse.getMedecin().getNom() : "");
            if (responderName.trim().isEmpty()) {
                responderName = "Médecin " + reponse.getMedecin().getId();
            }
        }
        Label responderLabel = new Label(responderName);
        responderLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #607d8b; -fx-font-family: 'Arial';");

        // Bouton Traduire
        Button translateButton = new Button("Traduire");
        translateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        translateButton.setOnAction(event -> showTranslationDialog(reponse.getContenu()));

        // Like/Dislike buttons (conservés si vous voulez les ajouter)
        HBox buttonsBox = new HBox(10, translateButton);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        card.getChildren().addAll(placeholder, dateLabel, contentLabel, responderLabel, buttonsBox);
        return card;
    }

    private void updatePagination() {
        int totalItems = responsesList.size();
        int pageCount = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);

        // Ensure the current page is valid
        int currentPage = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        if (currentPage < 0) currentPage = 0;
        pagination.setCurrentPageIndex(currentPage);
    }

    private void loadResponses() {
        try {
            responsesList.clear();
            responsesList.addAll(responseService.afficherByQuestion(question.getId()));

            // Sort responses by net votes (likes - dislikes), then by date (newest first) for ties
            responsesList.sort((r1, r2) -> {
                try {
                    long r1Likes = voteService.getLikeCount(r1.getId());
                    long r1Dislikes = voteService.getDislikeCount(r1.getId());
                    long r2Likes = voteService.getLikeCount(r2.getId());
                    long r2Dislikes = voteService.getDislikeCount(r2.getId());

                    long r1NetVotes = r1Likes - r1Dislikes;
                    long r2NetVotes = r2Likes - r2Dislikes;

                    // Primary sort: Net votes (descending)
                    int voteComparison = Long.compare(r2NetVotes, r1NetVotes);
                    if (voteComparison != 0) {
                        return voteComparison;
                    }

                    // Secondary sort: Date (newest first)
                    return r2.getDateReponse().compareTo(r1.getDateReponse());
                } catch (SQLException e) {
                    // If there's an error, log it and fall back to date sorting
                    System.err.println("Error sorting responses by votes: " + e.getMessage());
                    return r2.getDateReponse().compareTo(r1.getDateReponse());
                }
            });

            updatePagination();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réponses", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showTranslationDialog(String text) {
        ChoiceDialog<String> languageDialog = new ChoiceDialog<>("anglais", "français", "anglais", "italien");
        languageDialog.setTitle("Traduction");
        languageDialog.setHeaderText("Choisissez la langue de traduction");
        languageDialog.setContentText("Langue:");

        languageDialog.showAndWait().ifPresent(language -> {
            String targetLanguage = language.equals("français") ? "french" :
                    language.equals("anglais") ? "english" : "italian";

            String translatedText = geminiRapport.translateText(text, "french", targetLanguage);

            Alert translationAlert = new Alert(Alert.AlertType.INFORMATION);
            translationAlert.setTitle("Traduction");
            translationAlert.setHeaderText("Texte traduit en " + language);
            translationAlert.setContentText(translatedText);

            // Make the dialog larger to accommodate longer texts
            translationAlert.getDialogPane().setMinHeight(300);
            translationAlert.getDialogPane().setMinWidth(400);

            translationAlert.showAndWait();
        });
    }

    @FXML
    private void handleClose() {
        responsesScrollPane.getScene().getWindow().hide();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}