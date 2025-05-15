package controller;

// Importations des classes nécessaires
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import models.Question;
import models.Reponse;
import models.Utilisateur;
import models.Vote;
import enums.TypeVote;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import service.ReponseService;
import service.VoteService;
import utils.SessionManager;
import javafx.geometry.Insets;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManageResponsesController {
    // Composants UI
    @FXML private Label titleLabel;               // Titre de la fenêtre
    @FXML private ScrollPane responsesScrollPane; // Zone de défilement
    @FXML private Pagination pagination;          // Contrôle de pagination

    // Services et données
    private ReponseService responseService = new ReponseService(); // Service pour gérer les réponses
    private VoteService voteService = new VoteService();           // Service pour gérer les votes
    private ObservableList<Reponse> responsesList = FXCollections.observableArrayList(); // Liste des réponses
    private Question question; // Question associée

    // Constantes de configuration
    private static final int ITEMS_PER_PAGE = 6;    // Nombre d'items par page
    private static final int COLUMNS_PER_ROW = 3;   // Nombre de colonnes dans la grille

    /**
     * Définit la question et charge ses réponses
     * @param question La question à afficher
     */
    public void setQuestion(Question question) {
        this.question = question;
        titleLabel.setText("Gérer les réponses: " + question.getTitre());
        loadResponses(); // Charge les réponses depuis la base de données
    }

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Configure la pagination avec notre factory de pages
        pagination.setPageFactory(this::createPage);
    }

    /**
     * Crée une page de la pagination
     * @param pageIndex Index de la page à créer
     * @return Le contenu de la page
     */
    private Node createPage(int pageIndex) {
        // Crée une grille pour organiser les cartes
        GridPane pageGrid = new GridPane();
        pageGrid.setHgap(20); // Espacement horizontal
        pageGrid.setVgap(20);  // Espacement vertical
        pageGrid.setPadding(new Insets(15)); // Marge intérieure
        pageGrid.setAlignment(javafx.geometry.Pos.TOP_CENTER); // Alignement

        // Calcule les indices des éléments à afficher
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, responsesList.size());

        // Si aucune réponse à afficher
        if (fromIndex >= responsesList.size() || fromIndex < 0) {
            Label placeholder = new Label("Aucune réponse à afficher.");
            placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #607d8b;");
            return placeholder;
        }

        // Configure les colonnes de la grille
        for (int i = 0; i < COLUMNS_PER_ROW; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / COLUMNS_PER_ROW); // Largeur égale
            column.setHgrow(Priority.SOMETIMES); // Peut s'étendre
            pageGrid.getColumnConstraints().add(column);
        }

        // Ajoute les cartes de réponse
        int row = 0;
        int col = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            Reponse reponse = responsesList.get(i);
            VBox card = createResponseCard(reponse); // Crée une carte
            pageGrid.add(card, col, row); // Ajoute à la grille

            col++;
            if (col >= COLUMNS_PER_ROW) { // Passe à la ligne suivante
                col = 0;
                row++;
                RowConstraints rowConstraint = new RowConstraints();
                rowConstraint.setVgrow(Priority.SOMETIMES);
                pageGrid.getRowConstraints().add(rowConstraint);
            }
        }

        return pageGrid;
    }

    /**
     * Crée une carte visuelle pour une réponse
     * @param reponse La réponse à afficher
     * @return La carte créée
     */
    private VBox createResponseCard(Reponse reponse) {
        // Configuration de la carte
        VBox card = new VBox();
        card.setSpacing(12); // Espacement entre éléments
        card.setPrefWidth(250); // Largeur fixe
        card.setPrefHeight(220); // Hauteur fixe
        // Style CSS de la carte
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.2), 8, 0, 0, 2);");

        // Avatar placeholder
        Circle placeholder = new Circle(25, javafx.scene.paint.Color.LIGHTGRAY);
        placeholder.setStyle("-fx-fill: #e0e4e8;");

        // Date de la réponse formatée
        Label dateLabel = new Label(reponse.getDateReponse() != null ?
                reponse.getDateReponse().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #78909c; -fx-font-family: 'Segoe UI';");

        // Contenu de la réponse
        Label contentLabel = new Label(reponse.getContenu() != null ? reponse.getContenu() : "");
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-wrap-text: true; -fx-font-family: 'Arial';");
        contentLabel.setMaxWidth(230); // Largeur max
        contentLabel.setMaxHeight(60); // Hauteur max

        // Nom du médecin répondant
        String responderName = "Médecin";
        if (reponse.getMedecin() != null) {
            responderName = (reponse.getMedecin().getPrenom() != null ? reponse.getMedecin().getPrenom() + " " : "") +
                    (reponse.getMedecin().getNom() != null ? reponse.getMedecin().getNom() : "");
            if (responderName.trim().isEmpty()) {
                responderName = "Médecin " + reponse.getMedecin().getId();
            }
        }
        Label responderLabel = new Label(responderName);
        responderLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #607d8b; -fx-font-family: 'Arial';");

        // Conteneur pour les boutons de vote
        HBox voteBox = new HBox(10); // Espacement de 10px
        voteBox.setAlignment(javafx.geometry.Pos.CENTER); // Centré

        // Boutons Like/Dislike
        Button likeButton = new Button("👍");
        Button dislikeButton = new Button("👎");
        // Labels pour les compteurs
        Label likeCountLabel = new Label();
        Label dislikeCountLabel = new Label();

        try {
            // Récupère les compteurs depuis la base
            long likeCount = voteService.getLikeCount(reponse.getId());
            long dislikeCount = voteService.getDislikeCount(reponse.getId());
            likeCountLabel.setText(String.valueOf(likeCount));
            dislikeCountLabel.setText(String.valueOf(dislikeCount));
        } catch (SQLException e) {
            likeCountLabel.setText("0");
            dislikeCountLabel.setText("0");
        }

        // Style des boutons
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

        likeButton.setStyle(buttonStyle);
        dislikeButton.setStyle(buttonStyle);

        // Met à jour le style selon le vote de l'utilisateur
        updateButtonStyle(likeButton, reponse, TypeVote.Like);
        updateButtonStyle(dislikeButton, reponse, TypeVote.Dislike);

        // Gestion des événements pour Like
        likeButton.setOnAction(event -> {
            handleVote(reponse, TypeVote.Like);
            updateButtonStyle(likeButton, reponse, TypeVote.Like);
            updateButtonStyle(dislikeButton, reponse, TypeVote.Dislike);
            refreshCounts(reponse, likeCountLabel, dislikeCountLabel);
        });

        // Gestion des événements pour Dislike
        dislikeButton.setOnAction(event -> {
            handleVote(reponse, TypeVote.Dislike);
            updateButtonStyle(likeButton, reponse, TypeVote.Like);
            updateButtonStyle(dislikeButton, reponse, TypeVote.Dislike);
            refreshCounts(reponse, likeCountLabel, dislikeCountLabel);
        });

        // Ajoute les éléments au conteneur
        voteBox.getChildren().addAll(likeButton, likeCountLabel, dislikeButton, dislikeCountLabel);

        // Ajoute tous les éléments à la carte
        card.getChildren().addAll(placeholder, dateLabel, contentLabel, responderLabel, voteBox);
        return card;
    }

    /**
     * Met à jour les compteurs de votes
     */
    private void refreshCounts(Reponse reponse, Label likeCountLabel, Label dislikeCountLabel) {
        try {
            long likeCount = voteService.getLikeCount(reponse.getId());
            long dislikeCount = voteService.getDislikeCount(reponse.getId());
            likeCountLabel.setText(String.valueOf(likeCount));
            dislikeCountLabel.setText(String.valueOf(dislikeCount));
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour des votes", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Met à jour la pagination
     */
    private void updatePagination() {
        int totalItems = responsesList.size();
        int pageCount = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);

        // Assure que la page courante est valide
        int currentPage = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        if (currentPage < 0) currentPage = 0;
        pagination.setCurrentPageIndex(currentPage);
    }

    /**
     * Met à jour le style d'un bouton de vote
     */
    private void updateButtonStyle(Button button, Reponse reponse, TypeVote voteType) {
        try {
            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null || currentUser.getId() == 0) {
                button.setStyle(""); // Pas de style si non connecté
                return;
            }
            // Vérifie si l'utilisateur a déjà voté
            Vote vote = voteService.getVoteByUserAndResponse(currentUser.getId(), reponse.getId());
            boolean hasVoted = vote != null && vote.getValeur() == voteType;
            // Met en vert si déjà voté
            button.setStyle(hasVoted ? "-fx-background-color: #90EE90;" : "");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la vérification du vote", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Gère une action de vote
     */
    private void handleVote(Reponse reponse, TypeVote voteType) {
        try {
            // Vérifie la connexion
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert("Erreur", "Utilisateur non authentifié",
                        "Veuillez vous connecter pour voter.", Alert.AlertType.ERROR);
                return;
            }

            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            // Récupère le vote existant
            Vote existingVote = voteService.getVoteByUserAndResponse(currentUser.getId(), reponse.getId());

            if (existingVote != null) {
                if (existingVote.getValeur() == voteType) {
                    // Supprime si même vote
                    voteService.supprimer(existingVote.getId());
                } else {
                    // Modifie si vote différent
                    existingVote.setValeur(voteType);
                    voteService.modifier(existingVote);
                }
            } else {
                // Crée un nouveau vote
                Vote newVote = new Vote(currentUser, reponse, voteType);
                voteService.ajouter(newVote);
            }

            // Rafraîchit l'affichage
            pagination.setPageFactory(this::createPage);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la gestion du vote", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Charge les réponses depuis la base de données
     */
    private void loadResponses() {
        try {
            responsesList.clear();
            responsesList.addAll(responseService.afficherByQuestion(question.getId()));
            updatePagination(); // Met à jour la pagination
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réponses", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Ouvre la fenêtre d'ajout de réponse
     */
    @FXML
    private void handleAddResponse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddResponse.fxml"));
            Parent root = loader.load();

            AddResponseController controller = loader.getController();
            controller.setQuestion(question);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une réponse");
            stage.showAndWait();

            loadResponses(); // Recharge après fermeture
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Ferme la fenêtre
     */
    @FXML
    private void handleClose() {
        responsesScrollPane.getScene().getWindow().hide();
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}