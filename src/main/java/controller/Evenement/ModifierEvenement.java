package controller.Evenement;

import entities.Evenement;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import service.EvenementService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.json.JSONObject;

public class ModifierEvenement {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField lieuField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Button mapsButton;
    @FXML private WebView mapView;
    @FXML private VBox chatBox;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private Button sendChatButton;
    private Evenement eventToEdit;
    private static final Logger logger = Logger.getLogger(ModifierEvenement.class.getName());
    private Evenement evenement;
    private final EvenementService evenementService = new EvenementService();
    private WebEngine webEngine;
    private double latitude;
    private double longitude;
    private boolean isMapLoaded = false;
    private boolean isUserClick = false;

    @FXML
    public void initialize() {
        webEngine = mapView.getEngine();
        logger.info("Initialisation du WebView...");
        loadMap();

        statutComboBox.getItems().addAll("actif", "inactif", "annulé", "complet");

         }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        titreField.setText(evenement.getTitre());
        descriptionField.setText(evenement.getDescription());
        dateDebutPicker.setValue(evenement.getDateDebut());
        dateFinPicker.setValue(evenement.getDateFin());

        String lieu = evenement.getLieu();
        logger.info("Lieu brut de l'événement : " + lieu);
        String lieuName = lieu;
        if (lieu != null && lieu.contains("(") && lieu.endsWith(")")) {
            int startIndex = lieu.lastIndexOf("(");
            lieuName = lieu.substring(0, startIndex).trim();
            String coords = lieu.substring(startIndex + 1, lieu.length() - 1);
            logger.info("Coordonnées brutes extraites : " + coords);
            String[] latLon = coords.split(",");
            try {
                if (latLon.length == 2) {
                    latitude = Double.parseDouble(latLon[0].trim());
                    longitude = Double.parseDouble(latLon[1].trim());
                    if (Double.isNaN(latitude) || Double.isInfinite(latitude) || Double.isNaN(longitude) || Double.isInfinite(longitude)) {
                        throw new IllegalArgumentException("Coordonnées invalides (NaN ou Infinity) : lat=" + latitude + ", lng=" + longitude);
                    }
                    logger.info("Coordonnées valides : latitude=" + latitude + ", longitude=" + longitude);
                } else {
                    throw new IllegalArgumentException("Format de coordonnées invalide : " + coords);
                }
            } catch (Exception e) {
                logger.warning("Erreur lors de l'extraction des coordonnées : " + e.getMessage() + ". Utilisation des valeurs par défaut.");
                latitude = 36.8065; // Tunis par défaut
                longitude = 10.1815;
                lieuName = "Tunis";
            }
        } else {
            logger.warning("Format de lieu invalide ou sans coordonnées : " + lieu + ". Utilisation des valeurs par défaut.");
            latitude = 36.8065; // Tunis par défaut
            longitude = 10.1815;
            lieuName = "Tunis";
        }

        latitudeField.setText(String.format("%.6f", latitude));
        longitudeField.setText(String.format("%.6f", longitude));
        logger.info("Valeurs initiales assignées : latitudeField=" + latitudeField.getText() + ", longitudeField=" + longitudeField.getText());
        lieuField.setText(lieuName);
        statutComboBox.setValue(evenement.getStatut());

