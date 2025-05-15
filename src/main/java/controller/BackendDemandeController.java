package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Utilisateur;
import service.UtilisateurDAO;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BackendDemandeController {
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

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;
    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colPrenom;
    @FXML
    private TableColumn<Utilisateur, String> colSpecialite;
    @FXML
    private TableColumn<Utilisateur, Void> colAction;
    @FXML
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 10;
    private List<Utilisateur> allUtilisateurs = new ArrayList<>();


    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    @FXML
    private Label labelNomUtilisateur;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText("Bienvenue, " + currentUser.getNom());
        }
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));

        ajouterColonneAction();
        chargerUtilisateurs();
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

    private void ajouterColonneAction() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final HBox hbox = new HBox(10); // espacement entre les boutons
            private final Button btnAccepter = new Button("✅");
            private final Button btnRefuser = new Button("❌");
            private final Button btnVoirDiplome = new Button("Voir diplôme");
            private final HBox box = new HBox(5, btnAccepter, btnRefuser, btnVoirDiplome);

            {
                btnAccepter.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnRefuser.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnAccepter.setOnAction(event -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    utilisateurDAO.accepterUtilisateur(u.getId());
                    tableUtilisateurs.getItems().remove(u); // Optionnel : pour retirer de la liste immédiatement
                });


                btnRefuser.setOnAction(event -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    // Confirmation avant suppression (optionnel mais recommandé)
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText(null);
                    alert.setContentText("Voulez-vous vraiment refuser (et supprimer) cet utilisateur ?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            utilisateurDAO.supprimerUtilisateur(u.getId());
                            tableUtilisateurs.getItems().remove(u); // mise à jour locale de la table
                            // OU : chargerUtilisateurs(); // si tu préfères tout recharger depuis la BDD
                        }
                    });
                });


                btnVoirDiplome.setOnAction(event -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    afficherDiplomePopup(u.getDiplome());
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }


    private void chargerUtilisateurs() {
        try {
            allUtilisateurs = utilisateurDAO.getUtilisateursAvecStatus(0);
            int pageCount = (int) Math.ceil((double) allUtilisateurs.size() / ROWS_PER_PAGE);
            pagination.setPageCount(pageCount);
            pagination.setCurrentPageIndex(0);
            pagination.setPageFactory(this::createPage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void afficherDiplomePopup(String cheminImage) {
        if (cheminImage == null || cheminImage.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Diplôme");
            alert.setHeaderText(null);
            alert.setContentText("Aucun diplôme disponible pour cet utilisateur.");
            alert.showAndWait();
            return;
        }

        ImageView imageView = new ImageView(new javafx.scene.image.Image("file:" + cheminImage));
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        VBox vbox = new VBox(imageView);
        vbox.setPadding(new javafx.geometry.Insets(10));

        Scene scene = new Scene(vbox);
        Stage popupStage = new Stage();
        popupStage.setTitle("Diplôme de l'utilisateur");
        popupStage.setScene(scene);
        popupStage.show();
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
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allUtilisateurs.size());
        ObservableList<Utilisateur> pageData = FXCollections.observableArrayList(
                allUtilisateurs.subList(fromIndex, toIndex)
        );
        tableUtilisateurs.setItems(pageData);
        return tableUtilisateurs;
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



    /// ////////////////////
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
