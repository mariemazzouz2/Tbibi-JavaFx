package controller;

// Importations des classes nécessaires
import models.Question;
import models.Reponse;
import models.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import service.ReponseService;
import jakarta.mail.MessagingException; // Pour la gestion des erreurs d'envoi d'email
import utils.MailService; // Service personnalisé pour l'envoi d'emails

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AddResponseController {
    // Composants de l'interface graphique (liés au fichier FXML)
    @FXML private Label questionTitleLabel; // Affiche le titre de la question
    @FXML private TextArea contentField;    // Zone de texte pour la réponse

    // Services utilisés
    private ReponseService responseService = new ReponseService(); // Pour gérer les réponses en base de données
    private MailService emailService = new MailService();          // Pour envoyer des emails

    // Données
    private Question question; // La question à laquelle on répond
    private ManageResponsesController parentController; // Référence au contrôleur parent
    private final int currentMedecinId = 2; // ID du médecin connecté (en dur pour l'exemple)

    /**
     * Définit la question à laquelle on répond
     * @param question La question sélectionnée
     */
    public void setQuestion(Question question) {
        this.question = question;
        questionTitleLabel.setText("Question: " + question.getTitre()); // Affiche le titre
    }

    /**
     * Définit le contrôleur parent pour la communication
     * @param parentController Le contrôleur qui a ouvert cette fenêtre
     */
    public void setParentController(ManageResponsesController parentController) {
        this.parentController = parentController;
    }

    /**
     * Gère l'enregistrement d'une nouvelle réponse
     */
    @FXML
    private void handleSave() {
        // Récupère et nettoie le contenu
        String contenu = contentField.getText().trim();

        // Validation du contenu
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Validation", "Le contenu de la réponse ne peut pas être vide.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Création de l'objet Réponse
            Reponse reponse = new Reponse();
            reponse.setContenu(contenu);
            reponse.setDateReponse(LocalDateTime.now()); // Date actuelle
            reponse.setQuestion(question);

            // Création du médecin (simplifié)
            Utilisateur medecin = new Utilisateur();
            medecin.setId(currentMedecinId);
            reponse.setMedecin(medecin);

            // Sauvegarde en base de données
            responseService.ajouter(reponse);

            // ★ Notification par email ★
            Utilisateur questionOwner = question.getPatient(); // Auteur de la question
            System.out.println(questionOwner); // Debug

            if (questionOwner != null) {
                // Envoi de l'email (adresse en dur pour l'exemple)
                emailService.sendmail(
                        "siwarchouanine5@gmail.com", // Devrait être questionOwner.getEmail()
                        question.getTitre(),         // Sujet du mail
                        contenu                      // Contenu de la réponse
                );
            } else {
                showAlert("Avertissement",
                        "Email non envoyé",
                        "L'utilisateur ou l'email n'est pas disponible.",
                        Alert.AlertType.WARNING);
            }

            // Ferme la fenêtre
            contentField.getScene().getWindow().hide();

        } catch (SQLException e) {
            // Erreur de base de données
            showAlert("Erreur", "Erreur lors de l'enregistrement", e.getMessage(), Alert.AlertType.ERROR);
        } catch (MessagingException e) {
            // Erreur d'envoi d'email
            showAlert("Erreur", "Erreur lors de l'envoi de l'email", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Gère l'annulation (fermeture de la fenêtre)
     */
    @FXML
    private void handleCancel() {
        contentField.getScene().getWindow().hide();
    }

    /**
     * Affiche une alerte standard
     */
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}