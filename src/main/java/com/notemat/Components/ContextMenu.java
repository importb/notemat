package com.notemat.Components;

import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.InlineCssTextArea;

public class ContextMenu {
    private final InlineCssTextArea textArea;
    private final javafx.scene.control.ContextMenu contextMenu;

    public ContextMenu(InlineCssTextArea textArea) {
        this.textArea = textArea;
        this.contextMenu = new javafx.scene.control.ContextMenu();
        createContextMenu();
    }

    private void createContextMenu() {
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");

        // Bind menu actions to the text area's built-in methods.
        cut.setOnAction(e -> textArea.cut());
        copy.setOnAction(e -> textArea.copy());
        paste.setOnAction(e -> textArea.paste());

        contextMenu.getItems().addAll(cut, copy, paste);

        // Set the context menu on the text area so it shows up on right-click.
        textArea.setContextMenu(contextMenu);
    }

    public javafx.scene.control.ContextMenu getContextMenu() {
        return contextMenu;
    }
}
