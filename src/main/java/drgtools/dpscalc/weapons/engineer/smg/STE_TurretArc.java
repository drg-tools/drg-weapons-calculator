package drgtools.dpscalc.weapons.engineer.smg;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_TurretArc extends StatusEffect {
    public STE_TurretArc() {
        super(DamageElement.electric, 6, 6, 0.2, 0.2, 0.2, 1);
    }
}
