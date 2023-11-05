package drgtools.dpscalc.weapons.driller.subata;

import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_TranqSlowdown  extends StatusEffect {
    public STE_TranqSlowdown() {
        super(0.5, 4);
    }

    @Override
    public String getName() {
        return "STE_TranqSlowdown";
    }
}
