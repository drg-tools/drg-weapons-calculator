package drgtools.dpscalc.weapons.scout.classic;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_M1000_Electrocution extends StatusEffect {
    public STE_M1000_Electrocution() {
        super(DamageElement.electric, 4.5, 4.5, 0.2, 0.2, 0.2, 6);
    }
}
