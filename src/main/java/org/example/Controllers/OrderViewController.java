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
import org.example.Entities.Commande;
import org.example.Entities.Utilisateur;
import org.example.Services.CommandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the order management view
 */
public class OrderViewController implements Initializable {

    @FXML
    private TableView<Commande> orderTable;
    @FXML
    private TableColumn<Commande, Integer> colId;
    @FXML
    private TableColumn<Commande, Float> colMontantTotal;
    @FXML
    private TableColumn<Commande, LocalDate> colDateCommande;
    @FXML
    private TableColumn<Commande, String> colStatut;
    @FXML
    private TableColumn<Commande, Utilisateur> colUser;
    @FXML
    private TableColumn<Commande, Integer> colProductCount;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;

    private CommandeService commandeService;
    private ObservableList<Commande> orderList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        commandeService = new CommandeService();

        // Initialize columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colDateCommande.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));

        // Custom cell factory for User column to display the user's name
        colUser.setCellFactory(col -> new TableCell<Commande, Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getName());
                }
            }
        });

        // Custom cell factory for product count column
        colProductCount.setCellFactory(col -> new TableCell<Commande, Integer>() {
            @Override
            protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);
                if (empty) {
                    setText(null);
                } else {
                    Commande commande = getTableView().getItems().get(getIndex());
                    setText(String.valueOf(commande.getProduits().size()));
                }
            }
        });

        // Format currency for montant total column
        colMontantTotal.setCellFactory(col -> new TableCell<Commande, Float>() {
            @Override
            protected void updateItem(Float amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", amount));
                }
            }
        });

        // Set up date formatters for the date column
        colDateCommande.setCellFactory(col -> new TableCell<Commande, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            }
        });

        // Set up status filter
        statusFilterComboBox.getItems().addAll("All", "En cours", "Expédiée", "Livrée", "Annulée");
        statusFilterComboBox.setValue("All");

        // Setup date pickers
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());

        // Initial load of orders with a fade-in animation
        orderTable.setOpacity(0.0);
        loadOrders();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), orderTable);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Load all orders from database
     */
    private void loadOrders() {
        List<Commande> commandes = commandeService.getAllCommandes();
        orderList = FXCollections.observableArrayList(commandes);
        orderTable.setItems(orderList);
    }

    @FXML
    private void handleSearchOrders() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilterComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // If all filters are at default, load all
        if (searchTerm.isEmpty() && "All".equals(selectedStatus) &&
                fromDate == null && toDate == null) {
            loadOrders();
            return;
        }

        List<Commande> allOrders = commandeService.getAllCommandes();
        ObservableList<Commande> filteredList = FXCollections.observableArrayList();

        for (Commande order : allOrders) {
            boolean matchesSearch = searchTerm.isEmpty() ||
                    String.valueOf(order.getId()).contains(searchTerm) ||
                    (order.getUser() != null && order.getUser().getName().toLowerCase().contains(searchTerm));

            boolean matchesStatus = "All".equals(selectedStatus) ||
                    (order.getStatut() != null && order.getStatut().equals(selectedStatus));

            boolean matchesDateRange = true;
            if (fromDate != null && order.getDateCommande().isBefore(fromDate)) {
                matchesDateRange = false;
            }
            if (toDate != null && order.getDateCommande().isAfter(toDate)) {
                matchesDateRange = false;
            }

            if (matchesSearch && matchesStatus && matchesDateRange) {
                filteredList.add(order);
            }
        }

        // Apply a fade effect when changing the table items
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), orderTable);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.7);
        fadeOut.setOnFinished(e -> {
            orderTable.setItems(filteredList);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderTable);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handleResetFilters() {
        // Add animation when resetting filters
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), orderTable);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.5);
        fadeOut.setOnFinished(e -> {
            searchField.clear();
            statusFilterComboBox.setValue("All");
            fromDatePicker.setValue(LocalDate.now().minusMonths(1));
            toDatePicker.setValue(LocalDate.now());
            loadOrders();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), orderTable);
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handleAddOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/AddOrderView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Create New Order");
            stage.initModality(Modality.APPLICATION_MODAL);

            // Get controller and set callback for order addition
            AddOrderController controller = loader.getController();
            controller.setOrderAddedCallback(() -> {
                loadOrders();
                // Add animation when order is added
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), orderTable);
                fadeIn.setFromValue(0.7);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(OrderViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleEditOrder(ActionEvent event) {
        Commande selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/EditOrderView.fxml"));
            Parent root = loader.load();

            EditOrderController controller = loader.getController();
            controller.initData(selectedOrder);
            controller.setOrderEditedCallback(() -> {
                loadOrders();
                // Add animation when order is edited
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), orderTable);
                fadeIn.setFromValue(0.7);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Order #" + selectedOrder.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(OrderViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleViewOrderDetails(ActionEvent event) {
        Commande selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order to view its details.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/OrderDetailsView.fxml"));
            Parent root = loader.load();

            OrderDetailsController controller = loader.getController();
            controller.initData(selectedOrder);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Order Details - #" + selectedOrder.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(OrderViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleDeleteOrder(ActionEvent event) {
        Commande selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Order");
        alert.setHeaderText("Delete Order #" + selectedOrder.getId());
        alert.setContentText("Are you sure you want to delete this order? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandeService.deleteCommande(selectedOrder.getId());

                // Add fade out animation when deleting an order
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), orderTable);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.7);
                fadeOut.setOnFinished(e -> {
                    loadOrders();
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), orderTable);
                    fadeIn.setFromValue(0.7);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully.");
            } catch (SQLException ex) {
                Logger.getLogger(OrderViewController.class.getName()).log(Level.SEVERE, null, ex);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete order: " + ex.getMessage());
            }
        }
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