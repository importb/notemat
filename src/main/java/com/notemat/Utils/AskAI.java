package com.notemat.Utils;

import com.notemat.Components.Preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Provides functionality to interact with the Gemini API.
 * This class sends a prompt to the API and returns the AI-generated response.
 */
public class AskAI {
    // Extra instructions to be appended to the user's prompt.
    private static final String INPUT_EXTRA = "\nKeep your answer short, simple and factual. Don't use any formatting. Your prompt is: ";

    /**
     * Sends a request to the Gemini API with the provided input text.
     * Uses the API key from Preferences.
     *
     * @param input The prompt to send to the AI.
     * @return The raw response from the API as a String.
     * @throws IOException if an error occurs during the HTTP request.
     */
    public static String askAI(String input) throws IOException {
        input = filterString(input);

        // Get the API key
        String apiKey = Preferences.getGeminiApi();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is not set in preferences.");
        }

        // Build the URL
        String model = Preferences.getGeminiModel().toLowerCase();
        String urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-" + model + ":generateContent?key=" + apiKey;

        InputStream is;
        try {
            is = getInputStream(input, urlString);
        } catch (URISyntaxException e) {
            return "Error when generating a response.";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        // Get text.
        JSONObject jsonResponse = new JSONObject(response.toString());

        try {
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (!candidates.isEmpty()) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (!parts.isEmpty()) {
                    return parts.getJSONObject(0).getString("text").trim();
                }
            }
        } catch (Exception e) {
            return "Error when generating a response.";
        }
        return "Error when generating a response.";
    }

    /**
     * Creates an HTTP POST request to the specified URL with the given input.
     * Constructs the JSON payload and returns the appropriate input stream for the response.
     *
     * @param input     The prompt text to send.
     * @param urlString The API endpoint URL.
     * @return The input stream from the connection (input or error stream).
     * @throws IOException if an I/O error occurs.
     */
    private static InputStream getInputStream(String input, String urlString) throws IOException, URISyntaxException {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the request.
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Construct the JSON payload.
        String jsonInputString = String.format("{" + "\"contents\": [{" + "\"parts\": [{" + "\"text\": \"%s\"" + "}]" + "}]" + "}", INPUT_EXTRA + input);

        // Send the request body.
        try (OutputStream os = connection.getOutputStream()) {
            byte[] inputBytes = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(inputBytes, 0, inputBytes.length);
        }

        // Read the response.
        int responseCode = connection.getResponseCode();
        return (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();
    }

    /**
     * Filters the input string by replacing double quotes with single quotes.
     *
     * @param input The original input string.
     * @return The filtered string.
     */
    private static String filterString(String input) {
        String result;

        result = input.replaceAll("\"", "'");

        return result;
    }
}
