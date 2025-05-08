package controller;

import entities.Analyse;
import entities.DossierMedical;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ServiceAnalyse;
import services.ServiceDossierMedical;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class AnalyseFormController {

    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private DatePicker dateAnalyseField;
    @FXML private TextField donneesAnalyseField;
    @FXML private TextField diagnosticField;
    @FXML private Label typeErrorLabel; // Error label for type
    @FXML private Label dateErrorLabel; // Error label for date
    @FXML private Label donneesErrorLabel; // Error label for donnees
    @FXML private Label diagnosticErrorLabel; // Error label for diagnostic
    @FXML private Button saveAnalyseButton;

    private Analyse analyse;
    private AnalyseListController listController;
    private ServiceAnalyse serviceAnalyse;
    private ServiceDossierMedical serviceDossierMedical;
    private Integer dossierId;

    public AnalyseFormController() throws SQLException {
        this.serviceAnalyse = new ServiceAnalyse();
        this.serviceDossierMedical = new ServiceDossierMedical();
    }

    @FXML
    public void initialize() {
        // Initialize the ChoiceBox with analysis types
        typeChoiceBox.setItems(FXCollections.observableArrayList(
                "Analyse de sang",
                "Analyse d'urine",
                "Radiographie",
                "Échographie",
                "Test de glycémie"
        ));
        typeChoiceBox.setValue("Analyse de sang"); // Default value

        // Clear all error labels
        clearErrorLabels();

        // Automatically retrieve dossierId from the static user
        try {
            int utilisateurId = 1;
            if (utilisateurId == -1) {
                typeErrorLabel.setText("Aucun utilisateur connecté défini.");
                return;
            }
            DossierMedical dossier = serviceDossierMedical.getByUtilisateurId(utilisateurId);
            if (dossier != null) {
                dossierId = dossier.getId();
            } else {
                typeErrorLabel.setText("Aucun dossier médical trouvé pour l'utilisateur connecté.");
            }
        } catch (SQLException e) {
            typeErrorLabel.setText("Erreur lors de la récupération du dossier médical : " + e.getMessage());
        }
    }

    public void setAnalyse(Analyse analyse) {
        this.analyse = analyse;
        if (analyse != null) {
            typeChoiceBox.setValue(analyse.getType());
            dateAnalyseField.setValue(analyse.getDateAnalyse());
            donneesAnalyseField.setText(analyse.getDonneesAnalyse());
            diagnosticField.setText(analyse.getDiagnostic());
            dossierId = analyse.getDossierId();
        }
    }

    public void setListController(AnalyseListController listController) {
        this.listController = listController;
    }

    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),

                new FileChooser.ExtensionFilter("Documents", "*.txt", "*.doc", "*.docx")
        );

        File selectedFile = fileChooser.showOpenDialog(donneesAnalyseField.getScene().getWindow());
        if (selectedFile != null) {
            donneesAnalyseField.setText(selectedFile.getAbsolutePath());
            donneesErrorLabel.setText(""); // Clear error when file்
            if (listController != null) {
                listController.loadAnalyses();
            }
        }
    }

    @FXML
    private void saveAnalyse() {
        try {
            // Clear previous error messages
            clearErrorLabels();

            // Validate fields
            String type = typeChoiceBox.getValue();
            LocalDate dateAnalyse = dateAnalyseField.getValue();
            String donnees = donneesAnalyseField.getText().trim();
            String diagnostic = diagnosticField.getText().trim();

            boolean hasError = false;

            if (type == null) {
                typeErrorLabel.setText("Veuillez sélectionner un type d'analyse.");
                hasError = true;
            }
            if (dateAnalyse == null) {
                dateErrorLabel.setText("Veuillez sélectionner une date.");
                hasError = true;
            }
            if (donnees.isEmpty()) {
                donneesErrorLabel.setText("Veuillez sélectionner un fichier.");
                hasError = true;
            }
            if (diagnostic.isEmpty()) {
                diagnosticErrorLabel.setText("Veuillez entrer un diagnostic.");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            // Check if dossierId is set
            if (dossierId == null) {
                typeErrorLabel.setText("Impossible de déterminer le dossier médical associé.");
                return;
            }

            // Create or update analysis
            if (analyse == null) {
                analyse = new Analyse();
            }
            analyse.setDossierId(dossierId);
            analyse.setType(type);
            analyse.setDateAnalyse(dateAnalyse);
            analyse.setDonneesAnalyse(donnees);
            analyse.setDiagnostic(diagnostic);

            if (analyse.getId() == 0) {
                serviceAnalyse.ajouter(analyse);
            } else {
                serviceAnalyse.modifier(analyse);
            }

            // Close the window and refresh the list
            Stage stage = (Stage) typeChoiceBox.getScene().getWindow();
            stage.close();

            if (listController != null) {
                listController.loadAnalyses();
            }
        } catch (SQLException e) {
            typeErrorLabel.setText("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    private void clearErrorLabels() {
        typeErrorLabel.setText("");
        dateErrorLabel.setText("");
        donneesErrorLabel.setText("");
        diagnosticErrorLabel.setText("");
    }
}