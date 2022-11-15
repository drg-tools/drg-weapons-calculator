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

// TODO: I really think that these three methods can be combined and simplified somehow.
// But for just getting it out the door, this will do.
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

        /*
            By sheer luck, all of the Status Effects that I can think of that apply Heat ALSO start right away
            (either 100% chance to apply, or AoE). As a result of that good luck, I can just apply their Heat/sec
            constantly and right away to calculate Time to Ignite.
        */
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
            // TODO: should this be extended longer than the normal duration?
            double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
            double totalHeatPerSec = totalHeatPerHit * rateOfFire + stesHeatPerSec;

            // Instant ignition on the first hit
            if (totalHeatPerHit > temperatureComp.getEffectiveBurnTemperature()) {
                allStes.add(new PushSTEComponent(0, new STE_OnFire(burnDuration)));
            }
            // Ignition across time. Check to make sure that the total Heat/sec > Cooling Rate. If not, then it will never ignite. (PGL Incendiary vs Oppressor comes to mind)
            else if (totalHeatPerSec > temperatureComp.getCoolingRate()){
                double timeToIgnite;
                // First, check if the weapon can fully ignite the enemy in less than 1 sec (the default interval for CoolingRate, only Bulk Detonators use 0.25)
                if (totalHeatPerHit * Math.floor(0.99 * rateOfFire) + stesHeatPerSec >= temperatureComp.getEffectiveBurnTemperature()) {
                    timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / totalHeatPerSec;
                }
                // If not, then this has to account for the Cooling Rate increasing the number of shots required.
                else {
                    timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / (totalHeatPerSec - temperatureComp.getCoolingRate());
                }
                allStes.add(new PushSTEComponent(timeToIgnite, new STE_OnFire(burnDuration)));
            }
            // implicit "else { don't add STE_OnFire }"
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
        int breakpointCounter = 0;

        MaterialFlag breakpointMaterialFlag;
        if (targetIsFrozen) {
            breakpointMaterialFlag = MaterialFlag.frozen;
        }
        else {
            breakpointMaterialFlag = MaterialFlag.weakpoint;
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
        double totalDamagePerHitOnArmorHealth = 0;
        double totalArmorDamageDealtPerDirectHit = 0;
        boolean atLeastOneDamageComponentHasABGreaterThan100 = false;
        boolean atLeastOneDamageComponentDoesHeat = false;
        double totalHeatPerHit = 0;
        ArrayList<PushSTEComponent> allStes = new ArrayList<>();
        for (int i = 0; i < damagePerHit.length; i++) {
            totalDamagePerHit += damagePerHit[i].getTotalComplicatedDamageDealtPerHit(
                breakpointMaterialFlag,
                resistances,
                targetIsAffectedByIFG,
                target.getWeakpointMultiplier(),
                1
            );
            totalDamagePerHitOnArmorHealth += damagePerHit[i].getTotalComplicatedDamageDealtPerHit(
                MaterialFlag.armor,
                resistances,
                targetIsAffectedByIFG,
                1,
                0  // Setting ArmorReduction to x0 because this is modeling a Heavy Armor plate that uses ArmorHealth
            );

            totalArmorDamageDealtPerDirectHit += damagePerHit[i].getArmorDamagePerDirectHit(resistances);
            if (damagePerHit[i].armorBreakingIsGreaterThan100Percent()) {
                atLeastOneDamageComponentHasABGreaterThan100 = true;
            }

            allStes.addAll(damagePerHit[i].getStatusEffectsApplied());
            if (damagePerHit[i].appliesTemperature(TemperatureElement.heat)) {
                atLeastOneDamageComponentDoesHeat = true;
                totalHeatPerHit += damagePerHit[i].getTemperatureDealtPerHit(TemperatureElement.heat);
            }
        }

        /*
            By sheer luck, all of the Status Effects that I can think of that apply Heat ALSO start right away
            (either 100% chance to apply, or AoE). As a result of that good luck, I can just apply their Heat/sec
            constantly and right away to calculate Time to Ignite.
        */
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
            // TODO: should this be extended longer than the normal duration?
            double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
            double totalHeatPerSec = totalHeatPerHit * rateOfFire + stesHeatPerSec;

            // Instant ignition on the first hit
            if (totalHeatPerHit > temperatureComp.getEffectiveBurnTemperature()) {
                allStes.add(new PushSTEComponent(0, new STE_OnFire(burnDuration)));
            }
            // Ignition across time. Check to make sure that the total Heat/sec > Cooling Rate. If not, then it will never ignite. (PGL Incendiary vs Oppressor comes to mind)
            else if (totalHeatPerSec > temperatureComp.getCoolingRate()){
                double timeToIgnite;
                // First, check if the weapon can fully ignite the enemy in less than 1 sec (the default interval for CoolingRate, only Bulk Detonators use 0.25)
                if (totalHeatPerHit * Math.floor(0.99 * rateOfFire) + stesHeatPerSec >= temperatureComp.getEffectiveBurnTemperature()) {
                    timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / totalHeatPerSec;
                }
                // If not, then this has to account for the Cooling Rate increasing the number of shots required.
                else {
                    timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / (totalHeatPerSec - temperatureComp.getCoolingRate());
                }
                allStes.add(new PushSTEComponent(timeToIgnite, new STE_OnFire(burnDuration)));
            }
            // implicit "else { don't add STE_OnFire }"
        }

        MultipleSTEs allStatusEffects = new MultipleSTEs(allStes);
        // It's necessary to call this method right after instantiation because during construction it fully evaluates
        // to calculate max damage and cumulative slows.
        allStatusEffects.resetTimeElapsed();

        double heavyArmorHP;
        int numShotsToBreakArmor;
        if (target.weakpointIsCoveredByHeavyArmor()) {
            heavyArmorHP = target.getArmorBaseHealth() * normalScalingResistance;
            numShotsToBreakArmor = (int) Math.ceil(heavyArmorHP / totalArmorDamageDealtPerDirectHit);
        }
        else {
            heavyArmorHP = 0;
            numShotsToBreakArmor = 0;
        }

        double fourSecondsDoTDamage;
        while(effectiveHP > 0) {
            breakpointCounter++;

            // 1. Subtract the damage dealt on hit
            if (!targetIsFrozen && heavyArmorHP > 0){
                // heavyArmorHP > 0 will only evaluate to True when this is modeling an ArmorHealth plate covering the Weakpoint
                // If the ArmorHealth plate covering the Weakpoint has been broken, do full damage.
                if ((atLeastOneDamageComponentHasABGreaterThan100 && breakpointCounter >= numShotsToBreakArmor) || (!atLeastOneDamageComponentHasABGreaterThan100 && breakpointCounter > numShotsToBreakArmor)) {
                    effectiveHP -= totalDamagePerHit;
                }
                else {
                    effectiveHP -= totalDamagePerHitOnArmorHealth;
                }
            }
            // Either the target is Frozen, or it hit a Weakpoint not covered by an armor plate. Switching the MaterialFlag way at the top of this method accounts for either option.
            else {
                effectiveHP -= totalDamagePerHit;
            }

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

    private int calculateLightArmorBreakpoint() {
        int breakpointCounter = 0;

        MaterialFlag breakpointMaterialFlag;
        if (targetIsFrozen) {
            breakpointMaterialFlag = MaterialFlag.frozen;
        }
        else {
            breakpointMaterialFlag = MaterialFlag.armor;
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

        double totalDamagePerHitBeforeBreakingArmor = 0;
        double totalDamagePerHitAfterBreakingArmor = 0;
        double totalArmorDamageDealtPerDirectHit = 0;
        boolean atLeastOneDamageComponentHasABGreaterThan100 = false;
        boolean atLeastOneDamageComponentDoesHeat = false;
        double totalHeatPerHit = 0;
        ArrayList<PushSTEComponent> allStes = new ArrayList<>();
        for (int i = 0; i < damagePerHit.length; i++) {
            totalDamagePerHitBeforeBreakingArmor += damagePerHit[i].getTotalComplicatedDamageDealtPerHit(
                breakpointMaterialFlag,
                resistances,
                targetIsAffectedByIFG,
                1,
                UtilityInformation.LightArmor_DamageReduction  // TODO: This is where I could implement per-enemy Light Armor multiplier (Q'ronar Youngling)
            );
            totalDamagePerHitAfterBreakingArmor += damagePerHit[i].getTotalComplicatedDamageDealtPerHit(
                breakpointMaterialFlag,
                resistances,
                targetIsAffectedByIFG,
                1,
                1
            );

            totalArmorDamageDealtPerDirectHit += damagePerHit[i].getArmorDamagePerDirectHit(resistances);
            if (damagePerHit[i].armorBreakingIsGreaterThan100Percent()) {
                atLeastOneDamageComponentHasABGreaterThan100 = true;
            }

            allStes.addAll(damagePerHit[i].getStatusEffectsApplied());
            if (damagePerHit[i].appliesTemperature(TemperatureElement.heat)) {
                atLeastOneDamageComponentDoesHeat = true;
                totalHeatPerHit += damagePerHit[i].getTemperatureDealtPerHit(TemperatureElement.heat);
            }
        }

        /*
            By sheer luck, all of the Status Effects that I can think of that apply Heat ALSO start right away
            (either 100% chance to apply, or AoE). As a result of that good luck, I can just apply their Heat/sec
            constantly and right away to calculate Time to Ignite.
        */
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
            // TODO: should this be extended longer than the normal duration?
            double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
            double totalHeatPerSec = totalHeatPerHit * rateOfFire + stesHeatPerSec;

            // Instant ignition on the first hit
            if (totalHeatPerHit > temperatureComp.getEffectiveBurnTemperature()) {
                allStes.add(new PushSTEComponent(0, new STE_OnFire(burnDuration)));
            }
            // Ignition across time. Check to make sure that the total Heat/sec > Cooling Rate. If not, then it will never ignite. (PGL Incendiary vs Oppressor comes to mind)
            else if (totalHeatPerSec > temperatureComp.getCoolingRate()){
                double timeToIgnite;
                // First, check if the weapon can fully ignite the enemy in less than 1 sec (the default interval for CoolingRate, only Bulk Detonators use 0.25)
                if (totalHeatPerHit * Math.floor(0.99 * rateOfFire) + stesHeatPerSec >= temperatureComp.getEffectiveBurnTemperature()) {
                    timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / totalHeatPerSec;
                }
                // If not, then this has to account for the Cooling Rate increasing the number of shots required.
                else {
                    timeToIgnite = temperatureComp.getEffectiveBurnTemperature() / (totalHeatPerSec - temperatureComp.getCoolingRate());
                }
                allStes.add(new PushSTEComponent(timeToIgnite, new STE_OnFire(burnDuration)));
            }
            // implicit "else { don't add STE_OnFire }"
        }

        MultipleSTEs allStatusEffects = new MultipleSTEs(allStes);
        // It's necessary to call this method right after instantiation because during construction it fully evaluates
        // to calculate max damage and cumulative slows.
        allStatusEffects.resetTimeElapsed();

        // Because this breakpoint will only be calculated when target.hasLightArmor() is true, it's safe to fetch the ArmorStrength value like this.
        double probabilityToBreakArmorStrengthPlate = probabilityToBreakArmorStrengthPlate(totalArmorDamageDealtPerDirectHit, target.getArmorStrength());
        int numberOfShotsToBreakLightArmor = (int) Math.ceil(MathUtils.meanRolls(probabilityToBreakArmorStrengthPlate));

        double fourSecondsDoTDamage;
        while(effectiveHP > 0) {
            breakpointCounter++;

            // 1. Subtract the damage dealt on hit
            if (atLeastOneDamageComponentHasABGreaterThan100 && breakpointCounter >= numberOfShotsToBreakLightArmor) {
                effectiveHP -= totalDamagePerHitAfterBreakingArmor;
            }
            else if (!atLeastOneDamageComponentHasABGreaterThan100 && breakpointCounter > numberOfShotsToBreakLightArmor) {
                effectiveHP -= totalDamagePerHitAfterBreakingArmor;
            }
            else {
                effectiveHP -= totalDamagePerHitBeforeBreakingArmor;
            }

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

    // TODO: update EnemyInformation's version of this method, and then replace it later.
    // This method assumes that Armor Breaking and Damage Flags have already been factored in for the first argument.
    private double probabilityToBreakArmorStrengthPlate(double totalArmorDamageOnDirectHit, double armorStrength) {
        double lookupValue = totalArmorDamageOnDirectHit / armorStrength;

        if (lookupValue < 1.0) {
            return lookupValue / 2.0;
        }
        else if (lookupValue < 2.0) {
            return 0.5 + (lookupValue - 1.0) / 4.0;
        }
        else if (lookupValue < 4.0) {
            return 0.75 + (lookupValue - 2.0) / 8.0;
        }
        else {
            return 1.0;
        }
    }
}
