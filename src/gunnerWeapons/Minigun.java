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
	private double baseSpread;
	private double armorBreakChance;
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
		baseSpread = 1.0;
		armorBreakChance = 1.0;
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
		tier1[0] = new Mod("Magnetic Refrigeration", "Increases the cooling Rate", 1, 0);
		tier1[1] = new Mod("Improved Motor", "Increased rate of fire and faster gyro stabilization", 1, 1);
		tier1[2] = new Mod("Improved Platform Stability", "Increased Accuracy", 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Oversized Drum", "Expanded Ammo Bags", 2, 0);
		tier2[1] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Hardened Rounds", "Improved armor breaking", 3, 0);
		tier3[1] = new Mod("Stun Duration", "Stunned enemies are incapacitated for a longer period of time.", 3, 1);
		tier3[2] = new Mod("Blowthrough Rounds", "Shaped bullets capable of passing through a target!", 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Variable Chamber Pressure", "Damage increase when fully stabilized", 4, 0);
		tier4[1] = new Mod("Lighter Barrel Assembly", "Start killing things sooner with a shorter spinup time.", 4, 1);
		tier4[2] = new Mod("Magnetic Bearings", "Barrels keep spinning for a longer time after firing, keeping the gun stable for longer.", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Aggressive Venting", "Burn everything in a radius when the minigun overheats", 5, 0);
		tier5[1] = new Mod("Cold As The Grave", "Every kill cools the gun", 5, 1);
		tier5[2] = new Mod("Hot Bullets", "Rounds fired when the heat meter is red will burn the target", 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "A Little More Oomph!", "Get the most out of each shot without compromising any of the gun's systems.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Thinned Drum Walls", "Stuff more bullets into the ammo drum by thinning the material in non-critical areas.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Burning Hell", "Turn the area just infront of the minigun into an even worse place by venting all the combustion gasses forward. However, it does overheat rather quickly.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Compact Feed Mechanism", "More space left for ammo at the cost of a reduced rate of fire.", 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Exhaust Vectoring", "Increases damage at a cost to accuracy.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Bullet Hell", "Special bullets that ricochet off all surfaces and even enemies going on to hit nearby targets. However they deal less damage and are less accurate overall.", 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Lead Storm", "Pushing things to the limit this overclock greatly increases damage output but the kickback makes it almost impossible to move.", 6);
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
			toReturn += 5;
		}
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		if (selectedTier3 == 1) {
			toReturn += 1;
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
		double toReturn = baseSpread;
		if (selectedTier1 == 2) {
			toReturn -= 0.8;
		}
		if (selectedOverclock == 4) {
			toReturn *= 2.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 6.0;
		}
		return toReturn;
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		if (selectedTier3 == 0) {
			toReturn +=  2.0;
		}
		return toReturn;
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
				estimatedBurstDPS += 0.8 * DoTInformation.Burn_DPS;
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
		StatsRow[] toReturn = new StatsRow[20];
		
		boolean damageModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock > 3;
		toReturn[0] = new StatsRow("Damage per Pellet:", getDamagePerPellet(), damageModified);
		
		toReturn[1] = new StatsRow("Ammo Spent per Pellet:", 2, false);
		
		toReturn[2] = new StatsRow("Stun Chance per Pellet:", convertDoubleToPercentage(stunChancePerPellet), false);
		
		toReturn[3] = new StatsRow("Stun Duration:", getStunDuration(), selectedTier3 == 1);
		
		boolean ammoModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Max Ammo:", getMaxAmmo(), ammoModified);
		
		toReturn[5] = new StatsRow("Max Heat:", maxHeat, false);
		
		toReturn[6] = new StatsRow("Heat Per Second While Firing:", getHeatPerSecond(), selectedOverclock == 2);
		
		boolean pelletsPerBurstModified = selectedTier5 == 1 || selectedOverclock == 2 || selectedTier1 == 1 || selectedOverclock == 3;
		toReturn[7] = new StatsRow("Max Num Pellets Fired Per Burst:", calculateMaxNumPelletsFiredWithoutOverheating(), pelletsPerBurstModified);
		
		toReturn[8] = new StatsRow("Cooling Rate:", getCoolingRate(), selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[9] = new StatsRow("Max Cooldown Without Overheating:", calculateCooldownPeriod(), selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[10] = new StatsRow("Cooldown After Overheat:", cooldownAfterOverheat, false);
		
		toReturn[11] = new StatsRow("Rate of Fire (Ammo/Sec):", getRateOfFire(), selectedTier1 == 1 || selectedOverclock == 3);
		
		toReturn[12] = new StatsRow("Ammo Spent Until Stabilized:", bulletsFiredTilMaxStability, false);
		
		toReturn[13] = new StatsRow("Spinup Time:", getSpinupTime(), selectedTier4 == 1 || selectedOverclock == 0);
		
		toReturn[14] = new StatsRow("Spindown Time:", getSpindownTime(), selectedTier4 == 2);
		
		boolean baseSpreadModified = selectedTier1 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[15] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified);
		
		toReturn[16] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 6);
		
		toReturn[17] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier3 == 0);
		
		toReturn[18] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), selectedTier3 == 2, selectedTier3 == 2);
		
		toReturn[19] = new StatsRow("Max Ricochets:", getNumberOfRicochets(), selectedOverclock == 5, selectedOverclock == 5);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	private double calculateDamagePerBurst(boolean weakpointBonus) {
		/* 
			The length of the burst is determined by the heat accumulated. Each burst duration should stop just shy of 
			overheating the minigun so that it doesn't have the overheat cooldown penalty imposed.
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

	private double calculateIgnitionTime() {
		// It looks like Hot Bullets and Burning Hell both have -50% Burn DoT Durations?
		// Hot Bullets only
		if (selectedTier5 == 2 && selectedOverclock != 2) {
			// Hot Bullets adds 50% of of each pellet's Direct Damage as Heat Damage while the Heat Meter on the Minigun is red.
			// In practice, the meter turns red after 4 seconds of sustained firing, meaning that the last 5.5 seconds of the burst will have Hot Bullets.
			double heatPerPellet = ((double) getDamagePerPellet()) / 2.0;
			double RoF = getRateOfFire() / 2.0;
			return 4 + EnemyInformation.averageTimeToIgnite(heatPerPellet, RoF);
		}
		// Burning Hell only
		else if (selectedTier5 != 2 && selectedOverclock == 2) {
			// Burning Hell looks like it burns everything within 4m in a 20 degree arc in front of you at a rate of 30 heat/sec
			return EnemyInformation.averageTimeToIgnite(30);
		}
		// Both Hot Bullets AND Burning Hell
		else if (selectedTier5 == 2 && selectedOverclock == 2) {
			// Because Burning Hell reduces the Firing Period from 9.5 sec to 6.33 sec, this means that Hot Bullets gets activated after 2.66 seconds instead of 4.
			double heatPerPellet = ((double) getDamagePerPellet()) / 2.0;
			double RoF = getRateOfFire() / 2.0;
			double avgHeatPerSec = (2.66 * 30 + 3.66 * (heatPerPellet * RoF + 30)) / 6.33;
			return EnemyInformation.averageTimeToIgnite(avgHeatPerSec);
		}
		// Neither are equipped.
		else {
			return -1;
		}
	}
	
	@Override
	public double calculateIdealBurstDPS() {
		// damagePerBurst only accounts for damage dealt by pellets, not by any Burn DoTs applied.
		double damagePerBurst = calculateDamagePerBurst(false);
		double burstDuration = calculateFiringPeriod();
		double burstDPS = damagePerBurst / burstDuration;
		
		double fireDoTBurstDPS = 0;
		if (selectedTier5 == 2 || selectedOverclock == 2) {
			double ignitionTime = calculateIgnitionTime();
			double burnDoTUptime = (burstDuration - ignitionTime) / burstDuration;
			fireDoTBurstDPS = burnDoTUptime * DoTInformation.Burn_DPS;
		}
		
		return burstDPS + fireDoTBurstDPS;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		double damagePerBurst = calculateDamagePerBurst(false);
		double burstDuration = calculateFiringPeriod();
		double coolOffDuration = calculateCooldownPeriod();
		double sustainedDPS = damagePerBurst / (burstDuration + coolOffDuration);
		
		if (selectedTier5 == 2 || selectedOverclock == 2) {
			sustainedDPS += DoTInformation.Burn_DPS;
		}
		
		return sustainedDPS;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		double damagePerBurst = calculateDamagePerBurst(true);
		double burstDuration = calculateFiringPeriod();
		double coolOffDuration = calculateCooldownPeriod();
		double sustainedWeakpointDPS = damagePerBurst / (burstDuration + coolOffDuration);
		
		if (selectedTier5 == 2 || selectedOverclock == 2) {
			sustainedWeakpointDPS += DoTInformation.Burn_DPS;
		}
		
		return sustainedWeakpointDPS;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier3 == 2 || selectedOverclock == 5) {
			// This assumes that the penetrations and ricochets don't have their damage reduced.
			return calculateIdealSustainedDPS();
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
			timeBeforeFireProc = calculateIgnitionTime() - 4;
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeFireProc, 0.5 * EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
			
			// Because Hot Bullets only starts igniting enemies after 4 seconds, reduce this damage by the uptime coefficient.
			fireDoTDamagePerEnemy *= (5.5/9.5);
			
			estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
			fireDoTTotalDamage += fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		// Burning Hell, on the other hand, works great with this. Even with Hot Bullets stacked on top of it, it doesn't do negative damage.
		else if (selectedOverclock == 2) {
			timeBeforeFireProc = calculateIgnitionTime();
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeFireProc, 0.5 * EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
			
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
	public double estimatedAccuracy() {
		// I'm choosing to model Minigun as if it has no recoil. Although it does, its so negligible that it would have no effect.
		// Because it's being modeled without recoil, and its crosshair gets smaller as it fires, I'm making a quick-and-dirty estimate here instead of using AccuracyEstimator.
		
		// Baseline stats before mods/OCs alter them (measured as degrees of deviation from the central axis)
		double unchangingBaseSpread = 61.0/129.0;
		double changingBaseSpread = 68.0/129.0;
		
		double maxSpread = 14.54199762;
		double spreadPerShot = 0.5369524244;
		double minSpread = 4.116052903;
		double spreadVariance = maxSpread - minSpread;
		double modifiedMinSpread = unchangingBaseSpread * minSpread + changingBaseSpread * minSpread * getBaseSpread();
		double modifiedMaxSpread = modifiedMinSpread + spreadVariance;
		// double spreadRecoverySpeed = (maxSpread - minSpread) / ((double) getSpindownTime());
		
		// Borrowed from AccuracyEstimator
		// Because this is modeled without recoil, there are only two options: one where the crosshair is larger than the target, and one where it's <=.
		double sumOfAllProbabilities = 0.0;
		double targetRadius = AccuracyEstimator.targetRadius;
		int numPelletsFired = (int) calculateMaxNumPelletsFiredWithoutOverheating();
		int numPelletsUntilStable = bulletsFiredTilMaxStability/2;
		double currentSpreadRadius;
		for (int i = 0; i < numPelletsUntilStable; i++) {
			currentSpreadRadius = AccuracyEstimator.convertDegreesToMeters(modifiedMaxSpread - i*spreadPerShot);
			
			if (currentSpreadRadius > targetRadius) {
				sumOfAllProbabilities += Math.pow((targetRadius / currentSpreadRadius), 2);
			}
			else {
				sumOfAllProbabilities += 1.0;
			}
		}
		
		// Because only the first 20 shots have an accuracy penalty, the rest can be modeled with simple multiplication
		int numPelletsFiredAfterStable = numPelletsFired - numPelletsUntilStable;
		currentSpreadRadius = AccuracyEstimator.convertDegreesToMeters(modifiedMinSpread);
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
		
		// Armor Breaking
		utilityScores[2] = (getArmorBreakChance() - 1) * calculateMaxNumTargets() * UtilityInformation.ArmorBreak_Utility;
		
		// Mod Tier 5 "Aggressive Venting" induces Fear in a 3m radius (while also dealing 75 Heat Damage)
		if (selectedTier5 == 0) {
			int numGlyphidsFeared = 20;  // this.calculateNumGlyphidsInRadius(3);
			utilityScores[4] = numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Innate stun = 30% chance, 1 sec duration (duration improved by Mod Tier 3 "Stun Duration")
		utilityScores[5] = stunChancePerPellet * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}