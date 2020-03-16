package gunnerWeapons;

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
	private double minRateOfFire;
	private double maxRateOfFire;
	private int numBulletsFiredDuringRampup;
	private double reloadTime;
	private double baseSpread;
	private double armorBreakChance;
	
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
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 14;
		areaDamage = 8;
		aoeRadius = 1.2;  // meters
		magazineSize = 110;
		carriedAmmo = 440;
		movespeedWhileFiring = 0.5;
		minRateOfFire = 1.0;
		maxRateOfFire = 5.5;  // Before 5.5 was listed in-game this was measured to be 5.25
		numBulletsFiredDuringRampup = 10;
		reloadTime = 5.0;  // seconds
		baseSpread = 1.0;  // equivalent to its accuracy
		armorBreakChance = 1.0;
		
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
		tier1[0] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "The good thing about clips, magazines, ammo drums, fuel tanks ...you can always get bigger variants.", 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-space, but your total ammo capacity is increased!", 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Tighter Barrel Alignment", "Improved accuracy", 2, 0);
		tier2[1] = new Mod("Improved Gas System", "We overclocked your gun. It fires faster. Don't ask, just enjoy. Also probably don't tell Management, please.", 2, 1);
		tier2[2] = new Mod("Lighter Barrel Assembly", "Reach the max rate of fire faster", 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Supercharged Feed Mechanism", "We overclocked your gun. It fires faster. Don't ask, just enjoy. Also probably don't tell Management, please.", 3, 0);
		tier3[1] = new Mod("Loaded Rounds", "Increased splash damage", 3, 1);
		tier3[2] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Penetrating Rounds", "We're proud of this one. Armor shredding. Tear through that high-impact plating of those bug buggers like butter. What could be finer?", 4, 0);
		tier4[1] = new Mod("Shrapnel Rounds", "Greater splash damage radius", 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Feedback Loop", "Increased damage when at max rate of fire", 5, 0);
		tier5[1] = new Mod("Suppressive Fire", "Chance to scare enemies next to a bullet impact", 5, 1);
		tier5[2] = new Mod("Damage Resistance At Full RoF", "Gain damage reduction when at max rate of fire", 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Drums", "Lighter weight materials means you can carry even more ammo!", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Splintering Shells", "Specially designed shells splinter into smaller pieces increasing the splash damage range.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Carpet Bomber", "A few tweaks here and there and the autocannon can now shoot HE rounds! Direct damage is lower but the increased splash damage and range lets you saturate and area like no other weapon can.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Combat Mobility", "A slight reduction in the power of the rounds permits using a smaller chamber and a light-weight backplate with in turn allows extensive weight redistribution. The end result is a weapon that still packs a punch but is easier to handle on the move.", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Big Bertha", "Extensive tweaks give a huge bump in raw damage at the cost of ammo capacity and fire rate.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neurotoxin Payload", "Channel your inner war criminal by mixing some neurotoxin into the explosive compound. The rounds deal less direct damage and splash damage, but affected bugs move slower and take lots of damage over time.", 5);
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
			toReturn -= 6;
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
			toReturn += 4;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 6;
		}
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier4 == 1) {
			toReturn += 0.5;
		}
		if (selectedOverclock == 1) {
			toReturn += 0.3;
		}
		else if (selectedOverclock == 2) {
			toReturn += 1.0;
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
	private int getNumBulletsRampup() {
		if (selectedTier2 == 2) {
			return numBulletsFiredDuringRampup / 2;
		}
		else {
			return numBulletsFiredDuringRampup;
		}
	}
	private double getMaxRateOfFire() {
		double toReturn = maxRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 1;  // Before being listed in-game, this used to be a +15% modifier
		}
		if (selectedTier3 == 0) {
			toReturn += 2;  // Before being listed in-game, this used to be a +35% modifier
		}
		if (selectedOverclock == 4) {
			toReturn -= 1.5;  // Before being listed in-game, this used to be a -25% modifier
		}
		return toReturn;
	}
	private double getAverageRateOfFire() {
		int numBulletsRampup = getNumBulletsRampup();
		int magSize = getMagazineSize();
		double maxRoF = getMaxRateOfFire();
		return ((minRateOfFire + maxRoF) / 2.0 * numBulletsRampup + maxRoF * (magSize - numBulletsRampup)) / magSize;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		if (selectedOverclock == 0) {
			toReturn -= 0.5;
		}
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = baseSpread;
		if (selectedTier2 == 0) {
			toReturn -= 0.3;
		}
		return toReturn;
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		if (selectedTier4 == 0) {
			toReturn += 4.0;
		}
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[11];
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 2 || (selectedOverclock > 1 && selectedOverclock < 6);
		toReturn[0] = new StatsRow("Damage:", getDirectDamage(), directDamageModified);
		
		boolean areaDamageModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[2] = new StatsRow("Effect Radius:", getAoERadius(), aoeRadiusModified);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), selectedTier1 == 1 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 3);
		
		toReturn[6] = new StatsRow("Number of Bullets Fired Before Max RoF:", getNumBulletsRampup(), selectedTier2 == 2);
		
		// tier2 indexes 1 & 2 both increase RoF
		boolean RoFModified = selectedTier2 > 0 || selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[7] = new StatsRow("Average Rate of Fire:", getAverageRateOfFire(), RoFModified);
		
		toReturn[8] = new StatsRow("Reload Time:", getReloadTime(), selectedOverclock == 0);
		
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedTier2 == 0);
		
		toReturn[10] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier4 == 0);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return true;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus, int numTargets) {
		double damagePerBullet;
		if (weakpointBonus) {
			damagePerBullet = increaseBulletDamageForWeakpoints(getDirectDamage()) + numTargets * getAreaDamage();
		}
		else {
			damagePerBullet = getDirectDamage() + numTargets * getAreaDamage();
		}
		double magSize = (double) getMagazineSize();
		double damageMultiplier = 1.0;
		if (selectedTier5 == 0) {
			double numBulletsRampup = (double) getNumBulletsRampup();
			damageMultiplier = (numBulletsRampup + 1.2*(magSize - numBulletsRampup)) / magSize;
		}
		return damagePerBullet * magSize * damageMultiplier;
	}

	@Override
	public double calculateIdealBurstDPS() {
		/*
			There are two ways to calculate this: 
				(damage/bullet times bullets/mag) / (bullets/mag divided by bullets/sec) 
			OR 
				damage/bullet times bullets/sec
				
			I THINK that they're equivalent, but I'll do it the long way to be sure.
		*/
		double timeToFireMagazine = ((double) getMagazineSize()) / getAverageRateOfFire();
		double burstDPS = calculateDamagePerMagazine(false, 1) / timeToFireMagazine;
		
		if (selectedOverclock == 5) {
			// Neurotoxin Payload has a 20% chance to inflict the DoT
			burstDPS += calculateRNGDoTDPSPerMagazine(0.2, DoTInformation.Neuro_DPS, getMagazineSize());
		}
		
		return burstDPS;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		double sustainedDPS = calculateDamagePerMagazine(false, 1) / timeToFireMagazineAndReload;
		
		if (selectedOverclock == 5) {
			sustainedDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedDPS;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		double sustainedWeakpointDPS = calculateDamagePerMagazine(true, 1) / timeToFireMagazineAndReload;
		
		if (selectedOverclock == 5) {
			sustainedWeakpointDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedWeakpointDPS;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO: this is incomplete; I was just curious to see how the inaccuracy affected this gun.
		double magSize = getMagazineSize();
		double accuracy = estimatedAccuracy();
		// Assuming that all bullets' splash hits the intended target, but only accuracy% bullets hit directly
		double directDamage = this.increaseBulletDamageForWeakpoints(getDirectDamage());
		int areaDamage = getAreaDamage();
		double avgDmgPerBullet = (accuracy*(directDamage + areaDamage) + (100 - accuracy)*areaDamage)/100.0;
		
		double dmgPerMagazine = avgDmgPerBullet * magSize;
		double timeToFireMagazineAndReload = (magSize / getAverageRateOfFire()) + getReloadTime();
		
		return dmgPerMagazine / timeToFireMagazineAndReload;
	}


	@Override
	public double calculateAdditionalTargetDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		double magSize = (double) getMagazineSize();
		double damageMultiplier = 1.0;
		if (selectedTier5 == 0) {
			double numBulletsRampup = (double) getNumBulletsRampup();
			damageMultiplier = (numBulletsRampup + 1.2*(magSize - numBulletsRampup)) / magSize;
		}
		double areaDamagePerMag = getAreaDamage() * magSize * damageMultiplier;
		
		double sustainedAdditionalDPS = areaDamagePerMag / timeToFireMagazineAndReload;
		
		if (selectedOverclock == 5) {
			sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedAdditionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numTargets = calculateMaxNumTargets();
		double damagePerMagazine = calculateDamagePerMagazine(false, numTargets);
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedOverclock == 5) {
			double timeBeforeNeuroProc = Math.round(1.0 / 0.2) / getAverageRateOfFire();
			double neurotoxinDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return damagePerMagazine * numberOfMagazines + neurotoxinDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return calculateNumGlyphidsInRadius(getAoERadius());
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getAverageRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage()) + getAreaDamage();
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy() {
		double crosshairHeightDegrees, crosshairWidthDegrees;
		
		if (selectedTier2 == 0) {
			// Base Spead = 70%
			crosshairHeightDegrees = 7.939388723;
			crosshairWidthDegrees = 17.29030524;
		}
		else {
			// Base Spread = 100%
			crosshairHeightDegrees = 10.24520617;
			crosshairWidthDegrees = 23.89008816;
		}
		double crosshairHeightMeters = AccuracyEstimator.targetDistance * Math.tan(crosshairHeightDegrees * Math.PI / 180.0);
		double crosshairWidthMeters = AccuracyEstimator.targetDistance * Math.tan(crosshairWidthDegrees * Math.PI / 180.0);
		
		double targetRadius = AccuracyEstimator.targetRadius;
		
		/*
			From observation, it looks like the horizontal distribution of bullets followed a bell curve such that the highest probabilities were in the center of the rectangle, 
			and the lower probabilities were near the edges. To model that, I'm choosing to calculate the sum of the probabilities that the horizontal spread will be within 
			the target radius and then I'm going to multiply the area of the circle by that weight to artificially increase the accuracy.
		*/
		// Convert the target radius in meters to the unitless probability ellipse
		double endOfProbabilityCurve = 2.0 * Math.sqrt(2.0);
		double horizontalProbabilityRatio = endOfProbabilityCurve * targetRadius / (crosshairWidthMeters / 2.0);
		double hProb = MathUtils.areaUnderNormalDistribution(-1.0 * horizontalProbabilityRatio, horizontalProbabilityRatio);
		double verticalProbabilityRatio = endOfProbabilityCurve * targetRadius / (crosshairHeightMeters / 2.0);
		double vProb = MathUtils.areaUnderNormalDistribution(-1.0 * verticalProbabilityRatio, verticalProbabilityRatio);
		
		double areaOfProbabilityEllipse = Math.PI * hProb * vProb / 4.0;
		
		return areaOfProbabilityEllipse * 100.0;
	}

	@Override
	public double utilityScore() {
		// OC "Combat Mobility" increases Gunner's movespeed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Mod Tier 5 "Damage Resist" gives 30% damage reduction at max RoF
		if (selectedTier5 == 2) {
			double EHPmultiplier = (1 / (1 - 0.3));
			
			int numBulletsRampup = getNumBulletsRampup();
			int magSize = getMagazineSize();
			double maxRoF = getMaxRateOfFire();
			double timeRampingUp = numBulletsRampup / ((minRateOfFire + maxRoF) / 2.0); 
			double timeAtMaxRoF = (magSize - numBulletsRampup) / maxRoF;
			
			double fullRoFUptime = timeAtMaxRoF / (timeRampingUp + timeAtMaxRoF);
			
			utilityScores[1] = fullRoFUptime * EHPmultiplier * UtilityInformation.DamageResist_Utility;
		}
		else {
			utilityScores[1] = 0;
		}
		
		// Mod Tier 4 "Pentrating Rounds" armor breaking bonus
		utilityScores[2] = (getArmorBreakChance() - 1) * calculateMaxNumTargets() * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Neurotoxin Payload" has a 20% chance to inflict a 30% slow by poisoning enemies
		if (selectedOverclock == 5) {
			utilityScores[3] = 0.2 * calculateMaxNumTargets() * DoTInformation.Neuro_SecsDuration * UtilityInformation.Neuro_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Mod Tier 5 "Suppressive Fire" induces Fear (20-50% chance maybe?)
		if (selectedTier5 == 1) {
			utilityScores[4] = 0.2 * calculateMaxNumTargets() * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}

}
