package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;

// For things like Persistent Plasma, Fat Boy, Sticky Flames, Coilgun trail, etc. Any STE that has a short duration but
// gets re-applied frequently as long as enemies stay within the Area of Effect
public abstract class AoEStatusEffect extends StatusEffect {
    private double distanceAffected;
    private double effectiveDuration;
    private double maxDuration;

    protected AoEStatusEffect(double areaEffectDistanceMeters, DamageElement dmgElement, double minDmg, double maxDmg,
                           double minInterval, double maxInterval, double slowMultiplier, double dur, double maxDur) {
        super(dmgElement, minDmg, maxDmg, minInterval, maxInterval, slowMultiplier, dur);
        distanceAffected = areaEffectDistanceMeters;
        maxDuration = maxDur;
        calculateEffectiveDuration(slowMultiplier);
        // comparedDuration = effectiveDuration;
    }

    protected AoEStatusEffect(double areaEffectDistanceMeters, double slowMultiplier, double dur, double maxDur) {
        super(slowMultiplier, dur);
        distanceAffected = areaEffectDistanceMeters;
        maxDuration = maxDur;
        calculateEffectiveDuration(slowMultiplier);
        // comparedDuration = effectiveDuration;
    }

    protected AoEStatusEffect(double areaEffectDistanceMeters, DamageElement dmgElement, double minDmg, double maxDmg,
                           DamageElement tmpElement, double minTemp, double maxTemp,
                           double minInterval, double maxInterval, double slowMultiplier, double dur, double maxDur) {
        super(dmgElement, minDmg, maxDmg, tmpElement, minTemp, maxTemp, minInterval, maxInterval, slowMultiplier, dur);
        distanceAffected = areaEffectDistanceMeters;
        maxDuration = maxDur;
        calculateEffectiveDuration(slowMultiplier);
        // comparedDuration = effectiveDuration;
    }

    public void calculateEffectiveDuration(double slowMultiplier) {
        double timeItTakesAverageCreatureToTraverseDistance = distanceAffected / (EnemyInformation.averageMovespeed() * slowMultiplier);
        effectiveDuration = Math.min(
            Math.ceil(timeItTakesAverageCreatureToTraverseDistance / duration) * duration,
            maxDuration + duration
        );
        // comparedDuration = effectiveDuration;
    }

    public double getEffectiveDuration() {
        return effectiveDuration;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    // Do i even need these?
    @Override
    public double getAverageTotalDamage() {
        return getAverageDPS() * effectiveDuration;
    }

    @Override
    public double getSlowUtilityPerEnemy() {
        // This should evaluate to 0 unless the movespeed multiplier has been set.
        return (1.0 - movespeedMultiplier) * effectiveDuration;
    }

    public String prettyPrint(){
        return prettyPrint(0);
    }
    public String prettyPrint(int indentLevel) {
        String indent = "    ";
        String toReturn = "";
        if (inflictsDamage()) {
            toReturn += indent.repeat(indentLevel) + "Deals " + getAverageDPS() + " " + DamageElements.prettyPrint(getDamageElement()) + " DPS\n";
        }

        if (inflictsTemperature(DamageElement.heat)) {
            toReturn += indent.repeat(indentLevel) + "Deals " + getAverageTemperaturePerSecond(DamageElement.heat) + " Heat/sec\n";
        }

        if (inflictsTemperature(DamageElement.cold)) {
            toReturn += indent.repeat(indentLevel) + "Deals " + getAverageTemperaturePerSecond(DamageElement.cold) + " Cold/sec\n";
        }

        if (inflictsSlow()) {
            toReturn += indent.repeat(indentLevel) + "Slows by " + 100.0*(1.0 - movespeedMultiplier) + "%\n";
        }

        toReturn += indent.repeat(indentLevel) + "For " + getEffectiveDuration() + " seconds\n";
        toReturn += indent.repeat(indentLevel) + "In a distance of " + distanceAffected + " meters\n";

        return toReturn;
    }
}
