package drgtools.dpscalc.weapons.scout.plasmaCarbine;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.Projectile;

public class PRJ_PlasmaCarbineShot extends Projectile {
    public PRJ_PlasmaCarbineShot(double velocity, DamageComponent dmg) {
        // default velocity is 27 m/sec
        super(velocity, 0, 0.15, dmg);
    }
}
