package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.entities.Camera;
import cz.cvut.fel.pjv.entities.Entity;
import cz.cvut.fel.pjv.entities.TransmittedPlayer;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Update class provides static methods to update positions and visibility for both
 * blocks and entities based on the camera position. It also handles re-positioning blocks
 * when they move off-screen to create a seamlessly looping/screen-wrapping effect.
 */
public class Update {
    final static int ADDITIONAL_RENDERING_DISTANCE_BLOCKS = 2;
    final static int ADDITIONAL_RENDERING_DISTANCE_ENTITIES = 6;


    /// Calculates position for blocks on screen
    /// @param blockDrawers ArrayList of all blockDrawers
    /// @param level Level object
    /// @param cameraX X coordinate of the camera
    /// @param cameraY Y coordinate of the camera
    public static void calculateNewBlockDrawersPos(List<BlockDrawer> blockDrawers, Level level, double cameraX, double cameraY) {
        int screenWidthInBlocks = (int)(Game.WIDTH / Game.BLOCK_SIZE + ADDITIONAL_RENDERING_DISTANCE_BLOCKS);
        int screenHeightInBlocks = (int)(Game.HEIGHT / Game.BLOCK_SIZE + ADDITIONAL_RENDERING_DISTANCE_BLOCKS);

        for (BlockDrawer drawer : blockDrawers) {
            boolean isBlockChanged = false;
            if(drawer.getCoordXOnLevel() - cameraX < (float)-screenWidthInBlocks/2){
                drawer.setCoordXOnLevel(drawer.getCoordXOnLevel() + screenWidthInBlocks);
                isBlockChanged = true;
            }
            else if(drawer.getCoordXOnLevel() - cameraX > (float)screenWidthInBlocks/2){
                drawer.setCoordXOnLevel(drawer.getCoordXOnLevel() - screenWidthInBlocks/* *(1 + (int)(drawer.getCoordXOnLevel() - cameraX - screenWidthInBlocks/2)/ screenWidthInBlocks)*/);
                isBlockChanged = true;
            }

            if(drawer.getCoordYOnLevel() - cameraY < (float)-screenHeightInBlocks/2){
                drawer.setCoordYOnLevel(drawer.getCoordYOnLevel() + screenHeightInBlocks/* *(1 + (int)(drawer.getCoordYOnLevel() - cameraY + screenHeightInBlocks)/ screenHeightInBlocks)*/);
                isBlockChanged = true;
            }
            else if(drawer.getCoordYOnLevel() - cameraY > (float)screenHeightInBlocks/2){
                drawer.setCoordYOnLevel(drawer.getCoordYOnLevel() - screenHeightInBlocks);
                isBlockChanged = true;
            }

            drawer.setCoordXOnScreen((float) Game.WIDTH  / 2 + (drawer.getCoordXOnLevel() - cameraX)* Game.BLOCK_SIZE);
            drawer.setCoordYOnScreen((float) Game.HEIGHT / 2 + (drawer.getCoordYOnLevel() - cameraY)* Game.BLOCK_SIZE);

            if (isBlockChanged) {
                    drawer.setBlockTexture(level.getBlock(drawer.getCoordXOnLevel(), drawer.getCoordYOnLevel()).getTexture());
            }
        }
    }

    /// Calculates position for entities
    /// @param entities ArrayList of all entities
    /// @param camera camera object
    public static void calculateEntityPos(ArrayList<Entity> entities, Camera camera)
    {

        int screenWidthInBlocks = (int)(Game.WIDTH / Game.BLOCK_SIZE + ADDITIONAL_RENDERING_DISTANCE_ENTITIES);
        int screenHeightInBlocks = (int)(Game.HEIGHT / Game.BLOCK_SIZE + ADDITIONAL_RENDERING_DISTANCE_ENTITIES);

        for (Entity entity : entities) {
            if(isInSight(screenWidthInBlocks, screenHeightInBlocks, entity.getX(), entity.getY(), camera.getX(), camera.getY()))
            {
                if(Game.host && (!(entity instanceof TransmittedPlayer) || entity.id == Long.MIN_VALUE)) {
                    entity.drawer.blockImageView.setVisible(true);
                }
                else if(!Game.host) entity.drawer.blockImageView.setVisible(true);
                entity.setFreeze(false);

                entity.drawer.setCoordXOnScreen(Game.WIDTH/2 + (entity.getX() - camera.getX())* Game.BLOCK_SIZE);
                entity.drawer.setCoordYOnScreen(Game.HEIGHT/2 + (entity.getY() - camera.getY())* Game.BLOCK_SIZE);
            }
            else{
                entity.drawer.blockImageView.setVisible(false);
                entity.setFreeze(true);
            }
        }
    }

    /// Checks if the enemy is within rendering distance
    private static boolean isInSight(int screenWidthInBlocks, int screenHeightInBlocks, double x, double y, double cameraX, double cameraY) {
        if(x - cameraX < (float)-screenWidthInBlocks/2){
            return false;
        }
        else if(x - cameraX > (float)screenWidthInBlocks/2){
            return false;
        }
        else if(y - cameraY < (float)-screenHeightInBlocks/2){
            return false;
        }
        else if(y - cameraY > (float)screenHeightInBlocks/2){
            return false;
        }

        return true;
    }

    /// Updates the position of entity's Drawer
    public static void updateEntityOnScreen(ArrayList<Entity> entities, ObservableList<Node> entitiesViewersList)
    {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity entity = it.next();

            if (entity.getState() == Entity.State.TO_DESTROY) {
                entity.drawer.getBlockImageView().setVisible(false);
                entity.setState(Entity.State.DESTROYED);
            } else if (entity.getState() == Entity.State.DESTROYED){
                if(Game.multiplayer && Game.host && !entity.isTransmitted())  continue;
                entitiesViewersList.remove(entity.drawer.getBlockImageView());
                it.remove();
            }
            else {
                entity.drawer.update();
            }
        }
    }

    /// Updates the position of block's Drawer
    public static void updateBlockDrawers(List<BlockDrawer> blockDrawers) {
        for (BlockDrawer drawer : blockDrawers) {
            drawer.update();
        }
    }
}
