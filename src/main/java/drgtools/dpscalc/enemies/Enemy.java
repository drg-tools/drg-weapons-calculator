package drgtools.dpscalc.enemies;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.DamageFlag;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageInstance;
import drgtools.dpscalc.modelPieces.statusEffects.MultipleSTEs;
import drgtools.dpscalc.modelPieces.statusEffects.PushSTEComponent;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.STE_OnFire;

import java.util.ArrayList;

// TODO: Technically, this could model certain enemies' Stun Duration multiplier and Stun Immunity windows too. But for now, they're not implemented.
public abstract class Enemy {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	// These are the values that I guessed for the proportion of each enemy spawn type. It worked REALLY well for avg TTK-based mods like Cold as the Grave and Battle Cool, but it's not representative of the actual game.
	// All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	protected double guessedSpawnProbability;
	/* 
		When U33 introduced the Tri-Jaw and Brundle common enemies, I had to redo these probabilities. To that end I chose to write down what the current kill counter was for every enemy type,
		and then play vanilla Haz4/5 until I achieved at least 15,000 Grunt kills. In the end it took me about 50 hours of playtime to achieve that, and I ended up with a total of 33,606 kills 
		of all kinds for these probability amounts. It's not as broad as U31's 153,000 kills from 6 players, but I didn't want to ask people to go 50 hours of playtime only on vanilla Haz4/5.
		
		Biome-specific enemies, "hatchling" enemy types, and Dreadnoughts not included.
		All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	*/
	protected double exactSpawnProbability;
	
	// Only the largest/tankiest enemies have this set to false.
	protected boolean calculateBreakpoints = true;
	
	protected String enemyName;
	protected boolean macteraType = false;  // Used for Subata T5.B
	// These base values are just taken from the Wiki's default values; Hazard level and player count not factored in. (effectively Haz2, 4 players)
	protected double baseHealth;
	protected boolean normalScaling;
	
	// a.k.a. you can shoot this enemy somewhere that isn't covered by Armor and isn't a Weakpoint (used in Breakpoints)
	protected boolean hasExposedBodySomewhere = false;
	
	protected boolean hasWeakpoint = false;
	// These numbers are taken straight from the Wiki
	protected double weakpointMultiplier;
	// These numbers are estimates of what percentage of bullets shot at each enemy type will hit the enemy's weakpoints
	protected double estimatedProbabilityBulletHitsWeakpoint;
	// TODO: this could be a good place to model breakable Weakpoints later?
	
	// Resistance/weakness values taken from Elythnwaen's Spreadsheet
	// If this number is greater than 0, that means that it takes less damage from that particular element.
	// Conversely, if it's less than 0 it takes extra damage from that particular element
	protected ElementalResistancesMap resistances = new ElementalResistancesMap();
	
	// This info comes from Elythnwaen's Temperatures spreadsheet, and many of those values were seeded from MikeGSG giving us the values for the 5 "base" creature types.
	protected CreatureTemperatureComponent temperatureComponent;
	
	// This information extracted via UUU
	protected double courage = 0.0;  // aka "Fear Resistance"
	// Used to determine average regular Fear duration. Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
	// Additionally, all creatures that get Feared have a x1.5 speedboost, except for Oppressor (x2) and Bulk/Crassus/Dread (x1) which can only be feared by Field Medic/SYiH/Bosco Revive
	// Values listed as m/sec groundspeed
	protected double maxMovespeedWhenFeared = 0.0;
	
