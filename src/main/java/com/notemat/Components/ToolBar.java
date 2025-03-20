package com.notemat.Components;

import com.notemat.Filesystem.NTMFile;
import com.notemat.Filesystem.TXTFile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ToolBar extends BorderPane {
    private double xOffset = 0;
    private double yOffset = 0;
    private final EditorWindow editor;
    private final Label filenameLabel;

    public ToolBar(EditorWindow editor) {
        this.editor = editor;
        getStyleClass().add("toolbar");
        enableWindowDragging();

        MenuBar menuBar = new MenuBar();

        // File menu.
        Menu fileMenu = new Menu("File");

        MenuItem openFile = new MenuItem("Open");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem saveAsFile = new MenuItem("Save As");
        MenuItem exitItem = new MenuItem("Exit");
        enableLoadingSaving(saveFile, saveAsFile, openFile);

        // Import submenu.
        Menu importMenu = new Menu("Import");
        MenuItem importTxt = new MenuItem("Import .txt");
        importMenu.getItems().addAll(importTxt);

        // Export submenu.
        Menu exportMenu = new Menu("Export");
        MenuItem exportTxt = new MenuItem("Export to .txt");
        exportMenu.getItems().addAll(exportTxt);

        fileMenu.getItems().addAll(
                openFile,
                saveFile,
                saveAsFile,
                new SeparatorMenuItem(),
                importMenu,
                exportMenu,
                new SeparatorMenuItem(),
                exitItem
        );

        enableAlternativeLoadingSaving(importTxt, exportTxt);

        // Edit menu.
        Menu editMenu = new Menu("Edit");

        MenuItem undoItem = new MenuItem("Undo");
        MenuItem redoItem = new MenuItem("Redo");
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        addEditMenuFunctions(undoItem, redoItem, cutItem, copyItem, pasteItem);
        editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(), cutItem, copyItem, pasteItem);

        // Settings menu.
        Menu settingsMenu = new Menu("Settings");

        MenuItem preferencesItems = new MenuItem("Preferences");
        preferencesItems.setOnAction(event -> {
            editor.preferences.showAndWait();
        });

        settingsMenu.getItems().addAll(preferencesItems);


        // Add menus to the MenuBar.
        menuBar.getMenus().addAll(fileMenu, editMenu, settingsMenu);
        HBox.setHgrow(menuBar, Priority.ALWAYS);

        // Create filename label
        filenameLabel = new Label(getDisplayFilename());
        filenameLabel.setAlignment(Pos.CENTER);
        filenameLabel.setPadding(new Insets(0, 10, 0, 10));
        filenameLabel.getStyleClass().add("filenameLabel");
        HBox filenameBox = new HBox(filenameLabel);
        filenameBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(filenameBox, Priority.ALWAYS);

        // Create window control buttons
        HBox windowControls = new HBox(5);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.setPadding(new Insets(1, 5, 1, 5));

        Button minimizeButton = new Button("-");  // minimize
        minimizeButton.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setIconified(true);
        });

        Button closeButton = new Button("X");  // close
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });

        windowControls.getChildren().addAll(minimizeButton, closeButton);

        // Position components.
        setLeft(menuBar);
        setCenter(filenameBox);
        setRight(windowControls);

        // Mouse event handlers.
        enableWindowDragging();
    }

    private void enableWindowDragging() {
        // Dragging
        setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private void enableLoadingSaving(MenuItem saveFile, MenuItem saveAsFile, MenuItem openFile) {
        saveFile.setOnAction(event -> {
            editor.saveFile("ntm");
        });

        saveAsFile.setOnAction(event -> {
            editor.saveFile("ntm", true);
        });

        openFile.setOnAction(event -> {
            editor.openFile("ntm");
        });
    }

    private void enableAlternativeLoadingSaving(MenuItem importTxt, MenuItem exportTxt) {
        importTxt.setOnAction(event -> {
            String filePath = editor.openFileGetPath("txt");

            if (filePath != null) {
                try {
                    TXTFile.importFromFile(editor, filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        exportTxt.setOnAction(event -> {
            String filePath = editor.saveFileGetPath("txt");

            if (filePath != null) {
                try {
                    TXTFile.saveToFile(editor, filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void addEditMenuFunctions(MenuItem undoItem, MenuItem redoItem, MenuItem cutItem, MenuItem copyItem, MenuItem pasteItem) {
        undoItem.setOnAction(event -> {
            editor.getRichTextArea().undo();
        });
        redoItem.setOnAction(event -> {
            editor.getRichTextArea().redo();
        });
        cutItem.setOnAction(event -> {
            editor.getRichTextArea().cut();
        });
        copyItem.setOnAction(event -> {
            editor.getRichTextArea().copy();
        });
        pasteItem.setOnAction(event -> {
            editor.getRichTextArea().paste();
        });
    }

    public void updateFilenameLabel() {
        filenameLabel.setText(getDisplayFilename());
    }

    private String getDisplayFilename() {
        boolean changedSinceLastSave = NTMFile.getChangedSinceLastSave();

        String lastSavedPath = NTMFile.getLastSavedPath();
        String result = lastSavedPath != null ? new File(lastSavedPath).getName() : "Untitled";

        if (changedSinceLastSave) {
            return "*" + result;
        } else {
            return result;
        }
    }
}