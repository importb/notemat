package com.notemat.Components;

import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.InlineCssTextArea;

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

        // Actions
        cut.setOnAction(e -> textArea.cut());
        copy.setOnAction(e -> textArea.copy());
        paste.setOnAction(e -> editor.pasteTextOrImage());

        contextMenu.getItems().addAll(cut, copy, paste);
        textArea.setContextMenu(contextMenu);
    }

    public javafx.scene.control.ContextMenu getContextMenu() {
        return contextMenu;
    }
}
