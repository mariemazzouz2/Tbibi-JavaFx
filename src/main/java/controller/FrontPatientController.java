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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Utilisateur;
import tests.Patient.PatientConsultationFormController;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

import static utils.AlertUtils.showAlert;

public class FrontPatientController {
    @FXML
    public Button forum;
    @FXML
    private Label labelNomUtilisateur;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText(currentUser.getNom());
        }
    }

    public void logout(ActionEvent event) {
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

    public void goForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuestion.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Questions");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotoconsultations(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Consultations");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Bookapt(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_form.fxml"));
            Parent root = loader.load();

            PatientConsultationFormController controller = loader.getController();
            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            entities.Utilisateur currentUserobj = new entities.Utilisateur();
            currentUserobj.setId(currentUser.getId());
            currentUserobj.setNom(currentUser.getNom());
            currentUserobj.setAdresse(currentUser.getAdresse());
            currentUserobj.setImage(currentUser.getImage());
            currentUserobj.setPrenom(currentUser.getPrenom());
            currentUserobj.setDateNaissance(currentUser.getDateNaissance());
            currentUserobj.setEmail(currentUser.getEmail());
            currentUserobj.setTelephone(currentUser.getTelephone());
            controller.setCurrentUser(currentUserobj);
            controller.setMode("create");



            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Planifier Consultation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage());
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