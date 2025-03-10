package com.notemat.components;

import com.notemat.themes.Theme;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightClickMenu extends JPopupMenu {
    /**
     * Constructs a right‑click menu for the given text component.
     *
     * @param textComponent The text component to operate on.
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

        // Hyperlink
        JMenuItem insertLink = new JMenuItem("Linkify");
        insertLink.addActionListener(e -> {
            String selectedText = textComponent.getSelectedText();
            if (selectedText != null && (selectedText.startsWith("https://") || selectedText.startsWith("http://"))) {
                int selStart = textComponent.getSelectionStart();
                SimpleAttributeSet hyperlinkAttr = new SimpleAttributeSet();
                hyperlinkAttr.addAttribute("HYPERLINK", selectedText);
                StyleConstants.setUnderline(hyperlinkAttr, true);
                StyleConstants.setForeground(hyperlinkAttr, Theme.TEXT_COLOR_BLUE);
                ((JTextPane) textComponent).getStyledDocument().setCharacterAttributes(selStart, selectedText.length(), hyperlinkAttr, false);
            }
        });
        add(insertLink);
    }

    /**
     * Attaches the context menu to the provided component.
     *
     * @param component The component to attach the context menu to.
     * @param textComponent The text component to operate on.
     */
    public static void attachToComponent(JComponent component, JTextComponent textComponent) {
        RightClickMenu menu = new RightClickMenu(textComponent);
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
