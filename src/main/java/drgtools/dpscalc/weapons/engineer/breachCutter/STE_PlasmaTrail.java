package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_PlasmaTrail extends StatusEffect {
    public STE_PlasmaTrail() {
        super(DamageElement.fire, 5, 5, 0.2, 0.3, 0.6);
    }
}
