package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.Weapon;

/**
 * AWP represents a specific weapon type, inheriting common behavior from the Weapon class.
 *
 * <p>
 * The constructor initializes the weapon with predefined parameters such as rate of fire, damage,
 * magazine size, reload time, and more. These parameters are passed to the superclass Weapon.
 * </p>
 */
public class AWP extends Weapon {

    public AWP() {
        super(25, 5, 50, 7, 0, 1, 1,
                50, BulletType.PISTOL_AMMO, "/sounds/gun-gunshot-01", FireMode.MANUAL,
                "awp", "awp");
    }
}
