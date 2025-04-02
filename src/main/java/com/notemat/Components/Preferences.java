package com.notemat.Components;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * The Preferences class provides a custom window for managing user
 * preferences such as main font, font size, Gemini API key, and Gemini model.
 * The preferences are stored in a properties file.
 */
public class Preferences extends Stage {
    private static final String PREFS_FILE = "preferences.properties";
    private static final String KEY_MAIN_FONT = "mainFont";
    private static final String KEY_MAIN_FONT_SIZE = "mainFontSize";
    private static final String KEY_GEMINI_API = "geminiApi";
    private static final String KEY_GEMINI_MODEL = "geminiModel";
    private static final String KEY_ENABLE_GEMINI = "enableGemini";

    private final ComboBox<String> fontComboBox;
    private final ComboBox<Integer> fontSizeComboBox;
    private final TextField geminiApiTextField;
    private final ComboBox<String> geminiModelComboBox;
    private final CheckBox enableGeminiCheckBox;
    private final Properties properties;

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Constructs the Preferences window for the specified EditorWindow.
     *
     * @param editor The EditorWindow instance to be updated when preferences
     *               are saved.
     */
    public Preferences(EditorWindow editor) {
        // Initialization
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Preferences");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        // Load preferences from file.
        properties = new Properties();
        loadPreferences();

        // Toolbar
        HBox toolbar = createToolbar();

        // --- Font Settings ---
        // Main font selection
        Label fontLabel = new Label("Main Font:");
        fontComboBox = new ComboBox<>(FXCollections.observableArrayList("Lexend", "Arial", "Times New Roman"));
        String currentFont = properties.getProperty(KEY_MAIN_FONT, "Lexend");
        fontComboBox.setValue(currentFont);
        HBox fontBox = new HBox(10, fontLabel, fontComboBox);
        fontBox.setAlignment(Pos.CENTER_LEFT);

        // Main font size selection
        Label fontSizeLabel = new Label("Main Font Size:");
        fontSizeComboBox = new ComboBox<>(FXCollections.observableArrayList(8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 42, 48, 54, 60, 66, 72, 78, 84));
        int currentFontSize = 14;
        String fontSizeProp = properties.getProperty(KEY_MAIN_FONT_SIZE);
        if (fontSizeProp != null) {
            try {
                currentFontSize = Integer.parseInt(fontSizeProp);
            } catch (NumberFormatException e) {
                System.err.println("Invalid font size in preferences, using default: " + e.getMessage());
            }
        }
        fontSizeComboBox.setValue(currentFontSize);
        HBox fontSizeBox = new HBox(10, fontSizeLabel, fontSizeComboBox);
        fontSizeBox.setAlignment(Pos.CENTER_LEFT);

        // Group font settings into their own VBox
        VBox fontSettingsBox = new VBox(10, fontBox, fontSizeBox);
        fontSettingsBox.setPadding(new Insets(5, 0, 0, 8));
        fontSettingsBox.setAlignment(Pos.CENTER_LEFT);


        // --- Gemini Settings ---
        // Enable Gemini checkbox
        enableGeminiCheckBox = new CheckBox("Enable Gemini");
        String enableGeminiProp = properties.getProperty(KEY_ENABLE_GEMINI, "false");
        enableGeminiCheckBox.setSelected(Boolean.parseBoolean(enableGeminiProp));
        HBox enableGeminiBox = new HBox(10, enableGeminiCheckBox);
        enableGeminiBox.setAlignment(Pos.CENTER_LEFT);

        // Gemini API textbox
        Label geminiApiLabel = new Label("Gemini API Key:");
        geminiApiTextField = new TextField();
        String geminiApi = properties.getProperty(KEY_GEMINI_API, "");
        geminiApiTextField.setText(geminiApi);
        HBox geminiApiBox = new HBox(10, geminiApiLabel, geminiApiTextField);
        geminiApiBox.setAlignment(Pos.CENTER_LEFT);

        // Gemini Model combobox
        Label geminiModelLabel = new Label("Gemini Model:");
        geminiModelComboBox = new ComboBox<>(FXCollections.observableArrayList("Gemini 2.0 Flash", "Gemini 2.0 Flash-Lite", "Gemini 2.5 Pro"));
        String currentModel = properties.getProperty(KEY_GEMINI_MODEL, "Gemini 2.0 Flash");
        geminiModelComboBox.setValue(currentModel);
        HBox geminiModelBox = new HBox(10, geminiModelLabel, geminiModelComboBox);
        geminiModelBox.setAlignment(Pos.CENTER_LEFT);

        // Group Gemini settings into their own VBox
        VBox geminiSettingsBox = new VBox(10, enableGeminiBox, geminiApiBox, geminiModelBox);
        geminiSettingsBox.setPadding(new Insets(50, 0, 0, 8));
        geminiSettingsBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons for saving or canceling preferences.
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            savePreferences();
            editor.recreateContextMenu();
            close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setPadding(new Insets(65, 10, 0, 0));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);


