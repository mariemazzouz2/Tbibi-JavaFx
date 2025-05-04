package controller;



import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Utilisateur;
import service.UtilisateurDAO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ModifierController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField adresseField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<String> sexeComboBox;

    @FXML private TextField specialiteField;
    @FXML private TextField diplomeField;

    @FXML private TextField tailleField;
    @FXML private TextField poidsField;
    @FXML private ImageView imageView;
    private File selectedImageFile; // Pour garder une référence à la nouvelle image sélectionnée
    @FXML private ImageView diplomeImageView;
    private File selectedDiplomeImageFile;

    private Utilisateur utilisateur;

    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;

        // Remplir les champs
        nomField.setText(u.getNom());
        prenomField.setText(u.getPrenom());
        emailField.setText(u.getEmail());
        adresseField.setText(u.getAdresse());
        dateNaissancePicker.setValue(u.getDateNaissance());
        telephoneField.setText(String.valueOf(u.getTelephone()));
        sexeComboBox.setValue(u.getSexe());

        // Afficher les champs selon le rôle
        if (u.getRoles().contains("ROLE_MEDECIN")) {
            specialiteField.setVisible(true);
            specialiteField.setManaged(true);
            diplomeField.setVisible(true);
            diplomeField.setManaged(true);

            diplomeImageView.setVisible(true);
            diplomeImageView.setManaged(true);

            specialiteField.setText(u.getSpecialite());
            diplomeField.setText(u.getDiplome());

            if (u.getDiplome() != null && !u.getDiplome().isEmpty()) {
                File diplomeFile = new File(u.getDiplome());
                if (diplomeFile.exists()) {
                    diplomeImageView.setImage(new Image(diplomeFile.toURI().toString()));
                }
            }

    } else if (u.getRoles().contains("ROLE_PATIENT")) {
            tailleField.setVisible(true);
            tailleField.setManaged(true);
            poidsField.setVisible(true);
            poidsField.setManaged(true);

            if (u.getTaille() != null)
                tailleField.setText(String.valueOf(u.getTaille()));
            if (u.getPoids() != null)
                poidsField.setText(String.valueOf(u.getPoids()));
        }
        if (u.getImage() != null && !u.getImage().isEmpty()) {
            File imageFile = new File(u.getImage());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                System.out.println("Image non trouvée : " + imageFile.getAbsolutePath());
            }

        }

    }
    public void initData(Utilisateur utilisateur) {
        setUtilisateur(utilisateur);
    }

    @FXML
    private void onSave() {
        // Mettre à jour l'objet utilisateur
        utilisateur.setNom(nomField.getText());
        utilisateur.setPrenom(prenomField.getText());
        utilisateur.setEmail(emailField.getText());
        utilisateur.setAdresse(adresseField.getText());
        utilisateur.setDateNaissance(dateNaissancePicker.getValue());
        utilisateur.setTelephone(Integer.parseInt(telephoneField.getText()));
        utilisateur.setSexe(sexeComboBox.getValue());

        if (utilisateur.getRoles().contains("ROLE_MEDECIN")) {
            utilisateur.setSpecialite(specialiteField.getText());
            utilisateur.setDiplome(diplomeField.getText());
        } else {
            utilisateur.setSpecialite(null);
            utilisateur.setDiplome(null);
        }
        if (selectedImageFile != null) {
            // Copier l’image dans un dossier de ton application
            File destination = new File("user_images/" + selectedImageFile.getName());
            try {
                Files.createDirectories(destination.getParentFile().toPath());
                Files.copy(selectedImageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                utilisateur.setImage("user_images/" + selectedImageFile.getName());
                // Mettre à jour le chemin dans l'objet utilisateur
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (selectedDiplomeImageFile != null) {
            File destination = new File("user_diplomes/" + selectedDiplomeImageFile.getName());
            try {
                Files.createDirectories(destination.getParentFile().toPath());
                Files.copy(selectedDiplomeImageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                utilisateur.setDiplome("user_diplomes/" + selectedDiplomeImageFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (utilisateur.getRoles().contains("ROLE_PATIENT")) {
            utilisateur.setTaille(tailleField.getText().isEmpty() ? null : Double.parseDouble(tailleField.getText()));
            utilisateur.setPoids(poidsField.getText().isEmpty() ? null : Integer.parseInt(poidsField.getText()));
        } else {
            utilisateur.setTaille(null);
            utilisateur.setPoids(null);
        }

        // Appel à la DAO
        UtilisateurDAO dao = new UtilisateurDAO();
        dao.modifierUtilisateur(utilisateur);

        // Fermer la fenêtre
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void onChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            Image newImage = new Image(file.toURI().toString());
            imageView.setImage(newImage);
        }
    }
    @FXML
    private void onChangeDiplomeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image du diplôme");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(diplomeImageView.getScene().getWindow());

        if (file != null) {
            selectedDiplomeImageFile = file;
            diplomeImageView.setImage(new Image(file.toURI().toString()));
        }
    }


}

