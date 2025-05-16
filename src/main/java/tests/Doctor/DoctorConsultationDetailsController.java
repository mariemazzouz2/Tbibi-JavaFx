package tests.Doctor;

import entities.Consultation;
import entities.Ordonnance;
import entities.Utilisateur;
import exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceConsultation;
import utils.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

// Define our own callback interface
interface UpdateCallback {
    void run();
}

public class DoctorConsultationDetailsController implements Initializable {

    @FXML private Label consultationIdLabel;
    @FXML private Label typeLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Hyperlink patientNameLink;
    @FXML private Label patientEmailLabel;
    @FXML private Label patientPhoneLabel;
    @FXML private TextArea commentsArea;
    @FXML private Button acceptButton;
    @FXML private Button rejectButton;
    @FXML private Button completeButton;
    @FXML private Button prescriptionButton;
    @FXML private Button closeButton;
    @FXML private Label lblMeetLink;
    @FXML private Hyperlink hyperlinkMeet;

    private Consultation consultation;
    private ServiceConsultation serviceConsultation;
    private DoctorConsultationController parentController;
    private Stage stage;
    private UpdateCallback onUpdateCallback;
    private Runnable onActionCallback;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            serviceConsultation = new ServiceConsultation();

            // Disable buttons initially
            acceptButton.setDisable(true);
            rejectButton.setDisable(true);
            completeButton.setDisable(true);
            prescriptionButton.setDisable(true);

            // Initialize meet link components as hidden
            lblMeetLink.setVisible(false);
            hyperlinkMeet.setVisible(false);

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur d'initialisation", e.getMessage());
        }
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;

        try {
            // Get fresh data from database to ensure we have the latest
            this.consultation = serviceConsultation.getById(consultation.getId());

            if (this.consultation != null) {
                updateUI();
                updateButtonsVisibility();

                // Handle meet link for virtual consultations
                if (this.consultation.getMeetLink() != null && !this.consultation.getMeetLink().isEmpty()) {
                    lblMeetLink.setVisible(true);
                    hyperlinkMeet.setVisible(true);
                    hyperlinkMeet.setText(this.consultation.getMeetLink());
                    hyperlinkMeet.setOnAction(e -> {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(this.consultation.getMeetLink()));
                        } catch (Exception ex) {
                            AlertUtils.showError("Erreur", "Impossible d'ouvrir le lien", ex.getMessage());
                        }
                    });
                } else {
                    lblMeetLink.setVisible(false);
                    hyperlinkMeet.setVisible(false);
                }
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors du chargement des détails", e.getMessage());
        }
    }

    public void setParentController(DoctorConsultationController parentController) {
        this.parentController = parentController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void updateUI() {
        if (consultation == null) return;

        consultationIdLabel.setText(String.valueOf(consultation.getId()));
        typeLabel.setText(consultation.getType().toString());
        statusLabel.setText(consultation.getStatus());
        dateLabel.setText(consultation.getDateC().format(DATE_FORMATTER));
        String patientName = consultation.getPatient().getNom() + " " + consultation.getPatient().getPrenom();
        patientNameLink.setText(patientName);
        patientEmailLabel.setText(consultation.getPatient().getEmail());
        patientPhoneLabel.setText(String.valueOf(consultation.getPatient().getTelephone()));

        commentsArea.setText(consultation.getCommentaire());
        commentsArea.setEditable(false);

        // Check if there's an ordonnance
        Ordonnance ordonnance = consultation.getOrdonnance();
        prescriptionButton.setDisable(ordonnance != null);
    }

    private void updateButtonsVisibility() {
        String status = consultation.getStatus();

        // Accept/Reject buttons only visible for pending consultations
        boolean isPending = "pending".equals(status);
        acceptButton.setDisable(!isPending);
        rejectButton.setDisable(!isPending);

        // Complete button only visible for confirmed consultations
        completeButton.setDisable(!"confirmed".equals(status));

        // Prescription button only visible for confirmed or completed consultations
        boolean canCreateOrdonnance = ("confirmed".equals(status) || "completed".equals(status)) && consultation.getOrdonnance() == null;
        prescriptionButton.setDisable(!canCreateOrdonnance);

        // Set button visibility
        acceptButton.setVisible(isPending);
        rejectButton.setVisible(isPending);
        completeButton.setVisible("confirmed".equals(status));
        prescriptionButton.setVisible("confirmed".equals(status) || "completed".equals(status));
    }

    @FXML
    private void handleAcceptAction() {
        try {
            consultation.setStatus("confirmed");
            serviceConsultation.modifier(consultation);

            if (onActionCallback != null) {
                onActionCallback.run();
            }
            updateUI();
            updateButtonsVisibility();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la mise à jour du statut", e.getMessage());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleRejectAction() {
        try {
            consultation.setStatus("cancelled");
            serviceConsultation.modifier(consultation);

            if (onActionCallback != null) {
                onActionCallback.run();
            }
            updateUI();
            updateButtonsVisibility();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la mise à jour du statut", e.getMessage());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleCompleteAction() {
        try {
            consultation.setStatus("completed");
            serviceConsultation.modifier(consultation);

            if (onActionCallback != null) {
                onActionCallback.run();
            }
            updateUI();
            updateButtonsVisibility();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la mise à jour du statut", e.getMessage());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handlePrescriptionAction() {
        if ("confirmed".equals(consultation.getStatus()) || "completed".equals(consultation.getStatus())) {
            try {
                // Initialize the FXMLLoader with the correct FXML resource path
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/ordonnance_form.fxml"));
                Parent root = loader.load();

                DoctorOrdonnanceFormController controller = loader.getController();
                controller.setConsultation(consultation);
                controller.setOnSaveCallback(() -> {
                    if (onUpdateCallback != null) {
                        onUpdateCallback.run();
                    }
                    refreshConsultationDetails();
                });

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Ordonnance");
                stage.setScene(new Scene(root));
                stage.showAndWait();

            } catch (IOException e) {
                AlertUtils.showError("Erreur", "Erreur lors de l'ouverture du formulaire d'ordonnance", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCloseAction() {
        stage.close();
    }

    public void refreshConsultationDetails() {
        try {
            Consultation refreshedConsultation = serviceConsultation.getById(consultation.getId());
            setConsultation(refreshedConsultation);
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors de l'actualisation des détails de la consultation", e.getMessage());
        }
    }

    public void setOnActionCallback(Runnable callback) {
        this.onActionCallback = callback;
    }

    public void setOnUpdateCallback(UpdateCallback callback) {
        this.onUpdateCallback = callback;
    }
}