package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.MyDataBase;
import services.ServiceUtilisateur;

import java.net.URL;

public class MainPatient extends Application {
    private static final String PATIENT_ID = "1";

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Vérifier la connexion à la base de données avant de charger l'interface
            MyDataBase.getInstance().getConnection();

            URL location = getClass().getResource("/fxml/Patient/consultation_list.fxml");
            //URL location = getClass().getResource("/fxml/Doctor/consultation_list.fxml");

            if (location == null) {
                throw new RuntimeException("Impossible de trouver /fxml/Patient/consultation_list.fxml dans les ressources");
            }
            // Utiliser FXMLLoader pour pouvoir accéder au contrôleur
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            // Accéder au contrôleur et définir l'ID utilisateur
            tests.Patient.PatientConsultationController controller = loader.getController();
            controller.setCurrentUser(new ServiceUtilisateur().getById(Integer.parseInt(PATIENT_ID)));

            // Créer une scène avec un fond transparent
            Scene scene = new Scene(root, 849, 552);
            scene.setFill(null); // Rendre la scène transparente

            // Configurer la fenêtre
            primaryStage.setTitle("Gestion Médicale - Patient");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur de démarrage", "Une erreur est survenue lors du démarrage de l'application: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}