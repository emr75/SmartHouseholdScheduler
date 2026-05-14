/**
 * Represents a household task or appointment.
 */
public class Task {

    // Unique ID for the task
    private int taskId;

    // Name/description of the task
    private String taskName;

    // Family member assigned to the task
    private String familyMember;

    // Date of the task (Example: 2026-05-10)
    private String date;

    // Start time of the task (Example: 14:00)
    private String startTime;

    // Duration in minutes
    private int duration;

    // Task priority (High, Medium, Low)
    private String priority;

    /**
     * Constructor used when creating a new task.
     */
    public Task(String taskName, String familyMember,
                String date, String startTime,
                int duration, String priority) {

        this.taskName = taskName;
        this.familyMember = familyMember;
        this.date = date;
        this.startTime = startTime;
        this.duration = duration;
        this.priority = priority;
    }

    /**
     * Constructor used when loading a task from the database.
     */
    public Task(int taskId, String taskName, String familyMember,
                String date, String startTime,
                int duration, String priority) {

        this.taskId = taskId;
        this.taskName = taskName;
        this.familyMember = familyMember;
        this.date = date;
        this.startTime = startTime;
        this.duration = duration;
        this.priority = priority;
    }

    // Getters and Setters

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getFamilyMember() {
        return familyMember;
    }

    public void setFamilyMember(String familyMember) {
        this.familyMember = familyMember;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Calculates the ending hour and minute of the task.
     * E.g.
     * Start = 14:00
     * Duration = 90
     * End = 15:30
     */
    public String getEndTime() {

        String[] timeParts = startTime.split(":");

        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        int totalMinutes = (hour * 60) + minute + duration;

        int endHour = totalMinutes / 60;
        int endMinute = totalMinutes % 60;

        return String.format("%02d:%02d", endHour, endMinute);
    }

    /**
     * Returns task information as a readable string.
     */
    @Override
    public String toString() {
        return "Task ID: " + taskId +
                "\nTask: " + taskName +
                "\nFamily Member: " + familyMember +
                "\nDate: " + date +
                "\nStart Time: " + startTime +
                "\nEnd Time: " + getEndTime() +
                "\nDuration: " + duration + " minutes" +
                "\nPriority: " + priority;
    }
}