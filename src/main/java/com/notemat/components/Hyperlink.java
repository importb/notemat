package com.notemat.components;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.AttributeSet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hyperlink {
    private static final Logger LOGGER = Logger.getLogger(Hyperlink.class.getName());

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
                openURL(url);
            }
        }
    }

    /**
     * Attempts to open the provided URL using the Desktop API.
     * On Linux (or when not supported), falls back to xdg-open.
     *
     * @param url    The URL to open.
     */
    private static void openURL(String url) {
        // try: Desktop API
        if (Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                return;
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error using Desktop browse", ex);
            }
        }
        // fallback: using xdg-open
        try {
            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to open URL using xdg-open", ex);
            MessagePopup.showMessage("Error", "Browsing is not supported on your system.");
        }
    }
}
