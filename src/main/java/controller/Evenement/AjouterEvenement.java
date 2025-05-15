package controller.Evenement;

import entities.CategorieEv;
import entities.Evenement;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import service.CategorieEvService;
import service.EvenementService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class AjouterEvenement implements Initializable {

    @FXML private VBox rootVBox;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField lieuField;
    @FXML private Label imagePathLabel;
    @FXML private Button backButton;
    @FXML private ComboBox<CategorieEv> categorieComboBox;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private WebView mapView;
    @FXML private GridPane formGridPane;

    private File selectedImageFile;
    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvService categorieEvService = new CategorieEvService();
    private WebEngine webEngine;
    private static final String HF_API_KEY = "your-huggingface-api-key-here"; // Replace with your Hugging Face API key
    private final DecimalFormat decimalFormat;

    public AjouterEvenement() {
        // Initialize DecimalFormat with Locale.US to ensure dot as decimal separator
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        decimalFormat = new DecimalFormat("0.######", symbols); // Up to 6 decimal places
        decimalFormat.setMinimumFractionDigits(6);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setupDateValidation();
        setupStatutComboBox();
        setupMap();

        latitudeField.setEditable(false);
        longitudeField.setEditable(false);
        lieuField.setEditable(false);

        // Debug GridPane layout
        formGridPane.layoutBoundsProperty().addListener((obs, old, newVal) -> {
            System.out.println("GridPane layout bounds: " + newVal);
        });

        // Debug field changes
        latitudeField.textProperty().addListener((obs, old, newVal) -> {
            System.out.println("latitudeField changed to: '" + newVal + "'");
        });
        longitudeField.textProperty().addListener((obs, old, newVal) -> {
            System.out.println("longitudeField changed to: '" + newVal + "'");
        });
    }

    private void setupMap() {
        webEngine = mapView.getEngine();
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> {
            System.err.println("WebView Error: " + event.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur WebView", "Erreur dans le WebView : " + event.getMessage());
        });
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));

        URL resourceUrl = getClass().getResource("/Event/map.html");
        if (resourceUrl == null) {
            System.err.println("map.html not found in resources");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier map.html introuvable dans les ressources.");
            webEngine.loadContent("<html><body><h3>Erreur : Carte non chargée</h3></body></html>");
            return;
        }
        String mapUrl = resourceUrl.toExternalForm();
        System.out.println("Loading map from: " + mapUrl);
        webEngine.load(mapUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            System.out.println("WebView State: " + newVal);
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javafxCallback", new JavaBridge());
                    webEngine.executeScript("if (window.javafxCallback) window.javafxCallback.testBridge()");
                    // Debug WebView dimensions
                    System.out.println("WebView dimensions: width=" + mapView.getWidth() + ", height=" + mapView.getHeight());
                } catch (Exception e) {
                    System.err.println("Error initializing JavaBridge: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation de JavaBridge : " + e.getMessage());
                }
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("WebView Load Failed: " + webEngine.getLoadWorker().getException());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la carte : " + webEngine.getLoadWorker().getException());
                webEngine.loadContent("<html><body><h3>Erreur : Échec du chargement de la carte</h3></body></html>");
            }
        });

        // Force WebView layout
        mapView.setMinHeight(150);
        mapView.setMinWidth(30);
        mapView.widthProperty().addListener((obs, old, newVal) -> {
            System.out.println("WebView width changed to: " + newVal);
        });
        mapView.heightProperty().addListener((obs, old, newVal) -> {
            System.out.println("WebView height changed to: " + newVal);
        });
    }

    public class JavaBridge {
        public void setLocation(double lat, double lng, String placeName) {
            System.out.println("JavaBridge: setLocation called with lat=" + lat + ", lng=" + lng + ", place=" + placeName);
            if (latitudeField == null || longitudeField == null || lieuField == null) {
                System.err.println("JavaBridge: UI fields are null");
                return;
            }

            // Ensure lat and lng are valid, assign to final variables
            final double finalLat = Double.isNaN(lat) || Double.isInfinite(lat) ? 0.0 : lat;
            final double finalLng = Double.isNaN(lng) || Double.isInfinite(lng) ? 0.0 : lng;
            final String finalPlaceName = (placeName == null || placeName.trim().isEmpty()) ? "Tunis" : placeName;

            // Use final variables in the lambda expression
            Platform.runLater(() -> {
                // Format latitude and longitude with dot as decimal separator
                String latText = decimalFormat.format(finalLat);
                String lngText = decimalFormat.format(finalLng);
                latitudeField.setText(latText);
                longitudeField.setText(lngText);
                lieuField.setText(finalPlaceName);

                // Log the exact values being set
                System.out.println("JavaBridge: Setting latitudeField to '" + latText + "', longitudeField to '" + lngText + "', lieuField to '" + finalPlaceName + "'");

                String highlightStyle = "-fx-background-color: lightgreen;";
                latitudeField.setStyle(highlightStyle);
                longitudeField.setStyle(highlightStyle);
                lieuField.setStyle(highlightStyle);

                // Reset highlight after 1 second
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            latitudeField.setStyle("");
                            longitudeField.setStyle("");
                            lieuField.setStyle("");
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        }

        public void testBridge() {
            System.out.println("JavaBridge: testBridge called successfully");
        }
    }

    private void loadCategories() {
        try {
            categorieComboBox.getItems().clear();
            categorieComboBox.getItems().addAll(categorieEvService.afficher());

            categorieComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(CategorieEv categorie, boolean empty) {
                    super.updateItem(categorie, empty);
                    setText(empty || categorie == null ? null : categorie.getNom());
                }
            });

            categorieComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(CategorieEv categorie, boolean empty) {
                    super.updateItem(categorie, empty);
                    setText(empty || categorie == null ? null : categorie.getNom());
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    private void setupStatutComboBox() {
        statutComboBox.getItems().addAll("actif", "inactif", "annulé", "complet");
        statutComboBox.setValue("actif");
    }

    private void setupDateValidation() {
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebutPicker.getValue() != null && newVal.isBefore(dateDebutPicker.getValue())) {
                dateFinPicker.setValue(dateDebutPicker.getValue());
            }
        });
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            String filePath = "file:" + file.getAbsolutePath().replace("\\", "/");
            imagePathLabel.setText(filePath);
        }
    }

    @FXML
    void removeImage(ActionEvent event) {
        selectedImageFile = null;
        imagePathLabel.setText("Aucune image choisie");
    }

    @FXML
    void generateAIImage(ActionEvent event) {
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();

        if (titre.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir le titre et la description pour générer une image.");
            return;
        }

        try {
            String prompt = "Create an image for an event titled '" + titre + "' with description: " + description;
            File generatedImage = callAIImageGenerationAPI(prompt);

            if (generatedImage != null) {
                selectedImageFile = generatedImage;
                String filePath = "file:" + generatedImage.getAbsolutePath().replace("\\", "/");
                imagePathLabel.setText(filePath);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Image générée avec succès !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la génération de l'image.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération de l'image : " + e.getMessage());
        }
    }

    private File callAIImageGenerationAPI(String prompt) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = "{\"inputs\": \"" + prompt + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new java.net.URI("https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-2-1"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + HF_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to generate image with Hugging Face API: " + response.statusCode());
        }

        Path tempFile = Files.createTempFile("ai_generated_", ".png");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(response.body());
        }
        return tempFile.toFile();
    }

    @FXML
    void ajouter(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            Evenement evenement = new Evenement();
            evenement.setTitre(titreField.getText());
            evenement.setDescription(descriptionField.getText());
            evenement.setDateDebut(dateDebutPicker.getValue());
            evenement.setDateFin(dateFinPicker.getValue());
            evenement.setLieu(lieuField.getText());
            evenement.setStatut(statutComboBox.getValue());
            evenement.setCategorieId(categorieComboBox.getValue().getId());
            evenement.setImage(selectedImageFile != null ? "file:" + selectedImageFile.getAbsolutePath().replace("\\", "/") : "file:/default.png");

            // Log the raw field values before parsing
            String latitudeText = latitudeField.getText() != null ? latitudeField.getText().trim() : "";
            String longitudeText = longitudeField.getText() != null ? longitudeField.getText().trim() : "";
            System.out.println("Before parsing - LatitudeField: '" + latitudeText + "', LongitudeField: '" + longitudeText + "'");

            // Parse latitude and longitude (validation already ensures they are valid)
            double latitude = Double.parseDouble(latitudeText.replace(',', '.')); // Replace comma with dot if necessary
            double longitude = Double.parseDouble(longitudeText.replace(',', '.')); // Replace comma with dot if necessary
            System.out.println("Parsed latitude: " + latitude);
            System.out.println("Parsed longitude: " + longitude);

            // Set latitude and longitude on the Evenement object
            evenement.setLatitude(latitude);
            evenement.setLongitude(longitude);
            System.out.println("Setting Evenement latitude to: " + latitude + ", longitude to: " + longitude);

            evenementService.ajouter(evenement);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'événement a été ajouté avec succès.");
            onBackClick(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'événement : " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (titreField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() ||
                dateDebutPicker.getValue() == null ||
                dateFinPicker.getValue() == null ||
                lieuField.getText().isEmpty() ||
                statutComboBox.getValue() == null ||
                categorieComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (titreField.getText().length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        if (!titreField.getText().matches("[a-zA-Z0-9\\s\\-éèàçêâîïùûü]*")) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre contient des caractères non autorisés.");
            return false;
        }

        if (descriptionField.getText().length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Description invalide", "La description doit contenir au moins 10 caractères.");
            return false;
        }

        if (lieuField.getText().length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Emplacement invalide", "L'emplacement doit contenir au moins 3 caractères.");
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = dateDebutPicker.getValue();
        LocalDate endDate = dateFinPicker.getValue();

        if (startDate.isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de début ne peut pas être dans le passé.");
            return false;
        }

        if (endDate.isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de fin ne peut pas être dans le passé.");
            return false;
        }

        if (endDate.isBefore(startDate)) {
            showAlert(Alert.AlertType.WARNING, "Dates invalides", "La date de fin doit être après la date de début.");
            return false;
        }

        // Debug latitude and longitude values
        String latitudeText = latitudeField.getText() != null ? latitudeField.getText().trim() : "";
        String longitudeText = longitudeField.getText() != null ? longitudeField.getText().trim() : "";
        System.out.println("ValidateInputs - Latitude: '" + latitudeText + "', Longitude: '" + longitudeText + "'");

        // Add validation for latitude and longitude
        try {
            // Replace comma with dot to handle locale differences
            double latitude = Double.parseDouble(latitudeText.replace(',', '.'));
            if (latitude < -90 || latitude > 90) {
                showAlert(Alert.AlertType.WARNING, "Latitude invalide", "La latitude doit être entre -90 et 90.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse latitude: '" + latitudeText + "'");
            showAlert(Alert.AlertType.WARNING, "Latitude invalide", "Veuillez sélectionner une position valide sur la carte.");
            return false;
        }

        try {
            // Replace comma with dot to handle locale differences
            double longitude = Double.parseDouble(longitudeText.replace(',', '.'));
            if (longitude < -180 || longitude > 180) {
                showAlert(Alert.AlertType.WARNING, "Longitude invalide", "La longitude doit être entre -180 et 180.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse longitude: '" + longitudeText + "'");
            showAlert(Alert.AlertType.WARNING, "Longitude invalide", "Veuillez sélectionner une position valide sur la carte.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEventBack.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find AfficherEventBack.fxml");
            }
            Parent newPane = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newPane);
            stage.setScene(newScene);
            stage.setTitle("Event List");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    @FXML
    void annuler(ActionEvent actionEvent) {
        onBackClick(actionEvent);
    }
}