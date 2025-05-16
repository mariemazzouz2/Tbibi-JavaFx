package tests.Patient;

import entities.Consultation;
import entities.Ordonnance;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceConsultation;
import services.ServiceOrdonnance;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PatientConsultationDetailsController implements Initializable {

    @FXML private Label labelId;
    @FXML private Label labelType;
    @FXML private Label labelStatus;
    @FXML private Label labelDate;
    @FXML private Label labelMedecin;
    @FXML private TextArea textCommentaire;
    @FXML private Hyperlink linkMeet;
    @FXML private Label labelOrdonnanceStatus;
    @FXML private Button btnViewOrdonnance;
    @FXML private Button btnModify;
    @FXML private Button btnClose;
    
    private Consultation consultation;
    private ServiceConsultation serviceConsultation;
    private ServiceOrdonnance serviceOrdonnance;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceConsultation = new ServiceConsultation();
            serviceOrdonnance = new ServiceOrdonnance();
            
            // Disable modification for non-pending consultations
            btnModify.setDisable(true);
            
            // Disable ordonnance button initially
            btnViewOrdonnance.setDisable(true);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation", e.getMessage());
        }
    }
    
    public void setConsultation(Consultation consultation) {
        try {
            // Get fresh data from database to ensure we have the latest
            this.consultation = serviceConsultation.getById(consultation.getId());
            
            if (this.consultation != null) {
                updateUI();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des détails", e.getMessage());
        }
    }
    
    private void updateUI() {
        // Set all fields
        labelId.setText(consultation.getId().toString());
        labelType.setText(consultation.getType());
        labelStatus.setText(consultation.getStatus());
        
        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        labelDate.setText(consultation.getDateC().format(formatter));
        
        // Set doctor info
        labelMedecin.setText(consultation.getMedecin().getNom() + " " + consultation.getMedecin().getPrenom());
        
        // Set comment
        textCommentaire.setText(consultation.getCommentaire());
        textCommentaire.setEditable(false);
        
        // Handle meet link for virtual consultations
        if (
            consultation.getMeetLink() != null && 
            !consultation.getMeetLink().isEmpty()) {
            
            linkMeet.setText(consultation.getMeetLink());
            linkMeet.setVisible(true);
        } else {
            linkMeet.setVisible(false);
        }
        
        // Enable modification button only for pending consultations
        btnModify.setDisable(!Consultation.STATUS_PENDING.equals(consultation.getStatus()));
        
        // Check if there's an ordonnance
        Ordonnance ordonnance = consultation.getOrdonnance();
        if (ordonnance != null) {
            labelOrdonnanceStatus.setText("Ordonnance disponible");
            btnViewOrdonnance.setDisable(false);
        } else {
            labelOrdonnanceStatus.setText("Pas d'ordonnance");
            btnViewOrdonnance.setDisable(true);
        }
    }
    
    @FXML
    private void handleMeetLinkAction() {
        if (consultation.getMeetLink() != null && !consultation.getMeetLink().isEmpty()) {
            try {
                String meetLink = consultation.getMeetLink();
                System.out.println("Opening meet link: " + meetLink);
                
                // Check if the link is a valid URL
                java.net.URL url = new java.net.URL(meetLink);
                
                // Try to open in default browser
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    System.out.println("Opening with Desktop.browse()");
                    java.awt.Desktop.getDesktop().browse(url.toURI());
                } else {
                    // Fallback for systems where Desktop.browse() is not supported
                    System.out.println("Desktop.browse() not supported, trying Runtime.exec()");
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + meetLink);
                    } else if (os.contains("mac")) {
                        Runtime.getRuntime().exec("open " + meetLink);
                    } else if (os.contains("nix") || os.contains("nux")) {
                        Runtime.getRuntime().exec("xdg-open " + meetLink);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible d'ouvrir le lien", 
                    "Erreur: " + e.getMessage() + "\nLien: " + consultation.getMeetLink());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", 
                "Lien non disponible", 
                "Aucun lien de réunion n'est disponible pour cette consultation.");
        }
    }
    
    @FXML
    private void handleViewOrdonnance() {
        if (consultation.getOrdonnance() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/ordonnance_details.fxml"));
                Parent root = loader.load();
                
                PatientOrdonnanceDetailsController controller = loader.getController();
                controller.setOrdonnance(consultation.getOrdonnance());
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Ordonnance");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de l'ordonnance", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleModifyAction() {
        if (Consultation.STATUS_PENDING.equals(consultation.getStatus())) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_form.fxml"));
                Parent root = loader.load();
                
                PatientConsultationFormController controller = loader.getController();
                controller.setCurrentUser(consultation.getPatient());
                controller.setMode("edit");
                controller.setConsultation(consultation);
                
                // When save completes, refresh this view
                controller.setOnSaveCallback(() -> {
                    try {
                        consultation = serviceConsultation.getById(consultation.getId());
                        updateUI();
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'actualisation", e.getMessage());
                    }
                });
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Modifier la consultation");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClose() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 