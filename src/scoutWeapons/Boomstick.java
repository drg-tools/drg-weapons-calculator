package scoutWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

/*
	Extracted via UUU:
		ShockWaveLength: 150
		Radius: 150
		Distance: 250
		
		ShotgunJump Force: 1000
		Fear Factor Radius: 500
*/

public class Boomstick extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private int numberOfPellets;
	private int frontalConeDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double stunChance;
	private double stunDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Boomstick() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Boomstick(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Boomstick(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Jury-Rigged Boomstick";
		weaponPic = WeaponPictures.boomstick;
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 12;
		numberOfPellets = 8;
		frontalConeDamage = 20;
		carriedAmmo = 24;
		magazineSize = 2;
		rateOfFire = 1.5;
		reloadTime = 2.0;
		stunChance = 0.3;
		stunDuration = 2.5;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+8 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Double-Sized Buckshot", "+3 Damage per Pellet", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Double Trigger", "+7.5 Rate of Fire", modIcons.rateOfFire, 2, 0);
		tier2[1] = new Mod("Quickfire Ejector", "-0.7 Reload Time", modIcons.reloadSpeed, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Stun Duration", "+2.5 seconds Stun duration", modIcons.stun, 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "+12 Max Ammo", modIcons.carriedAmmo, 3, 1);
		tier3[2] = new Mod("High Capacity Shells", "+3 Pellets per Shot", modIcons.pelletsPerShot, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Tungsten Coated Buckshot", "+300% Armor Breaking", modIcons.armorBreaking, 4, 1);
		tier4[2] = new Mod("Improved Blast Wave", "+20 Blastwave Damage to any enemies in the area extending 4m infront of you.", modIcons.special, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Auto Reload", "Reloads automatically when unequipped for more than 5 seconds", modIcons.reloadSpeed, 5, 0, false);
		tier5[1] = new Mod("Fear The Boomstick", "50% Chance to inflict Fear on enemies caught within the Blastwave", modIcons.fear, 5, 1);
		tier5[2] = new Mod("White Phosphorous Shells", "Add 50% of the Damage per Pellet as Heat Damage, which can ignite enemies. Burn DoT does an average of " + MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) + " Fire Damage per Second", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Shells", "+6 Max Ammo, -0.2 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Double Barrel", "Fire both barrels with a single tigger pull. As a result, both Magazine Size and Max Ammo are effectively halved, while the "
				+ "number of Pellets per Shot gets doubled. Additionally, +1 Damage per Pellet.", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Special Powder", "Jump off of the ground and fire the shotgun to \"blast jump\" around the caves for increased mobility.", overclockIcons.shotgunJump, 2);
		overclocks[3] = new Overclock(Overclock.classification.clean, "Stuffed Shells", "+1 Damage per Pellet, +1 Pellet per Shot", overclockIcons.pelletsPerShot, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Shaped Shells", "-35% Base Spread, -2 Pellets per Shot", overclockIcons.baseSpread, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Jumbo Shells", "+8 Damage per Pellet, -10 Max Ammo, +0.5 Reload Time", overclockIcons.directDamage, 5);
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
				System.out.println("Boomstick's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Boomstick's second tier of mods only has two choices, so 'C' is an invalid choice.");
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
				case 'C': {
					selectedTier4 = 2;
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
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Boomstick clone() {
		return new Boomstick(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Boomstick";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.boomstickGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		
		if (selectedTier1 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1 || selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 5) {
			toReturn += 8;
		}
		
		return toReturn;
	}
	private int getNumberOfPellets() {
		int toReturn = numberOfPellets;
		
		if (selectedTier3 == 2) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= 2;
		}
		else if (selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 2;
		}
		
		return toReturn;
	}
	private int getBlastwaveDamage() {
		// Hits enemies within 4m in front of Scout
		// Area damage, instead of direct damage. Bulks + Dreads resist it.
		int toReturn = frontalConeDamage;
		
		if (selectedTier4 == 2) {
			toReturn += 20;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedOverclock == 1) {
			toReturn -= 1;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 8;
		}
		if (selectedTier3 == 1) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 6;
		}
		else if (selectedOverclock == 1) {
			// For the math of Double Barrel to work out correctly, the Carried Ammo should be halved since it fires 2 ammo per shot.
			toReturn /= 2;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 10;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 0) {
			toReturn += 7.5;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 0) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 5) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getStunDuration() {
		double toReturn = stunDuration;
		
		if (selectedTier3 == 0) {
			toReturn += 2.5;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 1) {
			return 4.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		if (selectedOverclock == 4) {
			return 0.65;
		}
		else {
			return 1.0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[13];
		
		boolean damageModified = selectedTier1 == 1 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Damage per Pellet:", getDamagePerPellet(), damageModified);
		
		boolean pelletsModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPellets(), pelletsModified);
		
		toReturn[2] = new StatsRow("Blastwave Damage:", getBlastwaveDamage(), selectedTier4 == 2);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), selectedOverclock == 1);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedTier3 == 1 || selectedOverclock == 0 || selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedTier2 == 0);
		
		boolean reloadTimeModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Reload Time:", getReloadTime(), reloadTimeModified);
		
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), selectedTier4 == 1, selectedTier4 == 1);
		
		toReturn[8] = new StatsRow("Fear Chance:", "50%", selectedTier5 == 1, selectedTier5 == 1);
		
		toReturn[9] = new StatsRow("Stun Chance:", convertDoubleToPercentage(stunChance), false);
		
		toReturn[10] = new StatsRow("Stun Duration:", getStunDuration(), selectedTier3 == 0);
		
		toReturn[11] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[12] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedOverclock == 4, selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Technically the Blastwave is Area Damage, but this flag is for spherical Area Damage like Grenade Launcher or Autocannon.
		return false;
	}
	
	private double calculateTimeToIgnite(boolean accuracy) {
		// This method gets used by the Tier 5 Mod "White Phosphorous Shells"
		int numPelletsThatApplyHeat;
		if (accuracy) {
			numPelletsThatApplyHeat = (int) Math.round(estimatedAccuracy(false) * getNumberOfPellets());
		}
		else {
			numPelletsThatApplyHeat = getNumberOfPellets();
		}
		
		// 50% of Direct Damage from the pellets gets added on as Heat Damage.
		double heatDamagePerShot = 0.5 * getDamagePerPellet() * numPelletsThatApplyHeat;
		if (getMagazineSize() > 1) {
			return EnemyInformation.averageTimeToIgnite(heatDamagePerShot * getRateOfFire());
		}
		else {
			return EnemyInformation.averageTimeToIgnite(heatDamagePerShot / getReloadTime());
		}
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, duration, directWeakpointDamagePerPellet;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		int magSize = getMagazineSize();
		if (magSize > 1) {
			if (burst) {
				duration = ((double) getMagazineSize()) / getRateOfFire();
			}
			else {
				duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			}
		}
		else {
			duration = getReloadTime();
		}
		
		double dmgPerPellet = getDamagePerPellet();
		// Frozen
		if (statusEffects[1]) {
			dmgPerPellet *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			dmgPerPellet *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamagePerPellet = increaseBulletDamageForWeakpoints2(dmgPerPellet);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamagePerPellet = dmgPerPellet;
		}
		
		// They way it's currently modeled, any time the WPS mod and Double Barrel OC are equipped simultaneously, then the Reload Time doesn't affect the Fire DoT Uptime.
		double burnDPS = 0;
		if (selectedTier5 == 2 && !statusEffects[1]) {
			if (burst) {
				double timeToIgnite = calculateTimeToIgnite(accuracy);
				double fireDoTUptimeCoefficient = (duration - timeToIgnite) / duration;
				
				burnDPS = fireDoTUptimeCoefficient * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		int numPelletsPerShot = getNumberOfPellets();
		int pelletsThatHitWeakpointPerShot = (int) Math.round(numPelletsPerShot * weakpointAccuracy);
		int pelletsThatHitTargetPerShot = (int) Math.round(numPelletsPerShot * generalAccuracy) - pelletsThatHitWeakpointPerShot;
		
		return (pelletsThatHitWeakpointPerShot * directWeakpointDamagePerPellet + pelletsThatHitTargetPerShot * getDamagePerPellet() + getBlastwaveDamage()) * magSize / duration + burnDPS;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		// TODO: I'd like to refactor this method out if possible
		double damagePerShot;
		if (weakpointBonus) {
			damagePerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet() * getNumberOfPellets()) + getBlastwaveDamage();
			return (double) damagePerShot * getMagazineSize();
		}
		else {
			damagePerShot = getDamagePerPellet() * getNumberOfPellets() + getBlastwaveDamage();
			return (double) damagePerShot * getMagazineSize();
		}
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
		int magSize = getMagazineSize();
		double secondaryDamage;
		if (selectedTier4 == 0) {
			secondaryDamage = calculateDamagePerMagazine(false);
		}
		else {
			secondaryDamage = getBlastwaveDamage();
		}
		
		double additionalDPS = 0;
		if (magSize > 1) {
			double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			additionalDPS += secondaryDamage / timeToFireMagazineAndReload;
		}
		else {
			additionalDPS += secondaryDamage / getReloadTime();
		}
		
		// Penetrations can ignite, too
		if (selectedTier4 == 0 && selectedTier5 == 2) {
			additionalDPS += DoTInformation.Burn_DPS;
		}
		
		return additionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int directDamagePerShot = getDamagePerPellet() * getNumberOfPellets();
		// The frontal blastwave is a 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
		int gruntsHitByBlastwave = 4;
		int blastwaveDamagePerShot = gruntsHitByBlastwave * getBlastwaveDamage();
		int numTargets = calculateMaxNumTargets();
		int numShots = getMagazineSize() + getCarriedAmmo();
		double totalDamage = numShots * (directDamagePerShot*numTargets + blastwaveDamagePerShot);
		
		double fireDoTTotalDamage = 0;
		if (selectedTier5 == 2) {
			
			double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			double fireDoTDamagePerEnemy;
			if (getMagazineSize() > 1) {
				double timeBeforeIgnite = calculateTimeToIgnite(false);
				fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				
				fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
			}
			else {
				double percentageOfEnemiesIgnitedPerShot = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(0.5 * directDamagePerShot);
				fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				
				fireDoTTotalDamage += numShots * (percentageOfEnemiesIgnitedPerShot * numTargets) * fireDoTDamagePerEnemy;
			}
		}
		
		return totalDamage + fireDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		
		if (magSize > 1) {
			int carriedAmmo = getCarriedAmmo();
			double timeToFireMagazine = ((double) magSize) / getRateOfFire();
			return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
		}
		else {
			// Since each shot gets fired instantly and there's only one shot in the magazine, the rate of fire isn't applicable. Simply add up all the reload times.
			return getCarriedAmmo() * getReloadTime();
		}
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet()) * getNumberOfPellets();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Even though this gun does have significant recoil, it recovers from that recoil entirely in 0.5 seconds. Rather than make an overly 
		// complicated model for 2 shots, I'm just going to use the accuracy for a single shot.
		double crosshairHeightPixels = 156;
		double crosshairWidthPixels;
		
		if (selectedOverclock == 4) {
			// Base Spread = 65%
			crosshairWidthPixels = 305;
		}
		else {
			// Base Spread = 100%
			crosshairWidthPixels = 468;
		}
		return AccuracyEstimator.calculateRectangularAccuracy(weakpointAccuracy, true, crosshairWidthPixels, crosshairHeightPixels);
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			getDamagePerPellet() * getNumberOfPellets(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			getBlastwaveDamage(),  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		// Because White Phosphorus Shells is a burst of Heat, it's not modeled like other DoTs are
		double burstOfHeatPerShot = 0;
		if (selectedTier5 == 2) {
			burstOfHeatPerShot = 0.5 * getDamagePerPellet() * getNumberOfPellets();
		}
		
		double[] DoTDamage = {
			0,  // Fire
			0,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, 0.0, 0.0, burstOfHeatPerShot);
		return MathUtils.sum(breakpoints);
	}
	@Override
	public double utilityScore() {
		// OC "Special Powder" gives a lot of Mobility (7.8m vertical per shot, 13m horizontal per shot)
		if (selectedOverclock == 2) {
			// Multiply by 2 for mobility per shot
			utilityScores[0] = 2 * (0.5 * 7.8 + 0.5 * 13) * UtilityInformation.BlastJump_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Light Armor Breaking probability
		// TODO: Should this Light Armor probability be calculated like its stun/pellet chance?
		int numPelletsThatHitLightArmorPlate = (int) Math.round(getNumberOfPellets() * estimatedAccuracy(false) / 100.0);
		double probabilityToBreakLightArmorPlatePerPellet = calculateProbabilityToBreakLightArmor(getDamagePerPellet() * numPelletsThatHitLightArmorPlate, getArmorBreaking());
		utilityScores[2] = probabilityToBreakLightArmorPlatePerPellet * UtilityInformation.ArmorBreak_Utility;
		
		// Mod Tier 5 "Fear the Boomstick" = 50% chance to Fear in same blast cone as the Blastwave damage
		if (selectedTier5 == 1) {
			// 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
			int gruntsHitByBlastwave = 4;
			utilityScores[4] = 0.5 * gruntsHitByBlastwave * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Innate Stun = 30% chance for 2.5 sec (improved by Mod Tier 3 "Stun Duration")
		// It looks like each shot has a 30% chance for all of its pellets to have 100% stun rate, so more pellets doesn't equal more likely to stun.
		utilityScores[5] = stunChance * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		// 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
		int gruntsHitByBlastwave = 4;
		return getMagazineSize() * (getDamagePerPellet() * getNumberOfPellets() * calculateMaxNumTargets() + getBlastwaveDamage() * gruntsHitByBlastwave);
	}
	
	@Override
	public double timeToFireMagazine() {
		int magSize = getMagazineSize();
		if (magSize > 1) {
			return magSize / getRateOfFire();
		}
		else {
			return 0;
		}
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.add(String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 8 } }", "Icon_Upgrade_Ammo"));
		toReturn.add(String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral"));
		
		// Tier 2
		toReturn.add(String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[0].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 7.5 } }", "Icon_Upgrade_FireRate"));
		toReturn.add(String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[1].getText(true), "{ \"reload\": { \"name\": \"Reload Time\", \"value\": 0.7, \"subtract\": true } }", "Icon_Upgrade_Speed"));
		
		// Tier 3
		toReturn.add(String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[0].getText(true), "{ \"ex9\": { \"name\": \"Stun Duration\", \"value\": 2.5 } }", "Icon_Upgrade_Stun"));
		toReturn.add(String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 12 } }", "Icon_Upgrade_Ammo"));
		toReturn.add(String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2200, 30, 0, 0, 0, 20, 0, tier3[2].getText(true), "{ \"ex1\": { \"name\": \"Pellets\", \"value\": 3 } }", "Icon_Upgrade_Shotgun_Pellet"));
		
		// Tier 4
		toReturn.add(String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 15, 0, 0, 0, 36, 25, tier4[0].getText(true), "{ \"ex3\": { \"name\": \"Max Penetrations\", \"value\": 3 } }", "Icon_Upgrade_BulletPenetration"));
		toReturn.add(String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 0, 36, 25, 0, 15, tier4[1].getText(true), "{ \"ex4\": { \"name\": \"Armor Breaking\", \"value\": 300, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking"));
		toReturn.add(String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 3800, 15, 36, 0, 0, 25, 0, tier4[2].getText(true), "{ \"ex5\": { \"name\": \"Front AoE shock wave Damage\", \"value\": 20 } }", "Icon_Upgrade_Special"));
		
		// Tier 5
		toReturn.add(String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 0, 40, 110, 0, 60, 0, tier5[0].getText(true), "{ \"ex6\": { \"name\": \"Auto Reload\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Speed"));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 110, 0, 0, 40, 0, 60, tier5[1].getText(true), "{ \"ex7\": { \"name\": \"Proximity Fear Chance\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_ScareEnemies"));
		toReturn.add(String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 4400, 0, 60, 40, 0, 0, 110, tier5[2].getText(true), "{ \"ex8\": { \"name\": \"Damage % as Fire\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Heat"));
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.add(String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8550, 65, 0, 120, 0, 0, 100, overclocks[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 6 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_Ammo"));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7950, 0, 0, 125, 100, 75, 0, overclocks[1].getText(true), "{ \"ex10\": { \"name\": \"Double Barrel\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_FireRate"));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7050, 0, 95, 0, 125, 65, 0, overclocks[2].getText(true), "{ \"ex11\": { \"name\": \"Shotgun Jump\", \"value\": 1, \"boolean\": true } }", "Icon_Overclock_ShotgunJump"));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7850, 0, 100, 80, 0, 135, 0, overclocks[3].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 }, "
				+ "\"ex1\": { \"name\": \"Pellets\", \"value\": 1 } }", "Icon_Upgrade_Shotgun_Pellet"));
		
		// Balanced
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7700, 0, 95, 0, 0, 70, 135, overclocks[4].getText(true), "{ \"ex12\": { \"name\": \"Base Spread\", \"value\": 35, \"percent\": true, \"subtract\": true }, "
				+ "\"ex1\": { \"name\": \"Pellets\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_Aim"));
		
		// Unstable
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8800, 0, 65, 0, 0, 105, 125, overclocks[5].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 8 }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 10, \"subtract\": true }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.5 } }", "Icon_Upgrade_DamageGeneral"));
		
		return toReturn;
	}
}
