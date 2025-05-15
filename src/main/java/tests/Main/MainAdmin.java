package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainAdmin extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = getClass().getResource("/fxml/Admin/ConsultationListAdmin.fxml");
        if (location == null) {
            throw new RuntimeException("Impossible de trouver /fxml/Admin/ConsultationListAdmin.fxml dans les ressources");
        }
        Parent root = FXMLLoader.load(location);

        // Créer une scène avec un fond transparent
        Scene scene = new Scene(root, 849, 552);
        scene.setFill(null); // Rendre la scène transparente

        // Configurer la fenêtre
        primaryStage.setTitle("Gestion Médicale - Admin");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}