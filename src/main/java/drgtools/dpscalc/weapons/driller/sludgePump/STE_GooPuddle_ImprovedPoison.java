package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_GooPuddle_ImprovedPoison extends AoEStatusEffect {
    public STE_GooPuddle_ImprovedPoison(double puddleRadius, double puddleDuration) {
        super(2 * puddleRadius, DamageElement.corrosive, 1, 1, 0.2, 0.3, 1.0, 0.75, puddleDuration);
    }
}
