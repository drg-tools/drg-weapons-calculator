package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;

// For the strange usecase where there are several StatusEffects being applied by one weapon to one enemy all at once
// Needs to add up the DPS, split by DMG_Element, aggregate Heat/sec, multiply the slows, etc etc
// Also needs to transparently work if there's only one STE, because that way it can be used as an entry point for Breakpoints()
public class MultipleSTEs {
    // TODO: consider refactoring this to an ArrayList, for inserting/deleting StatusEffects?
    private PushSTEComponent[] statusEffects;

    public MultipleSTEs(PushSTEComponent[] stes) {
        statusEffects = stes;
    }

    public double getRawSumDPS() {
        double sumDPS = 0;
        for (int i = 0; i < statusEffects.length; i++) {
            sumDPS += statusEffects[i].getSTE().getAverageDPS();
        }
        return sumDPS;
    }

    public double getResistedSumDPS(ElementalResistancesArray resistances) {
        double sumDPS = 0;
        for (int i = 0; i < statusEffects.length; i++) {
            sumDPS += statusEffects[i].getSTE().getAverageDPS() * resistances.getResistance(statusEffects[i].getSTE().getDamageElement());
        }
        return sumDPS;
    }

    public double getAggregateTemperaturePerSecond(TemperatureElement desiredTemperature) {
        double tempPerSecTotal = 0;
        for (int i = 0; i < statusEffects.length; i++) {
            if (statusEffects[i].getSTE().inflictsTemperature(desiredTemperature)) {
                tempPerSecTotal += statusEffects[i].getSTE().getAverageTemperaturePerSecond(desiredTemperature);
            }
        }
        return tempPerSecTotal;
    }

    public double getSlowProduct() {
        double slowProduct = 1.0;
        for (int i = 0; i < statusEffects.length; i++) {
            slowProduct *= statusEffects[i].getSTE().getMovespeedMultiplier();
        }
        return slowProduct;
    }

    public double getOverlappingDuration() {
        // TODO: this is gonna be a nightmare, because the stacking slows will increase the duration that the average bug stays within the AoEStatusEffects
        return 0;
    }
}
