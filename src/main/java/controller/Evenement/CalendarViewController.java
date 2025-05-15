package controller.Evenement;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarViewController {

    @FXML
    private WebView webView;

    private WebEngine webEngine;
    private List<Evenement> eventList;

    @FXML
    public void initialize() {
        System.out.println("Initializing CalendarViewController...");
        webEngine = webView.getEngine();

        String htmlContent = getCalendarHtml();
        webEngine.loadContent(htmlContent);

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("WebView load state: " + newValue);
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("WebView loaded successfully, loading events...");
                loadEvents();
            } else if (newValue == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("Failed to load WebView content");
                showErrorAlert("Erreur de chargement", "Impossible de charger le calendrier. Vérifiez votre connexion Internet.");
            }
        });

        webEngine.setOnError(event -> {
            System.err.println("JavaScript error: " + event.getMessage());
            showErrorAlert("Erreur JavaScript", "Une erreur s'est produite dans le calendrier: " + event.getMessage());
        });
    }

    public void setEventList(List<Evenement> eventList) {
        System.out.println("Setting event list with " + (eventList != null ? eventList.size() : 0) + " events");
        this.eventList = eventList;
        if (webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
            loadEvents();
        }
    }

    private String getCalendarHtml() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='utf-8' />
            <link href='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.css' rel='stylesheet' />
            <script src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.js'></script>
            <style>
                body {
                    background: #ffffff;
                    color: #212121;
                    font-family: "Segoe UI", Arial, sans-serif;
                    margin: 20px;
                }
                #calendar {
                    max-width: 700px;
                    margin: 0 auto;
                    background: #ffffff;
                    border-radius: 15px;
                    box-shadow: 0 6px 12px rgba(204, 204, 204, 0.2);
                    padding: 10px;
                }
                .fc-header-toolbar {
                    background: #ffffff;
                    border-radius: 15px 15px 0 0;
                    padding: 10px;
                    border-bottom: 2px solid #2196F3;
                }
                .fc-toolbar-title {
                    color: #212121;
                    font-weight: bold;
                    font-size: 20px;
                }
                .fc-button {
                    background: #2196F3 !important;
                    border: none !important;
                    color: #ffffff !important;
                    border-radius: 8px !important;
                    padding: 5px 10px !important;
                    font-weight: bold !important;
                    transition: background 0.3s;
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                }
                .fc-button:hover {
                    background: #1976D2 !important;
                    box-shadow: 0 4px 8px rgba(25, 118, 210, 0.3);
                }
                .fc-col-header-cell {
                    background: #f5f5f5;
                    color: #212121;
                    font-weight: bold;
                    border: 1px solid #eeeeee;
                }
                .fc-daygrid-day {
                    background: #ffffff;
                    border: 1px solid #eeeeee;
                    transition: background 0.2s, box-shadow 0.2s;
                }
                .fc-daygrid-day:hover {
                    background: #E3F2FD;
                    box-shadow: 0 4px 8px rgba(33, 150, 243, 0.2);
                }
                .fc-daygrid-day-number {
                    color: #212121;
                }
                .fc-day-today {
                    background: #2196F3 !important;
                }
                .fc-day-today .fc-daygrid-day-number {
                    color: #ffffff;
                    font-weight: bold;
                }
                .fc-event {
                    background: #2196F3;
                    color: #ffffff;
                    border: none;
                    border-radius: 5px;
                    padding: 2px 4px;
                    font-size: 12px;
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                }
                .fc-event:hover {
                    background: #1976D2;
                    box-shadow: 0 4px 8px rgba(25, 118, 210, 0.3);
                }
            </style>
        </head>
        <body>
            <div id='calendar'></div>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    try {
                        var calendarEl = document.getElementById('calendar');
                        if (!calendarEl) {
                            throw new Error("Calendar element not found");
                        }
                        window.calendar = new FullCalendar.Calendar(calendarEl, {
                            initialView: 'dayGridMonth',
                            headerToolbar: {
                                left: 'prev,next today',
                                center: 'title',
                                right: 'dayGridMonth,timeGridWeek,dayGridDay'
                            },
                            events: [],
                            eventClick: function(info) {
                                try {
                                    console.log("Event clicked in FullCalendar, ID: " + info.event.id);
                                    window.javaEventHandler.eventClicked(info.event.id);
                                } catch (e) {
                                    console.error("Error in eventClick: ", e);
                                }
                            }
                        });
                        window.calendar.render();
                    } catch (e) {
                        console.error("Error initializing FullCalendar: ", e);
                    }
                });

                function loadEvents(events) {
                    try {
                        if (!window.calendar) {
                            throw new Error("Calendar not initialized");
                        }
                        window.calendar.getEvents().forEach(event => event.remove());
                        events.forEach(event => window.calendar.addEvent(event));
                        window.calendar.render();
                    } catch (e) {
                        console.error("Error in loadEvents: ", e);
                    }
                }
            </script>
        </body>
        </html>
        """;
    }

    private void loadEvents() {
        System.out.println("Loading events into FullCalendar...");
        if (eventList == null || eventList.isEmpty()) {
            System.out.println("No events to load");
            return;
        }

        try {
            JSONArray eventsArray = new JSONArray();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Evenement event : eventList) {
                JSONObject eventJson = new JSONObject();
                eventJson.put("id", event.getId());
                eventJson.put("title", event.getTitre());
                eventJson.put("start", event.getDateDebut().format(formatter));
                eventJson.put("description", event.getDescription());
                eventJson.put("location", event.getLieu());
                eventsArray.put(eventJson);
            }

            System.out.println("Events JSON: " + eventsArray.toString());
            webEngine.executeScript("loadEvents(" + eventsArray.toString() + ")");

            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaEventHandler", new EventHandler());
            System.out.println("Events loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger les événements dans le calendrier: " + e.getMessage());
        }
    }

    public class EventHandler {
        public void eventClicked(String eventId) {
            System.out.println("Event clicked with ID: " + eventId);
            Evenement clickedEvent = eventList.stream()
                    .filter(e -> String.valueOf(e.getId()).equals(eventId))
                    .findFirst()
                    .orElse(null);

            if (clickedEvent != null) {
                System.out.println("Found event: " + clickedEvent.getTitre() + ", loading details...");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/EventDetails.fxml"));
                    Parent eventDetailsView = loader.load();
                    controller.Evenement.EvenementDetails controller = loader.getController();
                    controller.setEvenement(clickedEvent);

                    Stage stage = (Stage) webView.getScene().getWindow();
                    Scene scene = new Scene(eventDetailsView);
                    stage.setScene(scene);
                    stage.setTitle("Détails de l'événement");
                    stage.show();
                    System.out.println("Event details view displayed for event: " + clickedEvent.getTitre());
                } catch (IOException e) {
                    System.err.println("Error loading EventDetails.fxml: " + e.getMessage());
                    e.printStackTrace();
                    showErrorAlert("Erreur", "Échec du chargement des détails de l'événement: " + e.getMessage());
                }
            } else {
                System.out.println("No event found with ID: " + eventId);
                showErrorAlert("Erreur", "Aucun événement trouvé avec l'ID: " + eventId);
            }
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            System.out.println("Navigating back to AfficherEvent...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event/AfficherEvent.fxml"));
            Parent parent = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Event List");
            stage.show();
            System.out.println("Returned to AfficherEvent view");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Échec du retour à la liste des événements: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}