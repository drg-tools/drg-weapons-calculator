package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.temperatureElement;
import drgtools.dpscalc.modelPieces.temperature.EnvironmentalTemperature;

public class StatusEffect {
    protected damageElement damagePerTickElement = null;
    protected double minDamagePerTick = 0.0;
    protected double maxDamagePerTick = 0.0;

    protected boolean canDamageArmor = false;
    protected double minArmorDamagePerTick = 0.0;
    protected double maxArmorDamagePerTick = 0.0;

    protected temperatureElement temperaturePerTickElement = null;
    protected double minTemperaturePerTick = 0.0;
    protected double maxTemperaturePerTick = 0.0;
    protected EnvironmentalTemperature envTemp = null;

    // TODO: find the default tickrate values
    protected double minIntervalBetweenTicks = 0.0;
    protected double maxIntervalBetweenTicks = 0.0;
    /*
        Guess: if duration isn't set, maybe it defaults to 1 sec?
        STE_StickyFlame_Slowdown
        STE_FatBoyRadiation
        STE_TurretArc
        STE_NeuroLasso
    */
    protected double duration = 0.0;
    protected double movespeedMultiplier = 1.0;

    protected boolean effectsStackWithMultipleApplications = false;
    protected boolean canHaveDurationRefreshedWhileStillActive = false;

    // Shortcut constructor for a DoT that doesn't have a movespeed slow and doesn't do Heat/Cold
    protected StatusEffect(damageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double dur) {
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        duration = dur;
    }

    // Shortcut constructor for a DoT that has a movespeed slow and doesn't do Heat/Cold
    protected StatusEffect(damageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double slowMultiplier, double dur) {
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        movespeedMultiplier = slowMultiplier;
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

    public void setDamagePerTick(double newMin, double newMax) {
        minDamagePerTick = newMin;
        maxDamagePerTick = newMax;
    }
    public void setMovespeedMultiplier(double in) {
        movespeedMultiplier = in;
    }
    public void setDuration(double in) {
        duration = in;
    }

    public damageElement getDamageElement() {
        return damagePerTickElement;
    }
    public double getAverageDPS() {
        return (minDamagePerTick + maxDamagePerTick) * (minIntervalBetweenTicks + maxIntervalBetweenTicks);
    }
    public double getAverageTotalDamage() {
        // Reminder to self: DoTs do their first tick of damage instantly. There's something weird about Inferno's DoT
        // that makes it do an extra tick when it ends too or something, for 11 ticks instead of 10
        // TODO
        return getAverageDPS() * duration;
    }

    public double getArmorBreakUtilityPerEnemy() {
        if (canDamageArmor) {
            return EnemyInformation.lightArmorBreakProbabilityLookup((minArmorDamagePerTick + maxArmorDamagePerTick) / 2.0,
                    1.0, EnemyInformation.averageLightArmorStrength());
        }
        else {
            return 0;
        }
    }
    public double getSlowUtilityPerEnemy() {
        // This should evaluate to 0 unless the movespeed multiplier has been set.
        return (1.0 - movespeedMultiplier) * duration;
    }

    // TODO: add a toString
}
