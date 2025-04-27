package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;

public class RegistrationFormController {

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtTelephone, txtAdresse;
    @FXML private PasswordField txtPassword, txtConfirmPassword;
    @FXML private CheckBox checkHomme, checkFemme;
    @FXML private DatePicker txtDateNaissance;
    @FXML private RadioButton RadioMedecin, RadioPatient;
    @FXML private TextField txtTaille, txtPoids, txtSpecialite;
    @FXML private Button btnUploadImage, btnUploaddiplome;
    @FXML private ImageView imagePreview, diplomePreview;

    // Labels d'erreur
    @FXML private Label lblNomError, lblPrenomError, lblEmailError, lblPasswordError, lblPasswordMismatch, lblTelephoneError;
    @FXML private Label lblAdresseError, lblSexeError, lblDateNaissanceError, lblRoleError;

    private File selectedImageFile;
    private File selectedDiplomeFile;

    @FXML
    private void btnRegister() {
        // Réinitialiser les erreurs
        resetErrorLabels();

        boolean isValid = true;

        // Nom
        if (txtNom.getText().isEmpty()) {
            lblNomError.setText("Nom requis");
            lblNomError.setVisible(true);
            isValid = false;
        }

        // Prénom
        if (txtPrenom.getText().isEmpty()) {
            lblPrenomError.setText("Prénom requis");
            lblPrenomError.setVisible(true);
            isValid = false;
        }

        // Email
        if (txtEmail.getText().isEmpty() || !txtEmail.getText().contains("@")) {
            lblEmailError.setText("Email invalide");
            lblEmailError.setVisible(true);
            isValid = false;
        }

        // Password
        if (txtPassword.getText().isEmpty()) {
            lblPasswordError.setText("Mot de passe requis");
            lblPasswordError.setVisible(true);
            isValid = false;
        } else if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
            lblPasswordMismatch.setVisible(true);
            isValid = false;
        }

        // Téléphone
        if (txtTelephone.getText().isEmpty() || !txtTelephone.getText().matches("\\d+")) {
            lblTelephoneError.setText("Téléphone invalide");
            lblTelephoneError.setVisible(true);
            isValid = false;
        }

        // Adresse
        if (txtAdresse.getText().isEmpty()) {
            lblAdresseError.setText("Adresse requise");
            lblAdresseError.setVisible(true);
            isValid = false;
        }

        // Sexe
        if (!checkHomme.isSelected() && !checkFemme.isSelected()) {
            lblSexeError.setText("Veuillez sélectionner un sexe");
            lblSexeError.setVisible(true);
            isValid = false;
        }

        // Date de naissance
        LocalDate dateNaissance = txtDateNaissance.getValue();
        if (dateNaissance == null) {
            lblDateNaissanceError.setText("Date de naissance requise");
            lblDateNaissanceError.setVisible(true);
            isValid = false;
        }

        // Rôle
        if (!RadioMedecin.isSelected() && !RadioPatient.isSelected()) {
            lblRoleError.setText("Veuillez sélectionner un rôle");
            lblRoleError.setVisible(true);
            isValid = false;
        }

        if (isValid) {
            // ✅ Si tout est bon
            System.out.println("Inscription réussie !");
            // Tu peux ici appeler ta fonction pour sauvegarder l'utilisateur dans la BDD.
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    private void handleDiplomeUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un diplôme");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedDiplomeFile = fileChooser.showOpenDialog(null);
        if (selectedDiplomeFile != null) {
            diplomePreview.setImage(new Image(selectedDiplomeFile.toURI().toString()));
        }
    }

    private void resetErrorLabels() {
        lblNomError.setVisible(false);
        lblPrenomError.setVisible(false);
        lblEmailError.setVisible(false);
        lblPasswordError.setVisible(false);
        lblPasswordMismatch.setVisible(false);
        lblTelephoneError.setVisible(false);
        lblAdresseError.setVisible(false);
        lblSexeError.setVisible(false);
        lblDateNaissanceError.setVisible(false);
        lblRoleError.setVisible(false);
    }
}
