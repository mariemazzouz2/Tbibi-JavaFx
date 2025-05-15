package controller;



import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class SmsFormController {

    // Twilio credentials
    private static final String ACCOUNT_SID = "AC4bf034620b129334895dea366d549b4a";
    private static final String AUTH_TOKEN = "9406f4c46ef29fec1d3d55ff433b8e19";
    private static final String TWILIO_PHONE_NUMBER = "+15155199723";

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea messageArea;

    @FXML
    private Label statusLabel;

    @FXML
    private void sendSms() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String message = messageArea.getText();

        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(
                    new PhoneNumber(phone),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    "Bonjour " + name + ", " + message
            ).create();
            statusLabel.setText("✅ SMS envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur d'envoi du SMS !");
        }
    }
    public void goToUsers(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
