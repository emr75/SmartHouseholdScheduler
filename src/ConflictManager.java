import java.util.List;

/**
 * Handles all scheduling conflict checks for household tasks.
 * 
 * This class is responsible for validating schedules and preventing
 * overlapping tasks for the same family member.
 * 
 */
public class ConflictManager {

    /**
     * Checks whether a new task conflicts with any existing task.
     * 
     * A conflict occurs when:
     * a. Both tasks are on the same date
     * b. Both tasks belong to the same family member
     * c. The task times overlap
     * 
     * The method loops through all existing tasks and compares
     * them against the new task being added.
     * 
     * @param newTask The new task the user is attempting to add
     * @param existingTasks All currently scheduled tasks
     * @return true if a scheduling conflict exists, otherwise false
     */
    public boolean hasConflict(Task newTask, List<Task> existingTasks) {

        // Check the new task against every existing task
        for (Task existingTask : existingTasks) {

            // Verify whether both tasks occur on the same date
            boolean sameDate =
                    newTask.getDate().equals(existingTask.getDate());

            // Verify whether both tasks belong to the same family member
            boolean sameFamilyMember =
                    newTask.getFamilyMember().equals(existingTask.getFamilyMember());

            /*
             * A conflict only exists if:
             * - it is the same family member
             * - the same date
             * - overlapping times
             */
            if (sameDate &&
                    sameFamilyMember &&
                    overlaps(newTask, existingTask)) {

                return true;
            }
        }

        // No conflicts found
        return false;
    }

    /**
     * Determines whether two tasks overlap in time.
     * 
     * E.g.
     * Task 1 -> 2:00 PM to 3:00 PM
     * Task 2 -> 2:30 PM to 4:00 PM
     * 
     * These overlap because both tasks share time between
     * 2:30 PM and 3:00 PM.
     * 
     * @param task1 First task
     * @param task2 Second task
     * @return true if the tasks overlap, otherwise false
     */
    private boolean overlaps(Task task1, Task task2) {

        // Convert task 1 start time into total minutes
        int task1Start = convertTimeToMinutes(task1.getStartTime());

        // Calculate task 1 ending time
        int task1End = task1Start + task1.getDuration();

        // Convert task 2 start time into total minutes
        int task2Start = convertTimeToMinutes(task2.getStartTime());

        // Calculate task 2 ending time
        int task2End = task2Start + task2.getDuration();

        /*
         * Overlap logic:
         * 
         * Two tasks overlap if:
         * task1 starts before task2 ends
         * AND
         * task2 starts before task1 ends
         */
        return task1Start < task2End &&
                task2Start < task1End;
    }

    /**
     * Converts a time string into total minutes.
     * 
     * E.g.
     * 14:30 -> 870
     * 09:15 -> 555
     * 
     * This makes time comparisons easier for conflict detection.
     * 
     * @param time Time string in HH:MM format
     * @return Total number of minutes
     */
    private int convertTimeToMinutes(String time) {

        // Split the time into hour and minute components
        String[] parts = time.split(":");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        // Convert hours/minutes into total minutes
        return (hours * 60) + minutes;
    }
}