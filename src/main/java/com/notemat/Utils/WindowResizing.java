package com.notemat.Utils;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * The WindowResizing class enables custom resizing for a Stage (window).
 * It sets up mouse event filters to detect when the user is in a window
 * resize zone and then resizes the window accordingly. It also provides
 * a simple maximize/restore function.
 */
public class WindowResizing {
    private final Stage stage;
    private final double borderThickness = 5.0;

    private boolean inResizeZone = false;
    private boolean resizing = false;

    private double initX;
    private double initY;
    private double initStageX;
    private double initStageY;
    private double initStageWidth;
    private double initStageHeight;

    private ResizeDirection direction = ResizeDirection.NONE;
    private static boolean isMaximized = false;
    private static double prevX, prevY, prevWidth, prevHeight;

    /**
     * Enum representing the possible directions from which a window can be resized.
     */
    private enum ResizeDirection {
        NW, N, NE, E, SE, S, SW, W, NONE
    }

    /**
     * Constructs a WindowResizing object for the given Stage and installs the necessary event filters.
     *
     * @param stage the Stage (window) to enable resizing on.
     */
    public WindowResizing(Stage stage) {
        this.stage = stage;
        installEventFilters();
    }

    /**
     * Installs mouse event filters on the stage's scene to handle mouse movement,
     * press, drag, and release events for window resizing.
     */
    private void installEventFilters() {
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    /**
     * Handles mouse movements by checking if the pointer is in the resize zone.
     * If so, updates the cursor to the appropriate resize cursor.
     *
     * @param event the MouseEvent to process.
     */
    private void handleMouseMoved(MouseEvent event) {
        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();
        double width = stage.getWidth();
        double height = stage.getHeight();

        ResizeDirection newDirection = getResizeDirection(mouseX, mouseY, width, height);
        inResizeZone = (newDirection != ResizeDirection.NONE);
        updateTextAreaStyles(stage.getScene().getRoot(), inResizeZone);

        Cursor cursor = switch (newDirection) {
            case NW -> Cursor.NW_RESIZE;
            case N -> Cursor.N_RESIZE;
            case NE -> Cursor.NE_RESIZE;
            case E -> Cursor.E_RESIZE;
            case SE -> Cursor.SE_RESIZE;
            case S -> Cursor.S_RESIZE;
            case SW -> Cursor.SW_RESIZE;
            case W -> Cursor.W_RESIZE;
            default -> Cursor.DEFAULT;
        };
        stage.getScene().setCursor(cursor);
    }

    /**
     * Handles mouse press events. If the press is within the resize zone,
     * saves initial values for the stage bounds and sets the resizing flag.
     *
     * @param event the MouseEvent to process.
     */
    private void handleMousePressed(MouseEvent event) {
        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();
        double width = stage.getWidth();
        double height = stage.getHeight();

        direction = getResizeDirection(mouseX, mouseY, width, height);
        // Only start resizing (and consume events) if within the resize zone.
        if (direction != ResizeDirection.NONE) {
            resizing = true;
            initX = event.getScreenX();
            initY = event.getScreenY();
            initStageX = stage.getX();
            initStageY = stage.getY();
            initStageWidth = stage.getWidth();
            initStageHeight = stage.getHeight();
            event.consume();
        }
    }

    /**
     * Handles mouse drag events to resize the window based on the drag direction,
     * enforcing minimum window dimensions.
     *
     * @param event the MouseEvent to process.
     */
    private void handleMouseDragged(MouseEvent event) {
        if (!resizing) {
            return;
        }

        double deltaX = event.getScreenX() - initX;
        double deltaY = event.getScreenY() - initY;
        double newWidth = initStageWidth;
        double newHeight = initStageHeight;
        double newX = initStageX;
        double newY = initStageY;

        switch (direction) {
            case NW:
                newWidth = initStageWidth - deltaX;
                newHeight = initStageHeight - deltaY;
                newX = initStageX + deltaX;
                newY = initStageY + deltaY;
                break;
            case N:
                newHeight = initStageHeight - deltaY;
                newY = initStageY + deltaY;
                break;
            case NE:
                newWidth = initStageWidth + deltaX;
                newHeight = initStageHeight - deltaY;
                newY = initStageY + deltaY;
                break;
            case E:
                newWidth = initStageWidth + deltaX;
                break;
            case SE:
                newWidth = initStageWidth + deltaX;
                newHeight = initStageHeight + deltaY;
                break;
            case S:
                newHeight = initStageHeight + deltaY;
                break;
            case SW:
                newWidth = initStageWidth - deltaX;
                newHeight = initStageHeight + deltaY;
                newX = initStageX + deltaX;
                break;
            case W:
                newWidth = initStageWidth - deltaX;
                newX = initStageX + deltaX;
                break;
            default:
                break;
        }

        // Enforce minimum window dimensions.
        if (newWidth < 400) {
            if (direction == ResizeDirection.W || direction == ResizeDirection.NW || direction == ResizeDirection.SW) {
                newX = initStageX + (initStageWidth - 400);
            }
            newWidth = 400;
        }
        if (newHeight < 300) {
            if (direction == ResizeDirection.N || direction == ResizeDirection.NW || direction == ResizeDirection.NE) {
                newY = initStageY + (initStageHeight - 300);
            }
            newHeight = 300;
        }

        stage.setX(newX);
        stage.setY(newY);
        stage.setWidth(newWidth);
        stage.setHeight(newHeight);
        event.consume();
    }


    /**
     * Handles mouse release events by stopping any ongoing resizing
     * and resetting the cursor.
     *
     * @param event the MouseEvent to process.
     */
    private void handleMouseReleased(MouseEvent event) {
        // Only consume the event if we were actually resizing.
        if (resizing) {
            resizing = false;
            direction = ResizeDirection.NONE;
            stage.getScene().setCursor(Cursor.DEFAULT);
            event.consume();
        }
    }

    /**
     * Determines the resize direction based on the mouse position relative
     * to the stage's borders.
     *
     * @param mouseX the x-coordinate of the mouse.
     * @param mouseY the y-coordinate of the mouse.
     * @param width  the current width of the stage.
     * @param height the current height of the stage.
     * @return a ResizeDirection value indicating where the resize ROI is.
     */
    private ResizeDirection getResizeDirection(double mouseX, double mouseY, double width, double height) {
        boolean left = mouseX < borderThickness;
        boolean right = mouseX > width - borderThickness;
        boolean top = mouseY < borderThickness;
        boolean bottom = mouseY > height - borderThickness;

        if (left && top) {
            return ResizeDirection.NW;
        }
        if (right && top) {
            return ResizeDirection.NE;
        }
        if (left && bottom) {
            return ResizeDirection.SW;
        }
        if (right && bottom) {
            return ResizeDirection.SE;
        }
        if (top) {
            return ResizeDirection.N;
        }
        if (bottom) {
            return ResizeDirection.S;
        }
        if (left) {
            return ResizeDirection.W;
        }
        if (right) {
            return ResizeDirection.E;
        }
        return ResizeDirection.NONE;
    }

    /**
     * Maximizes or restores the stage. If the stage is maximized, it restores
     * the stage to its previous size and position; otherwise, it maximizes the
     * stage to the bounds of the primary screen.
     *
     * @param stage the Stage to maximize or restore.
     */
    public static void maximize(Stage stage) {
        if (isMaximized) {
            // Restore the window to its previous size and position
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            isMaximized = false;
        } else {
            // Save the current size and position
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            // Get the screen bounds
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Maximize the window to fill the screen
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            isMaximized = true;
        }
    }

    /**
     * Recursively updates style classes for nodes in the given scene graph.
     * Adds or removes the "resize-zone" class based on the inResizeZone flag.
     *
     * @param node         the root node of the scene graph to update.
     * @param inResizeZone if true, the node is in a resize zone.
     */
    private void updateTextAreaStyles(javafx.scene.Node node, boolean inResizeZone) {
        if (node.getStyleClass().contains("styled-text-area")) {
            if (inResizeZone) {
                if (!node.getStyleClass().contains("resize-zone")) {
                    node.getStyleClass().add("resize-zone");
                }
            } else {
                node.getStyleClass().remove("resize-zone");
            }
        }

        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                updateTextAreaStyles(child, inResizeZone);
            }
        }
    }
}
