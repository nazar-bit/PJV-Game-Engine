package cz.cvut.fel.pjv.blocks;

/**
 * The VoidBlock class represents a block that acts as a void or empty space
 * but is still considered a wall.
 *
 * <p>
 * It provides a unique identifier and a texture for rendering.
 * </p>
 */
public class VoidBlock extends  Block {
    public static final int ID = 0;
    public VoidBlock() {
        wall = true;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getTexture() {
        return "void";
    }
}
