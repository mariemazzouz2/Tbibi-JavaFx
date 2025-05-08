package controller.Categorie;

import entities.CategorieEv;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.CategorieEvService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjoutCategorieEv implements Initializable {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;

    private final CategorieEvService categorieEvService = new CategorieEvService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Pas d'initialisation nécessaire pour le ComboBox, car pas de statut
    }

    @FXML
    private void handleAdd() {
        if (validateInputs()) {
            CategorieEv categorieEv = new CategorieEv(
                    nomField.getText(),
                    descriptionField.getText()
            );

            try {
                categorieEvService.ajouter(categorieEv);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès");
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        // Vérification du nom
        String nom = nomField.getText();
        if (nom.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            return false;
        }
        if (nom.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom doit contenir au moins 3 caractères");
            return false;
        }
        if (!nom.matches("[a-zA-Z0-9 ]+")) { // Vérifie que le nom ne contient que des lettres, chiffres et espaces
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom ne doit contenir que des lettres, chiffres et espaces");
            return false;
        }

        // Vérification de la description
        String description = descriptionField.getText();
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est obligatoire");
            return false;
        }
        if (description.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description doit contenir au moins 10 caractères");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
