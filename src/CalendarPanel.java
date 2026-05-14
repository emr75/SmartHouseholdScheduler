import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

/**
 * Calendar view for the Smart Household Scheduler.
 *
 * This class owns the monthly grid, day cells, task pills, task popups, and
 * drag-and-drop rescheduling behavior.
 */
public class CalendarPanel extends JPanel {

        private static final long serialVersionUID = 1L;

    /** Text flavor from DataFlavor class for drag-and-drop to move task IDs as strings. */
    private static final DataFlavor TASK_FLAVOR = DataFlavor.stringFlavor;

    /** Database used to read tasks and update task dates. */
    private final DatabaseManager databaseManager;

    /** Conflict checker used before allowing a drag-and-drop reschedule. */
    private final ConflictManager conflictManager;

    /** Callback connection back to the main GUI. */
    private final SchedulerCallbacks callbacks;

    /** The month currently being displayed in the calendar grid. */
    private YearMonth calendarMonth;


        private final JLabel monthLabel = SchedulerUI.mkLabel("", new Font("SansSerif", Font.BOLD, 15), SchedulerUI.TEXT_PRIMARY);
        private final JPanel headerRow = new JPanel(new GridLayout(1, 7, 2, 0));
        private final JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));

    /**
     * Creates a calendar panel connected to the database and main GUI callbacks.
     *
     * @param databaseManager - database object used to load and update tasks
     * @param conflictManager - object used to check overlapping task times
     * @param callbacks - methods supplied by the main GUI for updates
     */
    public CalendarPanel(DatabaseManager databaseManager, ConflictManager conflictManager, SchedulerCallbacks callbacks) {
        this.databaseManager = databaseManager;
        this.conflictManager = conflictManager;
        this.callbacks = callbacks;
        this.calendarMonth = YearMonth.now();
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);

            add(buildNav(), BorderLayout.NORTH);
            add(headerRow, BorderLayout.CENTER);

            JPanel south = new JPanel(new BorderLayout());
            south.setOpaque(false);
            south.add(grid, BorderLayout.NORTH);
            add(south, BorderLayout.SOUTH);

            buildDayHeaders();
            updateMonthLabel();
            buildGrid();
        }

        /**
         * Creates previous/next month buttons and the current month label.
         */
        private JPanel buildNav() {
            JPanel nav = new JPanel(new BorderLayout());
            nav.setOpaque(false);
            nav.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

            JButton prev = navArrow("‹");
            JButton next = navArrow("›");
            monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

            prev.addActionListener(e -> {
                calendarMonth = calendarMonth.minusMonths(1);
                updateMonthLabel();
                buildGrid();
            });

            next.addActionListener(e -> {
                calendarMonth = calendarMonth.plusMonths(1);
                updateMonthLabel();
                buildGrid();
            });

            JLabel hint = SchedulerUI.mkLabel(
                    "drag tasks between days to reschedule",
                    new Font("SansSerif", Font.PLAIN, 10),
                    new Color(0x7A, 0x82, 0xA0, 160));
            hint.setHorizontalAlignment(SwingConstants.RIGHT);

            nav.add(prev, BorderLayout.WEST);
            nav.add(monthLabel, BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(nav, BorderLayout.CENTER);
            wrapper.add(hint, BorderLayout.SOUTH);

            return wrapper;
        }

        /**
         * Creates a month navigation arrow button.
         */
        private JButton navArrow(String symbol) {
            JButton b = new JButton(symbol);
            b.setFont(new Font("SansSerif", Font.BOLD, 20));
            b.setForeground(SchedulerUI.TEXT_MUTED);
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    b.setForeground(SchedulerUI.TEXT_PRIMARY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    b.setForeground(SchedulerUI.TEXT_MUTED);
                }
            });

            return b;
        }

        /**
         * Updates the text label at the top of the calendar.
         */
        private void updateMonthLabel() {
            monthLabel.setText(calendarMonth.format(DateTimeFormatter.ofPattern("MMMM  yyyy")).toUpperCase());
        }

        /**
         * Adds Sunday through Saturaday labels above the calendar grid.
         */
        private void buildDayHeaders() {
            headerRow.setOpaque(false);
            headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

            String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
            for (String d : days) {
                JLabel l = SchedulerUI.mkLabel(d, new Font("SansSerif", Font.BOLD, 10), SchedulerUI.TEXT_MUTED);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                headerRow.add(l);
            }
        }

        /**
         * Rebuilds all calendar day cells for the currently selected month.
         */
        private void buildGrid() {
            grid.removeAll();
            grid.setOpaque(false);

            List<Task> allTasks = databaseManager.getAllTasks();
            LocalDate first = calendarMonth.atDay(1);

            // Java's DayOfWeek uses Monday=1 through Sunday=7.
            // This converts it to Sunday=0 through Saturday=6 for the grid.
            int startDow = first.getDayOfWeek().getValue() % 7;
            int daysInMonth = calendarMonth.lengthOfMonth();
            LocalDate today = LocalDate.now();

            // Empty cells before the first day of the month.
            for (int i = 0; i < startDow; i++) {
                grid.add(blankCell());
            }

            // Real day cells.
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = calendarMonth.atDay(day);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

                List<Task> dayTasks = new ArrayList<>();
                for (Task t : allTasks) {
                    if (t.getDate().equals(dateStr)) {
                        dayTasks.add(t);
                    }
                }

                grid.add(dayCell(day, date.equals(today), dayTasks, dateStr));
            }

            // Empty cells after the last day so the grid stays rectangular.
            int trailing = (7 - (startDow + daysInMonth) % 7) % 7;
            for (int i = 0; i < trailing; i++) {
                grid.add(blankCell());
            }

            grid.revalidate();
            grid.repaint();
        }

        /**
         * Creates an empty spacer cell for days outside the selected month.
         */
        private JPanel blankCell() {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(0, 86));
            return p;
        }

        /**
         * Creates one calendar day cell. The cell displays the day number,
         * task pills, and acts as a drop target for dragged tasks.
         */
        private JPanel dayCell(int dayNum, boolean isToday, List<Task> tasks, String dateStr) {

            // Small mutable arrays are used because anonymous inner classes need
            // accessed local variables to be effectively final.
            boolean[] dropActive = {false};
            boolean[] dropReady = {false};

            JPanel cell = new JPanel() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Fill color changes depending on the cell state.
                    if (dropActive[0]) {
                        g2.setColor(dropReady[0] ? SchedulerUI.DROP_OK_BG : SchedulerUI.DROP_HOVER_BG);
                    } else if (isToday) {
                        g2.setColor(SchedulerUI.CAL_TODAY_BG);
                    } else {
                        g2.setColor(new Color(0x12, 0x15, 0x1F, 180));
                    }
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                    // Border color also changes while dragging over a day.
                    if (dropActive[0]) {
                        g2.setColor(dropReady[0] ? SchedulerUI.DROP_OK_BD : SchedulerUI.DROP_HOVER_BD);
                        g2.setStroke(new BasicStroke(1.8f));
                    } else if (isToday) {
                        g2.setColor(SchedulerUI.CAL_TODAY_BD);
                    } else {
                        g2.setColor(SchedulerUI.BORDER_SUBTLE);
                    }
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));

                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            cell.setLayout(new BorderLayout(0, 2));
            cell.setOpaque(false);
            cell.setPreferredSize(new Dimension(0, 86));
            cell.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 6));

            if (!tasks.isEmpty()) {
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            JLabel numLbl = new JLabel(String.valueOf(dayNum));
            numLbl.setFont(new Font("SansSerif", isToday ? Font.BOLD : Font.PLAIN, 12));
            numLbl.setForeground(isToday ? SchedulerUI.ACCENT_BLUE : SchedulerUI.TEXT_MUTED);
            cell.add(numLbl, BorderLayout.NORTH);

            JPanel pills = new JPanel();
            pills.setLayout(new BoxLayout(pills, BoxLayout.Y_AXIS));
            pills.setOpaque(false);

            // Only show the first few tasks so the calendar cell does not get overcrowded.
            if (!tasks.isEmpty()) {
                int shown = Math.min(tasks.size(), 3);
                for (int i = 0; i < shown; i++) {
                    pills.add(draggablePill(tasks.get(i)));
                    if (i < shown - 1) {
                        pills.add(Box.createVerticalStrut(2));
                    }
                }

                if (tasks.size() > 3) {
                    JLabel more = SchedulerUI.mkLabel(
                            "+" + (tasks.size() - 3) + " more",
                            new Font("SansSerif", Font.PLAIN, 9),
                            SchedulerUI.TEXT_MUTED);
                    pills.add(Box.createVerticalStrut(2));
                    pills.add(more);
                }
            }
            cell.add(pills, BorderLayout.CENTER);

            // Clicking a day with tasks opens a small popup summary.
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!tasks.isEmpty()) {
                        showDayPopup(cell, dateStr, tasks);
                    }
                }
            });

            // Drop target: accepts a dragged task ID and attempts to move it to this date.
            new DropTarget(cell, DnDConstants.ACTION_MOVE, new DropTargetAdapter() {

                @Override
                public void dragEnter(DropTargetDragEvent e) {
                    if (e.isDataFlavorSupported(TASK_FLAVOR)) {
                        e.acceptDrag(DnDConstants.ACTION_MOVE);
                        dropActive[0] = true;
                        dropReady[0] = true;
                        cell.repaint();
                    } else {
                        e.rejectDrag();
                    }
                }

                @Override
                public void dragOver(DropTargetDragEvent e) {
                    if (e.isDataFlavorSupported(TASK_FLAVOR)) {
                        e.acceptDrag(DnDConstants.ACTION_MOVE);
                    }
                }

                @Override
                public void dragExit(DropTargetEvent e) {
                    dropActive[0] = false;
                    dropReady[0] = false;
                    cell.repaint();
                }

                @Override
                public void drop(DropTargetDropEvent e) {
                    dropActive[0] = false;
                    dropReady[0] = false;
                    cell.repaint();

                    try {
                        e.acceptDrop(DnDConstants.ACTION_MOVE);

                        String payload = (String) e.getTransferable().getTransferData(TASK_FLAVOR);
                        if (!payload.startsWith("taskId:")) {
                            e.dropComplete(false);
                            return;
                        }

                        int taskId = Integer.parseInt(payload.substring(7));
                        Task moving = findTaskById(taskId);

                        if (moving == null) {
                            e.dropComplete(false);
                            return;
                        }

                        // Do not update the database if the task was dropped on its current date.
                        if (moving.getDate().equals(dateStr)) {
                            e.dropComplete(false);
                            return;
                        }

                        if (wouldConflictAfterMove(moving, dateStr)) {
                            callbacks.showStatus("✖  Conflict on " + dateStr + " — task not moved.", SchedulerUI.DANGER);
                            SwingUtilities.invokeLater(() -> callbacks.showDialog(
                                    moving.getTaskName() + " conflicts with an existing task on " + dateStr
                                            + ".\nThe task was not moved.",
                                    "Scheduling Conflict",
                                    JOptionPane.ERROR_MESSAGE));
                            e.dropComplete(false);
                            return;
                        }

                        callbacks.rescheduleTask(taskId, dateStr, moving.getTaskName());
                        e.dropComplete(true);
                    } catch (Exception ex) {
                        e.dropComplete(false);
                        callbacks.showStatus("✖  Drop failed: " + ex.getMessage(), SchedulerUI.DANGER);
                    }
                }
            }, true);

            return cell;
        }

        /**
         * Builds a small colored task label that can be dragged to another day.
         */
        private JLabel draggablePill(Task task) {
            String name = task.getTaskName();
            if (name.length() > 14) {
                name = name.substring(0, 13) + "…";
            }

            Color priorityColor = SchedulerUI.priorityColor(task.getPriority());

            JLabel label = new JLabel("⠿ " + name) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color base = getForeground();
                    g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 30));
                    g2.fill(new RoundRectangle2D.Float(0, 1, getWidth(), getHeight() - 2, 4, 4));
                    g2.dispose();

                    super.paintComponent(g);
                }
            };

            label.setFont(new Font("SansSerif", Font.PLAIN, 9));
            label.setForeground(priorityColor);
            label.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
            label.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            label.setToolTipText(
                    "Drag to reschedule · "
                            + task.getTaskName()
                            + "  "
                            + task.getStartTime()
                            + "–"
                            + task.getEndTime());

            DragSource ds = DragSource.getDefaultDragSource();
            ds.createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_MOVE, new DragGestureListener() {
                @Override
                public void dragGestureRecognized(DragGestureEvent dge) {
                    Transferable transferable = new StringSelection("taskId:" + task.getTaskId());
                    callbacks.showStatus(
                            "↕  Dragging " + task.getTaskName() + " — drop on a day to reschedule.",
                            SchedulerUI.TEXT_MUTED);

                    dge.startDrag(DragSource.DefaultMoveDrop, transferable, new DragSourceAdapter() {
                        @Override
                        public void dragDropEnd(DragSourceDropEvent dsde) {
                            if (!dsde.getDropSuccess()) {
                                callbacks.showStatus("Drag cancelled.", SchedulerUI.TEXT_MUTED);
                            }
                        }
                    });
                }
            });

            return label;
        }

        /**
         * Creates a popup with all tasks scheduled for the clicked day.
         */
        private void showDayPopup(Component anchor, String dateStr, List<Task> tasks) {
            JPopupMenu popup = new JPopupMenu();
            popup.setBackground(SchedulerUI.BG_CARD);
            popup.setBorder(new CompoundBorder(
                    new LineBorder(SchedulerUI.BORDER_SUBTLE, 1, true),
                    BorderFactory.createEmptyBorder(6, 0, 6, 0)));

            JLabel heading = SchedulerUI.mkLabel("  " + dateStr, new Font("SansSerif", Font.BOLD, 11), SchedulerUI.ACCENT_BLUE);
            heading.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));
            popup.add(heading);

            JSeparator sep = new JSeparator();
            sep.setForeground(SchedulerUI.BORDER_SUBTLE);
            popup.add(sep);

            for (Task t : tasks) {
                String label = "<html><b style='color:#F0F2FF'>"
                        + t.getTaskName()
                        + "</b>&nbsp;&nbsp;<span style='color:#7A82A0'>"
                        + t.getFamilyMember()
                        + " · "
                        + t.getStartTime()
                        + "–"
                        + t.getEndTime()
                        + " ("
                        + t.getDuration()
                        + " min)</span></html>";

                JMenuItem item = new JMenuItem(label);
                item.setBackground(SchedulerUI.BG_CARD);
                item.setForeground(SchedulerUI.TEXT_PRIMARY);
                item.setFont(SchedulerUI.FONT_TABLE);
                item.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
                item.setOpaque(true);

                Color dot = SchedulerUI.priorityColor(t.getPriority());
                item.setIcon(new Icon() {
                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(dot);
                        g2.fillRoundRect(x, y + 2, 3, getIconHeight() - 4, 3, 3);
                        g2.dispose();
                    }

                    @Override
                    public int getIconWidth() {
                        return 7;
                    }

                    @Override
                    public int getIconHeight() {
                        return 18;
                    }
                });

                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        item.setBackground(SchedulerUI.ROW_HOVER);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        item.setBackground(SchedulerUI.BG_CARD);
                    }
                });

                popup.add(item);
            }

            popup.show(anchor, anchor.getWidth() / 2, anchor.getHeight() / 2);
        }

        /**
         * Refresh method used by outer GUI after data changes.
         */
    public void refresh() {
            buildGrid();
        }
    

    /**
     * Finds a task by its database ID.
     *
     * @param taskId - ID of the task being dragged
     * @return the task, or null if doesn't exist
     */
    private Task findTaskById(int taskId) {
        for (Task task : databaseManager.getAllTasks()) {
            if (task.getTaskId() == taskId) {
                return task;
            }
        }
        return null;
    }

    /**
     * Checks whether moving a task to a new date would create a conflict.
     *
     * The task keeps its same start time and duration. Only the date changes.
     * The moving task is removed from the comparison list so it does not conflict
     * with itself.
     *
     * @param moving - task being moved
     * @param newDate - target date in YYYY-MM-DD format
     * @return true if another task overlaps with the moved task
     */
    private boolean wouldConflictAfterMove(Task moving, String newDate) {
        Task testTask = new Task(
                moving.getTaskName(),
                moving.getFamilyMember(),
                newDate,
                moving.getStartTime(),
                moving.getDuration(),
                moving.getPriority());

        List<Task> otherTasks = databaseManager.getAllTasks();
        otherTasks.removeIf(task -> task.getTaskId() == moving.getTaskId());

        return conflictManager.hasConflict(testTask, otherTasks);
    }

    /**
     * Methods the calendar needs from the main GUI without depending on the
     * entire SmartHouseholdGUI class.
     */
    public interface SchedulerCallbacks {

        /** Called when the calendar successfully drops a task onto a new date. */
        void rescheduleTask(int taskId, String newDate, String taskName);

        /** Called when the calendar needs to update the status bar. */
        void showStatus(String message, Color color);

        /** Called when the calendar needs to show a message dialog. */
        void showDialog(String message, String title, int messageType);
    }
}
