package com.notemat.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomWindowResizing {
    private final JFrame frame;
    private final int borderThickness = 5;
    private final Cursor lastCursor;
    private Point initialResizeClick;
    private Rectangle initialBounds;

    public CustomWindowResizing(JFrame frame) {
        this.frame = frame;
        this.lastCursor = frame.getCursor();
        initGlassPane();
    }

    private void initGlassPane() {
        // Create a custom glass pane that activates resizing zones.
        JPanel glassPane = new JPanel(null) {
            @Override
            public boolean contains(int x, int y) {
                int w = getWidth();
                int h = getHeight();
                return (x <= borderThickness || x >= w - borderThickness ||
                        y <= borderThickness || y >= h - borderThickness);
            }
        };
        glassPane.setOpaque(false);
        frame.setGlassPane(glassPane);
        glassPane.setVisible(true);

        // Mouse listener for mousePressed and mouseReleased
        glassPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Only start resizing if the cursor is not the default one.
                if (frame.getCursor().getType() != Cursor.DEFAULT_CURSOR) {
                    initialResizeClick = e.getLocationOnScreen();
                    initialBounds = frame.getBounds();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                frame.setCursor(lastCursor);
            }
        });

        // Mouse motion listener for changing the cursor and performing resizing.
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int x = p.x;
                int y = p.y;
                int w = frame.getWidth();
                int h = frame.getHeight();

                if (x <= borderThickness && y <= borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                } else if (x >= w - borderThickness && y <= borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                } else if (x <= borderThickness && y >= h - borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                } else if (x >= w - borderThickness && y >= h - borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else if (x <= borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                } else if (x >= w - borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (y <= borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                } else if (y >= h - borderThickness) {
                    frame.setCursor(
                            Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else {
                    frame.setCursor(lastCursor);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialResizeClick == null || initialBounds == null) return;

                Point currentPoint = e.getLocationOnScreen();
                int deltaX = currentPoint.x - initialResizeClick.x;
                int deltaY = currentPoint.y - initialResizeClick.y;

                int newX = initialBounds.x;
                int newY = initialBounds.y;
                int newWidth = initialBounds.width;
                int newHeight = initialBounds.height;

                int cursorType = frame.getCursor().getType();
                switch (cursorType) {
                    case Cursor.NW_RESIZE_CURSOR:
                        newX = initialBounds.x + deltaX;
                        newY = initialBounds.y + deltaY;
                        newWidth = initialBounds.width - deltaX;
                        newHeight = initialBounds.height - deltaY;
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        newY = initialBounds.y + deltaY;
                        newWidth = initialBounds.width + deltaX;
                        newHeight = initialBounds.height - deltaY;
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        newX = initialBounds.x + deltaX;
                        newWidth = initialBounds.width - deltaX;
                        newHeight = initialBounds.height + deltaY;
                        break;
                    case Cursor.SE_RESIZE_CURSOR:
                        newWidth = initialBounds.width + deltaX;
                        newHeight = initialBounds.height + deltaY;
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        newX = initialBounds.x + deltaX;
                        newWidth = initialBounds.width - deltaX;
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        newWidth = initialBounds.width + deltaX;
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        newY = initialBounds.y + deltaY;
                        newHeight = initialBounds.height - deltaY;
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        newHeight = initialBounds.height + deltaY;
                        break;
                    default:
                        break;
                }

                newWidth = Math.max(newWidth, 400);
                newHeight = Math.max(newHeight, 300);

                frame.setBounds(newX, newY, newWidth, newHeight);
                frame.revalidate();
                frame.repaint();
            }
        });
    }
}
