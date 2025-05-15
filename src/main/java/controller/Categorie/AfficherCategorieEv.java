package controller.Categorie;

import entities.CategorieEv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.CategorieEvService;
import utils.SceneSwitch;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static utils.AlertUtils.showAlert;

public class AfficherCategorieEv extends SceneSwitch {

    @FXML private FlowPane cardsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Label labelNomUtilisateur;

    private final CategorieEvService categorieEvService = new CategorieEvService();

    @FXML
    public void initialize() {
        System.out.println("Initializing AfficherCategorieEv...");
        if (cardsContainer == null) {
            System.err.println("cardsContainer is null. Check FXML file for fx:id='cardsContainer'.");
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", null, "Le conteneur des cartes est manquant dans l'interface.");
            return;
        }
        if (scrollPane == null) {
            System.err.println("scrollPane is null. Check FXML file for fx:id='scrollPane'.");
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", null, "Le conteneur de défilement est manquant dans l'interface.");
            return;
        }
        loadCategoryCards();
    }

    private void loadCategoryCards() {
        cardsContainer.getChildren().clear();

        try {
            List<CategorieEv> categories = categorieEvService.afficher();
            System.out.println("Catégories chargées : " + categories.size());

            for (CategorieEv categorieEv : categories) {
                VBox card = createCategoryCard(categorieEv);
                cardsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    private VBox createCategoryCard(CategorieEv categorieEv) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        Label nameLabel = new Label(categorieEv.getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(categorieEv.getDescription());
        descLabel.setWrapText(true);

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEdit(categorieEv));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete(categorieEv));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(nameLabel, descLabel, buttonsBox);
        return card;
    }

    @FXML
    private void handleAdd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Category/AjouterCategorieEv.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Ajouter une catégorie");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        // Rafraîchir la liste après ajout
        loadCategoryCards();
    }

    private void handleEdit(CategorieEv categorieEv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Category/ModifierCategorieEv.fxml"));
            VBox modifierRoot = loader.load();

            ModifierCategorieEv controller = loader.getController();
            controller.setCategorieEv(categorieEv);

            Stage stage = new Stage();
            stage.setTitle("Modifier la catégorie");
            stage.setScene(new Scene(modifierRoot));
            stage.showAndWait();

            loadCategoryCards();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible d'ouvrir l'interface de modification : " + e.getMessage());
        }
    }

    private void handleDelete(CategorieEv categorieEv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + categorieEv.getNom() + "' ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categorieEvService.supprimer(categorieEv.getId());
                    cardsContainer.getChildren().removeIf(node -> {
                        if (node instanceof VBox card && card.getChildren().get(0) instanceof Label label) {
                            return label.getText().equals(categorieEv.getNom());
                        }
                        return false;
                    });
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, "Échec de la suppression : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        try {
            Stage statsStage = new Stage();
            statsStage.setTitle("Statistiques des catégories");

            // Créer les axes
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Catégorie");
            yAxis.setLabel("Nombre d'événements");

            // Créer le graphique
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Nombre d'événements par catégorie");
            barChart.setLegendVisible(false);

            // Liste de couleurs prédéfinies
            String[] colors = {
                    "#FF6347", "#4682B4", "#9ACD32", "#FFD700",
                    "#6A5ACD", "#FF4500", "#20B2AA", "#9932CC"
            };

            // Ajouter les données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Événements");

            List<CategorieEv> categories = categorieEvService.afficher();
            for (int i = 0; i < categories.size(); i++) {
                CategorieEv category = categories.get(i);
                int eventCount = categorieEvService.getEventCountForCategory(category.getId());
                XYChart.Data<String, Number> data = new XYChart.Data<>(category.getNom(), eventCount);
                final int colorIndex = i % colors.length;
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: " + colors[colorIndex] + ";");
                    }
                });
                series.getData().add(data);
            }

            barChart.getData().add(series);

            // Ajouter du style global au graphique
            barChart.setStyle("-fx-background-color: #f4f4f4;");

            // Créer la scène
            VBox statsVBox = new VBox(10);
            statsVBox.setPadding(new Insets(20));
            statsVBox.getChildren().add(barChart);

            Scene statsScene = new Scene(statsVBox, 600, 400);
            statsStage.setScene(statsScene);
            statsStage.show();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    @FXML
    public void onAddClick(ActionEvent actionEvent) {
        SceneSwitch.switchScene(cardsContainer, "/Category/AjouterCategorieEv.fxml");
    }

    public void logout(ActionEvent event) {
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
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la page de connexion : " + e.getMessage());
        }
    }

    public void goToListeDemandes(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendDemande.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Demandes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des demandes : " + e.getMessage());
        }
    }

    public void goToListeUsers(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackendUsers.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des utilisateurs : " + e.getMessage());
        }
    }

    public void goToListeStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statadmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    public void goToEvent(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEventBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Événements");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des événements : " + e.getMessage());
        }
    }

    public void goToCategory(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Category/AfficherCategorieEv.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Catégories");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des catégories : " + e.getMessage());
        }
    }
}