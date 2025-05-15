package controller.Evenement;

import entities.CategorieEv;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import service.CategorieEvService;
import utils.SceneSwitch;

import java.sql.SQLException;

public class AjouterCategorieEv extends SceneSwitch {

    @FXML
    private TextField nomField;
    
    @FXML
    private TextArea descriptionField;

    private final CategorieEvService service = new CategorieEvService();

    @FXML
    void ajouterCategorie() {
        String nom = nomField.getText().trim();
        String description = descriptionField.getText().trim();

        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom de la catégorie ne peut pas être vide");
            return;
        }

        try {
            CategorieEv categorie = new CategorieEv();
            categorie.setNom(nom);
            categorie.setDescription(description);
            service.ajouter(categorie);
            
            showAlert("Succès", "Catégorie ajoutée avec succès");
            nomField.clear();
            descriptionField.clear();
            
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la catégorie: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 