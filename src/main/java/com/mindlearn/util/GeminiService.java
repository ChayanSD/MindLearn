package com.mindlearn.util;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeminiService {

    private static final Logger LOGGER = Logger.getLogger(GeminiService.class.getName());
    private static final String PROPERTIES_FILE = "gemini.properties";

    private static String apiKey;
    private static String model;
    private static Client client;

    static {
        loadProperties();
        initializeClient();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = GeminiService.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                LOGGER.severe("Unable to find " + PROPERTIES_FILE);
                return;
            }
            props.load(input);
            apiKey = props.getProperty("gemini.api.key");
            model = props.getProperty("gemini.model", "gemini-1.5-flash-latest");
            LOGGER.info("Gemini configuration loaded successfully");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Gemini configuration", e);
        }
    }

    private static void initializeClient() {
        if (apiKey != null && !apiKey.isEmpty()) {
            client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            LOGGER.info("Google Gen AI Client initialized");
        } else {
            LOGGER.severe("Gemini API key is missing. Client initialization failed.");
        }
    }

    public static String generateMindmap(String topic) throws IOException {
        if (client == null) {
            throw new IOException("Gemini client not initialized");
        }

        String prompt = buildPrompt(topic);
        LOGGER.info("Generating mindmap for topic: " + topic);

        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7F)
                    .maxOutputTokens(2048)
                    .topP(0.9F)
                    .topK(40F)
                    .build();

            GenerateContentResponse response = client.models.generateContent(model, prompt, config);
            
            if (response == null || response.text() == null) {
                throw new IOException("Empty response received from Gemini SDK");
            }

            String text = response.text();
            LOGGER.info("Successfully received response from Gemini API");
            return cleanJsonString(text);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calling Gemini SDK", e);
            throw new IOException("Failed to generate mindmap: " + e.getMessage(), e);
        }
    }

    private static String buildPrompt(String topic) {
        return "Create a structured mindmap for the topic: " + topic + "\n" +
                "Return ONLY valid JSON in this exact format (no additional text, no markdown code blocks):\n" +
                "{\n" +
                "  \"topic\": \"TOPIC_NAME\",\n" +
                "  \"subtopics\": [\n" +
                "    {\n" +
                "      \"title\": \"SUBTOPIC_TITLE\",\n" +
                "      \"items\": [\"ITEM1\", \"ITEM2\", \"ITEM3\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Include 3-5 subtopics with 3-5 items each.";
    }

    private static String cleanJsonString(String text) {
        text = text.trim();
        // Remove markdown code blocks if present
        if (text.startsWith("```")) {
            // Find the end of the first line (e.g., ```json)
            int firstNewline = text.indexOf('\n');
            if (firstNewline != -1) {
                text = text.substring(firstNewline).trim();
            } else {
                text = text.substring(3).trim();
            }
            
            // Remove the closing backticks
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3).trim();
            }
        }
        return text;
    }
}