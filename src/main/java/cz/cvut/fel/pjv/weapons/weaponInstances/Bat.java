package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.MeleeWeapon;

/**
 * Bat represents a specific melee weapon type, inheriting common behavior from the Weapon class.
 *
 * <p>
 * The constructor initializes the weapon with predefined parameters such as rate of fire, damage,
 * magazine size, reload time, and more. These parameters are passed to the superclass Weapon.
 * </p>
 */
public class Bat extends MeleeWeapon {

    public Bat() {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, 15, 1, 0, 1, 1,
                30, BulletType.PISTOL_AMMO, "/sounds/gun-gunshot-01", FireMode.MELEE,
                "batIcon", "batIcon", 2, 1);
    }
}
