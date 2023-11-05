package drgtools.dpscalc.weapons;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

/*
    Technically, this isn't a Status Effect in-game. However, modeling it as one makes the ConditionalDamageConversions
    a lot easier.
*/
public class STE_Frozen extends StatusEffect {
    public STE_Frozen() {
        super(0.0, EnemyInformation.averageFreezeDuration());
    }
    public STE_Frozen(double duration) {
        super(0.0, duration);
    }

    @Override
    public String getName() {
        return "STE_Frozen";
    }
}
