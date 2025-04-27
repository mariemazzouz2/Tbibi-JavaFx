package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Utilisateur;
import service.UtilisateurDAO;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;

public class RegistrationFormController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblPasswordMismatch;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private DatePicker txtDateNaissance;
    @FXML private CheckBox checkHomme;
    @FXML private CheckBox checkFemme;
    @FXML private TextField txtTaille;
    @FXML private TextField txtPoids;
    @FXML private TextField txtSpecialite;
    @FXML private Button btnUploaddiplome;
    @FXML private Button btnUploadImage;
    @FXML private ImageView diplomePreview;
    @FXML private RadioButton RadioMedecin;
    @FXML private RadioButton RadioPatient;

    private File selectedImageFile;
    private File selectedDiplomeFile;
    @FXML private Label lblNomError;
    @FXML private Label lblPrenomError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblTelephoneError;
    @FXML private Label lblAdresseError;
    @FXML private Label lblRoleError;
    @FXML private Label lblSexeError;
    @FXML private Label lblDateNaissanceError;





    @FXML
    public void initialize() {
        // Cacher tous les champs spécifiques au départ
        togglePatientFields(false);
        toggleMedecinFields(false);

        // Ajouter des listeners sur les radio boutons
        RadioPatient.setOnAction(event -> {
            togglePatientFields(true);
            toggleMedecinFields(false);
        });

        RadioMedecin.setOnAction(event -> {
            togglePatientFields(false);
            toggleMedecinFields(true);
        });
    }

    private void togglePatientFields(boolean visible) {
        txtTaille.setVisible(visible);
        txtPoids.setVisible(visible);
    }

    private void toggleMedecinFields(boolean visible) {
        txtSpecialite.setVisible(visible);
        btnUploaddiplome.setVisible(visible);
        diplomePreview.setVisible(visible);
    }
    @FXML
    private void handleDiplomeUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un diplôme");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );
        File selectedFile = fileChooser.showOpenDialog(btnUploaddiplome.getScene().getWindow());

        if (selectedFile != null) {
            selectedDiplomeFile = selectedFile; // 🔵 on stocke dans la variable
            System.out.println("Diplôme sélectionné : " + selectedDiplomeFile.getAbsolutePath());
        }
    }
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png")
        );
        File selectedFile = fileChooser.showOpenDialog(btnUploadImage.getScene().getWindow());

        if (selectedFile != null) {
            selectedImageFile = selectedFile; // ⬅️ stockage
            System.out.println("Image sélectionnée : " + selectedImageFile.getAbsolutePath());
        }
    }



    @FXML
    private void btnRegister() {
        try {
            hideErrorLabels(); // Cacher tous les anciens messages d'erreur
            boolean isValid = true;

            // Validation Nom
            if (txtNom.getText().isEmpty()) {
                lblNomError.setText("Le nom est obligatoire.");
                lblNomError.setVisible(true);
                isValid = false;
            } else if (!txtNom.getText().matches("[A-Z][a-zA-Z]*")) {
                lblNomError.setText("Le nom doit commencer par une majuscule.");
                lblNomError.setVisible(true);
                isValid = false;
            }

            // Validation Prénom
            if (txtPrenom.getText().isEmpty()) {
                lblPrenomError.setText("Le prénom est obligatoire.");
                lblPrenomError.setVisible(true);
                isValid = false;
            } else if (!txtPrenom.getText().matches("[A-Z][a-zA-Z]*")) {
                lblPrenomError.setText("Le prénom doit commencer par une majuscule.");
                lblPrenomError.setVisible(true);
                isValid = false;
            }

            // Validation Email
            if (txtEmail.getText().isEmpty()) {
                lblEmailError.setText("L'email est obligatoire.");
                lblEmailError.setVisible(true);
                isValid = false;
            } else if (!txtEmail.getText().matches("[\\w.-]+@[\\w.-]+\\.\\w+")) {
                lblEmailError.setText("Format d'email invalide.");
                lblEmailError.setVisible(true);
                isValid = false;
            }

            // Validation Mot de passe
            if (txtPassword.getText().isEmpty()) {
                lblPasswordError.setText("Le mot de passe est obligatoire.");
                lblPasswordError.setVisible(true);
                isValid = false;
            } else if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
                lblPasswordMismatch.setText("Les mots de passe ne correspondent pas.");
                lblPasswordMismatch.setVisible(true);
                isValid = false;
            }

            // Validation Téléphone
            if (txtTelephone.getText().isEmpty()) {
                lblTelephoneError.setText("Le téléphone est obligatoire.");
                lblTelephoneError.setVisible(true);
                isValid = false;
            } else if (!txtTelephone.getText().matches("\\d{8}")) { // exemple : téléphone à 8 chiffres
                lblTelephoneError.setText("Numéro invalide.");
                lblTelephoneError.setVisible(true);
                isValid = false;
            }

            // Validation Adresse
            if (txtAdresse.getText().isEmpty()) {
                lblAdresseError.setText("L'adresse est obligatoire.");
                lblAdresseError.setVisible(true);
                isValid = false;
            }

            // Validation Date de naissance
            if (txtDateNaissance.getValue() == null) {
                lblDateNaissanceError.setText("La date de naissance est obligatoire.");
                lblDateNaissanceError.setVisible(true);
                isValid = false;
            }

            // Validation Sexe
            if (!checkHomme.isSelected() && !checkFemme.isSelected()) {
                lblSexeError.setText("Veuillez choisir un sexe.");
                lblSexeError.setVisible(true);
                isValid = false;
            }

            // Validation Role
            if (!RadioPatient.isSelected() && !RadioMedecin.isSelected()) {
                lblRoleError.setText("Veuillez sélectionner un rôle.");
                lblRoleError.setVisible(true);
                isValid = false;
            }

            if (isValid) {
                // Création et insertion de l'utilisateur
                Utilisateur user = new Utilisateur();
                user.setNom(txtNom.getText());
                user.setPrenom(txtPrenom.getText());
                user.setEmail(txtEmail.getText());
                String hashedPassword = BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt());
                user.setPassword(hashedPassword);
                user.setTelephone(Integer.parseInt(txtTelephone.getText()));
                user.setAdresse(txtAdresse.getText());
                user.setDateNaissance(txtDateNaissance.getValue());

                // Sexe
                if (checkHomme.isSelected()) user.setSexe("Homme");
                else if (checkFemme.isSelected()) user.setSexe("Femme");

                // Rôle
                if (RadioPatient.isSelected()) {
                    user.setRoles("[\"ROLE_PATIENT\"]");
                    user.setTaille(Double.parseDouble(txtTaille.getText()));
                    user.setPoids(Integer.parseInt(txtPoids.getText()));
                    user.setStatus(1);
                } else if (RadioMedecin.isSelected()) {
                    user.setRoles("[\"ROLE_MEDECIN\"]");
                    user.setSpecialite(txtSpecialite.getText());
                    if (selectedDiplomeFile != null) {
                        user.setDiplome(selectedDiplomeFile.getAbsolutePath()); // 🔵 chemin absolu dans la base
                    } else {
                        showAlert("Veuillez insérer votre diplôme !");
                        return;
                    }
                    user.setStatus(0);

                }

                if (selectedImageFile != null) {

                    // Dossier cible dans le projet (à créer si nécessaire)
                    File destDir = new File("user_images");
                    if (!destDir.exists()) {
                        destDir.mkdirs(); // crée le dossier s’il n’existe pas
                    }

                    // Destination du fichier
                    File destFile = new File(destDir, selectedImageFile.getName());

                    // Copie de l’image sélectionnée
                    try {
                        java.nio.file.Files.copy(selectedImageFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        user.setImage(destFile.getAbsolutePath()); // on stocke juste le nom
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur lors de la copie de l'image !");
                        return;
                    }
                } else {
                    user.setImage(null); // ou image par défaut
                }


                // Ajout à la DB
                UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
                utilisateurDAO.ajouterUtilisateur(user);
                showAlert("Inscription réussie !");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginForm.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) txtNom.getScene().getWindow(); // ou n'importe quel autre élément du formulaire
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.show();
            }
            } catch(Exception e){
                e.printStackTrace();
                showAlert("Erreur lors de l'inscription !");
            }

    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void hideErrorLabels() {
        lblNomError.setVisible(false);
        lblPrenomError.setVisible(false);
        lblEmailError.setVisible(false);
        lblPasswordError.setVisible(false);
        lblPasswordMismatch.setVisible(false);
        lblTelephoneError.setVisible(false);
        lblAdresseError.setVisible(false);
        lblDateNaissanceError.setVisible(false);
        lblSexeError.setVisible(false);
        lblRoleError.setVisible(false);
    }


}