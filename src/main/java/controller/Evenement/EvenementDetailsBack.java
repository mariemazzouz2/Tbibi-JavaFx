package controller.Evenement;
import entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import service.CategorieEvService;
import service.EvenementService;
import utils.SceneSwitch;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EvenementDetailsBack implements javafx.fxml.Initializable {

    public Label imagePathLabel;  // Renommé de imagelabel
    @FXML
    private Label titreLabel;  // Renommé de titleLabel

    @FXML
    private Label dateDebutLabel;  // Renommé de getStartingDatelabel

    @FXML
    private Label lieuLabel;  // Renommé de locationlabel

    @FXML
    private Label statutLabel;  // Renommé de statusLabel

    @FXML
    private ImageView imageView;

    @FXML
    private Button supprimerButton;  // Renommé de deleteButton

    @FXML
    private Button modifierButton;  // Renommé de modifyButton

    @FXML
    private Button retourButton;  // Renommé de backButton

    @FXML
    private FlowPane categorieContainer;  // Renommé de categoryContainer

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvService categorieEvService = new CategorieEvService();

    private Evenement evenement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du contrôleur EvenementDetailsBack");

        // Configuration du FlowPane
        categorieContainer.setHgap(15);
        categorieContainer.setVgap(15);
        categorieContainer.setPrefWrapLength(700);
    }

    public void setEvenement(Evenement evenement) {
        System.out.println("Configuration de l'événement: " + evenement.getId() + " - " + evenement.getTitre());
        this.evenement = evenement;

        // Définition des détails de l'événement
        titreLabel.setText(evenement.getTitre());
        lieuLabel.setText(evenement.getLieu());
        statutLabel.setText(evenement.getStatut());
        imagePathLabel.setText(evenement.getImage());

        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            try {
                Image image = new Image(evenement.getImage());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Erreur de chargement de l'image: " + e.getMessage());
            }
        }

        // Configuration du bouton supprimer
        supprimerButton.setOnAction(e -> {
            try {
                evenementService.supprimer(evenement.getId());
                // Navigation vers la liste des événements
                Node mainRouter = supprimerButton.getScene().getRoot().lookup("#mainRouter");
                if (mainRouter instanceof Pane) {
                    SceneSwitch.switchScene((Pane) mainRouter, "/Event/AfficherEvenement.fxml");
                    System.out.println("Retour à la vue des événements après suppression");
                } else {
                    System.out.println("Impossible de trouver mainRouter après suppression");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void onRetourClick() {  // Renommé de onBackClick
        Node node = retourButton.getScene().getRoot().lookup("#mainRouter");

        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/Event/AfficherEvenement.fxml");
            System.out.println("Retour à la vue des événements");
        } else {
            System.out.println("Impossible de trouver mainRouter pour la navigation");
        }
    }
}