package com.notemat.themes;

import com.notemat.components.ColorIcon;
import com.notemat.components.MessagePopup;
import com.notemat.components.RightClickMenu;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class Theme {
    public static final Color MAIN_COLOR_1 = new Color(32, 32, 50);
    public static final Color MAIN_COLOR_2 = new Color(24, 24, 36);
    public static final Color MAIN_COLOR_3 = new Color(20, 20, 30);
    public static final Color MAIN_COLOR_4 = new Color(15, 15, 20);
    public static final Color TEXT_COLOR = new Color(240, 240, 250);

    public static final Color TEXT_COLOR_RED = new Color(235, 64, 52);
    public static final Color TEXT_COLOR_PEACH = new Color(255, 203, 164);
    public static final Color TEXT_COLOR_GREEN = new Color(113, 235, 52);
    public static final Color TEXT_COLOR_BLUE = new Color(52, 134, 235);
    public static final Color TEXT_COLOR_PINK = new Color(235, 52, 217);

    // Font
    // todo: make every function use this.
    private static final String defaultFont = (Font.decode("Lexend") != null) ? "Lexend" : "Arial";
    private static final int defaultFontSize = (defaultFont.equals("Lexend")) ? 16 : 14;
    public static final Font MAIN_FONT = new Font(defaultFont, Font.PLAIN, defaultFontSize);

    public static void applyMenuBarStyle(JMenuBar menuBar, Font font) {
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        menuBar.setBackground(MAIN_COLOR_4);
        menuBar.setForeground(TEXT_COLOR);

        for (Component component : menuBar.getComponents()) {
            if (component instanceof JMenu menu) {
                menu.setFont(font);
                menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                menu.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                menu.setForeground(TEXT_COLOR);

                for (Component menuItemComponent : menu.getMenuComponents()) {
                    if (menuItemComponent instanceof JMenuItem menuItem) {
                        menuItem.setFont(font);
                        menuItem.setBorder(null);
                        menuItem.setBackground(MAIN_COLOR_1);
                        menuItem.setForeground(TEXT_COLOR);
                    }
                }
            } else if (component instanceof JButton button) {
                button.setFont(font);
                button.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
                button.setBackground(MAIN_COLOR_4);
                button.setForeground(TEXT_COLOR);
                button.setFocusable(false);
            }
        }
    }


    public static void applyPopUpStyle(RightClickMenu menu) {
        menu.setBackground(MAIN_COLOR_2);
        menu.setForeground(TEXT_COLOR);
        menu.setBorder(BorderFactory.createLineBorder(MAIN_COLOR_3, 2));

        for (Component comp : menu.getComponents()) {
            if (comp instanceof JMenuItem menuItem) {
                menuItem.setBackground(MAIN_COLOR_2);
                menuItem.setForeground(TEXT_COLOR);
                menuItem.setFont(MAIN_FONT);
                menuItem.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                menuItem.setFocusPainted(false);
            }
        }
    }


    public static void applyScrollPaneStyle(JScrollPane scrollPane, Font font) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel transparentPanel = new JPanel();
        transparentPanel.setForeground(MAIN_COLOR_2);
        transparentPanel.setBackground(MAIN_COLOR_2);
        scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, transparentPanel);


        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        verticalScrollBar.setBorder(BorderFactory.createEmptyBorder());
        verticalScrollBar.setBackground(MAIN_COLOR_2);
        verticalScrollBar.setForeground(MAIN_COLOR_3);
        verticalScrollBar.setFocusable(false);
        verticalScrollBar.setUI(new ThemedScrollBarUI());

        horizontalScrollBar.setBorder(BorderFactory.createEmptyBorder());
        horizontalScrollBar.setBackground(MAIN_COLOR_2);
        horizontalScrollBar.setForeground(MAIN_COLOR_3);
        horizontalScrollBar.setFocusable(false);
        horizontalScrollBar.setUI(new ThemedScrollBarUI());
    }


    public static void applyMainContainerStyle(JPanel mainContainer) {
        mainContainer.setFocusable(false);
        mainContainer.setBorder(BorderFactory.createLineBorder(MAIN_COLOR_4, 2));
        mainContainer.setBackground(MAIN_COLOR_4);
    }


    public static void applyStylePanelButtonStyle(JToggleButton button, Font font) {
        button.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
        button.setBackground(MAIN_COLOR_3);
        button.setForeground(TEXT_COLOR);
        button.setFocusable(false);
        button.setFont(font);
    }

    public static void applyLabelStyle(JLabel label, Font font) {
        label.setForeground(TEXT_COLOR);
        label.setBorder(BorderFactory.createEmptyBorder());
        label.setFont(font);
    }

    public static void applyTextColorComboBoxStyle(JComboBox textColorComboBox, Font font) {
        textColorComboBox.setBorder(BorderFactory.createEmptyBorder());
        textColorComboBox.setPreferredSize(new Dimension(110, 26));
        textColorComboBox.setForeground(TEXT_COLOR);
        textColorComboBox.setBackground(MAIN_COLOR_2);
        textColorComboBox.setFont(font);
        textColorComboBox.setFocusable(false);

        // Hide the arrow
        textColorComboBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = new JButton();
                arrowButton.setVisible(false);
                return arrowButton;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(MAIN_COLOR_3);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        textColorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                Color color = (Color) value;
                setText(colorName(color)); // Display color name
                setIcon(new ColorIcon(color, 16, 16)); // Display color icon

                // Set the background color for the combo box's displayed item
                setBackground(isSelected ? MAIN_COLOR_3 : MAIN_COLOR_2);

                // Set the foreground color for the combo box's displayed item
                setForeground(isSelected ? TEXT_COLOR : color);

                return this;
            }

            private String colorName(Color color) {
                if (color.equals(TEXT_COLOR_RED)) return "  Red";
                if (color.equals(TEXT_COLOR_GREEN)) return "  Green";
                if (color.equals(TEXT_COLOR_BLUE)) return "  Blue";
                if (color.equals(TEXT_COLOR_PEACH)) return "  Peach";
                if (color.equals(TEXT_COLOR_PINK)) return "  Pink";
                return "  White";
            }
        });
    }

    public static void applyFontSizeComboBoxStyle(JComboBox fontSizeComboBox, Font font) {
        fontSizeComboBox.setPreferredSize(new Dimension(55, 26));
        fontSizeComboBox.setBackground(MAIN_COLOR_3);
        fontSizeComboBox.setForeground(TEXT_COLOR);
        fontSizeComboBox.setMaximumRowCount(200);
        fontSizeComboBox.setFont(font);
        fontSizeComboBox.setBorder(BorderFactory.createEmptyBorder());
        fontSizeComboBox.setFocusable(false);

        // Hide the arrow
        fontSizeComboBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = new JButton();
                arrowButton.setVisible(false);
                return arrowButton;
            }
        });

        fontSizeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel(String.valueOf(value));
                label.setFont(font);
                label.setOpaque(true);

                if (isSelected) {
                    label.setBackground(MAIN_COLOR_3);
                    label.setForeground(TEXT_COLOR);
                } else {
                    label.setBackground(MAIN_COLOR_2);
                    label.setForeground(TEXT_COLOR);
                }

                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return label;
            }
        });

        fontSizeComboBox.setSelectedItem(font.getSize());
    }

    public static void applyTextPaneStyle(JTextPane textPane, Font mainFont) {
        textPane.setBackground(MAIN_COLOR_1);
        textPane.setForeground(TEXT_COLOR);
        textPane.setCaretColor(TEXT_COLOR);
        textPane.setFont(mainFont);
        textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SimpleAttributeSet initialAttributes = new SimpleAttributeSet();
        StyleConstants.setFontFamily(initialAttributes, mainFont.getName());
        StyleConstants.setFontSize(initialAttributes, mainFont.getSize());
        textPane.setCharacterAttributes(initialAttributes, false);
    }

    public static void applyStylePanelStyle(JPanel fontPanel) {
        fontPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        fontPanel.setBackground(MAIN_COLOR_2);
        fontPanel.setForeground(TEXT_COLOR);
    }

    public static void applyFontComboBoxStyle(JComboBox fontComboBox, Font font) {
        // Remove border
        for (Component comp : fontComboBox.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBorder(BorderFactory.createEmptyBorder());
            }
        }

        fontComboBox.setPreferredSize(new Dimension(160, 30));
        fontComboBox.setMaximumRowCount(200);
        fontComboBox.setFont(font);
        fontComboBox.setBorder(BorderFactory.createLineBorder(MAIN_COLOR_2, 2));
        fontComboBox.setBackground(MAIN_COLOR_3);
        fontComboBox.setForeground(TEXT_COLOR);
        fontComboBox.setFocusable(false);

        fontComboBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = new JButton();
                arrowButton.setBorder(BorderFactory.createEmptyBorder());
                arrowButton.setBackground(MAIN_COLOR_3);
                arrowButton.setFocusable(false);

                arrowButton.setFocusPainted(false);
                arrowButton.setContentAreaFilled(false);
                arrowButton.setRolloverEnabled(false);

                arrowButton.setIcon(new javax.swing.Icon() {
                    private final int WIDTH = 12;
                    private final int HEIGHT = 6;

                    @Override
                    public int getIconWidth() {
                        return WIDTH;
                    }

                    @Override
                    public int getIconHeight() {
                        return HEIGHT;
                    }

                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        g.setColor(TEXT_COLOR);
                        int[] xPoints = { x, x + WIDTH / 2, x + WIDTH };
                        int[] yPoints = { y, y + HEIGHT, y };
                        g.fillPolygon(xPoints, yPoints, 3);
                    }
                });

                return arrowButton;
            }
        });

        fontComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel((String) value);
                label.setFont(new Font((String) value, Font.PLAIN, 16));
                label.setOpaque(true);

                if (isSelected) {
                    label.setBackground(MAIN_COLOR_3);
                    label.setForeground(TEXT_COLOR);
                } else {
                    label.setBackground(MAIN_COLOR_2);
                    label.setForeground(TEXT_COLOR);
                }

                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return label;
            }
        });
    }

    /**
     * Applies the custom styling for MessagePopup dialogs.
     *
     * @param popup The MessagePopup to style.
     */
    public static void applyMessagePopupStyle(MessagePopup popup) {
        Container content = popup.getContentPane();
        content.setBackground(MAIN_COLOR_4);

        applyComponentStyle(content);

        if (content instanceof JComponent) {
            ((JComponent) content).setBorder(BorderFactory.createLineBorder(MAIN_COLOR_4, 2));
        }
    }

    /**
     * Recursively applies theme styling to components.
     *
     * @param comp The component to customize.
     */
    private static void applyComponentStyle(Component comp) {
        if (comp instanceof JPanel) {
            if (comp.getBackground() != MAIN_COLOR_4) {
                comp.setBackground(MAIN_COLOR_3);
            }
        }
        if (comp instanceof JLabel) {
            comp.setForeground(TEXT_COLOR);
            comp.setFont(MAIN_FONT);
        }
        if (comp instanceof JButton) {
            comp.setForeground(TEXT_COLOR);

            if (comp.getBackground() != MAIN_COLOR_4) {
                comp.setBackground(MAIN_COLOR_3);
            }

            comp.setFont(MAIN_FONT);
            ((JButton) comp).setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyComponentStyle(child);
            }
        }
    }
}
