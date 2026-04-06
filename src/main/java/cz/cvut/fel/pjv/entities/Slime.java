package cz.cvut.fel.pjv.entities;
import cz.cvut.fel.pjv.weapons.weaponInstances.Bat;

/**
 * The Slime class represents a melee enemy that follows a defined behavior.
 *
 * <p>
 * This enemy uses a Bat as its weapon and exhibits specific attributes such as health,
 * speed, damage, shooting cooldown, and pushing power. The enemy's behavior is defined as
 * FOLLOW_THEN_GO_SPAWN.
 * </p>
 */
public class Slime extends Enemy {

    /**
     * Constructs a new Slime with the specified position and dimensions.
     *
     * @param x      the x-coordinate of the Slime.
     * @param y      the y-coordinate of the Slime.
     * @param width  the width of the Slime.
     * @param height the height of the Slime.
     */
    public Slime(double x, double y, double width, double height) {
        super(x, y, width, height, "slime", 50, 6.6, 1, 1000, EntityBehaviour.FOLLOW_THEN_GO_SPAWN,
                new Bat());
        this.pushingPower = 2;
    }

    /**
     * Constructs a new Slime with the specified position and default dimensions (1, 1).
     *
     * @param x the x-coordinate of the Slime.
     * @param y the y-coordinate of the Slime.
     */
    public Slime(double x, double y) {
        super(x, y, 1, 1, "slime", 50, 6.6, 1, 1000, EntityBehaviour.FOLLOW_THEN_GO_SPAWN,
                new Bat());
        this.pushingPower = 2;
    }


}
