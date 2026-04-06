package cz.cvut.fel.pjv.blocks;
import java.io.Serializable;

/**
 * The Block class serves as a base for various block types in the game.
 *
 * <p>
 * It implements Serializable for potential file operations and defines common
 * behaviors such as identifying if a block is a wall.
 * </p>
 */
public abstract class Block implements Serializable {
    protected boolean wall = false;
    public abstract int getId();
    public abstract String getTexture();
    public boolean isWall(){
        return wall;
    }
}
