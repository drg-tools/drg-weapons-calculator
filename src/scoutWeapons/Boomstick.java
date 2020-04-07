package scoutWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

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
		tier1[0] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 1, 0);
		tier1[1] = new Mod("Double-Sized Buckshot", "Bigger and heavier handcrafted specialist dwarf buckshot. Accept no substitute.", 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Double Trigger", "Tweaked trigger mechanism allows you to unload both barrels in quick succession dealing massive damage to anything in front of you.", 2, 0);
		tier2[1] = new Mod("Quickfire Ejector", "Experience, training, and a couple of under-the-table design \"adjustments\" means your gun can be reloaded significantly faster.", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Stun Duration", "Stunned enemies are incapacitated for a longer period of time.", 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 3, 1);
		tier3[2] = new Mod("High Capacity Shells", "It took some creating thinking, but we finally found out how to pack more buckshot into each shell. Just... Handle with care, they're liable to take your eye out.", 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "Shaped projectiles designed to over-penetrate targets with a minimal loss of energy. In other words: Fire straight through several enemies at once!", 4, 0);
		tier4[1] = new Mod("Tungsten Coated Buckshot", "We're proud of this one. Armor shredding. Tear through that high-impact plating of those big buggers like butter. What could be finer?", 4, 1);
		tier4[2] = new Mod("Improved Blast Wave", "The shockwave from the blast deals extra damage to any enemies unlucky enough to be in the area extending 4m infront of you.", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Auto Reload", "Reloads automatically when unequipped for more than 5 seconds", 5, 0, false);
		tier5[1] = new Mod("Fear The Boomstick", "Chance to scare nearby creatures whenever you shoot", 5, 1);
		tier5[2] = new Mod("White Phosphorous Shells", "Convert some of the damage to fire damage", 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Shells", "You can carry a few more of these compact shells in your pockets and they are a bit faster to reload with.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Double Barrel", "Unload both barrels at once, no regrets.", 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Special Powder", "Less like gunpowder and more like rocketfuel, this mixture gives a hell of a kick that you can use to get places.", 2);
		overclocks[3] = new Overclock(Overclock.classification.clean, "Stuffed Shells", "With a bit of patience and some luck you can get one more pellet and a few more grains of powder into each shell without affecting the gun's performance or losing an eye in the process.", 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Shaped Shells", "Specially shaped shells result in a tighter shot but the number of pellets is reduced.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Jumbo Shells", "These large shells pack a lot more charge for a big increase in damage but they also take up more space so total ammo is limited.", 5);
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
	private int getNumberOfPelletsPerShot() {
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
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPelletsPerShot(), pelletsModified);
		
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
			numPelletsThatApplyHeat = (int) Math.round(estimatedAccuracy(false) * getNumberOfPelletsPerShot());
		}
		else {
			numPelletsThatApplyHeat = getNumberOfPelletsPerShot();
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
		
		double weakpointAccuracy;
		if (weakpoint) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamagePerPellet = increaseBulletDamageForWeakpoints2(getDamagePerPellet());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamagePerPellet = getDamagePerPellet();
		}
		
		// They way it's currently modeled, any time the WPS mod and Double Barrel OC are equipped simultaneously, then the Reload Time doesn't affect the Fire DoT Uptime.
		double burnDPS = 0;
		if (selectedTier5 == 2) {
			if (burst) {
				double timeToIgnite = calculateTimeToIgnite(accuracy);
				double fireDoTUptimeCoefficient = (duration - timeToIgnite) / duration;
				
				burnDPS = fireDoTUptimeCoefficient * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		int numPelletsPerShot = getNumberOfPelletsPerShot();
		int pelletsThatHitWeakpointPerShot = (int) Math.round(numPelletsPerShot * weakpointAccuracy);
		int pelletsThatHitTargetPerShot = (int) Math.round(numPelletsPerShot * generalAccuracy) - pelletsThatHitWeakpointPerShot;
		
		return (pelletsThatHitWeakpointPerShot * directWeakpointDamagePerPellet + pelletsThatHitTargetPerShot * getDamagePerPellet() + getBlastwaveDamage()) * magSize / duration + burnDPS;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		// TODO: I'd like to refactor this method out if possible
		double damagePerShot;
		if (weakpointBonus) {
			damagePerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet() * getNumberOfPelletsPerShot()) + getBlastwaveDamage();
			return (double) damagePerShot * getMagazineSize();
		}
		else {
			damagePerShot = getDamagePerPellet() * getNumberOfPelletsPerShot() + getBlastwaveDamage();
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
		int directDamagePerShot = getDamagePerPellet() * getNumberOfPelletsPerShot();
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
				fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeIgnite, EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
				
				fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
			}
			else {
				double percentageOfEnemiesIgnitedPerShot = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(0.5 * directDamagePerShot);
				fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
				
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
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet() * getNumberOfPelletsPerShot());
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
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
	public double utilityScore() {
		// OC "Special Powder" gives a lot of Mobility (7.8m vertical per shot, 13m horizontal per shot)
		if (selectedOverclock == 2) {
			// Multiply by 2 for mobility per shot
			utilityScores[0] = 2 * (0.5 * 7.8 + 0.5 * 13) * UtilityInformation.BlastJump_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Armor Breaking bonuses
		utilityScores[2] = (getArmorBreaking() - 1) * calculateMaxNumTargets() * UtilityInformation.ArmorBreak_Utility;
		
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
}
