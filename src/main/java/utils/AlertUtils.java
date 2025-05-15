package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Utility class for displaying alerts in the application
 */
public class AlertUtils {
    
    /**
     * Shows an error alert with the specified title and content
     * 
     * @param title The title of the alert
     * @param content The main content of the alert
     */
    public static void showError(String title, String content) {
        showAlert(AlertType.ERROR, title, content, null);
    }
    
    /**
     * Shows an error alert with the specified title, content, and detail message
     * 
     * @param title The title of the alert
     * @param content The main content of the alert
     * @param details Additional details to display
     */
    public static void showError(String title, String content, String details) {
        showAlert(AlertType.ERROR, title, content, details);
    }
    
    /**
     * Shows an information alert with the specified title and content
     * 
     * @param title The title of the alert
     * @param content The main content of the alert
     */
    public static void showInformation(String title, String content) {
        showAlert(AlertType.INFORMATION, title, null, content);
    }
    
    /**
     * Shows a warning alert with the specified title and content
     * 
     * @param title The title of the alert
     * @param content The main content of the alert
     */
    public static void showWarning(String title, String content) {
        showAlert(AlertType.WARNING, title, null, content);
    }
    
    /**
     * Shows a confirmation alert with the specified title and content
     * 
     * @param title The title of the alert
     * @param content The main content of the alert
     * @return true if the user confirmed, false otherwise
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        return alert.showAndWait()
                .filter(response -> response == javafx.scene.control.ButtonType.OK)
                .isPresent();
    }
    
    /**
     * Generic method to show an alert with the specified type, title, header, and content
     * 
     * @param type The type of alert
     * @param title The title of the alert
     * @param header The header text of the alert (can be null)
     * @param content The main content of the alert
     */
    public static void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 