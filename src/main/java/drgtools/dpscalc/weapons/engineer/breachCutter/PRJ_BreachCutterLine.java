package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_BreachCutterLine extends Projectile {
    public PRJ_BreachCutterLine(double velocity, DamageComponent dmgPerTick) {
        // default velocity is 10 m/sec
        super(velocity, 0, 0.15, dmgPerTick);
    }

    // TODO: maybe move the "intersection time" methods into this object?
}
