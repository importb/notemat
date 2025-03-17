package com.notemat.Components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.InlineCssTextArea;

public class StyleBar extends HBox {
    private final ComboBox<String> fontCombo;
    private final ComboBox<Integer> sizeCombo;
    private final ToggleButton boldToggle;
    private final ToggleButton italicToggle;
    private final ToggleButton underlineToggle;
    private final ColorPicker textColorPicker;
    private final InlineCssTextArea textArea;
    private boolean ignoreControlEvents = false;
    private boolean ignoreCaretUpdate = false;

    public StyleBar(InlineCssTextArea textArea) {
        this.textArea = textArea;

        // Fonts
        fontCombo = new ComboBox<>(FXCollections.observableArrayList("Arial", "Lexend"));
        fontCombo.setValue("Lexend");
        fontCombo.getStyleClass().add("stylebar-combobox");
        fontCombo.getStyleClass().add("font-combobox");

        // Font sizes
        sizeCombo = new ComboBox<>(FXCollections.observableArrayList(8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 42, 48, 54, 60, 66, 72, 78, 84));
        sizeCombo.setValue(14);
        sizeCombo.getStyleClass().add("stylebar-combobox");

        // Bold, Italic, and Underline.
        boldToggle = new ToggleButton("B");
        boldToggle.getStyleClass().add("stylebar-togglebutton");

        italicToggle = new ToggleButton("I");
        italicToggle.getStyleClass().add("stylebar-togglebutton");

        underlineToggle = new ToggleButton("U");
        underlineToggle.getStyleClass().add("stylebar-togglebutton");

        boldToggle.setSelected(false);
        italicToggle.setSelected(false);
        underlineToggle.setSelected(false);

        // Text color
        textColorPicker = new ColorPicker(Color.WHITE);
        textColorPicker.getStyleClass().add("stylebar-combobox");
        textColorPicker.getStyleClass().add("stylebar-colorpicker");

        // Set up action listeners for each control to apply only its specific property
        fontCombo.setOnAction(e -> {
            if (!ignoreControlEvents) updateSpecificStyle("font");
        });
        sizeCombo.setOnAction(e -> {
            if (!ignoreControlEvents) updateSpecificStyle("size");
        });
        boldToggle.setOnAction(e -> {
            if (!ignoreControlEvents) updateSpecificStyle("bold");
        });
        italicToggle.setOnAction(e -> {
            if (!ignoreControlEvents) updateSpecificStyle("italic");
        });
        underlineToggle.setOnAction(e -> {
            if (!ignoreControlEvents) updateSpecificStyle("underline");
        });
        textColorPicker.setOnAction(e -> {
            if (!ignoreControlEvents) updateSpecificStyle("color");
        });

        // Listen for selection changes and caret movement
        textArea.selectionProperty().addListener((obs, oldSel, newSel) -> {
            updateStyleControlsFromSelection();
        });

        // Add the controls to the HBox.
        getChildren().addAll(fontCombo, sizeCombo, boldToggle, italicToggle, underlineToggle, textColorPicker);
        setSpacing(10);
        setPadding(new Insets(5));
    }

    /**
     * Updates the style controls based on the current selection or caret position
     * without applying any changes to the text.
     */
    private void updateStyleControlsFromSelection() {
        if (ignoreCaretUpdate) {
            ignoreCaretUpdate = false;
            return;
        }

        int pos = textArea.getSelection().getStart();

        // If we're at position 0 with no text, use defaults
        if (pos == 0 && textArea.getText().isEmpty()) {
            return;
        }

        // For position 0 with text or at the end of text, adjust position if needed
        if (pos == textArea.getLength()) {
            pos = Math.max(0, pos - 1);
        }

        // Get the style at the position
        String style = textArea.getStyleAtPosition(pos);
        if (style == null || style.trim().isEmpty()) {
            return;
        }

        // Extract values from the style string
        String extractedFont = extractFontFamily(style);
        Integer extractedSize = extractFontSize(style);
        String weight = extractProperty(style, "-fx-font-weight:\\s*(\\w+);");
        String fontStyle = extractProperty(style, "-fx-font-style:\\s*(\\w+);");
        String underlineVal = extractProperty(style, "-fx-underline:\\s*(\\w+);");
        String extractedColor = extractProperty(style, "-fx-fill:\\s*(#[0-9A-Fa-f]{6});");

        // Update controls without triggering style changes
        ignoreControlEvents = true;

        if (extractedFont != null && (extractedFont.equals("Arial") || extractedFont.equals("Lexend"))) {
            fontCombo.setValue(extractedFont);
        }
        if (extractedSize != null) {
            sizeCombo.setValue(extractedSize);
        }
        boldToggle.setSelected("bold".equalsIgnoreCase(weight));
        italicToggle.setSelected("italic".equalsIgnoreCase(fontStyle));
        underlineToggle.setSelected("true".equalsIgnoreCase(underlineVal));
        if (extractedColor != null) {
            textColorPicker.setValue(Color.web(extractedColor));
        }

        ignoreControlEvents = false;
    }

