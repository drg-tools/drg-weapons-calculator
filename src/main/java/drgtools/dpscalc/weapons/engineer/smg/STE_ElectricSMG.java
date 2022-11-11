package drgtools.dpscalc.weapons.engineer.smg;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_ElectricSMG extends StatusEffect {
    public STE_ElectricSMG() {
        super(DamageElement.electric, 3, 3, 0.25, 0.25, 0.2, 3);
    }
}
