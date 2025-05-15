package utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class GoogleApiUtil {
    private static final HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME;
    private static final String CREDENTIALS_PATH;
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    // Private constructor to prevent instantiation
    private GoogleApiUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        try {
            // Load configuration
            Properties props = new Properties();
            try (InputStream input = GoogleApiUtil.class.getClassLoader().getResourceAsStream("google_api_config.properties")) {
                if (input == null) {
                    throw new RuntimeException("Could not find google_api_config.properties");
                }
                props.load(input);
                APPLICATION_NAME = props.getProperty("google.application.name");
                CREDENTIALS_PATH = props.getProperty("google.credentials.path");
            }

            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Google API utilities: " + e.getMessage(), e);
        }
    }

    /**
     * Create a Google Meet link for a consultation
     *
     * @param consultationTitle The title of the consultation
     * @param startTime The start time of the consultation
     * @param endTime The end time of the consultation
     * @return The Google Meet link
     * @throws Exception if creation fails
     */
    public static String createGoogleMeetLink(String consultationTitle, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        try {
            System.out.println("Creating Google Meet link for: " + consultationTitle);
            System.out.println("Start time: " + startTime);
            System.out.println("End time: " + endTime);
            
            // Check if credentials file exists
            File credFile = new File(CREDENTIALS_PATH);
            if (!credFile.exists()) {
                throw new Exception("Credentials file not found at: " + credFile.getAbsolutePath());
            }
            System.out.println("Found credentials file at: " + credFile.getAbsolutePath());
            
            Calendar service = getCalendarService();
            System.out.println("Calendar service created successfully");

            Event event = new Event()
                    .setSummary(consultationTitle)
                    .setDescription("Consultation virtuelle via Google Meet");

            DateTime startDateTime = new DateTime(java.sql.Timestamp.valueOf(startTime));
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Africa/Tunis");

            DateTime endDateTime = new DateTime(java.sql.Timestamp.valueOf(endTime));
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Africa/Tunis");

            event.setStart(start);
            event.setEnd(end);

            System.out.println("Event configured with times: " + startDateTime + " to " + endDateTime);

            // Add Google Meet conferencing
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(new CreateConferenceRequest()
                            .setRequestId(UUID.randomUUID().toString()));

            event.setConferenceData(conferenceData);

            System.out.println("Attempting to create calendar event with Meet...");
            // Insert the event with conferencing
            Event createdEvent = service.events()
                    .insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute();

            String meetLink = createdEvent.getHangoutLink();
            System.out.println("Created event. Meet link: " + meetLink);
            
            if (meetLink == null || meetLink.isEmpty()) {
                throw new Exception("Google Meet link was not generated");
            }
            return meetLink;
        } catch (Exception e) {
            System.err.println("Failed to create Google Meet link: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to create Google Meet link: " + e.getMessage(), e);
        }
    }

    private static Calendar getCalendarService() throws Exception {
        GoogleCredentials credentials = getCredentials();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static GoogleCredentials getCredentials() throws Exception {
        try (InputStream credentialsStream = new FileInputStream(CREDENTIALS_PATH)) {
            return GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(SCOPES);
        } catch (IOException e) {
            throw new Exception("Failed to load Google credentials: " + e.getMessage(), e);
        }
    }

    /**
     * Test the Google Calendar API connection
     *
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Calendar service = getCalendarService();
            // Try to list one event to test the connection
            service.events().list("primary").setMaxResults(1).execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}