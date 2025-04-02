package com.notemat.Components;

import com.notemat.Utils.AskAI;
import com.notemat.Utils.Gemini;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.http.HttpException;
import org.fxmisc.richtext.InlineCssTextArea;
import java.io.IOException;


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
     * @param editor    Main editor window.
     * @param textArea  Main text area.
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

            // Query Gemini.
            String result = "Error generating a response.";
            try {
                result = gemini.getResponse(Preferences.getGeminiModel(), toAsk).text();
            } catch (HttpException | IOException e) {
                // fancy error catch or smth.
            }

            // Insert the response with a custom style, to the end add a space with the old styling.
            textArea.insertText(selectionEnd, "\n" + result);
            textArea.setStyle(selectionEnd, selectionEnd + result.length() + 1, updatedStyle);
            textArea.insertText(selectionEnd + result.length() + 1, " ");
            textArea.setStyle(selectionEnd + result.length() + 1, selectionEnd + result.length() + 2, currentStyle);
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
