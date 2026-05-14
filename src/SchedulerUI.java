import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Shared visual styling and reusable Swing components for the scheduler.
 *
 * This class keeps colors, fonts, borders, table renderings, and widget
 * factory methods outside of the main GUI class so that
 * SmartHouseholdGUI is easier to read because it can focus on application
 * behavior instead of repeated styling code.
 */
public final class SchedulerUI {

    /** Prevents recreating SchedulerUI objects through this is a helper class. */
    private SchedulerUI() {
    }

    //  Color palette

    public static final Color BG_DEEP = new Color(0x0F, 0x11, 0x17);
    public static final Color BG_PANEL = new Color(0x16, 0x19, 0x22);
    public static final Color BG_CARD = new Color(0x1E, 0x22, 0x2E);
    public static final Color BG_INPUT = new Color(0x12, 0x15, 0x1F);

    public static final Color ACCENT_BLUE = new Color(0x4A, 0x9E, 0xFF);
    public static final Color ACCENT_PURPLE = new Color(0x9B, 0x72, 0xFF);

    public static final Color TEXT_PRIMARY = new Color(0xF0, 0xF2, 0xFF);
    public static final Color TEXT_MUTED = new Color(0x7A, 0x82, 0xA0);

    public static final Color BORDER_SUBTLE = new Color(0x2A, 0x2F, 0x42);
    public static final Color ROW_ALT = new Color(0x1A, 0x1E, 0x2A);
    public static final Color ROW_HOVER = new Color(0x24, 0x2A, 0x3E);

    public static final Color SUCCESS = new Color(0x3D, 0xD6, 0x8C);
    public static final Color DANGER = new Color(0xFF, 0x5C, 0x5C);

    public static final Color CAL_TODAY_BG = new Color(0x4A, 0x9E, 0xFF, 30);
    public static final Color CAL_TODAY_BD = new Color(0x4A, 0x9E, 0xFF, 120);

    public static final Color DROP_HOVER_BG = new Color(0x9B, 0x72, 0xFF, 35);
    public static final Color DROP_HOVER_BD = new Color(0x9B, 0x72, 0xFF, 180);
    public static final Color DROP_OK_BG = new Color(0x3D, 0xD6, 0x8C, 35);
    public static final Color DROP_OK_BD = new Color(0x3D, 0xD6, 0x8C, 180);

    public static final Color PRI_HIGH = new Color(0xFF, 0x7B, 0x7B);
    public static final Color PRI_MEDIUM = new Color(0xFF, 0xC1, 0x5E);
    public static final Color PRI_LOW = new Color(0x4D, 0xD0, 0x8D);

    //  Fonts

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BTN = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_TABLE_H = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_TABLE = new Font("SansSerif", Font.PLAIN, 12);

    /**
     * Returns the display color for a priority value.
     *
     * @param priority - task priority text, usually High, Medium, or Low
     * @return the color used to display that priority
     */
    public static Color priorityColor(String priority) {
    	// If the priority is missing, use the safest/default visual option.
        if (priority == null) {
            return PRI_LOW;
        }
        // Convert the priority text to lowercase so comparisons work even if capitalization differs.
        return switch (priority.toLowerCase()) {
            case "high" -> PRI_HIGH;
            case "medium" -> PRI_MEDIUM;
         // Any unknown priority defaults to the low-priority color.
            default -> PRI_LOW;
        };
    }

    //  Widget factory methods:
    // These methods reduce repeated Swing styling code.

    /**
     * Creates a JLabel with a specific font and color.
     */
    public static JLabel mkLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates one label + input field pair for the sidebar form.
     */
    public static JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = mkLabel(labelText, new Font("SansSerif", Font.BOLD, 10), TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        row.add(label);
        row.add(field);

        return row;
    }

    /**
     * Creates a dark rounded text field.
     */
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        f.setFont(FONT_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT);
        f.setCaretColor(ACCENT_BLUE);
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER_SUBTLE, 8),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        return f;
    }

    /**
     * Creates a dark themed combo box.
     */
    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_INPUT);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);

        cb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
                label.setBackground(selected ? ROW_HOVER : BG_CARD);
                label.setForeground(TEXT_PRIMARY);
                label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return label;
            }
        });

        cb.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER_SUBTLE, 8),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));

        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("▾");
                b.setContentAreaFilled(false);
                b.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                b.setForeground(TEXT_MUTED);
                b.setFont(new Font("SansSerif", Font.PLAIN, 11));
                return b;
            }
        });

        return cb;
    }

    /**
     * Creates a rounded button with hover and pressed styling.
     */
    public static JButton pillButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentColor;
                if (getModel().isPressed()) {
                    currentColor = bg.darker();
                } else if (getModel().isRollover()) {
                    currentColor = bg.brighter();
                } else {
                    currentColor = bg;
                }

                g2.setColor(currentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.setFont(FONT_BTN);
        b.setForeground(fg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return b;
    }

    /**
     * Creates a small text-only button, used for Refresh.
     */
    public static JButton textButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(ACCENT_BLUE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * Applies table colors, fonts, row height, and column widths.
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_TABLE);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(ROW_HOVER);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_PANEL);
        header.setForeground(TEXT_MUTED);
        header.setFont(FONT_TABLE_H);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_SUBTLE));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        int[] widths = {40, 180, 100, 100, 75, 75, 55, 90};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    //  Custom renderings and borders

    /**
     * Custom renderer that displays task priority as a colored badge.
     */
    public static class PriorityBadgeRenderer extends JLabel implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        PriorityBadgeRenderer() {
            setOpaque(false);
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int col) {
            setText(value == null ? "" : value.toString());
            setFont(new Font("SansSerif", Font.BOLD, 10));

            String val = value == null ? "" : value.toString().toLowerCase();
            if (val.equals("high")) {
                setForeground(PRI_HIGH);
            } else if (val.equals("medium")) {
                setForeground(PRI_MEDIUM);
            } else {
                setForeground(PRI_LOW);
            }

            setBackground(selected ? ROW_HOVER : (row % 2 == 0 ? BG_CARD : ROW_ALT));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color badgeColor = getForeground();
            g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 25));
            g2.fill(new RoundRectangle2D.Float(6, 6, getWidth() - 12, getHeight() - 12, 8, 8));
            g2.dispose();

            super.paintComponent(g);
        }
    }

    /**
     * Rounded border used by text fields and combo boxes.
     */
    public static class RoundedBorder extends AbstractBorder {

        private static final long serialVersionUID = 1L;

        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }

    /**
     * Scrollbar used by the task table scroll pane.
     */
    public static class SlimScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0x3A, 0x40, 0x58);
            trackColor = BG_CARD;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return ghostButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return ghostButton();
        }

        private JButton ghostButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fill(new RoundRectangle2D.Float(
                    thumbBounds.x + 2,
                    thumbBounds.y + 2,
                    thumbBounds.width - 4,
                    thumbBounds.height - 4,
                    6,
                    6));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }


}
