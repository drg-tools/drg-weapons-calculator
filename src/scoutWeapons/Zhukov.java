package scoutWeapons;

import java.util.Arrays;
import java.util.List;

import drillerWeapons.Subata;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

// Embedded Detonators does 5 damage per ammo (10/bullet) on reload

public class Zhukov extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double baseSpread;
	private int maxPenetrations;
	private double weakpointBonus;
	private double movespeedWhileFiring;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Zhukov() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Zhukov(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Zhukov(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Zhukov Nuk17";
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 11;
		carriedAmmo = 600;
		magazineSize = 50;  // Really 25
		rateOfFire = 30.0;  // Really 15
		reloadTime = 1.8;
		baseSpread = 1.0;
		maxPenetrations = 0;
		weakpointBonus = 0.0;
		movespeedWhileFiring = 1.0;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased", 1, 0);
		tier1[1] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "The good thing about clips, magazines, ammo drums, fuel tanks...you can always get bigger variants.", 2, 0);
		tier2[1] = new Mod("Supercharged Feed Mechanism", "We overclocked your gun. It fires faster. Don't ask. Just enjoy. Also probably don't tell Management, please.", 2, 1);
		tier2[2] = new Mod("Quickfire Ejector", "Experience, training, and a couple of under-the-table design \"adjustments\" means your gun can be reloaded significantly faster.", 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 3, 0);
		tier3[1] = new Mod("Better Weight Balance", "Base accuracy increase", 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Blowthrough Rounds", "Shaped projectiles designed to over-penetrate targets with a minimal loss of energy. In other words: Fire straight through an enemy!", 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "Hit em' where it hurts! Literally! We've upped the damage you'll be able to do to any creature's fleshy bits. You're welcome.", 4, 1);
		tier4[2] = new Mod("Expanded Ammo Bags", "You had to give up on some sandwich-storage, but your total ammo capacity is increased!", 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Conductive Bullets", "More damage to targets that are in an electric field", 5, 0, false);
		tier5[1] = new Mod("Get In, Get Out", "Temporary movement speed bonus after emptying clip", 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Minimal Magazines", "By filling away unnecessary material from the magazines you've made them lighter, and that means they pop out faster when reloading. Also the rounds can move more freely increasing the max rate of fire slightly.", 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Custom Casings", "Fit more of these custom rounds in each magazine but at small loss in raw damage.", 1);
		overclocks[2] = new Overclock(Overclock.classification.unstable, "Cryo Minelets", "After impacting terrain, these high-tech bullets convert into cryo-minelets that will super-cool anything that comes close. However they don't last forever and the rounds themselves take more space in the clip and deal less direct damage.", 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Embedded Detonators", "Special bullets contain micro-explosives that detonate when you reload the weapon at the cost of total ammo and direct damage.", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Gas Recycling", "Special hardened bullets combined with rerouting escaping gasses back into the chamber greatly increases the raw damage of the weapon but makes it more difficult to control and removes any bonus to weakpoint hits.", 4);
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
				System.out.println("Zhukov's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Zhukov's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Zhukov's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-5 or a hyphen");
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
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Zhukov clone() {
		return new Zhukov(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 1) {
			toReturn += 1;
		}
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 1 || selectedOverclock == 2) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 4) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private int getAreaDamage() {
		// Equipping the Overclock "Embedded Detonators" leaves a detonator inside enemies that does 5 Area Damage per Ammo (10/Bullet) that deals damage to an enemy upon reloading the Zhukovs
		if (selectedOverclock == 3) {
			return 10;
		}
		else { 
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 75;
		}
		if (selectedTier4 == 2) {
			toReturn += 150;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 75;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier2 == 0) {
			toReturn += 10;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 30;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 10;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 1) {
			toReturn += 8.0;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 2) {
			toReturn -= 0.6;
		}
		
		if (selectedOverclock == 0) {
			toReturn -= 0.4;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = baseSpread;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		int toReturn = maxPenetrations;
		
		if (selectedTier4 == 0) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 1) {
			toReturn += 0.3;
		}
		
		if (selectedOverclock == 4) {
			// Since this removes the Zhukov's ability to get weakpoint bonus damage, return a -100% to symbolize it.
			return -1.0;
		}
		
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = movespeedWhileFiring;
		
		if (selectedOverclock == 4) {
			modifier -= 0.5;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[10];
		
		boolean directDamageModified = selectedTier1 == 1 || selectedTier3 == 0 || (selectedOverclock > 0 && selectedOverclock < 5);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		// This stat only applies to OC "Embedded Detonators"
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), selectedOverclock == 3, selectedOverclock == 3);
		
		boolean magSizeModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedTier4 == 2 || selectedOverclock == 3;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedTier2 == 1 || selectedOverclock == 0);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), selectedTier2 == 2 || selectedOverclock == 0);
		
		String sign = "";
		if (selectedOverclock != 4) {
			sign = "+";
		}
		
		boolean weakpointModified = selectedTier4 == 1 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Weakpoint Bonus:", sign + convertDoubleToPercentage(getWeakpointBonus()), weakpointModified, weakpointModified);
		
		toReturn[7] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedTier3 == 1 || selectedOverclock == 4);
		
		toReturn[8] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[9] = new StatsRow("Movespeed While Firing: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// This weapon can never deal splash damage
		return false;
	}
	
	private double calculateAvgNumBulletsNeededToFreeze() {
		// Minelets do 8 Cold Damage upon detonation, but they have to take 1 second to arm first.
		// While Frozen, bullets do x3 Direct Damage.
		double effectiveRoF = getRateOfFire() / 2.0;
		double timeToFreeze = EnemyInformation.averageTimeToFreeze(-8, effectiveRoF);
		return Math.ceil(timeToFreeze * effectiveRoF);
	}
	
	// Single-target calculations
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		double effectiveMagazineSize = getMagazineSize() / 2;
		
		if (selectedOverclock == 2) {
			// First, you have to intentionally miss bullets in order to convert them to Cryo Minelets, then wait 1 second, and unload the rest of the clip into
			// the now-frozen enemy for x3 damage. Damage vs frozen enemies does NOT benefit from weakpoint damage on top of the frozen multiplier.
			double numBulletsMissedToBecomeCryoMinelets = calculateAvgNumBulletsNeededToFreeze();
			return (getDirectDamage() * UtilityInformation.Frozen_Damage_Multiplier) * (effectiveMagazineSize - numBulletsMissedToBecomeCryoMinelets);
		}
		else {
			if (weakpointBonus) {
				return (increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getAreaDamage()) * effectiveMagazineSize;
			}
			else {
				return (getDirectDamage() + getAreaDamage()) * effectiveMagazineSize;
			}
		}
	}

	@Override
	public double calculateIdealBurstDPS() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		double effectiveRoF = getRateOfFire() / 2.0;
		
		double delayBeforeCroyMineletsArm = 0;
		if (selectedOverclock == 2) {
			delayBeforeCroyMineletsArm = 1;
		}
		
		double timeToFireMagazine = effectiveMagazineSize / effectiveRoF + delayBeforeCroyMineletsArm;
		return calculateDamagePerMagazine(false) / timeToFireMagazine;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		double effectiveRoF = getRateOfFire() / 2.0;
		
		double delayBeforeCroyMineletsArm = 0;
		if (selectedOverclock == 2) {
			delayBeforeCroyMineletsArm = 1;
		}
		
		double timeToFireMagazineAndReload = (effectiveMagazineSize / effectiveRoF + delayBeforeCroyMineletsArm) + getReloadTime();
		return calculateDamagePerMagazine(false) / timeToFireMagazineAndReload;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		double effectiveRoF = getRateOfFire() / 2.0;
		
		double delayBeforeCroyMineletsArm = 0;
		if (selectedOverclock == 2) {
			delayBeforeCroyMineletsArm = 1;
		}
		
		double timeToFireMagazineAndReload = (effectiveMagazineSize / effectiveRoF + delayBeforeCroyMineletsArm) + getReloadTime();
		
		// Because the Overclock "Gas Recycling" removes the ability to get any weakpoint bonus damage, that has to be modeled here.
		boolean canGetWeakpointBonus = selectedOverclock != 4;
		
		return calculateDamagePerMagazine(canGetWeakpointBonus) / timeToFireMagazineAndReload;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateIdealSustainedDPS();
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		// If there's an odd number carried ammo, round up since you can fire the last "odd" ammo as a full-damage shot
		double effectiveCarriedAmmo = Math.ceil(getCarriedAmmo() / 2.0);
		
		if (selectedOverclock == 2) {
			double bulletsIntentionallyMissedPerMag = calculateAvgNumBulletsNeededToFreeze();
			double numMags = numMagazines((int) effectiveCarriedAmmo, (int) effectiveMagazineSize);
			double totalBulletsIntentionallyWasted = Math.round(bulletsIntentionallyMissedPerMag * numMags);
			
			return (effectiveMagazineSize + effectiveCarriedAmmo - totalBulletsIntentionallyWasted) * (getDirectDamage() * UtilityInformation.Frozen_Damage_Multiplier) * calculateMaxNumTargets();
		}
		else {
			// Area Damage only applies when using OC "Embedded Detonators", so it doesn't need to be modeled for the Cryo Minelets' max damage.
			return (effectiveMagazineSize + effectiveCarriedAmmo) * (getDirectDamage() + getAreaDamage()) * calculateMaxNumTargets();
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		// Because of how this weapon works, all these numbers need to be halved to be accurate.
		int effectiveMagazineSize = getMagazineSize() / 2;
		// If there's an odd number carried ammo, round up since you can fire the last "odd" ammo as a full-damage shot
		int effectiveCarriedAmmo = (int) Math.ceil(((double) getCarriedAmmo()) / 2.0);
		double effectiveRoF = getRateOfFire() / 2.0;
		
		double timeToFireMagazine = ((double) effectiveMagazineSize) / effectiveRoF;
		return numMagazines(effectiveCarriedAmmo, effectiveMagazineSize) * timeToFireMagazine + numReloads(effectiveCarriedAmmo, effectiveMagazineSize) * getReloadTime();
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		// Because the Overclock "Gas Recycling" removes the ability to get any weakpoint bonus damage, that has to be modeled here.
		double dmgPerShot;
		if (selectedOverclock == 4) {
			dmgPerShot = getDirectDamage();
		}
		else {
			dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		}
		
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double utilityScore() {
		// OC "Gas Recycling" reduces Scout's movement speed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Mod Tier 5 "Get In, Get Out" gives 100% movement speed increase for 2 sec after reloading empty clips
		if (selectedTier5 == 1) {
			// Because this buff lasts 2 seconds, but I don't think it's possible to have 100% uptime. Use the uptime as a coefficient to reduce the value of the movespeed buff.
			double effectiveMagazineSize = getMagazineSize() / 2.0;
			double effectiveRoF = getRateOfFire() / 2.0;
			double timeToFireMagazineAndReload = (effectiveMagazineSize / effectiveRoF) + getReloadTime();
			
			// Just because I don't think it's possible doesn't mean I'm not safeguarding against it.
			double uptimeCoefficient = Math.min(2.0 / timeToFireMagazineAndReload, 1);
			
			utilityScores[0] += uptimeCoefficient * DwarfInformation.walkSpeed * UtilityInformation.Movespeed_Utility;
		}
		
		// OC "Cryo Minelets" applies Cryo damage to missed bullets
		if (selectedOverclock == 2) {
			// Cryo minelets: 1 placed per 2 ammo, minelets arm in 1 second, and detonate in 3 seconds if no enemy is around.
			// Minelets seem to do 8 Cold Damage each, and they don't explode in a radius -- instead it seems that they spurt off in a random direction for 2.5m.
			int estimatedNumTargetsSlowedOrFrozen = 3;  // This is a pure, unadulterated guess.
			
			utilityScores[3] = estimatedNumTargetsSlowedOrFrozen * UtilityInformation.Cold_Utility;
			utilityScores[6] = estimatedNumTargetsSlowedOrFrozen * UtilityInformation.Frozen_Utility;
		}
		else {
			utilityScores[3] = 0;
			utilityScores[6] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
}
