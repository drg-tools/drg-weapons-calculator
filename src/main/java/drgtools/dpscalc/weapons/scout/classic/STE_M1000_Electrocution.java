package drgtools.dpscalc.weapons.scout.classic;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_M1000_Electrocution extends StatusEffect {
    public STE_M1000_Electrocution() {
        super(damageElement.electric, 4.5, 4.5, 0.2, 0.2, 0.2, 6);
    }
}
