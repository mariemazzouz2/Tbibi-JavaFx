package utils;

import entities.Prediction;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {
    private static final String API_URL = "http://127.0.0.1:5000/predict";

    public boolean predictDiabetes(Prediction prediction, int age, String gender) throws Exception {
        // Create the JSON object with one-hot encoded fields
        JSONObject data = new JSONObject();

        // Add numeric fields
        data.put("age", age);
        data.put("hypertension", prediction.isHypertension() ? 1 : 0);
        data.put("heart_disease", prediction.isheart_disease() ? 1 : 0);
        data.put("bmi", prediction.getBmi());
        data.put("HbA1c_level", prediction.gethbA1c_level());
        data.put("blood_glucose_level", prediction.getBloodGlucoseLevel());

        // One-hot encode gender
        data.put("gender_Female", gender.equalsIgnoreCase("Female") ? 1 : 0);
        data.put("gender_Male", gender.equalsIgnoreCase("Male") ? 1 : 0);
        data.put("gender_Other", gender.equalsIgnoreCase("Other") ? 1 : 0);

        // One-hot encode smoking_history
        String smokingHistory = prediction.getsmoking_history() != null ? prediction.getsmoking_history() : "No Info";
        data.put("smoking_history_No Info", smokingHistory.equals("No Info") ? 1 : 0);
        data.put("smoking_history_current", smokingHistory.equals("current") ? 1 : 0);
        data.put("smoking_history_ever", smokingHistory.equals("ever") ? 1 : 0);
        data.put("smoking_history_former", smokingHistory.equals("former") ? 1 : 0);
        data.put("smoking_history_never", smokingHistory.equals("never") ? 1 : 0);
        data.put("smoking_history_not current", smokingHistory.equals("not current") ? 1 : 0);

        // Print the JSON data for debugging
        System.out.println("JSON data being sent to Flask API: " + data.toString());

        // Send the request to the Flask API
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = data.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Erreur lors de la prédiction : " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getInt("prediction") == 1;
        } finally {
            conn.disconnect();
        }
    }
}