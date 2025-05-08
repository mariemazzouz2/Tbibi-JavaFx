package controller;

import controller.ListResponsesController;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import models.Question;
import enums.Specialite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import service.GeminiRapport;
import service.QuestionService;
import utils.SessionManager;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListQuestionController {
    @FXML private ScrollPane questionsScrollPane;
    @FXML private Button addButton;
    @FXML private TextField searchField;
    @FXML private Button chatbotBubble;
    @FXML private VBox chatWindow;
    @FXML private TextArea chatHistory;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;
    @FXML private Pagination pagination;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button sortOrderButton;

    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionsList = FXCollections.observableArrayList();
    private ObservableList<Question> filteredQuestionsList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 6;
    private static final int COLUMNS_PER_ROW = 3;
    private GeminiRapport geminiRapport;
    private String currentSortAttribute = "Titre"; // Default sort by Title
    private boolean isAscending = true; // Default sort order: Ascending

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert("Erreur", "Connexion requise",
                    "Vous devez être connecté pour voir vos questions.", Alert.AlertType.ERROR);
            closeWindow();
            return;
        }

        geminiRapport = new GeminiRapport(HttpClient.newHttpClient(), LoggerFactory.getLogger(GeminiRapport.class));

        // Configure Pagination
        pagination.setPageFactory(this::createPage);

        // Initialize sort ComboBox
        sortComboBox.setItems(FXCollections.observableArrayList("Titre", "Date"));
        sortComboBox.setValue("Titre"); // Default selection
        sortComboBox.setOnAction(event -> {
            System.out.println("Sort ComboBox changed to: " + sortComboBox.getValue());
            currentSortAttribute = sortComboBox.getValue();
            applySortAndFilter();
        });

        // Initialize sort order button
        sortOrderButton.setText(isAscending ? "⬆" : "⬇");
        sortOrderButton.setOnAction(event -> {
            System.out.println("Sort order button clicked. Current ascending: " + isAscending);
            handleSort(event);
        });

        try {
            loadQuestions();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'initialisation", e.getMessage(), Alert.AlertType.ERROR);
        }

        // Configuration du chat
        chatInput.setOnAction(event -> handleSendMessage());
        chatHistory.appendText("Chatbot: Bonjour ! Je suis ici pour répondre à vos questions médicales.\n\n");

        // Apply consistent styling to buttons
        String buttonStyle = "-fx-background-color: #5c6bc0; "
                + "-fx-text-fill: white; "
                + "-fx-background-radius: 5; "
                + "-fx-border-radius: 5; "
                + "-fx-border-color: #3949ab; "
                + "-fx-border-width: 1px; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 14px; "
                + "-fx-alignment: center; "
                + "-fx-effect: dropshadow(gaussian, rgba(92, 107, 192, 0.2), 5, 0, 0, 5);";

        addButton.setStyle(buttonStyle);
        sendButton.setStyle(buttonStyle);
        sortOrderButton.setStyle(buttonStyle);

        // Add search listener
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("Search field changed: " + newValue);
            applySortAndFilter();
        });
    }

    private Node createPage(int pageIndex) {
        GridPane pageGrid = new GridPane();
        pageGrid.setHgap(20);
        pageGrid.setVgap(20);
        pageGrid.setPadding(new Insets(15));
        pageGrid.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredQuestionsList.size());

        if (fromIndex >= filteredQuestionsList.size() || fromIndex < 0) {
            System.out.println("Page index " + pageIndex + " is out of bounds. Filtered list size: " + filteredQuestionsList.size());
            Label placeholder = new Label("Aucune question à afficher.");
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

        // Add question cards
        int row = 0;
        int col = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            Question question = filteredQuestionsList.get(i);
            VBox card = createQuestionCard(question);
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

        System.out.println("Created page " + pageIndex + " with " + (toIndex - fromIndex) + " items");
        return pageGrid;
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPrefWidth(250);
        card.setPrefHeight(180);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.2), 8, 0, 0, 2);");

        Circle placeholder = new Circle(25, javafx.scene.paint.Color.LIGHTGRAY);
        placeholder.setStyle("-fx-fill: #e0e4e8;");

        Label dateLabel = new Label(question.getDateCreation() != null ?
                question.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #78909c; -fx-font-family: 'Segoe UI';");

        Label titleLabel = new Label(question.getTitre() != null ? question.getTitre() : "");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3949ab; -fx-font-family: 'Segoe UI';");

        Label specialiteLabel = new Label(question.getSpecialite() != null ? question.getSpecialite().getValue() : "");
        specialiteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #607d8b; -fx-wrap-text: true; -fx-font-family: 'Arial';");

        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        Button responsesButton = new Button("Réponses");

        editButton.getStyleClass().add("edit-button");
        deleteButton.getStyleClass().add("delete-button");
        responsesButton.getStyleClass().add("responses-button");

        editButton.setOnAction(event -> handleEditQuestion(question));
        deleteButton.setOnAction(event -> handleDeleteQuestion(question));
        responsesButton.setOnAction(event -> handleShowResponses(question));

        HBox actionsBox = new HBox(10, editButton, deleteButton, responsesButton);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        String cardButtonStyle = "-fx-background-color: #5c6bc0; "
                + "-fx-text-fill: white; "
                + "-fx-background-radius: 5; "
                + "-fx-border-radius: 5; "
                + "-fx-border-color: #3949ab; "
                + "-fx-border-width: 1px; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 14px; "
                + "-fx-alignment: center; "
                + "-fx-effect: dropshadow(gaussian, rgba(92, 107, 192, 0.2), 5, 0, 0, 5);";

        editButton.setStyle(cardButtonStyle);
        deleteButton.setStyle(cardButtonStyle);
        responsesButton.setStyle(cardButtonStyle);

        card.getChildren().addAll(placeholder, dateLabel, titleLabel, specialiteLabel, actionsBox);
        return card;
    }

    private void applySortAndFilter() {
        String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        System.out.println("Applying sort and filter - Search text: '" + searchText + "', Sort attribute: " + currentSortAttribute + ", Ascending: " + isAscending);

        filteredQuestionsList.clear();
        System.out.println("Cleared filteredQuestionsList. Size: " + filteredQuestionsList.size());

        // Step 1: Apply search filter
        List<Question> tempList = questionsList.stream()
                .filter(q -> {
                    if (searchText.isEmpty()) return true;
                    boolean matchesTitle = q.getTitre() != null && q.getTitre().toLowerCase().contains(searchText);
                    boolean matchesSpecialite = q.getSpecialite() != null && q.getSpecialite().getValue().toLowerCase().contains(searchText);
                    return matchesTitle || matchesSpecialite;
                })
                .collect(Collectors.toList());
        System.out.println("After search filter - Temp list size: " + tempList.size());
        if (!tempList.isEmpty()) {
            System.out.println("First item after filter - Title: " + tempList.get(0).getTitre() + ", Date: " + tempList.get(0).getDateCreation());
        }

        // Step 2: Apply sorting
        Comparator<Question> comparator = null;
        if ("Titre".equals(currentSortAttribute)) {
            System.out.println("Sorting by Titre");
            comparator = Comparator.comparing(Question::getTitre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("Date".equals(currentSortAttribute)) {
            System.out.println("Sorting by Date");
            comparator = Comparator.comparing(Question::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            System.out.println("No valid sort attribute selected");
        }

        if (comparator != null) {
            if (!isAscending) {
                comparator = comparator.reversed();
                System.out.println("Reversed comparator for descending order");
            }
            filteredQuestionsList.setAll(tempList.stream().sorted(comparator).collect(Collectors.toList()));
            System.out.println("After sorting - Filtered list size: " + filteredQuestionsList.size());
            if (!filteredQuestionsList.isEmpty()) {
                System.out.println("First item after sort - Title: " + filteredQuestionsList.get(0).getTitre() + ", Date: " + filteredQuestionsList.get(0).getDateCreation());
            }
        } else {
            filteredQuestionsList.setAll(tempList);
            System.out.println("No sorting applied - Filtered list size: " + filteredQuestionsList.size());
        }

        updatePagination();
    }

    @FXML
    private void handleSort(ActionEvent event) {
        isAscending = !isAscending; // Toggle sort order
        sortOrderButton.setText(isAscending ? "⬆" : "⬇");
        System.out.println("Sort order toggled to: " + (isAscending ? "Ascending" : "Descending"));
        applySortAndFilter();
    }

    private void loadQuestions() throws SQLException {
        questionsList.clear();
        int patientId = SessionManager.getInstance().getCurrentUser().getId();
        questionsList.addAll(questionService.getByPatientId(patientId));
        System.out.println("Loaded questions - Total: " + questionsList.size());
        if (!questionsList.isEmpty()) {
            System.out.println("First loaded question - Title: " + questionsList.get(0).getTitre() + ", Date: " + questionsList.get(0).getDateCreation());
        }
        applySortAndFilter();
    }

    private void updatePagination() {
        int totalItems = filteredQuestionsList.size();
        int pageCount = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);

        // Ensure the current page is valid
        int currentPage = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        if (currentPage < 0) currentPage = 0;
        pagination.setCurrentPageIndex(currentPage);

        // Update ScrollPane content
        if (filteredQuestionsList.isEmpty()) {
            Label placeholder = new Label("Aucune question trouvée");
            placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #607d8b;");
            questionsScrollPane.setContent(placeholder);
        } else {
            questionsScrollPane.setContent(pagination);
            // Force refresh by re-setting page factory
            pagination.setPageFactory(this::createPage);
        }

        System.out.println("Updated pagination - Total items: " + totalItems + ", Page count: " + pageCount + ", Current page: " + currentPage);
    }

    @FXML
    private void handleAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddQuestion.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditQuestion.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListResponses.fxml"));
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

    @FXML
    private void toggleChatWindow() {
        chatWindow.setVisible(!chatWindow.isVisible());
        if (chatWindow.isVisible()) {
            chatInput.requestFocus();
        }
    }

    @FXML
    private void handleSendMessage() {
        String question = chatInput.getText().trim();
        if (question.isEmpty()) {
            return;
        }
        chatHistory.appendText("Vous: " + question + "\n");
        String response = geminiRapport.answerMedicalQuestion(question);
        chatHistory.appendText("Chatbot: " + response + "\n\n");
        chatInput.clear();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) chatWindow.getScene().getWindow();
        stage.close();
    }

    public void logout(ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuestion.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Questions");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goEvement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEvent.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(" Evenement");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}