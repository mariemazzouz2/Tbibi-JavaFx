package tests.Patient;

import entities.Consultation;
import entities.TypeConsultation;
import entities.Utilisateur;
import exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import services.ServiceConsultation;
import services.ServiceUtilisateur;
import utils.GoogleApiUtil;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.time.format.DateTimeFormatter;


public class PatientConsultationFormController implements Initializable {

    @FXML private ComboBox<TypeConsultation> comboType;
    @FXML private ComboBox<Utilisateur> comboMedecin;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboHour;
    @FXML private TextArea textCommentaire;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    private Consultation consultation;

    private ServiceConsultation serviceConsultation;
    private ServiceUtilisateur serviceUtilisateur;
    private Utilisateur currentUser;
    private String mode = "create"; // "create" or "edit"
    private Consultation currentConsultation;
    private Runnable onSaveCallback;
    private static HttpTransport HTTP_TRANSPORT = null;
    private static  JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME;
    private static final String CREDENTIALS_PATH;
    private static final List<String> SCOPES;


    static {
        // Load Google API configuration
        Properties props = new Properties();
        try (InputStream input = PatientConsultationFormController.class.getClassLoader().getResourceAsStream("google_api_config.properties")) {
            props.load(input);
            APPLICATION_NAME = props.getProperty("google.application.name");
            CREDENTIALS_PATH = props.getProperty("google.credentials.path");
            SCOPES = Arrays.asList(props.getProperty("google.calendar.scopes").split(","));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Google API configuration", e);
        }

        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize HTTP transport", e);
        }
    }

    private GoogleCredentials getCredentials() throws Exception {
        try (InputStream credentialsStream = new FileInputStream(CREDENTIALS_PATH)) {
            return GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(SCOPES);
        } catch (IOException e) {
            throw new Exception("Failed to load Google credentials: " + e.getMessage(), e);
        }
    }

    private String createGoogleMeetLink(String title, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(getCredentials()))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Event event = new Event()
                    .setSummary(title)
                    .setDescription("Consultation virtuelle via Google Meet");

            DateTime startDateTime = new DateTime(java.sql.Timestamp.valueOf(startTime));
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Africa/Tunis"); // Set to Tunisia timezone

            DateTime endDateTime = new DateTime(java.sql.Timestamp.valueOf(endTime));
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Africa/Tunis");

            event.setStart(start);
            event.setEnd(end);

            // Add Google Meet conferencing
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(new CreateConferenceRequest()
                            .setRequestId(UUID.randomUUID().toString()));

            event.setConferenceData(conferenceData);

            // Insert the event with conferencing
            Event createdEvent = service.events()
                    .insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute();

            // Return the Google Meet link
            return createdEvent.getHangoutLink();
        } catch (Exception e) {
            e.printStackTrace();
            // Log the error for debugging
            System.err.println("Failed to create Google Meet link: " + e.getMessage());
            // Fallback to a Jitsi link in case of errors
            return "https://meet.jit.si/tbibi-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    @FXML
    private void handleSaveAction(ActionEvent event) {
        // ... existing validation code ...

        try {
            // For virtual consultations, generate a meeting link
            //this.currentConsultation = consultation;

            if (consultation.getType() == TypeConsultation.VIRTUELLE) {
                // Get patient and doctor names
                String patientName = currentUser.getNom() + " " + currentUser.getPrenom();
                String doctorName = comboMedecin.getValue().getNom() + " " + comboMedecin.getValue().getPrenom();
                String title = "Consultation " + comboType.getValue();

                // Calculate end time (1 hour after start)
                java.time.LocalDateTime startTime = consultation.getDateC();
                java.time.LocalDateTime endTime = startTime.plusHours(1);

                // Generate Google Meet link
                String meetLink = GoogleApiUtil.createGoogleMeetLink(
                        title, startTime, endTime);

                consultation.setMeetLink(meetLink);
            }

            // ... existing save code ...

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement", e.getMessage());
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceConsultation = new ServiceConsultation();
            serviceUtilisateur = new ServiceUtilisateur();
            
            // Initialize Type ComboBox
            comboType.setItems(FXCollections.observableArrayList(TypeConsultation.values()));
            comboType.setConverter(new StringConverter<TypeConsultation>() {
                @Override
                public String toString(TypeConsultation object) {
                    return object != null ? object.getDisplayName() : "";
                }

                @Override
                public TypeConsultation fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
            // Initialize hours ComboBox (8:00 to 18:00)
            ObservableList<String> hours = FXCollections.observableArrayList();
            for (int i = 8; i <= 18; i++) {
                hours.add(String.format("%02d:00", i));
                if (i < 18) {
                    hours.add(String.format("%02d:30", i));
                }
            }
            comboHour.setItems(hours);
            loadMedecins();
            // Set today as default date
            datePicker.setValue(LocalDate.now());
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation", e.getMessage());
        }
    }
    
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        loadMedecins();
    }
    
    public void setMode(String mode) {
        this.mode = mode;
        updateUI();
    }
    
    public void setConsultation(Consultation consultation) {
        this.currentConsultation = consultation;
        
        if (consultation != null) {
            comboType.setValue(consultation.getType());
            comboMedecin.setValue(consultation.getMedecin());
            datePicker.setValue(consultation.getDateC().toLocalDate());
            
            // Format time to match ComboBox format
            int hour = consultation.getDateC().getHour();
            int minute = consultation.getDateC().getMinute();
            String timeStr = String.format("%02d:%02d", hour, minute);
            comboHour.setValue(timeStr);
            
            textCommentaire.setText(consultation.getCommentaire());
            
            updateUI();
        }
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    private void loadMedecins() {
        try {
            // Load doctors (users with role ROLE_MEDECIN)
            List<Utilisateur> medecins = serviceUtilisateur.afficher();
            System.out.println("setting combomedecin");
            comboMedecin.setItems(FXCollections.observableArrayList(medecins));
            System.out.println(comboMedecin.getValue());

            // Set display for doctors (Full name)
            comboMedecin.setConverter(new StringConverter<Utilisateur>() {
                @Override
                public String toString(Utilisateur user) {
                    return user != null ? user.getNom() + " " + user.getPrenom() : "";
                }

                @Override
                public Utilisateur fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des médecins", e.getMessage());
        }
    }
    
    private void updateUI() {
        if ("edit".equals(mode)) {
            btnSave.setText("Modifier");
        } else {
            btnSave.setText("Planifier");
        }
    }
    
    @FXML
    private void handleSave() {
        try {
          
            if (comboType.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner un type de consultation");
            }
            if (comboMedecin.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner un médecin");
            }
            if (datePicker.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner une date");
            }
            if (comboHour.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner une heure");
            }
            
            // Check if the date is not in the past
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate.isBefore(LocalDate.now())) {
                throw new ValidationException("Vous ne pouvez pas planifier une consultation dans le passé");
            }
            
            // Parse time from ComboBox
            String[] timeParts = comboHour.getValue().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Create LocalDateTime for the consultation
            LocalDateTime dateTime = LocalDateTime.of(
                selectedDate,
                LocalTime.of(hour, minute)
            );
            
            // Check if the date/time is already past for today
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Vous ne pouvez pas planifier une consultation à une heure déjà passée");
            }
            
            if ("create".equals(mode)) {
                // Create new consultation
                Consultation consultation = new Consultation();
                consultation.setType(comboType.getValue());
                consultation.setStatus(Consultation.STATUS_PENDING);
                consultation.setCommentaire(textCommentaire.getText());
                consultation.setDateC(dateTime);
                consultation.setMedecin(comboMedecin.getValue());
                consultation.setPatient(this.currentUser);

                // For virtual consultations, generate a Google Meet link
                if (consultation.getType() == TypeConsultation.VIRTUELLE) {
                    String title = String.format("Consultation %s - Dr. %s avec %s",
                        comboType.getValue(),
                        consultation.getMedecin().getNom(),
                        consultation.getPatient().getNom()
                    );
                    
                    LocalDateTime endTime = dateTime.plusHours(1); // 1-hour consultation
                    String meetLink = createGoogleMeetLink(title, dateTime, endTime);
                    consultation.setMeetLink(meetLink);
                }
                
                // Save to database
                serviceConsultation.ajouter(consultation);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Consultation planifiée", 
                    "Votre demande de consultation a été soumise avec succès." + 
                    (consultation.getType() == TypeConsultation.VIRTUELLE ? 
                    "\nLien de consultation: " + consultation.getMeetLink() : ""));
                
            } else {
                // Update existing consultation
                currentConsultation.setType(comboType.getValue());
                currentConsultation.setCommentaire(textCommentaire.getText());
                currentConsultation.setDateC(dateTime);
                currentConsultation.setMedecin(comboMedecin.getValue());
                
                // Update meet link if type changes to virtual
                if (currentConsultation.getType() == TypeConsultation.VIRTUELLE &&
                        (currentConsultation.getMeetLink() == null || currentConsultation.getMeetLink().isEmpty())) {
                    String title = String.format("Consultation %s - Dr. %s avec %s",
                        comboType.getValue(),
                        currentConsultation.getMedecin().getNom(),
                        currentConsultation.getPatient().getNom()
                    );
                    
                    LocalDateTime endTime = dateTime.plusHours(1); // 1-hour consultation
                    String meetLink = createGoogleMeetLink(title, dateTime, endTime);
                    currentConsultation.setMeetLink(meetLink);
                }
                
                // Save changes
                serviceConsultation.modifier(currentConsultation);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Consultation modifiée", 
                    "Votre consultation a été modifiée avec succès.");
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
    private String generateMeetCode() {
        // Generate a unique, readable code (3 groups of 3 letters)
        String chars = "abcdefghijkmnopqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int group = 0; group < 3; group++) {
            if (group > 0) code.append("-");
            for (int i = 0; i < 3; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        }

        return code.toString();
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