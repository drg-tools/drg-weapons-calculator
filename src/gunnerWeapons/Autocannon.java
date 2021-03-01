package gunnerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

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
		increaseScalingRate = 0.3;
		minRateOfFire = 3.0;
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
		tier1 = new Mod[2];
		tier1[0] = new Mod("Shrapnel Rounds", "+0.6m AoE Radius", modIcons.aoeRadius, 1, 0);
		tier1[1] = new Mod("Expanded Ammo Bags", "+220 Max Ammo", modIcons.carriedAmmo, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Loaded Rounds", "+2 Area Damage", modIcons.areaDamage, 2, 0);
		tier2[1] = new Mod("Improved Gas System", "+0.2 Min Rate of Fire, +1.5 Max Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] =  new Mod("High Capacity Magazine", "+110 Magazine Size", modIcons.magSize, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Supercharged Feed Mechanism", "+0.6 Min Rate of Fire, +2 Max Rate of Fire", modIcons.rateOfFire, 3, 0);
		tier3[1] = new Mod("Increased Caliber Rounds", "+7 Direct Damage", modIcons.directDamage, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Penetrating Rounds", "+400% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Tighter Barrel Alignment", "x0.7 Base Spread", modIcons.baseSpread, 4, 1);
		tier4[2] = new Mod("Suppressive Fire", "Deal 0.5 Fear to enemies within a 1m radius of bullet impact", modIcons.fear, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Feedback Loop", "x1.2 Direct and Area Damage when at Max Rate of Fire", modIcons.directDamage, 5, 0);
		tier5[1] = new Mod("Lighter Barrel Assembly", "+1 Min Rate of Fire, x2 RoF Scaling Rate", modIcons.rateOfFire, 5, 1);
		tier5[2] = new Mod("Damage Resistance At Full RoF", "33% Damage Resistance when at Max Rate of Fire", modIcons.damageResistance, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Drums", "+110 Max Ammo, -0.5 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Flak Cannon", "If a bullet would miss an enemy but passes within 0.75m of their hitbox, it detonates in midair automatically. Additionally raises Damage Falloff at outer radius from 50% to 90%", overclockIcons.aoeRadius, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Carpet Bomber", "+3 Area Damage, +0.7m AoE Radius, -7 Direct Damage", overclockIcons.areaDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Combat Mobility", "Increases movement speed while using from 50% to 85% of normal walk speed, -1 sec Reload Time, x1.3 Base Spread", overclockIcons.movespeed, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Big Bertha", "+9 Direct Damage, x0.7 Base Spread, x0.5 Magazine Size, -110 Max Ammo, -1.5 Max Rate of Fire", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neurotoxin Payload", "30% Chance to inflict a Neurotoxin DoT that deals an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + 
				" Poison Damage per Second for 10 seconds to all enemies within the AoE Radius upon impact. +0.3m AoE Radius, -3 Area Damage, -110 Max Ammo", overclockIcons.neurotoxin, 5);
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
			if (symbols[0] == 'C') {
				System.out.println("Autocannon's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Autocannon's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			// Start by setting all mods/OC to -1 so that no matter what the old build was, the new build will go through with no problem.
			setSelectedModAtTier(1, -1, false);
			setSelectedModAtTier(2, -1, false);
			setSelectedModAtTier(3, -1, false);
			setSelectedModAtTier(4, -1, false);
			setSelectedModAtTier(5, -1, false);
			setSelectedOverclock(-1, false);
			
			switch (symbols[0]) {
				case 'A': {
					setSelectedModAtTier(1, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(1, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(1, 2, false);
					break;
				}
			}
			
			switch (symbols[1]) {
				case 'A': {
					setSelectedModAtTier(2, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(2, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(2, 2, false);
					break;
				}
			}
			
			switch (symbols[2]) {
				case 'A': {
					setSelectedModAtTier(3, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(3, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(3, 2, false);
					break;
				}
			}
			
			switch (symbols[3]) {
				case 'A': {
					setSelectedModAtTier(4, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(4, 1, false);
					break;
				}
			}
			
			switch (symbols[4]) {
				case 'A': {
					setSelectedModAtTier(5, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(5, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(5, 2, false);
					break;
				}
			}
			
			switch (symbols[5]) {
				case '1': {
					setSelectedOverclock(0, false);
					break;
				}
				case '2': {
					setSelectedOverclock(1, false);
					break;
				}
				case '3': {
					setSelectedOverclock(2, false);
					break;
				}
				case '4': {
					setSelectedOverclock(3, false);
					break;
				}
				case '5': {
					setSelectedOverclock(4, false);
					break;
				}
				case '6': {
					setSelectedOverclock(5, false);
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

	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedTier3 == 1) {
			toReturn += 7;
		}
		if (selectedOverclock == 2) {
			toReturn -= 7;
		}
		else if (selectedOverclock == 4) {
			toReturn += 9;
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
		if (selectedTier2 == 0) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 3;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		
		// Multiplicative bonuses last
		if (selectedTier5 == 0) {
			toReturn *= feedbackLoopMultiplier();
		}
		
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier1 == 0) {
			toReturn += 0.6;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.7;
		}
		else if (selectedOverclock == 5) {
			toReturn += 0.3;
		}
		
		return toReturn;
	}
	private double getFalloff() {
		if (selectedOverclock == 1) {
			return 0.9;
		}
		else {
			return 0.5;
		}
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedTier2 == 2) {
			toReturn *= 2.0;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		if (selectedTier1 == 1) {
			toReturn += 220;
		}
		if (selectedOverclock == 0) {
			toReturn += 110;
		}
		else if (selectedOverclock == 4 || selectedOverclock == 5) {
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
		if (selectedTier5 == 1) {
			toReturn += 0.3;
		}
		return toReturn;
	}
	private double getMinRateOfFire() {
		double toReturn = minRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 0.2;
		}
		
		if (selectedTier3 == 0) {
			toReturn += 0.6;
		}
		
		if (selectedTier5 == 1) {
			toReturn += 1.0;
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
		// Special case: When T2.C and OC Big Bertha get combined, the Min RoF == Max RoF. When T3.A is equipped, this is no longer the case.
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
		else if (selectedOverclock == 3) {
			toReturn -= 1.0;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier4 == 1) {
			toReturn *= 0.7;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.3;
		}
		else if (selectedOverclock == 4) {
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
		return averageBonusPerMagazineForLongEffects(1.2, getNumBulletsRampup(), getMagazineSize());
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[16];

		boolean directDamageModified = selectedTier3 == 1 || selectedTier5 == 0 || selectedOverclock == 2 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamageModified = selectedTier2 == 0 || selectedTier5 == 0 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier1 == 0 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], modIcons.aoeRadius, aoeRadiusModified);
		
		toReturn[3] = new StatsRow("Falloff:", convertDoubleToPercentage(getFalloff()), modIcons.aoeRadius, selectedOverclock == 1);
		
		toReturn[4] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedTier2 == 2 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier1 == 1 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean minRoFModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedTier5 == 1;
		toReturn[6] = new StatsRow("Starting Rate of Fire:", getMinRateOfFire(), modIcons.rateOfFire, minRoFModified);
		
		boolean maxRoFModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[7] = new StatsRow("Max Rate of Fire:", getMaxRateOfFire(), modIcons.rateOfFire, maxRoFModified);
		
		toReturn[8] = new StatsRow("Number of Bullets Fired Before Max RoF:", getNumBulletsRampup(), modIcons.special, false);
		
		toReturn[9] = new StatsRow("Average Rate of Fire:", getAverageRateOfFire(), modIcons.rateOfFire, minRoFModified || maxRoFModified);
		
		toReturn[10] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 0 || selectedOverclock == 3);
		
		toReturn[11] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[12] = new StatsRow("Fear Factor:", 0.5, modIcons.fear, selectedTier4 == 2, selectedTier4 == 2);
		
		boolean baseSpreadModified = selectedTier4 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
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
	protected void setAoEEfficiency() {
		aoeEfficiency =  calculateAverageAreaDamage(getAoERadius(), 0.75, getFalloff());
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
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.3) / getAverageRateOfFire();
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
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		double[] areaDamage = new double[5];
		areaDamage[1] = getAreaDamage();  // Explosive
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		if (selectedOverclock == 5) {
			dot_dps[1] = DoTInformation.Neuro_DPS;
			dot_duration[1] = DoTInformation.Neuro_SecsDuration;
			dot_probability[1] = 0.3;
		}
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															0.0, getArmorBreaking(), getAverageRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
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
		if (selectedTier4 == 2) {
			int numGlyphidsFeared = 5;  // calculateNumGlyphidsInRadius(1.0);
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
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 110 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1200, 0, 0, 0, 25, 0, 0, tier1[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 220 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"ex3\": { \"name\": \"Base Spread\", \"value\": 30, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 0, 0, 0, 15, 24, tier2[1].getText(true), "{ \"rate\": { \"name\": \"Top Rate of Fire\", \"value\": 1.5 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 2000, 0, 15, 0, 0, 24, 0, tier2[2].getText(true), "{ \"ex4\": { \"name\": \"Rate of Fire Growth Speed\", \"value\": 100, \"percent\": true } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 0, 0, 50, 0, 35, tier3[0].getText(true), "{ \"rate\": { \"name\": \"Top Rate of Fire\", \"value\": 2 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 35, 0, 50, 0, 0, 0, tier3[1].getText(true), "{ \"ex1\": { \"name\": \"Area Damage\", \"value\": 2 } }", "Icon_Upgrade_AreaDamage", "Area Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2800, 50, 0, 0, 0, 35, 0, tier3[2].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 4 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 48, 0, 0, 0, 50, 72, tier4[0].getText(true), "{ \"ex5\": { \"name\": \"Armor Breaking\", \"value\": 400, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 50, 0, 48, 0, 0, 72, tier4[1].getText(true), "{ \"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.6 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 64, 70, 0, 140, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Top RoF Damage Bonus\", \"value\": 20, \"percent\": true } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 70, 140, 0, 0, 0, tier5[1].getText(true), "{ \"ex8\": { \"name\": \"Impact Fear AoE\", \"value\": 1 } }", "Icon_Upgrade_ScareEnemies", "Fear"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 5600, 0, 0, 0, 64, 70, 140, tier5[2].getText(true), "{ \"ex9\": { \"name\": \"Damage Resistance at Full RoF\", \"value\": 33, \"percent\": true } }", "Icon_Upgrade_Resistance", "Resistance"),
				exportAllMods || false);
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL(boolean exportAllOCs) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7850, 105, 0, 0, 135, 70, 0, overclocks[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 110 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.5, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7300, 65, 0, 0, 95, 0, 125, overclocks[1].getText(true), "{ \"ex1\": { \"name\": \"Area Damage\", \"value\": 1 }, "
				+ "\"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.3 } }", "Icon_Upgrade_Area"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7350, 105, 0, 70, 120, 0, 0, overclocks[2].getText(true), "{ \"ex1\": { \"name\": \"Area Damage\", \"value\": 3 }, "
				+ "\"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.7 }, \"dmg\": { \"name\": \"Damage\", \"value\": 6, \"subtract\": true } }", "Icon_Upgrade_AreaDamage"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7650, 95, 0, 0, 70, 0, 120, overclocks[3].getText(true), "{ \"ex6\": { \"name\": \"Movement Speed While Using\", \"value\": 35, \"percent\": true }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 1, \"subtract\": true }, \"ex3\": { \"name\": \"Base Spread\", \"value\": 30, \"percent\": true, \"subtract\": true }, \"clip\": { \"name\": \"Magazine Size\", \"value\": 0.5, \"multiply\": true } }", "Icon_Upgrade_MovementSpeed"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8400, 0, 125, 80, 105, 0, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 12 }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 0.5, \"multiply\": true }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 110, \"subtract\": true }, \"ex3\": { \"name\": \"Base Spread\", \"value\": 30, \"percent\": true, \"subtract\": true }, "
				+ "\"rate\": { \"name\": \"Top Rate of Fire\", \"value\": 1.5, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8100, 135, 0, 0, 100, 0, 75, overclocks[5].getText(true), "{ \"ex10\": { \"name\": \"Neurotoxin Payload\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 3, \"subtract\": true }, \"ex1\": { \"name\": \"Area Damage\", \"value\": 6, \"subtract\": true }, \"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.3 } }", "Icon_Overclock_Neuro"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
