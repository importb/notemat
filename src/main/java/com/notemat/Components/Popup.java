package com.notemat.Components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The Popup class provides a customizable popup window with a header, title,
 * description, and two buttons: "Cancel" and a user-defined button.
 */
public class Popup extends Stage {

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Constructs a Popup window with the specified header, title, description,
     * and user-defined button.
     *
     * @param header       The header text displayed at the top of the popup.
     * @param description  The description text displayed below the title.
     * @param buttonName   The name of the user-defined button.
     * @param buttonAction The action to be executed when the user-defined
     *                     button is clicked.
     */
    public Popup(String header, String description, String buttonName, Runnable buttonAction) {
        // Initialization
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Popup");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        // Toolbar
        HBox toolbar = createToolbar(header);

        // description
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("popup-description");

        VBox contentBox = new VBox(10, descriptionLabel);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(28, 10, 10, 10));

        // Buttons
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());

        Button userButton = new Button(buttonName);
        userButton.setOnAction(e -> {
            buttonAction.run();
            close();
        });

        HBox buttonBox = new HBox(10, cancelButton, userButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(40, 10, 10, 10));

        // Combine all sections into the root container
        VBox root = new VBox(10, toolbar, contentBox, buttonBox);
        root.getStyleClass().add("msg-popup");

        Scene scene = new Scene(root, 300, 200);
        setScene(scene);

        // Apply theme from CSS resources
        String css = getClass().getResource("/theme.css").toExternalForm();
        String fontsCss = getClass().getResource("/fonts.css").toExternalForm();
        scene.getStylesheets().addAll(fontsCss, css);
    }

    /**
     * Creates the toolbar for the Popup window which includes the header text
     * and enables window dragging.
     *
     * @param header The header text to display in the toolbar.
     * @return the configured HBox toolbar.
     */
    private HBox createToolbar(String header) {
        Label headerLabel = new Label(header);
        headerLabel.getStyleClass().add("popup-header");

        HBox toolbar = new HBox(headerLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5, 5, 5, 5));
        toolbar.getStyleClass().add("toolbar");

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
     * Static method to call and display the Popup window.
     *
     * @param header       The header text displayed at the top of the popup.
     * @param description  The description text displayed below the title.
     * @param buttonName   The name of the user-defined button.
     * @param buttonAction The action to be executed when the user-defined
     *                     button is clicked.
     */
    public static void callPopup(String header, String description, String buttonName, Runnable buttonAction) {
        Popup popup = new Popup(header, description, buttonName, buttonAction);
        popup.showAndWait();
    }
}
