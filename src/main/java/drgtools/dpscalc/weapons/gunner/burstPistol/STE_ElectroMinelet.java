package drgtools.dpscalc.weapons.gunner.burstPistol;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_ElectroMinelet extends StatusEffect {
    public STE_ElectroMinelet() {
        super(DamageElement.electric, 3, 3, 0.25, 0.25, 0.2, 6);
    }
}
