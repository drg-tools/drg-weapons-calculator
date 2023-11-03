package drgtools.dpscalc.weapons.engineer.grenadeLauncher;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_Grenade extends Projectile {
    public PRJ_Grenade(double velocity, DamageComponent dmg) {
        // default velocity is 30 m/sec.
        super(velocity, 1.25, 0.05, dmg);
    }
}
