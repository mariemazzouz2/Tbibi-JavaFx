package service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class GeminiRapport {

    private final HttpClient httpClient;
    private final String apiKey = "AIzaSyCVQHI_ArRIWqOmqDwi0D1cC5kBKXq-gVI"; // 🔴 Replace with your actual API key
    private final Logger logger;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=";
    private static final Map<String, String> LANGUAGE_CODES = new HashMap<>();

    static {
        LANGUAGE_CODES.put("french", "fr");
        LANGUAGE_CODES.put("english", "en");
        LANGUAGE_CODES.put("italian", "it");
    }

    public GeminiRapport(HttpClient httpClient, Logger logger) {
        this.httpClient = httpClient;
        this.logger = logger != null ? logger : LoggerFactory.getLogger(GeminiRapport.class);
    }

    public String answerMedicalQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return formatHumanResponse("Please provide a valid medical question.");
        }

        String prompt = """
                You are a friendly and knowledgeable medical assistant with expertise in healthcare and patient care. 
                Your task is to answer medical-related questions in a clear, empathetic, and conversational way, as if speaking to a patient or curious individual.

                ### Instructions:
                - Answer this question: "%s"
                - Use a warm, professional tone, avoiding complex medical jargon unless necessary, and explain terms if used.
                - If the question is vague or lacks detail, kindly ask for clarification, e.g., "Could you share more details so I can assist you better?"
                - If the question isn't medical, gently say, "I'm here to help with medical questions. Could you ask something health-related?"
                - Always include a note to consult a doctor for personalized advice, but weave it naturally into the response.
                - Do not provide diagnoses or specific treatment plans, as these require a doctor's evaluation.

                ### Guidelines:
                - Keep the response concise, natural, and easy to understand.
                - Avoid assumptions beyond the question.
                - Write as if you're having a friendly conversation, not a formal report.
                - Ensure answers are grounded in medical knowledge and best practices.

                ### Example:
                Question: "Why do I feel tired all the time?"
                Response: "Feeling tired constantly can be tough! It might be due to things like lack of sleep, stress, or even low iron levels. I'd suggest tracking your sleep and diet for a few days to see if there's a pattern. For a clear answer, it's best to check with a doctor who can run some tests. Anything specific about your tiredness you'd like to share?"

                Now, answer the question provided.
                """.formatted(question);

        return callGeminiAPI(prompt);
    }

    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return formatHumanResponse("Please provide some text to translate.");
        }

        if (!LANGUAGE_CODES.containsKey(sourceLanguage.toLowerCase()) ||
                !LANGUAGE_CODES.containsKey(targetLanguage.toLowerCase())) {
            return formatHumanResponse("Sorry, I only support translation between French, English, and Italian.");
        }

        String sourceLangCode = LANGUAGE_CODES.get(sourceLanguage.toLowerCase());
        String targetLangCode = LANGUAGE_CODES.get(targetLanguage.toLowerCase());

        String prompt = """
                You are an expert translator with fluency in French, English, and Italian. 
                Translate the following text from %s to %s:

                Text to translate: "%s"

                ### Instructions:
                - Provide only the translated text, without additional commentary or notes
                - Maintain the original meaning and tone
                - Preserve any technical or specific terms
                - Keep the same level of formality as the original
                - If the text contains idioms, use the closest equivalent in the target language
                - For medical terms, use the standard translation in the target language

                ### Example:
                Original (English): "You should consult a doctor if the symptoms persist."
                Translation (French): "Vous devriez consulter un médecin si les symptômes persistent."

                Now, provide the translation.
                """.formatted(sourceLanguage, targetLanguage, text);

        return callGeminiAPI(prompt);
    }

    private String callGeminiAPI(String prompt) {
        String url = API_URL + apiKey;
        int maxRetries = 3;
        int retryDelaySeconds = 2;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String jsonPayload = """
                        {
                            "contents": [
                                {
                                    "parts": [
                                        {
                                            "text": "%s"
                                        }
                                    ]
                                }
                            ]
                        }
                        """.formatted(prompt.replace("\"", "\\\""));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                String responseBody = response.body();

                logger.info("Gemini API Response (Attempt: {}), Status: {}, Response: {}", attempt, statusCode, responseBody);

                if (statusCode == 200 && responseBody.contains("\"candidates\"")) {
                    String text = extractTextFromResponse(responseBody);
                    if (text != null) {
                        return formatHumanResponse(text.trim().replace("\\n", "\n"));
                    }
                }

                logger.warn("Gemini API returned an unexpected response: {}", responseBody);

                if (attempt < maxRetries) {
                    Thread.sleep(retryDelaySeconds * 1000);
                    continue;
                }

                return formatHumanResponse("Sorry, I couldn't process your request right now. Please try again in a moment.");

            } catch (Exception e) {
                logger.error("Gemini API Exception (Attempt: {}): {}", attempt, e.getMessage(), e);

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelaySeconds * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("Retry interrupted", ie);
                    }
                    continue;
                }

                return formatHumanResponse("Oops, something went wrong on my end. Please try again later.");
            }
        }

        return formatHumanResponse("I'm having trouble processing that request. Could you try again?");
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray candidates = json.getJSONArray("candidates");
            if (!candidates.isEmpty()) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (!parts.isEmpty()) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to parse response: {}", e.getMessage());
            return null;
        }
    }

    private String formatHumanResponse(String rawResponse) {
        // Clean up the response for better human readability
        String formatted = rawResponse
                .replace("\"", "") // Remove quotes that might come from JSON
                .replace("\\n", "\n") // Ensure proper line breaks
                .replaceAll("\\s+", " ") // Collapse multiple spaces
                .trim();

        // Capitalize first letter and ensure proper punctuation
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
            if (!formatted.endsWith(".") && !formatted.endsWith("!") && !formatted.endsWith("?")) {
                formatted += ".";
            }
        }

        return formatted;
    }
}
// Explication Logique
//Séparation des responsabilités :
//
//GeminiRapport est un service dédié à l'interaction avec l'API Gemini
//
//Il sert de pont entre votre app et les capacités d'IA de Google
//
//La traduction est une des fonctions offertes par Gemini
//
//Avantages de cette architecture :
//
//Centralisation : Tous les appels à Gemini sont au même endroit
//
//Réutilisabilité : Le service peut être utilisé par d'autres contrôleurs
//
//Maintenance : Si l'API change, on modifie seulement GeminiRapport
//
//Pourquoi ne pas mettre la traduction directement dans le contrôleur ?
//
//Cela créerait de la duplication de code
//
//Le contrôleur deviendrait trop complexe
//
//Plus difficile à tester unitairement