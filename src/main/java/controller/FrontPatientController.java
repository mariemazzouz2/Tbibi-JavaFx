package controller;
import controller.Patient.DossierMedicalDetailsPatientController;
import entities.DossierMedical;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
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
import services.ServiceDossierMedical;
import utils.SessionManager;
import tests.Patient.PatientConsultationFormController;
import java.io.IOException;
import java.sql.SQLException;
import static utils.AlertUtils.showAlert;

public class FrontPatientController {
    @FXML
    public Button forum;
    @FXML
    private Label labelNomUtilisateur;
    @FXML
    private Button suiviMedicalButton;
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
    private void goToDossierList() {
        try {
            // Vérifier si un utilisateur_id a été fourni
            /*if (utilisateurId == null || utilisateurId.isEmpty()) {
                showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter.");
                return;
            }*/

            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            int userId;
            try {
                //userId = Integer.parseInt(utilisateurId);
                userId = currentUser.getId();
            } catch (NumberFormatException e) {
                return;
            }

            // Récupérer le dossier médical de l'utilisateur
            ServiceDossierMedical service = new ServiceDossierMedical();
            DossierMedical dossier = service.getByUtilisateurId(userId);

            if (dossier == null) {
                showAlert("Erreur", "Aucun dossier médical trouvé pour l'utilisateur avec l'ID " + userId);
                return;
            }

            // Charger DossierMedicalDetails.fxml pour afficher les détails du dossier
            String fxmlPath = "/fxml/Patient/DossierMedicalDetails.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                System.err.println("Chemin absolu des ressources : " + getClass().getResource("/").getPath());
                throw new RuntimeException("Impossible de trouver " + fxmlPath + " dans les ressources");
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Passer le dossier au contrôleur
            DossierMedicalDetailsPatientController controller = loader.getController();
            controller.setDossier(dossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical (Patient)");
            stage.setScene(new Scene(root, 800, 600)); // Taille augmentée pour un grand format
            stage.setResizable(true);
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) suiviMedicalButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du dossier : " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la récupération du dossier : " + e.getMessage());
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


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
