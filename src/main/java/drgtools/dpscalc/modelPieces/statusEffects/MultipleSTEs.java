package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.utilities.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

// For the strange usecase where there are several StatusEffects being applied by one weapon to one enemy all at once
// Needs to add up the DPS, split by DMG_Element, aggregate Heat/sec, multiply the slows, etc etc
// Also needs to transparently work if there's only one STE, because that way it can be used as an entry point for Breakpoints()
public class MultipleSTEs {
    // TODO: consider refactoring this to an ArrayList, for inserting/deleting StatusEffects?
    private PushSTEComponent[] statusEffects;
    private double[] totalDamageByElement;
    private double complicatedSlowUtilitySum;

    public MultipleSTEs(PushSTEComponent[] stes) {
        statusEffects = stes;
        totalDamageByElement = new double[DamageElements.numElements];
        complicatedSlowUtilitySum = 0;
        doComplexCalculations();
    }

    public void doComplexCalculations() {
        // 20 seconds sounds long enough to account for everything?
        doComplexCalculations(20);
    }
    public void doComplexCalculations(double maxDuration) {
        // Zero out the current values
        int i;
        for(i = 0; i < totalDamageByElement.length; i++) {
            totalDamageByElement[i] = 0.0;
        }
        complicatedSlowUtilitySum = 0.0;

        ArrayList<PushSTEComponent> orderableSteList = new ArrayList<>(Arrays.asList(statusEffects));
        StatusEffect alias;
        double totalTimeElapsed = 0;
        double currentSlow, shortestDuration;

        // Ugh, so inefficient! iterating through the STE list 6 times per loop :'(
        while (totalTimeElapsed < maxDuration && orderableSteList.size() > 0) {
            // Calculate the total slow of all active Status Effects
            currentSlow = 1.0;
            for (i = 0; i < orderableSteList.size(); i++) {
                currentSlow *= orderableSteList.get(i).getSTE().getMovespeedMultiplier();
            }

            // Iterate through all of the AoE Status Effects and update their durations based on the aggregate slow
            for (i = 0; i < orderableSteList.size(); i++) {
                alias = orderableSteList.get(i).getSTE();
                if (alias instanceof AoEStatusEffect) {
                    ((AoEStatusEffect) alias).calculateEffectiveDuration(currentSlow);
                    orderableSteList.get(i).setSTE(alias);
                }
            }

            // Sort the Status Effects to put the shortest duration first
            Collections.sort(orderableSteList);

            // Iterate through the next chunk of time
            shortestDuration = Math.min(orderableSteList.get(0).getSTE().getComparedDuration(), maxDuration - totalTimeElapsed);
            for (i = 0; i < orderableSteList.size(); i++) {
                alias = orderableSteList.get(i).getSTE();
                if (alias.inflictsDamage()) {
                    totalDamageByElement[DamageElements.getElementIndex(alias.getDamageElement())] += shortestDuration * alias.getAverageDPS();
                }
            }
            complicatedSlowUtilitySum += shortestDuration * (1.0 - currentSlow);
            totalTimeElapsed += shortestDuration;

            // Shorten all STEs' duration by shortestDuration, and if the remaining duration is zero (guaranteed true on #0) remove it from the list.
            for (i = 0; i < orderableSteList.size(); i++) {
                orderableSteList.get(i).getSTE().reduceComparedDuration(shortestDuration);
            }
            i = 0;
            while (i < orderableSteList.size()) {
                if (orderableSteList.get(i).getSTE().getComparedDuration() == 0) {
                    orderableSteList.remove(i);
                }
                else {
                    i++;
                }
            }
        }
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

    public double getRawTotalDamage() {
        return MathUtils.sum(totalDamageByElement);
    }
    public double getResistedTotalDamage(ElementalResistancesArray resistances) {
        return MathUtils.vectorDotProduct(totalDamageByElement, resistances.getResistances());
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

    public double getSlowUtility() {
        return complicatedSlowUtilitySum;
    }
}
