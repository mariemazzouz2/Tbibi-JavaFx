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
import utils.SceneSwitch;

import java.io.IOException;

public class EvenementCardBack extends VBox {
    @FXML
    private Label dateDebutLabel;    // date_debut → dateDebutLabel

    @FXML
    private Label dateFinLabel;      // date_fin → dateFinLabel

    @FXML
    private Label categorieLabel;    // categorie_id → categorieLabel

    @FXML
    private ImageView evenementImage; // image → evenementImage

    @FXML
    private Label titreLabel;         // titre → titreLabel

    @FXML
    private Label lieuLabel;          // lieu → lieuLabel

    @FXML
    private Button voirDetailsButton;

    private Evenement evenement;

    public EvenementCardBack() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Event/EvenementCard.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        titreLabel.setText(evenement.getTitre());
        lieuLabel.setText("Lieu: " + evenement.getLieu());

        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            try {
                Image image = new Image(evenement.getImage());
                evenementImage.setImage(image);
            } catch (Exception e) {
                evenementImage.setImage(new Image("/images/default-event.jpg"));
            }
        }
    }

    Pane container;

    @FXML
    private void handleVoirDetails() {
        SceneSwitch.switchScene(container, "/Event/EvenementDetails.fxml");
    }

    public void setData(Evenement e) {
        this.titreLabel.setText(e.getTitre());
        this.dateDebutLabel.setText("Début: " + e.getDateDebut().toString());
        this.dateFinLabel.setText("Fin: " + e.getDateFin().toString());
        this.lieuLabel.setText("Lieu: " + e.getLieu());
        this.categorieLabel.setText("Catégorie: " + e.getCategorieId());

        if (e.getImage() != null && !e.getImage().isEmpty()) {
            try {
                Image image = new Image(e.getImage());
                this.evenementImage.setImage(image);
            } catch (Exception ex) {
                System.out.println("Erreur de chargement d'image: " + ex.getMessage());
            }
        } else {
            this.evenementImage.setImage(null);
        }
    }
}