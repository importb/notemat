package com.notemat.Utils;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WindowResizing {
    private final Stage stage;
    private final double borderThickness = 5.0;
    private boolean inResizeZone = false;
    private double initX; // Mouse screen coordinate at press
    private double initY;
    private double initStageX; // Stage X coordinate at press
    private double initStageY; // Stage Y coordinate at press
    private double initStageWidth;
    private double initStageHeight;
    private boolean resizing = false;
    private ResizeDirection direction = ResizeDirection.NONE;

    private enum ResizeDirection {
        NW, N, NE, E, SE, S, SW, W, NONE
    }

    public WindowResizing(Stage stage) {
        this.stage = stage;
        installEventFilters();
    }

    private void installEventFilters() {
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

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

    private void handleMouseReleased(MouseEvent event) {
        // Only consume the event if we were actually resizing.
        if (resizing) {
            resizing = false;
            direction = ResizeDirection.NONE;
            stage.getScene().setCursor(Cursor.DEFAULT);
            event.consume();
        }
    }

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
