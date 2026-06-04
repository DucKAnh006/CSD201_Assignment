package pikachu.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("Pikachu Classic Group 1");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MenuUI menu = new MenuUI(this);
        switchPanel(menu);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void switchPanel(JPanel nextPanel) {
        this.getContentPane().removeAll();
        
        add(nextPanel);
        
        revalidate();
        repaint();
    }
}