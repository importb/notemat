package com.notemat.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;


/**
 * A Transferable implementation for styled text, allowing the transfer of
 * styled text data between components.
 */
public class StyledTextTransferable implements Transferable {
    public static final DataFlavor STYLED_TEXT_FLAVOR = new DataFlavor(StyledText.class, "Styled Text");
    private StyledText styledText;
    private static final Logger LOGGER = Logger.getLogger(StyledTextTransferable.class.getName());

    /**
     * Constructs a StyledTextTransferable object from the specified
     * StyledDocument and text range.
     *
     * @param doc   the StyledDocument containing the text
     * @param start the starting index of the text range
     * @param end   the ending index of the text range
     */
    public StyledTextTransferable(StyledDocument doc, int start, int end) {
        try {
            String text = doc.getText(start, end - start);
            AttributeSet[] attributes = new AttributeSet[text.length()];

            for (int i = 0; i < text.length(); i++) {
                attributes[i] = doc.getCharacterElement(start + i).getAttributes();
            }

            this.styledText = new StyledText(text, attributes);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {STYLED_TEXT_FLAVOR, DataFlavor.stringFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return STYLED_TEXT_FLAVOR.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (STYLED_TEXT_FLAVOR.equals(flavor)) {
            return styledText;
        } else if (DataFlavor.stringFlavor.equals(flavor)) {
            return styledText.text;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
