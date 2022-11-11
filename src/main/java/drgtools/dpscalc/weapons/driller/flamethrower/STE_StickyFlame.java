package drgtools.dpscalc.weapons.driller.flamethrower;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;
import drgtools.dpscalc.modelPieces.temperature.EnvironmentalTemperature;
import drgtools.dpscalc.modelPieces.temperature.EnvironmentalTemperature.TemperatureIntensity;

public class STE_StickyFlame extends StatusEffect {
    public STE_StickyFlame() {
        super(DamageElement.fire, 15, 15, 0.25, 0.75, 0.9, 0.25);
        envTemp = new EnvironmentalTemperature(TemperatureIntensity.Heat3);
    }
}
