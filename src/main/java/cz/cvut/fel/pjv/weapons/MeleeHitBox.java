package cz.cvut.fel.pjv.weapons;
import cz.cvut.fel.pjv.Drawer;
import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.entities.Entity;
import cz.cvut.fel.pjv.entities.Team;
import cz.cvut.fel.pjv.Level;

import java.util.ArrayList;



/**
 * The MeleeHitBox class represents the area of effect for melee attacks in the game.
 *
 * <p>This class extends the Projectile class and is used to detect and resolve collisions
 * for close-range melee attacks. It handles the lifespan of the hitbox (TTL - Time To Live) and the damage
 * applied to enemy entities when a collision occurs.</p>
 */
public class MeleeHitBox extends Projectile {
    public static double MAX_TTL = 150;

    protected boolean enemyHit = false;
    protected double damage;
    protected double TTL = MAX_TTL;
    public double opacity = 0.7;

    /**
     * Constructs a new MeleeHitBox with the provided parameters.
     *
     * @param x      the x-coordinate of the hitbox's position.
     * @param y      the y-coordinate of the hitbox's position.
     * @param width  the width of the hitbox.
     * @param height the height of the hitbox.
     * @param damage the damage that is applied when a collision with an enemy is detected.
     * @param team   the team which the attacking entity belongs to; used for collision filtering.
     */
    public MeleeHitBox(double x, double y, double width, double height, double damage, Team team) {
        super(x, y, width, height, "meleeHitBox", false, true, false,
                team.getCollisionExempt());

        this.damage = damage;
        this.collisionWithBlocks = false;
        drawer.getBlockImageView().setOpacity(opacity);

        if(team == Team.Enemies){
            this.drawer = new Drawer(x, y, "meleeHitBoxEnemy", width, height);
        }

        movable = false;
    }


    @Override
    protected void resolveCollision(Level level) {
        if(enemyHit) return;
        enemyHit = collisionResolver(damage);
    }

    /**
     * Updates the state of the mêlée hitbox.
     *
     * <p>This method decreases the Time To Live (TTL) based on the game's delta time.
     * Once the TTL runs out, the hitbox is destroyed. It also delegates additional updates
     * such as collision detection to the superclass.</p>
     *
     * @param level    the current game level.
     * @param entities a list containing all active entities in the game level.
     */
    public void update(Level level, ArrayList<Entity> entities) {
        this.TTL -= Game.deltaTime * 1000;
        if(TTL <= 0) {
            this.destroy();
        }
        super.update(level, entities);
    }
}
