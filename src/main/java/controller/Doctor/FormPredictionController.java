package controller.Doctor;

import entities.DossierMedical;
import entities.Prediction;
import entities.Utilisateur;
import services.EmailService;
import services.ServiceDossierMedical;
import services.ServicePrediction;
import utils.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class FormPredictionController {
    @FXML private Label titleLabel;
    @FXML private Label dossierIdLabel;
    @FXML private CheckBox hypertensionCheckBox;
    @FXML private CheckBox heartDiseaseCheckBox;
    @FXML private ChoiceBox<String> smokingHistoryChoiceBox;
    @FXML private TextField bmiField;
    @FXML private TextField hbA1cLevelField;
    @FXML private TextField bloodGlucoseLevelField;

    private PredictionListDoctorController listController; // Reference to the list controller
    private Prediction prediction;
    private Integer dossierId;
    private String doctorEmail; // Ajouté pour stocker l'email du docteur
    private final ApiClient apiClient = new ApiClient();
    private final ServiceDossierMedical serviceDossierMedical;
    private final EmailService emailService;
    private final ServicePrediction servicePrediction; // Ajouté pour réutilisation

    public FormPredictionController() throws SQLException {
        this.serviceDossierMedical = new ServiceDossierMedical();
        this.emailService = new EmailService();
        this.servicePrediction = new ServicePrediction(); // Initialisation
    }

    // Méthode pour définir l'email du docteur (à appeler lors de l'initialisation)
    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    @FXML
    public void initialize() {
        smokingHistoryChoiceBox.getItems().addAll("No Info", "current", "ever", "former", "never", "not current");
        smokingHistoryChoiceBox.setValue("No Info");
    }

    public void setDossierId(Integer dossierId) {
        this.dossierId = dossierId;
        dossierIdLabel.setText("Dossier ID: " + dossierId);
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
        if (prediction != null) {
            titleLabel.setText("Modifier une Prédiction");
            dossierIdLabel.setText("Dossier ID: " + prediction.getDossierId());
            hypertensionCheckBox.setSelected(prediction.isHypertension());
            heartDiseaseCheckBox.setSelected(prediction.isheart_disease());
            smokingHistoryChoiceBox.setValue(prediction.getsmoking_history() != null ? prediction.getsmoking_history() : "No Info");
            bmiField.setText(String.valueOf(prediction.getBmi()));
            hbA1cLevelField.setText(String.valueOf(prediction.gethbA1c_level()));
            bloodGlucoseLevelField.setText(String.valueOf(prediction.getBloodGlucoseLevel()));
        }
    }

    @FXML
    private void enregistrerPrediction() {
        try {
            // Validate numeric fields
            float bmi = Float.parseFloat(bmiField.getText());
            float hbA1cLevel = Float.parseFloat(hbA1cLevelField.getText());
            float bloodGlucoseLevel = Float.parseFloat(bloodGlucoseLevelField.getText());

            // Get the smoking history value
            String smokingHistory = smokingHistoryChoiceBox.getValue();

            // Create or update the prediction
            Prediction tempPrediction;
            if (prediction == null) {
                tempPrediction = new Prediction(
                        dossierId,
                        hypertensionCheckBox.isSelected(),
                        heartDiseaseCheckBox.isSelected(),
                        smokingHistory,
                        bmi,
                        hbA1cLevel,
                        bloodGlucoseLevel
                );
            } else {
                tempPrediction = prediction;
                tempPrediction.setHypertension(hypertensionCheckBox.isSelected());
                tempPrediction.setheart_disease(heartDiseaseCheckBox.isSelected());
                tempPrediction.setsmoking_history(smokingHistory);
                tempPrediction.setBmi(bmi);
                tempPrediction.sethbA1c_level(hbA1cLevel);
                tempPrediction.setBloodGlucoseLevel(bloodGlucoseLevel);
            }

            // Fetch the Utilisateur associated with the dossierId
            System.out.println("Fetching Utilisateur for dossierId: " + dossierId);
            Utilisateur utilisateur = serviceDossierMedical.getUtilisateurByDossierId(dossierId);
            if (utilisateur == null) {
                throw new Exception("Utilisateur introuvable pour le dossier ID: " + dossierId);
            }
            System.out.println("Utilisateur fetched: " + utilisateur);

            // Validate dateNaissance and calculate age
            System.out.println("Date de naissance: " + utilisateur.getDateNaissance());
            LocalDate dateNaissance = utilisateur.getDateNaissance();
            if (dateNaissance == null) {
                throw new Exception("La date de naissance de l'utilisateur est manquante.");
            }
            if (dateNaissance.isAfter(LocalDate.now())) {
                throw new Exception("La date de naissance ne peut pas être dans le futur : " + dateNaissance);
            }

            int age = serviceDossierMedical.calculateAge(dateNaissance);
            System.out.println("Âge calculé: " + age);

            // If age is 0, prompt the user to confirm or enter a different age
            if (age == 0) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Âge calculé égal à 0");
                confirmation.setHeaderText("L'âge calculé est 0 (patient de moins d'un an).");
                confirmation.setContentText("Voulez-vous continuer avec cet âge, ou entrer un âge différent ?");
                ButtonType continueButton = new ButtonType("Continuer avec 0");
                ButtonType enterAgeButton = new ButtonType("Entrer un âge");
                ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmation.getButtonTypes().setAll(continueButton, enterAgeButton, cancelButton);

                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isEmpty() || result.get() == cancelButton) {
                    return; // User canceled
                } else if (result.get() == enterAgeButton) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Entrer l'âge");
                    dialog.setHeaderText("Âge calculé égal à 0.");
                    dialog.setContentText("Veuillez entrer l'âge du patient :");
                    String ageInput = dialog.showAndWait().orElse(null);
                    if (ageInput == null) {
                        showAlert("Erreur", "L'âge est requis pour effectuer la prédiction.");
                        return;
                    }
                    try {
                        age = Integer.parseInt(ageInput);
                        if (age < 0) {
                            throw new NumberFormatException("L'âge ne peut pas être négatif.");
                        }
                    } catch (NumberFormatException ex) {
                        showAlert("Erreur", "Veuillez entrer un âge valide (nombre entier non négatif).");
                        return;
                    }
                }
                // If user chooses "Continue with 0", age remains 0
            }

            // Map sexe to the API's expected gender values
            String gender = mapSexeToGender(utilisateur.getSexe());

            // Call the Flask API to predict diabetes
            System.out.println("Appel de l'API pour prédire le diabète...");
            boolean hasDiabetes = apiClient.predictDiabetes(tempPrediction, age, gender);
            System.out.println("Résultat de la prédiction : " + hasDiabetes);
            tempPrediction.setDiabete(hasDiabetes);

            // Save to the database
            System.out.println("Enregistrement de la prédiction dans la base de données...");
            if (prediction == null) {
                servicePrediction.ajouter(tempPrediction);
                System.out.println("Prédiction ajoutée avec succès : diabète = " + tempPrediction.isDiabete());
                showAlert("Succès", "Prédiction ajoutée avec succès ! Résultat : " + (hasDiabetes ? "Positif pour le diabète" : "Négatif pour le diabète"));
            } else {
                servicePrediction.modifier(tempPrediction);
                System.out.println("Prédiction modifiée avec succès : diabète = " + tempPrediction.isDiabete());
                showAlert("Succès", "Prédiction modifiée avec succès ! Résultat : " + (hasDiabetes ? "Positif pour le diabète" : "Négatif pour le diabète"));
            }

            // Vérifier que la prédiction a bien été enregistrée
            Prediction savedPrediction = servicePrediction.getByDossierId(dossierId).stream().findFirst().orElse(null);
            if (savedPrediction != null) {
                System.out.println("Prédiction enregistrée dans la base de données : diabète = " + savedPrediction.isDiabete());
            } else {
                System.out.println("Erreur : Impossible de récupérer la prédiction après enregistrement.");
            }

            // If prediction is 1 (diabetes), send email to doctor and patient
            if (hasDiabetes) {
                String[] emails = serviceDossierMedical.getDoctorAndPatientEmails(dossierId, doctorEmail);
                String patientEmail = emails[0];

                if (patientEmail != null || doctorEmail != null) {
                    String subject = "Alerte : Prédiction de diabète positive";
                    String body = "Bonjour,\n\nUne prédiction de diabète a été détectée pour le patient " +
                            utilisateur.getPrenom() + " " + utilisateur.getNom() + " (Dossier ID: " + dossierId + ").\n" +
                            "Résultat de la prédiction : Positif pour le diabète.\n\n" +
                            "Veuillez prendre les mesures nécessaires.\n\nCordialement,\nSystème de Prédiction";

                    // Send email to patient
                    if (patientEmail != null) {
                        emailService.sendEmail(patientEmail, subject, body);
                        System.out.println("Email envoyé au patient : " + patientEmail);
                    } else {
                        System.out.println("Email du patient manquant, email non envoyé.");
                    }

                    // Send email to doctor
                    if (doctorEmail != null) {
                        emailService.sendEmail(doctorEmail, subject, body);
                        System.out.println("Email envoyé au docteur : " + doctorEmail);
                    } else {
                        System.out.println("Email du docteur manquant, email non envoyé.");
                    }
                } else {
                    System.out.println("Erreur : Impossible d'envoyer l'email - Adresses email manquantes.");
                }
            } else {
                System.out.println("Aucune alerte email nécessaire : prédiction négative pour le diabète.");
            }

            annuler();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides pour IMC, HbA1c et Glucose.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement de la prédiction : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la prédiction via l'API : " + e.getMessage());
        }
    }

    public void setListController(PredictionListDoctorController listController) {
        this.listController = listController;
    }

    // Map the sexe field to the API's expected gender values
    private String mapSexeToGender(String sexe) {
        if (sexe == null) {
            return "Other";
        }
        switch (sexe.toLowerCase()) {
            case "femme":
            case "female":
                return "Female";
            case "homme":
            case "male":
                return "Male";
            default:
                return "Other";
        }
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) dossierIdLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}