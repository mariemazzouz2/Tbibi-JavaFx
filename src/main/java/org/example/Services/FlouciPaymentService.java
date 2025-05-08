package org.example.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.Entities.Commande;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlouciPaymentService {
    private static final String BASE_URL = "https://developers.flouci.com/api/";
    private static final String GENERATE_PAYMENT_ENDPOINT = "generate_payment";
    private static final String VERIFY_PAYMENT_ENDPOINT = "verify_payment";

    // Your tokens from Flouci
    private static final String APP_TOKEN = "8a9225db-338b-43a0-b2ee-6bbe150f7ea5";
    private static final String APP_SECRET = "244e0911-b564-4cb4-a151-08d889153081";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Track payment failure through URL navigation
    private AtomicBoolean paymentFailedFlag = new AtomicBoolean(false);

    public FlouciPaymentService() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a payment link
     * @param commande The order to be paid
     * @return Response containing the payment URL and payment ID
     */
    public Map<String, String> initiatePayment(Commande commande) throws IOException {
        // Reset payment failure flag
        paymentFailedFlag.set(false);

        // In development, use google.com URLs as they will always work
        // We'll intercept the navigation in our WebView
        String successUrl = "https://www.google.com/payment/success";
        String failUrl = "https://www.google.com/payment/fail";

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("app_token", APP_TOKEN);
        requestBody.put("app_secret", APP_SECRET);

        // Convert float to int millimes (multiply by 1000)
        int amountMillimes = (int)(commande.getMontantTotal() * 1000);
        requestBody.put("amount", String.valueOf(amountMillimes));
        requestBody.put("accept_card", "true");
        requestBody.put("session_timeout_secs", 1200);
        requestBody.put("success_link", successUrl);
        requestBody.put("fail_link", failUrl);

        // Use timestamp to ensure tracking ID is unique and long enough (10-50 chars)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String trackingId = "ORDER_" + commande.getId() + "_" + timestamp;
        requestBody.put("developer_tracking_id", trackingId);

        // Convert to JSON
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        System.out.println("Request Body: " + jsonBody);

        // Create request
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        Request request = new Request.Builder()
                .url(BASE_URL + GENERATE_PAYMENT_ENDPOINT)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = "";
            if (response.body() != null) {
                responseBody = response.body().string();
                System.out.println("Response Body: " + responseBody);
            }

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response + "\nBody: " + responseBody);
            }

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Extract payment URL and ID
            Map<String, String> result = new HashMap<>();
            result.put("paymentUrl", jsonResponse.path("result").path("link").asText());
            result.put("paymentId", jsonResponse.path("result").path("payment_id").asText());

            return result;
        }
    }

    /**
     * Verify payment status
     * @param paymentId Payment ID received from Flouci
     * @return true if payment is successful, false otherwise
     */
    public boolean verifyPayment(String paymentId) throws IOException {
        System.out.println("Verifying payment ID: " + paymentId);

        // First check if we've observed a failure through URL navigation
        if (paymentFailedFlag.get()) {
            System.out.println("Payment already marked as failed through navigation");
            return false;
        }

        // Create request with all required headers
        Request request = new Request.Builder()
                .url(BASE_URL + VERIFY_PAYMENT_ENDPOINT + "/" + paymentId)
                .addHeader("Content-Type", "application/json")
                .addHeader("apppublic", APP_TOKEN)
                .addHeader("appsecret", APP_SECRET)
                .get()
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            System.out.println("Verify Payment Response: " + responseBody);

            if (!response.isSuccessful()) {
                System.out.println("Payment verification failed with code: " + response.code());
                return false;
            }

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // First check if the response indicates success
            boolean isSuccessResponse = jsonResponse.path("success").asBoolean(false);

            if (!isSuccessResponse) {
                System.out.println("API response indicates failure");
                return false;
            }

            // Check payment status - the field is called "status" not "payment_status"
            String status = jsonResponse.path("result").path("status").asText("");
            System.out.println("Payment status: " + status);

            // Check if the payment was successful
            boolean isSuccess = "SUCCESS".equalsIgnoreCase(status) ||
                    "COMPLETED".equalsIgnoreCase(status) ||
                    "SUCCEEDED".equalsIgnoreCase(status);

            // If status is pending or not found, check our URL navigation flag as a fallback
            if (!isSuccess && (status.isEmpty() || "PENDING".equalsIgnoreCase(status))) {
                System.out.println("Payment status is pending or empty, checking navigation flag");
                isSuccess = !paymentFailedFlag.get();
            }

            return isSuccess;
        }
    }

    /**
     * Mark a payment as failed based on URL navigation
     */
    public void setPaymentFailedByNavigation() {
        System.out.println("Setting payment as failed based on navigation");
        paymentFailedFlag.set(true);
    }

    /**
     * Check if payment is marked as failed through navigation
     */
    public boolean isPaymentFailedByNavigation() {
        return paymentFailedFlag.get();
    }
    public Map<String, Object> checkPaymentStatus(String paymentId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("status", "ERROR");
        result.put("message", "Unknown error");

        try {
            // Create request with required headers
            Request request = new Request.Builder()
                    .url(BASE_URL + VERIFY_PAYMENT_ENDPOINT + "/" + paymentId)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("apppublic", APP_TOKEN)
                    .addHeader("appsecret", APP_SECRET)
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                System.out.println("Check Payment Status Response: " + responseBody);

                if (!response.isSuccessful()) {
                    result.put("message", "HTTP Error: " + response.code());
                    return result;
                }

                // Parse response
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                JsonNode resultNode = jsonResponse.path("result");

                // Get status information
                boolean apiSuccess = jsonResponse.path("success").asBoolean(false);
                String status = resultNode.path("status").asText("UNKNOWN");

                result.put("success", apiSuccess);
                result.put("status", status);
                result.put("message", "Status retrieved: " + status);
                result.put("rawResponse", responseBody);

                if (resultNode.has("receiver_wallet")) {
                    result.put("walletCode", resultNode.path("receiver_wallet").asText(""));
                }

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("message", "Exception: " + e.getMessage());
            return result;
        }
    }
}