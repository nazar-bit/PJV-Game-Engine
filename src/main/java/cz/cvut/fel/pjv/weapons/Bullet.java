package cz.cvut.fel.pjv.weapons;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.entities.Team;
import cz.cvut.fel.pjv.Level;
import cz.cvut.fel.pjv.entities.Entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Bullet class represents a projectile in the game, extending the base Projectile class.
 *
 * <div>
 * This class defines properties specific to a bullet, including:
 * <ul>
 *   <li><b>damage</b>: The amount of damage the bullet deals upon collision.</li>
 *   <li><b>bulletType</b>: The type of the bullet, which can indicate different behaviors or effects.</li>
 *   <li><b>timeToLive (TTL)</b>: The lifespan of the bullet in milliseconds. The bullet will destroy itself
 *       once the TTL expires.</li>
 * </ul>
 * </div>
 *
 * <p>The Bullet class is responsible for updating its state over time and handling interactions with other entities.
 * The update method decreases the TTL based on the game’s deltaTime and eventually destroys the bullet if TTL reaches zero.
 * The resolveCollision method handles collisions by applying the appropriate damage using a collision resolver mechanism.</p>
 */
public class Bullet extends Projectile {


    private final double damage;
    private final BulletType bulletType;
    /// Time to live is in milliseconds
    private double timeToLive;


    /**
     * Constructs a new Bullet with the specified parameters.
     *
     * @param x           the initial x-coordinate
     * @param y           the initial y-coordinate
     * @param velocity    the speed of the bullet
     * @param velocityAngle the angle at which the bullet is fired
     * @param damage      the damage inflicted on collision
     * @param bulletType  the type of the bullet
     * @param width       the width of the bullet
     * @param height      the height of the bullet
     * @param texture     the texture identifier for the bullet's image
     * @param team        the team to which the bullet belongs, used for collision filtering
     */
    public Bullet(double x, double y, double velocity, double velocityAngle, double damage, BulletType bulletType, double width,
                  double height, String texture, Team team) {
        super(x, y, width, height, texture, true, true, true, team.getCollisionExempt());
        this.velocity = velocity;
        this.velocityAngle = velocityAngle;
        this.damage = damage;
        this.bulletType = bulletType;
        this.timeToLive = 2000;
        movable = false;
    }


    /** Updates TTL and position
     * @param level Level
     * @param entities all Entities
     */
    public void update(Level level, ArrayList<Entity> entities) {
        this.timeToLive -= Game.deltaTime * 1000;
        if(timeToLive <= 0) {
            this.destroy();
        }
        super.update(level, entities);
    }


    @Override
    protected void resolveCollision(Level level) {
        collisionResolver(damage);
    }
}
