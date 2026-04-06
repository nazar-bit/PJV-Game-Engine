package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Level;
import cz.cvut.fel.pjv.MouseClick;
import cz.cvut.fel.pjv.ShareSound;
import cz.cvut.fel.pjv.managers.HudManager;
import cz.cvut.fel.pjv.weapons.*;
import cz.cvut.fel.pjv.weapons.weaponInstances.*;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The Player class represents a controllable player entity in the game.
 *
 * <p>
 * It extends the base Entity class and provides additional functionality related to player
 * movement, inertia handling, weapons management, health updates, interactions with the HUD,
 * and response to death.
 * </p>
 */
public class Player extends Entity {
    private static final Logger logger = Logger.getLogger("Player");

    private double baseVelocity;
    private double startingInertia;
    private double endInertia;
    private transient Entity interactionTarget;
    private double inertiaDecayRate;
    private double pickupTimestamp = 0;
    private double pickupCooldown = 100_000_000;
    private Direction lastDir;

    private ArrayList<Weapon> weapons = new ArrayList<>(3);
    private int selectedWeapon = 0;  //<0;2>

    public transient HudManager hudManager;
    public TransmittedPlayer transmittedPlayer;

    /**
     * Constructs a new Player entity at the specified position with defined dimensions.
     *
     * @param x      the x-coordinate of the player.
     * @param y      the y-coordinate of the player.
     * @param width  the width of the player.
     * @param height the height of the player.
     */
    public Player(double x, double y, double width, double height) {

        super(x, y, width, height, "player", 10000, 7, Team.Players);
        this.baseVelocity = 7;
        this.startingInertia = 7;
        this.inertiaDecayRate = 30;
        this.endInertia = 0;
        this.direction = Direction.NONE;
        this.lastDir = Direction.NONE;

        this.weapons.add(new M16());
        this.weapons.add(new AWP());
        this.weapons.add(new Bat());

        this.hudManager = new HudManager(this);

        transmittedPlayer = new TransmittedPlayer(x, y, 0, 0, "player", 50, 7, Team.Players);
    }

    /**
     * Updates the player's state including movement, collision detection, weapon updates,
     * interactions, and HUD display.
     *
     * @param level    the current game level.
     * @param entities list of all entities in the game.
     */
    @Override
    public void update(Level level, ArrayList<Entity> entities)
    {
        if(state == State.DEAD)  return;
        calculateVelocity();
        this.x += this.velocity * Math.cos(this.velocityAngle) * Game.deltaTime;
        this.y += this.velocity * Math.sin(this.velocityAngle) * Game.deltaTime;
        transmittedPlayer.setX(x);
        transmittedPlayer.setY(y);
        checkIntersectionWithBlocks(level);
        checkIntersectionWithEntities(entities);
        if(!collisions.isEmpty()){
            resolveCollision(level);
        }

        updateWeapons();
        checkInteractables(entities);
        Platform.runLater(hudManager::updateKeyTip);
    }

    /**
     * Calculates the player's velocity based on movement direction and inertia.
     *
     * <p>
     * When moving, the player's inertia decays and acceleration is applied. When the player
     * stops moving, inertia slows the player gradually.
     * </p>
     */
    @Override
    public void calculateVelocity(){
        if (this.direction != Direction.NONE) {
            lastDir = direction;
            velocityAngle = direction.getAngle();
            startingInertia = calculateInertia(startingInertia);
            velocity = baseVelocity - startingInertia;
            endInertia = velocity;
        } else {
            velocityAngle = lastDir.getAngle();
            endInertia = calculateInertia(endInertia);
            velocity = endInertia;
            startingInertia = baseVelocity - velocity;
        }
    }

    /**
     * Checks for nearby interactable entities and sets the closest one as the current interaction target.
     *
     * @param entities a list of all active entities in the game.
     */
    private void checkInteractables(ArrayList<Entity> entities) {
        double smallestDistance = Double.MAX_VALUE;
        boolean existsInteraction = false;
        for(Entity entity : entities) {
            if(entity instanceof Interactable) {
                double distance = Math.hypot(Math.abs(entity.getX() - entity.getWidth()/2 - (this.x - this.width/2)),Math.abs( entity.getY() - entity.getHeight()/2 - (this.y - this.height/2)));
                if(distance < ((Interactable) entity).getInteractionDistance()) {
                    existsInteraction = true;
                    if(distance < smallestDistance) {
                        interactionTarget = entity;
                        smallestDistance = distance;
                    }
                }
            }
        }
        if(!existsInteraction) {
            interactionTarget = null;
        }
    }

    /**
     * Calculates and updates the inertia value for the player.
     *
     * <p>
     * Inertia is reduced over time based on the decay rate and deltaTime. When inertia reaches
     * zero, it no longer contributes to movement.
     * </p>
     *
     * @param inertia the current inertia value.
     * @return the updated inertia value.
     */
    private double calculateInertia(double inertia)
    {
        if(inertia-inertiaDecayRate*Game.deltaTime > 0){
            inertia -= inertiaDecayRate*Game.deltaTime;
        }
        else{
            inertia = 0;
        }
        return inertia;
    }

    /**
     * Updates all weapons in the player's inventory.
     */
    private void updateWeapons()
    {
        for(Weapon weapon : this.weapons)
        {
            weapon.update();
        }
    }

