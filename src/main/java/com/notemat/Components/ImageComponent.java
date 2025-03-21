package com.notemat.Components;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;



/**
 * A custom Pane to display and interact with an image.
 * Provides functionality for dragging, resizing, and deletion.
 */
public class ImageComponent extends Pane {
    private static final double MIN_SIZE = 10;
    private static final double HANDLE_SIZE = 10;
    private final ImageView imageView;
    private final Rectangle resizeHandle;

    private boolean dragging = false;
    private boolean resizing = false;
    private boolean selected = false;
    private double dragOffsetX, dragOffsetY;
    private double initWidth, initHeight;
    private double initMouseX, initMouseY;


    /**
     * Constructs an ImageComponent with the given image.
     *
     * @param image the Image to be displayed.
     */
    public ImageComponent(Image image) {
        // Set up the image view.
        imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());
        setPadding(new Insets(2));

        // Create the resize handle.
        resizeHandle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE, Color.WHITE);
        resizeHandle.setStroke(Color.DARKGRAY);
        resizeHandle.setStrokeWidth(1);
        resizeHandle.setVisible(false);
        resizeHandle.setLayoutX(imageView.getFitWidth() - HANDLE_SIZE);
        resizeHandle.setLayoutY(imageView.getFitHeight() - HANDLE_SIZE);

        // Add image and resize handle.
        getChildren().addAll(imageView, resizeHandle);

        // Enable mouse events for the entire node.
        initMouseEvents();

        // Deleting the img
        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            if (selected && event.getCode() == KeyCode.DELETE) {
                Parent parent = getParent();
                if (parent instanceof Pane) {
                    ((Pane) parent).getChildren().remove(ImageComponent.this);
                }
                event.consume();
            }
        });
    }

    /**
     * Initializes mouse event handlers for selection, dragging, and resizing.
     */
    private void initMouseEvents() {
        // Prevent toggling selection when clicking on the resize handle to avoid conflict.
        setOnMouseClicked(e -> {
            if (e.getTarget() != resizeHandle && !dragging && !resizing) {
                toggleSelection();
                e.consume();
            }
        });

        // When the user presses the mouse, decide if a drag or resize should begin.
        setOnMousePressed((MouseEvent e) -> {
            if (e.getTarget() == resizeHandle) {
                resizing = true;
                initWidth = imageView.getFitWidth();
                initHeight = imageView.getFitHeight();
                initMouseX = e.getSceneX();
                initMouseY = e.getSceneY();
            } else if (selected) {
                dragging = true;
                dragOffsetX = e.getSceneX() - getLayoutX();
                dragOffsetY = e.getSceneY() - getLayoutY();
            }
            e.consume();
        });

        // Update the image size or position as the mouse is dragged.
        setOnMouseDragged((MouseEvent e) -> {
            if (resizing) {
                double deltaX = e.getSceneX() - initMouseX;
                double deltaY = e.getSceneY() - initMouseY;

                // If Shift is held down, constrain the aspect ratio.
                if (e.isShiftDown()) {
                    double aspectRatio = initHeight / initWidth;
                    deltaY = deltaX * aspectRatio;
                }

                double newWidth = Math.max(MIN_SIZE, initWidth + deltaX);
                double newHeight = Math.max(MIN_SIZE, initHeight + deltaY);
                imageView.setFitWidth(newWidth);
                imageView.setFitHeight(newHeight);
                resizeHandle.setLayoutX(newWidth - HANDLE_SIZE);
                resizeHandle.setLayoutY(newHeight - HANDLE_SIZE);
            } else if (dragging) {
                double newX = e.getSceneX() - dragOffsetX;
                double newY = e.getSceneY() - dragOffsetY;
                setLayoutX(newX);
                setLayoutY(newY);
            }
            e.consume();
        });

        setOnMouseReleased((MouseEvent e) -> {
            dragging = false;
            resizing = false;
            e.consume();
        });

        // Cursor change
        resizeHandle.setOnMouseEntered(e -> setCursor(Cursor.SE_RESIZE));
        resizeHandle.setOnMouseExited(e -> setCursor(Cursor.DEFAULT));
    }

    /**
     * Toggles the selection state of this image. When selected, the resize
     * handle is shown and a drop shadow effect is applied.
     */
    private void toggleSelection() {
        selected = !selected;
        updateStyle();

        if (selected) {
            requestFocus();
        }
    }

    /**
     * Updates the visual style based on the current selection state.
     * If selected, applies a drop shadow and shows the resize handle;
     * otherwise, removes these effects.
     */
    private void updateStyle() {
        if (selected) {
            imageView.setEffect(new javafx.scene.effect.DropShadow(5, Color.BLACK));
            resizeHandle.setVisible(true);
        } else {
            imageView.setEffect(null);
            resizeHandle.setVisible(false);
        }
    }

    /**
     * Gets the image displayed in this component.
     *
     * @return the Image object.
     */
    public Image getImage() {
        return imageView.getImage();
    }

    /**
     * Gets the current width of the image component.
     *
     * @return the width in pixels.
     */
    public double getImageWidth() {
        return imageView.getFitWidth();
    }

    /**
     * Gets the current height of the image component.
     *
     * @return the height in pixels.
     */
    public double getImageHeight() {
        return imageView.getFitHeight();
    }
}
