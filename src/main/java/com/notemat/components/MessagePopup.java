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
     */
    public MessagePopup(Frame owner, String title, String message) {
        super(owner, true);
        setUndecorated(true); // Remove native decorations
        initComponents(title, message);
        Theme.applyMessagePopupStyle(this);
    }

    private void initComponents(String title, String message) {
        // We set the headerPanel color here, because Theme uses recursive way of setting the style.
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

        JLabel messageLabel = new JLabel("<html><body style='width:300px;'>" + message + "</body></html>");
        messageLabel.setBorder(BorderFactory.createEmptyBorder(25, 5, 5, 5));

        JButton okButton = new JButton("OK");
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 40));
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

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
     */
    public static void showMessage(Frame owner, String title, String message) {
        MessagePopup popup = new MessagePopup(owner, title, message);
        popup.setVisible(true);
    }

    /**
     * Overloaded method to allow showing the popup without an owner.
     *
     * @param title       The title.
     * @param message     The message.
     */
    public static void showMessage(String title, String message) {
        showMessage(null, title, message);
    }
}
