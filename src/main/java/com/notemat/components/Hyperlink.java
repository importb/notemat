package com.notemat.components;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.AttributeSet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URI;

public class Hyperlink {

    /**
     * Checks if the clicked position in the JTextPane has a hyperlink attribute.
     * If so, attempts to open the URL.
     *
     * @param textPane The text pane containing the text.
     * @param e        The mouse event.
     */
    public static void processHyperlinkClick(JTextPane textPane, MouseEvent e) {
        if (e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
            int pos = textPane.viewToModel2D(e.getPoint());
            StyledDocument doc = textPane.getStyledDocument();
            Element elem = doc.getCharacterElement(pos);
            AttributeSet as = elem.getAttributes();
            String url = (String) as.getAttribute("HYPERLINK");
            if (url != null) {
                openURL(url, textPane);
            }
        }
    }

    /**
     * Attempts to open the provided URL using the Desktop API.
     * On Linux (or when not supported), falls back to xdg-open.
     *
     * @param url    The URL to open.
     * @param parent The parent component (for error dialogs).
     */
    private static void openURL(String url, Component parent) {
        // try: Desktop API
        if (Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // fallback: using xdg-open
        try {
            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
