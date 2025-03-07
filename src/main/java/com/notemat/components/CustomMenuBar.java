package com.notemat.components;

import com.notemat.Notemat;
import com.notemat.themes.Theme;
import com.notemat.utils.UndoRedoManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class CustomMenuBar extends JMenuBar {
    private final Notemat notematFrame;
    private final UndoRedoManager undoRedoManager;
    private final Font mainFont;
    private Point initialClick;

    public CustomMenuBar(Notemat frame, UndoRedoManager undoRedoManager, Font mainFont) {
        this.notematFrame = frame;
        this.undoRedoManager = undoRedoManager;
        this.mainFont = mainFont;
        initMenuBar();
    }

    private void initMenuBar() {
        // Allow dragging the window when clicking on the menu bar.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                notematFrame.getComponentAt(initialClick);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = notematFrame.getLocation().x;
                int thisY = notematFrame.getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                notematFrame.setLocation(X, Y);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                notematFrame.setCursor(Cursor.getDefaultCursor());
            }
        });

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // --- Create actions ---
        Action saveFileAction = new AbstractAction("Save File") {
            @Override
            public void actionPerformed(ActionEvent e) {
                notematFrame.saveFile();
            }
        };

        Action loadFileAction = new AbstractAction("Load File") {
            @Override
            public void actionPerformed(ActionEvent e) {
                notematFrame.loadFile();
            }
        };

        Action closeAction = new AbstractAction("x") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        Action minimizeAction = new AbstractAction("-") {
            @Override
            public void actionPerformed(ActionEvent e) {
                notematFrame.setExtendedState(JFrame.ICONIFIED);
            }
        };

        // --- Build File and Edit Menus ---
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(saveFileAction));
        fileMenu.add(new JMenuItem(loadFileAction));

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem(undoRedoManager.getUndoAction()));
        editMenu.add(new JMenuItem(undoRedoManager.getRedoAction()));

        // --- Add minimize and close buttons ---
        JButton minimizeButton = new JButton(minimizeAction);
        JButton closeButton = new JButton(closeAction);

        // --- Assemble the Menu Bar ---
        add(fileMenu);
        add(editMenu);
        add(Box.createHorizontalGlue());
        add(minimizeButton);
        add(Box.createHorizontalStrut(8));
        add(closeButton);
        add(Box.createHorizontalStrut(5));

        // --- Apply theming ---
        Theme.applyMenuBarStyle(this, mainFont);
    }
}
