import javax.swing.SwingUtilities;

/**
 * Start for application.
 *
 * This class is responsible for starting the program and launching
 * the GUI.
 *
 * The application uses SwingUtilities.invokeLater() to ensure that
 * all Swing GUI components are created and updated on the
 * Event Dispatch Thread (EDT) to prevent thread issues and keep interface responsive.
 */
public class Main {

    /**
     * Main method that starts the application.
     *
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // Create the main GUI window
            SmartHouseholdGUI gui = new SmartHouseholdGUI();

            // Make the GUI visible to the user
            gui.setVisible(true);

        });
    }
}