package weapons.scout;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;
import weapons.Weapon;

public class PlasmaCarbine extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double rateOfFire;
	private int batteryCapacity;
	private double heatPerShot;
	private double maxHeat;
	private double coolingRate;
	private double cooldownDelay;
	private double overheatDuration;
	
	// This gets used by T5.A, T5.B, and Thermal Exhaust Feedback
	private enum heatPercentages {zero, fifty, sixty, seventy, eighty, ninety, oneHundred}
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public PlasmaCarbine() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public PlasmaCarbine(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public PlasmaCarbine(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Drak-25 Plasma Carbine";
		weaponPic = WeaponPictures.assaultRifle;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 9.0;
		rateOfFire = 12.0;
		batteryCapacity = 750;
		heatPerShot = 0.045;
		maxHeat = 2.0;
		coolingRate = 1.1;
		cooldownDelay = 0.3;
		overheatDuration = 2.5;
		
		initializeModsAndOverclocks();
		// Grab initial values before customizing mods and overclocks
		setBaselineStats();
		
		// Selected Mods
		selectedTier1 = mod1;
		selectedTier2 = mod2;
		selectedTier3 = mod3;
		selectedTier4 = mod4;
		selectedTier5 = mod5;
		
		// Overclock slot
		selectedOverclock = overclock;
	}
	
	@Override
	protected void initializeModsAndOverclocks() {
		tier1 = new Mod[3];
		tier1[0] = new Mod("High-Volume Plasma Feed", "+4 Rate of Fire", modIcons.rateOfFire, 1, 0);
		tier1[1] = new Mod("Improved Thermals", "x0.75 Heat per Shot", modIcons.coolingRate, 1, 1);
		tier1[2] = new Mod("Stronger Particle Accelerator", "x2.0 Projectile Velocity", modIcons.projectileVelocity, 1, 2, false);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Larger Battery", "+200 Battery Capacity", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Increased Particle Density", "+2 Direct Damage", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Custom Coil Alignment", "-70% Horizontal and Vertical Base Spread", modIcons.baseSpread, 3, 0);
		tier3[1] = new Mod("Gen 2 Cooling System", "x1.5 Cooling Rate, x0.7 Cooldown Delay", modIcons.coolingRate, 3, 1);
		tier3[2] = new Mod("Hot Feet", "Move 50% faster while the weapon is Overheated and for 2 seconds afterwards.", modIcons.movespeed, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Overcharged PCF", "15% Chance per bullet to inflict an Electrocute DoT which does " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric "
				+ "Damage per Second and slows enemies by 80% for 4 seconds.", modIcons.electricity, 4, 0);
		tier4[1] = new Mod("Plasma Splash", "-5 Direct Damage, +5 Area Damage in a 1m radius", modIcons.addedExplosion, 4, 1);
		tier4[2] = new Mod("Destructive Resonance Amp", "+200% Armor Breaking", modIcons.armorBreaking, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Manual Heat Dump", "When the Heat Meter is greater than 50%, you can press the Reload button to manually activate the Overheat mode. When used in this way, Overheat's duration gets multiplied by x0.65, "
				+ "and also scales with the current Heat level (50% Heat Meter = x0.5 Overheat Duration).", modIcons.specialReload, 5, 0, false);
		tier5[1] = new Mod("Thermal Feedback Loop", "When the Heat Meter is greater than 50%, the Rate of Fire is increased by +5 (up to a maximum of 20 RoF).", modIcons.special, 5, 1);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Aggressive Venting", "Upon Overheating, deal 6 Damage, 60 Heat, and 5.0 Fear Factor to enemies within 5m of you. This effect scales with the "
				+ "current Heat level, so manually Overheating early deals less Damage and Heat. Additionally, reduces Overheat duration by x0.8.", overclockIcons.special, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Thermal Liquid Coolant", "x1.25 Cooling Rate, x0.85 Heat per Shot", overclockIcons.coolingRate, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Rewiring Mod", "Regenerate Ammo while Overheated. At full Overheat, up to 31.5 ammo gets regenerated on average. This effect scales with the "
				+ "current Heat level, so manually Overheating early returns less ammo. In exchange, +0.8 sec Overheat Duration and x0.7 Battery Capacity", overclockIcons.carriedAmmo, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Impact Deflection", "Projectiles will bounce 1 time, either off of terrain or enemies. In exchange, -2 Rate of Fire", overclockIcons.ricochet, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Overtuned Particle Accelerator", "+8 Direct Damage, x0.8 Battery Capacity, x1.5 Heat per Shot, +400% Horizontal Base Spread, +133% Vertical Base Spread", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Shield Battery Booster", "+1 Rate of Fire, +100 Battery Capacity, x0.5 Cooling Rate, x1.5 Heat per Shot, x2 Overheat Duration. While Shield is full, +5.5 Direct Damage and "
				+ "2x Projectile Velocity. Upon Overheating, your shield gets disabled until the Overheat finishes.", overclockIcons.damageResistance, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Thermal Exhaust Feedback", "Starting when the Heat Meter reaches 50%, the next four intervals of 10% on the Heat Meter each add 1.5 Fire Damage and 1.5 Heat to every projectile, "
				+ "up to +6 Damage/Heat at 90%. In exchange, x1.28 Overheat Duration and x1.2 Heat per Shot.", overclockIcons.heatDamage, 6);

		// TODO: Aggressive Venting converts 10% of the 60 Heat to Fire-element + Heat, and its 60 damage scales with the Heat Level (manual Overheat at 50% = 30 damage)

		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}

	@Override
	public PlasmaCarbine clone() {
		return new PlasmaCarbine(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}

	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "PlasmaCarbine";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.plasmaCarbineGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		if (selectedTier4 == 1) {
			toReturn -= 5;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 8;
		}
		else if (selectedOverclock == 5) {
			// Need some way to model this interaction with Shield...
			// Although the files list it as +2.75, in game it behaves like +5.5
			toReturn += 5.5;
		}
		else if (selectedOverclock == 6) {
			toReturn += calculateTEFDamageBonus();
		}
		
		return toReturn;
	}
	private double getAreaDamage() {
		if (selectedTier4 == 1) {
			// It looks like this does 5 damage in a 1m radius, with 50% falloff at 0.5m.
			return 5;
		}
		else {
			return 0;
		}
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier1 == 0) {
			toReturn += 3.0;
		}
		
		if (selectedOverclock == 5) {
			toReturn += 1.0;
		}
		
		return toReturn;
	}
	private int getBatteryCapacity() {
		double toReturn = batteryCapacity;
		
		if (selectedTier2 == 0) {
			toReturn += 200;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 0.7;
			
			// This is going to be the crazy part of Rewiring Mod. I'm choosing to model it as if the player is firing until Overheat to maximize the ammo regen.
			double magSizeToOverheat = calculateNumShotsFiredBeforeOverheating() + 1;
			double ammoRegendDuringOverheat = 31.5;  // TODO: find the exact formula for this number. 31.5 is just the average that I've gathered experimentally.
			double ammoSpentPerMag = magSizeToOverheat - ammoRegendDuringOverheat;
			
			double numOverheats = Math.floor((toReturn - ammoRegendDuringOverheat) / ammoSpentPerMag);
			
			toReturn = Math.round(toReturn + numOverheats * ammoRegendDuringOverheat);
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0.8;
		}
		else if (selectedOverclock == 5) {
			toReturn += 100;
		}
		
		return (int) toReturn;
	}
	private double getHeatPerShot() {
		double modifier = 1.0;
		
		if (selectedTier1 == 1) {
			modifier *= 0.75;
		}
		
		if (selectedOverclock == 1) {
			modifier *= 0.85;
		}
		else if (selectedOverclock == 4 || selectedOverclock == 5) {
			modifier *= 1.5;
		}
		else if (selectedOverclock == 6) {
			modifier *= 1.2;
		}
		
		return heatPerShot * modifier;
	}
	private double getCoolingRate() {
		double modifier = 1.0;
		
		if (selectedTier3 == 1) {
			modifier *= 1.5;
		}
		
		if (selectedOverclock == 1) {
			modifier *= 1.25;
		}
		else if (selectedOverclock == 5) {
			modifier *= 0.5;
		}
		
		return coolingRate * modifier;
	}
	private double getCooldownDelay() {
		double toReturn = cooldownDelay;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.7;
		}
		
		return toReturn;
	}
	private double getOverheatDuration() {
		double toReturn = overheatDuration;
		
		if (selectedOverclock == 2) {
			toReturn += 0.8;
		}
		else if (selectedOverclock == 0) {
			toReturn *= 0.8;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 2.0;
		}
		else if (selectedOverclock == 6) {
			toReturn *= 1.28;
		}
		
		return toReturn;
	}
	private double getProjectileVelocity() {
		// According to gamefiles, the default velocity is 36 m/sec
		double toReturn = 1.0;
		
		if (selectedTier1 == 2) {
			toReturn *= 1.5;
		}
		
		if (selectedOverclock == 5) {
			// Need some way to model this interaction with Shield...
			toReturn *= 2.0;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 2) {
			return 3.0;
		}
		else {
			return 1.0;
		}
	}
	private double getHorizontalBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 4.0;
		}
		
		return toReturn;
	}
	private double getVerticalBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 1.33;
		}
		
		return toReturn;
	}
	private int getNumBounces() {
		if (selectedOverclock == 3) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[19];
		
		boolean directDamageModified = selectedTier2 == 1 || selectedTier4 == 1 || selectedOverclock > 3;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamage = selectedTier4 == 1;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamage, areaDamage);
		toReturn[2] = new StatsRow("AoE Radius:", 1.0, modIcons.aoeRadius, areaDamage, areaDamage);
		
		toReturn[3] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getProjectileVelocity()), modIcons.projectileVelocity, selectedTier1 == 2 || selectedOverclock == 5, selectedTier1 == 2 || selectedOverclock == 5);
		
		boolean heatPerShotModified = selectedTier1 == 1 || selectedOverclock == 1 || selectedOverclock > 3;
		toReturn[4] = new StatsRow("Heat/Shot:", getHeatPerShot(), modIcons.blank, heatPerShotModified);
		toReturn[5] = new StatsRow("Shots Fired Before Overheating:", calculateNumShotsFiredBeforeOverheating(), modIcons.magSize, heatPerShotModified);
		
		boolean batteryCapacityModified = selectedTier2 == 0 || (selectedOverclock > 1 && selectedOverclock < 6); 
		toReturn[6] = new StatsRow("Battery Capacity:", getBatteryCapacity(), modIcons.carriedAmmo, batteryCapacityModified);
		toReturn[7] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier1 == 0 || selectedOverclock == 5);
		boolean heatRoF = selectedTier5 == 1;
		toReturn[8] = new StatsRow("Rate of Fire at High Heat:", Math.min(getRateOfFire() + 5.0, 20.0), modIcons.rateOfFire, heatRoF, heatRoF);
		toReturn[9] = new StatsRow("Average Rate of Fire:", calculateAverageRoF(), modIcons.rateOfFire, heatRoF, heatRoF);
		
		boolean coolingRateModified = selectedTier3 == 1 || selectedOverclock % 2 == 1;
		toReturn[10] = new StatsRow("Cooling Rate:", getCoolingRate(), modIcons.coolingRate, coolingRateModified);
		toReturn[11] = new StatsRow("Cooldown Delay:", getCooldownDelay(), modIcons.duration, selectedTier3 == 1);
		toReturn[12] = new StatsRow("Cooldown period (no Overheat):", calculateCoolingPeriod(), modIcons.hourglass, coolingRateModified);
		
		boolean overheatModified = selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[13] = new StatsRow("Overheat Duration:", getOverheatDuration(), modIcons.coolingRate, overheatModified);
		toReturn[14] = new StatsRow("Manual Overheat Duration:", "< " + 0.65 * getOverheatDuration(), modIcons.coolingRate, overheatModified, selectedTier5 == 0);
		
		toReturn[15] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 2, selectedTier4 == 2);
		// Maybe add Electrocute from T4.A and/or Fear from OC "AV" here?
		toReturn[16] = new StatsRow("Bounces per Projectile:", getNumBounces(), modIcons.ricochet, selectedOverclock == 3, selectedOverclock == 3);
		
		boolean baseSpreadModified = selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[17] = new StatsRow("Horizontal Base Spread:", convertDoubleToPercentage(getHorizontalBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		toReturn[18] = new StatsRow("Vertical Base Spread:", convertDoubleToPercentage(getVerticalBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		// Check to see if T3.A has a Max Bloom bonus listed, and if OC "OPA" has any hidden Accuracy penalties
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private double getProportionOfMaxHeatBetweenPercentages(heatPercentages lower, heatPercentages higher) {
		/*
			63.8x + 4.73x^2 + -5.84x^3
			
			Internal Heat | Displayed %
			0.78214 | 50
			0.95255 | 60
			1.1356  | 70
			1.3415  | 80
			1.5922  | 90
			2.0     | 100
		*/
		
		double lowerHeat, higherHeat;
		switch (lower) {
			case zero: {
				lowerHeat = 0.0;
				break;
			}
			case fifty: {
				lowerHeat = 0.78214;
				break;
			}
			case sixty: {
				lowerHeat = 0.95255;
				break;
			}
			case seventy: {
				lowerHeat = 1.1356;
				break;
			}
			case eighty: {
				lowerHeat = 1.3415;
				break;
			}
			case ninety: {
				lowerHeat = 1.5922;
				break;
			}
			case oneHundred: {
				lowerHeat = 2.0;
				break;
			}
			default: {
				lowerHeat = 0.0;
				break;
			}
		}
		switch (higher) {
			case zero: {
				higherHeat = 0.0;
				break;
			}
			case fifty: {
				higherHeat = 0.78214;
				break;
			}
			case sixty: {
				higherHeat = 0.95255;
				break;
			}
			case seventy: {
				higherHeat = 1.1356;
				break;
			}
			case eighty: {
				higherHeat = 1.3415;
				break;
			}
			case ninety: {
				higherHeat = 1.5922;
				break;
			}
			case oneHundred: {
				higherHeat = 2.0;
				break;
			}
			default: {
				higherHeat = 0.0;
				break;
			}
		}
		
		return (higherHeat - lowerHeat) / maxHeat;
	}
	
	private double calculateAverageRoF() {
		double baseRoF = getRateOfFire();
		
		// Early exit condition: if T5.B isn't equipped, then this is the same as the base RoF
		if (selectedTier5 != 1) {
			return baseRoF;
		}
		
		double percentageOfMagFiredBelowFiftyPercentHeat = getProportionOfMaxHeatBetweenPercentages(heatPercentages.zero, heatPercentages.fifty);
		double percentageOfMagFiredAboveFiftyPercentHeat = getProportionOfMaxHeatBetweenPercentages(heatPercentages.fifty, heatPercentages.oneHundred);
		
		return (percentageOfMagFiredBelowFiftyPercentHeat * baseRoF + percentageOfMagFiredAboveFiftyPercentHeat * Math.min(baseRoF + 5.0, 20.0));
	}
	
	private double calculateTEFDamageBonus() {
		// UUU testing on Nov 2nd indicates that at 90% heat this adds +6 Damage/Heat per bullet. Divide by 4 tiers to get 1.5 per tier.
		double damageAddedPerTier = 1.5;
		
		double oneBonusDamage = getProportionOfMaxHeatBetweenPercentages(heatPercentages.sixty, heatPercentages.seventy);
		double twoBonusDamage = getProportionOfMaxHeatBetweenPercentages(heatPercentages.seventy, heatPercentages.eighty);
		double threeBonusDamage = getProportionOfMaxHeatBetweenPercentages(heatPercentages.eighty, heatPercentages.ninety);
		double fourBonusDamage = getProportionOfMaxHeatBetweenPercentages(heatPercentages.ninety, heatPercentages.oneHundred);
		
		return oneBonusDamage * damageAddedPerTier + twoBonusDamage * damageAddedPerTier * 2.0 + threeBonusDamage * damageAddedPerTier * 3.0 + fourBonusDamage * damageAddedPerTier * 4.0;
	}
	
	private double calculateNumShotsFiredBeforeOverheating() {
		// Because it only cools down after the player stops firing, then the "magazine size" is just ceil(maxHeat/heatPerShot)
		// Subtract 1 so that it doesn't Overheat.
		return Math.ceil(maxHeat / getHeatPerShot()) - 1.0;
	}
	
	private double calculateCoolingPeriod() {
		return getCooldownDelay() + maxHeat / getCoolingRate();
	}

	@Override
	public boolean currentlyDealsSplashDamage() {
		return selectedTier4 == 1;
	}
	
	@Override
	protected void setAoEEfficiency() {
		if (selectedTier4 == 1) {
			aoeEfficiency = calculateAverageAreaDamage(1.0, 0.5, 0.5);
		}
		else {
			aoeEfficiency = new double[3];
		}
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage, magSize, RoF, coolingPeriod;
		
		magSize = calculateNumShotsFiredBeforeOverheating();
		RoF = calculateAverageRoF();
		
		// To maximize the ammo regen of Rewiring Mod, this has to Overheat.
		if (selectedOverclock == 2) {
			magSize += 1.0;
			coolingPeriod = getOverheatDuration();
		}
		else {
			coolingPeriod = calculateCoolingPeriod();
		}
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		double electrocuteDPS = 0, burnDPS = 0;
		if (burst) {
			duration = magSize / RoF;
			
			if (selectedTier4 == 0) {
				electrocuteDPS = calculateRNGDoTDPSPerMagazine(0.15, DoTInformation.Electro_DPS, (int) magSize);
			}
			
			if (selectedOverclock == 6) {
				double burnDoTUptime = (duration - averageTimeToCauterize()) / duration;
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
		}
		else {
			// To maximize the ammo regen of Rewiring Mod, this has to Overheat.
			/*if (selectedOverclock == 2) {
				// Rew
			}*/
			duration = (magSize / RoF) + coolingPeriod;
			
			if (selectedTier4 == 0) {
				electrocuteDPS = DoTInformation.Electro_DPS;
			}
			
			if (selectedOverclock == 6) {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
		// Damage wasted by Armor
		if (armorWasting && !statusEffects[1]) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			directDamage *= armorWaste;
		}
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
			areaDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, 0.0, 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + (bulletsThatHitTarget + bulletsThatHitWeakpoint) * areaDamage) / duration + electrocuteDPS + burnDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		// I'm choosing to model this as if a secondary target can be hit by a splash, and then the projectile can bounce into it to hit it again for the Direct+Area again. As such, these DPS add together.
		double magSize = calculateNumShotsFiredBeforeOverheating();
		double coolingPeriod;
		if (selectedOverclock == 2) {
			magSize += 1;
			coolingPeriod = getOverheatDuration();
		}
		else {
			coolingPeriod = calculateCoolingPeriod();
		}
		
		double timeToFireMagazineAndReload = (magSize / calculateAverageRoF()) + coolingPeriod;
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
		double plasmaSplashDPS = 0;
		if (selectedTier4 == 1) {
			plasmaSplashDPS = areaDamage * aoeEfficiency[1] * magSize / timeToFireMagazineAndReload;
		}
		
		double bouncyBulletsDPS = 0;
		if (selectedOverclock == 3) {
			bouncyBulletsDPS = (directDamage + areaDamage) * magSize / timeToFireMagazineAndReload;
		}
		
		return plasmaSplashDPS + bouncyBulletsDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// I'm choosing to model the interaction between Bouncy and Splash as if the Splash doesn't hit new targets, just re-hits the same targets the first Splash already did.
		double areaDamage = 0;
		if (selectedTier4 == 1) {
			areaDamage = getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		}
		double baseDamage = (getDirectDamage() + areaDamage) * getBatteryCapacity() * calculateBlowthroughDamageMultiplier(getNumBounces());
		
		// As for the DoT damage -- I'm very fortunate. GSG coded it so that the AoE damage is mutually exclusive with Electrocute chance, and TEF's damage boost only applies to single-target.
		// Thus, I don't need to model the T4.A AoE radius scaling these two DoT effects.
		double estimatedNumEnemiesKilled = calculateFiringDuration() / averageTimeToKill();
		
		double burnDamage = 0;
		if (selectedOverclock == 0) {
			// OC "Aggressive Venting"
			// Adapted from Minigun's T5.A "Aggressive Venting"
			// Modeled only for full Overheats, not for T5.A's manual overheats which do less Damage/Heat
			double[] aggressiveVentingAoeEfficiency = calculateAverageAreaDamage(5.0, 1.5, 0.25);
			double percentageOfEnemiesIgnitedByAV = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(60 * aggressiveVentingAoeEfficiency[1]);
			double numGlyphidsHitByHeatBurst = aggressiveVentingAoeEfficiency[2];
			int numTimesAVcanTrigger = (int) Math.floor(getBatteryCapacity() / calculateNumShotsFiredBeforeOverheating());
			double burnDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			burnDamage = numTimesAVcanTrigger * numGlyphidsHitByHeatBurst * (6.0 * aggressiveVentingAoeEfficiency[1] + percentageOfEnemiesIgnitedByAV  * burnDoTDamagePerEnemy);
		}
		else if (selectedOverclock == 6) {
			// OC "Thermal Exhaust Feedback"
			// Adapted from Flamethrower
			double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(averageTimeToCauterize(), DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			burnDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		double electrocuteDamage = 0;
		if (selectedTier4 == 0) {
			// T4.A "Overcharged PCF"
			// Adapted from Autocannon OC "NTP" code block
			double timeBeforeElectrocuteProc = MathUtils.meanRolls(0.15) / calculateAverageRoF();
			double electrocuteDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeElectrocuteProc, 4.0, DoTInformation.Electro_DPS);
			
			electrocuteDamage = electrocuteDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return baseDamage + burnDamage + electrocuteDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier4 == 1 && selectedOverclock != 0) {
			return (int) aoeEfficiency[2];
		}
		else if (selectedTier4 != 1 && selectedOverclock == 3) {
			// Bouncy Bullets can bounce once, for 2 total targets
			return 1 + getNumBounces();
		}
		else if (selectedTier4 == 1 && selectedOverclock == 3) {
			// It might be a bit of an over-simplification, but because Plasma Splash's AoE radius is only 1m I can't imagine it would hit more than 1 new target on each bounce.
			return (int) aoeEfficiency[2] + getNumBounces();
		}
		else {
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = (int) calculateNumShotsFiredBeforeOverheating();
		double coolingPeriod;
		if (selectedOverclock == 2) {
			// To use Rewiring Mod to its fullest extent, the player has to fire the last shot to Overheat.
			magSize += 1;
			coolingPeriod = getOverheatDuration();
		}
		else {
			coolingPeriod = calculateCoolingPeriod();
		}
		
		double numMags = numMagazines(getBatteryCapacity(), magSize);
		double numCooldowns = numReloads(getBatteryCapacity(), magSize);
		
		return numMags * (double) magSize / calculateAverageRoF() + numCooldowns * coolingPeriod;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage()) + getAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		/*
		double hBaseSpread = 4.5 * getHorizontalBaseSpread();
		double vBaseSpread = 4.5 * getVerticalBaseSpread();
		double spreadPerShot = 0;
		double spreadRecoverySpeed = 1.0;
		double maxBloom = 1.0;
		double minSpreadWhileMoving = 0.0;
		
		double recoilPitch = 1.3;
		double recoilYaw = 1.3;
		double mass = 100.0;
		double springStiffness = 375.0;
		// RecoilSettings.CriticalDampening = 0.5 means that this is under-damped. My model only uses critical dampening = 1, so technically this estimate will be a little more accurate than the game.
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, calculateAverageRoF(), (int) calculateNumShotsFiredBeforeOverheating(), 1, 
				hBaseSpread, vBaseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
		*/
		// WeaponsNTools/PlasmaCarbine/PRJ_PlasmaCarbineShot
		double projectileVelocity = 36.0 * getProjectileVelocity();
		double projectileRadius = 0.15;
		return accEstimator.calculateProjectileAccuracy(weakpointAccuracy, projectileRadius, projectileVelocity, 0.0);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		
		// I can reuse Heat per Shot for the Fire-element damage done by TEF. Subtracting from DirectDamage will either do nothing when TEF is equipped,
		// or bring it back down to only the Kinetic/Disintegrate element component. Then the Fire-element damage added by TEF will either be 0 or the right number.
		double heatPerShot = 0.0;
		if (selectedOverclock == 6) {
			heatPerShot = calculateTEFDamageBonus();
		}
		
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage() - heatPerShot;  // Kinetic and Disintegrate
		directDamage[2] = heatPerShot;  // Fire element, from TEF if it's equipped.
		
		// From testing, it seems like the Area Damage is half Kinetic/Disintegrate, half Fire-element.
		double aDamage = 0.5 * getAreaDamage();
		double[] areaDamage = new double[5];
		areaDamage[0] = aDamage;  // Kinetic or Disintegrate, not sure which.
		areaDamage[2] = aDamage;  // Fire element from T4.B Plasma Splash
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		if (selectedTier4 == 0) {
			dot_dps[0] = 12.0;
			dot_duration[0] = 4.0;
			dot_probability[0] = 0.15;
		}
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															0.0, getArmorBreaking(), calculateAverageRoF(), heatPerShot, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Mobility
		if (selectedTier3 == 2) {
			utilityScores[0] = (getOverheatDuration() + 2.0) * MathUtils.round(0.5 * DwarfInformation.walkSpeed, 2) * UtilityInformation.Movespeed_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Armor Breaking
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getAreaDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// Slow
		if (selectedTier4 == 0) {
			// T4.A gives every projectile a 15% chance to electrocute an enemy, doing 12 DPS and slowing by 80% for 4 seconds (re-uses DoT from M1000 OC "EFS")
			utilityScores[3] = calculateMaxNumTargets() * 0.15 * 4.0 * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Fear
		if (selectedOverclock == 0) {
			// OC "Aggressive Venting" does 60 Heat and 5 Fear in a 5m radius, 1.5m MaxDmgRadius and 25% falloff.
			double[] aggressiveVentingAoeEfficiency = calculateAverageAreaDamage(5, 1.5, 0.25);
			int numGlyphidsFeared = (int) Math.round(aggressiveVentingAoeEfficiency[1] * aggressiveVentingAoeEfficiency[2]);
			double probabilityToFear = calculateFearProcProbability(5.0);
			utilityScores[4] = probabilityToFear * numGlyphidsFeared * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedOverclock == 6) {
			double RoF = getRateOfFire();
			if (selectedTier5 == 1) {
				// Because the Heat only activates after the 50% Heat mark, it can just be modeled as igniting using the faster RoF.
				RoF = Math.min(RoF + 5.0, 20.0);
			}
			
			//double heatActivationDelay = (maxHeat * getProportionOfMaxHeatBetweenPercentages(heatPercentages.zero, heatPercentages.sixty) / getHeatPerShot()) / getRateOfFire();
			double ignitionTime = EnemyInformation.averageTimeToIgnite(0, calculateTEFDamageBonus(), RoF, 0);
			// This returns longer than one magazine of duration, so I'm going to omit the activation delay.
			return ignitionTime;  // heatActivationDelay + ignitionTime;
		}
		else {
			return -1;
		}
	}
	
	@Override
	public double damagePerMagazine() {
		double magSize = calculateNumShotsFiredBeforeOverheating();
		
		double areaDamage = 0;
		if (selectedTier4 == 1) {
			areaDamage = getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		}
		double baseDamage = (getDirectDamage() + areaDamage) * magSize * calculateBlowthroughDamageMultiplier(getNumBounces());
		
		double estimatedNumEnemiesKilled = timeToFireMagazine() / averageTimeToKill();
		
		double burnDamage = 0;
		if (selectedOverclock == 0) {
			// OC "Aggressive Venting"
			double[] aggressiveVentingAoeEfficiency = calculateAverageAreaDamage(5.0, 1.5, 0.25);
			double percentageOfEnemiesIgnitedByAV = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(60 * aggressiveVentingAoeEfficiency[1]);
			double numGlyphidsHitByHeatBurst = aggressiveVentingAoeEfficiency[2];
			double burnDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			burnDamage = numGlyphidsHitByHeatBurst * (6.0 * aggressiveVentingAoeEfficiency[1] + percentageOfEnemiesIgnitedByAV  * burnDoTDamagePerEnemy);
		}
		else if (selectedOverclock == 6) {
			// OC "Thermal Exhaust Feedback"
			double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(averageTimeToCauterize(), DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			burnDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		double electrocuteDamage = 0;
		if (selectedTier4 == 0) {
			// T4.A "Overcharged PCF"
			double timeBeforeElectrocuteProc = MathUtils.meanRolls(0.15) / calculateAverageRoF();
			double electrocuteDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeElectrocuteProc, 4.0, DoTInformation.Electro_DPS);
			
			electrocuteDamage = electrocuteDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return baseDamage + burnDamage + electrocuteDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		double magSize = calculateNumShotsFiredBeforeOverheating();
		if (selectedOverclock == 2) {
			magSize += 1;
		}
		return magSize / calculateAverageRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), getArmorBreaking(), 0.0, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
