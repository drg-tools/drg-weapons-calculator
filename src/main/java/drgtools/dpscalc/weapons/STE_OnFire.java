package drgtools.dpscalc.weapons;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_OnFire extends StatusEffect {
    public STE_OnFire() {
        super(damageElement.fire, 6, 6, 0.3, 0.5, EnemyInformation.averageBurnDuration());
    }
}
