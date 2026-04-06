package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.Weapon;

/**
 * Shotgun represents a specific weapon type, inheriting common behavior from the Weapon class.
 *
 * <p>
 * The constructor initializes the weapon with predefined parameters such as rate of fire, damage,
 * magazine size, reload time, and more. These parameters are passed to the superclass Weapon.
 * </p>
 */
public class Shotgun extends Weapon {
    public Shotgun() {
        super(40, 2, 3.5, 5, 10, 5, 5,
                10, BulletType.SHOTGUN_AMMO, "/sounds/gun-gunshot-01", FireMode.AUTOMATIC,
                "shotgunIcon", "shotgunIcon");
    }

}
