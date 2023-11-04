package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_Inferno extends StatusEffect {
    public STE_Inferno() {
        // TODO: remember to add logic to make this do 11 ticks, instead of 10
        super(DamageElement.fire, 7, 7, TemperatureElement.heat, 7, 7, 0.5, 0.5, 1.0, 5);
    }
}
