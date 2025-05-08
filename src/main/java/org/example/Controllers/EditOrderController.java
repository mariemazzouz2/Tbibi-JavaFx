package org.example.Controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.Commande;
import org.example.Entities.Produit;
import org.example.Entities.Utilisateur;
import org.example.Services.CommandeService;
import org.example.Services.ProduitService;
import org.example.Services.UtilisateurService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for editing an order
 */
public class EditOrderController implements Initializable {

    @FXML
    private Label orderIdLabel;
    @FXML
    private ComboBox<Utilisateur> customerComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TableView<Produit> availableProductsTable;
    @FXML
    private TableColumn<Produit, Integer> colProductId;
    @FXML
    private TableColumn<Produit, String> colProductName;
    @FXML
    private TableColumn<Produit, String> colProductType;
    @FXML
    private TableColumn<Produit, Float> colProductPrice;
    @FXML
    private ListView<Produit> orderProductsList;
    @FXML
    private Label totalLabel;

    private CommandeService commandeService;
    private ProduitService produitService;
    private UtilisateurService utilisateurService;

    private ObservableList<Produit> availableProducts;
    private ObservableList<Produit> selectedProducts;

    private Commande currentOrder;
    private Runnable orderEditedCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        commandeService = new CommandeService();
        produitService = new ProduitService();
        utilisateurService = new UtilisateurService();

        selectedProducts = FXCollections.observableArrayList();

        // Initialize table columns for available products
        colProductId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colProductType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colProductPrice.setCellValueFactory(new PropertyValueFactory<>("prix"));

        // Format currency for price column
        colProductPrice.setCellFactory(col -> new TableCell<Produit, Float>() {
            @Override
            protected void updateItem(Float price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", price));
                }
            }
        });

        // Set up order products list with a custom cell factory
        orderProductsList.setCellFactory(param -> new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getNom() + " - €" + String.format("%.2f", product.getPrix()));
                }
            }
        });

        // Set up order products list listener to recalculate total when items change
        orderProductsList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Produit> c) -> {
            updateTotalPrice();
        });

        // Initialize status dropdown
        statusComboBox.getItems().addAll("En cours", "Expédiée", "Livrée", "Annulée");

        // Load customers
        loadCustomers();
    }

    /**
     * Initialize the form with order data
     * @param order Order to edit
     */
    public void initData(Commande order) {
        this.currentOrder = order;

        // Set order details
        orderIdLabel.setText("#" + order.getId());
        customerComboBox.setValue(order.getUser());
        datePicker.setValue(order.getDateCommande());
        statusComboBox.setValue(order.getStatut());

        // Load products in the order
        selectedProducts.addAll(order.getProduits());
        orderProductsList.setItems(selectedProducts);

        // Load available products (excluding those already in the order)
        loadAvailableProducts();

        // Update total price
        updateTotalPrice();
    }

    /**
     * Set callback to be called after order is edited
     * @param callback Runnable to execute
     */
    public void setOrderEditedCallback(Runnable callback) {
        this.orderEditedCallback = callback;
    }

    /**
     * Load all available products
     */
    private void loadAvailableProducts() {
        List<Produit> products = produitService.getAllProduits();
        // Filter out products already associated with orders
        List<Produit> availableProductsList = new ArrayList<>();
        for (Produit product : products) {
            if (product.getCommande() == null ||
                    (currentOrder != null && product.getCommande().getId() == currentOrder.getId())) {
                // If product is not in any order or is in the current order
                // (then it's available to select but not already selected)
                if (!selectedProducts.contains(product)) {
                    availableProductsList.add(product);
                }
            }
        }
        availableProducts = FXCollections.observableArrayList(availableProductsList);
        availableProductsTable.setItems(availableProducts);
    }

    /**
     * Load all customers
     */
    private void loadCustomers() {
        List<Utilisateur> customers = utilisateurService.getAllUtilisateurs();
        customerComboBox.setItems(FXCollections.observableArrayList(customers));

        // Custom display for customer ComboBox
        customerComboBox.setCellFactory(param -> new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getName() + " (" + user.getEmail() + ")");
                }
            }
        });

        // Custom display for selected value in ComboBox
        customerComboBox.setButtonCell(new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getName() + " (" + user.getEmail() + ")");
                }
            }
        });
    }

    /**
     * Update the total price label
     */
    private void updateTotalPrice() {
        float total = 0;
        for (Produit product : selectedProducts) {
            total += product.getPrix();
        }
        totalLabel.setText(String.format("€%.2f", total));
    }

    @FXML
    private void handleAddToOrder() {
        Produit selectedProduct = availableProductsTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product to add to the order.");
            return;
        }

        // Add to selected products and remove from available products
        selectedProducts.add(selectedProduct);
        availableProducts.remove(selectedProduct);

        // Update list view with animation
        orderProductsList.setOpacity(0.5);
        orderProductsList.setItems(selectedProducts);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderProductsList);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Update total price
        updateTotalPrice();
    }

    @FXML
    private void handleRemoveFromOrder() {
        Produit selectedProduct = orderProductsList.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product to remove from the order.");
            return;
        }

        // Remove from selected products and add back to available products
        selectedProducts.remove(selectedProduct);
        availableProducts.add(selectedProduct);

        // Update list view with animation
        orderProductsList.setOpacity(0.5);
        orderProductsList.setItems(selectedProducts);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderProductsList);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Update total price
        updateTotalPrice();
    }

    @FXML
    private void handleUpdateOrder() {
        if (!validateInput()) {
            return;
        }

        try {
            Utilisateur customer = customerComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String status = statusComboBox.getValue();

            // Calculate total amount
            float totalAmount = 0;
            for (Produit product : selectedProducts) {
                totalAmount += product.getPrix();
            }

            // Update order
            currentOrder.setUser(customer);
            currentOrder.setDateCommande(date);
            currentOrder.setStatut(status);
            currentOrder.setMontantTotal(totalAmount);

            // Update products
            List<Produit> currentProducts = new ArrayList<>(currentOrder.getProduits());

            // Remove products no longer in the order
            for (Produit product : currentProducts) {
                if (!selectedProducts.contains(product)) {
                    currentOrder.removeProduit(product);
                }
            }

            // Add new products to the order
            for (Produit product : selectedProducts) {
                if (!currentProducts.contains(product)) {
                    currentOrder.addProduit(product);
                }
            }

            // Save order to database
            commandeService.updateCommande(currentOrder);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order updated successfully.");

            if (orderEditedCallback != null) {
                orderEditedCallback.run();
            }

            closeWindow();
        } catch (SQLException ex) {
            Logger.getLogger(EditOrderController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Validate user input
     * @return true if input is valid
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (customerComboBox.getValue() == null) {
            errorMessage.append("Please select a customer.\n");
        }

        if (datePicker.getValue() == null) {
            errorMessage.append("Please select a date.\n");
        }

        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            errorMessage.append("Please select a status.\n");
        }

        if (selectedProducts.isEmpty()) {
            errorMessage.append("Please add at least one product to the order.\n");
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
        Stage stage = (Stage) datePicker.getScene().getWindow();
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