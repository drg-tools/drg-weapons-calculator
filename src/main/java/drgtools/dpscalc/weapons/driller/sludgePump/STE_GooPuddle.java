package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_GooPuddle extends AoEStatusEffect {
    public STE_GooPuddle(double puddleRadius, double puddleDuration) {
        super(2 * puddleRadius, DamageElement.corrosive, 4, 4, 0.2, 0.3, 0.55, 0.75, puddleDuration);
    }
}
