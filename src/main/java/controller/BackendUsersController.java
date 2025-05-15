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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Utilisateur;
import service.UtilisateurDAO;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BackendUsersController {

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
    private List<Utilisateur> utilisateurs;
    @FXML private TextField filterNomField;
    @FXML private ComboBox<String> filterRoleBox;

    @FXML private ComboBox<String> filterSexeBox;
    @FXML private DatePicker filterDateNaissanceStartPicker;
    @FXML private DatePicker filterDateNaissanceEndPicker;
    private List<Utilisateur> filteredUtilisateurs;



    private static final int pageSize = 10; // Taille de la page

    @FXML
    private Pagination pagination;
    @FXML
    public void goToListeDemandes(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendDemande.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Demandes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToListeUsers(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Label labelNomUtilisateur;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        try {
            utilisateurs = utilisateurDAO.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        int totalPages = (int) Math.ceil((double) utilisateurs.size() / pageSize);
        pagination.setPageCount(totalPages);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        if (currentUser != null) {
            labelNomUtilisateur.setText( currentUser.getNom());
        }
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> {
            String rawRoles = data.getValue().getRoles(); // Exemple : ["ROLE_PATIENT"]
            String cleanedRole = rawRoles.replaceAll("[\\[\\]\"]", ""); // Retire [ ] et "
            String[] roles = cleanedRole.split(",");
            return new SimpleStringProperty(roles.length > 0 ? roles[0].trim() : "Aucun rôle");
        });


        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        colDateNaissance.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateNaissance().toString()));
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTelephone())));
        colSexe.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSexe()));

        ajouterBoutonsActions();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            onSearchClicked();
        });
        ObservableList<String> roles = FXCollections.observableArrayList("ROLE_PATIENT", "ROLE_MEDECIN");
        filterRoleBox.setItems(roles);


    }

    public void logout(javafx.event.ActionEvent event) {
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

                        // Récupération du contrôleur de la vue
                        ModifierController controller = loader.getController();
                        controller.initData(utilisateur); // Passe les données à préremplir

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Modifier Utilisateur");
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
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
                            try {
                                utilisateurDAO.supprimerUtilisateur(utilisateur.getId());
                                tableViewUtilisateurs.getItems().remove(utilisateur);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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


    public void goToAddUser(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Node createPage(int pageIndex) {
        int start = pageIndex * pageSize;
        int end = Math.min(start + pageSize, utilisateurs.size());
        List<Utilisateur> pageData = utilisateurs.subList(start, end);

        ObservableList<Utilisateur> pageItems = FXCollections.observableArrayList(pageData);
        tableViewUtilisateurs.setItems(pageItems);

        // On peut renvoyer n'importe quel Node ici, mais on a déjà la TableView affichée
        return new Label(); // ou même `return null;` ça fonctionnera aussi
    }
    @FXML
    private TextField searchField;

    @FXML
    private void onSearchClicked() {
        String keyword = searchField.getText().toLowerCase();
        List<Utilisateur> filteredList = utilisateurs.stream()
                .filter(u -> u.getNom().toLowerCase().contains(keyword) ||
                        u.getPrenom().toLowerCase().contains(keyword) ||
                        u.getEmail().toLowerCase().contains(keyword) ||
                        u.getRoles().toLowerCase().contains(keyword) ||
                        u.getAdresse().toLowerCase().contains(keyword) ||
                        u.getSexe().toLowerCase().contains(keyword) ||
                        String.valueOf(u.getTelephone()).contains(keyword) ||
                        u.getDateNaissance().toString().contains(keyword))
                .toList();

        ObservableList<Utilisateur> filteredObservableList = FXCollections.observableArrayList(filteredList);
        tableViewUtilisateurs.setItems(filteredObservableList);
        pagination.setPageCount(1); // Plus de pagination pour une recherche
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

    private void updatePagination() {
        List<Utilisateur> sourceList = (filteredUtilisateurs != null) ? filteredUtilisateurs : utilisateurs;

        int totalPageCount = (int) Math.ceil((double) sourceList.size() / pageSize);

        pagination.setPageCount(Math.max(totalPageCount, 1));

        pagination.setPageFactory(pageIndex -> {
            int fromIndex = pageIndex * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, sourceList.size());
            tableViewUtilisateurs.setItems(FXCollections.observableArrayList(sourceList.subList(fromIndex, toIndex)));
            return new VBox(); // Obligatoire pour le setPageFactory
        });
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
    @FXML
    public void goToSmsForm(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sms_form.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Envoyer SMS");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToListeStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statadmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /// ///////////////////////
    public void goToEvent(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEventBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Event");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToCategory(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category/AfficherCategorieEv.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Catagorie");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
