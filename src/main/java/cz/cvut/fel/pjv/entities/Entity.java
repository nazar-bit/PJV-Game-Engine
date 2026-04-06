package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.Drawer;
import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Level;

import java.io.Serializable;
import java.util.*;

/**
 * The Entity class represents a basic game object with position, dimensions,
 * health, and collision handling. This abstract class serves as a base for all
 * game objects, providing common functionality like movement, collision detection,
 * and state management.
 *
 * <p>
 * Each entity holds a Drawer for rendering and may participate in collision
 * detection with both blocks and other entities.
 * </p>
 */
public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The possible directions for an entity.
     */
    public enum Direction {
        /// DIRECTION NONE MUST ALWAYS BE AT THE END!
        UP(-Math.PI/2), DOWN(Math.PI/2), LEFT(Math.PI), RIGHT(0),
        UP_LEFT(-Math.PI*3/4), UP_RIGHT(-Math.PI/4),
        DOWN_LEFT(3*Math.PI/4), DOWN_RIGHT(Math.PI/4), NONE(0);

        private final double angle;

        /**
         * Constructs a direction with the specified angle.
         *
         * @param angle the angle, in radians, associated with the direction.
         */
        Direction(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    /**
     * Represents the state of an entity.
     */
    public enum State {
        ALIVE, DEAD, TO_DESTROY, DESTROYED;
    }
    protected boolean collisionImmunity = false;
    protected boolean movable = true;
    protected boolean collisionWithBlocks = true;
    protected double health = Double.POSITIVE_INFINITY;
    protected double x;
    protected double y;
    protected Direction direction;
    protected double velocity;
    protected double speed;
    protected double velocityAngle; /// in radians
    protected double width;
    protected double height;
    public transient Drawer drawer;
    protected boolean freeze;
    protected State state;
    protected double pushingPower = 1;
    protected Team team;
    protected String texture;
    public long id;
    protected double rotationAngle = 0;
    protected boolean transmitted = true;

    /**
     * Comparator used for sorting collisions in order of collision area.
     */
    private static class CollisionAreaComparator implements Comparator<Collision>, Serializable {
        @Override
        public int compare(Collision c1, Collision c2) {
            double area1 = Math.abs(c1.getWidth() * c1.getHeight());
            double area2 = Math.abs(c2.getWidth() * c2.getHeight());
            return Double.compare(area1, area2);
        }
    }

    /**
     * Sorted set holding collisions for the entity.
     * Sorted by the collision area.
     */
    protected transient SortedSet<Collision> collisions = new TreeSet<>(new CollisionAreaComparator());

    /**
     * Constructs a new Entity with the specified position, dimensions, and texture.
     *
     * @param x       the x-coordinate of the entity.
     * @param y       the y-coordinate of the entity.
     * @param width   the width of the entity.
     * @param height  the height of the entity.
     * @param texture the texture identifier for the entity.
     */
    public Entity(double x, double y, double width, double height, String texture) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.drawer = new Drawer(x, y, texture, (int)width, (int)height);
        this.freeze = false;
        state = State.ALIVE;
        this.id = Game.entityIdCounter++;
    }

    /**
     * Constructs a new Entity with additional parameters such as health, speed, and team.
     *
     * @param x       the x-coordinate of the entity.
     * @param y       the y-coordinate of the entity.
     * @param width   the width of the entity.
     * @param height  the height of the entity.
     * @param texture the texture identifier for the entity.
     * @param health  the initial health of the entity.
     * @param speed   the movement speed of the entity.
     * @param team    the team to which the entity belongs.
     */
    public Entity(double x, double y, double width, double height, String texture, double health, double speed, Team team) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.drawer = new Drawer(x, y, texture, (int)width, (int)height);
        this.freeze = false;
        state = State.ALIVE;
        this.health = health;
        this.speed = speed;
        this.team = team;
        this.id = Game.entityIdCounter++;
    }

    /**
     * Recreates the collision set using a new TreeSet.
     */
    public void createCollisionSet()
    {
        collisions = new TreeSet<>(new CollisionAreaComparator());
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setTransmitted(boolean transmitted) {
        this.transmitted = transmitted;
    }

    public boolean isTransmitted() {
        return transmitted;
    }

    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void rotate(double angle) {
        drawer.rotate(angle);
    }

    public void setMovable(boolean bool) {
        movable = bool;
    }

    public void setCollisionImmunity(boolean bool) {
        collisionImmunity = bool;
    }



    public void setFreeze(boolean freeze) { this.freeze = freeze; }

    public boolean isFreeze() { return freeze; }


    public void createNewDrawer()
    {
        drawer = new Drawer(x, y, texture, (int)width, (int)height);
    }

    /**
     * Updates the entity by calculating its velocity, updating its position,
     * and checking for collisions with blocks and other entities.
     *
     * @param level    the current game level.
     * @param entities a list of all entities in the game.
     */
    public void update(Level level, ArrayList<Entity> entities) {
        calculateVelocity();
        this.x += this.velocity * Math.cos(this.velocityAngle) * Game.deltaTime;
        this.y += this.velocity * Math.sin(this.velocityAngle) * Game.deltaTime;
        checkIntersectionWithBlocks(level);
        checkIntersectionWithEntities(entities);

        if(collisions.size() > 0){
            resolveCollision(level);
        }
    }

    /**
     * Checks for intersections with wall blocks in the level and populates the collision set.
     *
     * @param level the current game level.
     */
    protected void checkIntersectionWithBlocks(Level level) {
        collisions.clear();
        if(collisionImmunity) return;
        if (!collisionWithBlocks) return;

        int startX = (int) Math.floor(this.x);
        int endX = (int) Math.floor(this.x + this.width);
        int startY = (int) Math.floor(this.y);
        int endY = (int) Math.floor(this.y + this.height);

        for (int blockX = startX; blockX <= endX; blockX++) {
            for (int blockY = startY; blockY <= endY; blockY++) {
                if (level.isWall(blockX, blockY)) {
                    double x_left = Math.max(this.x, blockX);
                    double y_top = Math.max(this.y, blockY);
                    double x_right = Math.min(this.x + this.width, blockX + 1);
                    double y_bottom = Math.min(this.y + this.height, blockY + 1);

                    double intersectionWidth = x_right - x_left;
                    double intersectionHeight = y_bottom - y_top;

                    if (intersectionWidth > 0 && intersectionHeight > 0) {
                        double dx = (blockX + 0.5) - (this.x + this.width / 2);
                        double dy = (blockY + 0.5) - (this.y + this.height / 2);
                        if (dx > 0) intersectionWidth = -intersectionWidth;
                        if (dy > 0) intersectionHeight = -intersectionHeight;

                        collisions.add(new Collision(
                                x_left,
                                y_top,
                                intersectionWidth,
                                intersectionHeight,
                                Collision.CollisionType.BLOCK
                        ));
                    }
                }
            }
        }
    }

    /**
     * Checks for intersections with other entities and adds any detected collisions to the collision set.
     *
     * @param entities list of all entities in the game.
     */
    protected void checkIntersectionWithEntities(ArrayList<Entity> entities){
        if(collisionImmunity) return;
        for(Entity entity : entities){
            if(entity == this) continue;
            double x_left = Math.max(this.x, entity.getX());
            double y_top = Math.max(this.y, entity.getY());
            double x_right = Math.min(this.x + this.width, entity.getX() + entity.getWidth());
            double y_bottom = Math.min(this.y + this.height, entity.getY() + entity.getHeight());
            if(x_right - x_left > 0 && y_bottom - y_top > 0){
                double IntersectionWidth = x_right - x_left;
                double IntersectionHeight = y_bottom - y_top;
                if(entity.x > this.x) IntersectionWidth = -IntersectionWidth;
                if(entity.x > this.y) IntersectionHeight = -IntersectionHeight;
                Collision collision;
                if(entity.movable) collision = new Collision(x_left, y_top, IntersectionWidth, IntersectionHeight, Collision.CollisionType.ENTITY, entity);
                else collision = new Collision(x_left, y_top, IntersectionWidth, IntersectionHeight, Collision.CollisionType.UNMOVABLE, entity);
                collisions.add(collision);
            }
        }
    }

    /**
     * Resolves collisions by adjusting the entity's position based on collision data.
     *
     * @param level the current game level.
     */
    protected void resolveCollision(Level level) {
        double xChange = 0;
        double yChange = 0;
        Map<Double, Boolean> resolvedX = new HashMap<>();
        Map<Double, Boolean> resolvedY = new HashMap<>();
        for (Collision collision : collisions) {

            if(!resolvedX.containsKey(collision.getX()) && !resolvedY.containsKey(collision.getY())) {

                if(Collision.CollisionType.BLOCK.equals(collision.getType())) {
                    if (Math.abs(collision.getWidth()) < Math.abs(collision.getHeight())) {
                        this.x += collision.getWidth() - xChange;
                        resolvedX.put(collision.getX(), true);
                    } else {
                        this.y += collision.getHeight() - yChange;
                        resolvedY.put(collision.getY(), true);
                    }
                }
                if(Collision.CollisionType.ENTITY.equals(collision.getType())) {
                    if(!movable) return;
                    double pushingPower = collision.getEntity().getPushingPower();
                    double x_change = Math.abs(collision.getWidth()) * pushingPower;
                    double y_change = Math.abs(collision.getHeight()) * pushingPower;
                    x_change = x_change>0.01 ? x_change/100 : x_change;
                    y_change = y_change>0.01 ? y_change/100 : y_change;
                    x_change = collision.getX() > this.x ? -x_change : x_change;
                    y_change = collision.getY() > this.y ? -y_change : y_change;
                    this.x += x_change;
                    xChange += x_change;

                    this.y += y_change;
                    yChange += y_change;
                }
            }
        }
    }

    /**
     * Marks the entity for destruction.
     *
     * <p>
     * If in a multiplayer host scenario, ensures the entity is no longer transmitted.
     * </p>
     */
    public void destroy() {
        if(Game.multiplayer && Game.host)
        {
            transmitted = false;
        }
        this.state = State.TO_DESTROY;
    }

    public State getState() {
        return state;
    }

    public String getTexture() {
        return texture;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void calculateVelocity() {};


    public void setDirection(Direction directionX) {
        this.direction = directionX;
    }

    public Direction getDirection() {
        return  this.direction;
    }

    public SortedSet<Collision> getCollisions() {
        return collisions;
    }

    public void subtractHealth(double damage) {
        this.health -= damage;
        if(this.health <= 0) {
            this.state = State.TO_DESTROY;
        }
    }

    public double getHealth() {
        return this.health;
    }
    public void setHealth(double health) {
        this.health = health;
    }
    public double getPushingPower() {
        return pushingPower;
    }
    public Team getTeam() {
        return team;
    }

    public void setY(int i) {
        this.y = i;
    }

    public void setX(int i) {
        this.x = i;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
