package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.Weapon;

/**
 * AK47 represents a specific weapon type, inheriting common behavior from the Weapon class.
 *
 * <p>
 * The constructor initializes the weapon with predefined parameters such as rate of fire, damage,
 * magazine size, reload time, and more. These parameters are passed to the superclass Weapon.
 * </p>
 */
public class AK47 extends Weapon {

    public AK47() {
        super(180, 25, 7, 5, 0, 3, 1,
                40, BulletType.PISTOL_AMMO, "/sounds/gun-gunshot-01", FireMode.AUTOMATIC,
                "ak47", "ak47");
    }
}