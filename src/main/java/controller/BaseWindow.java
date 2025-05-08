package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Utilisateur;
import service.UtilisateurDAO;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaseWindow {

    @FXML private AnchorPane mainRouter;
    @FXML private TableView<Utilisateur> tableViewUtilisateurs;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Void> colActions;
    @FXML private TableColumn<Utilisateur, String> colAdresse;
    @FXML private TableColumn<Utilisateur, String> colDateNaissance;
    @FXML private TableColumn<Utilisateur, String> colTelephone;
    @FXML private TableColumn<Utilisateur, String> colSexe;
    @FXML private TextField filterNomField;
    @FXML private ComboBox<String> filterRoleBox;
    @FXML private ComboBox<String> filterSexeBox;
    @FXML private DatePicker filterDateNaissanceStartPicker;
    @FXML private DatePicker filterDateNaissanceEndPicker;
    @FXML private TextField searchField;
    @FXML private Pagination pagination;
    @FXML private Label labelNomUtilisateur;

    private List<Utilisateur> utilisateurs;
    private List<Utilisateur> filteredUtilisateurs;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private static final int pageSize = 10;

    @FXML
    public void initialize() {
        // Initialize utilisateurs list to avoid NullPointerException
        utilisateurs = new ArrayList<>();
        try {
            utilisateurs = utilisateurDAO.getAllUsers();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }

        // Set up user label
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText(currentUser.getNom());
        } else {
            labelNomUtilisateur.setText("Utilisateur inconnu");
        }

        // Initialize TableView columns
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> {
            String rawRoles = data.getValue().getRoles();
            String cleanedRole = rawRoles.replaceAll("[\\[\\]\"]", "");
            String[] roles = cleanedRole.split(",");
            return new SimpleStringProperty(roles.length > 0 ? roles[0].trim() : "Aucun rôle");
        });
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        colDateNaissance.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateNaissance().toString()));
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTelephone())));
        colSexe.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSexe()));

        // Set up filters
        ObservableList<String> roles = FXCollections.observableArrayList("ROLE_PATIENT", "ROLE_MEDECIN");
        filterRoleBox.setItems(roles);
        ObservableList<String> sexes = FXCollections.observableArrayList("Homme", "Femme");
        filterSexeBox.setItems(sexes);

        // Set up pagination
        updatePagination();

        // Add action buttons
        ajouterBoutonsActions();

        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearchClicked());
    }

    private void updatePagination() {
        List<Utilisateur> sourceList = (filteredUtilisateurs != null && !filteredUtilisateurs.isEmpty()) ? filteredUtilisateurs : utilisateurs;
        int totalPageCount = (int) Math.ceil((double) sourceList.size() / pageSize);
        pagination.setPageCount(Math.max(totalPageCount, 1));
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        List<Utilisateur> sourceList = (filteredUtilisateurs != null && !filteredUtilisateurs.isEmpty()) ? filteredUtilisateurs : utilisateurs;
        int start = pageIndex * pageSize;
        int end = Math.min(start + pageSize, sourceList.size());
        List<Utilisateur> pageData = sourceList.subList(start, end);
        tableViewUtilisateurs.setItems(FXCollections.observableArrayList(pageData));
        return new VBox();
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Modifier.fxml"));
                        Parent root = loader.load();
                        ModifierController controller = loader.getController();
                        controller.initData(utilisateur);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Modifier Utilisateur");
                        stage.show();
                    } catch (IOException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de modification : " + e.getMessage());
                    }
                });

                btnSupprimer.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression d'un utilisateur");
                    alert.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            utilisateurDAO.supprimerUtilisateur(utilisateur.getId());
                            utilisateurs.remove(utilisateur);
                            updatePagination();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    @FXML
    private void onSearchClicked() {
        String keyword = searchField.getText().toLowerCase().trim();
        filteredUtilisateurs = utilisateurs.stream()
                .filter(u -> u.getNom().toLowerCase().contains(keyword) ||
                        u.getPrenom().toLowerCase().contains(keyword) ||
                        u.getEmail().toLowerCase().contains(keyword) ||
                        u.getRoles().toLowerCase().contains(keyword) ||
                        u.getAdresse().toLowerCase().contains(keyword) ||
                        u.getSexe().toLowerCase().contains(keyword) ||
                        String.valueOf(u.getTelephone()).contains(keyword) ||
                        u.getDateNaissance().toString().contains(keyword))
                .toList();
        updatePagination();
    }

    @FXML
    private void onAppliquerFiltres() {
        String nom = filterNomField.getText().toLowerCase().trim();
        String selectedRole = filterRoleBox.getValue();
        String finalSelectedRole = (selectedRole != null) ? selectedRole.trim() : "";
        String sexe = filterSexeBox.getValue();
        LocalDate startDate = filterDateNaissanceStartPicker.getValue();
        LocalDate endDate = filterDateNaissanceEndPicker.getValue();

        filteredUtilisateurs = utilisateurs.stream()
                .filter(u -> {
                    String cleanedRole = u.getRoles().replace("[", "").replace("]", "").replace("\"", "");
                    return (nom.isEmpty() || u.getNom().toLowerCase().contains(nom)) &&
                            (finalSelectedRole.isEmpty() || cleanedRole.contains(finalSelectedRole)) &&
                            (sexe == null || u.getSexe().equalsIgnoreCase(sexe)) &&
                            (startDate == null || !u.getDateNaissance().isBefore(startDate)) &&
                            (endDate == null || !u.getDateNaissance().isAfter(endDate));
                })
                .toList();

        pagination.setCurrentPageIndex(0);
        updatePagination();
    }

    @FXML
    private void onResetFiltres() {
        filterNomField.clear();
        filterRoleBox.setValue(null);
        filterSexeBox.setValue(null);
        filterDateNaissanceStartPicker.setValue(null);
        filterDateNaissanceEndPicker.setValue(null);
        filteredUtilisateurs = null;
        pagination.setCurrentPageIndex(0);
        updatePagination();
    }

    public void goToAddUser(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Utilisateur");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'ajout : " + e.getMessage());
        }
    }

    public void goToListeDemandes(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendDemande.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Demandes");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des demandes : " + e.getMessage());
        }
    }

    public void goToListeUsers(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des utilisateurs : " + e.getMessage());
        }
    }

    public void goToSmsForm(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sms_form.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Envoyer SMS");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire SMS : " + e.getMessage());
        }
    }

    public void goToListeStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statadmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    public void goToEvent(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEventBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Événements");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des événements : " + e.getMessage());
        }
    }

    public void goToCategory(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category/AfficherCategorie.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Catégories");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des catégories : " + e.getMessage());
        }
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de connexion : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}