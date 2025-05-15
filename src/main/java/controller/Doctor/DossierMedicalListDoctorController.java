package controller.Doctor;

import entities.DossierMedical;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceDossierMedical;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class DossierMedicalListDoctorController implements Initializable {

    @FXML private HBox filterHBox;
    @FXML private ChoiceBox<String> uniteFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TextField searchField;
    @FXML private Button resetButton;
    @FXML private GridPane dossierGrid;
    @FXML private HBox paginationHBox;
    @FXML private Button prevPageButton;
    @FXML private Label pageLabel;
    @FXML private Button nextPageButton;
    @FXML private HBox header; // Ajouté pour accéder à l'élément .header
    @FXML private Label headerTitle; // Ajouté pour accéder à l'élément .header-title

    private ObservableList<DossierMedical> dossierListItems = FXCollections.observableArrayList();
    private final ServiceDossierMedical serviceDossierMedical;
    private String doctorEmail;
    private int currentPage = 1;
    private final int pageSize = 3; // Nombre de dossiers par page
    private int totalDossiers = 0;
    private DossierMedical selectedDossier; // Pour suivre le dossier sélectionné (pour d'éventuelles actions)

    public DossierMedicalListDoctorController() throws SQLException {
        this.serviceDossierMedical = new ServiceDossierMedical();
    }

    @FXML
    private void showPredictionStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/PredictionStats.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Statistiques des Prédictions");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de l'ouverture des statistiques : " + e.getMessage());
            alert.showAndWait();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Remplir le ChoiceBox avec les unités distinctes
        try {
            List<String> unites = serviceDossierMedical.getDistinctUnites();
            uniteFilter.setItems(FXCollections.observableArrayList(unites));
            uniteFilter.getItems().add(0, ""); // Ajouter une option vide pour "aucun filtre"
            uniteFilter.setValue(""); // Sélectionner l'option vide par défaut
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les unités distinctes : " + e.getMessage());
        }

        // Ajouter un écouteur sur le TextField pour une recherche dynamique
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            loadDossiers();
        });

        // Ajouter des écouteurs sur les autres filtres pour déclencher loadDossiers
        uniteFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            loadDossiers();
        });

        dateFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            loadDossiers();
        });

        // Ajouter la classe "visible" pour déclencher les animations
        if (header != null) {
            header.getStyleClass().add("visible");
        }
        if (headerTitle != null) {
            headerTitle.getStyleClass().add("visible");
        }
        if (filterHBox != null) {
            filterHBox.getStyleClass().add("visible");
        }
        if (paginationHBox != null) {
            paginationHBox.getStyleClass().add("visible");
        }

        // Charger les dossiers initiaux
        loadDossiers();
    }

    private void loadDossiers() {
        try {
            String unite = uniteFilter.getValue();
            LocalDate date = dateFilter.getValue();
            String searchText = searchField.getText();

            totalDossiers = serviceDossierMedical.countDossiers(unite, date, searchText);
            int totalPages = (int) Math.ceil((double) totalDossiers / pageSize);

            if (currentPage < 1) {
                currentPage = 1;
            } else if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }

            dossierListItems.setAll(serviceDossierMedical.filterDossiers(unite, date, searchText, currentPage, pageSize));
            populateGrid(dossierListItems);

            pageLabel.setText("Page " + currentPage + " / " + (totalPages == 0 ? 1 : totalPages));
            prevPageButton.setDisable(currentPage == 1);
            nextPageButton.setDisable(currentPage == totalPages || totalPages == 0);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des dossiers : " + e.getMessage());
        }
    }

    private void populateGrid(ObservableList<DossierMedical> dossiers) {
        // Effacer le contenu existant du GridPane
        dossierGrid.getChildren().clear();
        selectedDossier = null; // Réinitialiser la sélection

        // Ajouter les cartes au GridPane
        int row = 0;
        int col = 0;
        for (DossierMedical dossier : dossiers) {
            // Créer une carte pour chaque dossier
            VBox card = new VBox(5);
            card.getStyleClass().add("dossier-card");

            Label titleLabel = new Label("Dossier #" + dossier.getId());
            titleLabel.getStyleClass().add("title");

            Label utilisateurIdLabel = new Label("Utilisateur ID: " + dossier.getUtilisateurId());
            utilisateurIdLabel.getStyleClass().add("label");

            Label dateLabel = new Label("Date: " + dossier.getDate());
            dateLabel.getStyleClass().add("label");

            Label fichierLabel = new Label("Fichier: " + dossier.getFichier());
            fichierLabel.getStyleClass().add("label");

            Label uniteLabel = new Label("Unité: " + (dossier.getUnite() != null ? dossier.getUnite() : "N/A"));
            uniteLabel.getStyleClass().add("label");

            Label mesureLabel = new Label("Mesure: " + dossier.getMesure());
            mesureLabel.getStyleClass().add("label");

            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().add("details-button");
            detailsButton.setOnAction(event -> showDetails(dossier));

            card.getChildren().addAll(titleLabel, utilisateurIdLabel, dateLabel, fichierLabel, uniteLabel, mesureLabel, detailsButton);

            // Ajouter la classe "visible" pour déclencher l'animation sur la carte
            card.getStyleClass().add("visible");

            // Ajouter un gestionnaire de clic pour sélectionner la carte
            card.setOnMouseClicked(event -> {
                selectedDossier = dossier;
                // Ajouter une indication visuelle de sélection
                dossierGrid.getChildren().forEach(node -> node.setStyle("-fx-border-color: transparent;"));
                card.setStyle("-fx-border-color: #6C5CE7; -fx-border-width: 2; -fx-border-radius: 8;");
                // Si double-clic, ouvrir les détails
                if (event.getClickCount() == 2) {
                    showDetails(dossier);
                }
            });

            // Ajouter la carte au GridPane
            dossierGrid.add(card, col, row);

            // Passer à la colonne suivante
            col++;
            if (col == 3) { // 3 cartes par ligne
                col = 0;
                row++;
            }
        }
    }

    private void showDetails(DossierMedical dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/DossierMedicalDetailsDoctor.fxml"));
            Parent root = loader.load();

            DossierMedicalDetailsDoctorController controller = loader.getController();
            controller.setDossier(dossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical");
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);
            stage.showAndWait();

            loadDossiers();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les détails du dossier : " + e.getMessage());
        }
    }

    @FXML
    private void resetFilters() {
        uniteFilter.setValue("");
        dateFilter.setValue(null);
        searchField.setText("");
        currentPage = 1;
        loadDossiers();
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadDossiers();
        }
    }

    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) totalDossiers / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            loadDossiers();
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) dossierGrid.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }
}