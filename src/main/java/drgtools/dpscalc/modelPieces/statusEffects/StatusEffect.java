package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.temperatureElement;

public class StatusEffect {
    protected damageElement damagePerTickElement = null;
    protected double minDamagePerTick = 0.0;
    protected double maxDamagePerTick = 0.0;
    protected temperatureElement temperaturePerTickElement = null;
    protected double minTemperaturePerTick = 0.0;
    protected double maxTemperaturePerTick = 0.0;
    protected double minIntervalBetweenTicks = 0.0;
    protected double maxIntervalBetweenTicks = 0.0;
    protected double duration = 0.0;
    protected double movespeedMultiplier = 1.0;

    // Shortcut constructor for a DoT that doesn't have a movespeed slow and doesn't do Heat/Cold
    protected StatusEffect(damageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double dur) {
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        duration = dur;
    }

    // Shortcut constructor for a Slow that doesn't deal damage or Heat/Cold
    protected StatusEffect(double slowMultiplier, double dur) {
        movespeedMultiplier = slowMultiplier;
        duration = dur;
    }

    protected StatusEffect(damageElement dmgElement, double minDmg, double maxDmg,
                           temperatureElement tmpElement, double minTemp, double maxTemp,
                           double minInterval, double maxInterval, double slowMultiplier, double dur) {
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        temperaturePerTickElement = tmpElement;
        minTemperaturePerTick = minTemp;
        maxTemperaturePerTick = maxTemp;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        movespeedMultiplier = slowMultiplier;
        duration = dur;
    }

    public damageElement getDamageElement() {
        return damagePerTickElement;
    }
    public double getAverageDPS() {
        double avgDamagePerTick = (minDamagePerTick + maxDamagePerTick) / 2.0;
        double avgNumTicksPerSec = 2.0 / (minIntervalBetweenTicks + maxIntervalBetweenTicks);
        return avgDamagePerTick * avgNumTicksPerSec;
    }

    public double getSlowUtilityPerEnemy() {
        // This should evaluate to 0 unless the movespeed multiplier has been set.
        return (1.0 - movespeedMultiplier) * duration;
    }

    // TODO: add a toString
}
