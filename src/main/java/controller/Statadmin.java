package controller;

import entities.CategorieEv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CategorieEvService;
import service.QuestionService;
import service.VoteService;
import utils.AlertUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Statadmin {

    @FXML private VBox chartsContainer;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalVotesLabel;
    @FXML private Label popularSpecialiteLabel;

    private final QuestionService questionService = new QuestionService();
    private final VoteService voteService = new VoteService();
    private final CategorieEvService categorieEvService = new CategorieEvService();

    @FXML
    public void initialize() {
        if (chartsContainer == null) {
            System.err.println("chartsContainer is null. Check FXML file for fx:id='chartsContainer'.");
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Le conteneur des graphiques est manquant dans l'interface.");
            return;
        }
        try {
            loadStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    private void loadStatistics() throws SQLException {
        // Statistiques de base
        int totalQuestions = questionService.afficher().size();
        long totalVotes = voteService.afficher().size();

        // Mise à jour des labels
        totalQuestionsLabel.setText("Total Questions: " + totalQuestions);
        totalVotesLabel.setText("Total Votes: " + totalVotes);

        // Calcul de la spécialité la plus populaire
        Map<String, Long> specialiteCounts = questionService.afficher().stream()
                .filter(q -> q.getSpecialite() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getSpecialite().name(),
                        Collectors.counting()
                ));

        String popularSpecialite = specialiteCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        popularSpecialiteLabel.setText("Most Popular Specialité: " + popularSpecialite);

        // Création des graphiques
        createSpecialiteChart(specialiteCounts);
        createVoteDistributionChart();
        createQuestionsOverTimeChart();
        createCategoryEventsChart();
    }

    private void createSpecialiteChart(Map<String, Long> specialiteCounts) {
        PieChart specialiteChart = new PieChart();
        specialiteChart.setTitle("Questions by Specialité");

        specialiteCounts.forEach((specialite, count) -> {
            PieChart.Data slice = new PieChart.Data(specialite + " (" + count + ")", count);
            specialiteChart.getData().add(slice);
        });

        chartsContainer.getChildren().add(specialiteChart);
    }

    private void createVoteDistributionChart() throws SQLException {
        long likes = voteService.afficher().stream()
                .filter(v -> v.getValeur().name().equals("Like"))
                .count();
        long dislikes = voteService.afficher().size() - likes;

        PieChart voteChart = new PieChart();
        voteChart.setTitle("Vote Distribution");

        voteChart.getData().add(new PieChart.Data("Likes (" + likes + ")", likes));
        voteChart.getData().add(new PieChart.Data("Dislikes (" + dislikes + ")", dislikes));

        chartsContainer.getChildren().add(voteChart);
    }

    private void createQuestionsOverTimeChart() throws SQLException {
        Map<String, Long> questionsByMonth = questionService.afficher().stream()
                .collect(Collectors.groupingBy(
                        q -> q.getDateCreation().getMonth().toString() + " " + q.getDateCreation().getYear(),
                        Collectors.counting()
                ));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> timeChart = new BarChart<>(xAxis, yAxis);
        timeChart.setTitle("Questions Over Time");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Questions");

        questionsByMonth.forEach((month, count) -> {
            series.getData().add(new XYChart.Data<>(month, count));
        });

        timeChart.getData().add(series);
        chartsContainer.getChildren().add(timeChart);
    }

    private void createCategoryEventsChart() {
        try {
            List<CategorieEv> categories = categorieEvService.afficher();
            System.out.println("Catégories chargées pour le graphique : " + categories.size());

            if (categories.isEmpty()) {
                Label noDataLabel = new Label("Aucune catégorie disponible pour afficher le graphique.");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
                chartsContainer.getChildren().add(noDataLabel);
                return;
            }

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

            // Ajouter le graphique au conteneur
            chartsContainer.getChildren().add(barChart);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger les statistiques des catégories : " + e.getMessage());
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
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des demandes : " + e.getMessage());
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
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des utilisateurs : " + e.getMessage());
        }
    }

    public void goToListeStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statadmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistics Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger le tableau de bord des statistiques : " + e.getMessage());
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
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des événements : " + e.getMessage());
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
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de charger la liste des catégories : " + e.getMessage());
        }
    }
}