package controller.Evenement;

import entities.CategorieEv;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import service.CategorieEvService;
import utils.SceneSwitch;

import java.sql.SQLException;

public class ModifierCategorieEv extends SceneSwitch {

    @FXML
    private TextField nomField;
    
    @FXML
    private TextArea descriptionField;

    private final CategorieEvService service = new CategorieEvService();
    private CategorieEv categorie;

    public void setCategorie(CategorieEv categorie) {
        this.categorie = categorie;
        nomField.setText(categorie.getNom());
        descriptionField.setText(categorie.getDescription());
    }

    @FXML
    void modifierCategorie() {
        String nom = nomField.getText().trim();
        String description = descriptionField.getText().trim();

        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom de la catégorie ne peut pas être vide");
            return;
        }

        try {
            categorie.setNom(nom);
            categorie.setDescription(description);
            service.modifier(categorie);
            
            showAlert("Succès", "Catégorie modifiée avec succès");
            
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification de la catégorie: " + e.getMessage());
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