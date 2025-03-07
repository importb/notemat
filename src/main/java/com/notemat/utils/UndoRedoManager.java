package com.notemat.utils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;

public class UndoRedoManager {
    private final UndoManager undoManager;
    private final Action undoAction;
    private final Action redoAction;

    public UndoRedoManager() {
        undoManager = new UndoManager();
        undoAction = new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    try {
                        undoManager.undo();
                    } catch (CannotUndoException ex) {
                        ex.printStackTrace();
                    }
                    updateActions();
                }
            }
        };

        redoAction = new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    try {
                        undoManager.redo();
                    } catch (CannotRedoException ex) {
                        ex.printStackTrace();
                    }
                    updateActions();
                }
            }
        };

        updateActions();
    }

    /**
     * Adds an undoable edit to the underlying UndoManager.
     *
     * @param event The UndoableEditEvent to add.
     */
    public void addEdit(UndoableEditEvent event) {
        undoManager.addEdit(event.getEdit());
        updateActions();
    }

    /**
     * Manually triggers an undo operation.
     */
    public void undo() {
        if (undoManager.canUndo()) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                ex.printStackTrace();
            }
            updateActions();
        }
    }

    /**
     * Manually triggers a redo operation.
     */
    public void redo() {
        if (undoManager.canRedo()) {
            try {
                undoManager.redo();
            } catch (CannotRedoException ex) {
                ex.printStackTrace();
            }
            updateActions();
        }
    }

    /**
     * Updates the enabled state of the undo and redo actions.
     */
    private void updateActions() {
        undoAction.setEnabled(undoManager.canUndo());
        redoAction.setEnabled(undoManager.canRedo());
    }

    /**
     * Returns the undo action for integration with UI components.
     *
     * @return the undo Action.
     */
    public Action getUndoAction() {
        return undoAction;
    }

    /**
     * Returns the redo action for integration with UI components.
     *
     * @return the redo Action.
     */
    public Action getRedoAction() {
        return redoAction;
    }
}
