import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for the Smart Household Scheduler.
 *
 * This class is responsible for connecting to the SQLite database,
 * creating the tasks table, inserting tasks, loading tasks, updating tasks,
 * and deleting tasks.
 *
 */
public class DatabaseManager {

    /*
     * SQLite database connection URL.
     *
     * If database file does not exist, SQLite will create it
     * automatically when program connects.
     */
    private static final String DB_URL = "jdbc:sqlite:household_scheduler.db";

    /**
     * Opens a connection to the SQLite database.
     *
     * Each database operation creates its own connection using this method.
     * Try-with-resources blocks below automatically close the connection after the operation finishes.
     * @return A Connection object connected to the SQLite database
     * @throws SQLException if the database connection fails
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Creates the tasks table if it does not already exist.
     *
     */
    public void createTasksTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    task_name TEXT NOT NULL,
                    family_member TEXT NOT NULL,
                    date TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    duration INTEGER NOT NULL,
                    priority TEXT NOT NULL
                );
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("Error creating tasks table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new task into the database.
     *
     * PreparedStatement is used instead of directly joining strings into the
     * SQL command for safety and reliability
     * 
     * @param task - The Task object to save
     */
    public void insertTask(Task task) {
        String sql = """
                INSERT INTO tasks(task_name, family_member, date, start_time, duration, priority)
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTaskName());
            pstmt.setString(2, task.getFamilyMember());
            pstmt.setString(3, task.getDate());
            pstmt.setString(4, task.getStartTime());
            pstmt.setInt(5, task.getDuration());
            pstmt.setString(6, task.getPriority());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error inserting task: " + e.getMessage());
        }
    }

    /**
     * Loads all tasks from the database.
     *
     * The results are sorted by date and start time so the schedule appears
     * in a logical order in the GUI table.
     *
     * @return A list of Task objects loaded from the database
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();

        String sql = """
                SELECT id, task_name, family_member, date, start_time, duration, priority
                FROM tasks
                ORDER BY date, start_time;
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            /*
             * Each row in the ResultSet represents one task from the database.
             * The row data is used to rebuild a Task object in Java.
             */
            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("task_name"),
                        rs.getString("family_member"),
                        rs.getString("date"),
                        rs.getString("start_time"),
                        rs.getInt("duration"),
                        rs.getString("priority")
                );

                tasks.add(task);
            }

        } catch (SQLException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }

        return tasks;
    }

    /**
     * Deletes a task from the database by its unique task ID.
     *
     * @param taskId The ID of the task to delete
     */
    public void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?;";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error deleting task: " + e.getMessage());
        }
    }

    /**
     * Updates the date of a task already stored in the database.
     *
     * This can be used if the user wants to move a task to a different date
     * without deleting and recreating the task.
     *
     * @param taskId The ID of the task to update
     * @param newDate The new date in YYYY-MM-DD format
     */
    public void updateTaskDate(int taskId, String newDate) {
        String sql = "UPDATE tasks SET date = ? WHERE id = ?;";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newDate);
            ps.setInt(2, taskId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error updating task date: " + e.getMessage());
        }
    }

    /**
     * Clears all tasks from the database.
     *
     * This method is useful for testing or resetting the application.
     * However, it will remove every saved task.
     */
    public void clearAllTasks() {
        String sql = "DELETE FROM tasks;";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            System.out.println("Error clearing tasks: " + e.getMessage());
        }
    }
}