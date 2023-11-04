package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.Projectile;

public class PRJ_RegularShot extends Projectile {
    public PRJ_RegularShot(double velocity, double gravity, DamageComponent dmg) {
        // default velocity is 15 m/sec
        super(velocity, gravity, 0.25, dmg);
    }
}
