package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_GooPuddle_ImprovedSlow extends AoEStatusEffect {
    public STE_GooPuddle_ImprovedSlow(double puddleRadius, double puddleDuration) {
        // TODO: find default TickInterval values
        super(2 * puddleRadius, null, 0, 0, 0.5, 0.5, 0.5, 0.75, puddleDuration);
    }
}