        // Combine all the sections into the root container
        VBox root = new VBox(10, toolbar, fontSettingsBox, geminiSettingsBox, buttonBox);
        root.getStyleClass().add("preferences");

        Scene scene = new Scene(root, 400, 405);
        setScene(scene);

        // Apply theme from CSS resources.
        String css = getClass().getResource("/theme.css").toExternalForm();
        String fontsCss = getClass().getResource("/fonts.css").toExternalForm();
        scene.getStylesheets().addAll(fontsCss, css);
    }

    /**
     * Creates the toolbar for the Preferences window which includes
     * minimize and close buttons and enables window dragging.
     *
     * @return the configured HBox toolbar.
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(5);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(5, 5, 5, 5));
        toolbar.getStyleClass().add("toolbar");

        Button minimizeButton = new Button("-");
        minimizeButton.setOnAction(event -> setIconified(true));

        Button closeButton = new Button("X");
        closeButton.setOnAction(event -> close());

        toolbar.getChildren().addAll(minimizeButton, closeButton);

        // Enable window dragging
        toolbar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        toolbar.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });

        return toolbar;
    }


    /**
     * Loads the preferences from the file if it exists.
     */
    private void loadPreferences() {
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                System.err.println("Failed to load preferences: " + e.getMessage());
            }
        }
    }

    /**
     * Saves the current preferences (selected main font, font size, Gemini API
     * key, Gemini Model, and enable Gemini flag) to the file.
     */
    private void savePreferences() {
        properties.setProperty(KEY_MAIN_FONT, fontComboBox.getValue());
        properties.setProperty(KEY_MAIN_FONT_SIZE, fontSizeComboBox.getValue().toString());
        properties.setProperty(KEY_GEMINI_API, geminiApiTextField.getText());
        properties.setProperty(KEY_GEMINI_MODEL, geminiModelComboBox.getValue());
        properties.setProperty(KEY_ENABLE_GEMINI, Boolean.toString(enableGeminiCheckBox.isSelected()));
        try (FileOutputStream out = new FileOutputStream(PREFS_FILE)) {
            properties.store(out, "User Preferences");
        } catch (IOException e) {
            System.err.println("Failed to save preferences: " + e.getMessage());
        }
    }

    /**
     * Retrieves the main font setting from the preferences file.
     *
     * @return the main font name; defaults to "Lexend" if not set.
     */
    public static String getMainFont() {
        Properties props = new Properties();
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                return props.getProperty(KEY_MAIN_FONT, "Lexend");
            } catch (IOException e) {
                System.err.println("Failed to load main font from preferences: " + e.getMessage());
            }
        }
        return "Lexend";
    }

    /**
     * Retrieves the main font size setting from the preferences file.
     *
     * @return the main font size; defaults to 14 if not set or invalid.
     */
    public static int getMainFontSize() {
        Properties props = new Properties();
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                String sizeStr = props.getProperty(KEY_MAIN_FONT_SIZE, "14");
                return Integer.parseInt(sizeStr);
            } catch (IOException | NumberFormatException e) {
                System.err.println("Failed to load main font size from preferences: " + e.getMessage());
            }
        }
        return 14;
    }

    /**
     * Retrieves the Gemini API setting from the preferences file.
     *
     * @return the Gemini API string; defaults to an empty string if not set.
     */
    public static String getGeminiApi() {
        Properties props = new Properties();
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                return props.getProperty(KEY_GEMINI_API, "");
            } catch (IOException e) {
                System.err.println("Failed to load Gemini API from preferences: " + e.getMessage());
            }
        }
        return "";
    }

    /**
     * Retrieves the Gemini Model setting from the preferences file.
     *
     * @return the Gemini Model string; defaults to "Flash" if not set.
     */
    public static String getGeminiModel() {
        Properties props = new Properties();
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                return props.getProperty(KEY_GEMINI_MODEL, "Flash");
            } catch (IOException e) {
                System.err.println("Failed to load Gemini Model from preferences: " + e.getMessage());
            }
        }
        return "Flash";
    }

    /**
     * Retrieves the "Enable Gemini" flag from the preferences file.
     *
     * @return true if Gemini is enabled; defaults to false if not set.
     */
    public static boolean getEnableGemini() {
        Properties props = new Properties();
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                return Boolean.parseBoolean(props.getProperty(KEY_ENABLE_GEMINI, "false"));
            } catch (IOException e) {
                System.err.println("Failed to load enable Gemini flag from preferences: " + e.getMessage());
            }
        }
        return false;
    }
}
