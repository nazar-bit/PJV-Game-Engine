package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.Game;

/**
 * The TransmittedPlayer class represents a special version of the player used for
 * multiplayer synchronization.
 *
 * <p>
 * This entity is invisible, non-movable, and immune to collisions. It is added to the
 * game automatically upon creation of the player.
 * </p>
 */
public class TransmittedPlayer extends Entity {

    /**
     * Constructs a new TransmittedPlayer with the specified properties.
     *
     * <p>
     * The TransmittedPlayer is configured to:
     * - Be invisible (drawer.setVisible(false)).
     * - Have collision immunity enabled.
     * - Be non-movable.
     * - Have no pushing power.
     * - Not interact with blocks for collisions.
     * After creation, it is automatically added to the game.
     * </p>
     *
     * @param x       the x-coordinate of the transmitted player.
     * @param y       the y-coordinate of the transmitted player.
     * @param width   the width of the entity.
     * @param height  the height of the entity.
     * @param texture the texture identifier.
     * @param health  the starting health value.
     * @param speed   the movement speed (though this entity does not move).
     * @param team    the team to which this entity belongs.
     */
    public TransmittedPlayer(double x, double y, double width, double height, String texture, double health, double speed, Team team) {
            super(x, y, width, height, texture, health, speed, team);

            drawer.setVisible(false);
            collisionImmunity = true;
            movable = false;
            pushingPower = 0;
            collisionWithBlocks = false;
            Game.addEntity(this);
        }

}