    /**
     * Updates only the specific style property that was changed by the user.
     *
     * @param property The style property to update ("font", "size", "bold", "italic", "underline", or "color")
     */
    private void updateSpecificStyle(String property) {
        ignoreCaretUpdate = true;
        int selectionStart = textArea.getSelection().getStart();
        int selectionEnd = textArea.getSelection().getEnd();

        // No text selected - just update the caret style
        if (selectionStart == selectionEnd) {
            String newStyle = getStyleBarStyle();
            textArea.setStyle(selectionStart, selectionStart, newStyle);
            return;
        }

        // Text is selected - process each character position individually
        for (int pos = selectionStart; pos < selectionEnd; pos++) {
            String currentStyle = textArea.getStyleAtPosition(pos);

            // If no style exists at this position, use the full style bar style
            if (currentStyle == null || currentStyle.trim().isEmpty()) {
                textArea.setStyle(pos, pos + 1, getStyleBarStyle());
                continue;
            }

            // Otherwise, just update the specific property while preserving others
            String updatedStyle = updateSpecificProperty(currentStyle, property);
            textArea.setStyle(pos, pos + 1, updatedStyle);
        }
    }

    /**
     * Updates a specific property in an existing style string
     *
     * @param currentStyle The current CSS style string
     * @param property     The property to update
     * @return The updated style string
     */
    private String updateSpecificProperty(String currentStyle, String property) {
        switch (property) {
            case "font":
                currentStyle = updateCssProperty(currentStyle, "-fx-font-family", String.format("'%s'", fontCombo.getValue()));
                break;
            case "size":
                currentStyle = updateCssProperty(currentStyle, "-fx-font-size", String.format("%dpt", sizeCombo.getValue()));
                break;
            case "bold":
                currentStyle = updateCssProperty(currentStyle, "-fx-font-weight", boldToggle.isSelected() ? "bold" : "normal");
                break;
            case "italic":
                currentStyle = updateCssProperty(currentStyle, "-fx-font-style", italicToggle.isSelected() ? "italic" : "normal");
                break;
            case "underline":
                currentStyle = updateCssProperty(currentStyle, "-fx-underline", underlineToggle.isSelected() ? "true" : "false");
                break;
            case "color":
                currentStyle = updateCssProperty(currentStyle, "-fx-fill", colorToHex(textColorPicker.getValue()));
                break;
        }
        return currentStyle;
    }

    /**
     * Updates or adds a specific CSS property in a style string
     *
     * @param style    The original CSS style string
     * @param property The CSS property name (e.g., "-fx-font-weight")
     * @param value    The value to set
     * @return The updated style string
     */
    private String updateCssProperty(String style, String property, String value) {
        String regex = property + ":\\s*[^;]+;";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(style);
        String replacement = property + ": " + value + ";";
        if (m.find()) {
            return m.replaceFirst(replacement);
        } else {
            return style + " " + replacement;
        }
    }

    /**
     * Build a complete CSS style string based on the current state of the style bar controls.
     */
    public String getStyleBarStyle() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("-fx-font-family: '%s'; -fx-font-size: %dpt;", fontCombo.getValue(), sizeCombo.getValue()));
        sb.append(boldToggle.isSelected() ? " -fx-font-weight: bold;" : " -fx-font-weight: normal;");
        sb.append(italicToggle.isSelected() ? " -fx-font-style: italic;" : " -fx-font-style: normal;");
        sb.append(underlineToggle.isSelected() ? " -fx-underline: true;" : " -fx-underline: false;");
        String textHex = colorToHex(textColorPicker.getValue());
        sb.append(" -fx-fill: ").append(textHex).append(";");
        return sb.toString();
    }

    /**
     * Helper to convert a Color to a hex string (e.g., "#F0F0FA").
     * Special-case the transparent color.
     */
    private String colorToHex(Color color) {
        if (color.equals(Color.TRANSPARENT)) {
            return "transparent";
        }
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Extracts the font family from a CSS style string.
     * Expected format: -fx-font-family: 'Arial';
     */
    private String extractFontFamily(String style) {
        Pattern pattern = Pattern.compile("-fx-font-family:\\s*'([^']+)'");
        Matcher matcher = pattern.matcher(style);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extracts the font size (in points) from a CSS style string.
     * Expected format: -fx-font-size: 12pt;
     */
    private Integer extractFontSize(String style) {
        Pattern pattern = Pattern.compile("-fx-font-size:\\s*(\\d+)pt");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * A generic helper to extract a property from the CSS string using a regex.
     */
    private String extractProperty(String style, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(style);
        return matcher.find() ? matcher.group(1) : null;
    }
}