
package controller;

import models.Question;
import enums.Specialite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.QuestionService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ListQuestionController {
    @FXML private FlowPane cardsContainer; // Changé de VBox à FlowPane
    @FXML private ScrollPane scrollPane;
    @FXML private Button addButton;
    @FXML private TextField searchField;

    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionsList = FXCollections.observableArrayList();
    private ObservableList<Question> filteredQuestionsList = FXCollections.observableArrayList();
    private final int currentPatientId = 1;

    @FXML
    public void initialize() {
        // Style des composants
        addButton.setStyle("-fx-background-color: #2d5985; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 15;");
        searchField.setStyle("-fx-background-radius: 15; -fx-padding: 5 15; -fx-background-color: white; -fx-border-color: #cccccc;");

        // Configuration du ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        // Initialisation des listes
        filteredQuestionsList.addAll(questionsList);

        // Chargement des données
        try {
            loadQuestions();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'initialisation", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        filteredQuestionsList.clear();

        if (searchText.isEmpty()) {
            filteredQuestionsList.addAll(questionsList);
        } else {
            for (Question q : questionsList) {
                if (q.getTitre().toLowerCase().contains(searchText) ||
                        (q.getSpecialite() != null && q.getSpecialite().getValue().toLowerCase().contains(searchText))) {
                    filteredQuestionsList.add(q);
                }
            }
        }

        displayQuestions();
    }

    private void loadQuestions() throws SQLException {
        questionsList.clear();
        questionsList.addAll(questionService.afficher().stream()
                .filter(q -> q.getPatient().getId() == currentPatientId)
                .toList());
        handleSearch();
    }

    private void displayQuestions() {
        cardsContainer.getChildren().clear();

        for (Question question : filteredQuestionsList) {
            VBox card = createQuestionCard(question);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); " +
                "-fx-padding: 15; -fx-spacing: 10;");
        card.setPrefWidth(400); // au lieu de 850


        // Titre
        Label titleLabel = new Label(question.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Spécialité et Date
        HBox infoBox = new HBox(10);
        Label specialiteLabel = new Label(question.getSpecialite() != null ? question.getSpecialite().getValue() : "Non spécifié");
        specialiteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label dateLabel = new Label(question.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        infoBox.getChildren().addAll(specialiteLabel, dateLabel);

        // Image (si disponible)
        if (question.getImage() != null && !question.getImage().isEmpty()) {
            try {
                Image image = new Image(question.getImage());
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);
                    card.getChildren().add(imageView);
                }
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image: " + e.getMessage());
            }
        }

        // Statut de visibilité
        Label visibleLabel = new Label(question.isVisible() ? "Public" : "Privé");
        visibleLabel.setStyle("-fx-font-size: 14px; " +
                (question.isVisible() ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;"));

        // Boutons d'actions
        HBox buttonsBox = new HBox(10);
        buttonsBox.setStyle("-fx-alignment: CENTER_RIGHT;");

        Button editButton = createActionButton("Modifier", "#3498db", () -> handleEditQuestion(question));
        Button deleteButton = createActionButton("Supprimer", "#e74c3c", () -> handleDeleteQuestion(question));
        Button responsesButton = createActionButton("Réponses", "#2ecc71", () -> handleShowResponses(question));

        buttonsBox.getChildren().addAll(editButton, deleteButton, responsesButton);

        // Ajout des éléments à la carte
        card.getChildren().addAll(titleLabel, infoBox, visibleLabel, buttonsBox);

        return card;
    }

    private Button createActionButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 4;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -20%); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 4;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 4;"));

        button.setOnAction(e -> action.run());
        return button;
    }

    @FXML
    private void handleAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/AddQuestion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Poser une nouvelle question");
            stage.showAndWait();

            loadQuestions();
        } catch (IOException | SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditQuestion(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/EditQuestion.fxml"));
            Parent root = loader.load();

            EditQuestionController controller = loader.getController();
            controller.setQuestion(question);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier la question");
            stage.showAndWait();

            loadQuestions();
        } catch (IOException | SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteQuestion(Question question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la question");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette question ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                questionService.supprimer(question.getId());
                loadQuestions();
                showAlert("Succès", "Question supprimée", "La question a été supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleShowResponses(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/ListResponses.fxml"));
            Parent root = loader.load();

            ListResponsesController controller = loader.getController();
            controller.setQuestion(question);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Réponses à la question: " + question.getTitre());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture des réponses", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}