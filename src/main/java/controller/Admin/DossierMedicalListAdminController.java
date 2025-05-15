package controller.Admin;

import entities.DossierMedical;
import services.ServiceDossierMedical;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DossierMedicalListAdminController {
    @FXML private TableView<DossierMedical> dossierTable;
    @FXML private TableColumn<DossierMedical, Integer> idColumn;
    @FXML private TableColumn<DossierMedical, Integer> utilisateurIdColumn;
    @FXML private TableColumn<DossierMedical, String> dateColumn;
    @FXML private TableColumn<DossierMedical, String> fichierColumn;
    @FXML private TableColumn<DossierMedical, String> uniteColumn;
    @FXML private TableColumn<DossierMedical, String> mesureColumn;
    @FXML private TableColumn<DossierMedical, Void> detailsColumn;

    private ObservableList<DossierMedical> dossierList;
    private ServiceDossierMedical serviceDossierMedical = new ServiceDossierMedical();

    public DossierMedicalListAdminController() throws SQLException {
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        utilisateurIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        fichierColumn.setCellValueFactory(new PropertyValueFactory<>("fichier"));
        uniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));
        mesureColumn.setCellValueFactory(new PropertyValueFactory<>("mesure"));

        detailsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Détails");

            {
                detailsButton.setOnAction(event -> {
                    DossierMedical dossier = getTableView().getItems().get(getIndex());
                    showDetails(dossier);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });

        dossierList = FXCollections.observableArrayList();
        dossierTable.setItems(dossierList);
        chargerDossiers();
    }

    public void chargerDossiers() {
        try {
            ServiceDossierMedical service = new ServiceDossierMedical();
            List<DossierMedical> dossiers = service.afficher();
            dossierList.clear();
            dossierList.addAll(dossiers);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des dossiers : " + e.getMessage());
        }
    }

    private void showDetails(DossierMedical dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/DossierMedicalDetailsAdmin.fxml"));
            Parent root = loader.load();

            DossierMedicalDetailsAdminController controller = loader.getController();
            controller.setDossier(dossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(true);
            stage.showAndWait();

            chargerDossiers();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du dossier : " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void refreshDossiers() {
        try {
            List<DossierMedical> dossiers = serviceDossierMedical.afficher();
            dossierList.setAll(dossiers); // Assuming dossierList is an ObservableList bound to a TableView or GridPane
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du rafraîchissement des dossiers : " + e.getMessage());
        }
    }
    @FXML
    private void addDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormDossierMedicalAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Dossier Médical");
            stage.setScene(new Scene(root, 400, 500));
            stage.setResizable(false);
            stage.showAndWait();

            chargerDossiers();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) dossierTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}