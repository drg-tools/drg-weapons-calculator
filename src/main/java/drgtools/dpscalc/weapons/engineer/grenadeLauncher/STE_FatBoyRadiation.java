package drgtools.dpscalc.weapons.engineer.grenadeLauncher;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_FatBoyRadiation extends StatusEffect {
    public STE_FatBoyRadiation() {
        super(DamageElement.radiation, 25, 25, 0.75, 1.25, 1);
    }
}
