package org.example.Controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.Entities.Commande;
import org.example.Services.CartItemService;
import org.example.Services.CommandeService;
import org.example.Services.EmailService;
import org.example.Services.FlouciPaymentService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

public class PaymentController implements Initializable {

    @FXML
    private WebView webView;

    @FXML
    private Button closeButton;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox mainLayout;

    private WebEngine webEngine;
    private FlouciPaymentService paymentService;
    private Commande currentOrder;
    private Runnable onPaymentSuccess;
    private String currentPaymentId;
    private Button verifyButton;
    private EmailService emailService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paymentService = new FlouciPaymentService();
        emailService = new EmailService();
        webEngine = webView.getEngine();

        // Monitor URL changes for detecting payment result
        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            System.out.println("Navigation: " + newLocation);

            // For development environment: detect our redirects to Google
            if (newLocation.contains("google.com/payment/success")) {
                Platform.runLater(() -> {
                    if (currentPaymentId != null) {
                        verifyPayment();
                    }
                });
            } else if (newLocation.contains("google.com/payment/fail")) {
                // Mark payment as failed and process failure
                Platform.runLater(() -> {
                    System.out.println("Failed payment URL detected");
                    // Set the failed flag in the payment service
                    paymentService.setPaymentFailedByNavigation();
                    handlePaymentFailure();
                });
            } else if (newLocation.toLowerCase().contains("échec") ||
                    newLocation.toLowerCase().contains("echoue") ||
                    newLocation.toLowerCase().contains("cancel")) {
                // Also detect Flouci's own failure page
                Platform.runLater(() -> {
                    System.out.println("Flouci failure page detected");
                    paymentService.setPaymentFailedByNavigation();
                    handlePaymentFailure();
                });
            }
        });

        // Create a verify payment button for manual verification
        verifyButton = new Button("Verify Payment Status");
        verifyButton.setOnAction(e -> verifyPayment());
        verifyButton.setPrefWidth(200);
        verifyButton.setVisible(false); // Hide initially

        // Add the verify button to the layout
        mainLayout.getChildren().add(verifyButton);

        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    private void handlePaymentFailure() {
        statusLabel.setText("Payment verification failed");

        // Send failure email
        try {
            if (currentOrder.getUser() != null && currentOrder.getUser().getEmail() != null) {
                String userEmail = currentOrder.getUser().getEmail();
                emailService.sendPaymentFailureEmail(
                        userEmail,
                        currentOrder.getId()
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error sending failure email: " + ex.getMessage());
        }

        showPaymentFailedAlert();
    }

    /**
     * Start payment process for an order
     * @param order Order to process payment for
     * @param onSuccess Callback to run on successful payment
     */
    public void processPayment(Commande order, Runnable onSuccess) {
        this.currentOrder = order;
        this.onPaymentSuccess = onSuccess;
        this.currentPaymentId = null;

        statusLabel.setText("Initializing payment...");

        // Create a background task to initialize payment
        Task<Map<String, String>> task = new Task<Map<String, String>>() {
            @Override
            protected Map<String, String> call() throws Exception {
                return paymentService.initiatePayment(order);
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, String> result = task.getValue();
            String paymentUrl = result.get("paymentUrl");
            currentPaymentId = result.get("paymentId");

            System.out.println("Payment URL: " + paymentUrl);
            System.out.println("Payment ID: " + currentPaymentId);

            webEngine.load(paymentUrl);
            statusLabel.setText("Please complete the payment");

            // Show verify button after 15 seconds
            // (in case redirect doesn't work)
            new Thread(() -> {
                try {
                    Thread.sleep(15000);
                    Platform.runLater(() -> {
                        verifyButton.setVisible(true);
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Payment Error",
                    "Could not initiate payment: " + exception.getMessage());
            statusLabel.setText("Payment initialization failed");
        });

        new Thread(task).start();
    }

    /**
     * Verify payment status with Flouci API
     */
    /**
     * Verify payment status with Flouci API
     */
    private void verifyPayment() {
        if (currentPaymentId == null) {
            showAlert(Alert.AlertType.ERROR, "Verification Error",
                    "No payment ID available to verify");
            return;
        }

        statusLabel.setText("Verifying payment...");

        Task<Map<String, Object>> detailsTask = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return paymentService.checkPaymentStatus(currentPaymentId);
            }
        };

        detailsTask.setOnSucceeded(e -> {
            Map<String, Object> paymentDetails = detailsTask.getValue();
            System.out.println("Payment check details: " + paymentDetails);

            // Check if the wallet code is the test failure code
            if (paymentDetails.containsKey("walletCode") &&
                    "000000".equals(paymentDetails.get("walletCode"))) {

                // This is a test failure case
                statusLabel.setText("Payment failed (test wallet)");
                handlePaymentFailure();
                return;
            }

            // Now do the actual verification
            Task<Boolean> verifyTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return paymentService.verifyPayment(currentPaymentId);
                }
            };

            verifyTask.setOnSucceeded(event -> {
                boolean isSuccess = verifyTask.getValue();

                if (isSuccess) {
                    statusLabel.setText("Payment successful!");
                    handlePaymentSuccess();
                } else {
                    handlePaymentFailure();
                }
            });

            verifyTask.setOnFailed(event -> {
                Throwable exception = verifyTask.getException();
                exception.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Verification Error",
                        "Could not verify payment: " + exception.getMessage());
                statusLabel.setText("Payment verification error");

                handlePaymentFailure();
            });

            new Thread(verifyTask).start();
        });

        detailsTask.setOnFailed(e -> {
            Throwable exception = detailsTask.getException();
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Verification Error",
                    "Could not check payment details: " + exception.getMessage());
            statusLabel.setText("Payment verification error");

            handlePaymentFailure();
        });

        new Thread(detailsTask).start();
    }
    /**
     * Clear the shopping cart after successful payment
     */
    private void clearShoppingCart() {
        try {
            // Use your cart service to clear the cart
            CartItemService cartItemService = new CartItemService();
            cartItemService.clearCart();
            System.out.println("Shopping cart cleared successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error clearing shopping cart: " + ex.getMessage());
        }
    }
    private void handlePaymentSuccess() {
        try {
            CommandeService commandeService = new CommandeService();

            // Print current order details for debugging
            System.out.println("Before update - Order ID: " + currentOrder.getId());
            System.out.println("Before update - Order Status: " + currentOrder.getStatut());

            // Update the status
            currentOrder.setStatut("Paid");

            System.out.println("Attempting to update order with status: " + currentOrder.getStatut());
            commandeService.updateCommande(currentOrder);

            System.out.println("Order status updated successfully");

            // Clear the shopping cart
            clearShoppingCart();

            // Send confirmation email
            if (currentOrder.getUser() != null && currentOrder.getUser().getEmail() != null) {
                String userEmail = currentOrder.getUser().getEmail();
                emailService.sendPaymentConfirmationEmail(
                        userEmail,
                        currentOrder.getId(),
                        currentOrder.getMontantTotal()
                );
            }

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                    "Your payment has been processed successfully!");

            // Navigate to invoice after successful payment
            navigateToInvoice(currentOrder);

            // Run success callback
            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error updating order status: " + ex.getMessage());

            // Show error but still consider payment successful
            showAlert(Alert.AlertType.WARNING, "Order Update Warning",
                    "Payment was successful but there was an issue updating your order. Please contact support.");
        }
    }
    /**
     * Navigate to the invoice page for a completed order
     * @param order The completed order
     */
    private void navigateToInvoice(Commande order) {
        try {
            // Load the invoice view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/FactureView.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the order directly
            FactureController controller = loader.getController();
            controller.setCommande(order);

            // Create a new stage for the invoice
            Stage invoiceStage = new Stage();
            invoiceStage.setTitle("Facture #" + order.getId());
            invoiceStage.setScene(new Scene(root));

            // Close the current payment window
            Platform.runLater(() -> {
                Stage currentStage = (Stage) webView.getScene().getWindow();
                currentStage.close();

                // Show the invoice stage
                invoiceStage.show();
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open invoice: " + ex.getMessage());
        }
    }
    /**
     * Show payment failed alert
     */
    private void showPaymentFailedAlert() {
        showAlert(Alert.AlertType.ERROR, "Payment Failed",
                "Payment could not be completed. Please try again.");
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}