    /**
     * Fires the currently selected weapon towards the target coordinates.
     *
     * <p>
     * The firing behaviour adjusts based on the weapon's fire mode.
     * </p>
     *
     * @param targetX the x-coordinate of the firing target.
     * @param targetY the y-coordinate of the firing target.
     */
    public void shoot(double targetX, double targetY)
    {
        Weapon weaponInHand = this.weapons.get(this.selectedWeapon);
        switch(weaponInHand.getFireMode())
        {
            case MANUAL:
                Game.inputHandler.pressedMouseButtons.put(MouseButton.PRIMARY, new MouseClick(false, targetX, targetY));
                weaponInHand.shoot(targetX, targetY, x + width/2, y + height/2, team);
                break;

            case AUTOMATIC:
                weaponInHand.shoot(targetX, targetY, x + width/2, y + height/2, team);
                break;

            case MELEE:
                Game.inputHandler.pressedMouseButtons.put(MouseButton.PRIMARY, new MouseClick(false, targetX, targetY));
                weaponInHand.shoot(targetX, targetY, this.x, this.y, team);
                break;

            default:
                Game.inputHandler.pressedMouseButtons.put(MouseButton.PRIMARY, new MouseClick(false, targetX, targetY));
                weaponInHand.shoot(targetX, targetY, this.x, this.y, team);
        }
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public int getSelectedWeapon(){
        return this.selectedWeapon;
    }

    /**
     * Sets the selected weapon using the given index.
     *
     * @param newWeapon the new weapon's slot index (valid index: 0 to weapons list size).
     * @throws RuntimeException if the index is out of bounds.
     */
    public void setSelectedWeapon(int newWeapon)
    {
        if(newWeapon < 0 || newWeapon > this.weapons.size())
        {
            logger.warning("Invalid weapon index: " + newWeapon + ". Valid range is 0-" + this.weapons.size());
            throw new RuntimeException("Wrong Index For Weapon" + newWeapon);
        } else {
            this.selectedWeapon = newWeapon;
        }
    }

    /**
     * Checks whether a given weapon belongs to the player.
     *
     * @param weapon the weapon to check.
     * @return true if the player possesses the weapon, false otherwise.
     */
    public boolean weaponBelongToPlayer(Weapon weapon)
    {
        for(Weapon myWeapon : weapons)
        {
            if(myWeapon.equals(weapon))  return true;
        }
        return false;
    }


    public void changeSelectedWeapon(Weapon weapon)
    {
        weapons.set(selectedWeapon, weapon);
    }


    public void removeSelectedWeapon()
    {
        weapons.set(selectedWeapon, new BlankWeapon());
    }


    public Weapon getWeaponInHands()
    {
        return this.weapons.get(this.selectedWeapon);
    }

    /**
     * Retrieves a weapon from the player's inventory by index.
     *
     * @param index the index of the weapon.
     * @return the weapon if found; otherwise, a blank weapon.
     */
    public Weapon getWeapon(int index)
    {
        if(index < 0 || index >= this.weapons.size())
            return new BlankWeapon();
        else{
            return this.weapons.get(index);
        }
    }

    /**
     * Subtracts health from the player.
     *
     * <p>
     * If damage is applied (hpChange > 0), a sound is played. It also synchronizes health
     * with the transmitted player and handles death if health falls below zero.
     * </p>
     *
     * @param hpChange the amount of health change (positive values indicate damage).
     */
    @Override
    public void subtractHealth(double hpChange) {
        if(hpChange > 0) {
            Game.soundManager.playSound("/sounds/playerHit");
        }

        this.health -= hpChange;
        transmittedPlayer.health -= hpChange;
        if (this.health< 0) {
            this.health = 0;
            hudManager.updateHP(0);
            this.state = State.DEAD;
            transmittedPlayer.health = 0;
            transmittedPlayer.state = State.DEAD;
            onDeath();
            return;
        }
        Platform.runLater(()->{
            hudManager.updateHP((int)this.health);
        });
    }

    /**
     * Performs actions upon the player's death.
     *
     * <p>
     * In singleplayer mode, the game is paused and switches to a death screen. In multiplayer,
     *  the camera may be assigned to a friend if available.
     * </p>
     */
    private void onDeath()
    {
        if(!Game.multiplayer)
        {
            Game.paused = true;
            Game.currentStage.setScene(Game.deathScreenScene);
        }
        else{
            if(Game.host)
            {
                TransmittedPlayer friend = (TransmittedPlayer) Game.players.get(1);
                if(friend.state == State.DEAD || friend.state == State.DESTROYED || friend.state == State.TO_DESTROY)
                {
                    Game.paused = true;
                    Game.currentStage.setScene(Game.deathScreenScene);
                }
                else{
                    Game.camera.setObservedEntity(friend);
                }
            }
            else{
                TransmittedPlayer friend = null;
                for(Entity entity : Game.entities)
                {
                    if(entity instanceof TransmittedPlayer){
                        friend = (TransmittedPlayer) entity;
                        break;
                    }
                }
                if(friend == null)
                {
                    logger.warning("Friend was not found");
                    return;
                }
                if(friend.state == State.DEAD || friend.state == State.DESTROYED || friend.state == State.TO_DESTROY)
                {
                    Game.paused = true;
                    Game.currentStage.setScene(Game.deathScreenScene);
                }
                else{
                    Game.camera.setObservedEntity(friend);
                }
            }
        }
        hudManager.clearHud();
        weapons.clear();
    }


    public Entity getInteractionTarget() {
        return interactionTarget;
    }

    public double getPickupCooldown() {
        return pickupCooldown;
    }
    public double getPickupTimestamp() {
        return pickupTimestamp;
    }
    public void setPickupTimestamp(double pickupTimestamp) {
        this.pickupTimestamp = pickupTimestamp;
    }


}
