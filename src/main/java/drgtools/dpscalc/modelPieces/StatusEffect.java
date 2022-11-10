package drgtools.dpscalc.modelPieces;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;

public class StatusEffect {
    protected double chanceToInflict;
    protected damageElement damagePerTickElement = null;
    protected double minDamagePerTick = 0.0;
    protected double maxDamagePerTick = 0.0;
    protected double minIntervalBetweenTicks = 0.0;
    protected double maxIntervalBetweenTicks = 0.0;
    protected double duration = 0.0;
    protected double movespeedMultiplier = 1.0;

    // Shortcut constructor for a DoT that doesn't have a movespeed slow
    protected StatusEffect(double procChance, damageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double dur) {
        chanceToInflict = procChance;
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        duration = dur;
    }

    // Shortcut constructor for a Slow that doesn't deal damage
    protected StatusEffect(double procChance, double slowMultiplier, double dur) {
        chanceToInflict = procChance;
        movespeedMultiplier = slowMultiplier;
        duration = dur;
    }

    protected StatusEffect(double procChance, damageElement dmgElement, double minDmg, double maxDmg, double minInterval, double maxInterval, double slowMultiplier, double dur) {
        chanceToInflict = procChance;
        damagePerTickElement = dmgElement;
        minDamagePerTick = minDmg;
        maxDamagePerTick = maxDmg;
        minIntervalBetweenTicks = minInterval;
        maxIntervalBetweenTicks = maxInterval;
        movespeedMultiplier = slowMultiplier;
        duration = dur;
    }

    // Technically speaking, this value gets set in the DamageComponents. But for my own sake while programming this, it makes sense to store it with the STE instead.
    // TODO: This might get refactored later?
    public void overrideChanceToInflict(double newProcChance) {
        chanceToInflict = newProcChance;
    }

    public damageElement getDamageElement() {
        return damagePerTickElement;
    }
    public double getAverageDPS() {
        double avgDamagePerTick = (minDamagePerTick + maxDamagePerTick) / 2.0;
        double avgNumTicksPerSec = 2.0 / (minIntervalBetweenTicks + maxIntervalBetweenTicks);
        return avgDamagePerTick * avgNumTicksPerSec;
    }

    public double getUtilityPerEnemy() {
        return chanceToInflict * (1.0 - movespeedMultiplier) * duration;
    }

    // TODO: add a toString
}
