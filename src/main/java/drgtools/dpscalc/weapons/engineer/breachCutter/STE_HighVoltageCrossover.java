package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_HighVoltageCrossover extends StatusEffect {
    public STE_HighVoltageCrossover() {
        super(damageElement.electric, 4, 4, 0.25, 0.25, 0.2, 4);
    }
}
