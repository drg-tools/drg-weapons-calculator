package gunnerWeapons;

import java.util.Arrays;
import java.util.List;

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
import utilities.MathUtils;

public class Minigun extends Weapon {

	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private double stunChancePerPellet;
	private int stunDuration;
	private int maxAmmo;
	private double maxHeat;
	private double heatPerSecond;
	private double coolingRate;
	private int rateOfFire;
	private double spinupTime;
	private int spindownTime;
	private double movespeedWhileFiring;
	private int bulletsFiredTilMaxStability;
	private int cooldownAfterOverheat;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Minigun() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build without having to type a lot of enumerated variables
	public Minigun(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Minigun(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Lead Storm\" Powered Minigun";
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 10;
		stunChancePerPellet = 0.3;
		stunDuration = 1;
		maxAmmo = 2400; // equal to 1200 pellets
		maxHeat = 9.5;
		heatPerSecond = 1.0;
		coolingRate = 1.5;
		rateOfFire = 30;  // equal to 15 pellets/sec
		spinupTime = 0.7;
		spindownTime = 3;  // seconds for the stability to decay full rotational speed down to stationary barrels
		movespeedWhileFiring = 0.5;
		bulletsFiredTilMaxStability = 40;  // equals 20 pellets
		cooldownAfterOverheat = 10;
		
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
		tier1[0] = new Mod("Magnetic Refrigeration", "+1.5 Cooling Rate", modIcons.coolingRate, 1, 0);
		tier1[1] = new Mod("Improved Motor", "+4 Rate of Fire", modIcons.rateOfFire, 1, 1);
		tier1[2] = new Mod("Improved Platform Stability", "x0.2 Base Spread", modIcons.baseSpread, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Oversized Drum", "+600 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("High Velocity Rounds", "+2 Damage per Pellet", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Hardened Rounds", "+200% Armor Breaking", modIcons.armorBreaking, 3, 0);
		tier3[1] = new Mod("Stun Duration", "+1 second Stun duration", modIcons.stun, 3, 1);
		tier3[2] = new Mod("Blowthrough Rounds", "+1 Penetration", modIcons.blowthrough, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Variable Chamber Pressure", "+15% Damage per Pellet after reaching Base Spread", modIcons.directDamage, 4, 0); // TODO: find the right icon
		tier4[1] = new Mod("Lighter Barrel Assembly", "-0.4 seconds spinup time", modIcons.chargeSpeed, 4, 1);
		tier4[2] = new Mod("Magnetic Bearings", "+3 seconds spindown time", modIcons.special, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Aggressive Venting", "After overheating, deal 75 Heat Damage and 100% chance to apply Fear to all enemies within a 3m radius", modIcons.addedExplosion, 5, 0);
		tier5[1] = new Mod("Cold As The Grave", "Every kill reduces the current Heat Meter and thus increases the firing duration before overheating", modIcons.coolingRate, 5, 1);
		tier5[2] = new Mod("Hot Bullets", "After the Heat Meter turns red, 50% of the Damage per Pellet gets added as Heat Damage", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "A Little More Oomph!", "+1 Damage per Pellet, -0.2 spinup time", overclockIcons.directDamage, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Thinned Drum Walls", "+300 Max Ammo, +0.5 Cooling Rate", overclockIcons.coolingRate, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Burning Hell", "While firing, the Minigun deals 100 Heat per Second in a cone 6m in front of the muzzle. +50% heat accumulation in the "
				+ "gun's heat meter, which translates to 2/3 the firing period", overclockIcons.heatDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Compact Feed Mechanism", "+800 Max Ammo, -4 Rate of Fire", overclockIcons.carriedAmmo, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Exhaust Vectoring", "+2 Damage per Pellet, x2.5 Base Spread", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Bullet Hell", "50% for bullets that impact an enemy or terrain to ricochet into another enemy. -3 Damage per Pellet, x6 Base Spread", overclockIcons.ricochet, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Lead Storm", "+4 Damage per Pellet, x0 Movespeed while using, and the Minigun cannot stun enemies anymore.", overclockIcons.directDamage, 6);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		boolean codeIsValid = true;
		char[] symbols = combination.toCharArray();
		if (combination.length() != 6) {
			System.out.println(combination + " does not have 6 characters, which makes it invalid");
			codeIsValid = false;
		}
		else {
			List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
			for (int i = 0; i < 5; i ++) {
				if (!validModSymbols.contains(symbols[i])) {
					System.out.println("Symbol #" + (i+1) + ", " + symbols[i] + ", is not a capital letter between A-C or a hyphen");
					codeIsValid = false;
				}
			}
			if (symbols[1] == 'C') {
				System.out.println("Minigun's second tier of mods only has two choices, so 'C' is an invalid choice.");
				codeIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-7 or a hyphen");
				codeIsValid = false;
			}
		}
		
		if (codeIsValid) {
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
	public Minigun clone() {
		return new Minigun(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "Minigun";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	public int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		if (selectedOverclock == 0) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn += 2;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 6) {
			toReturn += 4;
		}
		return toReturn;
	}
	private double getStunChancePerPellet() {
		double toReturn = stunChancePerPellet;
		
		if (selectedOverclock == 6) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		if (selectedTier3 == 1) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 6) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	private int getMaxAmmo() {
		int toReturn = maxAmmo;
		if (selectedTier2 == 0) {
			toReturn += 600;
		}
		if (selectedOverclock == 1) {
			toReturn += 300;
		}
		else if (selectedOverclock == 3) {
			toReturn += 800;
		}
		return toReturn;
	}
	private double getHeatPerSecond() {
		double toReturn = heatPerSecond;
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private double getCoolingRate() {
		double toReturn = coolingRate;
		if (selectedTier1 == 0) {
			toReturn += 1.5;
		}
		if (selectedOverclock == 1) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private int getRateOfFire() {
		int toReturn = rateOfFire;
		if (selectedTier1 == 1) {
			toReturn += 4;
		}
		if (selectedOverclock == 3) {
			toReturn -= 4;
		}
		return toReturn;
	}
	private double getSpinupTime() {
		double toReturn = spinupTime;
		if (selectedTier4 == 1) {
			toReturn -= 0.4;
		}
		if (selectedOverclock == 0) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private int getSpindownTime() {
		int toReturn = spindownTime;
		if (selectedTier4 == 2) {
			toReturn += 3;
		}
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = movespeedWhileFiring;
		if (selectedOverclock == 6) {
			modifier *= 0;
		}
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		if (selectedTier1 == 2) {
			toReturn *= 0.2;
		}
		if (selectedOverclock == 4) {
			toReturn *= 2.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 6.0;
		}
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier3 == 0) {
			return 3.0;
		}
		else {
			return 1.0;
		}
	}
	private int getNumberOfPenetrations() {
		if (selectedTier3 == 2) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private int getNumberOfRicochets() {
		if (selectedOverclock == 5) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	private double calculateFiringPeriod() {
		double firingPeriod = maxHeat / getHeatPerSecond();
		
		// Cold as the Grave removes a set amount of Heat from the Minigun's meter every time that the Minigun gets the killing blow on an enemy.
		// TODO: Although the way it's implemented avoids the infinite loop of methods calling each other, I'm not satisfied with how CATG is modeled currently.
		if (selectedTier5 == 1) {
			// Amount of Heat removed per kill, depending on enemy size. (pure guesses)
			double smallEnemy = 0.1;
			double mediumEnemy = 0.5;
			double largeEnemy = 1.0;
			double [] heatRemovalVector = {
				smallEnemy,  // Glyphid Swarmer
				mediumEnemy,  // Glyphid Grunt
				mediumEnemy,  // Glyphid Grunt Guard
				mediumEnemy,  // Glyphid Grunt Slasher
				largeEnemy,  // Glyphid Praetorian
				smallEnemy,  // Glyphid Exploder
				largeEnemy,  // Glyphid Bulk Detonator
				largeEnemy,  // Glyphid Crassus Detonator
				mediumEnemy,  // Glyphid Webspitter
				mediumEnemy,  // Glyphid Acidspitter
				largeEnemy,  // Glyphid Menace
				largeEnemy,  // Glyphid Warden
				largeEnemy,  // Glyphid Oppressor
				largeEnemy,  // Q'ronar Shellback
				mediumEnemy,  // Mactera Spawn
				mediumEnemy,  // Mactera Grabber
				largeEnemy,  // Mactera Bomber
				largeEnemy,  // Naedocyte Breeder
				largeEnemy,  // Glyphid Brood Nexus
				largeEnemy,  // Spitball Infector
				smallEnemy   // Cave Leech
			};
			double averageHeatRemovedOnKill = EnemyInformation.dotProductWithSpawnRates(heatRemovalVector);
			
			// This is a quick-and-dirty way to guess what the Ideal Burst DPS will be when it's all said and done without calculating Firing Period and causing an infinite loop.
			double estimatedBurstDPS = getDamagePerPellet() * getRateOfFire() / 2.0;
			if (selectedTier4 == 0) {
				// Slight overestimation
				estimatedBurstDPS *= 1.13;
			}
			if (selectedOverclock == 2) {
				// Slight overestimation
				estimatedBurstDPS += 0.95 * DoTInformation.Burn_DPS;
			}
			
			double estimatedBurstTTK = EnemyInformation.averageHealthPool() / estimatedBurstDPS;
			double estimatedNumKillsDuringDefaultPeriod = firingPeriod / estimatedBurstTTK;
			firingPeriod += estimatedNumKillsDuringDefaultPeriod * averageHeatRemovedOnKill;
		}
		
		return firingPeriod;
	}
	private double calculateMaxNumPelletsFiredWithoutOverheating() {
		// Strangely, the RoF does NOT affect the heat/sec gain?
		return Math.floor(calculateFiringPeriod() * getRateOfFire() / 2.0);
	}
	private double calculateCooldownPeriod() {
		// This equation took a while to figure out, and it's still just an approximation. A very close approximation, but an approximation nonetheless.
		return 9.5 / getCoolingRate() + getCoolingRate() / 9;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[19];
		
		boolean damageModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock > 3;
		toReturn[0] = new StatsRow("Direct Damage per Pellet:", getDamagePerPellet(), damageModified);
		
		toReturn[1] = new StatsRow("Ammo Consumed per Pellet:", 2, false);
		
		toReturn[2] = new StatsRow("Ammo Spent Until Stabilized:", bulletsFiredTilMaxStability, false);
		
		toReturn[3] = new StatsRow("Max Duration of Firing Without Overheating:", calculateFiringPeriod(), selectedTier5 == 1 || selectedOverclock == 2);
		
		boolean pelletsPerBurstModified = selectedTier5 == 1 || selectedOverclock == 2 || selectedTier1 == 1 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Max Num Pellets Fired per Burst:", calculateMaxNumPelletsFiredWithoutOverheating(), pelletsPerBurstModified);
		
		boolean ammoModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[5] = new StatsRow("Max Ammo:", getMaxAmmo(), ammoModified);
		
		toReturn[6] = new StatsRow("Rate of Fire (Ammo/Sec):", getRateOfFire(), selectedTier1 == 1 || selectedOverclock == 3);
		
		toReturn[7] = new StatsRow("Cooling Rate:", getCoolingRate(), selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Max Cooldown Without Overheating:", calculateCooldownPeriod(), selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[9] = new StatsRow("Cooldown After Overheat:", cooldownAfterOverheat, false);
		
		toReturn[10] = new StatsRow("Spinup Time:", getSpinupTime(), selectedTier4 == 1 || selectedOverclock == 0);
		
		toReturn[11] = new StatsRow("Spindown Time:", getSpindownTime(), selectedTier4 == 2);
		
		toReturn[12] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), selectedTier3 == 0, selectedTier3 == 0);
		
		toReturn[13] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), selectedTier3 == 2, selectedTier3 == 2);
		
		toReturn[14] = new StatsRow("Max Ricochets:", getNumberOfRicochets(), selectedOverclock == 5, selectedOverclock == 5);
		
		toReturn[15] = new StatsRow("Stun Chance per Pellet:", convertDoubleToPercentage(getStunChancePerPellet()), selectedOverclock == 6);
		
		toReturn[16] = new StatsRow("Stun Duration:", getStunDuration(), selectedTier3 == 1 || selectedOverclock == 6);
		
		boolean baseSpreadModified = selectedTier1 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[17] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		toReturn[18] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 6);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	private double calculateIgnitionTime(boolean accuracy) {
		// It looks like Hot Bullets and Burning Hell both have -50% Burn DoT Durations?
		double burningHellHeatPerSec = 100;
		
		double generalAccuracy;
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false);
		}
		else {
			generalAccuracy = 1.0;
		}
		
		// Special case: the overclock Bullet Hell gives every bullet a 50% chance to ricochet into nearby enemies after impacting terrain or an enemy
		if (selectedOverclock == 5 && accuracy) {
			// Never let it be above 1.0 probability to hit a target.
			generalAccuracy = Math.min(generalAccuracy + 0.5, 1.0);
		}
		
		// Hot Bullets only
		if (selectedTier5 == 2 && selectedOverclock != 2) {
			// Hot Bullets adds 50% of of each pellet's Direct Damage as Heat Damage while the Heat Meter on the Minigun is red.
			// In practice, the meter turns red after 4 seconds of sustained firing, meaning that the last 5.5 seconds of the burst will have Hot Bullets.
			// I'm choosing to reduce the heatPerPellet by the Accuracy of the gun to imitate when pellets miss the target
			double heatPerPellet = ((double) getDamagePerPellet()) * generalAccuracy / 2.0;
			double RoF = getRateOfFire() / 2.0;
			return 4 + EnemyInformation.averageTimeToIgnite(heatPerPellet, RoF);
		}
		// Burning Hell only
		else if (selectedTier5 != 2 && selectedOverclock == 2) {
			// Burning Hell looks like it burns everything within 6m in a 20 degree arc in front of you at a rate of 100 heat/sec
			// TODO: I would like for this to have its AoE damage reflected in max damage too like Aggressive Venting
			return EnemyInformation.averageTimeToIgnite(burningHellHeatPerSec);
		}
		// Both Hot Bullets AND Burning Hell
		else if (selectedTier5 == 2 && selectedOverclock == 2) {
			// Because Burning Hell reduces the Firing Period from 9.5 sec to 6.33 sec, this means that Hot Bullets gets activated after 2.66 seconds instead of 4.
			double heatPerPellet = ((double) getDamagePerPellet()) * generalAccuracy / 2.0;
			double RoF = getRateOfFire() / 2.0;
			double avgHeatPerSec = (2.66 * burningHellHeatPerSec + 3.66 * (heatPerPellet * RoF + burningHellHeatPerSec)) / 6.33;
			return EnemyInformation.averageTimeToIgnite(avgHeatPerSec);
		}
		// Neither are equipped.
		else {
			return -1;
		}
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, shortDuration, longDuration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		// Special case: the overclock Bullet Hell gives every bullet a 50% chance to ricochet into nearby enemies after impacting terrain or an enemy
		if (selectedOverclock == 5 && accuracy) {
			// Never let it be above 1.0 probability to hit a target.
			generalAccuracy = Math.min(generalAccuracy + 0.5, 1.0);
		}
		
		if (burst) {
			shortDuration = calculateFiringPeriod();
			longDuration = calculateFiringPeriod();
			// I've considered adding the spinup time to the burst duration, but seeing it in the metrics was very counter-intuitive -- it made the burst DPS not the intuitively expected 150.
			// longDuration = getSpinupTime() + calculateFiringPeriod();
		}
		else {
			double firingPeriod = calculateFiringPeriod();
			double cooldownPeriod = calculateCooldownPeriod();
			
			shortDuration = firingPeriod + cooldownPeriod;
			
			double spindown = getSpindownTime();
			double spinup;
			if (cooldownPeriod < spindown) {
				double fractionOfSpinupNeeded = 1.0 - (cooldownPeriod / spindown);
				spinup = fractionOfSpinupNeeded * getSpinupTime();
			}
			else {
				spinup = getSpinupTime();
			}
			
			longDuration = firingPeriod + cooldownPeriod + spinup;
		}
		
		int burstSize = (int) calculateMaxNumPelletsFiredWithoutOverheating();
		double directDamage = getDamagePerPellet();
		if (selectedTier4 == 0) {
			double pelletsFiredWhileNotStabilized = bulletsFiredTilMaxStability / 2.0;
			directDamage *= (pelletsFiredWhileNotStabilized + 1.15*(burstSize - pelletsFiredWhileNotStabilized)) / burstSize;
		}
		
		double weakpointAccuracy;
		if (weakpoint) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		double burnDPS = 0;
		if (selectedTier5 == 2 || selectedOverclock == 2) {
			if (burst) {
				double ignitionTime = calculateIgnitionTime(accuracy);
				double burnDoTUptime = (shortDuration - ignitionTime) / shortDuration;
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		int pelletsThatHitWeakpoint = (int) Math.round(burstSize * weakpointAccuracy);
		int pelletsThatHitTarget = (int) Math.round(burstSize * generalAccuracy) - pelletsThatHitWeakpoint;
		
		// TODO: I'm not satisfied with how this turned out, because Ideal Burst DPS always turns out JUST shy of its true value. This is because the num pellets is always one less than what would make it overheat.
		return (pelletsThatHitWeakpoint * directWeakpointDamage + pelletsThatHitTarget * directDamage) / longDuration + burnDPS;
	}
	
	private double calculateDamagePerBurst(boolean weakpointBonus) {
		/* 
			The length of the burst is determined by the heat accumulated. Each burst duration should stop just shy of 
			overheating the minigun so that it doesn't have the overheat cooldown penalty imposed.
			
			TODO: I'd like to refactor out this method if at all possible
		*/
		double numPelletsFiredBeforeOverheat = calculateMaxNumPelletsFiredWithoutOverheating();
		double damageMultiplier = 1.0;
		if (selectedTier4 == 0) {
			double pelletsFiredWhileNotStabilized = bulletsFiredTilMaxStability / 2.0;
			damageMultiplier = (pelletsFiredWhileNotStabilized + 1.15*(numPelletsFiredBeforeOverheat - pelletsFiredWhileNotStabilized)) / numPelletsFiredBeforeOverheat;
		}
		
		if (weakpointBonus) {
			return numPelletsFiredBeforeOverheat * increaseBulletDamageForWeakpoints((double) getDamagePerPellet()) * damageMultiplier;
		}
		else {
			return numPelletsFiredBeforeOverheat * (double) getDamagePerPellet() * damageMultiplier;
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
		double idealSustained = calculateIdealSustainedDPS();
		
		if (selectedTier3 == 2) {
			// Blowthrough Rounds are just the same DPS, with Burn DPS already added if Burning Hell or Hot Bullets is already equipped
			return idealSustained;
		}
		else if (selectedOverclock == 2) {
			return DoTInformation.Burn_DPS;
		}
		else if (selectedOverclock == 5) {
			// Bullet Hell has a 50% chance to ricochet
			return 0.5 * idealSustained;
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numTargets = calculateMaxNumTargets();
		double numPelletsFiredBeforeOverheat = calculateMaxNumPelletsFiredWithoutOverheating();
		double numberOfBursts = (double) getMaxAmmo() / (2.0 * numPelletsFiredBeforeOverheat);
		double totalDamage = numberOfBursts * calculateDamagePerBurst(false) * numTargets;
		
		double fireDoTTotalDamage = 0;
		double timeBeforeFireProc, fireDoTDamagePerEnemy, estimatedNumEnemiesKilled;
		// Both Hot Bullets and Burning Hell are penalized with -50% DoT duration
		// Because of how Hot Bullets' ignition time is calculated, it returns (4 + the ignition time). As a result, it would end up subtracting from the total damage.
		if (selectedTier5 == 2 && selectedOverclock != 2) {
			timeBeforeFireProc = calculateIgnitionTime(false) - 4;
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeFireProc, 0.5 * EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
			
			// Because Hot Bullets only starts igniting enemies after 4 seconds, reduce this damage by the uptime coefficient.
			fireDoTDamagePerEnemy *= (5.5/9.5);
			
			estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
			fireDoTTotalDamage += fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		// Burning Hell, on the other hand, works great with this. Even with Hot Bullets stacked on top of it, it doesn't do negative damage.
		else if (selectedOverclock == 2) {
			timeBeforeFireProc = calculateIgnitionTime(false);
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeFireProc, 0.5 * EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
			
			// TODO: change numTargets to reflect the 6m 20* cone AoE igniting more than just the primary target and sometimes the blowthroughs
			estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
			fireDoTTotalDamage += fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		// Aggressive Venting does one burst of 75 Heat Damage in a 3m radius around the Gunner
		if (selectedTier5 == 0) {
			// I'm choosing to model Aggressive Venting as Fire DoT max damage without affecting DPS stats, since the 11 sec cooldown penalty would TANK all of those stats.
			// Additionally, I'm choosing to not combine its burst of 75 Heat Damage with the Heat/sec dealt by Hot Bullets or Burning Hell. It gets its own section, all to itself.
			double percentageOfEnemiesIgnitedByAV = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(75);
			double numGlyphidsHitByHeatBurst = 20;  // this.calculateNumGlyphidsInRadius(3);
			int numTimesAVcanTrigger = (int) Math.floor(numberOfBursts);
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
			
			fireDoTTotalDamage += numTimesAVcanTrigger * (percentageOfEnemiesIgnitedByAV * numGlyphidsHitByHeatBurst) * fireDoTDamagePerEnemy;
		}
		
		return totalDamage + fireDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// Because a ricochet from Bullet Hell consumes the penetration from Blowthrough Rounds, they don't stack together (unless BT Rounds gets buffed to do more than 1 penetration).
		if (selectedTier3 == 2 || selectedOverclock == 5) {
			return 2;
		}
		else {
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int ammoSpentBeforeOverheat = (int) (2.0 * calculateMaxNumPelletsFiredWithoutOverheating());
		int maxAmmo = getMaxAmmo();
		
		double numberOfBursts = ((double) maxAmmo) / ((double) ammoSpentBeforeOverheat);
		
		int numberOfCooldowns = 0;
		if (maxAmmo % ammoSpentBeforeOverheat == 0) {
			numberOfCooldowns = (int) (numberOfBursts - 1.0);
		}
		else {
			numberOfCooldowns = Math.floorDiv(maxAmmo, ammoSpentBeforeOverheat);
		}
		
		return (numberOfBursts * calculateFiringPeriod()) + (numberOfCooldowns * calculateCooldownPeriod());
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet());
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// I'm choosing to model Minigun as if it has no recoil. Although it does, its so negligible that it would have no effect.
		// Because it's being modeled without recoil, and its crosshair gets smaller as it fires, I'm making a quick-and-dirty estimate here instead of using AccuracyEstimator.
		double unchangingBaseSpread = 61;
		double changingBaseSpread = 68 * getBaseSpread();
		double spreadVariance = 334;
		double spreadPerShot = 16.7;
		// double spreadRecoverySpeed = 95.42857143;
		
		double baseSpread = unchangingBaseSpread + changingBaseSpread;
		double maxSpread = baseSpread + spreadVariance;
		
		// Adapted from AccuracyEstimator
		// Because this is modeled without recoil, there are only two options: one where the crosshair is larger than the target, and one where it's <=.
		double sumOfAllProbabilities = 0.0;
		double targetRadius;
		if (weakpointAccuracy) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		int numPelletsFired = (int) calculateMaxNumPelletsFiredWithoutOverheating();
		int numPelletsUntilStable = bulletsFiredTilMaxStability/2;
		double currentSpreadRadius;
		for (int i = 0; i < numPelletsUntilStable; i++) {
			currentSpreadRadius = AccuracyEstimator.convertSpreadPixelsToMeters(maxSpread - i*spreadPerShot, false);
			
			if (currentSpreadRadius > targetRadius) {
				sumOfAllProbabilities += Math.pow((targetRadius / currentSpreadRadius), 2);
			}
			else {
				sumOfAllProbabilities += 1.0;
			}
		}
		
		// Because only the first 20 shots have an accuracy penalty, the rest can be modeled with simple multiplication
		int numPelletsFiredAfterStable = numPelletsFired - numPelletsUntilStable;
		currentSpreadRadius = AccuracyEstimator.convertSpreadPixelsToMeters(baseSpread, false);
		if (currentSpreadRadius > targetRadius) {
			sumOfAllProbabilities += numPelletsFiredAfterStable * Math.pow((targetRadius / currentSpreadRadius), 2);
		}
		else {
			sumOfAllProbabilities += numPelletsFiredAfterStable;
		}
		
		return sumOfAllProbabilities / numPelletsFired * 100.0;
	}

	@Override
	public double utilityScore() {
		// OC "Lead Storm" reduces Gunner's movement speed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDamagePerPellet(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// Mod Tier 5 "Aggressive Venting" induces Fear in a 3m radius (while also dealing 75 Heat Damage)
		if (selectedTier5 == 0) {
			int numGlyphidsFeared = 20;  // this.calculateNumGlyphidsInRadius(3);
			utilityScores[4] = numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Innate stun = 30% chance, 1 sec duration (duration improved by Mod Tier 3 "Stun Duration")
		utilityScores[5] = getStunChancePerPellet() * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}