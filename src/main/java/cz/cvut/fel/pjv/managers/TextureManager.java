package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.Game;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * The TextureManager class is responsible for loading and storing texture images.
 *
 * <p>It retrieves textures from an internal resource path and caches them for reuse,
 * thereby reducing the need to repeatedly load images from disk or other sources.</p>
 */
public class TextureManager {

    private final String path = "/textures/";
    private final HashMap<String, Image> images = new HashMap<>();
    public static final Logger logger = Logger.getLogger("TextureManager");

    /**
     * Constructs a new TextureManager instance.
     */
    public TextureManager() {
    }
    /// returns texture from the internal resource
    public Image getTexture(String texture) {
        if (images.containsKey(texture)) {
            return images.get(texture);
        } else {
            InputStream stream = getClass().getResourceAsStream(path + texture + ".png");
            if (stream == null) {
                logger.severe("Texture not found: " + texture);
                return null;
            }
            Image image = new Image(stream); // Load directly from the stream
            images.put(texture, image);
            return image;
        }
    }


}


