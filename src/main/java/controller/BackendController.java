package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Utilisateur;
import utils.SessionManager;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import models.Utilisateur;
import service.UtilisateurDAO; // Exemple
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BackendController {

    @FXML
    private Label labelNomUtilisateur;
    @FXML
    private Button suiviMedicalButton;
    @FXML
    private PieChart rolePieChart;
    @FXML
    private VBox legendContainer;
    @FXML
    private Button produitButton;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            labelNomUtilisateur.setText(currentUser.getNom());
        }
        afficherStatistiquesRoles();
    }
    @FXML
    private void goToDossierList() {
        try {
            // Chemin vers le fichier FXML du Forum
            String fxmlPath = "/fxml/Admin/DossierMedicalListAdmin.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new RuntimeException("Impossible de trouver " + fxmlPath + " dans les ressources");
            }

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Forum");
            stage.setScene(new Scene(root, 800, 600)); // Taille personnalisable
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page du Forum : " + e.getMessage());
        }
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
    @FXML
    private void goToProduit() {
        try {
            // Chemin vers le fichier FXML des Produits
            String fxmlPath = "/BackOffice/Backoffice.fxml";
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
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToListeDemandes(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendDemande.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Demandes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToListeUsers(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void afficherStatistiquesRoles() {
        Map<String, Integer> roleCount = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Utilisateur> utilisateurs = utilisateurDAO.getAllUsers();

            for (Utilisateur u : utilisateurs) {
                try {
                    String[] roles = mapper.readValue(u.getRoles(), String[].class);
                    for (String role : roles) {
                        roleCount.put(role, roleCount.getOrDefault(role, 0) + 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            rolePieChart.getData().clear();
            for (Map.Entry<String, Integer> entry : roleCount.entrySet()) {
                rolePieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            // Attendre que les nodes soient rendus pour accéder à leur couleur
            Platform.runLater(() -> {
                legendContainer.getChildren().clear();

                for (PieChart.Data data : rolePieChart.getData()) {
                    Node node = data.getNode();
                    if (node != null && node instanceof javafx.scene.shape.Path) {
                        Color color = (Color) ((javafx.scene.shape.Shape) node).getFill();

                        Rectangle colorBox = new Rectangle(10, 10, color);
                        Label roleLabel = new Label(data.getName());
                        HBox legendItem = new HBox(10, colorBox, roleLabel);

                        legendContainer.getChildren().add(legendItem);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void goToListeStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statadmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToEvent(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEventBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Event");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToCategory(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category/AfficherCategorieEv.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Catagorie");
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

