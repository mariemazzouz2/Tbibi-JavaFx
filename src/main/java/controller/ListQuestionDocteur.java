package controller;

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
import service.QuestionService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListQuestionDocteur {
    @FXML private ScrollPane questionsScrollPane;
    @FXML private TextField searchField;
    @FXML private Pagination pagination;
    @FXML private Label labelNomUtilisateur;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button sortOrderButton;

    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionsList = FXCollections.observableArrayList();
    private ObservableList<Question> filteredQuestionsList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 6;
    private static final int COLUMNS_PER_ROW = 3;

    private String currentSortAttribute = "Titre"; // Default sort by Title
    private boolean isAscending = true; // Default sort order: Ascending

    @FXML
    public void initialize() {
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

        // Load data
        try {
            loadQuestions();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'initialisation", e.getMessage(), Alert.AlertType.ERROR);
        }

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

        Button responsesButton = new Button("Repondre");
        responsesButton.getStyleClass().add("responses-button");
        responsesButton.setOnAction(event -> handleShowResponses(question));

        HBox actionsBox = new HBox(10, responsesButton);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        String cardButtonStyle = "-fx-background-color: #5c6bc0; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: #3949ab; " +
                "-fx-border-width: 1px; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-alignment: center; " +
                "-fx-effect: dropshadow(gaussian, rgba(92, 107, 192, 0.2), 5, 0, 0, 5);";

        responsesButton.setStyle(cardButtonStyle);

        card.getChildren().addAll(placeholder, dateLabel, titleLabel, specialiteLabel, actionsBox);
        return card;
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
        List<Question> questions = questionService.afficher();
        questionsList.addAll(questions);
        System.out.println("Loaded questions - Total: " + questionsList.size());
        if (!questionsList.isEmpty()) {
            System.out.println("First loaded question - Title: " + questionsList.get(0).getTitre() + ", Date: " + questionsList.get(0).getDateCreation());
        }
        applySortAndFilter();
    }

    private void handleShowResponses(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ManageResponses .fxml"));
            Parent root = loader.load();

            ManageResponsesController controller = loader.getController();
            controller.setQuestion(question);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gérer les réponses: " + question.getTitre());
            stage.showAndWait();

            loadQuestions();
        } catch (IOException | SQLException e) {
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

    @FXML
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

    @FXML
    public void goForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuestionDocteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Questions");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}