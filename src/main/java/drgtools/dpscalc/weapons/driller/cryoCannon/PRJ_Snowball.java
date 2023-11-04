package drgtools.dpscalc.weapons.driller.cryoCannon;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.modelPieces.Projectile;

public class PRJ_Snowball extends Projectile {
    public PRJ_Snowball() {
        super(
                30,
                1.25,
                0.35,
                new DamageComponent(0, null, 0, TemperatureElement.cold,
                        false, false, false, false,
                        0, null, 200, 2, 4, 0.5,
                        false, 0, 0, 0, 0, 0.2, null
                )
        );
    }
}
