package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;

// For things like Persistent Plasma, Fat Boy, Sticky Flames, Coilgun trail, etc. Any STE that has a short duration but
// gets re-applied frequently as long as enemies stay within the Area of Effect
public class AoEStatusEffect extends StatusEffect {
    private double maxDuration;
    private double effectiveDuration;

    public AoEStatusEffect(double areaEffectDistanceMeters, DamageElement dmgElement, double minDmg, double maxDmg,
                           double minInterval, double maxInterval, double slowMultiplier, double dur, double maxDur) {
        super(dmgElement, minDmg, maxDmg, minInterval, maxInterval, slowMultiplier, dur);
        double timeItTakesAverageCreatureToTraverseDistance = areaEffectDistanceMeters / (EnemyInformation.averageMovespeed() * slowMultiplier);
        maxDuration = maxDur;
        effectiveDuration = Math.min(Math.ceil(timeItTakesAverageCreatureToTraverseDistance / dur) * dur, maxDur + dur);
    }

    public AoEStatusEffect(double areaEffectDistanceMeters, DamageElement dmgElement, double minDmg, double maxDmg,
                           TemperatureElement tmpElement, double minTemp, double maxTemp,
                           double minInterval, double maxInterval, double slowMultiplier, double dur, double maxDur) {
        super(dmgElement, minDmg, maxDmg, tmpElement, minTemp, maxTemp, minInterval, maxInterval, slowMultiplier, dur);
        double timeItTakesAverageCreatureToTraverseDistance = areaEffectDistanceMeters / (EnemyInformation.averageMovespeed() * slowMultiplier);
        maxDuration = maxDur;
        effectiveDuration = Math.min(Math.ceil(timeItTakesAverageCreatureToTraverseDistance / dur) * dur, maxDur + dur);
    }

    public double getMaxDuration() {
        return maxDuration;
    }
    public double getEffectiveDuration() {
        return effectiveDuration;
    }
}
