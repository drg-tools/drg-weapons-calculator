package drgtools.dpscalc.modelPieces.statusEffects.commonSTEs;

import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_IFG extends AoEStatusEffect {
    public STE_IFG() {
        super(3.0, 0.25, 0.5, 15);
    }

    @Override
    public String getName() {
        return "STE_IFG";
    }
}
