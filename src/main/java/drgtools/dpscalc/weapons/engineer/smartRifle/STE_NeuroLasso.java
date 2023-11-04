package drgtools.dpscalc.weapons.engineer.smartRifle;

import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_NeuroLasso extends StatusEffect {
    public STE_NeuroLasso() {
        super(0.9, 1);
        effectsStackWithMultipleApplications = true;
    }
}
