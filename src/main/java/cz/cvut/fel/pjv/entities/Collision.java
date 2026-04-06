package cz.cvut.fel.pjv.entities;

import java.io.Serializable;


/**
 * The Collision class represents a collision boundary used to detect
 * interactions between entities, blocks, or other unmovable objects.
 *
 * <p>
 * This class stores the position and dimensions of the collision area,
 * along with the type of collision it represents.
 * </p>
 */
public class Collision implements Serializable {
    public enum CollisionType{
        BLOCK,
        ENTITY,
        UNMOVABLE,
    }
    private double x, y, width, height;
    private CollisionType type;
    private transient Entity entity;
    /**
     * Constructs a Collision instance for blocks with the given parameters.
     *
     * @param x the x-coordinate of the collision area.
     * @param y the y-coordinate of the collision area.
     * @param width the width of the collision area.
     * @param height the height of the collision area.
     * @param type the type of collision.
     */
    public  Collision(double x, double y, double width, double height, CollisionType type){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    /**
     * Constructs a Collision instance for entities with an associated Entity.
     *
     * @param x the x-coordinate of the collision area.
     * @param y the y-coordinate of the collision area.
     * @param width the width of the collision area.
     * @param height the height of the collision area.
     * @param type the type of collision.
     * @param entity the entity associated with this collision.
     */
    public  Collision(double x, double y, double width, double height, CollisionType type, Entity entity){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.entity = entity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public double getWidth() {
        return width;
    }
    public double getHeight() {
        return height;
    }
    public CollisionType getType() {
        return type;
    }
    public Entity getEntity() {
        return entity;
    }
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
