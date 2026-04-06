package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.DeleteRequest;
import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.weapons.Weapon;

import static cz.cvut.fel.pjv.Game.*;


/**
 * The WeaponEntity class represents a special entity used for in-game weapons that are
 * containing weapon instances.
 *
 * <p>
 * It implements the Interactable interface, allowing players to interact with it. This entity is not movable.
 * WeaponEntity is used in two scenarios:
 * - Creating new weapons in the game.
 * - Dropping weapons by players.
 * </p>
 */
public class WeaponEntity extends Entity implements Interactable {

    public static final double WIDTH = 1;
    public static final double HEIGHT = 1;

    protected Weapon weapon;

    /**
     * Constructor used for creating new weapon entities.
     *
     * @param x          the x-coordinate where the weapon entity is created.
     * @param y          the y-coordinate where the weapon entity is created.
     * @param texture    the texture identifier for the weapon entity.
     * @param weaponName the name used to create the weapon instance.
     */
    public WeaponEntity(double x, double y, String texture, String weaponName)
    {

        super(x, y, WIDTH, HEIGHT, texture);
        this.weapon = weaponManager.createWeapon(weaponName);
        this.movable = false;
        Game.addEntity(this);
    }

    /**
     * Constructor used for dropping weapons. The entity is created at the player's current location.
     *
     * @param texture the texture identifier for the weapon entity.
     * @param weapon  the weapon instance to be dropped.
     */
    public WeaponEntity(String texture, Weapon weapon)
    {
        super(player.getX(), player.getY(), WIDTH, HEIGHT, texture);
        this.weapon = weapon;
        this.movable = false;
        Game.addEntity(this);
    }
    /**
     * Constructor that creates a weapon entity at a specified location using the weapon's name.
     *
     * @param x          the x-coordinate where the weapon entity is created.
     * @param y          the y-coordinate where the weapon entity is created.
     * @param weaponName the name used to create and configure the weapon instance.
     */
    public WeaponEntity(double x, double y, String weaponName)
    {
        super(x, y, WIDTH, HEIGHT, weaponManager.createWeapon(weaponName).getTexture());
        this.weapon = weaponManager.createWeapon(weaponName);
        this.movable = false;
        Game.addEntity(this);
    }

    /**
     * Processes interaction with the weapon entity.
     *
     * <p>
     * When a player interacts with the weapon entity:
     * - It checks if the entity is in a state that prevents interaction.
     * - If eligible, it drops the current weapon (if any), updates HUD and player's active weapon,
     *   and marks the entity for removal.
     * - In multiplayer mode, a delete request is exported for proper synchronization.
     * </p>
     */
    @Override
    public void interact() {
        if(state == State.TO_DESTROY || state == State.DESTROYED || System.nanoTime() - player.getPickupTimestamp() < player.getPickupCooldown()) return;
        Game.inputHandler.dropWeapon();
        player.setPickupTimestamp(System.nanoTime());
        player.hudManager.setNewIcon(player.getSelectedWeapon(), weapon.getIcon());
        player.changeSelectedWeapon(weapon);
        state = State.TO_DESTROY;
        if(!Game.host){
            if(multiplayer) exportEntities.add(new DeleteRequest(id));
        }
        player.hudManager.updateAmmoLabel();

    }
    @Override
    public double getInteractionDistance() { return 1;}
    @Override
    public String getInteractionText() {return "Pick Up";}

    public String getWeaponName() {
        return weapon.getTexture();
    }
}
