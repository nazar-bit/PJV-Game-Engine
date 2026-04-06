package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.Weapon;

/**
 * M16 represents a specific weapon type, inheriting common behavior from the Weapon class.
 *
 * <p>
 * The constructor initializes the weapon with predefined parameters such as rate of fire, damage,
 * magazine size, reload time, and more. These parameters are passed to the superclass Weapon.
 * </p>
 */
public class M16 extends Weapon {

    public M16() {
        super(250, 50, 3, 4, 10, 8, 1,
                30, BulletType.PISTOL_AMMO, "/sounds/gun-gunshot-01", FireMode.AUTOMATIC,
                "m16", "m16");
    }
}
