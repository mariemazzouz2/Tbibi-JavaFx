package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;
import service.UtilisateurDAO;
import utils.MailService;
import utils.SessionManager;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LoginFormController implements Initializable {

    public TextField txtUserName;
    public PasswordField txtPassword;
    public CheckBox checkRememberMe;
    private Preferences preferences;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        preferences = Preferences.userRoot().node(this.getClass().getName());

        String savedEmail = preferences.get("email", "");
        String savedPassword = preferences.get("password", "");

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            txtUserName.setText(savedEmail);
            txtPassword.setText(savedPassword);
            checkRememberMe.setSelected(true);
        }
    }

    public void btnSignIn(ActionEvent actionEvent) {
        try {
            String email = txtUserName.getText();
            String password = txtPassword.getText();

            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            Utilisateur user = utilisateurDAO.getUserByEmail(email);

            if (user == null) {
                showAlert("Utilisateur non trouvé !");
                return;
            }

            if (!BCrypt.checkpw(password, user.getPassword())) {
                showAlert("Mot de passe incorrect !");
                return;
            }

            if (user.getStatus() == 0) {
                showAlert("Vous n'êtes pas encore accepté par l'admin !");
                return;
            }

            // Remember Me Logic
            if (checkRememberMe.isSelected()) {
                preferences.put("email", email);
                preferences.put("password", password);
            } else {
                preferences.remove("email");
                preferences.remove("password");
            }

            // ✅ Stocke l'utilisateur connecté dans la session
            SessionManager.getInstance().login(user);

            // Redirection selon le rôle
            String role = user.getRoles();

            String fxmlFile;
            String title;

            if (role.contains("MEDECIN")) {
                fxmlFile = "/FrontMedecin.fxml";
                title = "Accueil Médecin";
            } else if (role.contains("PATIENT")) {
                fxmlFile = "/FrontPatient.fxml";
                title = "Accueil Patient";
            } else if (role.contains("ADMIN")) {
                fxmlFile = "/backendAdmin.fxml";
                title = "Espace Admin";
            } else {
                showAlert("Rôle inconnu !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) txtUserName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de la connexion !");
        }
    }

    public void btnSignUp(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistrationForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUserName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Connexion");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void handleForgotPassword(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Récupération du mot de passe");
        dialog.setContentText("Veuillez entrer votre adresse email:");

        dialog.showAndWait().ifPresent(email -> {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            Utilisateur user = utilisateurDAO.getUserByEmail(email);

            if (user != null) {
                String tempPassword = generateTemporaryPassword();
                user.setPassword(BCrypt.hashpw(tempPassword, BCrypt.gensalt()));
                utilisateurDAO.updateUserPassword(user);

                // Envoi d'email
                MailService mailService = new MailService();
                String subject = "Récupération de mot de passe";
                String body = "Bonjour,\n\nVotre nouveau mot de passe temporaire est : " + tempPassword + "\n\nMerci de le changer après connexion.";
                mailService.sendMail(email, subject, body);

                showAlertInfo("Un mot de passe temporaire vous a été envoyé par email.");
            } else {
                showAlert("Email non trouvé !");
            }
        });
    }
    private void showAlertInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }



}
