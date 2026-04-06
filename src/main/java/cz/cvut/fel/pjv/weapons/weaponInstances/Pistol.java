package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.Weapon;

/**
 * Pistol represents a specific weapon type, inheriting common behavior from the Weapon class.
 *
 * <p>
 * The constructor initializes the weapon with predefined parameters such as rate of fire, damage,
 * magazine size, reload time, and more. These parameters are passed to the superclass Weapon.
 * </p>
 */
public class Pistol extends Weapon {

    public Pistol() {
        super(70, 7, 15, 2.5, 0, 2, 1,
                30, BulletType.PISTOL_AMMO, "/sounds/gun-gunshot-01", FireMode.MANUAL,
                "pistolIcon", "pistolIcon");
    }

}
