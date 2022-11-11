package drgtools.dpscalc.weapons.gunner.revolver;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_RevolverNeurotoxin extends StatusEffect {
    public STE_RevolverNeurotoxin() {
        super(damageElement.poison, 12, 12, 0.75, 1.25, 0.7, 10);
    }
}
