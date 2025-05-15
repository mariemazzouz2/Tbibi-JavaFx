package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = getClass().getResource("/fxml/Main.fxml");
        if (location == null) {
            throw new RuntimeException("Impossible de trouver /fxml/Main.fxml dans les ressources");
        }
        Parent root = FXMLLoader.load(location);
        primaryStage.setTitle("Gestion Médicale");
        primaryStage.setScene(new Scene(root, 849, 552));
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}