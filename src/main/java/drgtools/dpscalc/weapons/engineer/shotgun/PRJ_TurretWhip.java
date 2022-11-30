package drgtools.dpscalc.weapons.engineer.shotgun;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_TurretWhip extends Projectile {
    public PRJ_TurretWhip() {
        super(
                75,
                0,
                0.32,
                new DamageComponent(
                        0, null, 0, null,
                        false, false, false, false,
                        160, DamageElement.explosive, 0, 1.5, 2, 0.5,
                        false, 1.0, 1.5, 1.0, 2.0, 0.5, null
                )
        );
    }
}
