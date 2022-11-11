package drgtools.dpscalc.weapons.engineer.grenadeLauncher;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_FatBoyRadiation extends AoEStatusEffect {
    public STE_FatBoyRadiation() {
        super(8, DamageElement.radiation, 25, 25, 0.75, 1.25, 1.0, 1, 15);
    }
}
