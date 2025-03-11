package com.notemat.components;

import com.notemat.themes.Theme;
import javax.swing.*;
import java.awt.*;

public class MessagePopup extends JDialog {

    /**
     * Constructs the custom message popup.
     *
     * @param owner       The owning frame (or null).
     * @param title       The title to display.
     * @param message     The message text.
     * @param button1     The first button (or null for default "OK" button).
     * @param button2     The second button (or null for no second button).
     */
    public MessagePopup(Frame owner, String title, String message, JButton button1, JButton button2) {
        super(owner, true);
        setUndecorated(true); // Remove native decorations
        initComponents(title, message, button1, button2);
        Theme.applyMessagePopupStyle(this);
    }

    private void initComponents(String title, String message, JButton button1, JButton button2) {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.MAIN_COLOR_4);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JButton closeButton = new JButton("x");
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        closeButton.setBackground(Theme.MAIN_COLOR_4);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(closeButton, BorderLayout.EAST);

        // Message label
        JLabel messageLabel = new JLabel("<html><body style='width:300px;'>" + message + "</body></html>");
        messageLabel.setBorder(BorderFactory.createEmptyBorder(25, 5, 15, 5));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // First button
        if (button1 == null) {
            button1 = new JButton("OK");
            button1.setFocusPainted(false);
            button1.addActionListener(e -> dispose());
        }
        buttonPanel.add(button1);

        // Second button (optional)
        if (button2 != null) {
            buttonPanel.add(button2);
        }

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 40));
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(getOwner() != null ? getOwner() : null);
    }

    /**
     * Shows the MessagePopup with the given owner.
     *
     * @param owner       The parent frame.
     * @param title       The title.
     * @param message     The message.
     * @param button1     The first button (or null for default "OK" button).
     * @param button2     The second button (or null for no second button).
     */
    public static void showMessage(Frame owner, String title, String message, JButton button1, JButton button2) {
        MessagePopup popup = new MessagePopup(owner, title, message, button1, button2);
        popup.setVisible(true);
    }

    /**
     * Overloaded method to allow showing the popup without an owner.
     *
     * @param title       The title.
     * @param message     The message.
     * @param button1     The first button (or null for default "OK" button).
     * @param button2     The second button (or null for no second button).
     */
    public static void showMessage(String title, String message, JButton button1, JButton button2) {
        showMessage(null, title, message, button1, button2);
    }

    /**
     * Overloaded method to allow showing the popup with default "OK" button.
     *
     * @param owner       The parent frame.
     * @param title       The title.
     * @param message     The message.
     */
    public static void showMessage(Frame owner, String title, String message) {
        showMessage(owner, title, message, null, null);
    }

    /**
     * Overloaded method to allow showing the popup without an owner and with default "OK" button.
     *
     * @param title       The title.
     * @param message     The message.
     */
    public static void showMessage(String title, String message) {
        showMessage(null, title, message, null, null);
    }
}
