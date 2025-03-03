package com.notemat.components;

import javax.swing.text.AttributeSet;
import java.io.Serializable;


/**
 * Represents styled text with associated attributes.
 */
public class StyledText implements Serializable {
    public String text;
    public AttributeSet[] attributes;

    /**
     * Constructs a StyledText object with the specified text and attributes.
     *
     * @param text      the text content
     * @param attributes the array of attribute sets associated with the text
     */
    public StyledText(String text, AttributeSet[] attributes) {
        this.text = text;
        this.attributes = attributes;
    }
}