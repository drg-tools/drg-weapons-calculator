package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_GooPuddle extends StatusEffect {
    public STE_GooPuddle() {
        super(damageElement.corrosive, 4, 4, 0.2, 0.3, 0.55, 0.75);
    }
}
