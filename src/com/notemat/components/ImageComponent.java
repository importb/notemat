package com.notemat.components;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Custom component to hold an image and allow resizing and moving.
 */
public class ImageComponent extends JLabel {
    private static final int RESIZE_HANDLE_SIZE = 8;
    private static final int MIN_SIZE = 10;

    private final BufferedImage originalImage;
    private final double aspectRatio;
    private boolean isSelected = false;
    private boolean isResizing = false;
    private boolean isMoving = false;
    private int dragOffsetX, dragOffsetY;
    private int moveDragOffsetX, moveDragOffsetY;
    private int initialWidth, initialHeight;

    private final Color borderColor = new Color(52, 119, 235);

    /**
     * Constructs an ImageComponent with the specified icon and original image.
     *
     * @param icon          the image icon to display
     * @param originalImage the original buffered image
     * @throws IllegalArgumentException if icon or originalImage is null
     */
    public ImageComponent(ImageIcon icon, BufferedImage originalImage) {
        super(icon);
        if (icon == null || originalImage == null) {
            throw new IllegalArgumentException("Icon and original image cannot be null.");
        }
        this.originalImage = originalImage;
        this.aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleSelection();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isResizing = false;
                isMoving = false;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCursor(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });
    }

    private void toggleSelection() {
        setSelected(!isSelected);
        setBorder(isSelected ? new LineBorder(borderColor, 2) : BorderFactory.createEmptyBorder());
        repaint();
    }

    private void handleMousePressed(MouseEvent e) {
        if (isSelected) {
            Point p = e.getPoint();
            if (isNearResizeHandle(p)) {
                isResizing = true;
                initialWidth = getWidth();
                initialHeight = getHeight();
                dragOffsetX = p.x;
                dragOffsetY = p.y;
            } else {
                isMoving = true;
                moveDragOffsetX = e.getX();
                moveDragOffsetY = e.getY();
            }
        }
    }

    private void updateCursor(MouseEvent e) {
        if (isSelected) {
            Point p = e.getPoint();
            if (isNearResizeHandle(p)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (isSelected) {
            if (isResizing) {
                resizeImage(e);
            } else if (isMoving) {
                moveImage(e);
            }
        }
    }

    private void resizeImage(MouseEvent e) {
        int deltaX = e.getX() - dragOffsetX;
        int deltaY = e.getY() - dragOffsetY;
        int newWidth = initialWidth + deltaX;
        int newHeight = initialHeight + deltaY;

        // Keep aspect ratio if Shift is pressed.
        if (e.isShiftDown()) {
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                newHeight = (int) (newWidth / aspectRatio);
            } else {
                newWidth = (int) (newHeight * aspectRatio);
            }
        }

        newWidth = Math.max(MIN_SIZE, newWidth);
        newHeight = Math.max(MIN_SIZE, newHeight);
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(scaledImage));
        setSize(newWidth, newHeight);
        setPreferredSize(new Dimension(newWidth, newHeight));
        revalidate();
        repaint();
    }

    private void moveImage(MouseEvent e) {
        Point pt = SwingUtilities.convertPoint(ImageComponent.this, e.getPoint(), getParent());
        int newX = pt.x - moveDragOffsetX;
        int newY = pt.y - moveDragOffsetY;
        Container parent = getParent();
        if (parent != null) {
            int parentWidth = parent.getWidth();
            int parentHeight = parent.getHeight();
            newX = Math.max(0, Math.min(newX, parentWidth - getWidth()));
            newY = Math.max(0, Math.min(newY, parentHeight - getHeight()));
        }
        setLocation(newX, newY);
    }

    // Returns true if the point is near the lower-right resize handle.
    private boolean isNearResizeHandle(Point p) {
        int x = getWidth() - RESIZE_HANDLE_SIZE;
        int y = getHeight() - RESIZE_HANDLE_SIZE;
        return p.x > x && p.x < getWidth() && p.y > y && p.y < getHeight();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isSelected) {
            int x = getWidth() - RESIZE_HANDLE_SIZE;
            int y = getHeight() - RESIZE_HANDLE_SIZE;
            g.setColor(Color.WHITE);
            g.fillRect(x, y, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
        }
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }
}
