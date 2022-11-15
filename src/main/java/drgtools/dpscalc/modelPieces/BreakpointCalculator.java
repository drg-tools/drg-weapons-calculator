package drgtools.dpscalc.modelPieces;

import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.statusEffects.MultipleSTEs;
import drgtools.dpscalc.modelPieces.statusEffects.PushSTEComponent;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.STE_OnFire;

import java.util.ArrayList;

public class BreakpointCalculator {
    private DamageComponent[] damagePerHit;
    private double rateOfFire;

    private Enemy target;
    private boolean targetIsAffectedByIFG;
    private boolean targetIsFrozen;

    // Difficulty Scaling Resistance numbers which are private to EnemyInformation, so they have to be copied here.
    private double normalScalingResistance;
    private double largeScalingResistance;

    public BreakpointCalculator(DamageComponent[] dmgPerHit, double RoF, Enemy enmy, boolean IFG, boolean frozen, double normalScaling, double largeScaling) {
        damagePerHit = dmgPerHit;
        rateOfFire = RoF;
        target = enmy;
        targetIsAffectedByIFG = IFG;
        targetIsFrozen = frozen;
        normalScalingResistance = normalScaling;
        largeScalingResistance = largeScaling;
    }

    public ArrayList<Integer> getBreakpoints() {
        ArrayList<Integer> toReturn = new ArrayList<>();
        if (target.hasExposedBodySomewhere()) {
            toReturn.add(calculateNormalFleshBreakpoint());
        }
        if (target.hasLightArmor()) {
            toReturn.add(calculateLightArmorBreakpoint());
        }
        if (target.hasWeakpoint()) {
            toReturn.add(calculateWeakpointBreakpoint());
        }
        return toReturn;
    }

    private int calculateNormalFleshBreakpoint() {
        int breakpointCounter = 0;

        MaterialFlag breakpointMaterialFlag;
        if (targetIsFrozen) {
            breakpointMaterialFlag = MaterialFlag.frozen;
        }
        else {
            breakpointMaterialFlag = MaterialFlag.normalFlesh;
        }

        double effectiveHP;
        if (target.usesNormalScaling()) {
            effectiveHP = target.getBaseHealth() * normalScalingResistance;
        }
        else {
            effectiveHP = target.getBaseHealth() * largeScalingResistance;
        }

        CreatureTemperatureComponent temperatureComp = target.getTemperatureComponent();
        ElementalResistancesArray resistances = target.getElementalResistances();

        double totalDamagePerHit = 0;
        boolean atLeastOneDamageComponentDoesHeat = false;
        double totalHeatPerHit = 0;
        ArrayList<PushSTEComponent> allStes = new ArrayList<>();
        for (int i = 0; i < damagePerHit.length; i++) {
            totalDamagePerHit += damagePerHit[i].getTotalComplicatedDamageDealtPerHit(
                breakpointMaterialFlag,
                resistances,
                targetIsAffectedByIFG,
                1,
                1
            );
            allStes.addAll(damagePerHit[i].getStatusEffectsApplied());
            if (damagePerHit[i].appliesTemperature(TemperatureElement.heat)) {
                atLeastOneDamageComponentDoesHeat = true;
                totalHeatPerHit += damagePerHit[i].getTemperatureDealtPerHit(TemperatureElement.heat);
            }
        }

        // By sheer luck, all of the Status Effects that I can think of that apply Heat ALSO start right away (100%
        // chance to apply, or AoE). As a result of that good luck, I can just apply their Heat/sec constantly to
        // calculate Time to Ignite.
        boolean atLeastOneSteAppliesHeat = false;
        double stesHeatPerSec = 0;
        for (PushSTEComponent pstec: allStes) {
            if (pstec.getSTE().inflictsTemperature(TemperatureElement.heat)) {
                atLeastOneSteAppliesHeat = true;
                stesHeatPerSec += pstec.getSTE().getAverageTemperaturePerSecond(TemperatureElement.heat);
            }
        }

        // Check for Heat/shot or Heat/sec stuff to see if this needs to add STE_OnFire into the mix.
        if (!targetIsFrozen && (atLeastOneDamageComponentDoesHeat || atLeastOneSteAppliesHeat)) {
            double totalHeatPerSec = totalHeatPerHit * rateOfFire + stesHeatPerSec;
            double timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / totalHeatPerSec;
            // TODO: should this be extended longer than the normal duration?
            double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
            allStes.add(new PushSTEComponent(timeToIgnite, new STE_OnFire(burnDuration)));
        }

        MultipleSTEs allStatusEffects = new MultipleSTEs(allStes);
        // It's necessary to call this method right after instantiation because during construction it fully evaluates
        // to calculate max damage and cumulative slows.
        allStatusEffects.resetTimeElapsed();

        double fourSecondsDoTDamage;
        while(effectiveHP > 0) {
            breakpointCounter++;

            // 1. Subtract the damage dealt on hit
            effectiveHP -= totalDamagePerHit;

            // 2. Check if the next 4 seconds of DoT damage will kill the creature.
            fourSecondsDoTDamage = allStatusEffects.predictResistedDamageDealtInNextTimeInterval(4.0, resistances);
            if (fourSecondsDoTDamage >= effectiveHP) {
                break;
            }

            // 3. If not, subtract 1/RoF seconds' worth of DoT Damage and increment all STEs by 1/RoF seconds
            effectiveHP -= allStatusEffects.predictResistedDamageDealtInNextTimeInterval(1.0 / rateOfFire, resistances);
            allStatusEffects.progressTime(1.0 / rateOfFire);

            // Do some rounding because double operations are tricky
            effectiveHP = MathUtils.round(effectiveHP, 4);
        }

        return breakpointCounter;
    }

    private int calculateWeakpointBreakpoint() {
        return 0;
    }

    private int calculateLightArmorBreakpoint() {
        return 0;
    }
}
