package controller;

// Importations des classes nécessaires
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.QuestionService;
import service.VoteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

public class Statadmin {
    // Déclaration des éléments FXML
    @FXML private VBox chartsContainer; // Conteneur pour les graphiques
    @FXML private Label totalQuestionsLabel; // Label pour le total des questions
    @FXML private Label totalVotesLabel; // Label pour le total des votes
    @FXML private Label popularSpecialiteLabel; // Label pour la spécialité la plus populaire

    // Services pour accéder aux données
    private QuestionService questionService = new QuestionService();
    private VoteService voteService = new VoteService();

    // Méthode d'initialisation appelée automatiquement par JavaFX
    @FXML
    public void initialize() {
        try {
            loadStatistics(); // Charge les statistiques au démarrage
        } catch (SQLException e) {
            e.printStackTrace(); // Gestion basique des erreurs
        }
    }

    /**
     * Charge et affiche les statistiques principales
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    private void loadStatistics() throws SQLException {
        // Statistiques de base
        int totalQuestions = questionService.afficher().size(); // Nombre total de questions
        long totalVotes = voteService.afficher().size(); // Nombre total de votes

        // Mise à jour des labels
        totalQuestionsLabel.setText("Total Questions: " + totalQuestions);
        totalVotesLabel.setText("Total Votes: " + totalVotes);

        // Calcul de la spécialité la plus populaire
        Map<String, Long> specialiteCounts = questionService.afficher().stream()
                .collect(Collectors.groupingBy(
                        q -> q.getSpecialite().name(), // Groupement par spécialité
                        Collectors.counting() // Comptage par groupe
                ));

        // Trouver la spécialité avec le plus grand nombre de questions
        String popularSpecialite = specialiteCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue()) // Trouve l'entrée avec la valeur max
                .map(Map.Entry::getKey) // Extrait la clé (nom de la spécialité)
                .orElse("N/A"); // Valeur par défaut si aucune donnée

        popularSpecialiteLabel.setText("Most Popular Specialité: " + popularSpecialite);

        // Création des graphiques
        createSpecialiteChart(specialiteCounts); // Graphique des spécialités
        createVoteDistributionChart(); // Graphique de distribution des votes
        createQuestionsOverTimeChart(); // Graphique temporel des questions
    }

    /**
     * Crée un diagramme circulaire montrant la répartition des questions par spécialité
     * @param specialiteCounts Map contenant les comptages par spécialité
     */
    private void createSpecialiteChart(Map<String, Long> specialiteCounts) {
        PieChart specialiteChart = new PieChart();
        specialiteChart.setTitle("Questions by Specialité");

        // Ajout des données au graphique
        specialiteCounts.forEach((specialite, count) -> {
            PieChart.Data slice = new PieChart.Data(specialite + " (" + count + ")", count);
            specialiteChart.getData().add(slice);
        });

        chartsContainer.getChildren().add(specialiteChart); // Ajout au conteneur
    }

    /**
     * Crée un diagramme circulaire montrant la distribution des votes (like/dislike)
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    private void createVoteDistributionChart() throws SQLException {
        // Comptage des likes et dislikes
        long likes = voteService.afficher().stream()
                .filter(v -> v.getValeur().name().equals("Like"))
                .count();
        long dislikes = voteService.afficher().size() - likes;

        PieChart voteChart = new PieChart();
        voteChart.setTitle("Vote Distribution");

        // Ajout des données
        voteChart.getData().add(new PieChart.Data("Likes (" + likes + ")", likes));
        voteChart.getData().add(new PieChart.Data("Dislikes (" + dislikes + ")", dislikes));

        chartsContainer.getChildren().add(voteChart); // Ajout au conteneur
    }

    /**
     * Crée un diagramme en barres montrant l'évolution des questions dans le temps
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    private void createQuestionsOverTimeChart() throws SQLException {
        // Groupement des questions par mois et année
        Map<String, Long> questionsByMonth = questionService.afficher().stream()
                .collect(Collectors.groupingBy(
                        q -> q.getDateCreation().getMonth().toString() + " " + q.getDateCreation().getYear(),
                        Collectors.counting()
                ));

        // Création des axes
        CategoryAxis xAxis = new CategoryAxis(); // Axe X (mois/année)
        NumberAxis yAxis = new NumberAxis(); // Axe Y (nombre de questions)
        BarChart<String, Number> timeChart = new BarChart<>(xAxis, yAxis);
        timeChart.setTitle("Questions Over Time");

        // Création de la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Questions");

        // Ajout des données
        questionsByMonth.forEach((month, count) -> {
            series.getData().add(new XYChart.Data<>(month, count));
        });

        timeChart.getData().add(series); // Ajout de la série au graphique
        chartsContainer.getChildren().add(timeChart); // Ajout au conteneur
    }

    // Méthodes de navigation entre les différentes vues

    /**
     * Navigue vers la liste des demandes
     * @param actionEvent événement déclencheur
     */
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
        }
    }

    /**
     * Navigue vers la liste des utilisateurs
     * @param actionEvent événement déclencheur
     */
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
        }
    }

    /**
     * Navigue vers le tableau de bord des statistiques
     * @param actionEvent événement déclencheur
     */
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
        }
    }
}