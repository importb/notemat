package com.notemat.Components;

import com.notemat.Utils.AskAI;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.IOException;

public class ContextMenu {
    private final InlineCssTextArea textArea;
    private final javafx.scene.control.ContextMenu contextMenu;
    private final EditorWindow editor;

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
        ask.setOnAction(e -> {
            querySelectedTextToAI();
        });

        if (Preferences.getEnableGemini()) {
            contextMenu.getItems().addAll(cut, copy, paste, new SeparatorMenuItem(), ask);
        } else {
            contextMenu.getItems().addAll(cut, copy, paste);
        }
        textArea.setContextMenu(contextMenu);
    }

    public javafx.scene.control.ContextMenu getContextMenu() {
        return contextMenu;
    }

    private void querySelectedTextToAI() {
        try {
            // Get selection
            int selectionStart = textArea.getSelection().getStart();
            int selectionEnd = textArea.getSelection().getEnd();
            String currentStyle = textArea.getStyleAtPosition(selectionStart);
            String toAsk = textArea.getText(selectionStart, selectionEnd);

            if (!toAsk.isEmpty()) {
                String updatedStyle = editor.getStylebar().updateCssProperty(currentStyle, "-fx-fill", "#b380b3");

                String result = AskAI.askAI(toAsk);

                textArea.insertText(selectionEnd, "\n" + result);
                textArea.setStyle(selectionEnd, selectionEnd + result.length() + 1, updatedStyle);
                textArea.insertText(selectionEnd + result.length() + 1, " ");
                textArea.setStyle(selectionEnd + result.length() + 1, selectionEnd + result.length() + 2, currentStyle);
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
