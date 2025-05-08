package org.example.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the backoffice main view
 */
public class BackofficeController implements Initializable {

    @FXML
    private BorderPane mainContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load products view by default when backoffice is opened
        loadView("/BackOffice/ProductView.fxml");
    }

    @FXML
    private void handleProductsAction(ActionEvent event) {
        loadView("/BackOffice/ProductView.fxml");
    }

    @FXML
    private void handleCommandesAction(ActionEvent event) {
        // Will implement later
        loadView("/BackOffice/OrderView.fxml");
    }



    /**
     * Loads a view into the main container
     * @param fxmlPath Path to the FXML file
     */
    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContainer.setCenter(view);
        } catch (IOException ex) {
            Logger.getLogger(BackofficeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}