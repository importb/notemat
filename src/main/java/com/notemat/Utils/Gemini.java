package com.notemat.Utils;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.google.genai.Client;

import java.io.IOException;
import java.util.Objects;

import com.google.genai.types.*;
import com.notemat.Components.Preferences;
import org.apache.http.HttpException;

public class Gemini {
    private Client client;
    private GenerateContentConfig config;

    public Gemini() {
        String apiKey = Preferences.getGeminiApi();

        client = Client.builder().apiKey(apiKey).build();

        String systemPrompt = """
                You are "Notemat AI", an AI assistant. Your role is to assist and engage in conversation while being helpful, respectful, and engaging.

                - If you are specifically asked about the model you are using, you may mention it. If you are not asked specifically about the model you are using, you do not need to mention it.

                - Output Formatting Constraint: You must generate responses using exclusively plain text. Absolutely no text styling or formatting elements are allowed. This includes, but is not limited to:
                    - Markdown formatting (e.g., **, __, *, _, ```, `, #, -, *, 1., >, etc.)
                    - HTML tags
                    - Bold, italics, underline, strikethrough
                    - Bullet points or numbered lists
                    - Code blocks or inline code
                    - Headings or horizontal rules
                - Your entire response must consist solely of unformatted text characters.
                """;



        Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));
        Tool googleSearchTool = Tool.builder().googleSearch(GoogleSearch.builder().build()).build();
        config = GenerateContentConfig.builder().candidateCount(1).maxOutputTokens(10240).systemInstruction(systemInstruction).tools(ImmutableList.of(googleSearchTool)).build();
    }

    private String formatModel(String model) {
        if (Objects.equals(model, "Gemini 2.0 Flash")) return "gemini-2.0-flash";
        if (Objects.equals(model, "Gemini 2.0 Flash-Lite")) return "gemini-2.0-flash-lite";
        if (Objects.equals(model, "Gemini 2.5 Pro")) return "gemini-2.5-pro-exp-03-25";

        return "gemini-2.0-flash-lite";
    }

    public GenerateContentResponse getResponse(String model, String query) throws HttpException, IOException {
        return client.models.generateContent(formatModel(model), query, config);
    }
}