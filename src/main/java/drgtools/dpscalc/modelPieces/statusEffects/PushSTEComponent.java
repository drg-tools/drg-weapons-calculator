package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.utilities.MathUtils;

public class PushSTEComponent implements Comparable<PushSTEComponent> {
    private boolean rngChance;
    private double chanceToInflict = 1.0;  // default to 1 to simplify getSlowUtilityPerEnemy() logic
    private double avgNumShotsBeforeProc = 0;
    private double rateOfFire = 0.00001;  // avoid divide-by-zero error, but still return an obviously wrong value.
    private double timeBeforeInflicted = 0;
    private StatusEffect stePushed;
    private double timeElapsed = 0;

    public PushSTEComponent(double RoF, double procChance, StatusEffect ste) {
        rngChance = true;
        stePushed = ste;
        rateOfFire = RoF;
        setChanceToInflict(procChance);
    }

    // For things like STE_OnFire which isn't RNG-based
    public PushSTEComponent(double timeDelay, StatusEffect ste) {
        rngChance = false;
        stePushed = ste;
        timeBeforeInflicted = timeDelay;
    }

    public void setChanceToInflict(double newProcChance) {
        if (rngChance) {
            chanceToInflict = newProcChance;
            avgNumShotsBeforeProc = Math.ceil(MathUtils.meanRolls(chanceToInflict));
            timeBeforeInflicted = (avgNumShotsBeforeProc - 1) / rateOfFire;
        }
    }
    public void setRoF(double newRoF) {
        if (rngChance) {
            rateOfFire = newRoF;
            timeBeforeInflicted = (avgNumShotsBeforeProc - 1) / rateOfFire;
        }
    }

    public double getSlowUtilityPerEnemy() {
        return chanceToInflict * stePushed.getSlowUtilityPerEnemy();
    }

    // TODO: Does this need to exist?
    public double avgNumShotsExpectedBeforeProc() {
        return avgNumShotsBeforeProc;
    }

//    public void setSTE(StatusEffect ste) {
//        stePushed = ste;
//    }
    public StatusEffect getSTE() {
        return stePushed;
    }

    // All of this stuff is being configured so that MultipleSTEs can emulate real-time passage for Breakpoints insanity.
    public void updateEffectiveDuration(double slowMultiplier) {
        if (stePushed instanceof AoEStatusEffect) {
            ((AoEStatusEffect) stePushed).calculateEffectiveDuration(slowMultiplier);
        }
    }
    public void resetTimeElapsed() {
        timeElapsed = 0;
    }
    public void progressTime(double secondsElapsed) {
        timeElapsed += secondsElapsed;
    }
    private double getComparedDuration() {
        if (stePushed instanceof AoEStatusEffect) {
            return ((AoEStatusEffect) stePushed).getEffectiveDuration();
        }
        else {
            return stePushed.getDuration();
        }
    }
    public boolean isActive() {
        return timeElapsed >= timeBeforeInflicted && timeElapsed <= timeBeforeInflicted + getComparedDuration();
    }
    public double getSTEComparedDurationPlusDelay() {
        return timeBeforeInflicted + getComparedDuration();
    }
    public double predictDamageDealtInNextTimeInterval(double secondsToPredict) {
        // Early exit: if the STE does no damage, this doesn't need to be evaluated
        if (!stePushed.inflictsDamage()) {
            return 0;
        }

        // Base cases: either the DoT hasn't started yet (and won't in the predicted interval), or it's already finished
        if ((timeElapsed + secondsToPredict < timeBeforeInflicted) || (timeElapsed > timeBeforeInflicted + getComparedDuration())) {
            return 0;
        }
        else {
            // Don't let this prediction artificially extend the DoT duration past when it's supposed to end!
            double secondsActiveDuringPrediction = Math.min(secondsToPredict, (timeBeforeInflicted + getComparedDuration()) - timeElapsed);

            // If the prediction starts before the DoT is active, subtract the offset from the duration so that it doesn't over-estimate.
            if (timeElapsed < timeBeforeInflicted) {
                secondsActiveDuringPrediction -= (timeBeforeInflicted - timeElapsed);
            }

            // TODO: if possible, make sure this value honors the "damage tick applied instantly" thing. it doesn't feel possible right now, but we'll see.
            // If I did the logic of this right, I think that this can use timeElapsed = 0 and secondsToPredict = 10000000,
            // and it would still only return the max duration of the DoT * avg DPS... I hope.
            return stePushed.getAverageDPS() * secondsActiveDuringPrediction;
        }
    }

    @Override
    public int compareTo(PushSTEComponent other) {
        return Double.compare(this.getSTEComparedDurationPlusDelay(), other.getSTEComparedDurationPlusDelay());
    }
}
