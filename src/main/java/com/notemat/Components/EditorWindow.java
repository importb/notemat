package com.notemat.Components;

import com.notemat.Filesystem.NTMFile;
import com.notemat.Utils.KeyBindings;
import com.notemat.Utils.WindowResizing;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.File;
import java.io.IOException;

public class EditorWindow extends Stage {
    private final InlineCssTextArea richTextArea;
    private final Pane imageLayer;
    private final ToolBar toolBar;

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
        new KeyBindings(this, scene, richTextArea, styleBar);

        richTextArea.textProperty().addListener((obs, oldText, newText) -> {
            NTMFile.markChanged(toolBar);
        });
    }

    private void initImageLayer() {
        imageLayer.setPickOnBounds(false);

        // Image scrolling
        richTextArea.estimatedScrollYProperty().addListener((obs, oldVal, newVal) -> {
            imageLayer.setTranslateY(-newVal);
        });

        // Paste handler.
        richTextArea.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.V) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasImage()) {
                    Image clipboardImage = clipboard.getImage();
                    ImageComponent imageComponent = new ImageComponent(clipboardImage);
                    imageComponent.setManaged(false);
                    imageComponent.setLayoutX(10);
                    imageComponent.setLayoutY(10);
                    imageLayer.getChildren().add(imageComponent);
                    e.consume();
                }
            }
        });
    }

    public InlineCssTextArea getRichTextArea() {
        return richTextArea;
    }

    public Pane getImageLayer() {
        return imageLayer;
    }

    public void saveFile(boolean bypassAutoSave) {
        String lastSavedPath = NTMFile.getLastSavedPath();

        if (bypassAutoSave || lastSavedPath == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Notemat File (*.ntm)", "*.ntm"));
            // Open save dialog.
            File file = fileChooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                try {
                    NTMFile.saveToFile(this, file.getAbsolutePath());
                    toolBar.updateFilenameLabel();
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

    public void saveFile() {
        saveFile(false);
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Notemat File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Notemat File (*.ntm)", "*.ntm"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                NTMFile.loadFromFile(this, file.getAbsolutePath());
                toolBar.updateFilenameLabel();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
