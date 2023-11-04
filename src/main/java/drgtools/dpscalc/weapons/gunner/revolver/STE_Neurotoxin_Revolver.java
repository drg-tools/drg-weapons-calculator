package drgtools.dpscalc.weapons.gunner.revolver;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_Neurotoxin_Revolver extends StatusEffect {
    public STE_Neurotoxin_Revolver() {
        super(DamageElement.poison, 12, 12, 0.75, 1.25, 0.7, 10);
    }
}
