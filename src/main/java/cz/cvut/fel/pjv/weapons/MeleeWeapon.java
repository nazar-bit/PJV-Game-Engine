package cz.cvut.fel.pjv.weapons;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.entities.Team;


/**
 * The MeleeWeapon class represents a melee-type weapon used within the game.
 *
 * <p>This class extends the Weapon base class and includes additional parameters
 * specific to melee attacks, such as the attack width and range. It implements the
 * shoot method to create a MeleeHitBox, which is used to detect and deal damage for a
 * melee attack.</p>
 */
public class MeleeWeapon extends Weapon{

    protected double attackWidth;
    protected double attackRange;


    /**
     * Constructs a new MeleeWeapon with the specified parameters.
     *
     * @param maxAmmo           the maximum ammunition capacity (though melee weapons often don't use ammo)
     * @param magazineSize      the magazine size (may not be applicable to melee weapons)
     * @param damage            the damage inflicted by the weapon per successful hit
     * @param reloadTime        the time required to reload the weapon (if applicable)
     * @param scatterAngle      the spread angle of the weapon's attack (if applicable)
     * @param fireRate          the rate of fire for the weapon
     * @param bulletCountPerShot the number of projectiles fired per shot
     * @param bulletSpeed       the speed of the projectile (if applicable)
     * @param bulletType        the type of bullet/projectile (though melee weapons create hitboxes)
     * @param shootingSound     the sound played when the weapon is fired
     * @param fireMode          the firing mode of the weapon
     * @param icon              the icon representing the weapon
     * @param texture           the texture used for the weapon's appearance
     * @param attackWidth       the effective width of the melee attack's hitbox
     * @param attackRange       the effective range (or depth) of the melee attack's hitbox
     */
    public MeleeWeapon(int maxAmmo, int magazineSize, double damage, double reloadTime, double scatterAngle,
                       double fireRate, int bulletCountPerShot, double bulletSpeed, BulletType bulletType, String shootingSound, FireMode fireMode,
                       String icon, String texture, double attackWidth, double attackRange)
    {
        super(maxAmmo, magazineSize, damage, reloadTime, scatterAngle, fireRate, bulletCountPerShot, bulletSpeed, bulletType, shootingSound, fireMode, icon, texture);

        this.attackRange = attackRange;
        this.attackWidth = attackWidth;
    }


    /** Calculates the position of MeleeHitBox and creates one
     * @param targetX target pos X
     * @param targetY target pos Y
     * @param fromX shooter pos X
     * @param fromY shooter pos Y
     * @param shooterTeam shooter Team
     */
    @Override
    public void shoot(double targetX, double targetY, double fromX, double fromY, Team shooterTeam)
    {
        if(cooldown != 0)  return;
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


        double distance = 1;
        double tangle = Math.tan(angle);
        double x = Math.sqrt((distance*distance)/(1 + tangle*tangle));
        double y =tangle*x;

        if((angle >= -Math.PI && angle < -Math.PI/2) || (angle > Math.PI/2 && angle <= Math.PI)){
            x = (fromX) - x - attackWidth / 4;
            y = (fromY) - y;
        }
        else {
            x = (fromX) + x - attackWidth / 4;
            y = (fromY) + y;
        }


        MeleeHitBox meleeHitBox = new MeleeHitBox(x, y, attackWidth, attackRange, damage, shooterTeam);
        Game.addEntity(meleeHitBox);

        meleeHitBox.setRotationAngle(Math.toDegrees(angle) + 90);
        meleeHitBox.drawer.rotate(meleeHitBox.getRotationAngle());
        this.cooldown = 60 / this.fireRate;
    }


    public double getAttackWidth()
    {
        return attackWidth;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public void setAttackWidth(double attackWidth)
    {
        this.attackWidth = attackWidth;
    }

    public void setAttackRange(double attackRange){
        this.attackRange = attackRange;
    }



}
