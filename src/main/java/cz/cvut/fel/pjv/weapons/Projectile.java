package cz.cvut.fel.pjv.weapons;

import cz.cvut.fel.pjv.entities.Collision;
import cz.cvut.fel.pjv.entities.Entity;
import cz.cvut.fel.pjv.entities.Player;
import cz.cvut.fel.pjv.entities.TransmittedPlayer;

import java.util.ArrayList;


/**
 * The Projectile class represents any projectile in the game that affects entities and environment through collisions.
 *
 * <p>This class extends Entity and provides the basic structure and behaviors for projectiles, including properties
 * related to collisions and damage application. It includes a utility method for resolving collisions with both blocks
 * and entities.</p>
 */

public class Projectile extends Entity {

    protected boolean destroyedByBlocks;
    protected boolean collidesWithEntities;
    protected boolean destroyAfterEntityCollision;
    protected ArrayList<Class<?>> collisionExempt;

    /**
     * Constructs a new Projectile with the provided parameters.
     *
     * @param x                          the x-coordinate of the projectile.
     * @param y                          the y-coordinate of the projectile.
     * @param width                      the width of the projectile.
     * @param height                     the height of the projectile.
     * @param texture                    the texture identifier for rendering the projectile.
     * @param destroyedByBlocks          determines if the projectile should be destroyed on collision with blocks.
     * @param collidesWithEntities       indicates if the projectile should interact with other entities.
     * @param destroyAfterEntityCollision if true, the projectile destroys itself after colliding with an entity.
     * @param collisionExempt            a list of entity classes to be exempted from collision detection.
     */
    public Projectile(double x, double y, double width, double height, String texture, boolean destroyedByBlocks,
                      boolean collidesWithEntities, boolean destroyAfterEntityCollision, ArrayList<Class<?>> collisionExempt)
    {
        super(x, y, width, height, texture);

        this.destroyedByBlocks = destroyedByBlocks;
        this.collidesWithEntities = collidesWithEntities;
        this.destroyAfterEntityCollision = destroyAfterEntityCollision;
        this.collisionExempt = collisionExempt;
    }


    /// Ultimate Collision Resolver for projectiles
    /// Use inside @Override protected void resolveCollision(LevelManager levelManager)
    protected boolean collisionResolver(double damage)
    {
        boolean enemyHit = false;
        for (Collision collision : this.collisions) {
            /// Checks if collides with blocks and resolves this collision
            if (destroyedByBlocks && Collision.CollisionType.BLOCK.equals(collision.getType())) {
                this.destroy();
            }
            /// Checks if collides with entities and resolves this collision
            if (collidesWithEntities && Collision.CollisionType.ENTITY.equals(collision.getType())) {
                boolean exemptionFound = false;
                if(collision.getEntity() instanceof Projectile)  exemptionFound = true;
                if(exemptionFound) continue;
                /// Checks for collision exemptions - the objects with which no collision should occur
                for(Class<?> exemption : collisionExempt)
                {
                    if (exemption.isInstance(collision.getEntity()))   exemptionFound = true;
                }
                if(exemptionFound) continue;

                enemyHit = true;
                collision.getEntity().subtractHealth(damage);
                /// Checks whether to destroy itself after collision
                if(destroyAfterEntityCollision)    this.destroy();
            }
        }
        return enemyHit;
    }

}
