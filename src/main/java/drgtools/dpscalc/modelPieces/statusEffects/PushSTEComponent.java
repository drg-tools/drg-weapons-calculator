package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.utilities.MathUtils;

public class PushSTEComponent implements Comparable<PushSTEComponent> {
    private double chanceToInflict;
    private StatusEffect stePushed;

    public PushSTEComponent(double procChance, StatusEffect ste){
        chanceToInflict = procChance;
        stePushed = ste;
    }

    public void setChanceToInflict(double newProcChance) {
        chanceToInflict = newProcChance;
    }

    public double getSlowUtilityPerEnemy() {
        return chanceToInflict * stePushed.getSlowUtilityPerEnemy();
    }

    public int avgNumHitsExpectedBeforeProc() {
        return (int) Math.ceil(MathUtils.meanRolls(chanceToInflict));
    }

    public void setSTE(StatusEffect ste) {
        stePushed = ste;
    }
    public StatusEffect getSTE() {
        return stePushed;
    }

    @Override
    public int compareTo(PushSTEComponent other) {
        return Double.compare(this.getSTE().getComparedDuration(), other.getSTE().getComparedDuration());
    }
}
