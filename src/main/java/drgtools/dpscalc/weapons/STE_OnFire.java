package drgtools.dpscalc.weapons;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_OnFire extends StatusEffect {
    public STE_OnFire() {
        super(DamageElement.fire, 6, 6, 0.3, 0.5, EnemyInformation.averageBurnDuration());
    }
    public STE_OnFire(double duration) {
        super(DamageElement.fire, 6, 6, 0.3, 0.5, duration);
    }

    @Override
    public String getName() {
        return "STE_OnFire";
    }
}
