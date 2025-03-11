package com.notemat.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * A simple implementation of the Icon interface that paints a rectangle
 * filled with color.
 */
public class ColorIcon implements Icon {
    private final Color color;
    private final int width;
    private final int height;

    /**
     * Constructs a ColorIcon with the specified color, width, and height.
     *
     * @param color  the color of the icon
     * @param width  the width of the icon
     * @param height the height of the icon
     * @throws IllegalArgumentException if width or height < 0
     */
    public ColorIcon(Color color, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive.");
        }
        this.color = color;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x + 5, y, width, height);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
