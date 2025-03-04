package com.notemat.themes;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;


public class ThemedScrollBarUI extends BasicScrollBarUI {
    @Override
    protected void configureScrollBarColors() {
        thumbColor = Theme.MAIN_COLOR_3;
        trackColor = Theme.MAIN_COLOR_2;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton button = super.createDecreaseButton(orientation);
        button.setBackground(Theme.MAIN_COLOR_2);
        button.setForeground(Theme.MAIN_COLOR_2);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusable(false);
        return button;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton button = super.createIncreaseButton(orientation);
        button.setBackground(Theme.MAIN_COLOR_2);
        button.setForeground(Theme.MAIN_COLOR_2);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusable(false);
        return button;
    }
}
