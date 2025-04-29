package controller;


import models.Question;
import enums.Specialite;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.QuestionService;
import javafx.util.StringConverter;

import java.sql.SQLException;

public class EditQuestionController {
    // Constantes pour les limites de saisie
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_TITLE_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MIN_CONTENT_LENGTH = 20;

    @FXML private TextField titleField;
    @FXML private ComboBox<Specialite> specialiteCombo;
    @FXML private TextArea contentArea;
    @FXML private CheckBox visibleCheck;
    @FXML private Label titleCharCount;
    @FXML private Label contentCharCount;

    private QuestionService questionService = new QuestionService();
    private Question currentQuestion;

    public void setQuestion(Question question) {
        this.currentQuestion = question;
        populateFields();
    }

    @FXML
    public void initialize() {
        setupSpecialiteCombo();
        setupInputValidation();
        setupAutoCorrection();
    }

    private void setupSpecialiteCombo() {
        specialiteCombo.getItems().setAll(Specialite.values());
        specialiteCombo.setConverter(new StringConverter<Specialite>() {
            @Override
            public String toString(Specialite specialite) {
                return specialite != null ? specialite.getValue() : "";
            }

            @Override
            public Specialite fromString(String string) {
                return specialiteCombo.getItems().stream()
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
            validateCapitalization(titleField, newVal);
        });

        // Validation en temps réel pour le contenu
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > MAX_CONTENT_LENGTH) {
                contentArea.setText(oldVal);
            }
            updateCharCount(contentCharCount, newVal.length(), MAX_CONTENT_LENGTH);
            validateCapitalization(contentArea, newVal);
        });

        // Initialiser les compteurs
        updateCharCount(titleCharCount, 0, MAX_TITLE_LENGTH);
        updateCharCount(contentCharCount, 0, MAX_CONTENT_LENGTH);
    }

    private void setupAutoCorrection() {
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) capitalizeFirstLetter(titleField);
        });

        contentArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) capitalizeFirstLetter(contentArea);
        });
    }

    private void populateFields() {
        if (currentQuestion != null) {
            titleField.setText(currentQuestion.getTitre());
            contentArea.setText(currentQuestion.getContenu());
            specialiteCombo.getSelectionModel().select(currentQuestion.getSpecialite());
            visibleCheck.setSelected(currentQuestion.isVisible());

            // Mettre à jour les compteurs
            updateCharCount(titleCharCount, currentQuestion.getTitre().length(), MAX_TITLE_LENGTH);
            updateCharCount(contentCharCount, currentQuestion.getContenu().length(), MAX_CONTENT_LENGTH);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            updateQuestionFromFields();
            questionService.modifier(currentQuestion);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification",
                    "Une erreur est survenue: " + e.getMessage(),
                    Alert.AlertType.ERROR);
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

    private void updateQuestionFromFields() {
        currentQuestion.setTitre(titleField.getText().trim());
        currentQuestion.setContenu(contentArea.getText().trim());
        currentQuestion.setSpecialite(specialiteCombo.getValue());
        currentQuestion.setVisible(visibleCheck.isSelected());
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

    private void validateCapitalization(TextInputControl field, String text) {
        if (!text.isEmpty() && !Character.isUpperCase(text.charAt(0))) {
            field.setStyle("-fx-border-color: red;");
        } else {
            field.setStyle("");
        }
    }

    private void capitalizeFirstLetter(TextInputControl field) {
        String text = field.getText();
        if (!text.isEmpty() && Character.isLowerCase(text.charAt(0))) {
            String corrected = text.substring(0, 1).toUpperCase() + text.substring(1);
            field.setText(corrected);
            field.setStyle("");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        titleField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}