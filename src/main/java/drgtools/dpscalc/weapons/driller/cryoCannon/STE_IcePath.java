package drgtools.dpscalc.weapons.driller.cryoCannon;

import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_IcePath extends StatusEffect {
    public STE_IcePath() {
        super(null, 0, 0, TemperatureElement.cold, 8, 8, 0.5, 0.5, 1.0, 3);
    }
}