	protected boolean hasLightArmor = false, hasHeavyArmorRNG = false, hasHeavyArmorHealth = false, heavyArmorCoversWeakpoint = false, hasUnbreakableArmor = false;
	protected double armorStrength = 0.0, armorBaseHealth = 0.0, armorStrengthReduction = UtilityInformation.LightArmor_DamageReduction, armorHealthReduction = 1.0;
	protected double numArmorStrengthPlates = 0, numArmorHealthPlates = 0;  // These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	public double getSpawnProbability(boolean exact) {
		if (exact) {
			return exactSpawnProbability;
		}
		else {
			return guessedSpawnProbability;
		}
	}
	public boolean shouldHaveBreakpointsCalculated() {
		return calculateBreakpoints;
	}
	public String getName() {
		return enemyName;
	}
	public boolean isMacteraType() {
		return macteraType;
	}
	public double getBaseHealth() {
		return baseHealth;
	}
	public boolean usesNormalScaling() {
		return normalScaling;
	}
	public boolean hasExposedBodySomewhere() {
		return hasExposedBodySomewhere;
	}
	public boolean hasWeakpoint() {
		return hasWeakpoint;
	}
	public double getWeakpointMultiplier() {
		if (hasWeakpoint) {
			return weakpointMultiplier;
		}
		else {
			// Returning zero is necessary for some of the vector dot products to return the correct number.
			return 0.0;
		}
	}
	public double getProbabilityBulletHitsWeakpoint() {
		if (hasWeakpoint) {
			return estimatedProbabilityBulletHitsWeakpoint;
		}
		else {
			return 0.0;
		}
	}
	public ElementalResistancesMap getElementalResistances(){
		return resistances;
	}
	public CreatureTemperatureComponent getTemperatureComponent() {
		return temperatureComponent;
	}
	public double getCourage() {
		return courage;
	}
	public double getMaxMovespeedWhenFeared() {
		return maxMovespeedWhenFeared;
	}
	public boolean hasLightArmor() {
		return hasLightArmor;
	}
	public boolean hasHeavyArmorStrength() {
		return hasHeavyArmorRNG;
	}
	public boolean hasHeavyArmorHealth() {
		return hasHeavyArmorHealth;
	}
	public boolean weakpointIsCoveredByHeavyArmor() {
		return heavyArmorCoversWeakpoint;
	}
	public boolean hasUnbreakableArmor() {
		return hasUnbreakableArmor;
	}
	public double getArmorStrength() {
		if (hasLightArmor || hasHeavyArmorRNG) {
			return armorStrength;
		}
		else {
			return 0.0;
		}
	}
	public double getArmorStrengthReduction() {
		if (hasLightArmor || hasHeavyArmorRNG) {
			return armorStrengthReduction;
		}
		else {
			return 1.0;
		}
	}
	public double getNumArmorStrengthPlates() {
		if (hasLightArmor || hasHeavyArmorRNG) {
			return numArmorStrengthPlates;
		}
		else {
			return 0;
		}
	}
	public double getArmorBaseHealth() {
		if (hasHeavyArmorHealth) {
			return armorBaseHealth;
		}
		else {
			return 0.0;
		}
	}
	public double getArmorHealthReduction() {
		if (hasHeavyArmorHealth) {
			return armorHealthReduction;
		}
		else {
			return 0.0;
		}
	}
	public double getNumArmorHealthPlates() {
		if (hasHeavyArmorHealth) {
			return numArmorHealthPlates;
		}
		else {
			return 0;
		}
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// This method gets used in ArmorWasting()
	public boolean hasBreakableArmor() {
		return hasLightArmor || hasHeavyArmorRNG || hasHeavyArmorHealth;
	}
	
	// These methods get used in Weapon.breakpointsExplanation() to override Praetorian's bodyshot and Warden's Weakpoint terms
	public String getBodyshotName() {
		return "";
	}
	public String getWeakpointName() {
		return "Weakpoint";
	}

	// TODO: move Breakpoint, Overkill, and ArmorWasting to here
	public ArrayList<Integer> calculateBreakpoints(DamageInstance dmgInstance, double RoF, boolean IFG, boolean frozen,
												   double normalScaling, double largeScaling) {
		ArrayList<Integer> toReturn = new ArrayList<>();
		if (hasExposedBodySomewhere()) {
			toReturn.add(calculateNormalFleshBreakpoint(dmgInstance, RoF, IFG, frozen, normalScaling, largeScaling));
		}
		if (hasLightArmor()) {
			toReturn.add(calculateLightArmorBreakpoint(dmgInstance, RoF, IFG, frozen, normalScaling, largeScaling));
		}
		if (hasWeakpoint()) {
			toReturn.add(calculateWeakpointBreakpoint(dmgInstance, RoF, IFG, frozen, normalScaling, largeScaling));
		}
		return toReturn;
	}

	// TODO: I really think that these three methods can be combined and simplified somehow.
	// But for just getting it out the door, this will do.
	private int calculateNormalFleshBreakpoint(DamageInstance dmgInstance, double RoF, boolean IFG, boolean frozen,
											   double normalScaling, double largeScaling) {
		int breakpointCounter = 0;

		MaterialFlag breakpointMaterialFlag;
		if (frozen) {
			breakpointMaterialFlag = MaterialFlag.frozen;
		}
		else {
			breakpointMaterialFlag = MaterialFlag.normalFlesh;
		}

		double effectiveHP;
		if (usesNormalScaling()) {
			effectiveHP = getBaseHealth() * normalScaling;
		}
		else {
			effectiveHP = getBaseHealth() * largeScaling;
		}

		CreatureTemperatureComponent temperatureComp = getTemperatureComponent();
		ElementalResistancesMap resistances = getElementalResistances();

		double totalDamagePerHit = 0;
		boolean atLeastOneDamageComponentDoesHeat = false;
		double totalHeatPerHit = 0;
		ArrayList<PushSTEComponent> allStes = new ArrayList<>();
		DamageComponent dmgAlias;
		for (int i = 0; i < dmgInstance.getTotalNumberOfDamageComponents(); i++) {
			dmgAlias = dmgInstance.getDamageComponentAtIndex(i);

			totalDamagePerHit += dmgAlias.getTotalComplicatedDamageDealtPerHit(
					breakpointMaterialFlag,
					resistances,
					IFG,
					1,
					1
			);
			allStes.addAll(dmgAlias.getStatusEffectsApplied());
			if (dmgAlias.appliesTemperature(DamageElement.heat)) {
				atLeastOneDamageComponentDoesHeat = true;
				totalHeatPerHit += dmgAlias.getTemperatureDealtPerDirectHit(DamageElement.heat);
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
			if (pstec.getSTE().inflictsTemperature(DamageElement.heat)) {
				atLeastOneSteAppliesHeat = true;
				stesHeatPerSec += pstec.getSTE().getAverageTemperaturePerSecond(DamageElement.heat);
			}
		}

		// Check for Heat/shot or Heat/sec stuff to see if this needs to add STE_OnFire into the mix.
		if (!frozen && (atLeastOneDamageComponentDoesHeat || atLeastOneSteAppliesHeat)) {
			// TODO: should this be extended longer than the normal duration?
			double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
			double totalHeatPerSec = totalHeatPerHit * RoF + stesHeatPerSec;

			// Instant ignition on the first hit
			if (totalHeatPerHit > temperatureComp.getEffectiveBurnTemperature()) {
				allStes.add(new PushSTEComponent(0, new STE_OnFire(burnDuration)));
			}
			// Ignition across time. Check to make sure that the total Heat/sec > Cooling Rate. If not, then it will never ignite. (PGL Incendiary vs Oppressor comes to mind)
			else if (totalHeatPerSec > temperatureComp.getCoolingRate()){
				double timeToIgnite;
				// First, check if the weapon can fully ignite the enemy in less than 1 sec (the default interval for CoolingRate, only Bulk Detonators use 0.25)
				if (totalHeatPerHit * Math.floor(0.99 * RoF) + stesHeatPerSec >= temperatureComp.getEffectiveBurnTemperature()) {
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
			effectiveHP -= allStatusEffects.predictResistedDamageDealtInNextTimeInterval(1.0 / RoF, resistances);
			allStatusEffects.progressTime(1.0 / RoF);

			// Do some rounding because double operations are tricky
			effectiveHP = MathUtils.round(effectiveHP, 4);
		}

		return breakpointCounter;
	}

	private int calculateWeakpointBreakpoint(DamageInstance dmgInstance, double RoF, boolean IFG, boolean frozen,
											 double normalScaling, double largeScaling) {
		int breakpointCounter = 0;

		MaterialFlag breakpointMaterialFlag, coveringArmorMaterialFlag;
		if (frozen) {
			breakpointMaterialFlag = MaterialFlag.frozen;
			coveringArmorMaterialFlag = MaterialFlag.frozen;
		}
		else {
			breakpointMaterialFlag = MaterialFlag.weakpoint;
			coveringArmorMaterialFlag = MaterialFlag.heavyArmor;
		}

		double effectiveHP;
		if (usesNormalScaling()) {
			effectiveHP = getBaseHealth() * normalScaling;
		}
		else {
			effectiveHP = getBaseHealth() * largeScaling;
		}

		CreatureTemperatureComponent temperatureComp = getTemperatureComponent();
		ElementalResistancesMap resistances = getElementalResistances();

		double totalDamagePerHit = 0;
		double totalDamagePerHitOnHeavyArmor = 0;
		double totalArmorDamageDealtPerDirectHit = 0;
		boolean atLeastOneDamageComponentHasABGreaterThan100 = false;
		boolean atLeastOneDamageComponentDoesHeat = false;
		double totalHeatPerHit = 0;
		ArrayList<PushSTEComponent> allStes = new ArrayList<>();
		DamageComponent dmgAlias;
		for (int i = 0; i < dmgInstance.getTotalNumberOfDamageComponents(); i++) {
			dmgAlias = dmgInstance.getDamageComponentAtIndex(i);

			totalDamagePerHit += dmgAlias.getTotalComplicatedDamageDealtPerHit(
					breakpointMaterialFlag,
					resistances,
					IFG,
					getWeakpointMultiplier(),
					1
			);
			totalDamagePerHitOnHeavyArmor += dmgAlias.getTotalComplicatedDamageDealtPerHit(
					coveringArmorMaterialFlag,
					resistances,
					IFG,
					1,
					1
			);

			totalArmorDamageDealtPerDirectHit += dmgAlias.getTotalArmorDamageOnDirectHit();
			if (dmgAlias.armorBreakingIsGreaterThan100Percent()) {
				atLeastOneDamageComponentHasABGreaterThan100 = true;
			}

			allStes.addAll(dmgAlias.getStatusEffectsApplied());
			if (dmgAlias.appliesTemperature(DamageElement.heat)) {
				atLeastOneDamageComponentDoesHeat = true;
				totalHeatPerHit += dmgAlias.getTemperatureDealtPerDirectHit(DamageElement.heat);
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
			if (pstec.getSTE().inflictsTemperature(DamageElement.heat)) {
				atLeastOneSteAppliesHeat = true;
				stesHeatPerSec += pstec.getSTE().getAverageTemperaturePerSecond(DamageElement.heat);
			}
		}

		// Check for Heat/shot or Heat/sec stuff to see if this needs to add STE_OnFire into the mix.
		if (!frozen && (atLeastOneDamageComponentDoesHeat || atLeastOneSteAppliesHeat)) {
			// TODO: should this be extended longer than the normal duration?
			double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
			double totalHeatPerSec = totalHeatPerHit * RoF + stesHeatPerSec;

			// Instant ignition on the first hit
			if (totalHeatPerHit > temperatureComp.getEffectiveBurnTemperature()) {
				allStes.add(new PushSTEComponent(0, new STE_OnFire(burnDuration)));
			}
			// Ignition across time. Check to make sure that the total Heat/sec > Cooling Rate. If not, then it will never ignite. (PGL Incendiary vs Oppressor comes to mind)
			else if (totalHeatPerSec > temperatureComp.getCoolingRate()){
				double timeToIgnite;
				// First, check if the weapon can fully ignite the enemy in less than 1 sec (the default interval for CoolingRate, only Bulk Detonators use 0.25)
				if (totalHeatPerHit * Math.floor(0.99 * RoF) + stesHeatPerSec >= temperatureComp.getEffectiveBurnTemperature()) {
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
		if (weakpointIsCoveredByHeavyArmor()) {
			heavyArmorHP = getArmorBaseHealth() * normalScaling;
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
			if (!frozen && heavyArmorHP > 0){
				// heavyArmorHP > 0 will only evaluate to True when this is modeling an ArmorHealth plate covering the Weakpoint
				// If the ArmorHealth plate covering the Weakpoint has been broken, do full damage.
				if ((atLeastOneDamageComponentHasABGreaterThan100 && breakpointCounter >= numShotsToBreakArmor) || (!atLeastOneDamageComponentHasABGreaterThan100 && breakpointCounter > numShotsToBreakArmor)) {
					effectiveHP -= totalDamagePerHit;
				}
				else {
					effectiveHP -= totalDamagePerHitOnHeavyArmor;
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
			effectiveHP -= allStatusEffects.predictResistedDamageDealtInNextTimeInterval(1.0 / RoF, resistances);
			allStatusEffects.progressTime(1.0 / RoF);

			// Do some rounding because double operations are tricky
			effectiveHP = MathUtils.round(effectiveHP, 4);
		}

		return breakpointCounter;
	}

	private int calculateLightArmorBreakpoint(DamageInstance dmgInstance, double RoF, boolean IFG, boolean frozen,
											  double normalScaling, double largeScaling) {
		int breakpointCounter = 0;

		MaterialFlag preBreakMaterialFlag, postBreakMaterialFlag;
		if (frozen) {
			preBreakMaterialFlag = MaterialFlag.frozen;
			postBreakMaterialFlag = MaterialFlag.frozen;
		}
		else {
			preBreakMaterialFlag = MaterialFlag.lightArmor;
			postBreakMaterialFlag = MaterialFlag.normalFlesh;
		}

		double effectiveHP;
		if (usesNormalScaling()) {
			effectiveHP = getBaseHealth() * normalScaling;
		}
		else {
			effectiveHP = getBaseHealth() * largeScaling;
		}

		CreatureTemperatureComponent temperatureComp = getTemperatureComponent();
		ElementalResistancesMap resistances = getElementalResistances();

		double totalDamagePerHitBeforeBreakingArmor = 0;
		double totalDamagePerHitAfterBreakingArmor = 0;
		double totalArmorDamageDealtPerDirectHit = 0;
		boolean atLeastOneDamageComponentHasABGreaterThan100 = false;
		boolean atLeastOneDamageComponentDoesHeat = false;
		double totalHeatPerHit = 0;
		ArrayList<PushSTEComponent> allStes = new ArrayList<>();
		DamageComponent dmgAlias;
		for (int i = 0; i < dmgInstance.getTotalNumberOfDamageComponents(); i++) {
			dmgAlias = dmgInstance.getDamageComponentAtIndex(i);

			totalDamagePerHitBeforeBreakingArmor += dmgAlias.getTotalComplicatedDamageDealtPerHit(
					preBreakMaterialFlag,
					resistances,
					IFG,
					1,
					getArmorStrengthReduction()
			);
			totalDamagePerHitAfterBreakingArmor += dmgAlias.getTotalComplicatedDamageDealtPerHit(
					postBreakMaterialFlag,
					resistances,
					IFG,
					1,
					1
			);

			totalArmorDamageDealtPerDirectHit += dmgAlias.getTotalArmorDamageOnDirectHit();
			if (dmgAlias.armorBreakingIsGreaterThan100Percent()) {
				atLeastOneDamageComponentHasABGreaterThan100 = true;
			}

			allStes.addAll(dmgAlias.getStatusEffectsApplied());
			if (dmgAlias.appliesTemperature(DamageElement.heat)) {
				atLeastOneDamageComponentDoesHeat = true;
				totalHeatPerHit += dmgAlias.getTemperatureDealtPerDirectHit(DamageElement.heat);
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
			if (pstec.getSTE().inflictsTemperature(DamageElement.heat)) {
				atLeastOneSteAppliesHeat = true;
				stesHeatPerSec += pstec.getSTE().getAverageTemperaturePerSecond(DamageElement.heat);
			}
		}

		// Check for Heat/shot or Heat/sec stuff to see if this needs to add STE_OnFire into the mix.
		if (!frozen && (atLeastOneDamageComponentDoesHeat || atLeastOneSteAppliesHeat)) {
			// TODO: should this be extended longer than the normal duration?
			double burnDuration = (temperatureComp.getEffectiveBurnTemperature() - temperatureComp.getEffectiveDouseTemperature()) / temperatureComp.getCoolingRate();
			double totalHeatPerSec = totalHeatPerHit * RoF + stesHeatPerSec;

			// Instant ignition on the first hit
			if (totalHeatPerHit > temperatureComp.getEffectiveBurnTemperature()) {
				allStes.add(new PushSTEComponent(0, new STE_OnFire(burnDuration)));
			}
			// Ignition across time. Check to make sure that the total Heat/sec > Cooling Rate. If not, then it will never ignite. (PGL Incendiary vs Oppressor comes to mind)
			else if (totalHeatPerSec > temperatureComp.getCoolingRate()){
				double timeToIgnite;
				// First, check if the weapon can fully ignite the enemy in less than 1 sec (the default interval for CoolingRate, only Bulk Detonators use 0.25)
				if (totalHeatPerHit * Math.floor(0.99 * RoF) + stesHeatPerSec >= temperatureComp.getEffectiveBurnTemperature()) {
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
		// TODO: should this damage be divided by Normal Scaling resistance?
		double probabilityToBreakArmorStrengthPlate = EnemyInformation.armorStrengthBreakProbabilityLookup(totalArmorDamageDealtPerDirectHit / normalScaling, getArmorStrength());
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
			effectiveHP -= allStatusEffects.predictResistedDamageDealtInNextTimeInterval(1.0 / RoF, resistances);
			allStatusEffects.progressTime(1.0 / RoF);

			// Do some rounding because double operations are tricky
			effectiveHP = MathUtils.round(effectiveHP, 4);
		}

		return breakpointCounter;
	}

	public double calculatePercentageOfDamageWastedByArmor(DamageInstance dmgInstance, double generalAccuracy, double weakpointAccuracy,
														   double normalScaling, double largeScaling) {

		int totalNumDmgComponentsPerHit = dmgInstance.getTotalNumberOfDamageComponents();

		double baseHealth, heavyArmorPlateHealth;
		if (usesNormalScaling()) {
			baseHealth = getBaseHealth() * normalScaling;
		}
		else {
			baseHealth = getBaseHealth() * largeScaling;
		}
		if (hasHeavyArmorHealth()) {
			// All ArmorHealth plates have their health scale with difficulty resistance. By default, it's Normal Scaling but Elites override it.
			heavyArmorPlateHealth = getArmorBaseHealth() * normalScaling;
		}
		else {
			heavyArmorPlateHealth = 0;
		}

		double proportionOfDamageThatHitsWeakpoint = weakpointAccuracy / 100.0;
		double proportionOfDamageThatHitsArmor = 1.0 - proportionOfDamageThatHitsWeakpoint;

		int i;
		double armorDamagePerPellet, otherArmorDamage;
		double potentialMaxDamage, potentialWeakpointDamage, damageThatBypassesArmor, damageDealtToArmor, damageAffectedByArmor;
		int avgNumPelletsToBreakArmorStrengthPlate, numPelletsThatHaveHitArmorStrengthPlate = 0, numHitsEvaluated = 0;
		boolean armorStrengthPlateHasBroken;
		int[] avgNumHitsToBreakArmorStrengthPlate = new int[0];
		double totalDamageSpent = 0, damageDealtToHealth, actualDamageDealt = 0;
		DamageComponent dmgAlias;

		// Start by pre-calculating the number of pellets or loops that each DamageComponent would need to do before "statistically" it would break an armor plate that uses ArmorStrength
		armorDamagePerPellet = dmgInstance.getDamagePerPellet().getArmorDamageOnDirectHit() * proportionOfDamageThatHitsArmor + dmgInstance.getDamagePerPellet().getRadialArmorDamageOnDirectHit();
		if (hasLightArmor() || hasHeavyArmorStrength()) {
			if (armorDamagePerPellet > 0.0) {
				avgNumPelletsToBreakArmorStrengthPlate = (int) Math.ceil(MathUtils.meanRolls(EnemyInformation.armorStrengthBreakProbabilityLookup(armorDamagePerPellet, getArmorStrength())));
			}
			else {
				avgNumPelletsToBreakArmorStrengthPlate = -1;
			}

			if (dmgInstance.otherDamageIsDefined()) {
				avgNumHitsToBreakArmorStrengthPlate = new int[dmgInstance.getNumberOfOtherDamageComponents()];
				for (i = 0; i < dmgInstance.getNumberOfOtherDamageComponents(); i++) {
					otherArmorDamage = dmgInstance.getOtherDamageComponentAtIndex(i).getArmorDamageOnDirectHit() * proportionOfDamageThatHitsArmor + dmgInstance.getOtherDamageComponentAtIndex(i).getRadialArmorDamageOnDirectHit();
					if (otherArmorDamage > 0.0) {
						avgNumHitsToBreakArmorStrengthPlate[i] = (int) Math.ceil(
							MathUtils.meanRolls(EnemyInformation.armorStrengthBreakProbabilityLookup(otherArmorDamage, getArmorStrength()))
						);
					}
					else {
						avgNumHitsToBreakArmorStrengthPlate[i] = -1;
					}
				}
			}
		}
		else {
			avgNumPelletsToBreakArmorStrengthPlate = -1;
		}

		while (baseHealth > 0) {
			numHitsEvaluated++;
			for (i = 0; i < totalNumDmgComponentsPerHit; i++) {
				// 1. Select the right DamageComponent to evaluate for this loop
				dmgAlias = dmgInstance.getDamageComponentAtIndex(i);

				if (i < dmgInstance.getNumPellets()) {
					numPelletsThatHaveHitArmorStrengthPlate++;
				}

				// Early exit condition: if the current DamageComponent bypasses armor and doesn't damage it (e.g. Boomstick or Coilgun's Blastwave), skip the crazy calculations
				if (!dmgAlias.getDamageFlag(DamageFlag.canDamageArmor) && !dmgAlias.getDamageFlag(DamageFlag.reducedByArmor)) {
					damageThatBypassesArmor = dmgAlias.getTotalComplicatedDamageDealtPerHit(
						MaterialFlag.heavyArmor,
						getElementalResistances(),
						false,
						0,
						1
					);

					totalDamageSpent += damageThatBypassesArmor;
					actualDamageDealt += damageThatBypassesArmor;
					baseHealth -= damageThatBypassesArmor;
					continue;
				}

				// 2. Calculate its damage variants
				potentialMaxDamage = dmgAlias.getResistedDamage(getElementalResistances());  // this is equivalent to "complicated(normalFlesh) - Radial"
				damageThatBypassesArmor = dmgAlias.getResistedRadialDamage(getElementalResistances());  // alias of Radial Damage, temporarily
				potentialWeakpointDamage = dmgAlias.getTotalComplicatedDamageDealtPerHit(
					MaterialFlag.weakpoint,
					getElementalResistances(),
					false,
					getWeakpointMultiplier(),
					1
				) - damageThatBypassesArmor;  // By subtracting Radial Damage, this evaluates down to just the Weakpoint Damage (if applicable)
				damageThatBypassesArmor = dmgAlias.getTotalComplicatedDamageDealtPerHit(
					MaterialFlag.heavyArmor,
					getElementalResistances(),
					false,
					0,
					1
				);  // theoretically this should be identical to Radial Damage, but being verbose just to be safe.
				damageDealtToArmor = dmgAlias.getArmorDamageOnDirectHit() * proportionOfDamageThatHitsArmor + dmgAlias.getRadialArmorDamageOnDirectHit();
				damageAffectedByArmor = potentialMaxDamage * proportionOfDamageThatHitsArmor;

				// 3. Subtract from Armor and Health accordingly
				totalDamageSpent += damageAffectedByArmor + potentialWeakpointDamage * proportionOfDamageThatHitsWeakpoint + damageThatBypassesArmor;
				damageDealtToHealth = potentialWeakpointDamage * proportionOfDamageThatHitsWeakpoint + damageThatBypassesArmor;

				// 3a. Light Armor plates (always Armor Strength, mixes with Heavy Armor plates on Guards)
				armorStrengthPlateHasBroken = (
					(avgNumPelletsToBreakArmorStrengthPlate > 0 && (
						numPelletsThatHaveHitArmorStrengthPlate > avgNumPelletsToBreakArmorStrengthPlate ||
						(dmgAlias.armorBreakingIsGreaterThan100Percent() && numPelletsThatHaveHitArmorStrengthPlate == avgNumPelletsToBreakArmorStrengthPlate)
					)) ||
					// Because this will only evaluate for otherDamage[] DamageComponents, it should be safe to call the accessors like this.
					(i >= dmgInstance.getNumPellets() && avgNumHitsToBreakArmorStrengthPlate[i-dmgInstance.getNumPellets()] > 0 && (
						numHitsEvaluated > avgNumHitsToBreakArmorStrengthPlate[i-dmgInstance.getNumPellets()] ||
						(dmgAlias.armorBreakingIsGreaterThan100Percent() && numHitsEvaluated == avgNumHitsToBreakArmorStrengthPlate[i-dmgInstance.getNumPellets()])
					))
				);
				if (hasLightArmor()) {
					// Plate is already broken, or on the shot that breaks with AB > 100%
					if (armorStrengthPlateHasBroken) {
						damageDealtToHealth += damageAffectedByArmor * getNumArmorStrengthPlates() / (getNumArmorStrengthPlates() + getNumArmorHealthPlates());
					}
					// Plate is unbroken; account for damage reduction
					else {
						if (dmgAlias.getDamageFlag(DamageFlag.embeddedDetonator)){
							damageDealtToHealth += damageAffectedByArmor * getNumArmorStrengthPlates() / (getNumArmorStrengthPlates() + getNumArmorHealthPlates());
						}
						else {
							damageDealtToHealth += damageAffectedByArmor * getArmorStrengthReduction() * getNumArmorStrengthPlates() / (getNumArmorStrengthPlates() + getNumArmorHealthPlates());
						}
					}
				}

				// 3b. Heavy Armor Plates with health (mixes with Light Armor plates on Guards)
				if (hasHeavyArmorHealth()) {
					if (heavyArmorPlateHealth > 0) {
						if (dmgAlias.armorBreakingIsGreaterThan100Percent()) {
							if (damageDealtToArmor > heavyArmorPlateHealth) {
								damageDealtToHealth += damageAffectedByArmor * getNumArmorHealthPlates() / (getNumArmorStrengthPlates() + getNumArmorHealthPlates());
								heavyArmorPlateHealth = 0.0;
							}
							else {
								heavyArmorPlateHealth -= damageDealtToArmor;
							}
						}
						else {
							heavyArmorPlateHealth -= damageDealtToArmor;
						}
					}
					else {
						damageDealtToHealth += damageAffectedByArmor * getNumArmorHealthPlates() / (getNumArmorStrengthPlates() + getNumArmorHealthPlates());
					}
				}

				// 3c. Heavy Armor plates with Armor Strength (mutually exclusive with Light Armor plates)
				if (hasHeavyArmorStrength()) {
					// Plate is already broken, or on the shot that breaks with AB > 100%
					if (armorStrengthPlateHasBroken) {
						damageDealtToHealth += damageAffectedByArmor;
					}
					// Implicit "else add zero"
				}

				actualDamageDealt += damageDealtToHealth;
				baseHealth -= damageDealtToHealth;
			}
		}

		double damageWasted = 1.0 - actualDamageDealt / totalDamageSpent;
		// Mathematica's Chop[] function rounds any number lower than 10^-10 to the integer zero. Imitation, flattery, etc...
		if (damageWasted < Math.pow(10.0, -10.0)) {
			return 0.0;
		}
		else {
			return damageWasted;
		}
	}

	public double calculateOverkill(DamageInstance dmgInstance, double normalScaling, double largeScaling) {
		double creatureHP;
		if (usesNormalScaling()) {
			creatureHP = getBaseHealth() * normalScaling;
		}
		else {
			creatureHP = getBaseHealth() * largeScaling;
		}

		double totalDamagePerShot = dmgInstance.getNumPellets() * dmgInstance.getDamagePerPellet().getTotalComplicatedDamageDealtPerHit(
			MaterialFlag.normalFlesh,
			getElementalResistances(),
			false,
			0,
			1
		);

		if (dmgInstance.otherDamageIsDefined()) {
			for (int j = dmgInstance.getNumPellets(); j < dmgInstance.getTotalNumberOfDamageComponents(); j++) {
				totalDamagePerShot += dmgInstance.getDamageComponentAtIndex(j).getTotalComplicatedDamageDealtPerHit(
					MaterialFlag.normalFlesh,
					getElementalResistances(),
					false,
					0,
					1
				);
			}
		}
		return ((Math.ceil(creatureHP / totalDamagePerShot) * totalDamagePerShot) / creatureHP - 1.0) * 100.0;
	}
}
