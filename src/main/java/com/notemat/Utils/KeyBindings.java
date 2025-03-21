package com.notemat.Utils;

import com.notemat.Components.EditorWindow;
import com.notemat.Components.StyleBar;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.InlineCssTextArea;


/**
 * Configures key bindings for common editor actions.
 * This class installs an event filter on the provided Scene to handle
 * shortcut keys for styling, saving, and clipboard operations on the editor.
 */
public class KeyBindings {
    /**
     * Constructs key bindings for the provided scene.
     *
     * @param editor    the EditorWindow instance used for saving and pasting functions.
     * @param scene     the Scene on which key events will be filtered.
     * @param textArea  the InlineCssTextArea to perform copy, paste and other text operations.
     * @param styleBar  the StyleBar which manages font styling controls.
     * @param imageLayer the Pane containing the image layer.
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
