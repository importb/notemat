package com.notemat.components;

import com.notemat.themes.Theme;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.JTextComponent;

public class RightClickMenu extends JPopupMenu {
    /**
     * Constructs a right‑click menu for the given text component.
     *
     * @param textComponent     The text component to operate on.
     */
    public RightClickMenu(JTextComponent textComponent) {
        // Copy
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> textComponent.copy());
        add(copyItem);

        // Paste
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener(e -> textComponent.paste());
        add(pasteItem);
    }

    /**
     * Attaches the context menu to the provided component.
     *
     * @param component         The component to attach the context menu to.
     * @param textComponent     The text component to operate on.
     */
    public static void attachToComponent(JComponent component, JTextComponent textComponent) {
        RightClickMenu menu = new RightClickMenu(textComponent);

        // add styling
        Theme.applyPopUpStyle(menu);

        component.addMouseListener(new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
        });
    }
}
