package engineerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.AccuracyEstimator;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Shotgun extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private int numberOfPellets;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointStunChance;
	private int stunDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Shotgun() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Shotgun(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Shotgun(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Warthog\" Auto 210";
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 7;
		numberOfPellets = 8;
		carriedAmmo = 90;
		magazineSize = 6;
		rateOfFire = 2.0;
		reloadTime = 2.0;
		weakpointStunChance = 0.1;
		stunDuration = 3;
		
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
		tier1[0] = new Mod("Supercharged Feed Mechanism", "+1 Rate of Fire", 1, 0);
		tier1[1] = new Mod("Overstuffed Magazine", "+2 Magazine Size", 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", 2, 0);
		tier2[1] = new Mod("Loaded Shells", "+2 Pellets per Shot", 2, 1);
		tier2[2] = new Mod("Choke", "x0.5 Base Spread", 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Recoil Dampener", "x0.4 Recoil", 3, 0);
		tier3[1] = new Mod("Quickfire Ejector", "-0.5 Reload Time", 3, 1);
		tier3[2] = new Mod("High Capacity Magazine", "+3 Magazine Size", 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Tungsten Coated Buckshot", "+400% Armor Breaking", 4, 0);
		tier4[1] = new Mod("Bigger Pellets", "+1 Damage per Pellet", 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Turret Whip", "Shoot your turrets to make them shoot a projectile that does 120 Area Damage in a 1m Radius. 3 second cooldown per turret.", 5, 0, false);
		tier5[1] = new Mod("Miner Adjustments", "Changes the Shotgun from semi-automatic to fully automatic, +0.5 Rate of Fire", 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Stunner", "Pellets now have a 10% chance to stun any time they damage an enemy, and any shots that hit a target that's already stunned deal extra damage.", 0, false);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Light-Weight Magazines", "+20 Max Ammo, -0.2 Reload Time", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Magnetic Pellet Alignment", "x0.5 Base Spread, +30% Weakpoint Bonus, x0.75 Rate of Fire", 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Cycle Overload", "+1 Damage per Pellet, +2 Rate of Fire, +0.5 Reload Time, x1.5 Base Spread", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Mini Shells", "+90 Max Ammo, +6 Magazine Size, x0.5 Recoil, -2 Damage per Pellet", 4);
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
				System.out.println("Shotgun's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Shotgun's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Shotgun's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Shotgun clone() {
		return new Shotgun(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "Shotgun";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		
		if (selectedTier4 == 1) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 2;
		}
		
		return toReturn;
	}
	private int getNumberOfPellets() {
		int toReturn = numberOfPellets;
		
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 40;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 20;
		}
		else if (selectedOverclock == 4) {
			toReturn += 90;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 2;
		}
		if (selectedTier3 == 2) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 6;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier1 == 0) {
			toReturn += 1.0;
		}
		
		if (selectedTier5 == 1) {
			toReturn += 0.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 0.75;
		}
		else if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.5;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.4;
		}
		else if (selectedOverclock == 3) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointStunChance() {
		double toReturn = weakpointStunChance;
		
		// OC "Mini Shells" removes stun chance
		if (selectedOverclock == 4) {
			toReturn = 0;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		
		// OC "Mini Shells" removes stun chance
		if (selectedOverclock == 4) {
			toReturn = 0;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		if (selectedOverclock == 2) {
			return 0.3;
		}
		else {
			return 0;
		}
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 0) {
			return 5.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 2) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn *= 0.4;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		boolean damageModified = selectedTier4 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Damage per Pellet:", getDamagePerPellet(), damageModified);
		
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPellets(), selectedTier2 == 1);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		boolean RoFModified = selectedTier1 == 0 || selectedTier5 == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), RoFModified);
		
		boolean reloadModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), reloadModified);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedOverclock == 2, selectedOverclock == 2);
		
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), selectedTier4 == 0, selectedTier4 == 0);
		
		String stunDescription;
		if (selectedOverclock == 0) {
			stunDescription = "Stun Chance per Pellet:";
		}
		else {
			stunDescription = "Weakpoint Stun Chance per Pellet:";
		}
		toReturn[8] = new StatsRow(stunDescription, convertDoubleToPercentage(getWeakpointStunChance()), selectedOverclock == 0 || selectedOverclock == 4);
		
		toReturn[9] = new StatsRow("Stun Duration:", getStunDuration(), selectedOverclock == 4);
		
		boolean baseSpreadModified = selectedTier2 == 2 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[10] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		boolean recoilModified = selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[11] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
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
		double generalAccuracy, duration, directWeakpointDamagePerPellet;
		
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
			directWeakpointDamagePerPellet = increaseBulletDamageForWeakpoints2(getDamagePerPellet(), getWeakpointBonus());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamagePerPellet = getDamagePerPellet();
		}
		
		int numPelletsPerShot = getNumberOfPellets();
		int pelletsThatHitWeakpointPerShot = (int) Math.round(numPelletsPerShot * weakpointAccuracy);
		int pelletsThatHitTargetPerShot = (int) Math.round(numPelletsPerShot * generalAccuracy) - pelletsThatHitWeakpointPerShot;
		
		return (pelletsThatHitWeakpointPerShot * directWeakpointDamagePerPellet + pelletsThatHitTargetPerShot * getDamagePerPellet()) * getMagazineSize() / duration;
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
		// Shotgun cannot naturally hit more than one target, unless pellet spread is large enough to hit multiple swarmers. In that case, the DPS is equivalent to the single-target DPS but spread between targets.
		return 0.0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return (double) (getDamagePerPellet() * getNumberOfPellets() * (getMagazineSize() + getCarriedAmmo()));
	}

	@Override
	public int calculateMaxNumTargets() {
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
		double dmgPerShot = increaseBulletDamageForWeakpoints(increaseBulletDamageForWeakpoints(getDamagePerPellet()) * getNumberOfPellets());
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double unchangingBaseSpread = 104;
		double changingBaseSpread = 96;
		double spreadVariance = 0;
		double spreadPerShot = 0;
		double spreadRecoverySpeed = 0;
		double recoilPerShot = 124.036285;
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 3.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 4.0 / 3.0;
		
		double[] modifiers = {getBaseSpread(), 1.0, 1.0, 1.0, getRecoil()};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, true, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval, modifiers);
	}

	@Override
	public double utilityScore() {
		// Armor Breaking
		utilityScores[2] = (getArmorBreaking() - 1) * UtilityInformation.ArmorBreak_Utility;
		
		// Weakpoint = 10% stun chance per pellet, 2 sec duration (upgraded with Mod Tier 3 "Stun Duration")
		// Because Stunner changes it from weakpoints to anywhere on the body, I'm making the Accuracy change to reflect that.
		double stunAccuracy = estimatedAccuracy(selectedOverclock != 0) / 100.0;
		int numPelletsThatHaveStunChance = (int) Math.round(getNumberOfPellets() * stunAccuracy);
		// Only 1 pellet needs to succeed in order to stun the creature
		double totalStunChancePerShot = MathUtils.cumulativeBinomialProbability(getWeakpointStunChance(), numPelletsThatHaveStunChance, 1);
		utilityScores[5] = totalStunChancePerShot * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}
