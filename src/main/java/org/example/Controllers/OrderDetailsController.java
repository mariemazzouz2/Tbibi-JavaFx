package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Entities.Commande;
import org.example.Entities.Produit;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for viewing order details
 */
public class OrderDetailsController implements Initializable {

    @FXML
    private Label orderIdLabel;
    @FXML
    private Label customerLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<Produit> productsTable;
    @FXML
    private TableColumn<Produit, Integer> colProductId;
    @FXML
    private TableColumn<Produit, String> colProductName;
    @FXML
    private TableColumn<Produit, String> colProductType;
    @FXML
    private TableColumn<Produit, Float> colProductPrice;
    @FXML
    private Label totalLabel;

    private Commande order;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
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
    }

    /**
     * Initialize the view with order data
     * @param order Order to display
     */
    public void initData(Commande order) {
        this.order = order;

        // Set order details
        orderIdLabel.setText("#" + order.getId());
        customerLabel.setText(order.getUser() != null ?
                order.getUser().getName() + " (" + order.getUser().getEmail() + ")" : "N/A");
        dateLabel.setText(order.getDateCommande().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Set status with color coding
        String status = order.getStatut();
        statusLabel.setText(status);
        switch (status) {
            case "En cours":
                statusLabel.setStyle("-fx-text-fill: #f39c12;"); // Orange
                break;
            case "Expédiée":
                statusLabel.setStyle("-fx-text-fill: #3498db;"); // Blue
                break;
            case "Livrée":
                statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green
                break;
            case "Annulée":
                statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red
                break;
            default:
                statusLabel.setStyle("-fx-text-fill: #2c3e50;"); // Default text color
                break;
        }

        // Load products
        ObservableList<Produit> products = FXCollections.observableArrayList(order.getProduits());
        productsTable.setItems(products);

        // Set total
        totalLabel.setText(String.format("€%.2f", order.getMontantTotal()));
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) orderIdLabel.getScene().getWindow();
        stage.close();
    }
}