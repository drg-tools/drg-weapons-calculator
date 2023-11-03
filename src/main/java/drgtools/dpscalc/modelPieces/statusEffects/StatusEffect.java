package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.temperature.EnvironmentalTemperature;

public abstract class StatusEffect {
    protected DamageElement damagePerTickElement = null;
    protected double minDamagePerTick = 0.0;
    protected double maxDamagePerTick = 0.0;

    protected boolean canDamageArmor = false;
    protected double minArmorDamagePerTick = 0.0;
    protected double maxArmorDamagePerTick = 0.0;

    protected DamageElement temperaturePerTickElement = null;
    protected double minTemperaturePerTick = 0.0;
    protected double maxTemperaturePerTick = 0.0;
    protected EnvironmentalTemperature envTemp = null;

    // TODO: find the default tickrate values and default duration
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
    // protected double comparedDuration;

    protected boolean effectsStackWithMultipleApplications = false;
    protected boolean canHaveDurationRefreshedWhileStillActive = false;

    // Shortcut constructor for a DoT that only does damage; it doesn't have a movespeed slow and doesn't do Heat/Cold
    protected StatusEffect(DamageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double dur) {
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        duration = dur;
        // comparedDuration = duration;
    }

    // Shortcut constructor for a DoT that does damage and has a movespeed slow, but doesn't do Heat/Cold
    protected StatusEffect(DamageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double slowMultiplier, double dur) {
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        movespeedMultiplier = slowMultiplier;
        duration = dur;
        // comparedDuration = duration;
    }

    // Shortcut constructor for a Slow that doesn't deal damage or Heat/Cold
    protected StatusEffect(double slowMultiplier, double dur) {
        movespeedMultiplier = slowMultiplier;
        duration = dur;
        // comparedDuration = duration;
    }

    protected StatusEffect(DamageElement dmgElement, double minDmg, double maxDmg,
                           DamageElement tmpElement, double minTemp, double maxTemp,
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
        // comparedDuration = duration;
    }

    public void setDamagePerTick(double newMin, double newMax) {
        minDamagePerTick = newMin;
        maxDamagePerTick = newMax;
    }
    public double getMovespeedMultiplier() {
        return movespeedMultiplier;
    }
    public void setMovespeedMultiplier(double in) {
        movespeedMultiplier = in;
    }
    public double getDuration() {
        return duration;
    }
    public void setDuration(double in) {
        duration = in;
    }

    public boolean inflictsDamage() {
        return damagePerTickElement != null && minDamagePerTick > 0 && maxDamagePerTick > 0;
    }
    public DamageElement getDamageElement() {
        return damagePerTickElement;
    }
    public double getAverageDPS() {
        return (minDamagePerTick + maxDamagePerTick) * (minIntervalBetweenTicks + maxIntervalBetweenTicks);
    }
    public double getAverageTotalDamage() {
        // Reminder to self: DoTs do their first tick of damage instantly. There's something weird about Inferno's DoT
        // that makes it do an extra tick when it ends too or something, for 11 ticks instead of 10
        // TODO: write a more exact formula than this quick-and-dirty approximation
        return getAverageDPS() * duration;
    }

    public double getArmorBreakUtilityPerEnemy() {
        if (canDamageArmor) {
            return Enemy.armorStrengthBreakProbabilityLookup((minArmorDamagePerTick + maxArmorDamagePerTick) / 2.0, EnemyInformation.averageLightArmorStrength());
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
    public boolean inflictsTemperature(DamageElement desiredTemp) {
        switch(desiredTemp) {
            case heat: {
                return temperaturePerTickElement == DamageElement.heat || envTemp.getTempElement() == DamageElement.heat;
            }
            case cold: {
                return temperaturePerTickElement == DamageElement.cold || envTemp.getTempElement() == DamageElement.cold;
            }
            default: {
                return false;
            }
        }
    }

    public double getAverageTemperaturePerSecond(DamageElement desiredTemp) {
        double toReturn = 0;
        if (temperaturePerTickElement == desiredTemp) {
            toReturn += (minTemperaturePerTick + maxTemperaturePerTick) * (minIntervalBetweenTicks + maxIntervalBetweenTicks);
        }
        if (envTemp.getTempElement() == desiredTemp) {
            toReturn += envTemp.getTemperatureChangePerSec();
        }
        return toReturn;
    }

    public boolean inflictsSlow() {
        return movespeedMultiplier < 1.0;
    }
}
