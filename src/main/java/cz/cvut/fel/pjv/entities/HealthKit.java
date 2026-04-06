package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.Level;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The HealthKit class represents a healing item in the game.
 *
 * <p>
 * When a player collides with a HealthKit, the player's health is increased by
 * the healing amount provided by the kit, and the HealthKit is then destroyed.
 * HealthKit is immovable.
 * </p>
 */
public class HealthKit extends Entity implements Serializable {


    private final double healingPower = 40;

    /**
     * Constructs a new HealthKit at the specified position.
     *
     * @param x the x-coordinate where the HealthKit is placed.
     * @param y the y-coordinate where the HealthKit is placed.
     */
    public HealthKit(double x, double y) {
        super(x, y, 0.7, 0.7, "first_aid");

        movable = false;
        pushingPower = 0;
    }

    /**
     * Resolves collisions for the HealthKit.
     *
     * <p>
     * In addition to the default block collision handling, if a collision occurs with a player,
     * the player's health is increased by the healing power, and the HealthKit is destroyed.
     * </p>
     *
     * @param level the current game level.
     */
    @Override
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
                    if(collision.getEntity().getTeam() == Team.Players) {
                        collision.getEntity().subtractHealth(-healingPower);
                        destroy();
                    }
                }
            }
        }
    }



}