        if (isMapLoaded) {
            updateMap();
        } else {
            logger.info("Carte non encore chargée, mise à jour différée.");
        }
    }

    private void loadMap() {
        java.net.URL mapUrl = getClass().getResource("/Event/upmap.html");
        if (mapUrl == null) {
            logger.severe("Erreur : upmap.html introuvable dans les ressources.");
            mapView.setDisable(true);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier upmap.html introuvable.");
            return;
        }
        logger.info("Chargement de upmap.html depuis : " + mapUrl.toExternalForm());
        webEngine.load(mapUrl.toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                logger.info("WebView chargé avec succès.");
                isMapLoaded = true;
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javafxCallback", new MapCallback());
                updateMap();
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                logger.severe("Échec du chargement du WebView : " + webEngine.getLoadWorker().getException());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la carte.");
            }
        });

        webEngine.setOnAlert(event -> logger.info("Message JavaScript : " + event.getData()));
        webEngine.setOnError(event -> logger.severe("Erreur dans le WebView : " + event.getMessage()));
    }

    private void updateMap() {
        if (!isMapLoaded) {
            logger.warning("WebView non chargé, tentative de mise à jour différée.");
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    if (isMapLoaded) {
                        Platform.runLater(this::updateMap);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }

        if (webEngine == null || latitude == 0.0 || longitude == 0.0 || Double.isNaN(latitude) || Double.isInfinite(latitude) || Double.isNaN(longitude) || Double.isInfinite(longitude)) {
            logger.warning("Coordonnées invalides ou WebView non initialisé : latitude=" + latitude + ", longitude=" + longitude);
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Coordonnées invalides, veuillez sélectionner un lieu.");
            return;
        }

        logger.info("Mise à jour de la carte avec latitude=" + latitude + ", longitude=" + longitude + ", lieu=" + lieuField.getText());
        try {
            String lieu = lieuField.getText().replace("'", "\\'");
            webEngine.executeScript(String.format("setMarker(%f, %f, '%s');", latitude, longitude, lieu));
        } catch (netscape.javascript.JSException e) {
            logger.severe("Erreur JavaScript lors de la mise à jour de la carte : " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour la carte : " + e.getMessage());
        }
    }

    private void fetchPlaceName(double lat, double lng) {
        new Thread(() -> {
            try {
                URL url = new URL(String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=10&addressdetails=1", lat, lng));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject json = new JSONObject(response.toString());
                String placeName = json.optJSONObject("address").optString("city",
                        json.optJSONObject("address").optString("town",
                                json.optJSONObject("address").optString("village",
                                        json.optJSONObject("address").optString("road", "Lieu inconnu"))));
                Platform.runLater(() -> {
                    lieuField.setText(placeName);
                    logger.info("Nom du lieu récupéré : " + placeName);
                });
            } catch (Exception e) {
                logger.warning("Erreur lors de la récupération du nom du lieu : " + e.getMessage());
                Platform.runLater(() -> lieuField.setText("Lieu inconnu"));
            }
        }).start();
    }

    public class MapCallback {
        public void setLocation(double lat, double lng, String placeName) {
            logger.info("Coordonnées reçues de la carte : lat=" + lat + ", lng=" + lng + ", lieu=" + placeName);
            if (Double.isNaN(lat) || Double.isInfinite(lat) || Double.isNaN(lng) || Double.isInfinite(lng)) {
                logger.warning("Coordonnées invalides reçues : lat=" + lat + ", lng=" + lng);
                showAlert(Alert.AlertType.WARNING, "Coordonnées invalides", "Les coordonnées ne doivent pas être NaN ou infinies.");
                return;
            }

            latitude = lat;
            longitude = lng;
            Platform.runLater(() -> {
                latitudeField.setText(String.format("%.6f", lat));
                longitudeField.setText(String.format("%.6f", lng));
                logger.info("Valeurs après clic sur la carte : latitudeField=" + latitudeField.getText() + ", longitudeField=" + longitudeField.getText());
                String simplifiedPlaceName = simplifyPlaceName(placeName);
                lieuField.setText(simplifiedPlaceName);

                String highlightStyle = "-fx-background-color: lightgreen;";
                latitudeField.setStyle(highlightStyle);
                longitudeField.setStyle(highlightStyle);
                lieuField.setStyle(highlightStyle);

                updateMap(); // Mettre à jour la carte après le clic

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            latitudeField.setStyle("");
                            longitudeField.setStyle("");
                            lieuField.setStyle("");
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
    }

    private String simplifyPlaceName(String placeName) {
        String[] parts = placeName.split(",");
        return parts.length > 0 ? parts[0].trim() : placeName;
    }

    @FXML
    void onMapsClick(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner un emplacement");
        dialog.setHeaderText("Entrez les coordonnées de l'emplacement");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField latInput = new TextField(latitudeField.getText());
        latInput.setPromptText("Latitude (ex. 36.8065)");
        TextField lonInput = new TextField(longitudeField.getText());
        lonInput.setPromptText("Longitude (ex. 10.1815)");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Latitude:"), latInput,
                new Label("Longitude:"), lonInput
        );
        dialogPane.setContent(content);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Activer le bouton OK uniquement si les deux champs contiennent des nombres valides
        latInput.textProperty().addListener((obs, old, newVal) -> {
            boolean disable = true;
            try {
                if (!newVal.trim().isEmpty()) {
                    double lat = Double.parseDouble(newVal);
                    if (!lonInput.getText().trim().isEmpty()) {
                        double lon = Double.parseDouble(lonInput.getText());
                        if (!Double.isNaN(lat) && !Double.isInfinite(lat) && !Double.isNaN(lon) && !Double.isInfinite(lon)) {
                            disable = false;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Garder le bouton désactivé
            }
            okButton.setDisable(disable);
        });

        lonInput.textProperty().addListener((obs, old, newVal) -> {
            boolean disable = true;
            try {
                if (!newVal.trim().isEmpty()) {
                    double lon = Double.parseDouble(newVal);
                    if (!latInput.getText().trim().isEmpty()) {
                        double lat = Double.parseDouble(latInput.getText());
                        if (!Double.isNaN(lat) && !Double.isInfinite(lat) && !Double.isNaN(lon) && !Double.isInfinite(lon)) {
                            disable = false;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Garder le bouton désactivé
            }
            okButton.setDisable(disable);
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String latText = latInput.getText().trim();
                    String lonText = lonInput.getText().trim();

                    double newLatitude = Double.parseDouble(latText);
                    double newLongitude = Double.parseDouble(lonText);


                    latitude = newLatitude;
                    longitude = newLongitude;
                    latitudeField.setText(String.format("%.6f", latitude));
                    longitudeField.setText(String.format("%.6f", longitude));
                    logger.info("Valeurs après dialogue : latitudeField=" + latitudeField.getText() + ", longitudeField=" + longitudeField.getText());
                    updateMap();
                    fetchPlaceName(newLatitude, newLongitude);
                } catch (NumberFormatException e) {
                    logger.severe("Erreur lors de la conversion des coordonnées : " + e.getMessage());
                    showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Les valeurs de latitude et longitude doivent être des nombres valides.");
                }
            }
        });
    }

      private String generateAssistantResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        if (lowerMessage.contains("titre") || lowerMessage.contains("nom")) {
            return "Pour modifier le titre, entrez un nouveau nom dans le champ 'Titre de l'événement'.";
        } else if (lowerMessage.contains("lieu") || lowerMessage.contains("emplacement") || lowerMessage.contains("carte")) {
            return "Pour changer le lieu, utilisez le bouton 'Entrer les coordonnées' ou cliquez sur la carte.";
        } else if (lowerMessage.contains("date") || lowerMessage.contains("début") || lowerMessage.contains("fin")) {
            return "Vous pouvez ajuster les dates en utilisant les sélecteurs 'Date de début' et 'Date de fin'.";
        } else if (lowerMessage.contains("statut")) {
            return "Sélectionnez un statut ('actif', 'inactif', 'annulé', 'complet') dans la liste déroulante 'Statut'.";
        } else {
            return "Je suis là pour aider ! Parlez-moi du titre, lieu, dates, ou statut pour des conseils spécifiques. Votre message : " + userMessage;
        }
    }


    @FXML
    void modifierEvenement(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            evenement.setTitre(titreField.getText());
            evenement.setDescription(descriptionField.getText());
            evenement.setDateDebut(dateDebutPicker.getValue());
            evenement.setDateFin(dateFinPicker.getValue());

            // Utilisation des valeurs des champs sans conversion en double
            String lieuWithCoords = String.format("%s (%s, %s)", lieuField.getText(), latitudeField.getText(), longitudeField.getText());
            evenement.setLieu(lieuWithCoords);
            evenement.setStatut(statutComboBox.getValue());

            evenementService.modifier(evenement);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'événement a été modifié avec succès.");
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification de l'événement : " + e.getMessage());
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        // Vérification des champs obligatoires
        if (titreField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() ||
                dateDebutPicker.getValue() == null ||
                dateFinPicker.getValue() == null ||
                lieuField.getText().isEmpty() ||
                statutComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        // Vérification des dates
        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Dates invalides", "La date de fin doit être après la date de début.");
            return false;
        }

        // Suppression de la validation des coordonnées (latitude et longitude)
        // Les champs latitudeField et longitudeField ne sont plus vérifiés ici

        return true;
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    private void setupMap() {
        webEngine = mapView.getEngine();
        // Update user agent to include application identifier
        webEngine.setUserAgent("Inkspire-BMF/1.0 (JavaFX WebView; Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36)");
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> {
            showAlert(Alert.AlertType.ERROR, "Erreur WebView", "Erreur dans le WebView : " + event.getMessage());
        });

        URL resourceUrl = getClass().getResource("/Event/upmap.html");
        if (resourceUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier mapBack.html introuvable dans les ressources.");
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
                    if (eventToEdit != null && !Double.isNaN(eventToEdit.getLatitude()) && !Double.isNaN(eventToEdit.getLongitude()) &&
                            eventToEdit.getLatitude() != 0.0 && eventToEdit.getLongitude() != 0.0) {
                        String escapedLocation = eventToEdit.getLocation() != null ?
                                eventToEdit.getLocation().replace("'", "\\'") : "Lieu inconnu";
                        String script = String.format(java.util.Locale.US, "setMarker(%f, %f, '%s');",
                                eventToEdit.getLatitude(), eventToEdit.getLongitude(), escapedLocation);

                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                javafx.application.Platform.runLater(() -> {
                                    try {
                                        webEngine.executeScript(script);
                                    } catch (Exception e) {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exécution de setMarker : " + e.getMessage());
                                    }
                                });
                            } catch (InterruptedException e) {
                            }
                        }).start();
                    } else {
                        logger.warning("Invalid or missing coordinates for eventToEdit: lat=" +
                                (eventToEdit != null ? eventToEdit.getLatitude() : "null") +
                                ", lng=" + (eventToEdit != null ? eventToEdit.getLongitude() : "null"));
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation de JavaBridge : " + e.getMessage());
                    logger.severe("JavaBridge initialization error: " + e.getMessage());
                }
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la carte : " + webEngine.getLoadWorker().getException());
                logger.severe("Map loading failed: " + webEngine.getLoadWorker().getException());
            }
        });
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng, String address) {
            if (latitudeField == null || longitudeField == null || lieuField == null) {
                logger.warning("JavaBridge: Text fields are null");
                return;
            }
            if (!isUserClick) {
                logger.info("JavaBridge: Processing initial coordinates from setMarker: lat=" + lat + ", lng=" + lng + ", address=" + address);
            }

            if (address == null || address.trim().isEmpty()) {
                address = "Lieu inconnu";
                logger.warning("Empty address in JavaBridge, defaulting to: " + address);
            }

            latitudeField.setText(String.format(java.util.Locale.US, "%.6f", lat));
            longitudeField.setText(String.format(java.util.Locale.US, "%.6f", lng));
            lieuField.setText(address);

            logger.info("JavaBridge: Updated fields - Latitude=" + latitudeField.getText() +
                    ", Longitude=" + longitudeField.getText() +
                    ", Address=" + address);

            String highlightStyle = "-fx-background-color: lightgreen;";
            latitudeField.setStyle(highlightStyle);
            longitudeField.setStyle(highlightStyle);
            lieuField.setStyle(highlightStyle);

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        latitudeField.setStyle("");
                        longitudeField.setStyle("");
                        lieuField.setStyle("");
                    });
                } catch (InterruptedException e) {
                    logger.severe("JavaBridge highlight thread interrupted: " + e.getMessage());
                }
            }).start();
        }

    }}