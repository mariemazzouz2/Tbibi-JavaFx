package controller.Evenement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.CategorieEv;
import entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import service.CategorieEvService;
import service.EvenementService;
import utils.SceneSwitch;

import java.io.File;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class EvenementDetails implements javafx.fxml.Initializable {
    private static final Logger logger = Logger.getLogger(EvenementDetails.class.getName());
    private static final String WEATHER_API_KEY = "60114ea7d04f0e9bb984452f09d7ff7b";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";

    @FXML private Label titreLabel;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label lieuLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label statutLabel;
    @FXML private Label categorieLabel;
    @FXML private ImageView imageView;
    @FXML private Button backButton;
    @FXML private Button uploadImageButton;
    @FXML private Button weatherButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvService categorieService = new CategorieEvService();
    private Evenement evenement;
    private WebEngine webEngine;
    @FXML private WebView mapView;

    @Override
    public void initialize(URL location, java.util.ResourceBundle resources) {
        System.out.println("Initialisation du contrôleur EvenementDetails");
        setupMap();
    }

    public void setEvenement(Evenement evenement) {
        System.out.println("Setting evenement: " + evenement.getId() + " - " + evenement.getTitre());
        this.evenement = evenement;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        titreLabel.setText(evenement.getTitre());
        lieuLabel.setText(evenement.getLieu());
        dateDebutLabel.setText(evenement.getDateDebut().format(formatter));
        dateFinLabel.setText(evenement.getDateFin().format(formatter));
        descriptionLabel.setText(evenement.getDescription() != null ? evenement.getDescription() : "Aucune description");
        statutLabel.setText(evenement.getStatut() != null ? evenement.getStatut() : "Non défini");

        try {
            CategorieEv categorie = categorieService.getById(evenement.getCategorieId());
            categorieLabel.setText(categorie != null ? categorie.getNom() : "Non définie");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la catégorie: " + e.getMessage());
            categorieLabel.setText("Erreur");
        }
        if (webEngine != null) {
            double lat = evenement.getLatitude();
            double lng = evenement.getLongitude();
            if (!Double.isNaN(lat) && !Double.isNaN(lng) && lat != 0.0 && lng != 0.0) {
                String escapedLocation = evenement.getLocation() != null ?
                        evenement.getLocation().replace("'", "\\'") : "Lieu inconnu";
                String script = String.format(java.util.Locale.US, "setMarker(%f, %f, '%s');", lat, lng, escapedLocation);

                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                webEngine.executeScript(script);
                            } catch (Exception e) {
                                logger.severe("Error setting map marker: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        logger.severe("setMarker thread interrupted: " + e.getMessage());
                    }
                }).start();
            } else {
                logger.warning("Invalid or missing coordinates: lat=" + lat + ", lng=" + lng);
            }
        } else {
            logger.severe("WebEngine not initialized for map");
        }
        String imagePath = evenement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image;
                String cleanedPath = imagePath.replace("file:", "").trim();
                if (cleanedPath.startsWith("http")) {
                    image = new Image(cleanedPath, true);
                } else {
                    File imageFile = new File(cleanedPath);
                    if (!imageFile.exists()) {
                        System.out.println("Image file does not exist: " + cleanedPath);
                        loadDefaultImage();
                        return;
                    }
                    image = new Image(imageFile.toURI().toString(), true);
                }
                if (image.isError()) {
                    System.out.println("Erreur de chargement de l'image: " + image.getException());
                    loadDefaultImage();
                } else {
                    imageView.setImage(image);
                    System.out.println("Image chargée avec succès: " + cleanedPath);
                }
            } catch (Exception e) {
                System.out.println("Erreur de chargement de l'image: " + e.getMessage());
                loadDefaultImage();
            }
        } else {
            System.out.println("Aucune image disponible pour l'événement");
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            System.out.println("Chargement de l'image par défaut");
            Image defaultImage = new Image(getClass().getResourceAsStream("/assets/images/default-event.jpg"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Erreur de chargement de l'image par défaut: " + e.getMessage());
            imageView.setImage(null);
        }
    }

    private void setupMap() {
        webEngine = mapView.getEngine();
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> {
            logger.severe("WebView Error: " + event.getMessage());
        });
        URL resourceUrl = getClass().getResource("/Event/mapBack.html");
        if (resourceUrl == null) {
            logger.severe("map.html not found in resources at /EventUtils/map.html");
            return;
        }
        String mapUrl = resourceUrl.toExternalForm();
        webEngine.load(mapUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    String consoleLogScript = """
                            window.console.log = function(message) {
                                if (window.javaObj && typeof window.javaObj.logConsole === 'function') {
                                    window.javaObj.logConsole('LOG: ' + message);
                                }
                            };
                            window.console.error = function(message) {
                                if (window.javaObj && typeof window.javaObj.logConsole === 'function') {
                                    window.javaObj.logConsole('ERROR: ' + message);
                                }
                            };
                            """;
                    webEngine.executeScript(consoleLogScript);

                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaObj", new JavaBridge());

                    webEngine.executeScript(
                            "map.dragging.disable(); " +
                                    "map.touchZoom.disable(); " +
                                    "map.doubleClickZoom.disable(); " +
                                    "map.scrollWheelZoom.disable(); " +
                                    "map.boxZoom.disable(); " +
                                    "map.keyboard.disable(); " +
                                    "map.zoomControl.remove(); " +
                                    "map.removeEventListener('click');"
                    );
                } catch (Exception e) {
                    logger.severe("Error initializing map in EventDetails: " + e.getMessage());
                }
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                logger.severe("Map loading failed: " + webEngine.getLoadWorker().getException());
            }
        });
    }

    public class JavaBridge {
        public void logConsole(String message) {
        }
    }

    @FXML
    private void onBackClick() {
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");
        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/Event/AfficherEvent.fxml");
            System.out.println("Retour à la vue des événements réussi");
        } else {
            System.out.println("Impossible de trouver mainRouter pour la navigation");
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image pour l'événement");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String newImagePath = selectedFile.getAbsolutePath();
                evenement.setImage(newImagePath);
                evenementService.modifier(evenement);
                Image newImage = new Image(selectedFile.toURI().toString());
                imageView.setImage(newImage);
                System.out.println("Image chargée avec succès: " + newImagePath);
            } catch (SQLException e) {
                System.out.println("Erreur lors de la mise à jour de l'image dans la base de données: " + e.getMessage());
                showErrorAlert("Erreur", "Impossible de sauvegarder l'image dans la base de données.");
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
                showErrorAlert("Erreur", "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Evenement getEvenement() {
        return evenement;
    }

    @FXML
    private void onWeatherClick() {
        if (evenement == null) {
            showErrorAlert("No Event", "No event data available to fetch weather.");
            return;
        }

        double lat = evenement.getLatitude();
        double lng = evenement.getLongitude();

        if (Double.isNaN(lat) || Double.isNaN(lng) || lat == 0.0 || lng == 0.0) {
            showErrorAlert("Invalid Location", "Event location coordinates are invalid.");
            return;
        }

        try {
            String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric",
                    WEATHER_API_URL, lat, lng, WEATHER_API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(url).toURI())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.severe("Weather API request failed with status: " + response.statusCode() + ", body: " + response.body());
                showErrorAlert("Weather API Error", "Failed to fetch weather data: HTTP " + response.statusCode());
                return;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode weather = root.get("weather").get(0);
            String description = weather.get("description").asText();
            String iconCode = weather.get("icon").asText();
            double temp = root.get("main").get("temp").asDouble();
            int humidity = root.get("main").get("humidity").asInt();

            Stage weatherStage = new Stage();
            weatherStage.setTitle("Weather Forecast for " + evenement.getLieu());

            VBox weatherLayout = new VBox(20);
            weatherLayout.setStyle(
                    "-fx-padding: 20;" +
                            "-fx-alignment: center;"
            );

            String backgroundImagePath;
            if (description.toLowerCase().contains("cloud")) {
                backgroundImagePath = "/assets/images/event/cloudy.jpg";
            } else if (description.toLowerCase().contains("clear")) {
                backgroundImagePath = "/assets/images/event/sunny.jpg";
            } else if (description.toLowerCase().contains("rain")) {
                backgroundImagePath = "/assets/images/event/rainy.jpg";
            } else {
                backgroundImagePath = "/assets/images/event/default.jpg";
            }

            URL backgroundUrl = this.getClass().getResource(backgroundImagePath);
            if (backgroundUrl == null) {
                logger.warning("Background image not found: " + backgroundImagePath + ". Falling back to gradient.");
                weatherLayout.setStyle(weatherLayout.getStyle() +
                        "-fx-background-color: linear-gradient(to bottom, #87CEEB, #E0FFFF);");
            } else {
                Image backgroundImage = new Image(backgroundUrl.toExternalForm());
                BackgroundImage background = new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                weatherLayout.setBackground(new Background(background));
            }

            Label titleLabel = new Label("Weather in " + evenement.getLieu());
            titleLabel.setStyle(
                    "-fx-font-size: 24px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #2F4F4F;" +
                            "-fx-background-color: rgba(255, 255, 255, 0.7);" +
                            "-fx-padding: 10;" +
                            "-fx-background-radius: 10;"
            );

            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            ImageView weatherIcon = new ImageView(new Image(iconUrl));
            weatherIcon.setFitHeight(80);
            weatherIcon.setFitWidth(80);

            Label descriptionLabel = new Label(description.toUpperCase());
            descriptionLabel.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-text-fill: #4682B4;" +
                            "-fx-background-color: rgba(255, 255, 255, 0.8);" +
                            "-fx-padding: 10;" +
                            "-fx-background-radius: 10;"
            );

            Label tempLabel = new Label(String.format("Temperature: %.1f°C", temp));
            tempLabel.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-text-fill: #2F4F4F;" +
                            "-fx-background-color: rgba(255, 255, 255, 0.7);" +
                            "-fx-padding: 5;" +
                            "-fx-background-radius: 5;"
            );

            Label humidityLabel = new Label(String.format("Humidity: %d%%", humidity));
            humidityLabel.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-text-fill: #2F4F4F;" +
                            "-fx-background-color: rgba(255, 255, 255, 0.7);" +
                            "-fx-padding: 5;" +
                            "-fx-background-radius: 5;"
            );

            Button closeButton = new Button("Close");
            closeButton.setStyle(
                    "-fx-background-color: #4682B4;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 5;"
            );
            closeButton.setOnAction(e -> weatherStage.close());

            weatherLayout.getChildren().addAll(titleLabel, weatherIcon, descriptionLabel, tempLabel, humidityLabel, closeButton);

            Scene weatherScene = new Scene(weatherLayout, 400, 400);
            weatherStage.setScene(weatherScene);
            weatherStage.show();
        } catch (Exception e) {
            showErrorAlert("Weather Error", "Failed to fetch weather data: " + e.getMessage());
        }
    }
}