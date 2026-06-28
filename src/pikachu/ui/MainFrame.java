package pikachu.ui;

import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;

/**
 * The MainFrame class represents the main window of the Pikachu Classic game, which serves as the container for all the different panels (e.g., menu, game, etc.) and manages the switching between them.
 */
public class MainFrame extends JFrame {
    private Clip clip;
    private boolean isMuted = false;
    
    /**
     * Constructs the MainFrame by setting up the window properties, initializing the main menu panel, and making the frame visible. It also sets the default close operation to exit the application when the window is closed.
     */
    public MainFrame() {
        // Set the title of the window, its size, and the default close operation
        setTitle("Pikachu Classic Group 1");
        setSize(1200, 900);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Initialize the main menu panel and switch to it as the initial view of the application
        MenuUI menu = new MenuUI(this);
        switchPanel(menu);
        
        // Center the window on the screen and make it visible
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private Timer fadeTimer;
    private final float FADE_STEP = 2.0f; // decibels to adjust per tick
    private final int FADE_DELAY = 50; // ms per tick
    private String currentMusic = null;
    private float currentVolume = 0.0f;

    /**
     * Switches the current panel displayed in the main frame to the specified next panel. This method removes all existing components from the content pane, adds the new panel, and then revalidates and repaints the frame to update the display.
     * @param nextPanel The JPanel that should be displayed in the main frame.
     */
    public void switchPanel(JPanel nextPanel) {
        // Remove all existing components from the content pane to prepare for the new panel
        this.getContentPane().removeAll();
        
        String nextMusic = (nextPanel instanceof MenuUI) ? "menu_bgr.wav" : "game_bgr.wav";
        fadeToBackgroundMusic(nextMusic);
        
        // Add the new panel to the content pane of the main frame
        add(nextPanel);
        
        // Revalidate and repaint the frame to ensure the new panel is displayed correctly
        revalidate();
        repaint();
    }
    
    /**
     * Plays a sound effect file given its file name.
     * The sound will not play if the game is currently muted.
     * 
     * @param soundFileName The name of the sound file to be played.
     */
    public void playSound(String soundFileName) {
        if (isMuted) {
            return; // Skip sound playing if the game is muted
        }

        try {
            String soundPath = "/pikachu/sound/" + soundFileName;
            java.net.URL soundURL = InGame.class.getResource(soundPath);

            if (soundURL != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                Clip sfxClip = AudioSystem.getClip();
                sfxClip.open(audioIn);
                sfxClip.start();
                Timer timer = new Timer(3000, t -> {
                    sfxClip.close();
                });

                timer.setRepeats(false);
                timer.start();
            } else {
                System.out.println("Error: Can not find sound with path: " + soundFileName);
            }
        } catch (Exception e) {
            System.out.println("Can not play sound: " + e.getMessage());
        }
    }
    
    /**
     * Fades out the currently playing background music (if any) and then starts
     * fading in the newly specified background music. This creates a smooth
     * transition between different audio tracks (e.g. from Menu to InGame).
     * 
     * @param soundFileName The name of the new background music file to play.
     */
    public void fadeToBackgroundMusic(String soundFileName) {
        boolean sameMusic = soundFileName.equals(currentMusic);
        currentMusic = soundFileName;

        if (isMuted) {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
            return;
        }

        if (sameMusic && (clip != null && clip.isRunning())) {
            return; // Already playing this music track
        }

        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        if (clip != null && clip.isRunning()) {
            try {
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    currentVolume = gainControl.getValue();
                    float targetMin = Math.max(gainControl.getMinimum(), -40.0f);
                    
                    fadeTimer = new Timer(FADE_DELAY, e -> {
                        try {
                            currentVolume -= FADE_STEP;
                            if (currentVolume <= targetMin) {
                                gainControl.setValue(targetMin);
                                clip.stop();
                                clip.close();
                                fadeTimer.stop();
                                playNewMusic(soundFileName);
                            } else {
                                gainControl.setValue(currentVolume);
                            }
                        } catch (Exception ex) {
                            fadeTimer.stop();
                            clip.close();
                            playNewMusic(soundFileName);
                        }
                    });
                    fadeTimer.start();
                } else {
                    clip.stop();
                    clip.close();
                    playNewMusic(soundFileName);
                }
            } catch (Exception e) {
                clip.close();
                playNewMusic(soundFileName);
            }
        } else {
            if (clip != null) clip.close();
            playNewMusic(soundFileName);
        }
    }

    /**
     * Helper method to load and play a new background music track on a continuous loop.
     * 
     * @param soundFileName The name of the sound file to be played as background music.
     */
    private void playNewMusic(String soundFileName) {
        try {
            String soundPath = "/pikachu/sound/" + soundFileName;
            java.net.URL soundURL = InGame.class.getResource(soundPath);

            if (soundURL != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                clip = AudioSystem.getClip();
                clip.open(audioIn);
                
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float targetMax = Math.min(gainControl.getMaximum(), 0.0f);
                    gainControl.setValue(targetMax); // Start at normal volume immediately
                }
                
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Error: Can not find sound with path: " + soundFileName);
            }
        } catch (Exception e) {
            System.out.println("Can not play background sound: " + e.getMessage());
        }
    }
    
    /**
     * Checks whether the game's audio is currently muted.
     * 
     * @return true if the audio is muted, false otherwise.
     */
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Sets the mute state of the game's audio.
     * If muted, all currently playing sounds are stopped. If unmuted,
     * the current background music is resumed.
     * 
     * @param isMuted true to mute the audio, false to unmute.
     */
    public void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
        if (isMuted) {
            if (fadeTimer != null && fadeTimer.isRunning()) {
                fadeTimer.stop();
            }
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        } else {
            // Unmute: restart background music
            if (currentMusic != null) {
                playNewMusic(currentMusic);
            }
        }
    }
}