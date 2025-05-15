package tests.Patient;

import entities.Consultation;
import entities.TypeConsultation;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceConsultation;
import services.ServiceUtilisateur;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

public class PatientConsultationController implements Initializable {

    @FXML private ListView<Consultation> listViewConsultations;
    @FXML private TableView<Consultation> tableConsultations;
    @FXML private TableColumn<Consultation, Integer> colId;
    @FXML private TableColumn<Consultation, String> colType;
    @FXML private TableColumn<Consultation, String> colStatus;
    @FXML private TableColumn<Consultation, String> colDate;
    @FXML private TableColumn<Consultation, String> colMedecin;

    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private DatePicker filterDate;
    @FXML private TextField searchField;

    @FXML private Button btnSchedule;
    @FXML private Button btnCancel;
    @FXML private Button btnView;

    private ObservableList<Consultation> consultationsList = FXCollections.observableArrayList();
    private List<Consultation> allConsultations = new ArrayList<>();
    private ServiceConsultation serviceConsultation;
    private Utilisateur currentUser;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceConsultation = new ServiceConsultation();
            ServiceUtilisateur su = new ServiceUtilisateur();
            // TODO FIX IN INTEGRATION
            try {
                this.currentUser = su.getById(1);
                loadConsultations();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer les informations utilisateur",
                        "Vérifiez que le serveur MySQL est en cours d'exécution et que l'utilisateur avec ID=1 existe.");
                throw e; // Propager l'erreur pour éviter l'initialisation incomplète
            }

            // Configure ListView with custom cell factory
            listViewConsultations.setCellFactory(param -> new ConsultationListCell());

            // Initialize filter ComboBoxes
            initializeFilters();

            // Apply filters when values change
            filterStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            filterType.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            filterDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

            // Disable buttons initially
            btnCancel.setDisable(true);
            btnView.setDisable(true);

            // Enable buttons when an item is selected
            listViewConsultations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                btnView.setDisable(!hasSelection);

                // Only enable cancel for pending consultations
                if (hasSelection && "pending".equals(newSelection.getStatus())) {
                    btnCancel.setDisable(false);
                } else {
                    btnCancel.setDisable(true);
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données", e.getMessage());
        }
    }

    public void navigateBack(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontPatient.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Custom ListCell implementation for Consultation items
    private class ConsultationListCell extends ListCell<Consultation> {
        @Override
        protected void updateItem(Consultation consultation, boolean empty) {
            super.updateItem(consultation, empty);

            if (empty || consultation == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Create container for list item
                VBox container = new VBox(5);
                container.setPadding(new Insets(10));
                container.getStyleClass().add("consultation-item");

                // Main info
                Label titleLabel = new Label("Consultation #" + consultation.getId());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                // Doctor info
                HBox doctorBox = new HBox(5);
                Label doctorLabel = new Label("Médecin: ");
                doctorLabel.setStyle("-fx-font-weight: bold;");

                Label doctorNameLabel = new Label();
                Utilisateur medecin = consultation.getMedecin();
                if (medecin != null) {
                    doctorNameLabel.setText(medecin.getNom() + " " + medecin.getPrenom());
                } else {
                    doctorNameLabel.setText("Non assigné");
                }
                doctorBox.getChildren().addAll(doctorLabel, doctorNameLabel);

                // Date and type
                HBox detailsBox = new HBox(20);

                VBox dateBox = new VBox(2);
                Label dateHeaderLabel = new Label("Date:");
                dateHeaderLabel.setStyle("-fx-font-weight: bold;");
                Label dateValueLabel = new Label(consultation.getDateC().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                dateBox.getChildren().addAll(dateHeaderLabel, dateValueLabel);

                VBox typeBox = new VBox(2);
                Label typeHeaderLabel = new Label("Type:");
                typeHeaderLabel.setStyle("-fx-font-weight: bold;");
                Label typeValueLabel = new Label(consultation.getType().getDisplayName());
                typeBox.getChildren().addAll(typeHeaderLabel, typeValueLabel);

                VBox statusBox = new VBox(2);
                Label statusHeaderLabel = new Label("Statut:");
                statusHeaderLabel.setStyle("-fx-font-weight: bold;");
                Label statusValueLabel = new Label(consultation.getStatus());
                statusValueLabel.setStyle("-fx-font-style: italic;");
                statusBox.getChildren().addAll(statusHeaderLabel, statusValueLabel);

                detailsBox.getChildren().addAll(dateBox, typeBox, statusBox);

                // Add all elements to container
                container.getChildren().addAll(titleLabel, doctorBox, detailsBox);

                // Set container as cell graphic
                setGraphic(container);
            }
        }
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        try {
            loadConsultations();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des consultations", e.getMessage());
        }
    }

    private void loadConsultations() throws SQLException {
        if (currentUser != null) {
            String status = filterStatus.getValue();
            String type = filterType.getValue();
            LocalDateTime date = filterDate.getValue() != null ?
                    filterDate.getValue().atStartOfDay() : null;
            String search = searchField.getText();

            // Load consultations for the current patient with filters
            List<Consultation> consultations = serviceConsultation.filterConsultations(
                    status, type, date, search, String.valueOf(currentUser.getId()),
                    currentPage, PAGE_SIZE);

            // Update the ListView
            ObservableList<Consultation> observableConsultations = FXCollections.observableArrayList(consultations);
            listViewConsultations.setItems(observableConsultations);

            // Store original list for filtering
            this.allConsultations = new ArrayList<>(consultations);

            // Update the TableView if it's being used
            if (tableConsultations != null) {
                consultationsList.clear();
                consultationsList.addAll(consultations);
                tableConsultations.setItems(consultationsList);
            }
        }
    }

    /*private void applyFilters() {
        // Start with all consultations
        List<Consultation> filteredList = new ArrayList<>(allConsultations);

        // Apply status filter
        if (filterStatus.getValue() != null && !filterStatus.getValue().isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(c -> c.getStatus().equals(filterStatus.getValue()))
                    .collect(Collectors.toList());
        }

        // Apply type filter
        if (filterType.getValue() != null && !filterType.getValue().isEmpty()) {
            final String typeValue = filterType.getValue();
            filteredList = filteredList.stream()
                    .filter(c -> c.getType().getDisplayName().equals(typeValue))
                    .collect(Collectors.toList());
        }

        // Apply date filter
        if (filterDate.getValue() != null) {
            LocalDate selectedDate = filterDate.getValue();
            filteredList = filteredList.stream()
                    .filter(c -> c.getDateC().toLocalDate().equals(selectedDate))
                    .collect(Collectors.toList());
        }

        // Apply search filter
        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            String searchText = searchField.getText().toLowerCase();
            filteredList = filteredList.stream()
                    .filter(c -> {
                        // Search in doctor name
                        if (c.getMedecin() != null) {
                            String doctorName = c.getMedecin().getNom() + " " + c.getMedecin().getPrenom();
                            if (doctorName.toLowerCase().contains(searchText)) {
                                return true;
                            }
                        }
                        // Search in type
                        return c.getType().getDisplayName().toLowerCase().contains(searchText);
                    })
                    .collect(Collectors.toList());
        }

        // Update ListView with filtered results
        listViewConsultations.setItems(FXCollections.observableArrayList(filteredList));
    }*/
    private void applyFilters() {
        // Start with all consultations
        List<Consultation> filteredList = new ArrayList<>(allConsultations);

        // Apply status filter
        if (filterStatus.getValue() != null && !filterStatus.getValue().isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(c -> c.getStatus().equals(filterStatus.getValue()))
                    .collect(Collectors.toList());
        }

        // Apply type filter - FIXED
        if (filterType.getValue() != null && !filterType.getValue().isEmpty()) {
            final String typeValue = filterType.getValue().toUpperCase();
            filteredList = filteredList.stream()
                    .filter(c -> {
                        // Handle the special case for VIRTUELLE/EN LIGNE
                        if (typeValue.equals("VIRTUELLE") || typeValue.equals("EN LIGNE")) {
                            return c.getType() == TypeConsultation.VIRTUELLE ||
                                    c.getType().toString().equals("EN LIGNE");
                        } else {
                            return c.getType().toString().equalsIgnoreCase(typeValue);
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Apply date filter
        if (filterDate.getValue() != null) {
            LocalDate selectedDate = filterDate.getValue();
            filteredList = filteredList.stream()
                    .filter(c -> c.getDateC().toLocalDate().equals(selectedDate))
                    .collect(Collectors.toList());
        }

        // Apply search filter
        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            String searchText = searchField.getText().toLowerCase();
            filteredList = filteredList.stream()
                    .filter(c -> {
                        // Search in doctor name
                        if (c.getMedecin() != null) {
                            String doctorName = c.getMedecin().getNom() + " " + c.getMedecin().getPrenom();
                            if (doctorName.toLowerCase().contains(searchText)) {
                                return true;
                            }
                        }
                        // Search in type
                        return c.getType().getDisplayName().toLowerCase().contains(searchText);
                    })
                    .collect(Collectors.toList());
        }

        // Update ListView with filtered results
        listViewConsultations.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void initializeFilters() {
        try {
            // Add an empty option
            filterStatus.getItems().add("");
            filterStatus.getItems().addAll(serviceConsultation.getDistinctStatuses());

            filterType.getItems().add("");
            filterType.getItems().addAll(serviceConsultation.getDistinctTypes());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation des filtres", e.getMessage());
        }
    }

    @FXML
    private void handleScheduleAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_form.fxml"));
            Parent root = loader.load();

            PatientConsultationFormController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setMode("create");

            // On successful save, reload the consultations list
            controller.setOnSaveCallback(() -> {
                try {
                    loadConsultations();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du rechargement des consultations", e.getMessage());
                }
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Planifier Consultation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        Consultation selectedConsultation = listViewConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null && "pending".equals(selectedConsultation.getStatus())) {
            // Ask for confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir annuler cette consultation ?",
                    ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait();

            if (confirmation.getResult() == ButtonType.YES) {
                try {
                    // Update the status to cancelled
                    selectedConsultation.setStatus(Consultation.STATUS_CANCELLED);
                    serviceConsultation.modifier(selectedConsultation);

                    // Reload consultations
                    loadConsultations();

                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Consultation annulée", "La consultation a été annulée avec succès.");

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de l'annulation", e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleViewAction(ActionEvent event) {
        Consultation selectedConsultation = listViewConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_details.fxml"));
                Parent root = loader.load();

                PatientConsultationDetailsController controller = loader.getController();
                controller.setConsultation(selectedConsultation);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Détails de la Consultation");
                stage.setScene(new Scene(root));
                stage.showAndWait();

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture des détails", e.getMessage());
            }
        }
    }

    @FXML
    private void handleNextPage() {
        try {
            String status = filterStatus.getValue();
            String type = filterType.getValue();
            LocalDateTime date = filterDate.getValue() != null ?
                    filterDate.getValue().atStartOfDay() : null;
            String search = searchField.getText();

            int totalCount = serviceConsultation.countConsultations(
                    status, type, date, search, String.valueOf(currentUser.getId()));

            int maxPage = (int) Math.ceil((double) totalCount / PAGE_SIZE);

            if (currentPage < maxPage) {
                currentPage++;
                loadConsultations();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de pagination", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            try {
                loadConsultations();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de pagination", e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}