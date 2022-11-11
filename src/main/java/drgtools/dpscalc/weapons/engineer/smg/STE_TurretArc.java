package drgtools.dpscalc.weapons.engineer.smg;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_TurretArc extends StatusEffect {
    public STE_TurretArc() {
        super(damageElement.electric, 6, 6, 0.2, 0.2, 0.2, 1);
    }
}
