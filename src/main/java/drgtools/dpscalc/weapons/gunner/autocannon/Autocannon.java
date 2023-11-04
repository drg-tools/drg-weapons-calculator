package drgtools.dpscalc.weapons.gunner.autocannon;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.GuiConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.DoTInformation;
import drgtools.dpscalc.modelPieces.DwarfInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

public class Autocannon extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double areaDamage;
	private double aoeRadius;
	private int magazineSize;
	private int carriedAmmo;
	private double movespeedWhileFiring;
	private double increaseScalingRate;
	private double minRateOfFire;
	private double maxRateOfFire;
	private double reloadTime;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Autocannon() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Autocannon(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Autocannon(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Thunderhead\" Heavy Autocannon";
		weaponPic = WeaponPictures.autocannon;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 14;
		areaDamage = 9;
		aoeRadius = 1.4;  // meters
		magazineSize = 110;
		carriedAmmo = 440;
		movespeedWhileFiring = 0.5;
		increaseScalingRate = 0.4;
		minRateOfFire = 3.4;
		maxRateOfFire = 5.5;
		reloadTime = 5.0;  // seconds
		
		// Override default 10m distance
		accEstimator.setDistance(6.0);
		
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
		tier1[0] = new Mod("Increased Caliber Rounds", "+3 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "x2 Magazine Size", modIcons.magSize, 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "+220 Max Ammo", modIcons.carriedAmmo, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Tighter Barrel Alignment", "x0.7 Base Spread", modIcons.baseSpread, 2, 0);
		tier2[1] = new Mod("Improved Gas System", "+0.3 Min Rate of Fire, +2 Max Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Lighter Barrel Assembly", "+0.6 Min Rate of Fire, x1.5 RoF Scaling Rate", modIcons.rateOfFire, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Supercharged Feed Mechanism", "+0.3 Min Rate of Fire, +2 Max Rate of Fire", modIcons.rateOfFire, 3, 0);
		tier3[1] = new Mod("Loaded Rounds", "+2 Area Damage", modIcons.areaDamage, 3, 1);
		tier3[2] = new Mod("High Velocity Rounds", "+4 Direct Damage", modIcons.directDamage, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hardened Rounds", "+400% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Shrapnel Rounds", "+0.6m AoE Radius", modIcons.aoeRadius, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Feedback Loop", "x1.1 Direct and Area Damage when at Max Rate of Fire", modIcons.directDamage, 5, 0);
		tier5[1] = new Mod("Suppressive Fire", "Deal 0.5 Fear to enemies within a 1m radius of bullet impact. The radius of the Fear gets the same increases as the Area Damage.", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Damage Resistance At Full RoF", "33% Damage Resistance when at Max Rate of Fire", modIcons.damageResistance, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Drums", "+110 Max Ammo, -0.5 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Splintering Shells", "+1 Area Damage, +0.3m AoE Radius", overclockIcons.aoeRadius, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Carpet Bomber", "+3 Area Damage, +0.6m AoE Radius, -7 Direct Damage", overclockIcons.areaDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Combat Mobility", "Increases movement speed while using from 50% to 85% of normal walk speed, +0.9 Min Rate of Fire, x1.5 RoF Scaling Rate, x0.7 Base Spread, x0.5 Magazine Size", overclockIcons.movespeed, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Big Bertha", "+12 Direct Damage, x0.7 Base Spread, x0.5 Magazine Size, -110 Max Ammo, -1.5 Max Rate of Fire", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neurotoxin Payload", "50% Chance to inflict a Neurotoxin DoT that deals an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + 
				" Poison Damage per Second and slows enemies by 30% for 10 seconds to all enemies within the AoE Radius upon impact. +0.6m AoE Radius, -2 Direct Damage, -5 Area Damage", overclockIcons.neurotoxin, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public Autocannon clone() {
		return new Autocannon(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "Autocannon";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.gunnerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.autocannonGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedTier1 == 0) {
			toReturn += 3;
		}
		if (selectedTier3 == 2) {
			toReturn += 4;
		}
		if (selectedOverclock == 2) {
			toReturn -= 7;
		}
		else if (selectedOverclock == 4) {
			toReturn += 12;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 2;
		}
		
		// Multiplicative bonuses last
		if (selectedTier5 == 0) {
			toReturn *= feedbackLoopMultiplier();
		}
		
		return toReturn;
	}
	private double getAreaDamage() {
		double toReturn = areaDamage;
		
		// Additive bonuses first
		if (selectedTier3 == 1) {
			toReturn += 2;
		}
		if (selectedOverclock == 1) {
			toReturn += 1;
		}
		else if (selectedOverclock == 2) {
			toReturn += 3;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 5;
		}
		
		// Multiplicative bonuses last
		if (selectedTier5 == 0) {
			toReturn *= feedbackLoopMultiplier();
		}
		
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier4 == 1) {
			toReturn += 0.6;
		}
		if (selectedOverclock == 1) {
			toReturn += 0.3;
		}
		else if (selectedOverclock == 2 || selectedOverclock == 5) {
			toReturn += 0.6;
		}
		return toReturn;
	}
	private double getFearRadius() {
		// Dagadegatto informed me that the AoE Radius upgrades apply equally to the Radial Damage as well as the Fear.
		double toReturn = 1.0;
		
		if (selectedTier4 == 1) {
			toReturn += 0.6;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 0.3;
		}
		else if (selectedOverclock == 2 || selectedOverclock == 5) {
			toReturn += 0.6;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedTier1 == 1) {
			toReturn *= 2.0;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		if (selectedTier1 == 2) {
			toReturn += 220;
		}
		if (selectedOverclock == 0) {
			toReturn += 110;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 110;
		}
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = movespeedWhileFiring;
		if (selectedOverclock == 3) {
			modifier += 0.35;
		}
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private double getIncreaseScalingRate() {
		double toReturn = increaseScalingRate;
		
		if (selectedTier2 == 2) {
			toReturn *= 1.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getMinRateOfFire() {
		double toReturn = minRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 0.3;
		}
		else if (selectedTier2 == 2) {
			toReturn += 0.6;
		}
		
		if (selectedTier3 == 0) {
			toReturn += 0.3;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 0.9;
		}
		
		return toReturn;
	}
	private double getMaxRateOfFire() {
		double toReturn = maxRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		if (selectedOverclock == 4) {
			toReturn -= 1.5;
		}
		return toReturn;
	}
	private double avgRoFDuringRampup() {
		double startRoF = getMinRateOfFire();
		double maxRoF = getMaxRateOfFire();
		double scalingRate = getIncreaseScalingRate();
		double timeToFullRoF = Math.log(maxRoF / startRoF) / scalingRate;
		double exactNumBullets = (startRoF / scalingRate) * (Math.pow(Math.E, scalingRate * timeToFullRoF) - 1);
		return exactNumBullets / timeToFullRoF;
	}
	private int getNumBulletsRampup() {
		double startRoF = getMinRateOfFire();
		double maxRoF = getMaxRateOfFire();
		double scalingRate = getIncreaseScalingRate();
		double timeToFullRoF = Math.log(maxRoF / startRoF) / scalingRate;
		double exactNumBullets = (startRoF / scalingRate) * (Math.pow(Math.E, scalingRate * timeToFullRoF) - 1);
		return (int) Math.round(exactNumBullets);
	}
	private double getAverageRateOfFire() {
		// Special case: When T2.C and OC Big Bertha get combined, the Min RoF >= Max RoF. When T3.A is equipped, this is no longer the case.
		if (selectedTier2 == 2 && selectedTier3 != 0 && selectedOverclock == 4) {
			return getMaxRateOfFire();
		}
		
		int numBulletsRampup = getNumBulletsRampup();
		int magSize = getMagazineSize();
		return (avgRoFDuringRampup() * numBulletsRampup + getMaxRateOfFire() * (magSize - numBulletsRampup)) / magSize;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedOverclock == 0) {
			toReturn -= 0.5;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 0) {
			toReturn *= 0.7;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn *= 0.7;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 0) {
			return 5.0;
		}
		else {
			return 1.0;
		}
	}
	
	private double feedbackLoopMultiplier() {
		return averageBonusPerMagazineForLongEffects(1.1, getNumBulletsRampup(), getMagazineSize());
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[16];
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 2 || selectedTier5 == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamageModified = selectedTier3 == 1 || selectedTier5 == 0 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], modIcons.aoeRadius, aoeRadiusModified);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean minRoFModified = selectedTier2 > 0 || selectedTier3 == 0 || selectedOverclock == 3;
		toReturn[5] = new StatsRow("Starting Rate of Fire:", getMinRateOfFire(), modIcons.rateOfFire, minRoFModified);
		
		boolean maxRoFModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Max Rate of Fire:", getMaxRateOfFire(), modIcons.rateOfFire, maxRoFModified);
		
		toReturn[7] = new StatsRow("RoF Scaling Rate:", getIncreaseScalingRate(), modIcons.blank, selectedTier2 == 2 || selectedOverclock == 3);
		
		toReturn[8] = new StatsRow("Number of Bullets Fired Before Max RoF:", getNumBulletsRampup(), modIcons.special, false);
		
		toReturn[9] = new StatsRow("Average Rate of Fire:", getAverageRateOfFire(), modIcons.rateOfFire, minRoFModified || maxRoFModified);
		
		toReturn[10] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 0);
		
		toReturn[11] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[12] = new StatsRow("Fear Factor:", 0.5, modIcons.fear, selectedTier5 == 1, selectedTier5 == 1);
		
		boolean baseSpreadModified = selectedTier2 == 0 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[13] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[14] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), modIcons.movespeed, selectedOverclock == 3);
		
		toReturn[15] = new StatsRow("Damage Resistance at Full RoF:", "33%", modIcons.damageResistance, selectedTier5 == 2, selectedTier5 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return true;
	}
	
	@Override
	protected void recalculateAoEEfficiency() {
		aoeEfficiency = calculateAverageAreaDamage(getAoERadius(), 0.75, 0.5);
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = ((double) getMagazineSize()) / getAverageRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		}
		
		int magSize = getMagazineSize();
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
		
		double neuroDPS = 0;
		if (selectedOverclock == 5) {
			// Neurotoxin Payload has a 50% chance to inflict the DoT
			if (burst) {
				neuroDPS = calculateRNGDoTDPSPerMagazine(0.5, DoTInformation.Neuro_DPS, getMagazineSize());
			}
			else {
				neuroDPS = DoTInformation.Neuro_DPS;
			}
		}
		
		// I'm choosing to model this as if the splash damage from every bullet were to hit the primary target, even if the bullets themselves don't.
		// TODO: maybe multiply Area Damage by its AoE Efficiency coefficient?
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + magSize * areaDamage) / duration + neuroDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		double magSize = (double) getMagazineSize();
		double areaDamage = getAreaDamage();
		
		double areaDamagePerMag = areaDamage * aoeEfficiency[1] * magSize;
		double sustainedAdditionalDPS = areaDamagePerMag / timeToFireMagazineAndReload;
		
		if (selectedOverclock == 5) {
			sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedAdditionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double damagePerBullet = getDirectDamage() + getAreaDamage() * aoeEfficiency[2] * aoeEfficiency[1];
		double damagePerMagazine = getMagazineSize() * damagePerBullet;
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedOverclock == 5) {
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.5) / getAverageRateOfFire();
			double neurotoxinDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = aoeEfficiency[2] * (calculateFiringDuration() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return damagePerMagazine * numberOfMagazines + neurotoxinDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return (int) aoeEfficiency[2];
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getAverageRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
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
		double horizontalBaseSpread = 22.0 * getBaseSpread();
		double verticalBaseSpread = 8.0 * getBaseSpread();
		double recoilPitch = 30.0;
		double recoilYaw = 40.0;
		double mass = 1.0;
		double springStiffness = 200.0;
		
		return accEstimator.calculateRectangularAccuracy(weakpointAccuracy, horizontalBaseSpread, verticalBaseSpread, recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
//		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
//		double[] directDamage = new double[5];
//		directDamage[0] = getDirectDamage();  // Kinetic
//
//		double[] areaDamage = new double[5];
//		areaDamage[1] = getAreaDamage();  // Explosive
//
//		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
//		double[] dot_dps = new double[4];
//		double[] dot_duration = new double[4];
//		double[] dot_probability = new double[4];
//
//		if (selectedOverclock == 5) {
//			dot_dps[1] = DoTInformation.Neuro_DPS;
//			dot_duration[1] = DoTInformation.Neuro_SecsDuration;
//			dot_probability[1] = 0.5;
//		}
//
//		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability,
//															0.0, getArmorBreaking(), getAverageRateOfFire(), 0.0, 0.0,
//															statusEffects[1], statusEffects[3], false, false);
//		return MathUtils.sum(breakpoints);
		return 0;
	}

	@Override
	public double utilityScore() {
		// OC "Combat Mobility" increases Gunner's movespeed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Mod Tier 5 "Damage Resist" gives 33% damage reduction at max RoF
		if (selectedTier5 == 2) {
			double EHPmultiplier = (1 / (1 - 0.33));
			
			int numBulletsRampup = getNumBulletsRampup();
			int magSize = getMagazineSize();
			double minRoF = getMinRateOfFire();
			double maxRoF = getMaxRateOfFire();
			
			double fullRoFUptime;
			// Special case: when Min RoF == Max RoF the timeRampingUp is zero due to numBulletsRampup == 0.
			if (minRoF >= maxRoF) {
				fullRoFUptime = 1;
			}
			else {
				double timeRampingUp = numBulletsRampup / Math.log(maxRoF / getMinRateOfFire()) / getIncreaseScalingRate(); 
				double timeAtMaxRoF = (magSize - numBulletsRampup) / maxRoF;
				
				fullRoFUptime = timeAtMaxRoF / (timeRampingUp + timeAtMaxRoF);
			}
			
			utilityScores[1] = fullRoFUptime * EHPmultiplier * UtilityInformation.DamageResist_Utility;
		}
		else {
			utilityScores[1] = 0;
		}
		
		// Light Armor Breaking probability
		double AB = getArmorBreaking();
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double directDamageAB = calculateProbabilityToBreakLightArmor(directDamage + areaDamage, AB);
		double areaDamageAB = calculateProbabilityToBreakLightArmor(aoeEfficiency[1] * areaDamage, AB);
		// Average out the Area Damage Breaking and Direct Damage Breaking
		utilityScores[2] = (directDamageAB + (aoeEfficiency[2] - 1) * areaDamageAB) * UtilityInformation.ArmorBreak_Utility / aoeEfficiency[2];
		
		// OC "Neurotoxin Payload" has a 50% chance to inflict a 30% slow by poisoning enemies
		if (selectedOverclock == 5) {
			utilityScores[3] = 0.5 * calculateMaxNumTargets() * DoTInformation.Neuro_SecsDuration * UtilityInformation.Neuro_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// According to MikeGSG, Mod Tier 5 "Suppressive Fire" does 0.5 Fear in a 1m radius. U35.3 patchnotes say that the radius scales with AoE Radius upgrades now. 
		if (selectedTier5 == 1) {
			int numGlyphidsFeared = calculateNumGlyphidsInRadius(getFearRadius(), false);
			double probabilityToFear = calculateFearProcProbability(0.5);
			double fearDuration = 0;
			if (selectedOverclock == 5) {
				fearDuration = EnemyInformation.averageFearDuration(UtilityInformation.Neuro_Slow_Utility, 10.0);
			}
			else {
				fearDuration = EnemyInformation.averageFearDuration();
			}
			utilityScores[4] = probabilityToFear * numGlyphidsFeared * fearDuration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}

	@Override
	public double damagePerMagazine() {
		double damagePerBullet = getDirectDamage() + getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		return damagePerBullet * getMagazineSize();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getAverageRateOfFire();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), getArmorBreaking(), 0.0, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
