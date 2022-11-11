package drgtools.dpscalc.weapons.engineer.smg;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_TurretArc extends AoEStatusEffect {
    public STE_TurretArc() {
        super(0.4, DamageElement.electric, 6, 6, 0.2, 0.2, 0.2, 1, 20);
    }
}
