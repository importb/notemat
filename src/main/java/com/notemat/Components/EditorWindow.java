package com.notemat.Components;

import com.notemat.Filesystem.NTMFile;
import com.notemat.Utils.KeyBindings;
import com.notemat.Utils.WindowResizing;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class EditorWindow extends Stage {
    private final InlineCssTextArea richTextArea;
    private final Pane imageLayer;
    private final ToolBar toolBar;

    public EditorWindow(String filePath) {
        this();
        try {
            NTMFile.loadFromFile(this, filePath);
            toolBar.updateFilenameLabel();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public EditorWindow() {
        setTitle("Notemat");
        initStyle(StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();

        // Rich text area
        richTextArea = new InlineCssTextArea();
        richTextArea.setWrapText(true);
        richTextArea.setPrefSize(1024, 600);

        // Image layer
        imageLayer = new Pane();
        initImageLayer();

        // Stackpane that holds image layer and rta.
        StackPane centerStack = new StackPane();
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(centerStack.widthProperty());
        clip.heightProperty().bind(centerStack.heightProperty());
        centerStack.setClip(clip);

        centerStack.getChildren().addAll(richTextArea, imageLayer);
        root.setCenter(centerStack);

        // Toolbar
        toolBar = new ToolBar(this);

        // Stylebar
        StyleBar styleBar = new StyleBar(richTextArea);

        // VBox for toolbar and stylebar
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(toolBar, styleBar);
        root.setTop(topContainer);

        Scene scene = new Scene(root, 1024, 600);
        setScene(scene);

        richTextArea.setStyle(0, 0, styleBar.getStyleBarStyle());

        // Context menu
        new ContextMenu(richTextArea);

        // Load the theme.
        String css = getClass().getResource("/theme.css").toExternalForm();
        scene.getStylesheets().add(css);

        new WindowResizing(this);
        new KeyBindings(this, scene, richTextArea, styleBar, imageLayer);

        richTextArea.textProperty().addListener((obs, oldText, newText) -> {
            NTMFile.markChanged(toolBar);
        });

        richTextArea.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            Platform.runLater(() -> {
                String currentText = richTextArea.getText();
                int placeholderIndex = currentText.indexOf("\u200B");

                if (placeholderIndex != -1) {
                    richTextArea.replaceText(placeholderIndex, placeholderIndex + 1, "");
                    richTextArea.moveTo(placeholderIndex + 1);
                }
            });
        });
    }

    private void initImageLayer() {
        imageLayer.setPickOnBounds(false);

        // Image scrolling
        richTextArea.estimatedScrollYProperty().addListener((obs, oldVal, newVal) -> {
            imageLayer.setTranslateY(-newVal);
        });
    }

    public InlineCssTextArea getRichTextArea() {
        return richTextArea;
    }

    public Pane getImageLayer() {
        return imageLayer;
    }

    public void saveFile(String fileType, boolean bypassAutoSave) {
        String lastSavedPath = NTMFile.getLastSavedPath();

        if (bypassAutoSave || lastSavedPath == null) {
            String filePath = saveFileGetPath(fileType);

            if (filePath != null) {
                try {
                    NTMFile.saveToFile(this, filePath);

                    if (Objects.equals(fileType, "ntm")) {
                        toolBar.updateFilenameLabel();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                System.out.println("Saving to " + lastSavedPath);
                NTMFile.saveToFile(this, lastSavedPath);
                toolBar.updateFilenameLabel();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveFile(String fileType) {
        saveFile(fileType, false);
    }

    public void openFile(String fileType) {
        String filePath = openFileGetPath(fileType);

        if (filePath != null) {
            try {
                NTMFile.loadFromFile(this, filePath);
                if (Objects.equals(fileType, "ntm")) {
                    toolBar.updateFilenameLabel();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String openFileGetPath(String fileType) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File (*.%s)".formatted(fileType), "*.%s".formatted(fileType)));
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }

    public String saveFileGetPath(String fileType) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File (*.%s)".formatted(fileType), "*.%s".formatted(fileType)));
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }


}
