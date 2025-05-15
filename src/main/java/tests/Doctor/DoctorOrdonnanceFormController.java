package tests.Doctor;

import entities.Consultation;
import entities.Ordonnance;
import exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ServiceOrdonnance;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DoctorOrdonnanceFormController implements Initializable {

    @FXML private Label labelPatient;
    @FXML private Label labelDate;
    @FXML private TextArea textDescription;
    @FXML private TextField textSignature;
    @FXML private Button btnBrowseImage;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private Consultation consultation;
    private Ordonnance ordonnance;
    private ServiceOrdonnance serviceOrdonnance;
    private String mode = "create"; // "create" or "edit"
    private Runnable onSaveCallback;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceOrdonnance = new ServiceOrdonnance();
            
            // Set a placeholder for the description textarea
            textDescription.setPromptText("Saisissez ici les médicaments prescrits, la posologie, et les recommandations...");
            
            // Default signature to doctor name
            textSignature.setPromptText("Entrez le texte de signature ou sélectionnez une image");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation", e.getMessage());
        }
    }
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        
        if (consultation != null) {
            // Set patient name and date
            labelPatient.setText(consultation.getPatient().getNom() + " " + consultation.getPatient().getPrenom());
            labelDate.setText(consultation.getDateC().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Check if ordonnance already exists
            try {
                ordonnance = serviceOrdonnance.getByConsultationId(consultation.getId());
                if (ordonnance != null) {
                    mode = "edit";
                    loadOrdonnance();
                } else {
                    mode = "create";
                    // Set default signature to doctor name
                    textSignature.setText("Dr. " + consultation.getMedecin().getNom());
                }
                
                updateUI();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'ordonnance", e.getMessage());
            }
        }
    }
    
    private void loadOrdonnance() {
        if (ordonnance != null) {
            textDescription.setText(ordonnance.getDescription());
            textSignature.setText(ordonnance.getSignature());
        }
    }
    
    private void updateUI() {
        if ("edit".equals(mode)) {
            btnSave.setText("Mettre à jour");
        } else {
            btnSave.setText("Créer");
        }
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de signature");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(btnBrowseImage.getScene().getWindow());
        if (selectedFile != null) {
            textSignature.setText(selectedFile.toURI().toString());
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            // Validate input
            if (textDescription.getText().trim().isEmpty()) {
                throw new ValidationException("La description de l'ordonnance est requise");
            }
            if (textSignature.getText().trim().isEmpty()) {
                throw new ValidationException("La signature est requise");
            }
            
            if ("create".equals(mode)) {
                // Create new ordonnance
                Ordonnance newOrdonnance = new Ordonnance();
                newOrdonnance.setDescription(textDescription.getText().trim());
                newOrdonnance.setSignature(textSignature.getText().trim());
                newOrdonnance.setConsultation(consultation);
                
                // Save to database
                serviceOrdonnance.ajouter(newOrdonnance);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Ordonnance créée", 
                    "L'ordonnance a été créée avec succès.");
                
            } else {
                // Update existing ordonnance
                ordonnance.setDescription(textDescription.getText().trim());
                ordonnance.setSignature(textSignature.getText().trim());
                
                // Save changes
                serviceOrdonnance.modifier(ordonnance);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Ordonnance mise à jour", 
                    "L'ordonnance a été mise à jour avec succès.");
            }
            
            // Call the callback if provided
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            // Close the form
            ((Stage) btnSave.getScene().getWindow()).close();
            
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage(), null);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 