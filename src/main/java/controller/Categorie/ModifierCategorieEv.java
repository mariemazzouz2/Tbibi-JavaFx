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

public class ModifierCategorieEv implements Initializable {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;

    private CategorieEv categorieEv;
    private final CategorieEvService categorieEvService = new CategorieEvService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Pas d'initialisation nécessaire pour le ComboBox, car pas de statut
    }

    public void setCategorieEv(CategorieEv categorieEv) {
        this.categorieEv = categorieEv;
        nomField.setText(categorieEv.getNom());
        descriptionField.setText(categorieEv.getDescription());
    }

    @FXML
    private void handleSave() {
        if (validateInputs()) {
            categorieEv.setNom(nomField.getText());
            categorieEv.setDescription(descriptionField.getText());

            try {
                categorieEvService.modifier(categorieEv);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie modifiée avec succès");
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (nomField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            return false;
        }
        if (descriptionField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est obligatoire");
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