package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.utilities.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

// For the strange usecase where there are several StatusEffects being applied by one weapon to one enemy all at once
// Needs to add up the DPS, split by DMG_Element, aggregate Heat/sec, multiply the slows, etc etc
// Also needs to transparently work if there's only one STE, because that way it can be used as an entry point for Breakpoints()
public class MultipleSTEs {
    // TODO: I think this is only used in Breakpoints?
    private ArrayList<PushSTEComponent> statusEffects;
    private double[] totalDamageByElement;
    private double complicatedSlowUtilitySum;

    public MultipleSTEs(ArrayList<PushSTEComponent> stes) {
        statusEffects = stes;
        totalDamageByElement = new double[DamageElements.numElements];
        complicatedSlowUtilitySum = 0;
        doComplexCalculations();
    }

    public void doComplexCalculations() {
        // 20 seconds sounds long enough to account for everything?
        doComplexCalculations(30);
    }
    public void doComplexCalculations(double maxDuration) {
        // Zero out the current values
        int i;
        for(i = 0; i < totalDamageByElement.length; i++) {
            totalDamageByElement[i] = 0.0;
        }
        complicatedSlowUtilitySum = 0.0;

        StatusEffect alias;
        double totalTimeElapsed = 0;
        double currentSlow, shortestDuration;

        // Ugh, so inefficient! iterating through the STE list at least 5 times per loop :'(
        while (totalTimeElapsed < maxDuration && statusEffects.size() > 0) {
            // Calculate the total slow of all active Status Effects
            currentSlow = 1.0;
            for (i = 0; i < statusEffects.size(); i++) {
                alias = statusEffects.get(i).getSTE();
                if (statusEffects.get(i).isActive() && alias.inflictsSlow()) {
                    currentSlow *= alias.getMovespeedMultiplier();
                }
            }

            // Iterate through all of the AoE Status Effects and update their durations based on the aggregate slow
            for (i = 0; i < statusEffects.size(); i++) {
                // This internally handles the logic about whether or not it's an AoE Status Effect
                statusEffects.get(i).updateEffectiveDuration(currentSlow);
            }

            // Sort the Status Effects to put the shortest duration first
            Collections.sort(statusEffects);

            // Iterate through the next chunk of time
            shortestDuration = Math.min(statusEffects.get(0).getSTEComparedDurationPlusDelay(), maxDuration - totalTimeElapsed);
            for (i = 0; i < statusEffects.size(); i++) {
                alias = statusEffects.get(i).getSTE();
                // Don't check if it's active here, because the predictDamage() method already has logic to account for those variations.
                if (alias.inflictsDamage()) {
                    totalDamageByElement[DamageElements.getElementIndex(alias.getDamageElement())] += statusEffects.get(i).predictDamageDealtInNextTimeInterval(shortestDuration);
                }
            }
            complicatedSlowUtilitySum += shortestDuration * (1.0 - currentSlow);
            totalTimeElapsed += shortestDuration;

            // Shorten all STEs' duration by shortestDuration, and if the remaining duration is zero (guaranteed true on #0) then remove it from the list.
            i = 0;
            while (i < statusEffects.size()) {
                statusEffects.get(i).progressTime(shortestDuration);
                if (statusEffects.get(i).getSTEComparedDurationPlusDelay() <= totalTimeElapsed) {
                    statusEffects.remove(i);
                }
                else {
                    i++;
                }
            }
        }
    }

    public double getRawSumDPS() {
        double sumDPS = 0;
        for (int i = 0; i < statusEffects.size(); i++) {
            sumDPS += statusEffects.get(i).getSTE().getAverageDPS();
        }
        return sumDPS;
    }
    public double getResistedSumDPS(ElementalResistancesArray resistances) {
        double sumDPS = 0;
        for (int i = 0; i < statusEffects.size(); i++) {
            sumDPS += statusEffects.get(i).getSTE().getAverageDPS() * resistances.getResistance(statusEffects.get(i).getSTE().getDamageElement());
        }
        return sumDPS;
    }

    public double getRawTotalDamage() {
        return MathUtils.sum(totalDamageByElement);
    }
    public double getResistedTotalDamage(ElementalResistancesArray resistances) {
        return MathUtils.vectorDotProduct(totalDamageByElement, resistances.getResistances());
    }

    public double getAggregateTemperaturePerSecond(TemperatureElement desiredTemperature) {
        double tempPerSecTotal = 0;
        for (int i = 0; i < statusEffects.size(); i++) {
            if (statusEffects.get(i).getSTE().inflictsTemperature(desiredTemperature)) {
                tempPerSecTotal += statusEffects.get(i).getSTE().getAverageTemperaturePerSecond(desiredTemperature);
            }
        }
        return tempPerSecTotal;
    }

    public double getSlowUtility() {
        return complicatedSlowUtilitySum;
    }
}
