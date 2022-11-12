package drgtools.dpscalc.weapons.driller.epc;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_RegularShot extends Projectile {
    public PRJ_RegularShot(double velocity, DamageComponent dmg) {
        // default velocity is 35 m/sec
        super(velocity, 0, 0.2, dmg);
    }
}
