package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.enemies.ElementalResistancesMap;
import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.utilities.MathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;

// For the strange usecase where there are several StatusEffects being applied by one weapon to one enemy all at once
// Needs to add up the DPS, split by DMG_Element, aggregate Heat/sec, multiply the slows, etc etc
// Also needs to transparently work if there's only one STE, because that way it can be used as an entry point for Breakpoints()
public class MultipleSTEs {
    private ArrayList<PushSTEComponent> statusEffects;
    private EnumMap<DamageElement, Double> totalDamageByElement;
    private double complicatedSlowUtilitySum;

    public MultipleSTEs(ArrayList<PushSTEComponent> stes) {
        statusEffects = stes;
        totalDamageByElement = new EnumMap<>(DamageElement.class);
        complicatedSlowUtilitySum = 0;
        doComplexCalculations();
    }

    public void doComplexCalculations() {
        // 30 seconds sounds long enough to account for everything?
        doComplexCalculations(30);
    }
    public void doComplexCalculations(double maxDuration) {
        // Zero out the current values
        int i;
        for(i = 0; i < DamageElements.numElements; i++) {
            totalDamageByElement.put(DamageElements.getElementAtIndex(i), 0.0);
        }
        complicatedSlowUtilitySum = 0.0;

        StatusEffect alias;
        double totalTimeElapsed = 0;
        double currentSlow, shortestDuration;

        // Make a deep clone of the class variable that I can mutate in this method without affecting it.
        ArrayList<PushSTEComponent> orderableCopyListOfStes = new ArrayList<>(statusEffects);

        // Ugh, so inefficient! iterating through the STE list at least 5 times per loop :'(
        while (totalTimeElapsed < maxDuration && orderableCopyListOfStes.size() > 0) {
            // Calculate the total slow of all active Status Effects
            currentSlow = 1.0;
            for (i = 0; i < orderableCopyListOfStes.size(); i++) {
                alias = orderableCopyListOfStes.get(i).getSTE();
                if (orderableCopyListOfStes.get(i).isActive() && alias.inflictsSlow()) {
                    currentSlow *= alias.getMovespeedMultiplier();
                }
            }

            // Iterate through all of the AoE Status Effects and update their durations based on the aggregate slow
            for (i = 0; i < orderableCopyListOfStes.size(); i++) {
                // This internally handles the logic about whether or not it's an AoE Status Effect
                orderableCopyListOfStes.get(i).updateEffectiveDuration(currentSlow);
            }

            // Sort the Status Effects to put the shortest duration first
            Collections.sort(orderableCopyListOfStes);

            // Iterate through the next chunk of time
            shortestDuration = Math.min(orderableCopyListOfStes.get(0).getSTEComparedDurationPlusDelay(), maxDuration - totalTimeElapsed);
            for (i = 0; i < orderableCopyListOfStes.size(); i++) {
                alias = orderableCopyListOfStes.get(i).getSTE();
                // Don't check if it's active here, because the predictDamage() method already has logic to account for those variations.
                if (alias.inflictsDamage()) {
                    totalDamageByElement.put(alias.getDamageElement(), totalDamageByElement.get(alias.getDamageElement()) + orderableCopyListOfStes.get(i).predictDamageDealtInNextTimeInterval(shortestDuration));
                }
            }
            complicatedSlowUtilitySum += shortestDuration * (1.0 - currentSlow);
            totalTimeElapsed += shortestDuration;

            // Shorten all STEs' duration by shortestDuration, and if the remaining duration is zero (guaranteed true on #0) then remove it from the list.
            i = 0;
            while (i < orderableCopyListOfStes.size()) {
                orderableCopyListOfStes.get(i).progressTime(shortestDuration);
                if (orderableCopyListOfStes.get(i).getSTEComparedDurationPlusDelay() <= totalTimeElapsed) {
                    orderableCopyListOfStes.remove(i);
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
    public double getResistedSumDPS(ElementalResistancesMap resistances) {
        double sumDPS = 0;
        StatusEffect alias;
        for (int i = 0; i < statusEffects.size(); i++) {
            alias = statusEffects.get(i).getSTE();
            sumDPS += alias.getAverageDPS() * resistances.getResistance(alias.getDamageElement());
        }
        return sumDPS;
    }

    public double getRawTotalDamage() {
        return MathUtils.sum(totalDamageByElement);
    }
    public double getResistedTotalDamage(ElementalResistancesMap resistances) {
        return resistances.multiplyDamageByElements(totalDamageByElement);
    }

    public double getAggregateTemperaturePerSecond(DamageElement desiredTemperature) {
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

    // Pass-through methods that facilitate communication between BreakpointCalculator and its internal PushSTEComponents
    public void resetTimeElapsed() {
        for (PushSTEComponent pstec: statusEffects) {
            pstec.resetTimeElapsed();
        }
    }
    public void progressTime(double secondsElapsed) {
        for (PushSTEComponent pstec: statusEffects) {
            pstec.progressTime(secondsElapsed);
        }
    }
    public double predictResistedDamageDealtInNextTimeInterval(double secondsToPredict, ElementalResistancesMap creatureResistances) {
        EnumMap<DamageElement, Double> damageByElement = new EnumMap<>(DamageElement.class);

        // This is an imperfect approximation for the overlapping and interacting slows and AoEs. Works poorly on long
        // predictions, but it's passable when doing a series of tiny predictions like 0.25 sec.
        double totalSlow = 1.0;
        for (PushSTEComponent pstec: statusEffects) {
            if (pstec.getSTE().inflictsSlow() && pstec.isActive()) {
                totalSlow *= pstec.getSTE().getMovespeedMultiplier();
            }
        }

        DamageElement element;
        for (PushSTEComponent pstec: statusEffects) {
            pstec.updateEffectiveDuration(totalSlow);  // For any AoE Status Effects
            if (pstec.getSTE().inflictsDamage()) {
                element = pstec.getSTE().getDamageElement();
                if (damageByElement.containsKey(element)) {
                    damageByElement.put(element, damageByElement.get(element) + pstec.predictDamageDealtInNextTimeInterval(secondsToPredict));
                }
                else {
                    damageByElement.put(element, pstec.predictDamageDealtInNextTimeInterval(secondsToPredict));
                }
            }
        }

        return creatureResistances.multiplyDamageByElements(damageByElement);
    }
}
