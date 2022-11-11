package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_GooPuddle_ImprovedPoison extends StatusEffect {
    public STE_GooPuddle_ImprovedPoison() {
        super(DamageElement.corrosive, 1, 1, 0.2, 0.3, 0.75);
    }
}
