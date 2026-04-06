package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.ShareSound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * The SoundManager class is responsible for playing and managing .wav sound effects.
 *
 * <p>It maintains a pool of audio clips for efficient sound playback and avoids excessive sound
 * instantiation by reusing clips if available.</p>
 */
public class SoundManager {
    private static final Logger logger = Logger.getLogger("SoundManager");
    private final Map<String, List<Clip>> clipPool = new HashMap<>();
    private final int MAX_CLIPS = 30;


    /** Plays any .wav sound
     * @param path path to .wav file
     */
    public void playSound(String path) {
        if (!path.endsWith(".wav")) path += ".wav";

        List<Clip> pool = clipPool.computeIfAbsent(path, k -> new ArrayList<>());

        for (Clip clip : pool) {
            if (!clip.isRunning()) {
                clip.setFramePosition(0);
                clip.start();
                return;
            }
        }


        if (pool.size() < MAX_CLIPS) {
            Clip newClip = loadClip(path);
            if (newClip != null) {
                pool.add(newClip);
                newClip.start();
            }
        } else {
            logger.warning("Too many sounds playing: " + path);
        }
    }


    /** Loads clip if it's a new sound
     * @param path path to .wav file
     * @return Clip
     */
    private Clip loadClip(String path) {
        try {
            URL soundURL = getClass().getResource(path);
            if (soundURL == null) {
                logger.warning("Sound file not found: " + path);
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            logger.warning("Failed to load sound file: " + path);
            e.printStackTrace();
        }
        return null;
    }
}