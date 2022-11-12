package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_ChargedShot extends Projectile {
    public PRJ_ChargedShot(double velocity, double gravity, DamageComponent dmg) {
        // default velocity is 15 m/sec
        super(velocity, gravity, 0.45, dmg);
    }
}
