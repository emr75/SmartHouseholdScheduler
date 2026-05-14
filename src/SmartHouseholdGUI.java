import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Main Swing window for the Smart Household Scheduler application.
 *
 * This class controls the full user interface. It lets the user add tasks,
 * delete tasks, auto-schedule tasks, view tasks in a table, and view/reschedule
 * tasks in a calendar layout.
 *
 * The class works with three outside classes:
 * 
 *     DatabaseManager - saves, loads, deletes, and updates tasks.
 *     ConflictManager - checks whether two tasks overlap.
 *     Task - stores one scheduled household task.
 * 
 * For Drage_and_Drop:
 * 
 *     Each task pill stores its task ID as a text payload e.g. taskId:42.
 *     Each calendar day cell accepts dropped task IDs.
 *     When a task is dropped, the program checks for time conflicts.
 *     If no conflict exists, the task date is updated in the database.
 *
 */
public class SmartHouseholdGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    //  Needed fields for application state

    private final DatabaseManager databaseManager;
    private final ConflictManager conflictManager;

    private JTextField taskNameField;
    private JComboBox<String> familyMemberBox;
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField durationField;
    private JComboBox<String> priorityBox;

    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private CalendarPanel calendarPanel;

    //  Constructor and top-level setup

    /**
     * Creates the application window, initializes database access, and loads
     * the first set of tasks from the database.
     */
    public SmartHouseholdGUI() {
        databaseManager = new DatabaseManager();
        conflictManager = new ConflictManager();

        // Make sure the tasks table exists before the UI tries to read from it.
        databaseManager.createTasksTable();
        
        setTitle("Household Scheduler");
        setSize(1160, 700);

        // Prevent the window from becoming too small to use comfortably.
        setMinimumSize(new Dimension(960, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the window on the user's screen.
        setLocationRelativeTo(null);
        
        setBackground(SchedulerUI.BG_DEEP);
        buildGUI();

        loadTasksIntoTable();
    }

    /**
     * Builds the main window layout: header, sidebar, content area, and status bar.
     */
    private void buildGUI() {
        // BorderLayout to place sections in the top, left, center, and bottom.
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SchedulerUI.BG_DEEP);

        // Add each major screen section to the root layout.
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        // Set the completed root panel as the content area for the JFrame.
        setContentPane(root);
    }

    //  Header

    /**
     * Creates the top title bar with a gradient background.
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                // Use Graphics2D for smoother drawing.
                Graphics2D g2 = (Graphics2D) g.create();

                // Enable smoother lines and gradient rendering.
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Paint a horizontal dark gradient across the header.
                g2.setPaint(new GradientPaint(
                        0,
                        0,
                        new Color(0x1A, 0x1F, 0x35),
                        getWidth(),
                        0,
                        new Color(0x12, 0x15, 0x28)));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw a thin separator line under the header.
                g2.setColor(SchedulerUI.BORDER_SUBTLE);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

                // Dispose of the temporary graphics object.
                g2.dispose();
            }
        };

        // Aesthetics for header
        header.setPreferredSize(new Dimension(0, 64));
        header.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logo.setOpaque(false);
        JLabel title = new JLabel("  Household Scheduler");
        title.setFont(SchedulerUI.FONT_TITLE);
        title.setForeground(SchedulerUI.TEXT_PRIMARY);
        JLabel sub = new JLabel("  ·  Smart Family Task Manager");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(SchedulerUI.TEXT_MUTED);

        logo.add(title);
        logo.add(sub);
        header.add(logo, BorderLayout.WEST);

        return header;
    }

    //  Sidebar form

    /**
     * Creates the left sidebar where users enter task information.
     */
    private JPanel buildSidebar() {
        // Main sidebar container.
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SchedulerUI.BG_PANEL);
        sidebar.setPreferredSize(new Dimension(300, 0));

        // Add a right border and internal padding.
        sidebar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, SchedulerUI.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(24, 20, 24, 20)));

        // Stack form components vertically.
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Section heading for the task form.
        JLabel heading = SchedulerUI.mkLabel("NEW TASK", SchedulerUI.FONT_LABEL, SchedulerUI.TEXT_MUTED);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(heading);
        sidebar.add(Box.createVerticalStrut(16));

        // Input fields used by Add Task and Auto Schedule.
        taskNameField = SchedulerUI.styledField("e.g. Grocery Shopping");
        familyMemberBox = SchedulerUI.styledCombo(new String[] {"Mom", "Dad", "Lisa", "Carl", "Grandparent", "Other"});
        dateField = SchedulerUI.styledField("2026-05-10");
        startTimeField = SchedulerUI.styledField("14:00");
        durationField = SchedulerUI.styledField("60");
        priorityBox = SchedulerUI.styledCombo(new String[] {"High", "Medium", "Low"});

        // Add each form field as a labeled row.
        sidebar.add(SchedulerUI.formRow("TASK NAME", taskNameField));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(SchedulerUI.formRow("FAMILY MEMBER", familyMemberBox));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(SchedulerUI.formRow("DATE  (YYYY-MM-DD)", dateField));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(SchedulerUI.formRow("START TIME  (HH:MM)", startTimeField));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(SchedulerUI.formRow("DURATION  (minutes)", durationField));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(SchedulerUI.formRow("PRIORITY", priorityBox));
        sidebar.add(Box.createVerticalStrut(24));

        // Add Task button reads the form and attempts to save a task.
        JButton addBtn = SchedulerUI.pillButton("Add Task", SchedulerUI.ACCENT_BLUE, Color.WHITE);
        addBtn.addActionListener(e -> addTask());
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        sidebar.add(addBtn);
        sidebar.add(Box.createVerticalStrut(8));

        // Delete Selected button deletes the currently selected row in the table.
        JButton delBtn = SchedulerUI.pillButton("Delete Selected", SchedulerUI.BG_CARD, SchedulerUI.DANGER);
        delBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(SchedulerUI.DANGER.getRed(), SchedulerUI.DANGER.getGreen(), SchedulerUI.DANGER.getBlue(), 80), 1, true),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        delBtn.addActionListener(e -> deleteSelectedTask());
        delBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        delBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        sidebar.add(delBtn);
        sidebar.add(Box.createVerticalStrut(8));

        // Auto Schedule button finds an available time slot automatically.
        JButton autoBtn = SchedulerUI.pillButton("Auto Schedule", new Color(0x28, 0x2D, 0x42), SchedulerUI.ACCENT_PURPLE);
        autoBtn.setBorder(new CompoundBorder(
                new LineBorder(
                        new Color(SchedulerUI.ACCENT_PURPLE.getRed(), SchedulerUI.ACCENT_PURPLE.getGreen(), SchedulerUI.ACCENT_PURPLE.getBlue(), 80),
                        1,
                        true),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        autoBtn.addActionListener(e -> autoScheduleTask());
        autoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        sidebar.add(autoBtn);

        return sidebar;
    }

    //  Content area with List and Calendar views

    /**
     * Creates the main center area. A CardLayout is used so only one view is
     * visible at a time: the task table or the calendar.
     */
    private JPanel buildContent() {
        // Main content panel that contains the tabs and current view.
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SchedulerUI.BG_DEEP);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Tab bar contains List and Calendar buttons.
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setOpaque(false);
        tabBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // CardLayout lets the app switch views without creating new windows.
        JPanel cardStack = new JPanel(new CardLayout());
        cardStack.setOpaque(false);

        // Build both views before adding them to the card stack.
        JPanel listView = buildListView();

        // CalendarPanel handles the monthly calendar and drag/drop behavior.
        calendarPanel = new CalendarPanel(
                databaseManager,
                conflictManager,
                new CalendarPanel.SchedulerCallbacks() {
                    @Override
                    public void rescheduleTask(int taskId, String newDate, String taskName) {
                        // CalendarPanel asks the main GUI to update the database.
                        rescheduleTaskInBackground(taskId, newDate, taskName);
                    }

                    @Override
                    public void showStatus(String message, Color color) {
                        // CalendarPanel asks the main GUI to update the status bar.
                        SmartHouseholdGUI.this.showStatus(message, color);
                    }

                    @Override
                    public void showDialog(String message, String title, int messageType) {
                        // CalendarPanel asks the main GUI to show a message dialog.
                        dialog(message, title, messageType);
                    }
                });

        // Add both screens to the CardLayout.
        cardStack.add(listView, "list");
        cardStack.add(calendarPanel, "calendar");

        // Create tab buttons for switching views.
        JToggleButton tabList = tabToggle("☰  List");
        JToggleButton tabCal = tabToggle("▦  Calendar");

        // ButtonGroup ensures only one tab is selected at a time.
        ButtonGroup bg = new ButtonGroup();
        bg.add(tabList);
        bg.add(tabCal);

        // List view is the default selected view.
        tabList.setSelected(true);
        styleTabSelected(tabList);
        styleTabNormal(tabCal);

        // Show the task table when List is clicked.
        tabList.addActionListener(e -> {
            ((CardLayout) cardStack.getLayout()).show(cardStack, "list");
            styleTabSelected(tabList);
            styleTabNormal(tabCal);
        });

        // Show the monthly calendar when Calendar is clicked.
        tabCal.addActionListener(e -> {
            ((CardLayout) cardStack.getLayout()).show(cardStack, "calendar");
            styleTabSelected(tabCal);
            styleTabNormal(tabList);

            // Refresh ensures the calendar displays the newest database data.
            calendarPanel.refresh();
        });

        tabBar.add(tabList);
        tabBar.add(Box.createHorizontalStrut(4));
        tabBar.add(tabCal);

        content.add(tabBar, BorderLayout.NORTH);
        content.add(cardStack, BorderLayout.CENTER);

        return content;
    }

    /**
     * Creates a custom tab button for switching views.
     */
    private JToggleButton tabToggle(String text) {
        JToggleButton b = new JToggleButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                // Custom paint gives the tab a rounded background.
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // The background color is controlled by styleTabSelected/styleTabNormal.
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                g2.dispose();

                // Let the toggle button draw its text.
                super.paintComponent(g);
            }
        };

        // Apply shared button font and remove default Swing button styling.
        b.setFont(SchedulerUI.FONT_BTN);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);

        return b;
    }

    /**
     * Styles the currently selected tab.
     */
    private void styleTabSelected(AbstractButton b) {
        // Selected tab uses a filled card-style background.
        b.setBackground(SchedulerUI.BG_CARD);
        b.setForeground(SchedulerUI.TEXT_PRIMARY);
    }

    /**
     * Styles a tab that is not currently selected.
     */
    private void styleTabNormal(AbstractButton b) {
        // Unselected tab stays transparent and muted.
        b.setBackground(new Color(0, 0, 0, 0));
        b.setForeground(SchedulerUI.TEXT_MUTED);
    }

    //  List view

    /**
     * Builds the task table view.
     */
    private JPanel buildListView() {
        // Main table panel.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Top bar above the table.
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        bar.add(SchedulerUI.mkLabel("SCHEDULED TASKS", SchedulerUI.FONT_LABEL, SchedulerUI.TEXT_MUTED), BorderLayout.WEST);

        // Refresh button reloads table data from the database.
        JButton refresh = SchedulerUI.textButton("↻  Refresh");
        refresh.addActionListener(e -> loadTasksIntoTable());
        bar.add(refresh, BorderLayout.EAST);

        // Table column names.
        String[] cols = {"ID", "TASK", "MEMBER", "DATE", "START", "END", "MIN", "PRIORITY"};

        // DefaultTableModel stores the rows shown by JTable.
        tableModel = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                // Users should edit tasks through buttons/forms, not directly in the table.
                return false;
            }
        };

        taskTable = new JTable(tableModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);

                // Use alternate row colors, and a stronger background for selected rows.
                if (isRowSelected(row)) {
                    c.setBackground(SchedulerUI.ROW_HOVER);
                    c.setForeground(SchedulerUI.TEXT_PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? SchedulerUI.BG_CARD : SchedulerUI.ROW_ALT);
                    c.setForeground(col == 0 ? SchedulerUI.TEXT_MUTED : SchedulerUI.TEXT_PRIMARY);
                }

                // Add padding inside table cells so the text is easier to read.
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }

                return c;
            }
        };

        // The priority column gets a custom colored badge renderer.
        taskTable.getColumnModel().getColumn(7).setCellRenderer(new SchedulerUI.PriorityBadgeRenderer());

        // Apply shared dark table styling.
        SchedulerUI.styleTable(taskTable);

        // Put the table inside a scroll pane so large schedules remain usable.
        JScrollPane scroll = new JScrollPane(taskTable);
        scroll.setBackground(SchedulerUI.BG_CARD);
        scroll.getViewport().setBackground(SchedulerUI.BG_CARD);
        scroll.setBorder(new LineBorder(SchedulerUI.BORDER_SUBTLE, 1, true));
        scroll.getVerticalScrollBar().setUI(new SchedulerUI.SlimScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new SchedulerUI.SlimScrollBarUI());

        panel.add(bar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    //  Drag-and-drop helper methods

    /**
     * Updates a task date after a successful calendar drop.
     *
     * The update runs inside a SwingWorker so the interface does not freeze while the database operation is happening.
     */
    private void rescheduleTaskInBackground(int taskId, String newDate, String taskName) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // Run the database update on a background thread.
                databaseManager.updateTaskDate(taskId, newDate);
                return null;
            }

            @Override
            protected void done() {
                try {
                    // get() rethrows any exception that happened in doInBackground().
                    get();

                    // Reload table and calendar data after the move.
                    loadTasksIntoTable();

                    // Show feedback in the status bar.
                    showStatus("✔  " + taskName + " moved to " + newDate + ".", SchedulerUI.SUCCESS);
                } catch (Exception ex) {
                    dialog("Error rescheduling task: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    //  Task actions

    /**
     * Reads the sidebar form, validates the input, checks conflicts, and saves
     * a new task if everything is valid.
     */
    private void addTask() {
        try {
            // Read and clean user input from the sidebar form.
            String taskName = taskNameField.getText().trim();
            String member = (String) familyMemberBox.getSelectedItem();
            String date = dateField.getText().trim();
            String startTime = startTimeField.getText().trim();
            int duration = Integer.parseInt(durationField.getText().trim());
            String priority = (String) priorityBox.getSelectedItem();

            // Required fields cannot be blank.
            if (taskName.isEmpty() || date.isEmpty() || startTime.isEmpty()) {
                showStatus("⚠  Please fill in all required fields.", SchedulerUI.DANGER);
                dialog("Please fill in all required fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate date format before creating the task.
            if (!isValidDate(date)) {
                dialog("Date must be in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate time format before conflict checking.
            if (!isValidTime(startTime)) {
                dialog("Start time must be in HH:MM (24-hour) format.", "Invalid Time", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Duration must be a positive number of minutes.
            if (duration <= 0) {
                showStatus("⚠  Duration must be > 0.", SchedulerUI.DANGER);
                dialog("Duration must be greater than 0.", "Invalid Duration", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Convert the validated form data into a Task object.
            Task newTask = new Task(taskName, member, date, startTime, duration, priority);

            // Check the new task against all existing database tasks.
            if (conflictManager.hasConflict(newTask, databaseManager.getAllTasks())) {
                showStatus("✖  Conflict — overlapping task exists.", SchedulerUI.DANGER);
                dialog(
                        "Conflict detected! This family member already has a task during that time.",
                        "Scheduling Conflict",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save the task using a background thread.
            saveTaskInBackground(newTask);
            showStatus("✔  Task " + taskName + " added.", SchedulerUI.SUCCESS);
        } catch (NumberFormatException e) {
            // This catches invalid duration input, such as "sixty".
            showStatus("⚠  Duration must be a valid number.", SchedulerUI.DANGER);
            dialog("Duration must be a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // General safety catch so unexpected errors do not crash the app.
            showStatus("✖  " + e.getMessage(), SchedulerUI.DANGER);
            dialog("Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the currently selected row from the task table.
     */
    private void deleteSelectedTask() {
        // Get the row currently selected in the JTable.
        int row = taskTable.getSelectedRow();

        // If no row is selected, there is nothing to delete.
        if (row == -1) {
            showStatus("⚠  Select a task to delete.", SchedulerUI.DANGER);
            dialog("Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask the user to confirm before deleting.
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // The first column stores the task's database ID.
        int taskId = (int) tableModel.getValueAt(row, 0);

        // Delete using a background database operation.
        deleteTaskInBackground(taskId);
        showStatus("✔  Task deleted.", SchedulerUI.SUCCESS);
    }

    /**
     * Automatically chooses an available time slot for the task based on its
     * priority and duration.
     */
    private void autoScheduleTask() {
        // Auto Schedule does not use the start time field.
        String taskName = taskNameField.getText().trim();
        String member = (String) familyMemberBox.getSelectedItem();
        String date = dateField.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();

        int duration;
        try {
            duration = Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException e) {
            dialog("Duration must be a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Auto Schedule still requires a task name and date.
        if (taskName.isEmpty() || date.isEmpty()) {
            dialog("Please enter a task name and date.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate date format before searching for a slot.
        if (!isValidDate(date)) {
            dialog("Date must be YYYY-MM-DD.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Duration must be positive.
        if (duration <= 0) {
            dialog("Duration must be > 0.", "Invalid Duration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Search the existing schedule for the first available slot.
        String time = findAvailableTimeSlot(member, date, duration, priority, databaseManager.getAllTasks());

        if (time == null) {
            dialog("No available slot found.", "Auto Schedule Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a task using the automatically selected start time.
        Task task = new Task(taskName, member, date, time, duration, priority);

        // Save the auto-scheduled task in the background.
        saveAutoScheduledTaskInBackground(task, time);
    }

    /**
     * Searches for an open time slot on a specific date.
     *
     * Priority affects the starting search time:
     * 
     *   High priority - starts looking at 8:00 AM.
     *   Medium priority - starts looking at 10:00 AM.
     *   Low priority - starts looking at 12:00 PM.
     */
    private String findAvailableTimeSlot(String member, String date, int duration, String priority, List<Task> existing) {
        int start;

        // Choose the earliest search time based on priority.
        if ("Medium".equals(priority)) {
            start = 600;
        } else if ("Low".equals(priority)) {
            start = 720;
        } else {
            start = 480;
        }

        // Search every 30 minutes until 8:00 PM.
        for (int minutes = start; minutes + duration <= 1200; minutes += 30) {
            // Create a temporary task to test whether the slot would conflict.
            Task test = new Task("tmp", member, date, convertMinutesToTime(minutes), duration, priority);

            // If there is no conflict, this is the first available slot.
            if (!conflictManager.hasConflict(test, existing)) {
                return convertMinutesToTime(minutes);
            }
        }

        // Return null if no valid slot was found.
        return null;
    }

    /**
     * Converts minutes after midnight into HH:MM format.
     */
    private String convertMinutesToTime(int total) {
        // E.g. 480 becomes 08:00.
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    //  Database background workers

    /**
     * Loads all tasks from the database and displays them in the table.
     */
    private void loadTasksIntoTable() {
        new SwingWorker<List<Task>, Void>() {
            @Override
            protected List<Task> doInBackground() {
                // Database access happens in the background thread.
                return databaseManager.getAllTasks();
            }

            @Override
            protected void done() {
                try {
                    // Retrieve the background result.
                    List<Task> tasks = get();

                    // Clear the table before repopulating it.
                    tableModel.setRowCount(0);

                    // Add one row for each task.
                    for (Task t : tasks) {
                        tableModel.addRow(new Object[] {
                                t.getTaskId(),
                                t.getTaskName(),
                                t.getFamilyMember(),
                                t.getDate(),
                                t.getStartTime(),
                                t.getEndTime(),
                                t.getDuration(),
                                t.getPriority()
                        });
                    }

                    // Update the status bar with the current task count.
                    if (statusLabel != null) {
                        showStatus("Showing " + tasks.size() + " task(s).", SchedulerUI.TEXT_MUTED);
                    }

                    // Refresh calendar view so both views stay in sync.
                    if (calendarPanel != null) {
                        calendarPanel.refresh();
                    }
                } catch (Exception ex) {
                    dialog("Error loading tasks: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Saves a manually created task without freezing the UI.
     */
    private void saveTaskInBackground(Task task) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // Insert the task into SQLite in the background.
                databaseManager.insertTask(task);
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Confirm no background exception occurred.
                    get();

                    // Reload the table/calendar and reset the form.
                    loadTasksIntoTable();
                    clearForm();

                    dialog("Task added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    dialog("Error saving: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Deletes a task without freezing the UI.
     */
    private void deleteTaskInBackground(int id) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // Delete the selected task from SQLite in the background.
                databaseManager.deleteTask(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Confirm no background exception occurred.
                    get();

                    // Reload current data after deletion.
                    loadTasksIntoTable();
                } catch (Exception ex) {
                    dialog("Error deleting: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Saves an auto-scheduled task and tells the user which time was selected.
     */
    private void saveAutoScheduledTaskInBackground(Task task, String time) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // Save the auto-scheduled task in SQLite.
                databaseManager.insertTask(task);
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Confirm no background exception occurred.
                    get();

                    // Refresh UI and reset form after saving.
                    loadTasksIntoTable();
                    clearForm();

                    dialog("Auto-scheduled at " + time + "!", "Auto Schedule Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    dialog("Error saving: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    //  Validation and helper methods

    /**
     * Resets the sidebar form to default values after a task is saved.
     */
    private void clearForm() {
        // Clear task name because it should be unique for each task.
        taskNameField.setText("");

        // Reset the remaining fields to demo-friendly defaults.
        dateField.setText("2026-05-10");
        startTimeField.setText("14:00");
        durationField.setText("60");

        // Default priority is Medium.
        priorityBox.setSelectedIndex(1);
    }

    /**
     * Checks only the date format, not whether the date is a real calendar date.
     */
    private boolean isValidDate(String date) {
        // Format required: four digits, dash, two digits, dash, two digits.
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Checks that time is in 24-hour HH:MM format.
     */
    private boolean isValidTime(String time) {
        // Valid examples: 09:00, 14:30, 23:59.
        return time.matches("([01]\\d|2[0-3]):[0-5]\\d");
    }

    /**
     * Updates the bottom status bar message.
     */
    private void showStatus(String msg, Color color) {
        // Set both the message and its status color.
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    /**
     * Shows a styled JOptionPane dialog.
     */
    private void dialog(String msg, String title, int type) {
        // Apply dark theme colors to JOptionPane.
        UIManager.put("OptionPane.background", SchedulerUI.BG_CARD);
        UIManager.put("Panel.background", SchedulerUI.BG_CARD);
        UIManager.put("OptionPane.messageForeground", SchedulerUI.TEXT_PRIMARY);

        // Display the dialog message to the user.
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    //  Status bar

    /**
     * Creates the bottom status bar used for small feedback messages.
     */
    private JPanel buildStatusBar() {
        // Main status bar container.
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SchedulerUI.BG_PANEL);

        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, SchedulerUI.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)));

        // Status label is updated throughout the app.
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(SchedulerUI.TEXT_MUTED);

        // Small version label on the right side.
        JLabel ver = SchedulerUI.mkLabel("v1.0", new Font("SansSerif", Font.PLAIN, 11), new Color(0x3A, 0x40, 0x58));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(ver, BorderLayout.EAST);

        return bar;
    }

    //  Main

    /**
     * Starts the Swing application on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Use a consistent cross-platform look and feel.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // If the look and feel cannot be set, Swing will use the default one.
            }


            new SmartHouseholdGUI().setVisible(true);
        });
    }
}