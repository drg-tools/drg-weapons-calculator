package drgtools.dpscalc.weapons.gunner.guidedRocketLauncher;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_MicroMissile extends Projectile {
    public PRJ_MicroMissile(double startVelocity, double maxVelocity, DamageComponent dmg) {
        // starts at 10 m/sec, accelerates to 15 m/sec.
        // one curve takes 2.5 sec, the "improved one" only takes 1, but i can't figure out how to interpret the cubic splines yet.
        // TODO: develop a way to take in the Cubic Spline curves used everywhere and get polynomial equation equivalents
        // Cubic spline? https://forums.unrealengine.com/t/calculate-spline-tangent/477708/3
        super(startVelocity, maxVelocity, 0, 0.32, dmg);
    }
}
