package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_PlasmaTrail extends AoEStatusEffect {
    public STE_PlasmaTrail(double distance) {  // TODO: better name for this parameter
        super(distance, DamageElement.fire, 5, 5, 0.2, 0.3, 1.0, 0.6, 4);
    }
}
