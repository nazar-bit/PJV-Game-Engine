package cz.cvut.fel.pjv.entities;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.ShareSound;
import cz.cvut.fel.pjv.levelGraph.LevelNode;
import cz.cvut.fel.pjv.Level;
import cz.cvut.fel.pjv.weapons.MeleeWeapon;
import cz.cvut.fel.pjv.weapons.Weapon;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.cvut.fel.pjv.Game.*;

/**
 * The Enemy class defines the common behavior and properties of all enemy entities.
 *
 * <p>
 * It extends the Entity class and provides logic for movement, decision-making,
 * and interactions with the player. Different enemy behaviors (such as following,
 * pursuing, preparing for attack, wandering, etc.) are implemented based on the enemy's
 * state and class (melee or range). This abstract class is meant to be specialized by
 * concrete enemy classes.
 * </p>
 */
public abstract class Enemy extends Entity {

    private final double ATTACK_PREPARATION_TIME = 20;

    protected boolean LOS = false;  ///Line of Sight
    protected double attackDamage;
    protected double attackCooldown;
    protected double attackCooldownTimeStamp = 0;
    protected double spawnX;
    protected double spawnY;
    double lastPlayerX = 0;
    double lastPlayerY = 0;
    protected boolean playerLost = true;
    protected EntityStates entityState = EntityStates.IDLE;
    protected EntityBehaviour behaviour;
    protected double sightRadius = 9;
    protected Weapon weapon;
    protected EnemyType enemyType;
    protected double attackReadiness = 0;


    protected transient List<LevelNode> path;


    /**
     * Constructs a new Enemy with the provided attributes.
     *
     * @param x             the initial x-coordinate of the enemy.
     * @param y             the initial y-coordinate of the enemy.
     * @param width         the width of the enemy.
     * @param height        the height of the enemy.
     * @param texture       the texture identifier for the enemy.
     * @param health        the starting health of the enemy.
     * @param speed         the movement speed of the enemy.
     * @param attackDamage  the damage inflicted per attack.
     * @param attackCooldown the interval between consecutive attacks.
     * @param behaviour     the behavior pattern of the enemy.
     * @param weapon        the weapon used by the enemy.
     */
    public Enemy(double x, double y, double width, double height, String texture, double health, double speed,
                 double attackDamage, double attackCooldown, EntityBehaviour behaviour, Weapon weapon) {
        super(x, y, width, height, texture, health, speed, Team.Enemies);
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.behaviour = behaviour;
        this.spawnX = x;
        this.spawnY = y;
        this.weapon = weapon;

        this.weapon.setMaxAmmo(Integer.MAX_VALUE);
        this.weapon.setFireRate(this.weapon.getFireRate()/2);
        if(!(this.weapon instanceof MeleeWeapon))  this.weapon.setDamage(this.weapon.getDamage()/4);


        if(behaviour == EntityBehaviour.FOLLOW_FOREVER) {
            entityState = EntityStates.FOLLOW_PLAYER;
        }

        if(weapon instanceof MeleeWeapon)
        {
            enemyType = EnemyType.MELEE;
        }
        else{
            enemyType = EnemyType.RANGE;
        }

        path = null;
    }


    /**
     * Updates the enemy's state, weapon, and decision-making processes.
     *
     * <p>
     * In addition to calling the super update method, it updates the weapon state,
     * checks for line-of-sight (LOS) conditions, updates its own state, and decides
     * on the next action such as movement or attack.
     * </p>
     *
     * @param level    the current game level.
     * @param entities list of all entities in the game.
     */
    @Override
    public void update(Level level, ArrayList<Entity> entities) {
        super.update(level, entities);
        weapon.update();
        checkLOS(level);
        checkState();
        decide();
    }

    /**
     * Determines and updates the current state of the enemy based on its behavior and player detection.
     *
     * <p>
     * This method transitions the enemy between states such as PURSUE, WONDER,
     * GO_TO_SPAWN, or IDLE based on factors like LOS and whether the player has been lost.
     * </p>
     */
    private void checkState()
    {
        if(behaviour == EntityBehaviour.FOLLOW_THEN_GO_SPAWN || behaviour == EntityBehaviour.FOLLOW_THEN_IDLE ||
                behaviour == EntityBehaviour.FOLLOW_THEN_WONDER) {
            if(entityState == EntityStates.PREPARE_FOR_ATTACK)  return;
            if (LOS && isInSight()) {
                entityState = EntityStates.PURSUE;
                playerLost = false;
            } else if (!playerLost && entityState == EntityStates.PURSUE) {
                path = levelGraph.findPath(new LevelNode((int) Math.rint(x), (int) Math.rint(y)), new LevelNode((int) Math.rint(lastPlayerX), (int) Math.rint(lastPlayerY)));
                entityState = EntityStates.PURSUE_TO_LAST_KNOWN_POS;
            } else if (playerLost) {
                if(behaviour == EntityBehaviour.FOLLOW_THEN_WONDER)  entityState = EntityStates.WONDER;
                else if(behaviour == EntityBehaviour.FOLLOW_THEN_GO_SPAWN && entityState != EntityStates.GO_TO_SPAWN) {
                    entityState = EntityStates.GO_TO_SPAWN;
                    path = levelGraph.findPath(new LevelNode((int) Math.rint(x), (int) Math.rint(y)), new LevelNode((int) Math.rint(spawnX), (int) Math.rint(spawnY)));
                }
                else if(behaviour == EntityBehaviour.FOLLOW_THEN_IDLE)  entityState = EntityStates.IDLE;
            }
        }
    }

