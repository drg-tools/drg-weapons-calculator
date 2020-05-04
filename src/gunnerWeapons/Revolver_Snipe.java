package gunnerWeapons;

import java.util.Arrays;
import java.util.List;

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

public class Revolver_Snipe extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double stunChance;
	private double stunDuration;
	private double weakpointBonus;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/

	// Shortcut constructor to get baseline data
	public Revolver_Snipe() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Revolver_Snipe(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Revolver_Snipe(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Bulldog\" Heavy Revolver (Slow RoF)";
		weaponPic = WeaponPictures.revolver;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 50.0;
		carriedAmmo = 28;
		magazineSize = 4;
		rateOfFire = 2.0;  // bullets per second
		reloadTime = 2.0;  // seconds
		stunChance = 0.5;
		stunDuration = 1.5;  // seconds
		weakpointBonus = 0.15;
		
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
		tier1[0] = new Mod("Quickfire Ejector", "-0.7 Reload Time", modIcons.reloadSpeed, 1, 0);
		tier1[1] = new Mod("Perfect Weight Balance", "-70% Base Spread", modIcons.baseSpread, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Increased Caliber Rounds", "+15 Direct Damage", modIcons.directDamage, 2, 0);
		tier2[1] = new Mod("Floating Barrel", "-80% Spread per Shot, x0.75 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Expanded Ammo Bags", "+12 Max Ammo", modIcons.carriedAmmo, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 3, 0);
		tier3[1] = new Mod("Explosive Rounds", "+30 Area Damage in a 1.5m radius, x0.5 Direct Damage", modIcons.addedExplosion, 3, 1);
		tier3[2] = new Mod("Hollow-Point Bullets", "+50% Weakpoint Bonus", modIcons.weakpointBonus, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Expanded Ammo Bags", "+12 Max Ammo", modIcons.carriedAmmo, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+15 Direct Damage", modIcons.directDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Dead-Eye", "No aim penalty while moving", modIcons.baseSpread, 5, 0, false);
		tier5[1] = new Mod("Glyphid Neurotoxin Coating", "50% chance to inflict Neurotoxin DoT on all enemies hit by the Revolver"
				+ "Neurotoxin does an average of " + DoTInformation.Neuro_DPS + " DPS", modIcons.neurotoxin, 5, 1);  // It looks like whenever this procs for the main target, all splash targets get it too, instead of RNG/enemy.
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, overclockIcons.homebrewPowder, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a 33% chance to ricochet into a nearby enemy.", overclockIcons.ricochet, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Volatile Bullets", "x4 Damage to Burning targets, -25 Direct Damage", overclockIcons.heatDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Six Shooter", "+2 Magazine Size, +8 Max Ammo, +4 Rate of Fire, x1.5 Base Spread, +0.5 Reload Time", overclockIcons.magSize, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Elephant Rounds", "x2 Direct Damage, -1 Mag Size, -13 Max Ammo, +71% Spread per Shot, +150% Recoil, +0.5 Reload Time", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Magic Bullets", "All bullets that impact terrain automatically ricochet to nearby enemies (effectively raising accuracy to 100%). +8 Max Ammo, -20 Direct Damage", overclockIcons.ricochet, 5);
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
				System.out.println("Revolver's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Revolver's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Revolver's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
	public Revolver_Snipe clone() {
		return new Revolver_Snipe(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "Revolver_Snipe";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	private double getDirectDamage() {
		double toReturn = directDamage;
		// Start by adding flat damage bonuses
		if (selectedTier2 == 0) {
			toReturn += 15.0;
		}
		if (selectedTier4 == 1) {
			toReturn += 15.0;
		}
		
		if (selectedOverclock == 2) {
			toReturn -= 25.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 20.0;
		}
			
		// Then do multiplicative bonuses
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		if (selectedOverclock == 0) {
			toReturn *= homebrewPowderCoefficient;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 2.0;
		}
		return toReturn;
	}
	private int getAreaDamage() {
		if (selectedTier3 == 1) {
			return 30;
		}
		else {
			return 0;
		}
	}
	private double getAoERadius() {
		if (selectedTier3 == 1) {
			return 1.5;
		}
		else {
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		if (selectedTier2 == 2) {
			toReturn += 12;
		}
		if (selectedTier4 == 0) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 5) {
			toReturn += 8;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 13;
		}
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedOverclock == 3) {
			toReturn += 2;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 1;
		}
		return toReturn;
	}
	private double getRateOfFire() {
		double maxRoF = rateOfFire;
		if (selectedOverclock == 3) {
			maxRoF += 4.0;
		}
		return calculateAccurateRoF(maxRoF);
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		if (selectedTier1 == 0) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier3 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private int getMaxRicochets() {
		if (selectedOverclock == 1 || selectedOverclock == 5) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		if (selectedTier3 == 2) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		if (selectedTier1 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.8;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 0.71;
		}
		
		return toReturn;
	}
	private double getSpreadRecoverySpeed() {
		if (selectedOverclock == 4) {
			return 1.5;
		}
		else {
			return 1.0;
		}
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		// Additive first
		if (selectedOverclock == 4) {
			toReturn += 1.5;
		}
		
		// Multiplicative last
		if (selectedTier2 == 1) {
			toReturn *= 0.75;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[16];
		
		boolean directDamageModified = selectedTier2 == 0 || selectedTier3 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		boolean explosiveEquipped = selectedTier3 == 1;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), explosiveEquipped, explosiveEquipped);
		
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), explosiveEquipped, explosiveEquipped);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), selectedOverclock == 3 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier2 == 2 || selectedTier4 == 0 || (selectedOverclock > 2 && selectedOverclock < 6);
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		// For right now, the RoF is artificially slowed by trying to be accurate. I'm planning on re-modeling this weapon with full RoF to see what builds would be good for "fanning the hammer"
		toReturn[5] = new StatsRow("Rate of Fire:", getRateOfFire(), false);
		
		toReturn[6] = new StatsRow("Reload Time:", getReloadTime(), selectedTier1 == 0 || selectedOverclock == 3 || selectedOverclock == 4);
		
		toReturn[7] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier3 == 2);
		
		toReturn[8] = new StatsRow("Stun Chance:", convertDoubleToPercentage(stunChance), false);
		
		toReturn[9] = new StatsRow("Stun Duration:", stunDuration, false);
		
		toReturn[10] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier3 == 0, selectedTier3 == 0);
		
		toReturn[11] = new StatsRow("Weakpoint Chain Hit Chance:", "33%", selectedOverclock == 1, selectedOverclock == 1);
		
		boolean canRicochet = selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[12] = new StatsRow("Max Ricochets:", getMaxRicochets(), canRicochet, canRicochet);
		
		boolean baseSpreadModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[13] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		boolean spreadPerShotModified = selectedTier2 == 1 || selectedOverclock == 4;
		toReturn[14] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), spreadPerShotModified, spreadPerShotModified);
		
		toReturn[15] = new StatsRow("Spread Recovery:", convertDoubleToPercentage(getSpreadRecoverySpeed()), selectedOverclock == 4, selectedOverclock == 4);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 4;
		toReturn[15] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		// It appears that Revolver doesn't have any damage falloff within its 1.5m radius, so its AoE efficiency would be [1.5, 1.0, 5].
		// However, in order to save a few cycles every auto-calculate, I'm choosing not to implement that as it has no mathematical effect on the outputs.
		return selectedTier3 == 1;
	}
	
	/*
		I'm writing this method specifically because I know that the Revolver is never fired at max RoF -- it's used by the community as a sniper side-arm.
		
		I'm a bit worried that this is counter-intuitive in comparison to how the rest of the weapons are modeled, but I think this is a better approximation for how this weapon gets used in-game.
	*/
	private double calculateAccurateRoF(double maxRoF) {
		// Variables copied from estimatedAccuracy() to reverse-calculate the slow RoF needed for high accuracy
		double spreadPerShot = 129;
		double spreadRecoverySpeed = 109.1390954;
		double recoilPerShot = 155;
		
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 6.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 1.0;
		
		// Elephant Rounds significantly reduces the recoil speeds in addition to increasing recoil per shot
		double SpSModifier = getSpreadPerShot();
		if (selectedOverclock == 4) {
			
			if (selectedTier2 != 1) {
				// And if Floating Barrel isn't equipped, then the Spread per Shot takes it to Max Spread on first shot for some reason?
				spreadPerShot = 389;
				SpSModifier = 1.0;
				
			}
			
			recoilUpInterval = 16.0 / 60.0;
			recoilDownInterval = 140.0 / 60.0;
		}
		
		// These numbers are chosen arbitrarily.
		double desiredIncreaseInSpread = 52;
		double desiredIncreaseInRecoil = 62;
		
		double timeToRecoverSpread = (spreadPerShot * SpSModifier - desiredIncreaseInSpread) / (spreadRecoverySpeed * getSpreadRecoverySpeed());
		double timeToRecoverRecoil = recoilUpInterval + (recoilPerShot * getRecoil() - desiredIncreaseInRecoil) * recoilDownInterval / (recoilPerShot * getRecoil());
		
		double longerTime = Math.max(timeToRecoverSpread, timeToRecoverRecoil);
		
		return Math.min(1.0 / longerTime, maxRoF);
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
		double areaDamage = getAreaDamage();
		// OC Volatile Bullets deals x4 Direct Damage to Burning targets
		if (selectedOverclock == 2 && statusEffects[0]) {
			directDamage *= 4.0;
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
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage, getWeakpointBonus());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		double neuroDPS = 0;
		if (selectedTier5 == 1) {
			// Neurotoxin Coating has a 50% chance to inflict the DoT
			if (burst) {
				neuroDPS = calculateRNGDoTDPSPerMagazine(0.5, DoTInformation.Neuro_DPS, getMagazineSize());
			}
			else {
				neuroDPS = DoTInformation.Neuro_DPS;
			}
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * (directDamage + areaDamage)) / duration + neuroDPS;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus, int numTargets) {
		// TODO: I'd like to refactor this method out
		if (weakpointBonus) {
			return (increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + numTargets * getAreaDamage()) * getMagazineSize();
		}
		else {
			return (getDirectDamage() + numTargets * getAreaDamage()) * getMagazineSize();
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
		// TODO: I'd like to refactor this method a little.
		/*
			There are 8 combinations of ways for the Revolver to hit an additional target, based on various combinations of
			the Overclocks "Chain Hit" and "Magic Bullets", and the Tier 3 Mods "Super Blowthrough Rounds" and "Explosive Rounds"
		*/
		double sustainedAdditionalDPS;
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		
		// If Super Blowthrough Rounds is equipped, then the ricochets from either "Chain Hit" or "Magic Bullets" won't affect the additional DPS
		if (selectedTier3 == 0) {
			// Because Super Blowthrough Rounds are just the same damage to another enemy behind the primary target (or from a ricochet), return Ideal Sustained DPS
			return calculateIdealSustainedDPS();
		}
		
		// Only Explosive
		else if (selectedTier3 == 1 && selectedOverclock != 1 && selectedOverclock != 5) {
			// Explosive Rounds are just the Area Damage, so I have to re-model the sustained DPS formula here
			sustainedAdditionalDPS = getMagazineSize() * areaDamage / timeToFireMagazineAndReload;
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		
		// Only "Chain Hit" OR "Chain Hit" + Explosive Rounds
		else if (selectedOverclock == 1 && selectedTier3 != 0) {
			// If "Chain Hit" is equipped, 33% of bullets that hit a weakpoint will ricochet to nearby enemies.
			// Effectively 25% of ideal sustained DPS?
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.33 * estimatedAccuracy(true) / 100.0;
			double numBulletsRicochetPerMagazine = Math.round(ricochetProbability * getMagazineSize());
			
			sustainedAdditionalDPS = numBulletsRicochetPerMagazine * (directDamage + areaDamage) / timeToFireMagazineAndReload;
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		
		// Only "Magic Bullets"
		else if (selectedOverclock == 5 && selectedTier3 != 0 && selectedTier3 != 1) {
			// "Magic Bullets" mean that any bullet that MISSES the primary target will try to automatically ricochet to a nearby enemy.
			// This can be modeled by returning (1 - Accuracy) * Ideal Sustained DPS
			sustainedAdditionalDPS = (1.0 - estimatedAccuracy(false)/100.0) * calculateIdealSustainedDPS();
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		
		// "Magic Bullets" + Explosive
		else if (selectedOverclock == 5 && selectedTier3 == 1) {
			// This combination is the hardest to model: when a missed bullet ricochets, it still deals an explosion of damage on the ground before redirecting to the new target. This means that if you shoot the ground next to an
			// enemy with this combination, they'll take the Area Damage, followed by the Direct + Area Damage of the bullet after it redirects.
			sustainedAdditionalDPS = getMagazineSize() * (directDamage + 2 * areaDamage) / timeToFireMagazineAndReload;
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numberOfTargets = calculateMaxNumTargets();
		double damagePerMagazine = calculateDamagePerMagazine(false, numberOfTargets);
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		
		double ricochetTotalDamage = 0;
		// If Blowthrough Rounds is selected, multiply the dmg/mag times the total num targets hit
		if (selectedTier3 == 0) {
			damagePerMagazine *= numberOfTargets;
		}
		else if (selectedOverclock == 1 && selectedTier3 != 1) {
			// Only Chain Hit
			double ricochetProbability = 0.33 * EnemyInformation.probabilityBulletWillHitWeakpoint();
			double totalNumRicochets = Math.round(ricochetProbability * (getMagazineSize() + getCarriedAmmo()));
			ricochetTotalDamage = totalNumRicochets * getDirectDamage();
		}
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedTier5 == 1) {
			double timeBeforeNeuroProc = Math.round(1.0 / 0.5) / getRateOfFire();
			double neurotoxinDoTTotalDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = numberOfTargets * (calculateFiringDuration() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTTotalDamagePerEnemy * estimatedNumEnemiesKilled;
		}

		return damagePerMagazine * numberOfMagazines + ricochetTotalDamage + neurotoxinDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		/*
			There are 8 combinations of ways for the Revolver to hit an additional target, based on various combinations of
			the Overclocks "Chain Hit" and "Magic Bullets", and the Tier 3 Mods "Super Blowthrough Rounds" and "Explosive Rounds"
		*/
		// If Super Blowthrough Rounds is equipped, then the ricochets from either "Chain Hit" or "Magic Bullets" won't affect the additional targets
		if (selectedTier3 == 0) {
			return 1 + getMaxPenetrations();
		}
		
		// Only Explosive
		else if (selectedTier3 == 1 && selectedOverclock != 1 && selectedOverclock != 5) {
			// From my limited testing, it appears that the full damage radius == full radius, so the efficiency will be 100%
			return calculateNumGlyphidsInRadius(getAoERadius());
		}
		
		// Only "Chain Hit"
		else if (selectedOverclock == 1 && selectedTier3 != 0 && selectedTier3 != 1) {
			return 2;
		}
		
		// "Chain Hit" + Explosive
		else if (selectedOverclock == 1 && selectedTier3 == 1) {
			// Because the second hit is guaranteed to hit another primary target, this is 2*numTargets - overlap
			// I'm guessing that of the 8 Glyphid Grunts, about 3 would be hit by both explosions.
			return (2 * calculateNumGlyphidsInRadius(getAoERadius())) - 3;
		}
		
		// "Magic Bullets" + Explosive
		else if (selectedOverclock == 5 && selectedTier3 == 1) {
			// Because the bullet has to first MISS a target, but the ricochet explodes, this is effectively (2*numTargets - 1) - overlap so that the primary target doesn't get double-counted
			// I'm choosing to model the overlapping Grunts as 5 instead of 3, because it's likely that the bullet lands near the center target that it ricochets to so more of the Grunts would 
			// be hit by both explosions.
			return (2 * calculateNumGlyphidsInRadius(getAoERadius()) - 1) - 5;
		}
		
		else {
			// Because Magic Bullets have to MISS in order to hit a secondary target, they don't increase the numTarget count unless Explosive Rounds is equipped
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
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getAreaDamage();
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double unchangingBaseSpread = 14;
		double changingBaseSpread = 30;
		double spreadVariance = 148;
		double spreadPerShot = 129;
		double spreadRecoverySpeed = 109.1390954;
		double recoilPerShot = 155;
		
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 6.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 1.0;
		
		// Elephant Rounds significantly reduces the recoil speeds in addition to increasing recoil per shot
		double SpSModifier = getSpreadPerShot();
		if (selectedOverclock == 4) {
			// It also increases Max Spread
			spreadVariance = 389;
			
			if (selectedTier2 != 1) {
				// And if Floating Barrel isn't equipped, then the Spread per Shot takes it to Max Spread on first shot for some reason?
				spreadPerShot = 389;
				SpSModifier = 1.0;
				
			}
			
			recoilUpInterval = 16.0 / 60.0;
			recoilDownInterval = 140.0 / 60.0;
		}
		
		double[] modifiers = {getBaseSpread(), SpSModifier, getSpreadRecoverySpeed(), 1.0, getRecoil()};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, false, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval, modifiers);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getAreaDamage()) * UtilityInformation.ArmorBreak_Utility;
		
		// Neurotoxin Slow; 50% chance
		if (selectedTier5 == 1) {
			utilityScores[3] = 0.5 * calculateMaxNumTargets() * DoTInformation.Neuro_SecsDuration * UtilityInformation.Neuro_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Innate stun; 50% chance for 1.5 sec duration
		utilityScores[5] = stunChance * calculateMaxNumTargets() * stunDuration * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}
