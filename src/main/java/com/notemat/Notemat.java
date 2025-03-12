package com.notemat;

import com.notemat.components.*;
import com.notemat.filesystem.NTMFile;
import com.notemat.themes.Theme;
import com.notemat.utils.CustomWindowResizing;
import com.notemat.utils.UndoRedoManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Notemat extends JFrame {
    private final Font mainFont;
    private String savedFilePath = null;
    private boolean updatingButtons = false;
    private boolean bulletModeEnabled = false;
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
    private final UndoRedoManager undoRedoManager;
    private boolean changedSinceLastSave = false;
    private static final Color textColor = new Color(240, 240, 250);
    private boolean userChangedStyle = false;
    private static final Logger LOGGER = Logger.getLogger(Notemat.class.getName());

    public Notemat() {
        this(null);
    }

    public Notemat(File fileToOpen) {
        // Init
        setTitle("Notemat");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setIcon();

        // Default font
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        boolean hasLexend = false;
        for (String fontName : availableFonts) {
            if ("Lexend".equalsIgnoreCase(fontName)) {
                hasLexend = true;
                break;
            }
        }

        String defaultFont = hasLexend ? "Lexend" : "Arial";
        int defaultFontSize = defaultFont.equalsIgnoreCase("Lexend") ? 16 : 14;
        mainFont = new Font(defaultFont, Font.PLAIN, defaultFontSize);

        undoRedoManager = new UndoRedoManager();

        createStylePanel();
        createTextPane();
        addKeyBindings();
        createContentLayeredPane();
        createScrollPane();
        createMainContainer();

        setJMenuBar(new CustomMenuBar(this, undoRedoManager, mainFont));
        setVisible(true);
        new CustomWindowResizing(this);

        // Load file if given.
        if (fileToOpen != null) {
            loadFile(fileToOpen);
        }


    }

    /**
     * Saves the current file. Opens a file dialog if the file hasn't been saved before.
     */
    public void saveFile() {
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
                changedSinceLastSave = false;
                savedFilePath = filePath;
            } catch (IOException e) {
                MessagePopup.showMessage(
                        Notemat.this,
                        "Error",
                        "Error saving file: " + e.getMessage()
                );
            }
        }
    }

    /**
     * Loads a file from disk. Opens a file dialog to select the file.
     */
    public void loadFile() {
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
                MessagePopup.showMessage(
                        Notemat.this,
                        "Error",
                        "Error loading file: " + e.getMessage()
                );
            }
        }
    }

    /**
     * Loads a file from disk.
     */
    private void loadFile(File fileToOpen) {
        try {
            NTMFile.loadFromFile(this, fileToOpen.getAbsolutePath());
            savedFilePath = fileToOpen.getAbsolutePath();
        } catch (IOException | ClassNotFoundException e) {
            MessagePopup.showMessage(
                    Notemat.this,
                    "Error",
                    "Error loading file: " + e.getMessage()
            );
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
                        changedSinceLastSave = false;
                    } catch (IOException ex) {
                        MessagePopup.showMessage(
                                Notemat.this,
                                "Error",
                                "Error saving file: " + ex.getMessage()
                        );
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
        am.put("undo", undoRedoManager.getUndoAction());
        am.put("redo", undoRedoManager.getRedoAction());

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
        stylePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // ----------------------------
        // Font comboBox
        // ----------------------------
        String[] availableFonts = {"Lexend", "Arial", "Calibri", "Times New Roman", "Impact"};
        fontComboBox = new JComboBox<>(availableFonts);
        fontComboBox.setSelectedItem(mainFont.getName());
        Theme.applyFontComboBoxStyle(fontComboBox, mainFont);

        fontComboBox.addActionListener(e -> {
            userChangedStyle = true;
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
        Integer[] fontSizes = {8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 72, 80};
        fontSizeComboBox = new JComboBox<>(fontSizes);
        Theme.applyFontSizeComboBoxStyle(fontSizeComboBox, mainFont);

        fontSizeComboBox.addActionListener(e -> {
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if (start != end) {
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setFontSize(attributes, Objects.requireNonNullElse(selectedSize, 12));
                doc.setCharacterAttributes(start, end - start, attributes, false);
            } else {
                SimpleAttributeSet inputAttributes = new SimpleAttributeSet();
                StyleConstants.setFontSize(inputAttributes, Objects.requireNonNullElse(selectedSize, 12));
                applyStyleToCaret(inputAttributes);
            }
        });

        stylePanel.add(Box.createHorizontalStrut(5));
        stylePanel.add(fontSizeComboBox);

        // ----------------------------
        // Text color ComboBox
        // ----------------------------
        Color[] colors = {textColor, Theme.TEXT_COLOR_RED, Theme.TEXT_COLOR_PEACH, Theme.TEXT_COLOR_GREEN, Theme.TEXT_COLOR_BLUE, Theme.TEXT_COLOR_PINK};
        textColorComboBox = new JComboBox<>(colors);
        Theme.applyTextColorComboBoxStyle(textColorComboBox, mainFont);

        textColorComboBox.addActionListener(e -> {
            if (e.getSource() instanceof JComboBox && ((JComboBox<?>) e.getSource()).isPopupVisible()) {
                userChangedStyle = true;
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
            userChangedStyle = true;
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
            userChangedStyle = true;
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
            userChangedStyle = true;
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
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
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

        // Delegate the edits to the undo/redo manager.
        doc.addUndoableEditListener(undoRedoManager::addEdit);

        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedSinceLastSave = true;

                if (!updatingButtons) {
                    updateButtonStates();
                }
                SwingUtilities.invokeLater(() -> {
                    try {
                        String insertedText = doc.getText(e.getOffset(), e.getLength());
                        if (insertedText.contains("\n")) {
                            // Only inherit style if user hasn't explicitly changed it
                            if (!userChangedStyle) {
                                int prevCharPos = Math.max(0, e.getOffset() - 1);
                                AttributeSet attrs = doc.getCharacterElement(prevCharPos).getAttributes();
                                int newLineStart = e.getOffset() + 1;
                                SimpleAttributeSet newAttrs = new SimpleAttributeSet(attrs);
                                doc.setCharacterAttributes(newLineStart, 0, newAttrs, true);
                            }
                            // Handle bullet points if enabled
                            if (bulletModeEnabled) {
                                Element paragraph = doc.getParagraphElement(e.getOffset() + e.getLength());
                                int paraStart = paragraph.getStartOffset();
                                String paragraphText = doc.getText(paraStart, paragraph.getEndOffset() - paraStart);
                                if (!paragraphText.startsWith("• ")) {
                                    insertBulletPoint(paraStart);
                                }
                            }
                        }
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    } finally {
                        userChangedStyle = false;
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedSinceLastSave = true;

                if (!updatingButtons) {
                    updateButtonStates();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changedSinceLastSave = true;

                if (!updatingButtons) {
                    updateButtonStates();
                }
            }
        });

        textPane.addCaretListener(e -> SwingUtilities.invokeLater(() -> {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if (start != end) {
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
                    int selStart = textPane.getSelectionStart();
                    int selEnd = textPane.getSelectionEnd();
                    boolean hasSelection = selStart != selEnd;
                    if (hasImageFlavor(t.getTransferDataFlavors())) {
                        Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
                        insertImage((BufferedImage) image);
                        return true;
                    } else if (hasStyledTextFlavor(t.getTransferDataFlavors()) || hasStringFlavor(t.getTransferDataFlavors())) {
                        if (hasSelection) {
                            doc.remove(selStart, selEnd - selStart);
                        }
                        if (hasStyledTextFlavor(t.getTransferDataFlavors())) {
                            StyledText styledText = (StyledText) t.getTransferData(StyledTextTransferable.STYLED_TEXT_FLAVOR);
                            insertStyledText(styledText, selStart);
                            textPane.setCaretPosition(selStart + styledText.text.length());
                            return true;
                        } else {
                            String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                            doc.insertString(selStart, text, null);
                            textPane.setCaretPosition(selStart + text.length());
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
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

        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Hyperlink.processHyperlinkClick(textPane, e);

                Component clickedComponent = SwingUtilities.getDeepestComponentAt(contentLayeredPane, e.getX(), e.getY());
                if (clickedComponent == textPane) {
                    deselectAllImages();
                }
            }
        });

        textPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pos = textPane.viewToModel2D(e.getPoint());
                if (pos >= 0) {
                    StyledDocument doc = textPane.getStyledDocument();
                    Element elem = doc.getCharacterElement(pos);
                    AttributeSet as = elem.getAttributes();

                    // If hyperlink change cursor to hand.
                    if (as.getAttribute("HYPERLINK") != null) {
                        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }

                textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        });


        // Right click popup menu
        RightClickMenu.attachToComponent(textPane, textPane);
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
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(128);
        horizontalScrollBar.setUnitIncrement(16);
        horizontalScrollBar.setBlockIncrement(128);
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (changedSinceLastSave) {
                    JButton button1 = new JButton("Exit");
                    button1.setFocusPainted(false);
                    button1.addActionListener(e1 -> System.exit(0));

                    JButton button2 = new JButton("Save");
                    button2.setFocusPainted(false);
                    if (savedFilePath == null) {
                        button2.addActionListener(e1 -> {
                            saveFile();
                            System.exit(0);
                        });
                    } else {
                        button2.addActionListener(e1 -> {
                            try {
                                NTMFile.saveToFile(Notemat.this, savedFilePath);
                                System.exit(0);
                            } catch (IOException ex) {
                                MessagePopup.showMessage("Error", "Error saving file" + ex.getMessage());
                            }
                        });
                    }

                    MessagePopup.showMessage(
                            Notemat.this,
                            "Unsaved Changes",
                            "You have unsaved changes. Are you sure you want to exit?",
                            button1,
                            button2
                    );
                } else {
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Applies the given attributes to the caret. When no text is selected, the style should apply to the next typed character.
     * <p>
     * FIX: Instead of basing the new caret attributes on the previous character (which might still hold old style values),
     * we now base them on the current input attributes. This ensures that explicit user style changes (bold, italic, underline)
     * are immediately applied.
     *
     * @param attributes The attributes to apply.
     */
    private void applyStyleToCaret(AttributeSet attributes) {
        MutableAttributeSet newAttributes = new SimpleAttributeSet(textPane.getInputAttributes());
        newAttributes.addAttributes(attributes);
        fontComboBox.setSelectedItem(StyleConstants.getFontFamily(newAttributes));
        textPane.setCharacterAttributes(newAttributes, false);
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
                    attributes = doc.getCharacterElement(start).getAttributes();
                } else {
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
        Point caretPos = textPane.getCaret().getMagicCaretPosition();
        if (caretPos == null) {
            caretPos = new Point(10, 10);
        }
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
     * Updates the bounds of the text pane (and thus the preferred size of the layered pane)
     * to be at least as large as the viewport. If the text content requires more space, its natural preferred size is used.
     */
    private void updateTextPaneSize() {
        Dimension viewportSize = (scrollPane != null && scrollPane.getViewport() != null) ? scrollPane.getViewport().getExtentSize() : new Dimension(1024, 600);
        try {
            Dimension textPref = textPane.getUI().getPreferredSize(textPane);
            int width = Math.max(viewportSize.width, textPref.width);
            int height = Math.max(viewportSize.height, textPref.height);
            textPane.setBounds(0, 0, width, height);
            contentLayeredPane.setPreferredSize(new Dimension(width, height));
            contentLayeredPane.revalidate();
        } catch (Exception e) {
            // happens.
        }
    }

    /**
     * Inserts a bullet point at the given position in the document.
     *
     * @param position The position to insert the bullet point.
     */
    private void insertBulletPoint(int position) {
        try {
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
            MutableAttributeSet bulletAttributes = new SimpleAttributeSet(attributes);
            Color textColor = StyleConstants.getForeground(attributes);
            if (textColor == null || textColor.equals(Notemat.textColor)) {
                textColor = (Color) textColorComboBox.getSelectedItem();
            }
            StyleConstants.setForeground(bulletAttributes, textColor);
            doc.insertString(position, "• ", bulletAttributes);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
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
            if (comp instanceof ImageComponent imgComp) {
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
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/notemat.png")));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }
    }
}
