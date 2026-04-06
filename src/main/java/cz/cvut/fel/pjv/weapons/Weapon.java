package cz.cvut.fel.pjv.weapons;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Item;
import cz.cvut.fel.pjv.ShareSound;
import cz.cvut.fel.pjv.entities.Team;
import cz.cvut.fel.pjv.entities.WeaponEntity;
import javafx.application.Platform;

import java.io.Serializable;
import java.util.Random;

import static cz.cvut.fel.pjv.Game.*;

/**
 * The Weapon class is an abstract representation of a weapon in the game.
 * This class holds key properties for ranged or melee weapons such as ammunition,
 * damage, fire rate, cooldown, reload mechanics, and visual/sound effects.
 */
public abstract class Weapon extends Item implements Serializable {

    static final double COOLDOWN_DECAY_RATE = 100;
    static final double RELOADING_DECAY_RATE = 7;

    protected int maxAmmo;
    protected int magazineSize;
    protected int ammoInMagazine;
    protected double damage;
    protected double reloadTime;
    protected double scatterAngle;
    protected double fireRate;
    protected double cooldown;
    protected int bulletCountPerShot;
    protected double bulletSpeed;
    protected BulletType bulletType;
    protected String shootingSound;
    protected FireMode fireMode;
    protected String icon;
    protected boolean reloading = false;
    protected double reloadingTimeLeft;
    protected String texture;

    protected WeaponEntity weaponEntity = null;

    /**
     * Constructs a new Weapon instance with the provided parameters.
     *
     * @param maxAmmo           the maximum ammunition available
     * @param magazineSize      the capacity of the magazine
     * @param damage            the damage per shot
     * @param reloadTime        the reload duration for the weapon
     * @param scatterAngle      the scatter angle for the shot (inaccuracy)
     * @param fireRate          the rate of fire which is used to initialize the cooldown
     * @param bulletCountPerShot the number of bullets fired in one shot
     * @param bulletSpeed       the speed at which bullets travel
     * @param bulletType        the type of bullet used for the shot
     * @param shootingSound     the sound identifier for the firing sound
     * @param fireMode          the firing mode of the weapon
     * @param icon              the icon identifying the weapon
     * @param texture           the texture information for the weapon
     */
    public Weapon(int maxAmmo, int magazineSize, double damage, double reloadTime, double scatterAngle,
                  double fireRate, int bulletCountPerShot, double bulletSpeed, BulletType bulletType, String shootingSound, FireMode fireMode,
                  String icon, String texture) {
        this.maxAmmo = maxAmmo;
        this.magazineSize = magazineSize;
        this.damage = damage;
        this.reloadTime = reloadTime;
        this.scatterAngle = scatterAngle;
        this.fireRate = fireRate;
        this.cooldown = 60/fireRate;
        this.bulletCountPerShot = bulletCountPerShot;
        this.bulletType = bulletType;
        this.bulletSpeed = bulletSpeed;
        this.shootingSound = shootingSound;
        this.fireMode = fireMode;
        this.icon = icon;
        this.reloadingTimeLeft = reloadTime;
        this.ammoInMagazine = magazineSize;
        this.texture = texture;
    }


    /** Calculates position for Bullet and creates one
     * @param targetX target pos X
     * @param targetY target pos Y
     * @param fromX shooter pos X
     * @param fromY shooter pos Y
     * @param shooterTeam shooter's Team
     */
    public void shoot(double targetX, double targetY, double fromX, double fromY, Team shooterTeam)
    {
        if(cooldown != 0)  return;
        if(ammoInMagazine <= 0)  return;
        for(int i = 0; i < this.bulletCountPerShot; ++i) {
            /// Calculate bullet's angle
            double angle = 0;
            if(shooterTeam == Team.Players) {
                double distanceX = targetX - Game.WIDTH / 2;
                double distanceY = targetY - Game.HEIGHT / 2;
                angle = Math.atan2(distanceY, distanceX);
            }
            else
            {
                double distanceX = targetX - fromX;
                double distanceY = targetY - fromY;
                angle = Math.atan2(distanceY, distanceX);
            }

            Random random = new Random();
            double accuracyCorrectionAngle = Math.toRadians(random.nextDouble() * (this.scatterAngle));
            if (random.nextBoolean())
                accuracyCorrectionAngle = -accuracyCorrectionAngle;

            angle = angle + accuracyCorrectionAngle;

            /// Calculate the bullet's width, height, texture
            double bulletWidth;
            double bulletHeight;
            String texture;
            switch (this.bulletType) {
                case PISTOL_AMMO:
                    bulletWidth = 0.1;
                    bulletHeight = 0.1;
                    texture = "pistolBullet";
                    break;
                case SMG_AMMO:
                    bulletWidth = 0.2;
                    bulletHeight = 0.2;
                    texture = "smgBullet";
                    break;
                case SHOTGUN_AMMO:
                    bulletWidth = 0.12;
                    bulletHeight = 0.12;
                    texture = "shotgunBullet";
                    break;
                case NONE:
                default:
                    throw new RuntimeException("Bullet type not supported");
            }

            Game.addEntity(new Bullet(fromX, fromY, bulletSpeed,
                    angle, this.damage, this.bulletType, bulletWidth, bulletHeight, texture, shooterTeam));

            this.cooldown = 60 / this.fireRate;
        }
        if(ammoInMagazine != Integer.MAX_VALUE)  ammoInMagazine -= 1;
        soundManager.playSound(this.shootingSound);
        if(multiplayer) exportEntities.add(new ShareSound(this.shootingSound));
        if(shooterTeam == Team.Players)  player.hudManager.updateAmmoLabel();
    }


    /// Updates a weapon's cooldown and reloads a weapon
    public void update()
    {
        cooldown -= COOLDOWN_DECAY_RATE * Game.deltaTime;
        if(cooldown < 0) cooldown = 0;


        if(reloading)
        {
            if(player.weaponBelongToPlayer(this) && !(player.getWeaponInHands().equals(this)))  return;

            reloadingTimeLeft -= RELOADING_DECAY_RATE * Game.deltaTime;
            if(reloadingTimeLeft < 0)
            {
                if(maxAmmo < magazineSize)
                {
                    ammoInMagazine = maxAmmo;
                    maxAmmo = 0;
                }
                else {
                    maxAmmo -= magazineSize - ammoInMagazine;
                    ammoInMagazine = magazineSize;
                }

                reloadingTimeLeft = reloadTime;
                reloading = false;
                player.hudManager.updateAmmoLabel();
            }
        }
    }

    public void reload()
    {
        reloading = true;
    }

    public double getCooldown()
    {
        return this.cooldown;
    }

    public FireMode getFireMode() {
        return fireMode;
    }


    public void createWeaponEntity()
    {
        weaponEntity = new WeaponEntity(icon, this);
    }

    public String getIcon() {
        return icon;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public String getTexture(){
        return this.texture;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = maxAmmo;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public int getAmmoInMagazine() {
        return ammoInMagazine;
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setFireRate(double fireRate) {
        this.fireRate = fireRate;
    }

    public double getFireRate() {
        return fireRate;
    }

}
