package controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Utilisateur;
import utils.SessionManager;

import java.io.IOException;

public class FrontMedecinController {

    @FXML
    private Label labelNomUtilisateur;
    @FXML
    private Button produitButton;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText( currentUser.getNom());
        }

    }

    @FXML
    private void goToProduit() {
        try {
            // Chemin vers le fichier FXML des Produits
            String fxmlPath = "/FrontOffice/FrontOfficeView.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new RuntimeException("Impossible de trouver " + fxmlPath + " dans les ressources");
            }

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Produits");
            stage.setScene(new Scene(root, 800, 600)); // Taille personnalisable
            stage.setResizable(true);
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) produitButton.getScene().getWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void logout(javafx.event.ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToDossierList() {
        try {
            // Chemin vers le fichier FXML du Forum
            String fxmlPath = "/fxml/Doctor/DossierMedicalListDoctor.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new RuntimeException("Impossible de trouver " + fxmlPath + " dans les ressources");
            }

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Forum");
            stage.setScene(new Scene(root, 800, 600)); // Taille personnalisable
            stage.setResizable(true);
            stage.show();

            // Fermer la fenêtre actuelle
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page du Forum : " + e.getMessage());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void goForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuestionDocteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Questions");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToConsultation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/consultation_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Questions");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goEvement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEvent.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(" Evenement");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
