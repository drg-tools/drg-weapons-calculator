package drgtools.dpscalc.modelPieces.statusEffects;

import drgtools.dpscalc.utilities.MathUtils;

public class PushSTEComponent {
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

    public StatusEffect getSTE() {
        return stePushed;
    }
}
