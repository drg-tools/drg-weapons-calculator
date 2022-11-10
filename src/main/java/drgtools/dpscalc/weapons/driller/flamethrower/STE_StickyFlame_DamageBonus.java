package drgtools.dpscalc.weapons.driller.flamethrower;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_StickyFlame_DamageBonus extends StatusEffect {
    public STE_StickyFlame_DamageBonus() {
        super(damageElement.fire, 5, 5, 0.5, 0.5, 0.25);
    }
}
