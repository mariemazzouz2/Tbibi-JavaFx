package tests.Patient;

import entities.Ordonnance;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.io.IOException;
import javafx.stage.Window;
import javafx.application.Platform;

public class PatientOrdonnanceDetailsController implements Initializable {

    @FXML private Label labelId;
    @FXML private Label labelDate;
    @FXML private Label labelMedecin;
    @FXML private TextArea textDescription;
    @FXML private ImageView imgSignature;
    @FXML private Button btnDownloadPdf;
    @FXML private Button btnClose;
    
    private Ordonnance ordonnance;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Nothing specific to initialize here
    }
    
    public void setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        
        if (ordonnance != null) {
            updateUI();
        }
    }
    
    private void updateUI() {
        labelId.setText(ordonnance.getId().toString());
        
        // Format date from consultation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        labelDate.setText(ordonnance.getConsultation().getDateC().format(formatter));
        
        // Set medecin info from consultation
        labelMedecin.setText(
            ordonnance.getConsultation().getMedecin().getNom() + " " + 
            ordonnance.getConsultation().getMedecin().getPrenom()
        );
        
        // Set prescription description
        textDescription.setText(ordonnance.getDescription());
        textDescription.setEditable(false);
        
        // Set signature image if available
        if (ordonnance.getSignature() != null && !ordonnance.getSignature().isEmpty()) {
            try {
                Image signatureImage = new Image(ordonnance.getSignature());
                imgSignature.setImage(signatureImage);
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de la signature: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDownloadPdf() {
        try {
            // Create a file chooser
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer l'ordonnance en PDF");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("Ordonnance" + ordonnance.getId() + ".pdf");

            // Show save dialog
            java.io.File file = fileChooser.showSaveDialog(btnDownloadPdf.getScene().getWindow());

            if (file != null) {
                // Generate PDF with PDFBox
                org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();

                // Use A4 page size
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                        org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                document.addPage(page);

                float margin = 50;
                float width = page.getMediaBox().getWidth() - 2 * margin;

                // Drawing content
                org.apache.pdfbox.pdmodel.PDPageContentStream contentStream =
                        new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);

                // Add a light blue background header
                contentStream.setNonStrokingColor(235, 246, 255); // Light blue
                contentStream.addRect(margin, 770, width, 60);
                contentStream.fill();

                // Reset color to black for text
                contentStream.setNonStrokingColor(0, 0, 0);

                // Add a logo or medical symbol (here we'll create a simple caduceus-like symbol)
                drawMedicalSymbol(contentStream, margin + 25, 800, 30);

                // Title with better positioning and font
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 22);
                contentStream.setNonStrokingColor(0, 102, 204); // Dark blue
                contentStream.newLineAtOffset(margin + 80, 800);
                contentStream.showText("ORDONNANCE MÉDICALE");
                contentStream.endText();

                // Reset color to black
                contentStream.setNonStrokingColor(0, 0, 0);

                // Draw a decorative line
                contentStream.setLineWidth(2f);
                contentStream.setStrokingColor(0, 102, 204); // Dark blue
                contentStream.moveTo(margin, 760);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, 760);
                contentStream.stroke();

                // Doctor info box
                contentStream.setNonStrokingColor(245, 245, 245); // Light gray
                contentStream.addRect(margin, 680, width / 2 - 10, 60);
                contentStream.fill();

                contentStream.setNonStrokingColor(0, 0, 0); // Back to black
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin + 10, 725);
                contentStream.showText("MÉDECIN");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin + 10, 710);
                contentStream.showText("Dr. " + ordonnance.getConsultation().getMedecin().getNom() +
                        " " + ordonnance.getConsultation().getMedecin().getPrenom());
                contentStream.endText();

                // Patient info box
                contentStream.setNonStrokingColor(245, 245, 245); // Light gray
                contentStream.addRect(margin + width / 2 + 10, 680, width / 2 - 10, 60);
                contentStream.fill();

                contentStream.setNonStrokingColor(0, 0, 0); // Back to black
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin + width / 2 + 20, 725);
                contentStream.showText("PATIENT");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin + width / 2 + 20, 710);
                contentStream.showText(ordonnance.getConsultation().getPatient().getNom() +
                        " " + ordonnance.getConsultation().getPatient().getPrenom());
                contentStream.endText();

                // Date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, 650);
                contentStream.showText("Date de consultation: ");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 150, 650);
                contentStream.showText(ordonnance.getConsultation().getDateC().format(formatter));
                contentStream.endText();

                // Ordonnance ID
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - margin - 150, 650);
                contentStream.showText("Ordonnance N°: ");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - margin - 60, 650);
                contentStream.showText(ordonnance.getId().toString());
                contentStream.endText();

                // Draw a line before prescription
                contentStream.setLineWidth(1f);
                contentStream.setStrokingColor(0, 102, 204); // Dark blue
                contentStream.moveTo(margin, 630);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, 630);
                contentStream.stroke();

                // Prescription title with improved style
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 14);
                contentStream.setNonStrokingColor(0, 102, 204); // Dark blue
                contentStream.newLineAtOffset(margin, 610);
                contentStream.showText("PRESCRIPTION:");
                contentStream.endText();

                // Reset color to black
                contentStream.setNonStrokingColor(0, 0, 0);

                // Prescription text - handle multi-line with better formatting
                String prescriptionText = ordonnance.getDescription();
                String[] lines = prescriptionText.split("\n");
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                float y = 580;

                // Add light gray background for prescription area
                contentStream.setNonStrokingColor(250, 250, 250); // Very light gray
                contentStream.addRect(margin - 10, y - lines.length * 20 - 20, width + 20, lines.length * 20 + 40);
                contentStream.fill();

                contentStream.setNonStrokingColor(0, 0, 0); // Back to black

                for (String line : lines) {
                    // Further split if line is too long
                    if (line.length() > 80) {
                        int startIndex = 0;
                        while (startIndex < line.length()) {
                            int endIndex = Math.min(startIndex + 80, line.length());
                            String subLine = line.substring(startIndex, endIndex);

                            contentStream.beginText();
                            contentStream.newLineAtOffset(margin, y);
                            contentStream.showText(subLine);
                            contentStream.endText();

                            y -= 20;
                            startIndex = endIndex;
                        }
                    } else {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, y);
                        contentStream.showText(line);
                        contentStream.endText();
                        y -= 20;
                    }
                }

                // Signature area
                float signatureY = Math.max(y - 100, 200);

                // Signature box
                contentStream.setNonStrokingColor(245, 245, 245); // Light gray
                contentStream.addRect(page.getMediaBox().getWidth() - margin - 200, signatureY - 50, 180, 80);
                contentStream.fill();

                contentStream.setLineWidth(0.5f);
                contentStream.setStrokingColor(180, 180, 180);
                contentStream.moveTo(page.getMediaBox().getWidth() - margin - 180, signatureY);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin - 40, signatureY);
                contentStream.stroke();

                contentStream.setNonStrokingColor(0, 0, 0); // Back to black
                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - margin - 150, signatureY + 20);
                contentStream.showText("Signature du médecin");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - margin - 150, signatureY - 30);
                contentStream.showText("Dr. " + ordonnance.getConsultation().getMedecin().getNom());
                contentStream.endText();

                // Footer with gradient line
                float footerY = 50;
                drawGradientLine(contentStream, margin, footerY + 20, page.getMediaBox().getWidth() - margin, footerY + 20);

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 9);
                contentStream.setNonStrokingColor(100, 100, 100); // Dark gray
                contentStream.newLineAtOffset(margin, footerY);
                contentStream.showText("Ordonnance générée par l'application TBibi • ID: " + ordonnance.getId());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 9);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 250, footerY);
                contentStream.showText("Date d'impression: " +
                        java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();

                // Close content stream
                contentStream.close();

                // Save PDF
                document.save(file);
                document.close();

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "PDF généré avec succès",
                        "L'ordonnance a été enregistrée en PDF avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la génération du PDF",
                    "Une erreur est survenue: " + e.getMessage());
        }
    }

    // Helper method to draw a simple medical symbol
    private void drawMedicalSymbol(org.apache.pdfbox.pdmodel.PDPageContentStream contentStream,
                                   float x, float y, float size) throws IOException {
        contentStream.setLineWidth(2.0f);
        contentStream.setStrokingColor(0, 102, 204); // Dark blue

        // Draw staff
        contentStream.moveTo(x + size/2, y - size);
        contentStream.lineTo(x + size/2, y);

        // Draw snake
        float radius = size/4;
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            float offsetX = (float)(Math.sin(angle) * radius);
            contentStream.lineTo(x + size/2 + offsetX, y - i * size/8);
        }

        contentStream.stroke();
    }

    // Helper method to draw a gradient-like line
    private void drawGradientLine(org.apache.pdfbox.pdmodel.PDPageContentStream contentStream,
                                  float x1, float y, float x2, float y2) throws IOException {
        float stepSize = 2.0f;
        float currentPos = x1;

        while (currentPos < x2) {
            float fraction = (currentPos - x1) / (x2 - x1);
            int blue = (int)(204 * (1 - fraction));
            int red = (int)(fraction * 153);
            int green = (int)(102 * (1 - fraction) + fraction * 204);

            contentStream.setStrokingColor(red, green, blue);
            contentStream.setLineWidth(2.0f);

            float endPos = Math.min(currentPos + stepSize, x2);
            contentStream.moveTo(currentPos, y);
            contentStream.lineTo(endPos, y2);
            contentStream.stroke();

            currentPos = endPos;
        }
    }
    
    @FXML
    private void handleClose() {
        try {
            // Get the current stage from the button and close it
            Stage stage = (Stage) btnClose.getScene().getWindow();
            if (stage != null) {
                stage.close();
            } else {
                // Alternative approach if the first method fails
                Window window = btnClose.getScene().getWindow();
                if (window instanceof Stage) {
                    ((Stage) window).close();
                }
            }
        } catch (Exception e) {
            System.out.println("Error closing window: " + e.getMessage());
            e.printStackTrace();

            // Last resort: try to find the stage using a different approach
            Platform.exit();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 