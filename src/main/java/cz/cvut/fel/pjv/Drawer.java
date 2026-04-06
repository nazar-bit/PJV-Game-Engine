package cz.cvut.fel.pjv;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static cz.cvut.fel.pjv.managers.TextureManager.logger;

/**
 * Drawer is responsible for representing and updating the graphical aspects of game objects.
 *
 * <p>
 * It wraps an ImageView that displays an image on the screen, provides methods to update the
 * object's position, change its texture, and rotate it when necessary.
 * </p>
 */
public class Drawer {
    protected double coordXOnScreen;
    protected double coordYOnScreen;
    protected String blockTexture;
    protected boolean isTextureChanged = false;
    protected Image blockImage;
    protected ImageView blockImageView;


    /**
     * Constructs a new Drawer for a game object.
     *
     * @param coordXOnScreen the x-coordinate on the screen where the image is placed.
     * @param coordYOnScreen the y-coordinate on the screen where the image is placed.
     * @param texture the texture identifier to load the image.
     * @param width the number of blocks in the horizontal direction (multiplied by Game.BLOCK_SIZE).
     * @param height the number of blocks in the vertical direction (multiplied by Game.BLOCK_SIZE).
     */
    public Drawer(double coordXOnScreen, double coordYOnScreen , String texture, double width, double height) {
        this.coordXOnScreen = coordXOnScreen;
        this.coordYOnScreen = coordYOnScreen;
        blockTexture = texture;
        try{
            blockImage = Game.textureManager.getTexture(texture);
            blockImageView = new ImageView(blockImage);
            blockImageView.setX(coordXOnScreen);
            blockImageView.setY(coordYOnScreen);


            blockImageView.setFitWidth(width*Game.BLOCK_SIZE);
            blockImageView.setFitHeight(height*Game.BLOCK_SIZE);
        } catch (Exception e) {
            logger.warning("Texture " + texture + " not found.");
            e.printStackTrace();
        }
    }

    /**
     * Updates the position and texture of the ImageView.
     *
     * <p>
     * This method should be called when the object's screen position or texture has changed.
     * </p>
     */
    public void update() {
        blockImageView.setX(coordXOnScreen);
        blockImageView.setY(coordYOnScreen);
        if (isTextureChanged) {
            try{
                blockImageView.setImage(Game.textureManager.getTexture(blockTexture));
                isTextureChanged = false;
            } catch (Exception e) {
                logger.warning("Texture " + blockTexture + " not found.");
                e.printStackTrace();
            }
        }

    }

    /**
     * Sets a new texture for the block.
     *
     * @param texture the name of the new texture.
     */
    public void setBlockTexture(String texture) {
        this.blockTexture = texture;
        isTextureChanged = true;
    }

    public void setCoordXOnScreen(double coordXOnScreen) {
        this.coordXOnScreen = coordXOnScreen;
    }

    public void setCoordYOnScreen(double coordYOnScreen) {
        this.coordYOnScreen = coordYOnScreen;
    }

    public double getCoordXOnScreen() {
        return coordXOnScreen;
    }

    public double getCoordYOnScreen() {
        return coordYOnScreen;
    }


    public void rotate(double angle) {
        blockImageView.setRotate(angle);
    }


    public Node getBlockImageView() {
        return blockImageView;
    }

    public void setVisible(boolean b) {
        blockImageView.setVisible(b);
    }

}
