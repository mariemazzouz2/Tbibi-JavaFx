package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Utilisateur;
import service.UtilisateurDAO;
import utils.SessionManager;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.SQLException;

public class BackendUsersController {
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

    @FXML
    private Label labelNomUtilisateur;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText("Bienvenue, " + currentUser.getNom());
        }
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoles()));
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        colDateNaissance.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateNaissance().toString()));
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTelephone())));
        colSexe.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSexe()));

        ajouterBoutonsActions();

        try {
            tableViewUtilisateurs.getItems().setAll(utilisateurDAO.getAllUsers()); // ou getAllUsers si tu veux tout
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
                        Parent root = loader.load();

                        // Récupération du contrôleur de la vue
                        ModifierUserController controller = loader.getController();
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


}
