package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.blocks.Block;

/**
 * BlockDrawer extends the Drawer class to handle graphical representations of blocks.
 *
 * <p>
 * In addition to the screen coordinates and texture data inherited from Drawer, BlockDrawer also
 * stores the coordinates of the block on the level grid. It provides helper methods to convert screen
 * coordinates to level grid positions.
 * </p>
 */
public class BlockDrawer extends Drawer {
    private int coordXOnLevel;
    private int coordYOnLevel;

    /**
     * Constructs a new BlockDrawer with specified screen positions, texture, and level grid coordinates.
     *
     * @param coordXOnScreen the x-coordinate on the screen.
     * @param coordYOnScreen the y-coordinate on the screen.
     * @param texture the texture to be used for drawing the block.
     * @param coordXOnLevel the x-coordinate on the level grid.
     * @param coordYOnLevel the y-coordinate on the level grid.
     */
    public BlockDrawer(double coordXOnScreen, double coordYOnScreen, String texture, int coordXOnLevel, int coordYOnLevel) {
        super(coordXOnScreen, coordYOnScreen, texture, 1, 1);
        this.coordXOnLevel = coordXOnLevel;
        this.coordYOnLevel = coordYOnLevel;
    }

    public int getCoordXOnLevel() {
        return coordXOnLevel;
    }

    public int getCoordYOnLevel() {
        return coordYOnLevel;
    }

    public void setCoordXOnLevel(int coordXOnLevel) {
        this.coordXOnLevel = coordXOnLevel;
    }

    public void setCoordYOnLevel(int coordYOnLevel) {
        this.coordYOnLevel = coordYOnLevel;
    }

    public static int calculateBlockLevelX(int coordXOnScreen, double dCameraX, int playerWidth) {
        return (int)(dCameraX + (coordXOnScreen - Game.WIDTH /2 + playerWidth/2)/Game.BLOCK_SIZE);
    }

    public static int calculateBlockLevelY(int coordYOnScreen, double dCameraY, int playerHeight) {
        return (int)(dCameraY + (coordYOnScreen - Game.HEIGHT /2 + playerHeight/2)/Game.BLOCK_SIZE);
    }
}
