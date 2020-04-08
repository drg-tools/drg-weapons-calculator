package scoutWeapons;

import java.util.Arrays;
import java.util.List;

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

public class Deepcore extends Weapon {
	
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
	public Deepcore() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Deepcore(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Deepcore(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Deepcore GK2";
		
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
		tier1[0] = new Mod("Gyro Stabilisation", "-100% Base Spread", 1, 0);
		tier1[1] = new Mod("Supercharged Feed Mechanism", "+2 Rate of Fire", 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Increased Caliber Rounds", "+2 Direct Damage", 2, 0);
		tier2[1] = new Mod("Expanded Ammo Bags", "+100 Max Ammo", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Floating Barrel", "x0.5 Recoil", 3, 0);
		tier3[1] = new Mod("Improved Propellant", "+1 Direct Damage", 3, 1);
		tier3[2] = new Mod("High Capacity Magazine", "+10 Magazine Size", 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hollow-Point Bullets", "+20% Weakpoint Bonus", 4, 0);
		tier4[1] = new Mod("Hardened Rounds", "+500% Armor Breaking", 4, 1);
		tier4[2] = new Mod("Improved Gas System", "+2 Rate of Fire", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Battle Frenzy", "After killing an enemy, gain +50% Movement Speed for 3 seconds", 5, 0);
		tier5[1] = new Mod("Battle Cool", "After killing an enemy, Spread per Shot is set to 0 for 1.5 seconds", 5, 1);
		tier5[2] = new Mod("Stun", "+30% chance to Stun on Weakpoint hit", 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Ammo", "+5 Magazine Size, x0.7 Recoil", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Gas Rerouting", "+1 Rate of Fire, -0.3 Reload Time", 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Overclocked Firing Mechanism", "+3 Rate of Fire, x2.5 Recoil", 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Bullets of Mercy", "+33% Damage dealt to enemies that are burning, electrocuted, poisoned, stunned, or frozen. Additionally, -5 Magazine Size", 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "AI Stability Engine", "x0 Recoil, x10 Spread Recovery Speed, -1 Direct Damage, -2 Rate of Fire", 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Electrifying Reload", "If any bullets from a magazine damage an enemy's healthbar, then those enemies will have an Electrocute DoT applied when that "
				+ "magazine gets reloaded. Electrocute does an average of " + DoTInformation.Electro_DPS + " DPS. -3 Direct Damage, -5 Magazine Size", 6);
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
				case '7': {
					selectedOverclock = 6;
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
	public Deepcore clone() {
		return new Deepcore(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Deepcore";
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
	private double getSpreadPerShot() {
		if (selectedTier5 == 1) {
			// According to the Wiki, Battle Cool sets spreadPerShot = 0 for 1.5 seconds after a kill. This effectively lets the Spread decrease during that period due to the constant Spread Recovery.
			// I'm choosing to model this as an averaged reduction on Spread per Shot across the whole magazine instead of trying to model it as the On-Kill effect that it truly is. 
			double battleCoolDuration = 1.5;
			double burstTTK = EnemyInformation.averageHealthPool() / calculateIdealBurstDPS();
			double battleCoolUptimeCoefficient = battleCoolDuration / burstTTK;
			
			double magSize = getMagazineSize();
			double numBulletsPerMagAffected = Math.round(magSize * battleCoolUptimeCoefficient);
			double numBulletsUnaffected = magSize - numBulletsPerMagAffected;
			
			// This could be written simpler by not using the term multiplied by zero, but I'm choosing to write it explicitly to show how the averaging gets done.
			return (0.0 * numBulletsPerMagAffected + 1.0 * numBulletsUnaffected) / magSize;
		}
		else {
			return 1.0;
		}
	}
	private double getSpreadRecoverySpeed() {
		if (selectedOverclock == 5) {
			return 10.0;
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
		StatsRow[] toReturn = new StatsRow[13];
		
		boolean directDamageModified = selectedTier2 == 0 || selectedTier3 == 1 || selectedOverclock == 2 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		boolean magSizeModified = selectedTier3 == 2 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), selectedTier2 == 1);
		
		boolean rofModified = selectedTier1 == 1 || selectedTier4 == 2 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[3] = new StatsRow("Rate of Fire:", getRateOfFire(), rofModified);
		
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 0);
		
		toReturn[6] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), selectedTier4 == 1, selectedTier4 == 1);
		
		toReturn[7] = new StatsRow("Weakpoint Stun Chance:", convertDoubleToPercentage(getWeakpointStunChance()), selectedTier5 == 2);
		
		toReturn[8] = new StatsRow("Stun Duration:", stunDuration, false);
		
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedTier1 == 0, selectedTier1 == 0);
		
		toReturn[10] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), selectedTier5 == 1, selectedTier5 == 1);
		
		toReturn[11] = new StatsRow("Spread Recovery:", convertDoubleToPercentage(getSpreadRecoverySpeed()), selectedOverclock == 5, selectedOverclock == 5);
		
		boolean recoilModified = selectedTier3 == 0 || selectedOverclock == 0 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[12] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
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
		
		double weakpointAccuracy;
		if (weakpoint) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(getDirectDamage(), getWeakpointBonus());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = getDirectDamage();
		}
		
		double electroDPS = 0;
		if (selectedOverclock == 6) {
			double electroDoTUptimeCoefficient = Math.min(DoTInformation.Electro_SecsDuration / duration, 1);
			electroDPS += electroDoTUptimeCoefficient * DoTInformation.Electro_DPS;
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * getDirectDamage()) / duration + electroDPS;
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
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		/*
			Scout's Assault Rifle seems to use a different model of accuracy than the other guns do. Speficially, it does the following things differently:
			1. The Spread Recovery Speed seems to be non-linear; it seems to be more powerful at the start of the magazine and get weaker near the end
			2. The Spread Recovery starts getting applied on the first shot, whereas all the other guns have it applied on every shot after the first.
			3. When its Base Spread is reduced to 0, the Max Spread doesn't decrease as well (every other gun has Max Spread = Base Spread + Spread Variance)
			
			With those things in mind, I am choosing to model this slightly incorrectly with the current AccuracyEstimator because I want to get things finished up.
			If I keep developing this app, I'd like to come back and make a method specifically for this weapon.
		*/
		
		double unchangingBaseSpread = 19;
		double changingBaseSpread = 21 * getBaseSpread();
		double spreadVariance = 84;
		double spreadPerShot = 30 * getSpreadPerShot();
		double spreadRecoverySpeed = 170.6869145;
		double recoilPerShot = 41 * getRecoil();
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 6.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 2.0 / 3.0;
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, false, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval);
	}

	@Override
	public double utilityScore() {
		// Mod Tier 5 "Battle Frenzy" grants a 50% movespeed increase on kill for 3 seconds
		if (selectedTier5 == 0) {
			double uptimeCoefficient = Math.min(3.0 / averageTimeToKill(), 1);
			utilityScores[0] = uptimeCoefficient * MathUtils.round(0.5 * DwarfInformation.walkSpeed, 2) * UtilityInformation.Movespeed_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Armor Breaking
		// Like Burst Pistol, this armor break bonus only applies to the bullets so it's not multiplied by max num targets
		utilityScores[2] = (getArmorBreaking() - 1) * UtilityInformation.ArmorBreak_Utility;
		
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
}
