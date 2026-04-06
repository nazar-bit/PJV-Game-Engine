package cz.cvut.fel.pjv.blocks;

/**
 * The BlackBrick class represents a block with a black brick texture.
 *
 * <p>
 * This block is considered a wall, and it returns a unique ID and texture name.
 * </p>
 */
public class BlackBrick extends Block {
    public static final int ID = 1;

    public BlackBrick() {
        wall = true;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getTexture() {
        return "black_brick";
    }
}