    /**
     * Decides the enemy's next action based on its current state.
     *
     * <p>
     * The enemy may follow a path towards the player, prepare for an attack, or return to spawn.
     * This method handles state-specific behaviour, including deciding when to shoot and how to navigate the level.
     * </p>
     */
    private void decide()
    {
        if(entityState == EntityStates.FOLLOW_PLAYER) {
            path = levelGraph.findPath(new LevelNode((int) Math.rint(x), (int) Math.rint(y)), new LevelNode((int) Math.rint(lastPlayerX), (int) Math.rint(lastPlayerY)));
            if(path == null){
                entityState = EntityStates.IDLE;
                return;
            }
            followPath(path);
        }


        else if(entityState == EntityStates.PURSUE) {
            switch (enemyType)
            {
                case MELEE:
                    if (Math.hypot(lastPlayerX - x, lastPlayerY - y) < ((MeleeWeapon)weapon).getAttackRange())
                    {
                        if (weapon.getCooldown() <= 0) {
                            entityState = EntityStates.PREPARE_FOR_ATTACK;
                        }
                    }
                    break;

                case RANGE:
                    if(!weapon.isReloading()) {
                        entityState = EntityStates.PREPARE_FOR_ATTACK;
                    }
                    break;
            }

            if(enemyType != EnemyType.RANGE) {
                path = levelGraph.findPath(new LevelNode((int) Math.rint(x), (int) Math.rint(y)), new LevelNode((int) Math.rint(lastPlayerX), (int) Math.rint(lastPlayerY)));
                if (path == null) {
                    playerLost = true;
                    return;
                }
                followPath(path);
            }
        }


        else if(entityState == EntityStates.PREPARE_FOR_ATTACK) {
            attackReadiness += 100 * Game.deltaTime;
            velocity = 0;
            switch(enemyType)
            {
                case MELEE:
                    if(attackReadiness >= ATTACK_PREPARATION_TIME) {
                        Platform.runLater(() -> {
                            weapon.shoot(lastPlayerX, lastPlayerY, x, y, team);
                        });
                        entityState = EntityStates.PURSUE;
                        attackReadiness = 0;
                    }
                    break;

                case RANGE:
                    if(attackReadiness >= ATTACK_PREPARATION_TIME && LOS) {
                        Platform.runLater(() -> {
                            weapon.shoot(lastPlayerX+player.getWidth()/2, lastPlayerY+player.getHeight()/2, x+width/2, y+height/2, team);
                        });
                        if((weapon.getAmmoInMagazine() <= 0)) {
                            weapon.reload();
                            attackReadiness = 0;
                        }
                    }
                    else if(!LOS){
                        entityState = EntityStates.PURSUE;
                        attackReadiness = 0;
                    }
                    break;
            }

        }


        else if(entityState == EntityStates.PURSUE_TO_LAST_KNOWN_POS) {
            if(path == null){
                playerLost = true;
                return;
            }
            if(followPath(path) == 0){
                playerLost = true;
            }
        }

        //----After pursuit ended
        else if(entityState == EntityStates.GO_TO_SPAWN) {
            if(path == null){
                entityState = EntityStates.IDLE;
                return;
            }
            if(followPath(path) == 0) {
                entityState = EntityStates.IDLE;
            }
        }


        else if(entityState == EntityStates.IDLE) {
            velocity = 0;
        }
    }


    @Override
    public void calculateVelocity() {}

