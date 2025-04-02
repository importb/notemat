package com.notemat.Components;

import com.google.genai.types.GenerateContentResponse;
import com.notemat.Utils.Gemini;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.http.HttpException;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.IOException;
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

        // Actions
        cut.setOnAction(e -> textArea.cut());
        copy.setOnAction(e -> textArea.copy());
        paste.setOnAction(e -> editor.pasteTextOrImage());
        ask.setOnAction(e -> querySelectedTextToAI());

        if (Preferences.getEnableGemini()) {
            contextMenu.getItems().addAll(cut, copy, paste, new SeparatorMenuItem(), ask);
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
            String updatedStyle = editor.getStylebar().updateCssProperty(currentStyle, "-fx-fill", "#b380b3");

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
                        // Remove the "Generating..." text if it is still present.
                        textArea.deleteText(selectionEnd, generatingEnd);

                        // Insert the generated response.
                        String responseText = "\n" + response.text();
                        textArea.insertText(selectionEnd, responseText);
                        textArea.setStyle(selectionEnd, selectionEnd + responseText.length(), updatedStyle);
                        textArea.insertText(selectionEnd + responseText.length(), " ");
                        textArea.setStyle(selectionEnd + responseText.length(), selectionEnd + responseText.length() + 1, currentStyle);
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
