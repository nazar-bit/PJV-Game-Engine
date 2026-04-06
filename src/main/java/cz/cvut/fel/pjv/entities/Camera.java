package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.Level;


import java.util.ArrayList;

import static cz.cvut.fel.pjv.Game.*;


/**
 * The Camera class is used to follow an entity and keep the screen centered around it.
 *
 * <p>
 * This class extends the Entity class, allowing it to inherit position and dimension properties.
 * The camera's position is updated based on the observed entity's position.
 * If the observed entity is not alive or is not suitable to follow, it defaults back to the player.
 * </p>
 */
public class Camera extends Entity {
    private Entity observedEntity;
    private double entityImageViewCoordX;
    private double entityImageViewCoordY;
    /**
     * Constructs a Camera that follows the specified entity.
     *
     * @param entity the entity to follow
     */
    public Camera(Entity entity) {
        super(entity.getX(), entity.getY(), 0, 0, "empty");
        this.observedEntity = entity;
        this.entityImageViewCoordX = WIDTH/2 - entity.getWidth() * BLOCK_SIZE/2;
        this.entityImageViewCoordY = HEIGHT/2 - entity.getHeight() * BLOCK_SIZE/2;
    }
    /**
     * Constructs a Camera with a specified position.
     *
     * @param x the initial x-coordinate of the camera
     * @param y the initial y-coordinate of the camera
     */
    public Camera(double x, double y) {
        super(x, y, 0, 0, "empty");
    }
    @Override
    public void update(Level level, ArrayList<Entity> entities) {

        if (observedEntity != null && (observedEntity.getState() == State.ALIVE || observedEntity.getTeam() == Team.Players)) {
            x = observedEntity.getX() + observedEntity.getWidth() / 2;
            y = observedEntity.getY() + observedEntity.getHeight() / 2;
        }else {
            if (player != null) {
                setObservedEntity(player);
            }
        }

    }

    ///  setObservedEntity method is used to set the entity that the camera will follow
    public void setObservedEntity(Entity entity) { observedEntity = entity;
        this.entityImageViewCoordX = WIDTH/2 - entity.getWidth() * BLOCK_SIZE/2;
        this.entityImageViewCoordY = HEIGHT/2 - entity.getHeight() * BLOCK_SIZE/2;
    }
    public Entity getObservedEntity() { return observedEntity; }
    public double getEntityImageViewCoordX() {
        return entityImageViewCoordX;
    }
    public double getEntityImageViewCoordY() {
        return entityImageViewCoordY;
    }
}
