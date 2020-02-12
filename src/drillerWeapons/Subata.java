package drillerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.UtilityInformation;
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
	
	private int directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointBonus;
	private double baseSpread;
	private double spreadPerShot;
	private double recoil;
	private double stunChance;
	private int stunDuration;
	
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
		baseSpread = 1.0;
		spreadPerShot = 1.0;
		recoil = 1.0;
		stunChance = 0.0;
		stunDuration = 0;
		
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
		tier1[0] = new Mod("Improved Alignment", "Pin-point accuracy on first shot.", 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "The good thing about clips, magazines, ammo drums, fuel tanks... you can always get bigger variants.", 1, 1);
		tier1[2] = new Mod("Quickfire Ejector", "Experience, training, and a couple of under-the-table design \"adjustments\" means your gun can be reloaded significantly faster.", 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 2, 0);
		tier2[1] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Propellant", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 3, 0);
		tier3[1] = new Mod("Recoil Compensator", "This little tweak reduces weapon recoil and spread per shot helping you hit consecutive shots.", 3, 1);
		tier3[2] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hollow-Point Bullets", "Hit 'em where it hurts! Literally! We've upped the damage you'll be able to do to any creature's fleshy bits. You're welcome.", 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "The Good folk in R&D have been busy. The overall damage of your weapon is increased.", 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Volatile Bullets", "Bonus fire damage to burning targets.", 5, 0, false);
		tier5[1] = new Mod("Mactera Neurotoxin Coating", "Bonus damage against Mactera", 5, 1, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a chance to ricochet into a nearby enemy.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "More damage on average but it's a bit inconsistent.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Oversized Magazine", "Custom magazine that can fit a lot more ammo but it's a bit unwieldy and takes longer to reload.", 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Automatic Fire", "Fully automatic action, watch out for the recoil.", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Explosive Reload", "Micro-explosives that explode inside hit targets when you reload. However these fancy bullets come at the cost of raw damage, total ammo, and magazine capacity.", 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Tranquilizer Rounds", "Part bullet, part syringe these rounds are very effective at stunning most enemies.", 5);
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
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		
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
			toReturn = (int) Math.round(toReturn * 1.1);
		}
		else if (selectedOverclock == 4) {
			toReturn -= 3;
		}
		
		return toReturn;
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
	private double getBaseSpread() {
		double toReturn = baseSpread;
		
		if (selectedTier1 == 0) {
			toReturn -= 1.0;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = spreadPerShot;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = recoil;
		
		if (selectedOverclock == 3) {
			toReturn += 0.5;
		}
		
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getStunChance() {
		double toReturn = stunChance;
		
		if (selectedOverclock == 5) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		
		if (selectedOverclock == 5) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[11];
		
		boolean directDamageModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[3] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedOverclock == 3 || selectedOverclock == 5);
		
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), selectedTier1 == 2 || selectedOverclock == 2);
		
		toReturn[5] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 0);
		
		toReturn[6] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedTier1 == 0 || selectedOverclock == 3);
		
		toReturn[7] = new StatsRow("Spread Per Shot:", convertDoubleToPercentage(getSpreadPerShot()), selectedTier3 == 1);
		
		toReturn[8] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), selectedOverclock == 3 || selectedTier3 == 1);
		
		toReturn[9] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), selectedOverclock == 5);
		
		toReturn[10] = new StatsRow("Stun Duration:", getStunDuration(), selectedOverclock == 5);
		
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
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		// Somehow "Explosive Reload" will have to be modeled in here.
		if (weakpointBonus) {
			return (double) increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) * getMagazineSize();
		}
		else {
			return (double) getDirectDamage() * getMagazineSize();
		}
	}

	@Override
	public double calculateIdealBurstDPS() {
		double timeToFireMagazine = ((double) getMagazineSize()) / getRateOfFire();
		return calculateDamagePerMagazine(false) / timeToFireMagazine;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		return calculateDamagePerMagazine(false) / timeToFireMagazineAndReload;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		return calculateDamagePerMagazine(true) / timeToFireMagazineAndReload;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		// If "Chain Hit" is equipped, 50% of bullets that hit a weakpoint will ricochet to nearby enemies.
		// Effectively 25% of ideal sustained DPS?
		if (selectedOverclock == 0) {
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.5 * EnemyInformation.probabilityBulletWillHitWeakpoint();
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
			return (getMagazineSize() + getCarriedAmmo()) * getDirectDamage();
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
		double magSize = (double) getMagazineSize();
		// Don't forget to add the magazine that you start out with, in addition to the carried ammo
		double numberOfMagazines = (((double) getCarriedAmmo()) / magSize) + 1.0;
		double timeToFireMagazine = magSize / getRateOfFire();
		// There are one fewer reloads than there are magazines to fire
		// TODO: floor(numMagazines) - 1, make the change in all weapons
		return numberOfMagazines * timeToFireMagazine + (numberOfMagazines - 1.0) * getReloadTime();
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
	public double estimatedAccuracy() {
		// TODO Auto-generated method stub
		// convertDoubleToPercentage(new AccuracyEstimator(getRateOfFire(), getMagazineSize(), getBaseSpread(), getSpreadPerShot(), 1.0, 1.0, getRecoil(), 1.0, getRecoil()).calculateAccuracy())
		return 0;
	}

	@Override
	public double utilityScore() {
		double totalUtility = 0;
		
		// Tranq rounds = 50% chance to stun, 5 second stun
		if (selectedOverclock == 5) {
			totalUtility += getStunChance() * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		
		return totalUtility;
	}
}
