package com.notemat.Utils;

import com.notemat.Components.EditorWindow;
import com.notemat.Components.ImageComponent;
import com.notemat.Components.StyleBar;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.InlineCssTextArea;


public class KeyBindings {

    /**
     * Constructs key bindings for the provided scene.
     *
     * @param scene    the Scene on which key events will be filtered.
     * @param textArea the InlineCssTextArea to perform copy/paste and style actions.
     * @param styleBar the StyleBar which manages the font styling controls.
     */
    public KeyBindings(EditorWindow editor, Scene scene, InlineCssTextArea textArea, StyleBar styleBar, Pane imageLayer) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                KeyCode code = event.getCode();
                switch (code) {
                    case B:
                        event.consume();
                        styleBar.toggleBold();
                        break;
                    case I:
                        event.consume();
                        styleBar.toggleItalic();
                        break;
                    case U:
                        event.consume();
                        styleBar.toggleUnderline();
                        break;
                    case S:
                        event.consume();
                        editor.saveFile("ntm");
                        break;
                    case C:
                        event.consume();
                        textArea.copy();
                        break;
                    case V:
                        event.consume();
                        editor.pasteTextOrImage();
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
