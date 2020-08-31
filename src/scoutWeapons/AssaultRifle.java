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
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public class AssaultRifle extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double weakpointStunChance;
	private double stunDuration;
	private double reloadTime;
	private double weakpointBonus;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public AssaultRifle() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public AssaultRifle(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public AssaultRifle(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Deepcore GK2";
		weaponPic = WeaponPictures.assaultRifle;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 15;
		carriedAmmo = 350;
		magazineSize =25;
		rateOfFire = 7.0;
		weakpointStunChance = 0.1;
		stunDuration = 1.5;
		reloadTime = 1.8;
		weakpointBonus = 0.1;
		
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
		tier1[0] = new Mod("Gyro Stabilisation", "x0 Base Spread", modIcons.baseSpread, 1, 0);
		tier1[1] = new Mod("Supercharged Feed Mechanism", "+2 Rate of Fire", modIcons.rateOfFire, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Increased Caliber Rounds", "+2 Direct Damage", modIcons.directDamage, 2, 0);
		tier2[1] = new Mod("Expanded Ammo Bags", "+100 Max Ammo", modIcons.carriedAmmo, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Floating Barrel", "x0.5 Recoil", modIcons.recoil, 3, 0);
		tier3[1] = new Mod("Improved Propellant", "+1 Direct Damage", modIcons.directDamage, 3, 1);
		tier3[2] = new Mod("High Capacity Magazine", "+10 Magazine Size", modIcons.magSize, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hollow-Point Bullets", "+20% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("Hardened Rounds", "+500% Armor Breaking", modIcons.armorBreaking, 4, 1);
		tier4[2] = new Mod("Improved Gas System", "+2 Rate of Fire", modIcons.rateOfFire, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Battle Frenzy", "After killing an enemy, gain +50% Movement Speed for 2.5 seconds", modIcons.movespeed, 5, 0);
		tier5[1] = new Mod("Battle Cool", "After killing an enemy, Spread Recovery Speed gets increased by x12.5 for 1.5 seconds", modIcons.baseSpread, 5, 1);
		tier5[2] = new Mod("Stun", "+30% chance to Stun on Weakpoint hit", modIcons.stun, 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Ammo", "+5 Magazine Size, x0.7 Recoil", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Gas Rerouting", "+1 Rate of Fire, -0.3 Reload Time", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, overclockIcons.homebrewPowder, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Overclocked Firing Mechanism", "+3 Rate of Fire, x2.5 Recoil", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Bullets of Mercy", "+33% Damage dealt to enemies that are burning, electrocuted, poisoned, stunned, or frozen. In exchange, -5 Magazine Size", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "AI Stability Engine", "x0 Recoil, x10 Spread Recovery Speed, -1 Direct Damage, -2 Rate of Fire", overclockIcons.baseSpread, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Electrifying Reload", "If any bullets from a magazine damage an enemy's healthbar, then those enemies will have an Electrocute DoT applied when that "
				+ "magazine gets reloaded. Electrocute does an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per Second for 3 seconds. -3 Direct Damage, -5 Magazine Size", overclockIcons.specialReload, 6);
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
				System.out.println("Deepcore's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Deepcore's second tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-7 or a hyphen");
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
				case 'C': {
					setSelectedModAtTier(4, 2, false);
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
				case '7': {
					setSelectedOverclock(6, false);
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
	public AssaultRifle clone() {
		return new AssaultRifle(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "AssaultRifle";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.assaultRifleGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// First do additive bonuses
		if (selectedTier2 == 0) {
			toReturn += 2;
		}
		if (selectedTier3 == 1) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 5) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 6) {
			toReturn -= 3;
		}
		
		// Then do multiplicative bonuses
		if (selectedOverclock == 2) {
			toReturn *= homebrewPowderCoefficient;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 1) {
			toReturn += 100;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 2) {
			toReturn += 10;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 5;
		}
		else if (selectedOverclock == 4 || selectedOverclock == 6) {
			toReturn -= 5;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier1 == 1) {
			toReturn += 2.0;
		}
		if (selectedTier4 == 2) {
			toReturn += 2.0;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 3) {
			toReturn += 3.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getWeakpointStunChance() {
		double toReturn = weakpointStunChance;
		
		if (selectedTier5 == 2) {
			toReturn += 0.3;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedOverclock == 1) {
			toReturn -= 0.3;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 0) {
			toReturn += 0.2;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 1) {
			return 6.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		if (selectedTier1 == 0) {
			return 0.0;
		}
		else {
			return 1.0;
		}
	}
	private double getSpreadRecoverySpeed() {
		// I'm choosing to model it as if these two effects do not stack.
		if (selectedOverclock == 5) {
			return 10.0;
		}
		else if (selectedTier5 == 1) {
			// According to the MikeGSG, Battle Cool increases Spread Recovery Speed by x12.5 for 1.5 seconds after a kill.
			return averageBonusPerMagazineForShortEffects(12.5, 1.5, true, 0.0, getMagazineSize(), getRateOfFire());
		}
		else {
			return 1.0;
		}
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 0) {
			toReturn *= 0.7;
		}
		else if (selectedOverclock == 3) {
			toReturn *= 2.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		boolean directDamageModified = selectedTier2 == 0 || selectedTier3 == 1 || selectedOverclock == 2 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean magSizeModified = selectedTier3 == 2 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, selectedTier2 == 1);
		
		boolean rofModified = selectedTier1 == 1 || selectedTier4 == 2 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[3] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, rofModified);
		
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0);
		
		toReturn[6] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 1, selectedTier4 == 1);
		
		toReturn[7] = new StatsRow("Weakpoint Stun Chance:", convertDoubleToPercentage(getWeakpointStunChance()), modIcons.homebrewPowder, selectedTier5 == 2);
		
		toReturn[8] = new StatsRow("Stun Duration:", stunDuration, modIcons.stun, false);
		
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, selectedTier1 == 0, selectedTier1 == 0);
		
		boolean SRSmodified = selectedTier5 == 1 || selectedOverclock == 5;
		toReturn[10] = new StatsRow("Spread Recovery:", convertDoubleToPercentage(getSpreadRecoverySpeed()), modIcons.baseSpread, SRSmodified, SRSmodified);
		
		boolean recoilModified = selectedTier3 == 0 || selectedOverclock == 0 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[11] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
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
			duration = ((double) getMagazineSize()) / getRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage();
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		// Bullets of Mercy OC damage increase
		if (selectedOverclock == 4) {
			double BoMDamageMultiplier = 1.33;
			if (statusEffects[0] || statusEffects[1] || statusEffects[2] || statusEffects[3]) {
				directDamage *= BoMDamageMultiplier;
			}
			else {
				// If no Status Effects are active, then it only procs on the weapon's built-in Stun.
				directDamage *= averageBonusPerMagazineForShortEffects(BoMDamageMultiplier, 1.5, false, getWeakpointStunChance(), getMagazineSize(), getRateOfFire());
			}
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage, getWeakpointBonus());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		double electroDPS = 0;
		if (selectedOverclock == 6) {
			double electroDoTUptimeCoefficient = Math.min(DoTInformation.Electro_SecsDuration / duration, 1);
			electroDPS += electroDoTUptimeCoefficient * DoTInformation.Electro_DPS;
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage) / duration + electroDPS;
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
		// Deepcore can't hit any additional targets in a single shot
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalDamage = (getMagazineSize() + getCarriedAmmo()) * getDirectDamage();
		
		double electrocutionDoTTotalDamage = 0;
		if (selectedOverclock == 6) {
			double electrocuteDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Electro_SecsDuration, DoTInformation.Electro_DPS);
			double estimatedNumEnemiesKilled = calculateFiringDuration() / averageTimeToKill();
			
			electrocutionDoTTotalDamage = electrocuteDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return totalDamage + electrocutionDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// Deepcore can't hit any additional targets in a single shot
		return 1;
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		/*
			Scout's Assault Rifle seems to use a different model of accuracy than the other guns do. Specifically, it does the following things differently:
			1. The Spread Recovery Speed seems to be non-linear; it seems to be more powerful at the start of the magazine and get weaker near the end
			2. The Spread Recovery starts getting applied on the first shot, whereas all the other guns have it applied on every shot after the first.
			3. When its Base Spread is reduced to 0, the Max Spread doesn't decrease as well (every other gun has Max Spread = Base Spread + Spread Variance)
			
			With those things in mind, I am choosing to model this slightly incorrectly with the current AccuracyEstimator because I want to get things finished up.
			If I keep developing this app, I'd like to come back and make a method specifically for this weapon.
		*/
		
		double unchangingBaseSpread = 19;
		double changingBaseSpread = 21;
		double spreadVariance = 84;
		double spreadPerShot = 30;
		double spreadRecoverySpeed = 170.6869145;
		double recoilPerShot = 41;
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 6.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 2.0 / 3.0;
		
		double[] modifiers = {getBaseSpread(), 1.0, getSpreadRecoverySpeed(), 1.0, getRecoil()};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, accuracyDistance, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval, modifiers);
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			getDirectDamage(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double electroDmg = 0;
		if (selectedOverclock == 6) {
			electroDmg = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Electro_SecsDuration, DoTInformation.Electro_DPS);
		}
		double[] DoTDamage = {
			0,  // Fire
			electroDmg,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, getWeakpointBonus(), 0.0, 0.0, statusEffects[1], statusEffects[3]);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Mod Tier 5 "Battle Frenzy" grants a 50% movespeed increase on kill for 2.5 seconds
		if (selectedTier5 == 0) {
			// Again, using incorrect "guess" Spawn Rates to create believable uptimeCoefficient
			double uptimeCoefficient = Math.min(2.5 / averageTimeToKill(false), 1);
			utilityScores[0] = uptimeCoefficient * MathUtils.round(0.5 * DwarfInformation.walkSpeed, 2) * UtilityInformation.Movespeed_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Electrifying Reload" = 100% chance to electrocute on reload
		if (selectedOverclock == 6) {
			// This formula is entirely made up. It's designed to increase number electrocuted with Mag Size, and decrease it with Rate of Fire.
			int numEnemiesElectrocutedPerMagazine = (int) Math.ceil(2.0 * getMagazineSize() / getRateOfFire());
			utilityScores[3] = numEnemiesElectrocutedPerMagazine * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Innate Weakpoint stun = 10% chance for 1.5 sec stun (improved to 40% by Mod Tier 5 "Stun")
		utilityScores[5] = EnemyInformation.probabilityBulletWillHitWeakpoint() * getWeakpointStunChance() * stunDuration * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		double baseDamage = getMagazineSize() * getDirectDamage();
		
		double electrocutionDoTDamage = 0;
		if (selectedOverclock == 6) {
			electrocutionDoTDamage = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Electro_SecsDuration, DoTInformation.Electro_DPS);
		}
		
		return baseDamage + electrocutionDoTDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ex3\": { \"name\": \"Base Spread\", \"value\": 100, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 2 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 15, 0, 0, 0, 24, 0, tier2[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 2 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 24, 15, 0, 0, 0, tier2[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 100 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 50, 0, 35, 0, 0, tier3[0].getText(true), "{ \"ex4\": { \"name\": \"Recoil\", \"value\": 0.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Recoil", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 50, 0, 0, 0, 0, 35, tier3[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2800, 0, 35, 0, 50, 0, 0, tier3[2].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 10 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 0, 72, 0, 50, 0, 48, tier4[0].getText(true), "{ \"ex5\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 20, \"percent\": true } }", "Icon_Upgrade_Weakspot", "Weak Spot Bonus"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 0, 0, 0, 72, 48, 50, tier4[1].getText(true), "{ \"ex6\": { \"name\": \"Armor Breaking\", \"value\": 500, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 4800, 48, 0, 72, 0, 0, 50, tier4[2].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 2 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 0, 64, 140, 70, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Battle Frenzy\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_MovementSpeed", "Movement Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 0, 0, 140, 0, 70, tier5[1].getText(true), "{ \"ex8\": { \"name\": \"Battle Cool\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 5600, 64, 70, 140, 0, 0, 0, tier5[2].getText(true), "{ \"ex2\": { \"name\": \"Weakpoint Stun Chance\", \"value\": 30, \"percent\": true } }", "Icon_Upgrade_Stun", "Stun"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7250, 0, 125, 0, 0, 80, 105, overclocks[0].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 5 }, "
				+ "\"ex4\": { \"name\": \"Recoil\", \"value\": 0.7, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7800, 105, 0, 0, 60, 0, 125, overclocks[1].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 1 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.3, \"subtract\": true } }", "Icon_Upgrade_FireRate"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 8100, 0, 95, 65, 0, 0, 140, overclocks[2].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": " + homebrewPowderCoefficient + ", \"multiply\": true } }", 
				"Icon_Overclock_ChangeOfHigherDamage"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7950, 65, 95, 0, 0, 120, 0, overclocks[3].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 3 }, "
				+ "\"ex4\": { \"name\": \"Recoil\", \"value\": 2.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_FireRate"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8100, 125, 90, 0, 80, 0, 0, overclocks[4].getText(true), "{ \"ex9\": { \"name\": \"Bonus Damage to Afflicted Targets\", \"value\": 33, \"percent\": true }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 5, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8250, 0, 0, 100, 60, 125, 0, overclocks[5].getText(true), "{ \"ex4\": { \"name\": \"Recoil\", \"value\": 0, \"percent\": true, \"multiply\": true }, "
				+ "\"ex10\": { \"name\": \"Spread Recovery Speed\", \"value\": 10, \"percent\": true, \"multiply\": true }, \"dmg\": { \"name\": \"Damage\", \"value\": 1, \"subtract\": true }, \"rate\": { \"name\": \"Rate of Fire\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_Aim"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[6].getShortcutRepresentation(), overclocks[6].getName(), 7750, 65, 105, 135, 0, 0, 0, overclocks[6].getText(true), "{ \"ex11\": { \"name\": \"Electric Reload (100% chance)\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 3, \"subtract\": true }, \"clip\": { \"name\": \"Magazine Size\", \"value\": 5, \"subtract\": true } }", "Icon_Overclock_Special_Magazine"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
