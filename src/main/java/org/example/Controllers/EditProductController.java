package org.example.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Entities.Produit;
import org.example.Services.ProduitService;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for editing a product
 */
public class EditProductController implements Initializable {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prixField;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField imagePathField;

    private ProduitService produitService;
    private Produit produit;
    private Runnable productEditedCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();

        // Initialize type dropdown
        typeComboBox.getItems().addAll("Electronics", "Clothing", "Food", "Books");

        // Set up number validation for price field
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldValue);
            }
        });
    }

    /**
     * Initialize the form with product data
     * @param produit Product to edit
     */
    public void initData(Produit produit) {
        this.produit = produit;

        // Populate fields with product data
        nomField.setText(produit.getNom());
        prixField.setText(String.valueOf(produit.getPrix()));
        typeComboBox.setValue(produit.getType());
        descriptionArea.setText(produit.getDescription());
        imagePathField.setText(produit.getImage());
    }

    /**
     * Set callback to be called after product is edited
     * @param callback Runnable to execute
     */
    public void setProductEditedCallback(Runnable callback) {
        this.productEditedCallback = callback;
    }

    @FXML
    private void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getPath());
        }
    }

    @FXML
    private void handleUpdateProduct(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            String nom = nomField.getText().trim();
            float prix = Float.parseFloat(prixField.getText().trim());
            String type = typeComboBox.getValue();
            String description = descriptionArea.getText().trim();
            String imagePath = imagePathField.getText().trim();

            produit.setNom(nom);
            produit.setPrix(prix);
            produit.setType(type);
            produit.setDescription(description);
            produit.setImage(imagePath);

            produitService.updateProduit(produit);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully.");

            if (productEditedCallback != null) {
                productEditedCallback.run();
            }

            closeWindow();
        } catch (SQLException ex) {
            Logger.getLogger(EditProductController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price must be a valid number.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    /**
     * Validate user input
     * @return true if input is valid
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (nomField.getText().trim().isEmpty()) {
            errorMessage.append("Product name cannot be empty.\n");
        }

        if (prixField.getText().trim().isEmpty()) {
            errorMessage.append("Price cannot be empty.\n");
        }

        if (typeComboBox.getValue() == null) {
            errorMessage.append("Please select a product type.\n");
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            errorMessage.append("Description cannot be empty.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Close the current window
     */
    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    /**
     * Show an alert dialog
     * @param type Alert type
     * @param title Dialog title
     * @param message Dialog message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}