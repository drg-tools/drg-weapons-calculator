package drgtools.dpscalc.weapons.driller.subata;

import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_DeadBugWalking extends StatusEffect {
    public STE_DeadBugWalking() {
        super(0.1, 2);
    }

    @Override
    public String getName() {
        return "STE_DeadBugWalking";
    }
}
