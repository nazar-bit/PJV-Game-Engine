package cz.cvut.fel.pjv.blocks;

/**
 * The Brick class is an implementation of the Block class that represents a standard brick.
 *
 * <p>
 * This class provides a unique identifier and texture name for a brick block.
 * </p>
 */
public class Brick extends Block {
    public static final int ID = 2;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getTexture() {
        return "brick";
    }
}
