package com.notemat.Components;

import com.notemat.Filesystem.NTMFile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ToolBar extends BorderPane {
    private double xOffset = 0;
    private double yOffset = 0;
    private final EditorWindow editor;

    public ToolBar(EditorWindow editor) {
        this.editor = editor;
        getStyleClass().add("toolbar");
        enableWindowDragging();

        // Create the MenuBar with File and Edit menus
        MenuBar menuBar = new MenuBar();

        // Create "File" menu with basic menu items.
        Menu fileMenu = new Menu("File");

        MenuItem openFile = new MenuItem("Open");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem exitItem = new MenuItem("Exit");

        enableLoadingSaving(saveFile, openFile);

        // Add items
        fileMenu.getItems().addAll(openFile, saveFile, new SeparatorMenuItem(), exitItem);

        // Create "Edit" menu with basic menu items.
        Menu editMenu = new Menu("Edit");

        MenuItem undoItem = new MenuItem("Undo");
        MenuItem redoItem = new MenuItem("Redo");
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        addEditMenuFunctions(undoItem, redoItem, cutItem, copyItem, pasteItem);
        editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(), cutItem, copyItem, pasteItem);

        // Add menus to the MenuBar.
        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Make the MenuBar fill available space
        HBox.setHgrow(menuBar, Priority.ALWAYS);

        // Create window control buttons
        HBox windowControls = new HBox(5);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.setPadding(new Insets(1, 5, 1, 5));

        // Create Minimize button
        Button minimizeButton = new Button("-");
        minimizeButton.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.setIconified(true);
        });

        // Create Close button
        Button closeButton = new Button("X");
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
        });

        // Add buttons to HBox
        windowControls.getChildren().addAll(minimizeButton, closeButton);

        // Use BorderPane to position components
        setLeft(menuBar);
        setRight(windowControls);

        // Set mouse event handlers for the entire BorderPane
        enableWindowDragging();
    }

    public void enableWindowDragging() {
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

    public void enableLoadingSaving(MenuItem saveFile, MenuItem openFile) {
        saveFile.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Notemat File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Notemat File (*.ntm)", "*.ntm")
            );
            // Open save dialog.
            File file = fileChooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                try {
                    // Save the state of the EditorWindow.
                    NTMFile.saveToFile(editor, file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        openFile.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Notemat File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Notemat File (*.ntm)", "*.ntm")
            );
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                try {
                    NTMFile.loadFromFile(editor, file.getAbsolutePath());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addEditMenuFunctions(MenuItem undoItem, MenuItem redoItem, MenuItem cutItem, MenuItem copyItem, MenuItem pasteItem) {
        undoItem.setOnAction(event -> {editor.getRichTextArea().undo();});
        redoItem.setOnAction(event -> {editor.getRichTextArea().redo();});
        cutItem.setOnAction(event -> {editor.getRichTextArea().cut();});
        copyItem.setOnAction(event -> {editor.getRichTextArea().copy();});
        pasteItem.setOnAction(event -> {editor.getRichTextArea().paste();});
    }
}