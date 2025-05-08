package org.example.Controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.Produit;
import org.example.Services.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the product management view
 */
public class ProductViewController implements Initializable {

    @FXML
    private TableView<Produit> productTable;
    @FXML
    private TableColumn<Produit, Integer> colId;
    @FXML
    private TableColumn<Produit, String> colNom;
    @FXML
    private TableColumn<Produit, String> colType;
    @FXML
    private TableColumn<Produit, Float> colPrix;
    @FXML
    private TableColumn<Produit, String> colDescription;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeFilterComboBox;

    private ProduitService produitService;
    private ObservableList<Produit> productList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();

        // Initialize columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Set up type filter
        typeFilterComboBox.getItems().add("All Types");
        // You can populate this with actual types from database
        typeFilterComboBox.getItems().addAll("VITAMINE", "BEAUTÉ", "HYGIÈNE", "PEAU");
        typeFilterComboBox.setValue("All Types");

        // Add listener for search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchProducts();
        });

        // Add listener for type filter
        typeFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            searchProducts();
        });

        // Initial load of products with a fade-in animation
        productTable.setOpacity(0.0);
        loadProducts();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), productTable);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Load all products from database
     */
    private void loadProducts() {
        List<Produit> produits = produitService.getAllProduits();
        productList = FXCollections.observableArrayList(produits);
        productTable.setItems(productList);
    }

    /**
     * Search products based on search field and type filter
     */
    private void searchProducts() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        String selectedType = typeFilterComboBox.getValue();

        // If both search term and type filter are default, load all
        if (searchTerm.isEmpty() && "All Types".equals(selectedType)) {
            loadProducts();
            return;
        }

        List<Produit> allProducts = produitService.getAllProduits();
        ObservableList<Produit> filteredList = FXCollections.observableArrayList();

        for (Produit product : allProducts) {
            boolean matchesSearch = searchTerm.isEmpty() ||
                    product.getNom().toLowerCase().contains(searchTerm) ||
                    product.getDescription().toLowerCase().contains(searchTerm);

            boolean matchesType = "All Types".equals(selectedType) ||
                    (product.getType() != null && product.getType().equals(selectedType));

            if (matchesSearch && matchesType) {
                filteredList.add(product);
            }
        }

        // Apply a fade effect when changing the table items
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), productTable);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.7);
        fadeOut.setOnFinished(e -> {
            productTable.setItems(filteredList);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), productTable);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/AddProductView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL);

            // Get controller and set callback for product addition
            AddProductController controller = loader.getController();
            controller.setProductAddedCallback(() -> {
                loadProducts();
                // Add animation when product is added
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), productTable);
                fadeIn.setFromValue(0.7);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(ProductViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        Produit selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/EditProductView.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.initData(selectedProduct);
            controller.setProductEditedCallback(() -> {
                loadProducts();
                // Add animation when product is edited
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), productTable);
                fadeIn.setFromValue(0.7);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(ProductViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        Produit selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete Product: " + selectedProduct.getNom());
        alert.setContentText("Are you sure you want to delete this product? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                produitService.deleteProduit(selectedProduct.getId());

                // Add fade out animation when deleting a product
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), productTable);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.7);
                fadeOut.setOnFinished(e -> {
                    loadProducts();
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), productTable);
                    fadeIn.setFromValue(0.7);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully.");
            } catch (SQLException ex) {
                Logger.getLogger(ProductViewController.class.getName()).log(Level.SEVERE, null, ex);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleRefreshProducts(ActionEvent event) {
        // Add animation when refreshing the product list
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), productTable);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.5);
        fadeOut.setOnFinished(e -> {
            searchField.clear();
            typeFilterComboBox.setValue("All Types");
            loadProducts();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), productTable);
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
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