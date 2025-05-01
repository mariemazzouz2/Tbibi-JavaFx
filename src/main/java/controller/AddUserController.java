package controller;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import java.io.IOException;

public class AddUserController {
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
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
    @FXML private RadioButton RadioAdmin;
    private File selectedImageFile;


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
            System.out.println("Fichier sélectionné : " + selectedFile.getAbsolutePath());
            // Tu peux afficher un aperçu ou stocker le chemin pour l'enregistrement
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
            // 1. Vérification des champs obligatoires
            if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() || txtEmail.getText().isEmpty() ||
                    txtPassword.getText().isEmpty() || txtTelephone.getText().isEmpty() ||
                    txtAdresse.getText().isEmpty() || txtDateNaissance.getValue() == null ||
                    (!checkHomme.isSelected() && !checkFemme.isSelected()) ||
                    (!RadioPatient.isSelected() && !RadioMedecin.isSelected())) {
                showAlert("Veuillez remplir tous les champs obligatoires !");
                return;
            }

            // 2. Nom et prénom avec majuscule
            if (!txtNom.getText().matches("[A-Z][a-zA-Z]*") || !txtPrenom.getText().matches("[A-Z][a-zA-Z]*")) {
                showAlert("Le nom et le prénom doivent commencer par une majuscule.");
                return;
            }

            // 3. Email valide
            if (!txtEmail.getText().contains("@")) {
                showAlert("Adresse email invalide.");
                return;
            }

            // 4. Mot de passe sécurisé
            String password = txtPassword.getText();
            if (!password.matches(".*[A-Z].*") || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()].*")) {
                showAlert("Le mot de passe doit contenir au moins une majuscule, un chiffre et un caractère spécial.");
                return;
            }

            // 5. Téléphone : exactement 8 chiffres
            if (!txtTelephone.getText().matches("\\d{8}")) {
                showAlert("Le numéro de téléphone doit contenir exactement 8 chiffres.");
                return;
            }

            // 6. Champs spécifiques au rôle
            if (RadioPatient.isSelected() && (txtTaille.getText().isEmpty() || txtPoids.getText().isEmpty())) {
                showAlert("Veuillez remplir la taille et le poids pour un patient.");
                return;
            }

            if (RadioMedecin.isSelected() && txtSpecialite.getText().isEmpty()) {
                showAlert("Veuillez spécifier la spécialité pour un médecin.");
                return;
            }

            // Création et insertion de l'utilisateur
            Utilisateur user = new Utilisateur();
            user.setNom(txtNom.getText());
            user.setPrenom(txtPrenom.getText());
            user.setEmail(txtEmail.getText());
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
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
                user.setDiplome("diplome.jpg"); // ou le vrai fichier
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
                    user.setImage(destFile.getName()); // on stocke juste le nom
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
            showAlert("Ajout réussie !");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtNom.getScene().getWindow(); // ou n'importe quel autre élément du formulaire
            stage.setScene(new Scene(root));
            stage.setTitle("Ajout");
            stage.show();

        } catch (Exception e) {
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
    public void goToUsers(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
