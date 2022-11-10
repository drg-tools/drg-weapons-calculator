package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_GooPuddle_ImprovedPoison extends StatusEffect {
    public STE_GooPuddle_ImprovedPoison() {
        super(damageElement.corrosive, 1, 1, 0.2, 0.3, 0.75);
    }
}
