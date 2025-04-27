package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Utilisateur;
import service.UtilisateurDAO;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierUserController {

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtAdresse, txtTelephone, txtTaille, txtPoids, txtSpecialite;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker txtDateNaissance;
    @FXML private CheckBox checkHomme, checkFemme;
    @FXML private RadioButton RadioMedecin, RadioPatient;
    @FXML private ImageView imagePreview, diplomePreview;
    @FXML private Button btnUploadImage, btnUploaddiplome, btnRegister;
    private Utilisateur utilisateur;
    private ToggleGroup roleGroup;

    private File selectedImageFile;
    private File selectedDiplomeFile;

    @FXML
    public void initialize() {
        // Group radio buttons
        roleGroup = new ToggleGroup();
        RadioMedecin.setToggleGroup(roleGroup);
        RadioPatient.setToggleGroup(roleGroup);

// gestion des actions
        RadioMedecin.setOnAction(e -> toggleRoleFields());
        RadioPatient.setOnAction(e -> toggleRoleFields());



        // Gérer la sélection du sexe (un seul choix à la fois)
        checkHomme.setOnAction(e -> {
            if (checkHomme.isSelected()) checkFemme.setSelected(false);
        });

        checkFemme.setOnAction(e -> {
            if (checkFemme.isSelected()) checkHomme.setSelected(false);
        });
    }

    private void toggleRoleFields() {
        boolean isMedecin = RadioMedecin.isSelected();
        txtTaille.setVisible(!isMedecin);
        txtPoids.setVisible(!isMedecin);
        txtSpecialite.setVisible(isMedecin);
        btnUploaddiplome.setVisible(isMedecin);
        diplomePreview.setVisible(isMedecin);
    }

    @FXML
    public void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            imagePreview.setImage(image);
        }
    }

    @FXML
    public void handleDiplomeUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un diplôme");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        selectedDiplomeFile = fileChooser.showOpenDialog(null);
        if (selectedDiplomeFile != null) {
            diplomePreview.setImage(new Image(getClass().getResource("/icons/pdf_icon.png").toExternalForm())); // Exemple d’icône PDF
        }
    }

    @FXML
    public void btnRegister() {
        String nom = txtNom.getText();
        String prenom = txtPrenom.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String adresse = txtAdresse.getText();
        String telephone = txtTelephone.getText();
        LocalDate dateNaissance = txtDateNaissance.getValue();
        String sexe = checkHomme.isSelected() ? "Homme" : (checkFemme.isSelected() ? "Femme" : "");
        String role = RadioMedecin.isSelected() ? "[\"ROLE_MEDECIN\"]" : "[\"ROLE_PATIENT\"]";

        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setPassword(password);
        utilisateur.setAdresse(adresse);
        utilisateur.setTelephone(Integer.parseInt(telephone));
        utilisateur.setDateNaissance(dateNaissance);
        utilisateur.setSexe(sexe);
        utilisateur.setRoles(role);

        if (role.equals("[\"ROLE_MEDECIN\"]")) {
            utilisateur.setSpecialite(txtSpecialite.getText());
            utilisateur.setTaille(null);
            utilisateur.setPoids(null);
        } else {
            String tailleText = txtTaille.getText();
            if (tailleText != null && !tailleText.isEmpty()) {
                try {
                    utilisateur.setTaille(Double.parseDouble(tailleText));
                } catch (NumberFormatException e) {
                    // Affichez un message d'erreur ou gérez l'exception
                    System.out.println("Erreur de format pour la taille : " + e.getMessage());
                }
            } else {
                utilisateur.setTaille(null); // ou une autre valeur par défaut
            }

            String poidsText = txtPoids.getText();
            if (poidsText != null && !poidsText.isEmpty()) {
                try {
                    utilisateur.setPoids(Integer.valueOf(poidsText));
                } catch (NumberFormatException e) {
                    // Affichez un message d'erreur ou gérez l'exception
                    System.out.println("Erreur de format pour le poids : " + e.getMessage());
                }
            } else {
                utilisateur.setPoids(null); // ou une autre valeur par défaut
            }

            utilisateur.setSpecialite(null);
        }

// Ici, tu peux aussi gérer image et diplome si besoin :
        if (selectedImageFile != null)
            utilisateur.setImage(selectedImageFile.getAbsolutePath());
        if (selectedDiplomeFile != null)
            utilisateur.setDiplome(selectedDiplomeFile.getAbsolutePath());

        try {
            utilisateurDAO.updateUtilisateur(utilisateur);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        // Alerte de succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Utilisateur modifié avec succès !");
        alert.showAndWait();

        // Redirection vers BackendUsers.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml")); // <-- adapte le chemin ici
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void initData(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;

        txtNom.setText(utilisateur.getNom());
            txtPrenom.setText(utilisateur.getPrenom());
            txtEmail.setText(utilisateur.getEmail());
            txtPassword.setText(utilisateur.getPassword());
            txtAdresse.setText(utilisateur.getAdresse());
            txtTelephone.setText(String.valueOf(utilisateur.getTelephone()));
            txtDateNaissance.setValue(utilisateur.getDateNaissance());

            if ("Homme".equals(utilisateur.getSexe())) {
                checkHomme.setSelected(true);
                checkFemme.setSelected(false);
            } else if ("Femme".equals(utilisateur.getSexe())) {
                checkFemme.setSelected(true);
                checkHomme.setSelected(false);
            }

        if ("Medecin".equalsIgnoreCase(utilisateur.getRoles())) {
            RadioMedecin.setSelected(true);
            roleGroup.selectToggle(RadioMedecin); // très important !!
            txtSpecialite.setText(utilisateur.getSpecialite());
            toggleRoleFields();
        } else if ("Patient".equalsIgnoreCase(utilisateur.getRoles())) {
            RadioPatient.setSelected(true);
            roleGroup.selectToggle(RadioPatient); // très important !!
            txtTaille.setText(String.valueOf(utilisateur.getTaille()));
            txtPoids.setText(String.valueOf(utilisateur.getPoids()));
            toggleRoleFields();
        }



    }

}
