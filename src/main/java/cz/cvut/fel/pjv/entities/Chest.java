package cz.cvut.fel.pjv.entities;


import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Item;
import cz.cvut.fel.pjv.Level;

import java.util.logging.Logger;

/**
 * The Chest class represents an interactable container that can store items.
 *
 * <p>
 * It extends the Entity class and implements the Interactable interface so that players can interact with it.
 * A chest has a fixed size inventory and cannot be moved once placed.
 * </p>
 */
public class Chest extends Entity implements Interactable {
    private static final Logger logger = Logger.getLogger("Chest");
    protected int size;
    protected int fulfilment = 0;
    protected Item[] inventory;

    /**
     * Constructs a new Chest at the specified position.
     *
     * <p>
     * The chest is initialized with a default size of 5 slots and is set to be immovable.
     * </p>
     *
     * @param x the x-coordinate of the chest in the game world.
     * @param y the y-coordinate of the chest in the game world.
     */
    public Chest(double x, double y) {
        super(x, y, 1, 1, "chest");
        this.size = 5;
        this.inventory = new Item[size];
        this.movable = false;
    }


    /**
     * Resolves collisions with the chest.
     *
     * <p>
     * The chest does not perform any specific collision resolution.
     * </p>
     *
     * @param level the current game level.
     */
    @Override
    protected void resolveCollision(Level level) {}


    /**
     * Sets the chest's inventory to the provided array of items.
     *
     * <p>
     * The provided inventory array must not exceed the chest's capacity.
     * The chest's current inventory is cleared before the new items are copied.
     * </p>
     *
     * @param inventory the array of items to set as the chest's inventory.
     * @throws IndexOutOfBoundsException if the provided inventory is larger than the chest's capacity.
     */
    public void setInventory(Item[] inventory) {
        if (inventory.length > this.inventory.length) {
            logger.warning("Inventory you are trying to set is too big!");
            throw new IndexOutOfBoundsException("Inventory you are trying to set is too big!");
        }

        clearInventory();
        System.arraycopy(inventory, 0, this.inventory, 0, inventory.length);
    }


    public void clearInventory() {
        inventory = new Item[size];
        fulfilment = 0;
    }

    public Item[] getInventory() {
        return inventory;
    }

    public void addItemToInventory(Item item) {
        inventory[fulfilment++] = item;
    }

    public int getSize() {
        return size;
    }

    public int getFulfilment() {
        return fulfilment;
    }

    @Override
    public void interact() {
        logger.info("Interacting with chest");
    }

    @Override
    public double getInteractionDistance() {
        return 2;
    }

    @Override
    public String getInteractionText() {
        return "open chest";
    }
}
