package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_HighVoltageCrossover extends StatusEffect {
    public STE_HighVoltageCrossover() {
        super(DamageElement.electric, 4, 4, 0.25, 0.25, 0.2, 4);
    }
}
