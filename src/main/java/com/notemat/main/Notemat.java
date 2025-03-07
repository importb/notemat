package com.notemat.main;

import com.notemat.components.ImageComponent;
import com.notemat.components.StyledText;
import com.notemat.components.StyledTextTransferable;
import com.notemat.filesystem.NTMFile;
import com.notemat.themes.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Notemat extends JFrame {
    private final Font mainFont;
    private final Cursor lastCursor = getCursor();
    private final int borderThickness = 5;
    private final UndoManager undoManager;
    private String savedFilePath = null;
    private boolean userInitiatedStyleChange = false;
    private boolean updatingButtons = false;
    private boolean isResizing = false;
    private boolean bulletModeEnabled = false;
    private Point initialResizeClick;
    private Rectangle initialBounds;
    private Point initialClick;
    private Action undoAction;
    private Action redoAction;
    private JTextPane textPane;
    private StyledDocument doc;
    private JPanel stylePanel;
    private JComboBox<String> fontComboBox;
    private JComboBox<Integer> fontSizeComboBox;
    private JLayeredPane contentLayeredPane;
    private JScrollPane scrollPane;
    private JToggleButton boldButton;
    private JToggleButton italicButton;
    private JToggleButton underlineButton;
    private JComboBox<Color> textColorComboBox;
    private static final Color textColor = new Color(240, 240, 250);
    private static final Color textColorRed = new Color(235, 64, 52);
    private static final Color textColorOrange = new Color(235, 156, 52);
    private static final Color textColorGreen = new Color(113, 235, 52);
    private static final Color textColorBlue = new Color(52, 134, 235);
    private static final Color textColorPink = new Color(235, 52, 217);


    public Notemat() {
        this(null);
    }


    public Notemat(File fileToOpen) {
        // Init
        setTitle("Notemat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 600);
        setLocationRelativeTo(null);


        setUndecorated(true);

        setIcon();

        // Default font
        String defaultFont = (Font.decode("Lexend") != null) ? "Lexend" : "Arial";
        int defaultFontSize = (defaultFont.equals("Lexend")) ? 16 : 14;
        mainFont = new Font(defaultFont, Font.PLAIN, defaultFontSize);

        undoManager = new UndoManager();
        createUndoRedoActions();
        createStylePanel();
        createTextPane();
        addKeyBindings();
        createContentLayeredPane();
        createScrollPane();
        createMainContainer();

        createMenuBar();
        createGlassPane();

        setVisible(true);

        if (fileToOpen != null) {
            loadFile(fileToOpen);
        }
    }

    /**
     * Saves the current file. Opens a file dialog if the file hasn't been saved before.
     */
    private void saveFile() {
        deselectAllImages();

        FileDialog fileDialog = new FileDialog(this, "Save File", FileDialog.SAVE);
        fileDialog.setFile("*.ntm");
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String fileName = fileDialog.getFile();

        if (directory != null && fileName != null) {
            String filePath = directory + fileName;
            if (!filePath.endsWith(".ntm")) {
                filePath += ".ntm";
            }
            try {
                NTMFile.saveToFile(this, filePath);
                savedFilePath = filePath;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Loads a file from disk. Opens a file dialog to select the file.
     */
    private void loadFile() {
        FileDialog fileDialog = new FileDialog(this, "Open File", FileDialog.LOAD);
        fileDialog.setFile("*.ntm");
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String fileName = fileDialog.getFile();

        if (directory != null && fileName != null) {
            String filePath = directory + fileName;
            try {
                NTMFile.loadFromFile(this, filePath);
                savedFilePath = filePath;
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Loads a file from disk. */
    private void loadFile(File fileToOpen) {
        try {
            NTMFile.loadFromFile(this, fileToOpen.getAbsolutePath());
            savedFilePath = fileToOpen.getAbsolutePath();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                    this, "Error loading file: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds key bindings for various actions like bold, italic, underline, etc.
     */
    public void addKeyBindings() {
        InputMap im = textPane.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textPane.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK), "bold");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), "italic");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), "underline");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "load");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteImage");

        am.put("bold", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boldButton.doClick();
            }
        });

        am.put("italic", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                italicButton.doClick();
            }
        });

        am.put("underline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                underlineButton.doClick();
            }
        });

        am.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (savedFilePath == null) {
                    saveFile();
                } else {
                    try {
                        NTMFile.saveToFile(Notemat.this, savedFilePath);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(Notemat.this, "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        am.put("load", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });

        am.put("deleteImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedImages();
            }
        });

        am.put("undo", undoAction);
        am.put("redo", redoAction);

        // Image deletion
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedImages();
                return true;
            }
            return false;
        });
    }

    /**
     * Style panel initialization
     */
    public void createStylePanel() {
        // Create font panel
        stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Theme.applyStylePanelStyle(stylePanel);

        // ----------------------------
        // Font comboBox
        // ----------------------------
        String[] availableFonts = {"Lexend", "Arial", "Calibri", "Times New Roman", "Impact"};

        fontComboBox = new JComboBox<>(availableFonts);
        fontComboBox.setSelectedItem(mainFont.getName());

        Theme.applyFontComboBoxStyle(fontComboBox, mainFont);

        fontComboBox.addActionListener(e -> {
            userInitiatedStyleChange = true;
            String selectedFont = (String) fontComboBox.getSelectedItem();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            if (start != end) {
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setFontFamily(attributes, selectedFont);
                doc.setCharacterAttributes(start, end - start, attributes, false);
            } else {
                SimpleAttributeSet inputAttributes = new SimpleAttributeSet();
                StyleConstants.setFontFamily(inputAttributes, selectedFont);
                applyStyleToCaret(inputAttributes);
            }
        });

        JLabel fontLabel = new JLabel("Font: ");
        Theme.applyLabelStyle(fontLabel, mainFont);

        stylePanel.add(fontLabel);
        stylePanel.add(fontComboBox);

        // ----------------------------
        // Font Size ComboBox
        // ----------------------------
        Integer[] fontSizes = {8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 72};
        fontSizeComboBox = new JComboBox<>(fontSizes);
        Theme.applyFontSizeComboBoxStyle(fontSizeComboBox, mainFont);

        fontSizeComboBox.addActionListener(e -> {
            userInitiatedStyleChange = true;
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            if (start != end) {
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setFontSize(attributes, selectedSize);
                doc.setCharacterAttributes(start, end - start, attributes, false);
            } else {
                SimpleAttributeSet inputAttributes = new SimpleAttributeSet();
                StyleConstants.setFontSize(inputAttributes, selectedSize);
                applyStyleToCaret(inputAttributes);
            }
        });

        stylePanel.add(Box.createHorizontalStrut(5));
        stylePanel.add(fontSizeComboBox);

        // ----------------------------
        // Text color ComboBox
        // ----------------------------
        Color[] colors = {textColor, textColorRed, textColorOrange, textColorGreen, textColorBlue, textColorPink};
        textColorComboBox = new JComboBox<>(colors);
        Theme.applyTextColorComboBoxStyle(textColorComboBox, mainFont);

        textColorComboBox.addActionListener(e -> {
            if (e.getSource() instanceof JComboBox && ((JComboBox<?>) e.getSource()).isPopupVisible()) {
                userInitiatedStyleChange = true;
                Color selectedColor = (Color) textColorComboBox.getSelectedItem();
                int start = textPane.getSelectionStart();
                int end = textPane.getSelectionEnd();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, selectedColor);

                if (start != end) {
                    doc.setCharacterAttributes(start, end - start, attributes, false);
                } else {
                    applyStyleToCaret(attributes);
                }
            }
        });

        stylePanel.add(Box.createHorizontalStrut(5));
        stylePanel.add(textColorComboBox);

        // ----------------------------
        // Bold, Italic, Underline Buttons
        // ----------------------------
        boldButton = new JToggleButton("<html><b>Bold</b></html>");
        Theme.applyStylePanelButtonStyle(boldButton, mainFont);

        italicButton = new JToggleButton("<html><i>Itallic</i></html>");
        Theme.applyStylePanelButtonStyle(italicButton, mainFont);

        underlineButton = new JToggleButton("<html><u>Underline</u></html>");
        Theme.applyStylePanelButtonStyle(underlineButton, mainFont);

        boldButton.addActionListener(e -> {
            userInitiatedStyleChange = true;
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setBold(attributes, boldButton.isSelected());

            if (start != end) {
                doc.setCharacterAttributes(start, end - start, attributes, false);
            } else {
                applyStyleToCaret(attributes);
            }
        });

        italicButton.addActionListener(e -> {
            userInitiatedStyleChange = true;
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setItalic(attributes, italicButton.isSelected());

            if (start != end) {
                doc.setCharacterAttributes(start, end - start, attributes, false);
            } else {
                applyStyleToCaret(attributes);
            }
        });

        underlineButton.addActionListener(e -> {
            userInitiatedStyleChange = true;
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setUnderline(attributes, underlineButton.isSelected());

            if (start != end) {
                doc.setCharacterAttributes(start, end - start, attributes, false);
            } else {
                applyStyleToCaret(attributes);
            }
        });

        // Bullet Point Button
        JToggleButton bulletButton = new JToggleButton("Bullet Point");
        Theme.applyStylePanelButtonStyle(bulletButton, mainFont);

        bulletButton.addActionListener(e -> {
            bulletModeEnabled = bulletButton.isSelected();
            int selectionStart = textPane.getSelectionStart();
            int selectionEnd = textPane.getSelectionEnd();

            try {
                Element startPara = doc.getParagraphElement(selectionStart);
                Element endPara = doc.getParagraphElement(selectionEnd);

                for (int i = startPara.getStartOffset(); i <= endPara.getStartOffset(); i = doc.getParagraphElement(i).getEndOffset()) {
                    Element paragraph = doc.getParagraphElement(i);
                    int paraStart = paragraph.getStartOffset();
                    String paragraphText = doc.getText(paraStart, paragraph.getEndOffset() - paraStart);

                    if (bulletModeEnabled && !paragraphText.startsWith("• ")) {
                        insertBulletPoint(paraStart);
                    } else if (!bulletModeEnabled && paragraphText.startsWith("• ")) {
                        doc.remove(paraStart, 2);
                    }
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });

        stylePanel.add(boldButton);
        stylePanel.add(Box.createHorizontalStrut(5));
        stylePanel.add(italicButton);
        stylePanel.add(Box.createHorizontalStrut(5));
        stylePanel.add(underlineButton);
        stylePanel.add(Box.createHorizontalStrut(15));
        stylePanel.add(bulletButton);
        stylePanel.add(Box.createHorizontalStrut(5));
    }


    /**
     * TextPane initialization
     */
    public void createTextPane() {
        textPane = new JTextPane();
        Theme.applyTextPaneStyle(textPane, mainFont);

        doc = textPane.getStyledDocument();

        doc.addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
            updateUndoRedoState();
        });

        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!updatingButtons) {
                    updateButtonStates();
                }

                if (!bulletModeEnabled) return;

                SwingUtilities.invokeLater(() -> {
                    try {
                        String insertedText = doc.getText(e.getOffset(), e.getLength());
                        if (insertedText.contains("\n")) {
                            Element paragraph = doc.getParagraphElement(e.getOffset() + e.getLength());
                            int paraStart = paragraph.getStartOffset();
                            String paragraphText = doc.getText(paraStart, paragraph.getEndOffset() - paraStart);

                            if (!paragraphText.startsWith("• ")) {
                                insertBulletPoint(paraStart);
                            }
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!updatingButtons) {
                    updateButtonStates();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!updatingButtons) {
                    updateButtonStates();
                }
            }
        });

        textPane.addCaretListener(e -> SwingUtilities.invokeLater(() -> {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if (start != end) {
                // Check if mixed sizes.
                boolean mixedSizes = false;
                int firstFontSize = StyleConstants.getFontSize(doc.getCharacterElement(start).getAttributes());
                for (int i = start + 1; i < end; i++) {
                    int fontSize = StyleConstants.getFontSize(doc.getCharacterElement(i).getAttributes());
                    if (fontSize != firstFontSize) {
                        mixedSizes = true;
                        break;
                    }
                }
                if (mixedSizes) {
                    fontSizeComboBox.setEnabled(false);
                } else {
                    fontSizeComboBox.setEnabled(true);
                    fontSizeComboBox.setSelectedItem(firstFontSize);
                }
            } else {
                fontSizeComboBox.setEnabled(true);
                AttributeSet attributes = textPane.getInputAttributes();
                Integer fontSize = StyleConstants.getFontSize(attributes);
                fontSizeComboBox.setSelectedItem(fontSize);
            }

            updateButtonStates();
        }));

        textPane.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                JTextPane textPane = (JTextPane) c;
                int start = textPane.getSelectionStart();
                int end = textPane.getSelectionEnd();
                if (start == end) {
                    return null;
                }
                StyledDocument styledDoc = textPane.getStyledDocument();
                return new StyledTextTransferable(styledDoc, start, end);
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                for (DataFlavor flavor : transferFlavors) {
                    if (flavor.equals(DataFlavor.imageFlavor) || flavor.equals(DataFlavor.stringFlavor) || flavor.equals(StyledTextTransferable.STYLED_TEXT_FLAVOR)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    // Get selection start/end
                    int selStart = textPane.getSelectionStart();
                    int selEnd = textPane.getSelectionEnd();
                    boolean hasSelection = selStart != selEnd;

                    if (hasImageFlavor(t.getTransferDataFlavors())) {
                        // For images, just insert at caret position without removing selection
                        Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
                        insertImage((BufferedImage) image);
                        return true;
                    } else if (hasStyledTextFlavor(t.getTransferDataFlavors()) || hasStringFlavor(t.getTransferDataFlavors())) {
                        // For text, remove selection first if there is one
                        if (hasSelection) {
                            doc.remove(selStart, selEnd - selStart);
                        }

                        // The position to insert at is now the selection start
                        int insertPosition = selStart;

                        if (hasStyledTextFlavor(t.getTransferDataFlavors())) {
                            StyledText styledText = (StyledText) t.getTransferData(StyledTextTransferable.STYLED_TEXT_FLAVOR);
                            insertStyledText(styledText, insertPosition);
                            textPane.setCaretPosition(insertPosition + styledText.text.length());
                            return true;
                        } else {
                            String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                            doc.insertString(insertPosition, text, null);
                            textPane.setCaretPosition(insertPosition + text.length());
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }


            private boolean hasImageFlavor(DataFlavor[] flavors) {
                for (DataFlavor flavor : flavors) {
                    if (DataFlavor.imageFlavor.equals(flavor)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean hasStringFlavor(DataFlavor[] flavors) {
                for (DataFlavor flavor : flavors) {
                    if (DataFlavor.stringFlavor.equals(flavor)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean hasStyledTextFlavor(DataFlavor[] flavors) {
                for (DataFlavor flavor : flavors) {
                    if (StyledTextTransferable.STYLED_TEXT_FLAVOR.equals(flavor)) {
                        return true;
                    }
                }
                return false;
            }

            private void insertStyledText(StyledText styledText, int position) throws BadLocationException {
                String text = styledText.text;
                AttributeSet[] attributes = styledText.attributes;

                for (int i = 0; i < text.length(); i++) {
                    doc.insertString(position + i, String.valueOf(text.charAt(i)), attributes[i]);
                }
            }
        });

        // Add a mouse listener to the textPane to deselect images when clicking outside them
        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check if the click was directly on the text pane (not on an image)
                Component clickedComponent = SwingUtilities.getDeepestComponentAt(
                        contentLayeredPane, e.getX(), e.getY());

                // If we clicked directly on the textPane, deselect all images
                if (clickedComponent == textPane) {
                    deselectAllImages();
                }
            }
        });
    }


    /**
     * Creates the layered pane that holds the text pane and any images.
     */
    public void createContentLayeredPane() {
        contentLayeredPane = new JLayeredPane();
        contentLayeredPane.setLayout(null);
        contentLayeredPane.add(textPane, JLayeredPane.DEFAULT_LAYER);
        contentLayeredPane.setBorder(BorderFactory.createEmptyBorder());

        updateTextPaneSize();

        // Listen for text changes to update size
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTextPaneSize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (textPane.getSelectionStart() == textPane.getSelectionEnd()) {
                    updateTextPaneSize();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (textPane.getSelectionStart() == textPane.getSelectionEnd()) {
                    updateTextPaneSize();
                }
            }
        });
    }


    /**
     * Creates the scroll pane that contains the content layered pane.
     */
    public void createScrollPane() {
        scrollPane = new JScrollPane(contentLayeredPane);
        Theme.applyScrollPaneStyle(scrollPane, mainFont);

        // Increase scroll speed by setting larger unit and block increments
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(128);
        horizontalScrollBar.setUnitIncrement(16);
        horizontalScrollBar.setBlockIncrement(128);

        // When the viewport changes size, update our text pane bounds.
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTextPaneSize();
            }
        });
    }


    /**
     * Creates the main container panel and adds the style panel and scroll pane.
     */
    private void createMainContainer() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        Theme.applyMainContainerStyle(mainPanel);

        mainPanel.add(stylePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }


    /**
     * Creates the menu bar with file and edit menus.
     */
    private void createMenuBar() {
        Action saveFileAction = new AbstractAction("Save File") {
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        };
        Action loadFileAction = new AbstractAction("Load File") {
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        };

        Action closeAction = new AbstractAction("x") {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        Action minimizeAction = new AbstractAction("-") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setExtendedState(JFrame.ICONIFIED);
            }
        };

        JMenuBar menuBar = new JMenuBar();
        menuBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        menuBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // get location of Window
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });

        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton minimizeButton = new JButton(minimizeAction);
        JButton closeButton = new JButton(closeAction);
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

        JMenuItem saveFileMenuItem = new JMenuItem(saveFileAction);
        fileMenu.add(saveFileMenuItem);

        JMenuItem loadFileMenuItem = new JMenuItem(loadFileAction);
        fileMenu.add(loadFileMenuItem);

        JMenuItem undoMenuItem = new JMenuItem(undoAction);
        JMenuItem redoMenuItem = new JMenuItem(redoAction);
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(minimizeButton);
        menuBar.add(Box.createHorizontalStrut(8));
        menuBar.add(closeButton);
        menuBar.add(Box.createHorizontalStrut(5));

        Theme.applyMenuBarStyle(menuBar, mainFont);
        setJMenuBar(menuBar);
    }


    /**
     * Creates the glass pane for handling window resizing.
     */
    private void createGlassPane() {
        JPanel glassPane = new JPanel(null) {
            @Override
            public boolean contains(int x, int y) {
                int w = getWidth();
                int h = getHeight();

                return (x <= borderThickness || x >= w - borderThickness || y <= borderThickness || y >= h - borderThickness);
            }
        };
        glassPane.setOpaque(false);
        setGlassPane(glassPane);
        getGlassPane().setVisible(true);

        glassPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Only start resizing if we're in a resize zone
                if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
                    initialResizeClick = e.getLocationOnScreen();
                    initialBounds = getBounds();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isResizing = false;
                setCursor(lastCursor);
            }
        });

        glassPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int x = p.x;
                int y = p.y;
                int w = getWidth();
                int h = getHeight();

                if (x <= borderThickness && y <= borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                } else if (x >= w - borderThickness && y <= borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                } else if (x <= borderThickness && y >= h - borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                } else if (x >= w - borderThickness && y >= h - borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else if (x <= borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                } else if (x >= w - borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (y <= borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                } else if (y >= h - borderThickness) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else {
                    setCursor(lastCursor);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Only proceed if resizing
                if (initialResizeClick == null || initialBounds == null) return;
                isResizing = true;

                Point currentPoint = e.getLocationOnScreen();
                int deltaX = currentPoint.x - initialResizeClick.x;
                int deltaY = currentPoint.y - initialResizeClick.y;

                int newX = initialBounds.x;
                int newY = initialBounds.y;
                int newWidth = initialBounds.width;
                int newHeight = initialBounds.height;

                int cursorType = getCursor().getType();
                switch (cursorType) {
                    case Cursor.NW_RESIZE_CURSOR:
                        newX = initialBounds.x + deltaX;
                        newY = initialBounds.y + deltaY;
                        newWidth = initialBounds.width - deltaX;
                        newHeight = initialBounds.height - deltaY;
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        newY = initialBounds.y + deltaY;
                        newWidth = initialBounds.width + deltaX;
                        newHeight = initialBounds.height - deltaY;
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        newX = initialBounds.x + deltaX;
                        newWidth = initialBounds.width - deltaX;
                        newHeight = initialBounds.height + deltaY;
                        break;
                    case Cursor.SE_RESIZE_CURSOR:
                        newWidth = initialBounds.width + deltaX;
                        newHeight = initialBounds.height + deltaY;
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        newX = initialBounds.x + deltaX;
                        newWidth = initialBounds.width - deltaX;
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        newWidth = initialBounds.width + deltaX;
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        newY = initialBounds.y + deltaY;
                        newHeight = initialBounds.height - deltaY;
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        newHeight = initialBounds.height + deltaY;
                        break;
                    default:
                        break;
                }

                newWidth = Math.max(newWidth, 400);
                newHeight = Math.max(newHeight, 300);

                setBounds(newX, newY, newWidth, newHeight);
                revalidate();
                repaint();
            }
        });
    }


    /**
     * Applies the given attributes to the caret. Used when no text is selected, and the style should
     * apply to the next typed character.
     *
     * @param attributes The attributes to apply.
     */
    private void applyStyleToCaret(AttributeSet attributes) {
        if (userInitiatedStyleChange) {
            int caretPosition = textPane.getCaretPosition();
            if (caretPosition > 0) {
                // Get the attributes of the character before the caret
                Element run = doc.getCharacterElement(caretPosition - 1);
                AttributeSet previousAttributes = run.getAttributes();

                // Create a mutable copy of the previous attributes
                MutableAttributeSet mutableAttributes = new SimpleAttributeSet(previousAttributes);

                // Override the attributes with the new ones
                mutableAttributes.addAttributes(attributes);

                fontComboBox.setSelectedItem(StyleConstants.getFontFamily(mutableAttributes));

                textPane.setCharacterAttributes(mutableAttributes, false);
            } else {
                textPane.setCharacterAttributes(attributes, false);
            }
            userInitiatedStyleChange = false;
        }
    }


    /**
     * Updates the state of the bold, italic, and underline buttons based on the current selection.
     */
    private void updateButtonStates() {
        if (updatingButtons) return;

        updatingButtons = true;
        try {
            SwingUtilities.invokeLater(() -> {
                int start = textPane.getSelectionStart();
                int end = textPane.getSelectionEnd();

                AttributeSet attributes;
                if (start != end) {
                    // If there's a selection, get the attributes of the first character in the selection.
                    attributes = doc.getCharacterElement(start).getAttributes();
                } else {
                    // If there's no selection, get the input attributes.
                    attributes = textPane.getInputAttributes();
                }

                boldButton.setSelected(StyleConstants.isBold(attributes));
                italicButton.setSelected(StyleConstants.isItalic(attributes));
                underlineButton.setSelected(StyleConstants.isUnderline(attributes));

                Color foregroundColor = StyleConstants.getForeground(attributes);
                textColorComboBox.setSelectedItem(foregroundColor);
            });
        } finally {
            updatingButtons = false;
        }
    }


    /**
     * Inserts an image into the text pane at the current caret position.
     *
     * @param image The image to insert.
     */
    private void insertImage(BufferedImage image) {
        ImageIcon imageIcon = new ImageIcon(image);
        ImageComponent imageComponent = new ImageComponent(imageIcon, image);

        // Find caret position in textPane coordinates.
        Point caretPos = textPane.getCaret().getMagicCaretPosition();
        if (caretPos == null) {
            caretPos = new Point(10, 10);
        }

        // Convert to layered pane coordinates.
        Point converted = SwingUtilities.convertPoint(textPane, caretPos, contentLayeredPane);
        int iconWidth = imageIcon.getIconWidth();
        int iconHeight = imageIcon.getIconHeight();
        imageComponent.setBounds(converted.x, converted.y, iconWidth, iconHeight);
        contentLayeredPane.add(imageComponent, JLayeredPane.PALETTE_LAYER);
        contentLayeredPane.repaint();
    }


    /**
     * Deletes any selected images from the content layered pane.
     */
    private void deleteSelectedImages() {
        for (Component comp : contentLayeredPane.getComponents()) {
            if (comp instanceof ImageComponent imageComponent) {
                if (imageComponent.isSelected()) {
                    contentLayeredPane.remove(imageComponent);
                }
            }
        }
        contentLayeredPane.revalidate();
        contentLayeredPane.repaint();
    }


    /**
     * Updates the bounds of the text pane (and thus the preferred size of the layered pane) to be at
     * least as large as the viewport. If the text content requires more space, its natural preferred
     * size is used.
     */
    private void updateTextPaneSize() {
        // Get the current viewport size (fallback if not yet set)
        Dimension viewportSize = (scrollPane != null && scrollPane.getViewport() != null) ? scrollPane.getViewport().getExtentSize() : new Dimension(1024, 600);

        try {
            // Get the natural preferred size of the text pane
            Dimension textPref = textPane.getUI().getPreferredSize(textPane);
            int width = Math.max(viewportSize.width, textPref.width);
            int height = Math.max(viewportSize.height, textPref.height);
            textPane.setBounds(0, 0, width, height);
            contentLayeredPane.setPreferredSize(new Dimension(width, height));
            contentLayeredPane.revalidate();
        } catch (Exception e) {
        }
    }


    /**
     * Updates the enabled/disabled state of the undo and redo actions.
     */
    private void updateUndoRedoState() {
        boolean canUndo = undoManager.canUndo();
        boolean canRedo = undoManager.canRedo();
        undoAction.setEnabled(canUndo);
        redoAction.setEnabled(canRedo);
    }


    /**
     * Creates the undo and redo actions and associates them with the undo manager.
     */
    private void createUndoRedoActions() {
        undoAction = new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                    updateButtonStates();
                    updateUndoRedoState();
                }
            }
        };
        redoAction = new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                    updateButtonStates();
                    updateUndoRedoState();
                }
            }
        };
        updateUndoRedoState();
    }


    /**
     * Inserts a bullet point at the given position in the document.
     *
     * @param position The position to insert the bullet point.
     */
    private void insertBulletPoint(int position) {
        try {
            // Get attributes either from previous character or current input attributes
            AttributeSet attributes;
            if (position < doc.getLength()) {
                Element elementAfter = doc.getCharacterElement(position);
                attributes = elementAfter.getAttributes();
            } else if (position > 0) {
                Element element = doc.getCharacterElement(position - 1);
                attributes = element.getAttributes();
            } else {
                attributes = textPane.getInputAttributes();
            }

            // Create new attributes based on the inherited ones
            MutableAttributeSet bulletAttributes = new SimpleAttributeSet(attributes);

            Color textColor = StyleConstants.getForeground(attributes);
            if (textColor == null || textColor.equals(this.textColor)) {
                textColor = (Color) textColorComboBox.getSelectedItem();
            }
            StyleConstants.setForeground(bulletAttributes, textColor);

            // Insert the bullet point with the correct color
            doc.insertString(position, "• ", bulletAttributes);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Gets the text pane.
     *
     * @return The text pane.
     */
    public JTextPane getTextPane() {
        return textPane;
    }


    /**
     * Gets the content layered pane.
     *
     * @return The content layered pane.
     */
    public JLayeredPane getContentLayeredPane() {
        return contentLayeredPane;
    }


    /**
     * Deselects all imageComponents.
     */
    private void deselectAllImages() {
        for (Component comp : contentLayeredPane.getComponents()) {
            if (comp instanceof ImageComponent) {
                ImageComponent imgComp = (ImageComponent) comp;
                if (imgComp.isSelected()) {
                    imgComp.setSelected(false);
                    imgComp.setBorder(BorderFactory.createEmptyBorder());
                    imgComp.repaint();
                }
            }
        }
    }

    /**
     * Sets the application's icon.
     */
    private void setIcon() {
        try {
            setIconImage(null);
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/notemat.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }
    }
}
