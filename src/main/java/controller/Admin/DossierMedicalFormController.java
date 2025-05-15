package controller.Admin;

import entities.DossierMedical;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import services.ServiceDossierMedical;
import services.ServiceUtilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DossierMedicalFormController {

    @FXML private ChoiceBox<String> utilisateurChoiceBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField fichierField;
    @FXML private ChoiceBox<String> uniteChoiceBox; // Remplacé TextField par ChoiceBox
    @FXML private TextField mesureField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage stage;
    private DossierMedical dossier;
    private controller.Admin.DossierMedicalListAdminController listController;
    private ServiceDossierMedical serviceDossier;
    private ServiceUtilisateur serviceUtilisateur;
    private Map<String, Integer> emailToUserIdMap;

    public DossierMedicalFormController() {
        try {
            serviceDossier = new ServiceDossierMedical();
            serviceUtilisateur = new ServiceUtilisateur();
            emailToUserIdMap = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        try {
            List<Utilisateur> users = serviceUtilisateur.afficher();
            for (Utilisateur user : users) {
                utilisateurChoiceBox.getItems().add(user.getEmail());
                emailToUserIdMap.put(user.getEmail(), user.getId());
            }

            if (dossier == null) {
                utilisateurChoiceBox.getItems().clear();
                emailToUserIdMap.clear();
                List<Utilisateur> usersWithoutDossier = serviceUtilisateur.getUsersWithoutDossierMedical();
                for (Utilisateur user : usersWithoutDossier) {
                    utilisateurChoiceBox.getItems().add(user.getEmail());
                    emailToUserIdMap.put(user.getEmail(), user.getId());
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
        // Initialiser la ChoiceBox des unités
        uniteChoiceBox.setItems(FXCollections.observableArrayList(
                "mg/dL", "mmol/L", "g/L", "U/L", "mL"
        ));
    }

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;
        if (dossier != null) {
            try {
                Utilisateur user = serviceUtilisateur.getById(dossier.getUtilisateurId());
                if (user != null) {
                    if (!emailToUserIdMap.containsKey(user.getEmail())) {
                        utilisateurChoiceBox.getItems().add(user.getEmail());
                        emailToUserIdMap.put(user.getEmail(), user.getId());
                    }
                    utilisateurChoiceBox.setValue(user.getEmail());
                }
                datePicker.setValue(dossier.getDate());
                fichierField.setText(dossier.getFichier());
                uniteChoiceBox.setValue(dossier.getUnite());
                mesureField.setText(String.valueOf(dossier.getMesure()));
                saveButton.setText("Mettre à jour");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'utilisateur : " + e.getMessage());
            }
        }
    }

    public void setListController(controller.Admin.DossierMedicalListAdminController listController) {
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

        File selectedFile = fileChooser.showOpenDialog(fichierField.getScene().getWindow());
        if (selectedFile != null) {
            fichierField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void saveDossier() {
        try {
            String selectedEmail = utilisateurChoiceBox.getValue();
            if (selectedEmail == null || selectedEmail.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un utilisateur.");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une date.");
                return;
            }

            String fichier = fichierField.getText().trim();
            if (fichier.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un fichier.");
                return;
            }

            String unite = uniteChoiceBox.getValue(); // Utiliser la ChoiceBox
            if (unite.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer une unité.");
                return;
            }

            String mesureText = mesureField.getText().trim();
            if (mesureText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer une mesure.");
                return;
            }

            double mesure;
            try {
                mesure = Double.parseDouble(mesureText);
                if (mesure < 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "La mesure doit être un nombre positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer une valeur numérique valide pour la mesure.");
                return;
            }

            int utilisateurId = emailToUserIdMap.get(selectedEmail);

            if (dossier == null) {
                dossier = new DossierMedical();
                dossier.setUtilisateurId(utilisateurId);
                dossier.setDate(date);
                dossier.setFichier(fichier);
                dossier.setUnite(unite);
                dossier.setMesure(mesure);
                serviceDossier.ajouter(dossier);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dossier médical ajouté avec succès !");
            } else {
                dossier.setUtilisateurId(utilisateurId);
                dossier.setDate(date);
                dossier.setFichier(fichier);
                dossier.setUnite(unite);
                dossier.setMesure(mesure);
                serviceDossier.modifier(dossier);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dossier médical modifié avec succès !");
                // Sauvegarder l'unité et la mesure dans le fichier MesurePatient.txt
                saveToMesurePatientFile(selectedEmail, unite, String.valueOf(mesure));
            }

            if (listController != null) {
                listController.refreshDossiers();
            }

            stage = (Stage) fichierField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        stage = (Stage) fichierField.getScene().getWindow();
        stage.close();
    }

    private void saveToMesurePatientFile(String email, String unite, String mesure) {
        try {
            String fileName = "Fichier Patient de " + email + ".txt";
            fileName = fileName.replaceAll("[^a-zA-Z0-9.@ ]", "_");

            File directory = new File("src/main/resources/fichier");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, fileName);

            // Utiliser la date et l'heure actuelles
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedDate = currentDateTime.format(dateFormatter);
            String formattedTime = currentDateTime.format(timeFormatter);

            // Créer la ligne à écrire avec l'heure
            String line = String.format("Date: %s, Heure: %s, Unité: %s, Mesure: %s%n",
                    formattedDate, formattedTime, unite, mesure);

            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'écriture dans le fichier patient : " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (getClass().getResource("/MedicalStyle.css") != null) {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/MedicalStyle.css").toExternalForm());
        }
        alert.showAndWait();
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}