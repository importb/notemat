package com.notemat.Components;

import com.notemat.Filesystem.NTMFile;
import com.notemat.Utils.KeyBindings;
import com.notemat.Utils.WindowResizing;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.InlineCssTextArea;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;


/**
 * The EditorWindow class represents the main editor window for the Notemat
 * application. It extends JavaFX's Stage and provides a rich text editor along
 * with image support, toolbars, and context menu functionality.
 */
public class EditorWindow extends Stage {
    private final InlineCssTextArea richTextArea;
    private final Pane imageLayer;
    private final ToolBar toolBar;
    private final StyleBar styleBar;
    public final Preferences preferences;

    private ContextMenu contextMenu;

    /**
     * Constructs an EditorWindow by loading content from a file.
     *
     * @param filePath the path of the file to load.
     */
    public EditorWindow(String filePath) {
        this();
        try {
            NTMFile.loadFromFile(this, filePath);
            toolBar.updateFilenameLabel();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor to initialize the EditorWindow with UI components,
     * bindings, and event handlers.
     */
    public EditorWindow() {
        setTitle("Notemat");
        initStyle(StageStyle.TRANSPARENT);

        // Initialize the preferences.
        preferences = new Preferences(this);

        // Main layout container
        BorderPane root = new BorderPane();

        // Rich text area component.
        richTextArea = new InlineCssTextArea();
        richTextArea.setWrapText(true);
        richTextArea.setPrefSize(1024, 600);
        initRichTextArea();

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

        // Toolbar and stylebar
        toolBar = new ToolBar(this);
        styleBar = new StyleBar(richTextArea);
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(toolBar, styleBar);
        root.setTop(topContainer);

        // Main scene
        Scene scene = new Scene(root, 1024, 600);
        setScene(scene);

        // Initial style.
        richTextArea.setStyle(0, 0, styleBar.getStyleBarStyle());

        // Load the theme.
        String css = getClass().getResource("/theme.css").toExternalForm();
        String fontsCss = getClass().getResource("/fonts.css").toExternalForm();
        scene.getStylesheets().addAll(fontsCss, css);

        // Other components.
        contextMenu = new ContextMenu(this, richTextArea);
        new WindowResizing(this);
        new KeyBindings(this, scene, richTextArea, styleBar, imageLayer);

        setOnCloseRequest(event -> {
            event.consume();
            showCloseConfirmation();
        });
    }

    /**
     * Initializes the rich text area by adding a listener to track text changes
     * and an event filter to remove any placeholder characters.
     */
    private void initRichTextArea() {
        // Listen for text changes to mark the document as changed.
        richTextArea.textProperty().addListener((obs, oldText, newText) -> NTMFile.markChanged(toolBar));

        // Filter key typed events to remove the style applier placeholder character.
        richTextArea.addEventFilter(KeyEvent.KEY_TYPED, event -> Platform.runLater(() -> {
            String currentText = richTextArea.getText();
            int placeholderIndex = currentText.indexOf("\u200B");

            if (placeholderIndex != -1) {
                int selectionStart = richTextArea.getSelection().getStart();

                richTextArea.replaceText(placeholderIndex, placeholderIndex + 1, "");

                if (selectionStart - 2 == placeholderIndex) {
                    richTextArea.moveTo(placeholderIndex + 1);
                } else {
                    richTextArea.moveTo(selectionStart);
                }
            }
        }));
    }

    /**
     * Initializes the image layer pane which is used to display images. It binds the
     * layer's translation to the rich text area's scroll position.
     */
    private void initImageLayer() {
        imageLayer.setPickOnBounds(false);
        richTextArea.estimatedScrollYProperty().addListener((obs, oldVal, newVal) -> imageLayer.setTranslateY(-newVal));
    }

    /**
     * Saves the file based on the given file type, with an option to bypass
     * auto-saving to a previously stored file path.
     *
     * @param fileType       the file extension/type (e.g., "ntm").
     * @param bypassAutoSave if true, forces the file chooser dialog.
     */
    public void saveFile(String fileType, boolean bypassAutoSave) {
        // If bypassing auto-save or no previous save path exists, ask user for a file path.
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

    /**
     * Saves the file without bypassing auto-save.
     *
     * @param fileType the file extension/type (e.g., "ntm").
     */
    public void saveFile(String fileType) {
        saveFile(fileType, false);
    }

    /**
     * Opens a file with the specified file type.
     *
     * @param fileType the file extension/type (e.g., "ntm").
     */
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

    /**
     * Opens a FileChooser dialog to let the user select a file to open, filtering
     * files by the specified file type.
     *
     * @param fileType the file extension/type to filter (e.g., "ntm").
     * @return the absolute path of the selected file, or null if no file is selected.
     */
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

    /**
     * Opens a FileChooser dialog to let the user select a location to save the file,
     * filtering files by the specified file type.
     *
     * @param fileType the file extension/type to filter (e.g., "ntm").
     * @return the absolute path of the chosen save location, or null if cancelled.
     */
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

    /**
     * Handles pasting into the editor. If the clipboard contains an image, it
     * creates an ImageComponent and adds it to the image layer; otherwise, it
     * pastes text into the rich text area.
     */
    public void pasteTextOrImage() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasImage()) {
            Image clipboardImage = clipboard.getImage();
            ImageComponent imageComponent = new ImageComponent(clipboardImage);
            imageComponent.setManaged(false); // Allow manual positioning

            double verticalPosition = richTextArea.getEstimatedScrollY();
            imageComponent.setLayoutX(10);
            imageComponent.setLayoutY(verticalPosition + 10);

            imageLayer.getChildren().add(imageComponent);
            NTMFile.markChanged(toolBar);
        } else {
            richTextArea.paste();
        }
    }

    /**
     * Shows a confirmation dialog when the user attempts to close the window.
     */
    private void showCloseConfirmation() {
        // todo
    }

    /**
     * Gets the style bar for formatting the text.
     *
     * @return the StyleBar instance.
     */
    public StyleBar getStylebar() {
        return styleBar;
    }

    public void recreateContextMenu() {
        contextMenu = new ContextMenu(this, richTextArea);
    }

    /**
     * Gets the rich text area component.
     *
     * @return the InlineCssTextArea instance.
     */
    public InlineCssTextArea getRichTextArea() {
        return richTextArea;
    }

    /**
     * Gets the image layer pane.
     *
     * @return the Pane that holds image components.
     */
    public Pane getImageLayer() {
        return imageLayer;
    }
}