    /**
     * Resolves collisions encountered by the enemy with blocks and other entities.
     *
     * <p>
     * Adjusts the enemy's position in response to collisions to avoid overlapping objects.
     * For entity collisions (e.g., with the player), applies damage if the attack cooldown has passed.
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
                    double x_change = Math.abs(collision.getWidth());
                    double y_change = Math.abs(collision.getHeight());
                    x_change = x_change>0.01 ? x_change/100 : x_change;
                    y_change = y_change>0.01 ? y_change/100 : y_change;
                    x_change = collision.getX() > this.x ? -x_change : x_change;
                    y_change = collision.getY() > this.y ? -y_change : y_change;
                    this.x += x_change;
                    xChange += x_change;
                    this.y += y_change;
                    yChange += y_change;
                    if (collision.getEntity() instanceof Player) {
                        if (System.currentTimeMillis() - attackCooldownTimeStamp > attackCooldown) {
                            attackCooldownTimeStamp = System.currentTimeMillis();
                            (collision.getEntity()).subtractHealth(attackDamage);
                        }
                    }
                }
            }
        }
    }

    /**
     * Reduces the enemy's health by the specified damage amount.
     *
     * <p>
     * Plays a hit sound and exports a shared sound entity for multiplayer scenarios.
     * If health drops below or equal to zero, marks the enemy for destruction.
     * </p>
     *
     * @param damage the amount of damage to subtract.
     */
    @Override
    public void subtractHealth(double damage) {
        Game.soundManager.playSound("/sounds/enemyHit");
        if(multiplayer) exportEntities.add(new ShareSound("/sounds/enemyHit"));
        this.health -= damage;
        if(this.health <= 0) {
            this.state = State.TO_DESTROY;
        }
    }

    /**
     * Follows the provided path toward a target destination.
     *
     * <p>
     * The method calculates the required direction to move and updates the enemy's velocity and angle.
     * When a level node is reached within a small threshold, it is removed from the path.
     * </p>
     *
     * @param path the list of LevelNode objects defining the path.
     * @return the number of remaining nodes in the path.
     */
    private int followPath(List<LevelNode> path)
    {
        if(path == null) return 0;
        if(path.isEmpty())
        {
            velocity = 0;
            return 0;
        }

        if(path.get(path.size() - 1).getX() < x + 0.1 && path.get(path.size() - 1).getX() > x - 0.1 &&
                path.get(path.size() - 1).getY() < y + 0.1 && path.get(path.size() - 1).getY() > y - 0.1) {
            path.remove(path.size() - 1);
            return path.size();
        }


        double distanceX = path.get(path.size() - 1).getX() - x;
        double distanceY = path.get(path.size() - 1).getY() - y;
        velocityAngle = Math.atan2(distanceY, distanceX);
        velocity = speed;

        return path.size();
    }


    private boolean isInSight()
    {
        boolean inSight = false;
        for(Entity playerIterator : players)
        {
            if(Math.hypot(x - playerIterator.getX(), y - playerIterator.getY()) < sightRadius)
            {
                inSight = true;
                break;
            }
        }

        return inSight;
    }

    /**
     * Performs a line-of-sight (LOS) check for each player in the level.
     *
     * <p>
     * The method calculates the distance from the enemy to each player
     * and determines if obstacles (walls) block the LOS. The enemy's LOS flag is updated
     * according to the closest unobstructed distance.
     * </p>
     *
     * @param level the current game level used for wall detection.
     */
    private void checkLOS(Level level) {
        this.LOS = false;
        ArrayList<Double> distances = new ArrayList<>();
        for(Entity playerIterator : players) {
            if(playerIterator.state == State.DEAD)
            {
                distances.add(-1.0);
                continue;
            }
            boolean found = true;

            double dx = playerIterator.getX() - this.x;
            double dy = playerIterator.getY() - this.y;
            double distance = Math.hypot(dx, dy);

            double stepX = dx / distance;
            double stepY = dy / distance;

            double x = this.x;
            double y = this.y;

            for (int i = 0; i < distance; i++) {
                int tileX = (int) Math.rint(x);
                int tileY = (int) Math.rint(y);

                if (level.isWall(tileX, tileY)) {
                    found = false;
                    break;
                }
                x += stepX;
                y += stepY;
            }
            if(!found) distances.add(-1.0);
            else distances.add(distance);
        }

        double lowestDistance = Double.MAX_VALUE;
        for(int i = 0; i < distances.size(); i++) {
            if(distances.get(i) < 0) continue;
            else{
                if(distances.get(i) < lowestDistance) {
                    lowestDistance = distances.get(i);
                    lastPlayerX = players.get(i).getX();
                    lastPlayerY = players.get(i).getY();
                }
                this.LOS = true;
            }
        }
    }


    public Weapon getWeapon() {
        return weapon;
    }


    public void setWeapon(Weapon weapon)
    {
        this.weapon = weapon;
    }


    public boolean weaponBelongsToEnemy(Weapon weapon)
    {
        if(this.weapon.equals(weapon)) return true;
        return false;
    }



}
