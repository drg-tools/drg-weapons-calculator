package gunnerWeapons;

import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Autocannon extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int areaDamage;
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
		increaseScalingRate = 0.3;
		minRateOfFire = 3.0;
		maxRateOfFire = 5.5;
		reloadTime = 5.0;  // seconds
		
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
		tier1[1] = new Mod("High Capacity Magazine", "+110 Magazine Size", modIcons.magSize, 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "+220 Max Ammo", modIcons.carriedAmmo, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Tighter Barrel Alignment", "-30% Base Spread", modIcons.baseSpread, 2, 0);
		tier2[1] = new Mod("Improved Gas System", "+0.2 Min Rate of Fire, +1.5 Max Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Lighter Barrel Assembly", "+1 Min Rate of Fire, x2 RoF Scaling Rate", modIcons.rateOfFire, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Supercharged Feed Mechanism", "+0.6 Min Rate of Fire, +2 Max Rate of Fire", modIcons.rateOfFire, 3, 0);
		tier3[1] = new Mod("Loaded Rounds", "+2 Area Damage", modIcons.areaDamage, 3, 1);
		tier3[2] = new Mod("High Velocity Rounds", "+4 Direct Damage", modIcons.directDamage, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Penetrating Rounds", "+400% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Shrapnel Rounds", "+0.6 AoE Radius", modIcons.aoeRadius, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Feedback Loop", "x1.2 Direct and Area Damage when at Max Rate of Fire", modIcons.directDamage, 5, 0);
		tier5[1] = new Mod("Suppressive Fire", "50% chance to inflict Fear to enemies within a 1m radius on impact.", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Damage Resistance At Full RoF", "33% Damage Resistance when at Max Rate of Fire", modIcons.damageResistance, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Drums", "+110 Max Ammo, -0.5 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Splintering Shells", "+1 Area Damage, +0.3 AoE Radius", overclockIcons.aoeRadius, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Carpet Bomber", "+3 Area Damage, +0.7 AoE Radius, -6 Direct Damage", overclockIcons.areaDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Combat Mobility", "Increases movement speed while using from 50% to 65% of normal walk speed, -2 Direct Damage", overclockIcons.movespeed, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Big Bertha", "+12 Direct Damage, -30% Base Spread, x0.5 Magazine Size, -110 Max Ammo, -1.5 Max Rate of Fire", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neurotoxin Payload", "30% Chance to inflict a Neurotoxin DoT that deals an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + 
				" Poison Damage per Second to all enemies within the AoE Radius upon impact. +0.3 AoE Radius, -3 Direct Damage, -6 Area Damage", overclockIcons.neurotoxin, 5);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		boolean combinationIsValid = true;
		char[] symbols = combination.toCharArray();
		if (combination.length() != 6) {
			System.out.println(combination + " does not have 6 characters, which makes it invalid");
			combinationIsValid = false;
		}
		else {
			List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
			for (int i = 0; i < 5; i ++) {
				if (!validModSymbols.contains(symbols[i])) {
					System.out.println("Symbol #" + (i+1) + ", " + symbols[i] + ", is not a capital letter between A-C or a hyphen");
					combinationIsValid = false;
				}
			}
			if (symbols[3] == 'C') {
				System.out.println("Autocannon's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			switch (symbols[0]) {
				case '-': {
					selectedTier1 = -1;
					break;
				}
				case 'A': {
					selectedTier1 = 0;
					break;
				}
				case 'B': {
					selectedTier1 = 1;
					break;
				}
				case 'C': {
					selectedTier1 = 2;
					break;
				}
			}
			
			switch (symbols[1]) {
				case '-': {
					selectedTier2 = -1;
					break;
				}
				case 'A': {
					selectedTier2 = 0;
					break;
				}
				case 'B': {
					selectedTier2 = 1;
					break;
				}
				case 'C': {
					selectedTier2 = 2;
					break;
				}
			}
			
			switch (symbols[2]) {
				case '-': {
					selectedTier3 = -1;
					break;
				}
				case 'A': {
					selectedTier3 = 0;
					break;
				}
				case 'B': {
					selectedTier3 = 1;
					break;
				}
				case 'C': {
					selectedTier3 = 2;
					break;
				}
			}
			
			switch (symbols[3]) {
				case '-': {
					selectedTier4 = -1;
					break;
				}
				case 'A': {
					selectedTier4 = 0;
					break;
				}
				case 'B': {
					selectedTier4 = 1;
					break;
				}
			}
			
			switch (symbols[4]) {
				case '-': {
					selectedTier5 = -1;
					break;
				}
				case 'A': {
					selectedTier5 = 0;
					break;
				}
				case 'B': {
					selectedTier5 = 1;
					break;
				}
				case 'C': {
					selectedTier5 = 2;
					break;
				}
			}
			
			switch (symbols[5]) {
				case '-': {
					selectedOverclock = -1;
					break;
				}
				case '1': {
					selectedOverclock = 0;
					break;
				}
				case '2': {
					selectedOverclock = 1;
					break;
				}
				case '3': {
					selectedOverclock = 2;
					break;
				}
				case '4': {
					selectedOverclock = 3;
					break;
				}
				case '5': {
					selectedOverclock = 4;
					break;
				}
				case '6': {
					selectedOverclock = 5;
					break;
				}
			}
			
			// Re-set AoE Efficiency
			setAoEEfficiency();
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
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

	private int getDirectDamage() {
		int toReturn = directDamage;
		if (selectedTier1 == 0) {
			toReturn += 3;
		}
		if (selectedTier3 == 2) {
			toReturn += 4;
		}
		if (selectedOverclock == 2) {
			toReturn -= 6;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 2;
		}
		else if (selectedOverclock == 4) {
			toReturn += 12;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		return toReturn;
	}
	private int getAreaDamage() {
		int toReturn = areaDamage;
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
			toReturn -= 6;
		}
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier4 == 1) {
			toReturn += 0.6;
		}
		if (selectedOverclock == 1 || selectedOverclock == 5) {
			toReturn += 0.3;
		}
		else if (selectedOverclock == 2) {
			toReturn += 0.7;
		}
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedTier1 == 1) {
			toReturn *= 2.0;
		}
		
		if (selectedOverclock == 4) {
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
			modifier += 0.15;
		}
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private double getIncreaseScalingRate() {
		double toReturn = increaseScalingRate;
		if (selectedTier2 == 2) {
			toReturn += 0.3;
		}
		return toReturn;
	}
	private double getMinRateOfFire() {
		double toReturn = minRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 0.2;
		}
		else if (selectedTier2 == 2) {
			toReturn += 1.0;
		}
		
		if (selectedTier3 == 0) {
			toReturn += 0.6;
		}
		return toReturn;
	}
	private double getMaxRateOfFire() {
		double toReturn = maxRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 1.5;
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
		// Special case: When T2.C and OC Big Bertha get combined, the Min RoF == Max RoF
		if (selectedTier2 == 2 && selectedOverclock == 4) {
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
		if (selectedTier2 == 0 && selectedOverclock == 4) {
			toReturn -= 0.5;
		}
		else if (selectedTier2 == 0 || selectedOverclock == 4) {
			toReturn -= 0.3;
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
		double magSize = getMagazineSize();
		double numBulletsRampup = getNumBulletsRampup();
		return (numBulletsRampup + 1.2*(magSize - numBulletsRampup)) / magSize;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 2 || (selectedOverclock > 1 && selectedOverclock < 6);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		boolean areaDamageModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], aoeRadiusModified);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), selectedTier1 == 1 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		boolean minRoFModified = selectedTier2 > 0 || selectedTier3 == 0;
		toReturn[5] = new StatsRow("Starting Rate of Fire:", getMinRateOfFire(), minRoFModified);
		
		boolean maxRoFModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Max Rate of Fire:", getMaxRateOfFire(), maxRoFModified);
		
		toReturn[7] = new StatsRow("Number of Bullets Fired Before Max RoF:", getNumBulletsRampup(), false);
		
		toReturn[8] = new StatsRow("Average Rate of Fire:", getAverageRateOfFire(), minRoFModified || maxRoFModified);
		
		toReturn[9] = new StatsRow("Reload Time:", getReloadTime(), selectedOverclock == 0);
		
		toReturn[10] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[11] = new StatsRow("Fear Chance:", "20% (?)", selectedTier5 == 1, selectedTier5 == 1);
		
		boolean baseSpreadModified = selectedTier2 == 0 || selectedOverclock == 4;
		toReturn[12] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		toReturn[13] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 3);
		
		toReturn[14] = new StatsRow("Damage Resistance at Full RoF:", "33%", selectedTier5 == 2, selectedTier5 == 2);
		
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
	protected void setAoEEfficiency() {
		aoeEfficiency =  calculateAverageAreaDamage(getAoERadius(), 0.75, 0.5);
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
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
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
			areaDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		if (selectedTier5 == 0) {
			double feedbackLoopMultiplier = feedbackLoopMultiplier();
			directDamage *= feedbackLoopMultiplier;
			areaDamage *= feedbackLoopMultiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		double neuroDPS = 0;
		if (selectedOverclock == 5) {
			// Neurotoxin Payload has a 30% chance to inflict the DoT
			if (burst) {
				neuroDPS = calculateRNGDoTDPSPerMagazine(0.3, DoTInformation.Neuro_DPS, getMagazineSize());
			}
			else {
				neuroDPS = DoTInformation.Neuro_DPS;
			}
		}
		
		// I'm choosing to model this as if the splash damage from every bullet were to hit the primary target, even if the bullets themselves don't.
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + magSize * areaDamage) / duration + neuroDPS;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus, int numTargets) {
		// TODO: I'd like to refactor out this method if possible
		double damagePerBullet;
		double averageAreaDamage;
		if (numTargets > 1) {
			averageAreaDamage = aoeEfficiency[1];
		}
		else {
			averageAreaDamage = 1.0;
		}
		
		if (weakpointBonus) {
			damagePerBullet = increaseBulletDamageForWeakpoints(getDirectDamage()) + numTargets * getAreaDamage() * averageAreaDamage;
		}
		else {
			damagePerBullet = getDirectDamage() + numTargets * getAreaDamage() * averageAreaDamage;
		}
		double magSize = (double) getMagazineSize();
		double damageMultiplier = 1.0;
		if (selectedTier5 == 0) {
			damageMultiplier = feedbackLoopMultiplier();
		}
		return damagePerBullet * magSize * damageMultiplier;
	}

	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false, false);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(false, true, true);
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		double magSize = (double) getMagazineSize();
		double areaDamage = getAreaDamage();
		
		if (selectedTier5 == 0) {
			areaDamage *= feedbackLoopMultiplier();
		}
		
		double areaDamagePerMag = areaDamage * aoeEfficiency[1] * magSize;
		double sustainedAdditionalDPS = areaDamagePerMag / timeToFireMagazineAndReload;
		
		if (selectedOverclock == 5) {
			sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedAdditionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// TODO: refactor this
		int numTargets = (int) aoeEfficiency[2];
		double damagePerMagazine = calculateDamagePerMagazine(false, numTargets);
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedOverclock == 5) {
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.3) / getAverageRateOfFire();
			double neurotoxinDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
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
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double crosshairHeightPixels, crosshairWidthPixels;
		
		if (selectedTier2 == 0 && selectedOverclock == 4) {
			// Base Spead = 50%
			crosshairHeightPixels = 96;
			crosshairWidthPixels = 206;
		}
		else if (selectedTier2 == 0 || selectedOverclock == 4) {
			// Base Spread = 70%;
			crosshairHeightPixels = 125;
			crosshairWidthPixels = 279;
		}
		else {
			// Base Spread = 100%
			crosshairHeightPixels = 162;
			crosshairWidthPixels = 397;
		}
		
		return AccuracyEstimator.calculateRectangularAccuracy(weakpointAccuracy, false, crosshairWidthPixels, crosshairHeightPixels);
	}
	
	@Override
	public int breakpoints() {
		double dmgMultiplier = 1.0;
		
		if (selectedTier5 == 0) {
			dmgMultiplier = feedbackLoopMultiplier();
		}
		
		double[] directDamage = {
			getDirectDamage() * dmgMultiplier,  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			getAreaDamage() * dmgMultiplier,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double timeToNeurotoxin = MathUtils.meanRolls(0.3) / getAverageRateOfFire();
		double ntDoTDmg = calculateAverageDoTDamagePerEnemy(timeToNeurotoxin, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
		double[] DoTDamage = {
			0,  // Fire
			0,  // Electric
			ntDoTDmg,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, 0.0, 0.0, 0.0);
		return MathUtils.sum(breakpoints);
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
			if (minRoF == maxRoF) {
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
		
		// OC "Neurotoxin Payload" has a 30% chance to inflict a 30% slow by poisoning enemies
		if (selectedOverclock == 5) {
			utilityScores[3] = 0.3 * calculateMaxNumTargets() * DoTInformation.Neuro_SecsDuration * UtilityInformation.Neuro_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// According to MikeGSG, Mod Tier 5 "Suppressive Fire" does 0.5 Fear in a 1m radius
		if (selectedTier5 == 1) {
			int numGlyphidsFeared = 5;  // calculateNumGlyphidsInRadius(1.0);
			utilityScores[4] = 0.5 * numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}

	@Override
	public double damagePerMagazine() {
		double damagePerBullet = getDirectDamage() + getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		double magSize = getMagazineSize();
		double damageMultiplier = 1.0;
		if (selectedTier5 == 0) {
			damageMultiplier = feedbackLoopMultiplier();
		}
		return damagePerBullet * magSize * damageMultiplier;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getAverageRateOfFire();
	}
}
