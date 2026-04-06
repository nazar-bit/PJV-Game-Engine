package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.managers.TextureManager;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;


public class TestTextureManager {


    public TestTextureManager() throws FileNotFoundException {
    }

    @Test
    public void testGetTextureNotNull() {
        TextureManager textureManager = new TextureManager();
        Image texture = textureManager.getTexture("empty");
        assertNotNull(texture);
    }


    @Test
    public void testGetTextureSameTexture() {
        TextureManager textureManager = new TextureManager();
        Image texture = textureManager.getTexture("empty");
        Image texture2 = textureManager.getTexture("empty");
        assertEquals(texture, texture2);
    }


    @Test
    public void testGetTextureDifferentTexture() {
        TextureManager textureManager = new TextureManager();
        Image texture = textureManager.getTexture("empty");
        Image texture2 = textureManager.getTexture("ak47");
        assertNotEquals(texture, texture2);
    }


    @Test
    public void testGetTextureNotExistent() {
        TextureManager textureManager = new TextureManager();
        Image texture = textureManager.getTexture("abcdrt");
        assertNull(texture);
    }
}