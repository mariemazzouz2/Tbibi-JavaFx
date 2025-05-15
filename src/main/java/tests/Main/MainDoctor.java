package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import tests.Doctor.DossierMedicalListDoctorController;

import java.net.URL;

public class MainDoctor extends Application {
    // ID utilisateur statique pour le docteur
    private static final String DOCTOR_ID = "2";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        //URL location = getClass().getResource("/fxml/Doctor/DossierMedicalListDoctor.fxml");
        URL location = getClass().getResource("/fxml/Doctor/consultation_list.fxml");
        if (location == null) {
            //throw new RuntimeException("Impossible de trouver /fxml/Doctor/DossierMedicalListDoctor.fxml dans les ressources");
            throw new RuntimeException("Impossible de trouver /fxml/Doctor/consultation_list.fxml dans les ressources");

        }
        
        // Utiliser FXMLLoader pour pouvoir accéder au contrôleur
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        
        // Accéder au contrôleur et définir l'ID utilisateur
  //      DossierMedicalListDoctorController controller = loader.getController();
        // TODO: Ajouter une méthode setDoctorId dans DossierMedicalListDoctorController
        // controller.setDoctorId(DOCTOR_ID);

        // Créer une scène avec un fond transparent
        Scene scene = new Scene(root, 849, 552);
        scene.setFill(null); // Rendre la scène transparente

        // Configurer la fenêtre
        primaryStage.setTitle("Gestion Médicale - Docteur");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}