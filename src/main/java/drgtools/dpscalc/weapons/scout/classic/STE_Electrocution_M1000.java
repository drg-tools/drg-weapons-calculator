package drgtools.dpscalc.weapons.scout.classic;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_Electrocution_M1000 extends StatusEffect {
    public STE_Electrocution_M1000() {
        super(DamageElement.electric, 4.5, 4.5, 0.2, 0.2, 0.2, 6);
    }
}
