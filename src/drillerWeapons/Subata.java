package drillerWeapons;

import java.util.Arrays;
import java.util.List;

import guiPieces.ButtonIcons.drgIcons;
import modelPieces.UtilityInformation;
import modelPieces.AccuracyEstimator;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Subata extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointBonus;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Subata() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Subata(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Subata(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Subata 120";
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 12;
		carriedAmmo = 160;
		magazineSize = 12;
		rateOfFire = 8.0;
		reloadTime = 1.9;
		weakpointBonus = 0.2;
		// Subata has a hidden 50% Armor Breaking penalty (credit to Elythnwaen for pointing this out to me)
		armorBreaking = 0.5;
		
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
		tier1[0] = new Mod("Improved Alignment", "x0 Base Spread", drgIcons.baseSpread, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+5 Magazine Size", drgIcons.magSize, 1, 1);
		tier1[2] = new Mod("Quickfire Ejector", "-0.6 Reload Time", drgIcons.reloadSpeed, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", drgIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Increased Caliber Rounds", "+1 Direct Damage", drgIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Propellant", "+1 Direct Damage", drgIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Recoil Compensator", "-20% Spread per Shot, x0.5 Recoil", drgIcons.recoil, 3, 1);
		tier3[2] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", drgIcons.carriedAmmo, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hollow-Point Bullets", "+60% Weakpoint Bonus", drgIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+3 Direct Damage", drgIcons.directDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Volatile Bullets", "+50% Damage dealt to Burning enemies", drgIcons.heatDamage, 5, 0, false);
		tier5[1] = new Mod("Mactera Neurotoxin Coating", "+20% Damage dealt to Mactera-type enemies", drgIcons.special, 5, 1, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a 50% chance to ricochet into a nearby enemy.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Oversized Magazine", "+10 Magazine Size, +0.5 Reload Time", 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Automatic Fire", "Changes the Subata from semi-automatic to fully automatic, +2 Rate of Fire, +100% Base Spread, x2.5 Recoil", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Explosive Reload", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 15 Area Damage to the enemy upon reloading. -3 Direct Damage, -3 Magazine Size, -40 Max Ammo.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Tranquilizer Rounds", "Every bullet has a 50% chance to stun an enemy for 6 seconds. -4 Magazine Size, -4 Rate of Fire.", 5);
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
			if (symbols[1] == 'C') {
				System.out.println("Subata's second tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Subata's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Subata's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
	public Subata clone() {
		return new Subata(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "Subata";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier2 == 1) {
			toReturn += 1;
		}
		if (selectedTier3 == 0) {
			toReturn += 1;
		}
		if (selectedTier4 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= homebrewPowderCoefficient;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 3;
		}
		
		return toReturn;
	}
	private int getAreaDamage() {
		// Equipping the Overclock "Explosive Reload" leaves a detonator inside enemies that does 15 Area Damage per Bullet that deals damage to an enemy upon reloading the Subata
		if (selectedOverclock == 4) {
			return 15;
		}
		else { 
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 40;
		}
		if (selectedTier3 == 2) {
			toReturn += 40;
		}
		
		if (selectedOverclock == 4) {
			toReturn -= 40;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 10;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 4;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 4.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier1 == 2) {
			toReturn -= 0.6;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 0) {
			toReturn += 0.6;
		}
		
		return toReturn;
	}
	private int getMaxRicochets() {
		if (selectedOverclock == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		// Additive bonuses first
		if (selectedOverclock == 3) {
			toReturn += 1.0;
		}
		
		// Multiplicative bonuses last
		if (selectedTier1 == 0) {
			toReturn *= 0.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 2.5;
		}
		
		return toReturn;
	}
	private double getStunChance() {
		if (selectedOverclock == 5) {
			return 0.5;
		}
		else {
			return 0;
		}
	}
	private int getStunDuration() {
		if (selectedOverclock == 5) {
			return 6;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		// This stat only applies to OC "Explosive Reload"
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), selectedOverclock == 4, selectedOverclock == 4);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedOverclock == 3 || selectedOverclock == 5);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), selectedTier1 == 2 || selectedOverclock == 2);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 0);
		
		// Display Subata's hidden 50% armor break penalty
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(armorBreaking), false);
		
		// These two stats only apply to OC "Tranquilizer Rounds"
		boolean tranqRoundsEquipped = selectedOverclock == 5;
		toReturn[8] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), tranqRoundsEquipped, tranqRoundsEquipped);
		
		toReturn[9] = new StatsRow("Stun Duration:", getStunDuration(), tranqRoundsEquipped, tranqRoundsEquipped);
		
		boolean chainHitEquipped = selectedOverclock == 0;
		toReturn[10] = new StatsRow("Weakpoint Chain Hit Chance:", "50%", chainHitEquipped, chainHitEquipped);
		toReturn[11] = new StatsRow("Max Ricochets:", getMaxRicochets(), chainHitEquipped, chainHitEquipped);
		
		boolean baseSpreadModified = selectedTier1 == 0 || selectedOverclock == 3;
		toReturn[12] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		toReturn[13] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), selectedTier3 == 1, selectedTier3 == 1);
		
		boolean recoilModified = selectedOverclock == 3 || selectedTier3 == 1;
		toReturn[14] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
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
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * getDirectDamage() + (bulletsThatHitWeakpoint + bulletsThatHitTarget) * getAreaDamage()) / duration;
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

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		// If "Chain Hit" is equipped, 50% of bullets that hit a weakpoint will ricochet to nearby enemies.
		if (selectedOverclock == 0) {
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.5 * estimatedAccuracy(true) / 100.0;
			double numBulletsRicochetPerMagazine = Math.round(ricochetProbability * getMagazineSize());
			
			double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			return numBulletsRicochetPerMagazine * getDirectDamage() / timeToFireMagazineAndReload;
		}
		else {
			return 0.0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		if (selectedOverclock == 0) {
			// Chain Hit
			double ricochetProbability = 0.5 * EnemyInformation.probabilityBulletWillHitWeakpoint();
			double totalNumRicochets = Math.round(ricochetProbability * (getMagazineSize() + getCarriedAmmo()));
			
			return (getMagazineSize() + getCarriedAmmo() + totalNumRicochets) * getDirectDamage();
		}
		else {
			// Because the OCs Chain Hit and Explosive Reload are mutually exclusive, the Area Damage only needs to be called here.
			return (getMagazineSize() + getCarriedAmmo()) * (getDirectDamage() + getAreaDamage());
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedOverclock == 0) {
			// OC "Chain Hit"
			return 2;
		}
		else {
			return 1;
		}
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
		double unchangingBaseSpread = 20;
		double changingBaseSpread = 28;
		double spreadVariance = 53;
		double spreadPerShot = 24;
		double spreadRecoverySpeed = 127.2762815;
		double recoilPerShot = 28.23118843;
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 8.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 1.0 / 2.0;
		
		double[] modifiers = {getBaseSpread(), getSpreadPerShot(), 1.0, 1.0, getRecoil()};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, false, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval, modifiers);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		// The Area damage from Explosive Reload doesn't affect the chance to break the Light Armor plates since it's not part of the initial projectile
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), armorBreaking) * UtilityInformation.ArmorBreak_Utility;
		
		// Tranq rounds = 50% chance to stun, 5 second stun
		if (selectedOverclock == 5) {
			utilityScores[5] = getStunChance() * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
}
