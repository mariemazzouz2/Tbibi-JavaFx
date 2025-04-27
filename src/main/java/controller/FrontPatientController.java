package controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Utilisateur;
import utils.SessionManager;

import java.io.IOException;
public class FrontPatientController {
    @FXML private Button btnForum;
    @FXML private Button btnConsultation;
    @FXML private Button btnEvenement;
    @FXML private Button btnProduit;
    @FXML
    private Label labelNomUtilisateur;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText("Bienvenue, " + currentUser.getNom());
        }
        btnForum.setOnAction(e -> {
            // logiques ou navigation
            System.out.println("Forum cliqué");
        });
        btnConsultation.setOnAction(e -> {
            System.out.println("Consultation cliquée");
        });
        btnEvenement.setOnAction(e -> {
            System.out.println("Événement cliqué");
        });
        btnProduit.setOnAction(e -> {
            System.out.println("Produit cliqué");
        });
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
}
