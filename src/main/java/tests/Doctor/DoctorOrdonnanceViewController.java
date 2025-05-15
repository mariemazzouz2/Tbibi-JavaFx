package tests.Doctor;

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

public class DoctorOrdonnanceViewController implements Initializable {

    @FXML private Label labelId;
    @FXML private Label labelDate;
    @FXML private Label labelPatient;
    @FXML private TextArea textDescription;
    @FXML private ImageView imgSignature;
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
        
        // Set patient info from consultation
        labelPatient.setText(
            ordonnance.getConsultation().getPatient().getNom() + " " + 
            ordonnance.getConsultation().getPatient().getPrenom()
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
    private void handleClose() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }
} 