package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.blocks.Block;
import cz.cvut.fel.pjv.blocks.VoidBlock;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;
import java.util.List;

import static cz.cvut.fel.pjv.Game.player;

/**
 * Setup class provides helper methods to configure UI and block rendering for the game.
 */
public class Setup {


    /**
     * Creates and configures a label to display the current coordinates.
     *
     * <p>
     * The label is positioned in the top-right corner of the screen and formatted with a specific
     * font size and text color.
     * </p>
     *
     * @return a Label instance displaying coordinates.
     */
    public static Label setCoordLabel() {
        Label coordLabel = new Label("X:  Y:");
        coordLabel.setLayoutX(Game.WIDTH - 130);
        coordLabel.setLayoutY(10);
        coordLabel.setTextFill(Color.color(57/255.0, 255/255.0, 20/255.0));
        coordLabel.setStyle("-fx-font-size: 15");
        return coordLabel;
    }


    /// Sets up the block drawers and adds them to the blocksList for further rendering
    /// @param blocksList List of all blocks
    /// @param blockDrawers List of all block drawers
    /// @param level Level object
    /// @param cameraX X coordinate of the camera
    /// @param cameraY Y coordinate of the camera
    public static void setBlocksGroup(ObservableList<Node> blocksList, List<BlockDrawer> blockDrawers, Level level, double cameraX, double cameraY) {
        int blockX = (int)(cameraX - (Game.WIDTH /2)/ Game.BLOCK_SIZE - 1);
        int blockY = (int)(cameraY - (Game.HEIGHT /2)/ Game.BLOCK_SIZE - 1 );


        int numberOfBlocks = (int)((Game.HEIGHT /Game.BLOCK_SIZE+4) * (Game.WIDTH /Game.BLOCK_SIZE+4));
        for (int i = 0; i < (Game.HEIGHT /Game.BLOCK_SIZE+2); i++) {
            for (int j = 0; j < (Game.WIDTH /Game.BLOCK_SIZE+2); j++) {
                double blockXOnScreen = Game.WIDTH /2 + (blockX - cameraX)*Game.BLOCK_SIZE;
                double blockYOnScreen = Game.HEIGHT /2 + (blockY - cameraY)*Game.BLOCK_SIZE;

                if(level.isInLevel(blockX, blockY)){
                    Block block = level.getBlock(blockX, blockY);
                    BlockDrawer drawer = new BlockDrawer(blockXOnScreen, blockYOnScreen, block.getTexture(), blockX, blockY);
                    blockDrawers.add(drawer);
                    blocksList.add(drawer.getBlockImageView());
                }
                else {
                    BlockDrawer drawer = new BlockDrawer(blockXOnScreen, blockYOnScreen, new VoidBlock().getTexture(), blockX, blockY);
                    blockDrawers.add(drawer);
                    blocksList.add(drawer.getBlockImageView());
                }
                blockX+=1;
            }
                blockX = (int)(cameraX - (Game.WIDTH /2)/Game.BLOCK_SIZE);
                blockY+=1;

        }
    }
}
