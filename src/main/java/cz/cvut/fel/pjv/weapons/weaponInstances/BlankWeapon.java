package cz.cvut.fel.pjv.weapons.weaponInstances;

import cz.cvut.fel.pjv.entities.Team;
import cz.cvut.fel.pjv.weapons.BulletType;
import cz.cvut.fel.pjv.weapons.FireMode;
import cz.cvut.fel.pjv.weapons.Weapon;

/// Used as a filler
public class BlankWeapon extends Weapon {

    /// Used as a filler
    public BlankWeapon() {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, 2, 1, 0, 10, 1, 30,
                BulletType.PISTOL_AMMO, "/sounds/gun-gunshot-01", FireMode.MANUAL,
                "empty", "empty");
    }

    @Override
    public void shoot(double targetX, double targetY, double fromX, double fromY, Team shooterTeam) {
        return;
    }
}
