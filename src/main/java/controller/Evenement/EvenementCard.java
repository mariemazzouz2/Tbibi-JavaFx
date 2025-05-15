package controller.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EvenementCard {

    @FXML private VBox root;
    @FXML private Label titreLabel;
    @FXML private Label lieuLabel;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView imageView;
    @FXML private Button voirDetailsButton;

    private Evenement evenement;
    private Pane container;

    public EvenementCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Event/EventCard.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load EventCard.fxml: " + exception.getMessage(), exception);
        }

        // Appliquer un style moderne à la carte
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2); -fx-padding: 15;");
        root.setMinWidth(300);
        root.setMaxWidth(300);

        // Style des labels
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        lieuLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        dateDebutLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        dateFinLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Style du bouton
        voirDetailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-font-size: 14px;");
        voirDetailsButton.setOnMouseEntered(e -> voirDetailsButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-font-size: 14px;"));
        voirDetailsButton.setOnMouseExited(e -> voirDetailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-font-size: 14px;"));

        // Effet de survol pour la carte
        root.setOnMouseEntered(e -> root.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 3);"));
        root.setOnMouseExited(e -> root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);"));
    }

    public VBox getRoot() {
        return root;
    }

    public void setContainer(Pane container) {
        this.container = container;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;

        // Set titreLabel
        titreLabel.setText(evenement.getTitre() != null ? evenement.getTitre() : "Titre inconnu");

        // Set lieuLabel
        lieuLabel.setText(evenement.getLieu() != null ? "📍 " + evenement.getLieu() : "Lieu inconnu");

        // Set dateDebutLabel
        if (evenement.getDateDebut() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateDebutLabel.setText("📅 " + evenement.getDateDebut().format(formatter));
        } else {
            dateDebutLabel.setText("📅 Inconnue");
        }

        // Set dateFinLabel
        if (evenement.getDateFin() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateFinLabel.setText("⏰ " + evenement.getDateFin().format(formatter));
        } else {
            dateFinLabel.setText("⏰ Inconnue");
        }

        // Set descriptionLabel
        descriptionLabel.setText(evenement.getDescription() != null ? evenement.getDescription() : "Aucune description");

        // Set imageView
        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            String imagePath = evenement.getImage().startsWith("file:") ? evenement.getImage() : "file:" + evenement.getImage();
            Image image = new Image(imagePath, true);
            if (image.isError()) {
                System.out.println("Échec du chargement de l'image : " + image.getException().getMessage());
                try {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/assets/images/default-event.jpg")));
                } catch (NullPointerException e) {
                    System.err.println("Default image /assets/images/default-event.jpg not found");
                    imageView.setImage(null);
                }
            } else {
                imageView.setImage(image);
            }
        } else {
            System.out.println("Aucune image pour l'événement : " + evenement.getTitre());
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/assets/images/default-event.jpg")));
            } catch (NullPointerException e) {
                System.err.println("Default image /assets/images/default-event.jpg not found");
                imageView.setImage(null);
            }
        }
    }

    @FXML
    private void handleVoirDetails() {
        if (container == null) {
            System.err.println("Container non défini pour le changement de scène");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/EventDetails.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("EventDetails.fxml not found in classpath");
                return;
            }
            Pane detailsPane = loader.load();
            controller.Evenement.EvenementDetails controller = loader.getController();
            controller.setEvenement(evenement);
            container.getChildren().setAll(detailsPane);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de EventDetails.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }
}