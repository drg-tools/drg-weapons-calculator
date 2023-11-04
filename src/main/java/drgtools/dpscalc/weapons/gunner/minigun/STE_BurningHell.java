package drgtools.dpscalc.weapons.gunner.minigun;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

/*
    Ok, *technically* this isn't a Status Effect. However, the way that its damage flags work out I think it will be
    simpler to add it to Breakpoints() as a DoT instead of a secondary DamageComponent with a 4/sec tickrate unrelated
    to Minigun's RoF.
*/
public class STE_BurningHell extends StatusEffect {
    public STE_BurningHell() {
        super(DamageElement.fire, 5, 5, TemperatureElement.heat, 20, 20, 0.25, 0.25, 1.0, 9.5);
    }
}
