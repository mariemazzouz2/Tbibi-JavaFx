package controller.Evenement;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Utilisateur;
import service.EvenementService;
import tests.Patient.PatientConsultationFormController;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static utils.AlertUtils.showAlert;

public class AfficherEvent implements Initializable {

    @FXML private VBox rootVBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button calendarButton;
    @FXML private FlowPane cardsContainer;
    @FXML private Button previousPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private VBox chatBox;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private Button sendChatButton;
    @FXML private Pane mainRouter;
    @FXML private Label labelNomUtilisateur;

    private final EvenementService evenementService = new EvenementService();
    private List<Evenement> allEvenements;
    private int currentPage = 1;
    private final int eventsPerPage = 6;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Validate UI components
        if (sortComboBox == null) {
            System.err.println("sortComboBox is null. Check FXML file for fx:id='sortComboBox'.");
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", null, "Le composant de tri est manquant dans l'interface.");
            return;
        }
        if (searchField == null) {
            System.err.println("searchField is null. Check FXML file for fx:id='searchField'.");
        }
        if (cardsContainer == null) {
            System.err.println("cardsContainer is null. Check FXML file for fx:id='cardsContainer'.");
        }
        if (mainRouter == null) {
            System.err.println("mainRouter is null. Check FXML file for fx:id='mainRouter'.");
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", null, "Le conteneur principal est manquant dans l'interface.");
            return;
        }

        // Initialize sortComboBox options
        sortComboBox.getItems().addAll("Titre", "Date de début", "Lieu");
        sortComboBox.setValue("Titre");

        // Add listeners for search and sort
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterAndSortEvenements());
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> filterAndSortEvenements());

        // Load events
        loadEvenements();
    }

    private void loadEvenements() {
        try {
            System.out.println("Chargement des événements...");
            allEvenements = evenementService.recuperer();
            System.out.println("Événements chargés : " + allEvenements.size());
            filterAndSortEvenements();
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des événements : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger les événements : " + e.getMessage());
        }
    }

    private void filterAndSortEvenements() {
        if (cardsContainer == null || pageInfoLabel == null || previousPageButton == null || nextPageButton == null) {
            System.err.println("UI components are missing (cardsContainer, pageInfoLabel, or pagination buttons).");
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Composants d'interface manquants.");
            return;
        }

        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        String sortBy = sortComboBox.getValue() != null ? sortComboBox.getValue() : "Titre";

        List<Evenement> filtered = allEvenements.stream()
                .filter(e -> e.getTitre().toLowerCase().contains(searchText) ||
                        e.getLieu().toLowerCase().contains(searchText))
                .sorted((e1, e2) -> {
                    return switch (sortBy) {
                        case "Date de début" -> e1.getDateDebut().compareTo(e2.getDateDebut());
                        case "Lieu" -> e1.getLieu().compareToIgnoreCase(e2.getLieu());
                        default -> e1.getTitre().compareToIgnoreCase(e2.getTitre());
                    };
                })
                .collect(Collectors.toList());

        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / eventsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        pageInfoLabel.setText("Page " + currentPage + " de " + totalPages);
        previousPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage == totalPages);

        cardsContainer.getChildren().clear();

        if (filtered.isEmpty()) {
            System.out.println("Aucun événement correspondant à afficher.");
            Label noEvents = new Label("Aucun événement trouvé.");
            noEvents.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            cardsContainer.getChildren().add(noEvents);
            return;
        }

        int start = (currentPage - 1) * eventsPerPage;
        int end = Math.min(start + eventsPerPage, filtered.size());
        System.out.println("Affichage des événements de l'index " + start + " à " + (end - 1));

        for (int i = start; i < end; i++) {
            try {
                loadEventCard(filtered.get(i));
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la carte pour l'événement : " + filtered.get(i).getTitre());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors du chargement de l'événement : " + e.getMessage());
            }
        }
        System.out.println("Nombre de cartes ajoutées : " + cardsContainer.getChildren().size());
    }

    private void loadEventCard(Evenement evenement) throws IOException {
        EvenementCard card = new EvenementCard();
        if (mainRouter != null) {
            card.setContainer(mainRouter);
        } else {
            System.err.println("mainRouter is null in loadEventCard. Navigation in EvenementCard may not work.");
        }
        card.setEvenement(evenement);
        cardsContainer.getChildren().add(card.getRoot());
    }

    @FXML
    private void onPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            filterAndSortEvenements();
        }
    }

    @FXML
    private void onNextPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allEvenements.size() / eventsPerPage));
        if (currentPage < totalPages) {
            currentPage++;
            filterAndSortEvenements();
        }
    }

    @FXML
    private void onCalendarButtonClick(ActionEvent event) {
        if (mainRouter == null) {
            System.err.println("mainRouter is null. Cannot load calendar view.");
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Le conteneur principal est manquant pour afficher le calendrier.");
            return;
        }

        try {
            System.out.println("Ouverture de la vue du calendrier...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/CalendarView.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier /Event/CalendarView.fxml introuvable.");
            }
            Node calendarRoot = loader.load();
            CalendarViewController controller = loader.getController();
            if (allEvenements != null && !allEvenements.isEmpty()) {
                controller.setEventList(allEvenements);
            } else {
                System.err.println("Event list is null or empty");
                showAlert(Alert.AlertType.WARNING, "Aucun événement", null, "Aucun événement disponible pour afficher dans le calendrier.");
                return;
            }
            mainRouter.getChildren().setAll(calendarRoot);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du calendrier : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors du chargement du calendrier : " + e.getMessage());
        }
    }

    @FXML
    private void sendChatMessage() {
        if (chatInput == null || chatArea == null) {
            System.err.println("Chat components are missing.");
            return;
        }
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        chatArea.appendText("Vous : " + userMessage + "\n");
        chatInput.clear();

        String simulatedResponse = "Réponse simulée : Merci pour votre message ! (" + userMessage + ")";
        chatArea.appendText("Assistant : " + simulatedResponse + "\n");
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
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la page de connexion : " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger le forum : " + e.getMessage());
        }
    }

    public void gotoconsultations(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Consultations");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des consultations : " + e.getMessage());
        }
    }

    public void Bookapt(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_form.fxml"));
            Parent root = loader.load();

            PatientConsultationFormController controller = loader.getController();
            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            entities.Utilisateur currentUserobj = new entities.Utilisateur();
            currentUserobj.setId(currentUser.getId());
            currentUserobj.setNom(currentUser.getNom());
            currentUserobj.setAdresse(currentUser.getAdresse());
            currentUserobj.setImage(currentUser.getImage());
            currentUserobj.setPrenom(currentUser.getPrenom());
            currentUserobj.setDateNaissance(currentUser.getDateNaissance());
            currentUserobj.setEmail(currentUser.getEmail());
            currentUserobj.setTelephone(currentUser.getTelephone());
            controller.setCurrentUser(currentUserobj);
            controller.setMode("create");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Planifier Consultation");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger le formulaire de consultation : " + e.getMessage());
        }
    }

    public void goEvement(ActionEvent actionEvent) {
        if (mainRouter == null) {
            System.err.println("mainRouter is null. Cannot load event view.");
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Le conteneur principal est manquant pour afficher les événements.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEvent.fxml"));
            Node eventRoot = loader.load();
            mainRouter.getChildren().setAll(eventRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setTitle("Événements");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la vue des événements : " + e.getMessage());
        }
    }
}