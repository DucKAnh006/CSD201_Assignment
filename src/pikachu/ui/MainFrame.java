package pikachu.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The MainFrame class represents the main window of the Pikachu Classic game, which serves as the container for all the different panels (e.g., menu, game, etc.) and manages the switching between them.
 */
public class MainFrame extends JFrame {
    
    /**
     * Constructs the MainFrame by setting up the window properties, initializing the main menu panel, and making the frame visible. It also sets the default close operation to exit the application when the window is closed.
     */
    public MainFrame() {
        // Set the title of the window, its size, and the default close operation
        setTitle("Pikachu Classic Group 1");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Initialize the main menu panel and switch to it as the initial view of the application
        MenuUI menu = new MenuUI(this);
        switchPanel(menu);
        
        // Center the window on the screen and make it visible
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Switches the current panel displayed in the main frame to the specified next panel. This method removes all existing components from the content pane, adds the new panel, and then revalidates and repaints the frame to update the display.
     * @param nextPanel The JPanel that should be displayed in the main frame.
     */
    public void switchPanel(JPanel nextPanel) {
        // Remove all existing components from the content pane to prepare for the new panel
        this.getContentPane().removeAll();
        
        // Add the new panel to the content pane of the main frame
        add(nextPanel);
        
        // Revalidate and repaint the frame to ensure the new panel is displayed correctly
        revalidate();
        repaint();
    }
}