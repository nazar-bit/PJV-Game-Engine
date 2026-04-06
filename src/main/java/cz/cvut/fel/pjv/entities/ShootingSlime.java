package cz.cvut.fel.pjv.entities;
import cz.cvut.fel.pjv.weapons.weaponInstances.Pistol;

/**
 * The ShootingSlime class represents an enemy that uses ranged attacks.
 *
 * <p>
 * This enemy is configured with specific attributes such as health, speed, damage, cooldown,
 * behaviour, and a pistol as its weapon. It also has an increased pushing power.
 * </p>
 */
public class ShootingSlime extends Enemy {

    /**
     * Constructs a ShootingSlime with the specified position and dimensions.
     *
     * @param x      the x-coordinate of the ShootingSlime.
     * @param y      the y-coordinate of the ShootingSlime.
     * @param width  the width of the ShootingSlime.
     * @param height the height of the ShootingSlime.
     */
    public ShootingSlime(double x, double y, double width, double height) {
        super(x, y, width, height, "shootingSlime", 50, 5, 1, 1000, EntityBehaviour.FOLLOW_THEN_GO_SPAWN,
                new Pistol());
        this.pushingPower = 2;
    }

    /**
     * Constructs a ShootingSlime with the specified position and default dimensions (1, 1).
     *
     * @param x the x-coordinate of the ShootingSlime.
     * @param y the y-coordinate of the ShootingSlime.
     */
    public ShootingSlime(double x, double y) {
        super(x, y, 1, 1, "shootingSlime", 50, 5, 1, 1000, EntityBehaviour.FOLLOW_THEN_GO_SPAWN,
                new Pistol());
        this.pushingPower = 2;
    }


}

