package controller;



import models.Question;
import models.Utilisateur;
import enums.Specialite;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.QuestionService;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddQuestionController {
    // Constantes pour les limites de saisie
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_TITLE_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MIN_CONTENT_LENGTH = 20;
    private static final long MAX_IMAGE_SIZE = 5_000_000; // 5MB

    @FXML private TextField titleField;
    @FXML private ComboBox<Specialite> specialiteCombo;
    @FXML private TextArea contentArea;
    @FXML private CheckBox visibleCheck;
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;
    @FXML private Label titleCharCount;
    @FXML private Label contentCharCount;

    private QuestionService questionService = new QuestionService();
    private File selectedImageFile;
    private static final String IMAGE_DIR = "src/main/resources/images/";

    @FXML
    public void initialize() {
        createImageDirectory();
        initializeSpecialiteCombo();
        setupInputValidation();
        setupAutoCorrection();
    }

    private void initializeSpecialiteCombo() {
        specialiteCombo.getItems().setAll(Arrays.stream(Specialite.values())
                .filter(spec -> spec != Specialite.NONE)
                .sorted(Comparator.comparing(Specialite::getValue))
                .collect(Collectors.toList()));

        specialiteCombo.setConverter(new StringConverter<Specialite>() {
            @Override
            public String toString(Specialite specialite) {
                return specialite != null ? specialite.getValue() : "";
            }

            @Override
            public Specialite fromString(String string) {
                return Arrays.stream(Specialite.values())
                        .filter(spec -> spec.getValue().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void setupInputValidation() {
        // Validation en temps réel pour le titre
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > MAX_TITLE_LENGTH) {
                titleField.setText(oldVal);
            }
            updateCharCount(titleCharCount, newVal.length(), MAX_TITLE_LENGTH);

            // Validation majuscule en temps réel
            if (!newVal.isEmpty() && !Character.isUpperCase(newVal.charAt(0))) {
                titleField.setStyle("-fx-border-color: red;");
            } else {
                titleField.setStyle("");
            }
        });

        // Validation en temps réel pour le contenu
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > MAX_CONTENT_LENGTH) {
                contentArea.setText(oldVal);
            }
            updateCharCount(contentCharCount, newVal.length(), MAX_CONTENT_LENGTH);

            // Validation majuscule en temps réel
            if (!newVal.isEmpty() && !Character.isUpperCase(newVal.charAt(0))) {
                contentArea.setStyle("-fx-border-color: red;");
            } else {
                contentArea.setStyle("");
            }
        });

        // Initialiser les compteurs
        updateCharCount(titleCharCount, 0, MAX_TITLE_LENGTH);
        updateCharCount(contentCharCount, 0, MAX_CONTENT_LENGTH);
    }

    private void setupAutoCorrection() {
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Quand le champ perd le focus
                capitalizeFirstLetter(titleField);
            }
        });

        contentArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Quand le champ perd le focus
                capitalizeFirstLetter(contentArea);
            }
        });
    }

    private void capitalizeFirstLetter(TextInputControl field) {
        String text = field.getText();
        if (!text.isEmpty() && Character.isLowerCase(text.charAt(0))) {
            String corrected = text.substring(0, 1).toUpperCase() + text.substring(1);
            field.setText(corrected);
            field.setStyle(""); // Retire le style d'erreur
        }
    }

    private void updateCharCount(Label counterLabel, int currentLength, int maxLength) {
        if (counterLabel != null) {
            counterLabel.setText(String.format("%d/%d", currentLength, maxLength));
            if (currentLength > maxLength * 0.9) {
                counterLabel.setStyle("-fx-text-fill: red;");
            } else {
                counterLabel.setStyle("-fx-text-fill: gray;");
            }
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) uploadImageButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            if (file.length() > MAX_IMAGE_SIZE) {
                showAlert("Erreur", "Fichier trop volumineux",
                        "L'image ne doit pas dépasser " + (MAX_IMAGE_SIZE/1_000_000) + "MB",
                        Alert.AlertType.ERROR);
                return;
            }

            if (!isValidImageExtension(file)) {
                showAlert("Erreur", "Format non supporté",
                        "Seuls les formats JPG, JPEG et PNG sont acceptés",
                        Alert.AlertType.ERROR);
                return;
            }

            selectedImageFile = file;
            imagePathLabel.setText(file.getName());
        }
    }

    private boolean isValidImageExtension(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    @FXML
    private void handleSubmit() {
        if (!validateFields()) {
            return;
        }

        try {
            Question question = createQuestion();
            questionService.ajouter(question);
            showSuccessAndRedirect();
        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Erreur lors de l'ajout",
                    "Une erreur est survenue: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        // Validation du titre
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Erreur", "Titre manquant", "Le titre est obligatoire", Alert.AlertType.ERROR);
            titleField.requestFocus();
            return false;
        }
        if (title.length() < MIN_TITLE_LENGTH) {
            showAlert("Erreur", "Titre trop court",
                    "Le titre doit contenir au moins " + MIN_TITLE_LENGTH + " caractères",
                    Alert.AlertType.ERROR);
            titleField.requestFocus();
            return false;
        }
        if (!Character.isUpperCase(title.charAt(0))) {
            showAlert("Erreur", "Format incorrect",
                    "Le titre doit commencer par une majuscule",
                    Alert.AlertType.ERROR);
            titleField.requestFocus();
            return false;
        }

        // Validation du contenu
        String content = contentArea.getText().trim();
        if (content.isEmpty()) {
            showAlert("Erreur", "Contenu manquant", "Veuillez décrire votre question", Alert.AlertType.ERROR);
            contentArea.requestFocus();
            return false;
        }
        if (content.length() < MIN_CONTENT_LENGTH) {
            showAlert("Erreur", "Description trop courte",
                    "La description doit contenir au moins " + MIN_CONTENT_LENGTH + " caractères",
                    Alert.AlertType.ERROR);
            contentArea.requestFocus();
            return false;
        }
        if (!Character.isUpperCase(content.charAt(0))) {
            showAlert("Erreur", "Format incorrect",
                    "La description doit commencer par une majuscule",
                    Alert.AlertType.ERROR);
            contentArea.requestFocus();
            return false;
        }

        // Validation de la spécialité
        if (specialiteCombo.getValue() == null) {
            showAlert("Erreur", "Spécialité manquante", "Veuillez sélectionner une spécialité", Alert.AlertType.ERROR);
            specialiteCombo.requestFocus();
            return false;
        }

        return true;
    }

    private Question createQuestion() throws IOException {
        Question question = new Question();
        question.setTitre(titleField.getText().trim());
        question.setContenu(contentArea.getText().trim());
        question.setSpecialite(specialiteCombo.getValue());
        question.setVisible(visibleCheck.isSelected());
        question.setDateCreation(LocalDateTime.now());

        Utilisateur patient = new Utilisateur();
        patient.setId(1); // À remplacer par l'ID de l'utilisateur connecté
        question.setPatient(patient);

        if (selectedImageFile != null) {
            String imagePath = saveImage(selectedImageFile);
            question.setImage(imagePath);
        }

        return question;
    }

    private void showSuccessAndRedirect() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Votre question a été ajoutée avec succès !");

        alert.showAndWait().ifPresent(response -> {
            try {
                // Fermer la fenêtre actuelle
                Stage currentStage = (Stage) titleField.getScene().getWindow();
                currentStage.close();

                // Charger la liste des questions
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/ListQuestion.fxml"));
                Parent root = loader.load();

                // Créer une nouvelle scène
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Liste des Questions");
                stage.show();
            } catch (IOException e) {
                showAlert("Erreur", "Navigation",
                        "Impossible d'ouvrir la liste des questions: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void createImageDirectory() {
        Path dirPath = Paths.get(IMAGE_DIR);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                showAlert("Erreur", "Création du dossier",
                        "Impossible de créer le dossier images: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    private String saveImage(File sourceFile) throws IOException {
        String extension = getFileExtension(sourceFile);
        String uniqueFileName = UUID.randomUUID() + "." + extension;
        Path targetPath = Paths.get(IMAGE_DIR, uniqueFileName);
        Files.copy(sourceFile.toPath(), targetPath);
        return "images/" + uniqueFileName;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return lastIndex > 0 ? name.substring(lastIndex + 1).toLowerCase() : "";
    }
}