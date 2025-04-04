package com.notemat.Components;

import com.google.genai.types.GenerateContentResponse;
import com.notemat.Utils.Gemini;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.http.HttpException;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * Custom context menu for the editor, providing basic editing options and
 * an AI query option based on the selected text.
 */
public class ContextMenu {
    private final InlineCssTextArea textArea;
    private final javafx.scene.control.ContextMenu contextMenu;
    private final EditorWindow editor;
    private final Gemini gemini = new Gemini();

    /**
     * Constructor to initialize the context menu.
     *
     * @param editor   Main editor window.
     * @param textArea Main text area.
     */
    public ContextMenu(EditorWindow editor, InlineCssTextArea textArea) {
        this.editor = editor;
        this.textArea = textArea;
        this.contextMenu = new javafx.scene.control.ContextMenu();
        createContextMenu();
    }

    private void createContextMenu() {
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        MenuItem ask = new MenuItem("Ask Gemini");
        MenuItem debug = new MenuItem("debug");

        // Actions
        cut.setOnAction(e -> textArea.cut());
        copy.setOnAction(e -> textArea.copy());
        paste.setOnAction(e -> editor.pasteTextOrImage());
        ask.setOnAction(e -> querySelectedTextToAI());

        if (Preferences.getEnableGemini()) {
            contextMenu.getItems().addAll(cut, copy, paste, new SeparatorMenuItem(), ask, debug);
        } else {
            contextMenu.getItems().addAll(cut, copy, paste);
        }
        textArea.setContextMenu(contextMenu);
    }

    private void querySelectedTextToAI() {
        // Get selection area, style and text.
        int selectionStart = textArea.getSelection().getStart();
        int selectionEnd = textArea.getSelection().getEnd();
        String currentStyle = textArea.getStyleAtPosition(selectionStart);
        String toAsk = textArea.getText(selectionStart, selectionEnd);

        if (!toAsk.isEmpty()) {
            // Change the color to light purple.
            String updatedStyle = editor.getStylebar().updateCssProperty(currentStyle, "-fx-fill", "#b380b3").replace("-fx-font-weight: bold", "-fx-font-style: normal");
            String updatedStyleBold = updatedStyle.replace("-fx-font-style: normal", "-fx-font-weight: bold");

            System.out.println(updatedStyle);

            // Insert "Generating..." and record its start/end positions.
            String generating = "Generating...";
            String generatingText = "\n" + generating;
            textArea.insertText(selectionEnd, generatingText);
            int generatingEnd = selectionEnd + generatingText.length();
            textArea.setStyle(selectionEnd, generatingEnd, updatedStyle);
            textArea.insertText(generatingEnd, " ");
            textArea.setStyle(generatingEnd, generatingEnd + 1, currentStyle);

            // Query Gemini.
            try {
                CompletableFuture<GenerateContentResponse> resp = gemini.getResponse(Preferences.getGeminiModel(), toAsk);

                resp.thenAccept(response -> {
                    Platform.runLater(() -> {
                        // Get the raw response text (including the newline).
                        String rawResponse = "\n" + response.text();
                        StringBuilder processedResponse = new StringBuilder();

                        // List to keep track of bold regions. Each element is a 2-element int array: [startIndex, endIndex] relative to the final inserted text.
                        List<int[]> boldRanges = new ArrayList<>();

                        // Process rawResponse to remove the "**" markers.
                        for (int i = 0; i < rawResponse.length(); ) {
                            // Check if the current position starts with "**"
                            if (rawResponse.startsWith("**", i)) {
                                int boldStart = i + 2;
                                int boldEnd = rawResponse.indexOf("**", boldStart);

                                // If no matching closing "**" is found, just append the rest.
                                if (boldEnd == -1) {
                                    processedResponse.append(rawResponse.substring(i));
                                    break;
                                }
                                // Record where the bold text will start within the processed text.
                                int startBoldInProcessed = processedResponse.length();
                                // Append the text between the markers.
                                String boldText = rawResponse.substring(boldStart, boldEnd);
                                processedResponse.append(boldText);
                                // Record the end index.
                                int endBoldInProcessed = processedResponse.length();
                                // Save the bold region (we will need to adjust the absolute offset
                                // later when the text is inserted into the textArea).
                                boldRanges.add(new int[]{startBoldInProcessed + selectionEnd, endBoldInProcessed + selectionEnd});
                                // Move past the markers.
                                i = boldEnd + 2;
                            } else {
                                // Append the normal character.
                                processedResponse.append(rawResponse.charAt(i));
                                i++;
                            }
                        }

                        // Remove the "Generating..." text if it is still present.
                        textArea.deleteText(selectionEnd, generatingEnd);

                        // Insert the processed text into the textArea.
                        String finalResponse = processedResponse.toString();
                        textArea.insertText(selectionEnd, finalResponse);

                        // First, apply the default updatedStyle to the entire inserted text.
                        textArea.setStyle(selectionEnd, selectionEnd + finalResponse.length(), updatedStyle);

                        // Then, for each bold segment that we detected, update its style.
                        for (int[] range : boldRanges) {
                            textArea.setStyle(range[0], range[1], updatedStyleBold);
                        }
                    });
                });

            } catch (HttpException | IOException e) {
                // Remove the "Generating..." text if it's still there.
                textArea.deleteText(selectionEnd, generatingEnd);
                String result = "Error generating a response.";
                String errorText = "\n" + result;
                textArea.insertText(selectionEnd, errorText);
                textArea.setStyle(selectionEnd, selectionEnd + errorText.length(), updatedStyle);
                textArea.insertText(selectionEnd + errorText.length(), " ");
                textArea.setStyle(selectionEnd + errorText.length(), selectionEnd + errorText.length() + 1, currentStyle);
            }
        }
    }


    /**
     * Returns the ContextMenu instance.
     *
     * @return The context menu.
     */
    public javafx.scene.control.ContextMenu getContextMenu() {
        return contextMenu;
    }
}
