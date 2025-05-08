package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.example.Entities.Commande;
import org.example.Entities.Produit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class FactureController implements Initializable {

    @FXML private Label numeroFactureLabel;
    @FXML private Label dateLabel;
    @FXML private Label clientNameLabel;
    @FXML private Label clientEmailLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;
    @FXML private Button downloadPdfButton;
    @FXML private Button printButton;
    @FXML private Button closeButton;
    @FXML private VBox productsContainer;
    @FXML private VBox contentContainer;

    private Commande commande;
    private NumberFormat currencyFormat;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("fr", "TN"));

        // Setup current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateLabel.setText("Date: " + dateFormat.format(new Date()));

        // Setup buttons
        downloadPdfButton.setOnAction(e -> exportToPdf());
        printButton.setOnAction(e -> printFacture());
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Set the order and display its details
     * @param commande The order to display
     */
    public void setCommande(Commande commande) {
        this.commande = commande;

        if (commande == null) {
            showAlert("Erreur", "La commande n'a pas été trouvée.");
            return;
        }

        // Set basic information
        numeroFactureLabel.setText("N° " + commande.getId());

        // Set client information
        if (commande.getUser() != null) {
            clientNameLabel.setText(commande.getUser().getName());
            clientEmailLabel.setText(commande.getUser().getEmail());
        } else {
            clientNameLabel.setText("Client invité");
            clientEmailLabel.setText("Non disponible");
        }

        // Add product rows
        productsContainer.getChildren().clear();
        double subtotal = 0;

        if (commande.getProduits() != null && !commande.getProduits().isEmpty()) {
            for (Produit produit : commande.getProduits()) {
                // Create product row
                GridPane productRow = createProductRow(produit);
                productsContainer.getChildren().add(productRow);

                // Add to subtotal
                subtotal += produit.getPrix();
            }
        } else {
            Label noProductsLabel = new Label("Aucun produit disponible");
            noProductsLabel.getStyleClass().add("info-value");
            productsContainer.getChildren().add(noProductsLabel);
        }

        // Set totals
        double taxRate = 0.19; // 19% VAT
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        subtotalLabel.setText(currencyFormat.format(subtotal));
        taxLabel.setText(currencyFormat.format(tax));
        totalLabel.setText(currencyFormat.format(total));

        // Set status with appropriate style
        statusLabel.setText(commande.getStatut());
        statusLabel.getStyleClass().removeAll("status-paid", "status-pending");

        if ("Paid".equalsIgnoreCase(commande.getStatut()) || "Payé".equalsIgnoreCase(commande.getStatut())) {
            statusLabel.getStyleClass().add("status-paid");
        } else {
            statusLabel.getStyleClass().add("status-pending");
        }
    }

    /**
     * Create a row for a product in the invoice
     */
    private GridPane createProductRow(Produit produit) {
        GridPane row = new GridPane();
        row.getStyleClass().add("product-row");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(15);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(20);

        row.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Product name
        Label nameLabel = new Label(produit.getNom());
        GridPane.setConstraints(nameLabel, 0, 0);

        // Quantity (default to 1 for simplicity)
        Label qtyLabel = new Label("1");
        qtyLabel.setAlignment(Pos.CENTER);
        GridPane.setConstraints(qtyLabel, 1, 0);

        // Unit price
        Label priceLabel = new Label(currencyFormat.format(produit.getPrix()));
        priceLabel.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setConstraints(priceLabel, 2, 0);

        // Total price (same as unit price for quantity 1)
        Label totalLabel = new Label(currencyFormat.format(produit.getPrix()));
        totalLabel.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setConstraints(totalLabel, 3, 0);

        row.getChildren().addAll(nameLabel, qtyLabel, priceLabel, totalLabel);
        return row;
    }

    /**
     * Export the invoice to PDF
     */
    private void exportToPdf() {
        // Show file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Invoice");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Facture-" + commande.getId() + ".pdf");

        File file = fileChooser.showSaveDialog(contentContainer.getScene().getWindow());
        if (file == null) {
            return; // User canceled the save dialog
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float pageWidth = page.getMediaBox().getWidth();
                float xCenter = pageWidth / 2;
                float tableWidth = pageWidth - 2 * margin;
                float yPosition = yStart;
                float rowHeight = 20;

                // Add gradient-like colored header box
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f); // Blue color
                contentStream.addRect(margin, yStart - 80, pageWidth - 2*margin, 80);
                contentStream.fill();

                // Add company logo (if available)
                try {
                    PDImageXObject logo = PDImageXObject.createFromFile(
                            getClass().getResource("/images/logo.png").getFile(), document);
                    contentStream.drawImage(logo, margin, yStart - 60, 60, 60);
                } catch (Exception e) {
                    System.err.println("Could not load company logo: " + e.getMessage());
                    // Continue without logo
                }

                // Reset to white color for header text
                contentStream.setNonStrokingColor(1f, 1f, 1f);

                // Company Name (in white)
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin + 70, yStart - 20);
                contentStream.showText("Tbibi");
                contentStream.endText();

                // Company Details (in white)
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(margin + 70, yStart - 35);
                contentStream.showText("123 Esprit, Ariana, Tunis");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(margin + 70, yStart - 50);
                contentStream.showText("contact@Tbibi.com | +216 71 123 456");
                contentStream.endText();

                yPosition = yStart - 100;

                // Reset to black for rest of document
                contentStream.setNonStrokingColor(0f, 0f, 0f);

                // Invoice Title (colored)
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f); // Blue color
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(xCenter - 40, yPosition);
                contentStream.showText("FACTURE");
                contentStream.endText();

                yPosition -= rowHeight * 1.5;

                // Invoice Number
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(xCenter - 40, yPosition);
                contentStream.showText("N° " + commande.getId());
                contentStream.endText();

                yPosition -= rowHeight;

                // Date
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(xCenter - 60, yPosition);
                contentStream.showText("Date: " + dateFormat.format(new Date()));
                contentStream.endText();

                yPosition -= rowHeight * 2;

                // Reset color
                contentStream.setNonStrokingColor(0f, 0f, 0f);

                // Separator Line (colored)
                contentStream.setStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();

                yPosition -= rowHeight * 1.5;

                // Client Information
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Information Client");
                contentStream.endText();

                yPosition -= rowHeight * 1.5;

                // Create client info box
                contentStream.setNonStrokingColor(240/255f, 247/255f, 255/255f); // Light blue background
                contentStream.addRect(margin, yPosition - 45, tableWidth, 45);
                contentStream.fill();

                // Add border
                contentStream.setNonStrokingColor(0f, 0f, 0f);
                contentStream.setStrokingColor(208/255f, 225/255f, 249/255f); // Border color
                contentStream.addRect(margin, yPosition - 45, tableWidth, 45);
                contentStream.stroke();

                // Client Name
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + 10, yPosition - 15);
                contentStream.showText("Nom:");
                contentStream.endText();

                contentStream.setNonStrokingColor(52/255f, 73/255f, 94/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(margin + 50, yPosition - 15);
                contentStream.showText(clientNameLabel.getText());
                contentStream.endText();

                // Client Email
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + 10, yPosition - 35);
                contentStream.showText("Email:");
                contentStream.endText();

                contentStream.setNonStrokingColor(52/255f, 73/255f, 94/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(margin + 50, yPosition - 35);
                contentStream.showText(clientEmailLabel.getText());
                contentStream.endText();

                yPosition -= rowHeight * 4;

                // Order Details Title
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Détails de la Commande");
                contentStream.endText();

                yPosition -= rowHeight * 1.5;

                // Product Table Header - with blue background
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.addRect(margin, yPosition - 15, tableWidth, 20);
                contentStream.fill();

                // Add white text for column headers
                contentStream.setNonStrokingColor(1f, 1f, 1f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + 5, yPosition - 10);
                contentStream.showText("Produit");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + tableWidth * 0.5f + 5, yPosition - 10);
                contentStream.showText("Quantité");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + tableWidth * 0.65f + 5, yPosition - 10);
                contentStream.showText("Prix");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + tableWidth * 0.8f + 5, yPosition - 10);
                contentStream.showText("Total");
                contentStream.endText();

                yPosition -= rowHeight;

                // Reset to black for product rows
                contentStream.setNonStrokingColor(0f, 0f, 0f);

                // Product Rows
                boolean alternateRow = false;
                if (commande.getProduits() != null && !commande.getProduits().isEmpty()) {
                    for (Produit produit : commande.getProduits()) {
                        // Add alternating row background
                        if (alternateRow) {
                            contentStream.setNonStrokingColor(248/255f, 250/255f, 253/255f);
                            contentStream.addRect(margin, yPosition - 15, tableWidth, 20);
                            contentStream.fill();
                        }
                        alternateRow = !alternateRow;

                        // Reset to black color for text
                        contentStream.setNonStrokingColor(0f, 0f, 0f);

                        // Draw product row content
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(margin + 5, yPosition - 10);
                        contentStream.showText(produit.getNom());
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(margin + tableWidth * 0.5f + 20, yPosition - 10);
                        contentStream.showText("1");
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(margin + tableWidth * 0.65f + 5, yPosition - 10);
                        contentStream.showText(String.format("%.2f TND", produit.getPrix()));
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(margin + tableWidth * 0.8f + 5, yPosition - 10);
                        contentStream.showText(String.format("%.2f TND", produit.getPrix()));
                        contentStream.endText();

                        yPosition -= rowHeight;
                    }
                } else {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.newLineAtOffset(margin + 5, yPosition - 15);
                    contentStream.showText("Aucun produit disponible");
                    contentStream.endText();
                    yPosition -= rowHeight;
                }

                yPosition -= rowHeight;

                // Add summary box with white background
                contentStream.setNonStrokingColor(1f, 1f, 1f);
                contentStream.addRect(pageWidth - margin - 200, yPosition - 65, 200, 65);
                contentStream.fill();

                // Add border
                contentStream.setStrokingColor(229/255f, 236/255f, 240/255f);
                contentStream.setLineWidth(1f);
                contentStream.addRect(pageWidth - margin - 200, yPosition - 65, 200, 65);
                contentStream.stroke();

                // Summary Section
                double subtotal = 0;
                if (commande.getProduits() != null) {
                    for (Produit produit : commande.getProduits()) {
                        subtotal += produit.getPrix();
                    }
                }

                double taxRate = 0.19;
                double tax = subtotal * taxRate;
                double total = subtotal + tax;

                // Subtotal
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(pageWidth - margin - 160, yPosition - 15);
                contentStream.showText("Sous-total:");
                contentStream.endText();

                contentStream.setNonStrokingColor(52/255f, 73/255f, 94/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(pageWidth - margin - 60, yPosition - 15);
                contentStream.showText(String.format("%.2f TND", subtotal));
                contentStream.endText();

                yPosition -= rowHeight;

                // Tax
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(pageWidth - margin - 160, yPosition - 10);
                contentStream.showText("TVA (19%):");
                contentStream.endText();

                contentStream.setNonStrokingColor(52/255f, 73/255f, 94/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(pageWidth - margin - 60, yPosition - 10);
                contentStream.showText(String.format("%.2f TND", tax));
                contentStream.endText();

                // Add separator line
                contentStream.setStrokingColor(229/255f, 236/255f, 240/255f);
                contentStream.moveTo(pageWidth - margin - 180, yPosition - 20);
                contentStream.lineTo(pageWidth - margin - 20, yPosition - 20);
                contentStream.stroke();

                yPosition -= rowHeight;

                // Total
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(pageWidth - margin - 160, yPosition - 15);
                contentStream.showText("TOTAL:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(pageWidth - margin - 60, yPosition - 15);
                contentStream.showText(String.format("%.2f TND", total));
                contentStream.endText();

                yPosition -= rowHeight;

                // Status
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(pageWidth - margin - 160, yPosition - 10);
                contentStream.showText("Statut:");
                contentStream.endText();

                contentStream.beginText();
                if ("Paid".equalsIgnoreCase(commande.getStatut()) || "Payé".equalsIgnoreCase(commande.getStatut())) {
                    contentStream.setNonStrokingColor(40/255f, 167/255f, 69/255f); // Green color
                } else {
                    contentStream.setNonStrokingColor(255/255f, 193/255f, 7/255f); // Yellow/amber color
                }
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(pageWidth - margin - 60, yPosition - 10);
                contentStream.showText(commande.getStatut());
                contentStream.endText();

                yPosition -= rowHeight * 3;

                // Notes section with light background
                contentStream.setNonStrokingColor(240/255f, 240/255f, 240/255f);
                contentStream.addRect(margin, yPosition - 40, tableWidth, 40);
                contentStream.fill();

                contentStream.setStrokingColor(229/255f, 236/255f, 240/255f);
                contentStream.setLineWidth(1f);
                contentStream.addRect(margin, yPosition - 40, tableWidth, 40);
                contentStream.stroke();

                // Reset to blue for notes title
                contentStream.setNonStrokingColor(58/255f, 123/255f, 213/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(margin + 10, yPosition - 15);
                contentStream.showText("Notes:");
                contentStream.endText();

                // Set gray color for notes text
                contentStream.setNonStrokingColor(127/255f, 140/255f, 141/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(margin + 10, yPosition - 30);
                contentStream.showText("Merci pour votre achat! Pour toute assistance, veuillez contacter notre service client.");
                contentStream.endText();

                // Footer
                float footerY = 50;
                contentStream.setNonStrokingColor(127/255f, 140/255f, 141/255f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xCenter - 100, footerY);
                contentStream.showText("Facture générée automatiquement - Page 1/1");
                contentStream.endText();
            }

            document.save(file);
            showAlert("PDF Exporté", "La facture a été exportée avec succès en PDF.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'exporter le PDF: " + e.getMessage());
        }
    }

// No need for the drawTableHeader and drawProductRow methods since we're now
// drawing them directly in the exportToPdf method with styling

    /**
     * Draw the product table header in PDF
     */
    private void drawTableHeader(PDPageContentStream contentStream, float x, float y, float width) throws IOException {
        // Background
        contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f); // Light gray
        contentStream.addRect(x, y - 15, width, 20);
        contentStream.fill();

        // Reset color to black
        contentStream.setNonStrokingColor(0, 0, 0);

        // Column headers
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + 5, y - 10);
        contentStream.showText("Produit");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + width * 0.5f + 5, y - 10);
        contentStream.showText("Quantité");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + width * 0.65f + 5, y - 10);
        contentStream.showText("Prix");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + width * 0.8f + 5, y - 10);
        contentStream.showText("Total");
        contentStream.endText();
    }

    /**
     * Draw a product row in PDF
     */
    private void drawProductRow(PDPageContentStream contentStream, float x, float y,
                                float width, Produit produit) throws IOException {
        // Product name
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + 5, y - 10);
        contentStream.showText(produit.getNom());
        contentStream.endText();

        // Quantity (default to 1)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + width * 0.5f + 20, y - 10);
        contentStream.showText("1");
        contentStream.endText();

        // Price
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + width * 0.65f + 5, y - 10);
        contentStream.showText(String.format("%.2f TND", produit.getPrix()));
        contentStream.endText();

        // Total
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + width * 0.8f + 5, y - 10);
        contentStream.showText(String.format("%.2f TND", produit.getPrix()));
        contentStream.endText();
    }

    /**
     * Print the invoice
     */
    private void printFacture() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean printDialog = job.showPrintDialog(null);

            if (printDialog) {
                // Get the node to print (entire scene)
                Node nodeToPrint = contentContainer.getScene().getRoot();

                boolean success = job.printPage(nodeToPrint);
                if (success) {
                    job.endJob();
                    showAlert("Impression", "Facture envoyée à l'imprimante.");
                } else {
                    showAlert("Erreur", "L'impression a échoué.");
                }
            }
        } else {
            showAlert("Erreur", "Impossible de créer une tâche d'impression.");
        }
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}