package controller.Evenement;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Evenement;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import models.Utilisateur;
import service.EvenementService;
import utils.SceneSwitch;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AfficherEvenementBack extends SceneSwitch {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    @FXML
    private Button addButton;

    private final EvenementService service = new EvenementService();
    private static final Logger logger = Logger.getLogger(AfficherEvenementBack.class.getName());

    @FXML
    public void initialize() {
        // Vérifier que le bouton est bien chargé
        if (addButton != null) {
            System.out.println("Bouton 'Ajouter Event' trouvé dans le FXML.");
        } else {
            System.out.println("Bouton 'Ajouter Event' NON trouvé dans le FXML !");
        }

        loadEvenementCards();
    }

    /**
     * Charge tous les événements depuis la base de données et les affiche dans le FlowPane.
     */
    @FXML
    public void loadEvenementCards() {
        cardsContainer.getChildren().clear();

        try {
            List<Evenement> evenements = service.recuperer();

            for (Evenement evenement : evenements) {
                cardsContainer.getChildren().add(createEvenementCard(evenement));
            }

        } catch (SQLException e) {
            logger.severe("Erreur lors du chargement des événements : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée une carte (VBox) visuelle pour un événement donné.
     */
    private VBox createEvenementCard(Evenement evenement) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        // Titre de l'événement
        Label titleLabel = new Label(evenement.getTitre());
        titleLabel.setStyle("-fx-text-fill: #555;");

        // Lieu (ajuste selon tes données)
        Label lieuLabel = new Label("Lieu: " + evenement.getLieu());
        lieuLabel.setStyle("-fx-text-fill: #555;");

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button modifyButton = new Button("Modifier");
        modifyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        modifyButton.setOnAction(e -> modifyEvenement(evenement));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteEvenement(evenement));

        buttonsBox.getChildren().addAll(modifyButton, deleteButton);

        // Ajout à la carte
        card.getChildren().addAll(titleLabel, lieuLabel, buttonsBox);

        return card;
    }

    /**
     * Ouvre une nouvelle scène pour ajouter un événement.
     */
    @FXML
    void onAddClick(ActionEvent event) throws IOException {
        System.out.println("Bouton 'Ajouter Event' cliqué !");
        switchScene(rootVBox, "/Event/AjouterEvent.fxml");
        loadEvenementCards();
    }

    /**
     * Ouvre une nouvelle fenêtre pour modifier un événement.
     */
    private void modifyEvenement(Evenement evenement) {
        try {
            // Debug: Log classpath root
            logger.info("Classpath root: " + getClass().getResource("/"));
            // Try loading with getClassLoader
            java.net.URL fxmlUrl = getClass().getClassLoader().getResource("Event/ModifierEvent.fxml");
            if (fxmlUrl == null) {
                logger.severe("Erreur : ModifierEvenement.fxml introuvable dans les ressources.");
                throw new IOException("Fichier FXML non trouvé : ModifierEvenement.fxml");
            }
            logger.info("Chargement de ModifierEvenement.fxml depuis : " + fxmlUrl.toExternalForm());
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent modifierRoot = loader.load();

            controller.Evenement.ModifierEvenement controller = loader.getController();
            controller.setEvenement(evenement);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Événement");
            stage.setScene(new javafx.scene.Scene(modifierRoot));
            stage.showAndWait();

            loadEvenementCards();
        } catch (IOException e) {
            logger.severe("Erreur lors de la modification de l'événement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprime l’événement sélectionné.
     */
    private void deleteEvenement(Evenement evenement) {
        try {
            service.supprimer(evenement.getId());
            loadEvenementCards();
            logger.info("Événement supprimé : " + evenement.getTitre());
        } catch (SQLException e) {
            logger.severe("Erreur lors de la suppression de l'événement : " + e.getMessage());
            e.printStackTrace();
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
}