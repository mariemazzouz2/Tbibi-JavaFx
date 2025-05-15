package controller.Doctor;

import entities.DossierMedical;
import entities.Prediction;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import services.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionStatsController {

    @FXML
    private Label headerTitle;

    @FXML
    private PieChart diabetesPieChart;

    @FXML
    private Label diabetesPieChartErrorLabel;

    @FXML
    private ScatterChart<Number, Number> bmiHbA1cScatterChart;

    @FXML
    private Label bmiHbA1cScatterChartErrorLabel;

    @FXML
    private BarChart<String, Number> ageGroupBarChart;

    @FXML
    private Label ageGroupBarChartErrorLabel;

    @FXML
    private BarChart<String, Number> genderBarChart; // Nouveau BarChart pour la répartition par sexe

    @FXML
    private Label genderBarChartErrorLabel; // Label d'erreur pour le nouveau graphique

    private ServicePrediction servicePrediction;
    private ServiceDossierMedical serviceDossier;
    private ServiceUtilisateur serviceUtilisateur;

    public PredictionStatsController() {
        try {
            this.servicePrediction = new ServicePrediction();
            this.serviceDossier = new ServiceDossierMedical();
            this.serviceUtilisateur = new ServiceUtilisateur();
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'initialisation des services : " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        loadDiabetesPieChart();
        loadBmiHbA1cScatterChart();
        loadAgeGroupBarChart();
        loadGenderBarChart(); // Charger le nouveau graphique
    }

    private void loadDiabetesPieChart() {
        try {
            List<Prediction> predictions = servicePrediction.getAll();
            System.out.println("Nombre de prédictions récupérées : " + predictions.size());

            if (predictions.isEmpty()) {
                diabetesPieChartErrorLabel.setText("Aucune donnée disponible pour la répartition des prédictions.");
                diabetesPieChartErrorLabel.setVisible(true);
                return;
            }

            int positiveCount = 0;
            int negativeCount = 0;
            for (Prediction prediction : predictions) {
                if (prediction.isDiabete()) {
                    positiveCount++;
                } else {
                    negativeCount++;
                }
            }

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Diabète Positif", positiveCount),
                    new PieChart.Data("Diabète Négatif", negativeCount)
            );

            diabetesPieChart.setData(pieChartData);
            diabetesPieChartErrorLabel.setVisible(false);
        } catch (SQLException e) {
            diabetesPieChartErrorLabel.setText("Erreur lors du chargement des données : " + e.getMessage());
            diabetesPieChartErrorLabel.setVisible(true);
            System.out.println("Erreur lors du chargement du PieChart : " + e.getMessage());
        }
    }

    private void loadBmiHbA1cScatterChart() {
        try {
            List<Prediction> predictions = servicePrediction.getAll();
            System.out.println("Nombre de prédictions pour ScatterChart : " + predictions.size());

            if (predictions.isEmpty()) {
                bmiHbA1cScatterChartErrorLabel.setText("Aucune donnée disponible pour la corrélation BMI-HbA1c.");
                bmiHbA1cScatterChartErrorLabel.setVisible(true);
                return;
            }

            XYChart.Series<Number, Number> seriesPositive = new XYChart.Series<>();
            seriesPositive.setName("Diabète Positif");
            XYChart.Series<Number, Number> seriesNegative = new XYChart.Series<>();
            seriesNegative.setName("Diabète Négatif");

            for (Prediction prediction : predictions) {
                if (prediction.getBmi() == 0 || prediction.gethbA1c_level() == 0) {
                    System.out.println("Données manquantes pour la prédiction ID " + prediction.getId() + " (BMI ou HbA1c).");
                    continue;
                }

                if (prediction.isDiabete()) {
                    seriesPositive.getData().add(new XYChart.Data<>(prediction.getBmi(), prediction.gethbA1c_level()));
                } else {
                    seriesNegative.getData().add(new XYChart.Data<>(prediction.getBmi(), prediction.gethbA1c_level()));
                }
            }

            bmiHbA1cScatterChart.getData().clear();
            bmiHbA1cScatterChart.getData().addAll(seriesPositive, seriesNegative);
            bmiHbA1cScatterChartErrorLabel.setVisible(false);
        } catch (SQLException e) {
            bmiHbA1cScatterChartErrorLabel.setText("Erreur lors du chargement des données : " + e.getMessage());
            bmiHbA1cScatterChartErrorLabel.setVisible(true);
            System.out.println("Erreur lors du chargement du ScatterChart : " + e.getMessage());
        }
    }

    private void loadAgeGroupBarChart() {
        try {
            List<Prediction> predictions = servicePrediction.getAll();
            System.out.println("Nombre de prédictions pour AgeGroupBarChart : " + predictions.size());

            if (predictions.isEmpty()) {
                ageGroupBarChartErrorLabel.setText("Aucune donnée disponible pour les prédictions par tranche d'âge.");
                ageGroupBarChartErrorLabel.setVisible(true);
                return;
            }

            Map<String, Integer> positiveAgeGroups = new HashMap<>();
            Map<String, Integer> negativeAgeGroups = new HashMap<>();

            initializeAgeGroups(positiveAgeGroups, negativeAgeGroups);

            for (Prediction prediction : predictions) {
                DossierMedical dossier = serviceDossier.getById(prediction.getDossierId());
                if (dossier == null) {
                    System.out.println("Dossier introuvable pour la prédiction ID " + prediction.getId());
                    continue;
                }

                Utilisateur utilisateur = serviceUtilisateur.getById(dossier.getUtilisateurId());
                if (utilisateur == null || utilisateur.getDateNaissance() == null) {
                    System.out.println("Utilisateur ou date de naissance introuvable pour le dossier ID " + dossier.getId());
                    continue;
                }

                int age = Period.between(utilisateur.getDateNaissance(), LocalDate.now()).getYears();
                String ageGroup = getAgeGroup(age);

                if (prediction.isDiabete()) {
                    positiveAgeGroups.put(ageGroup, positiveAgeGroups.get(ageGroup) + 1);
                } else {
                    negativeAgeGroups.put(ageGroup, negativeAgeGroups.get(ageGroup) + 1);
                }
            }

            XYChart.Series<String, Number> seriesPositive = new XYChart.Series<>();
            seriesPositive.setName("Diabète Positif");
            XYChart.Series<String, Number> seriesNegative = new XYChart.Series<>();
            seriesNegative.setName("Diabète Négatif");

            for (String ageGroup : positiveAgeGroups.keySet()) {
                seriesPositive.getData().add(new XYChart.Data<>(ageGroup, positiveAgeGroups.get(ageGroup)));
                seriesNegative.getData().add(new XYChart.Data<>(ageGroup, negativeAgeGroups.get(ageGroup)));
            }

            ageGroupBarChart.getData().clear();
            ageGroupBarChart.getData().addAll(seriesPositive, seriesNegative);
            ageGroupBarChartErrorLabel.setVisible(false);
        } catch (SQLException e) {
            ageGroupBarChartErrorLabel.setText("Erreur lors du chargement des données : " + e.getMessage());
            ageGroupBarChartErrorLabel.setVisible(true);
            System.out.println("Erreur lors du chargement du AgeGroupBarChart : " + e.getMessage());
        }
    }

    private void loadGenderBarChart() {
        try {
            List<Prediction> predictions = servicePrediction.getAll();
            System.out.println("Nombre de prédictions pour GenderBarChart : " + predictions.size());

            if (predictions.isEmpty()) {
                genderBarChartErrorLabel.setText("Aucune donnée disponible pour les prédictions par sexe.");
                genderBarChartErrorLabel.setVisible(true);
                return;
            }

            Map<String, Integer> positiveGender = new HashMap<>();
            Map<String, Integer> negativeGender = new HashMap<>();

            // Initialiser les catégories pour les sexes
            positiveGender.put("Masculin", 0);
            positiveGender.put("Féminin", 0);
            negativeGender.put("Masculin", 0);
            negativeGender.put("Féminin", 0);

            for (Prediction prediction : predictions) {
                DossierMedical dossier = serviceDossier.getById(prediction.getDossierId());
                if (dossier == null) {
                    System.out.println("Dossier introuvable pour la prédiction ID " + prediction.getId());
                    continue;
                }

                Utilisateur utilisateur = serviceUtilisateur.getById(dossier.getUtilisateurId());
                if (utilisateur == null || utilisateur.getSexe() == null) {
                    System.out.println("Utilisateur ou sexe introuvable pour le dossier ID " + dossier.getId());
                    continue;
                }

                String gender = utilisateur.getSexe().equalsIgnoreCase("M") ? "Masculin" : "Féminin";

                if (prediction.isDiabete()) {
                    positiveGender.put(gender, positiveGender.get(gender) + 1);
                } else {
                    negativeGender.put(gender, negativeGender.get(gender) + 1);
                }
            }

            XYChart.Series<String, Number> seriesPositive = new XYChart.Series<>();
            seriesPositive.setName("Diabète Positif");
            XYChart.Series<String, Number> seriesNegative = new XYChart.Series<>();
            seriesNegative.setName("Diabète Négatif");

            for (String gender : positiveGender.keySet()) {
                seriesPositive.getData().add(new XYChart.Data<>(gender, positiveGender.get(gender)));
                seriesNegative.getData().add(new XYChart.Data<>(gender, negativeGender.get(gender)));
            }

            genderBarChart.getData().clear();
            genderBarChart.getData().addAll(seriesPositive, seriesNegative);
            genderBarChartErrorLabel.setVisible(false);
        } catch (SQLException e) {
            genderBarChartErrorLabel.setText("Erreur lors du chargement des données : " + e.getMessage());
            genderBarChartErrorLabel.setVisible(true);
            System.out.println("Erreur lors du chargement du GenderBarChart : " + e.getMessage());
        }
    }

    private void initializeAgeGroups(Map<String, Integer> positiveAgeGroups, Map<String, Integer> negativeAgeGroups) {
        String[] ageGroups = {"0-18", "19-30", "31-45", "46-60", "61+"};
        for (String ageGroup : ageGroups) {
            positiveAgeGroups.put(ageGroup, 0);
            negativeAgeGroups.put(ageGroup, 0);
        }
    }

    private String getAgeGroup(int age) {
        if (age <= 18) return "0-18";
        if (age <= 30) return "19-30";
        if (age <= 45) return "31-45";
        if (age <= 60) return "46-60";
        return "61+";
    }
}