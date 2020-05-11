package drillerWeapons;

import java.util.Arrays;
import java.util.List;

import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Flamethrower extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double particleDamage;
	private double particleHeat;
	private int carriedFuel;
	private int fuelTankSize;
	private double flowRate;
	private double reloadTime;
	private double flameReach;
	
	private double stickyFlamesDamagePerTick;
	private double stickyFlamesHeatPerTick;
	private double stickyFlamesTicksPerSec;
	private double stickyFlamesDuration;
	private double stickyFlamesSlow;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Flamethrower() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Flamethrower(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Flamethrower(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "CRSPR Flamethrower";
		weaponPic = WeaponPictures.flamethrower;
		
		// Base stats, before mods or overclocks alter them:
		particleDamage = 10;
		particleHeat = 10;
		carriedFuel = 300;
		fuelTankSize = 50;
		flowRate = 6.0;
		reloadTime = 3.0;
		flameReach = 10;
		
		stickyFlamesDamagePerTick = 15;
		stickyFlamesHeatPerTick = 5;
		stickyFlamesTicksPerSec = 2.0 / (0.25 + 0.75);
		stickyFlamesDuration = 2.0;
		stickyFlamesSlow = 0.1;
		
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
		tier1[0] = new Mod("High Capacity Tanks", "+25 Tank Size", modIcons.magSize, 1, 0);
		tier1[1] = new Mod("High Pressure Ejector", "+5m Flame Reach", modIcons.distance, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Unfiltered Fuel", "+5 Damage per Particle", modIcons.directDamage, 2, 0);
		tier2[1] = new Mod("Triple Filtered Fuel", "+10 Heat per Particle", modIcons.heatDamage, 2, 1);
		tier2[2] = new Mod("Sticky Flame Duration", "+3 sec Sticky Flames duration", modIcons.duration, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Oversized Valves", "+1.8 Flow Rate", modIcons.rateOfFire, 3, 0);
		tier3[1] = new Mod("Sticky Flame Slowdown", "Increases Sticky Flames' slow from 10% to 50%", modIcons.slowdown, 3, 1);
		tier3[2] = new Mod("More Fuel", "+75 Max Fuel", modIcons.carriedAmmo, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("It Burns!", "Every second that the direct stream is applied to an enemy, there's a 13% chance that it will inflict Fear", modIcons.fear, 4, 0);
		tier4[1] = new Mod("Sticky Flame Duration", "+3 sec Sticky Flames duration", modIcons.duration, 4, 1);
		tier4[2] = new Mod("More Fuel", "+75 Max Fuel", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Heat Radiance", "Heat up enemies within 5m of you at a rate of ??? Heat/sec. This stacks with the direct stream and Sticky Flames' heat sources as well.", modIcons.heatDamage, 5, 0, false);
		tier5[1] = new Mod("Targets Explode", "If the direct stream kills an enemy, there's a 50% chance that they will explode and deal 55 Fire Damage and 55 Heat Damage to all enemies within a 3m radius.", modIcons.addedExplosion, 5, 1, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Lighter Tanks", "+75 Max Fuel", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Sticky Additive", "+1 Damage per Particle, +1 sec Sticky Flame duration", overclockIcons.duration, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Compact Feed Valves", "+25 Fuel Tank Size, -2m Flame Reach", overclockIcons.magSize, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Fuel Stream Diffuser", "+5m Flame Reach, -1.2 Flow Rate", overclockIcons.distance, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Face Melter", "+2 Damage per Particle, +1.8 Flow Rate, -75 Max Fuel, x0.5 Movement Speed while using", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Sticky Fuel", "+5 Sticky Flames damage, +6 sec Sticky Flames duration, -25 Tank Size, -75 Max Fuel", overclockIcons.duration, 5);
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
				System.out.println("Flamethrower's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Flamethrower's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
	public Flamethrower clone() {
		return new Flamethrower(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "Flamethrower";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getParticleDamage() {
		double toReturn = particleDamage;
		
		if (selectedTier2 == 0) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn += 2;
		}
		
		return toReturn;
	}
	private double getParticleHeat() {
		double toReturn = particleHeat;
		
		if (selectedTier2 == 1) {
			toReturn += 10;
		}
		
		return toReturn;
	}
	private int getCarriedFuel() {
		int toReturn = carriedFuel;
		
		if (selectedTier3 == 2) {
			toReturn += 75;
		}
		if (selectedTier4 == 2) {
			toReturn += 75;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 75;
		}
		else if (selectedOverclock == 4 || selectedOverclock == 5) {
			toReturn -= 75;
		}
		
		return toReturn;
	}
	private int getFuelTankSize() {
		int toReturn = fuelTankSize;
		
		if (selectedTier1 == 0) {
			toReturn += 25;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 25;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 25;
		}
		
		return toReturn;
	}
	private double getFlowRate() {
		double toReturn = flowRate;
		
		if (selectedTier3 == 0) {
			toReturn += 1.8;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 1.2;
		}
		else if (selectedOverclock == 4) {
			toReturn += 1.8;
		}
		
		return toReturn;
	}
	private double getFlameReach() {
		double toReturn = flameReach;
		
		if (selectedTier1 == 1) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 2) {
			toReturn -= 2;
		}
		else if (selectedOverclock == 3) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private double getSFDamagePerTick() {
		double toReturn = stickyFlamesDamagePerTick;
		
		if (selectedOverclock == 5) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private double getSFDuration() {
		double toReturn = stickyFlamesDuration;
		
		if (selectedTier2 == 2) {
			toReturn += 3;
		}
		if (selectedTier4 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 1;
		}
		else if (selectedOverclock == 5) {
			toReturn += 6;
		}
		
		return toReturn;
	}
	private double getSFSlow() {
		double toReturn = stickyFlamesSlow;
		
		if (selectedTier3 == 1) {
			toReturn += 0.4;
		}
		
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = 1.0;
		if (selectedOverclock == 4) {
			modifier -= 0.5;
		}
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		// Stats about the direct stream's DPS
		boolean damageModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Damage per Particle:", getParticleDamage(), damageModified);
		
		toReturn[1] = new StatsRow("Heat per Particle:", getParticleHeat(), selectedTier2 == 1);
		
		boolean reachModified = selectedTier1 == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[2] = new StatsRow("Flame Reach:", getFlameReach(), reachModified);
		
		boolean tankSizeModified = selectedTier1 == 0 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[3] = new StatsRow("Fuel Tank Size:", getFuelTankSize(), tankSizeModified);
		
		boolean carriedFuelModified = selectedTier3 == 2 || selectedTier4 == 2 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Max Fuel:", getCarriedFuel(), carriedFuelModified);
		
		boolean flowRateModified = selectedTier3 == 0 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[5] = new StatsRow("Flow Rate:", getFlowRate(), flowRateModified);
		
		toReturn[6] = new StatsRow("Reload Time:", reloadTime, false);
		
		toReturn[7] = new StatsRow("Fear Chance per Second:", convertDoubleToPercentage(0.13), selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[8] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 4, selectedOverclock == 4);
		
		// Burn DPS
		toReturn[9] = new StatsRow("Burn DoT DPS:", DoTInformation.Burn_DPS, false);
		
		// Stats about the Sticky Flames
		toReturn[10] = new StatsRow("Sticky Flames Dmg per Tick:", getSFDamagePerTick(), selectedOverclock == 5);
		
		toReturn[11] = new StatsRow("Sticky Flames Heat per Tick:", stickyFlamesHeatPerTick, false);
		
		toReturn[12] = new StatsRow("Sticky Flames Avg Ticks/Sec:", stickyFlamesTicksPerSec, false);
		
		boolean SFDurationModified = selectedTier2 == 2 || selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[13] = new StatsRow("Sticky Flames Duration:", getSFDuration(), SFDurationModified);
		
		toReturn[14] = new StatsRow("Sticky Flames Slow:", convertDoubleToPercentage(getSFSlow()), selectedTier3 == 1);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	// Because the Flamethrower hits multiple targets with its stream, bypasses armor, and doesn't get weakpoint bonuses, this one method should be usable for all the DPS categories.
	private double calculateDPS(boolean burst, boolean primaryTarget) {
		double duration, burnDPS;
		
		double directHeatPerSec = getParticleHeat() * getFlowRate();
		
		double stickyFlamesDPS = getSFDamagePerTick() * stickyFlamesTicksPerSec;
		double stickyFlamesHeatPerSec = stickyFlamesHeatPerTick * stickyFlamesTicksPerSec;
		
		if (burst) {
			duration = ((double) getFuelTankSize()) / getFlowRate();
			
			double totalHeatPerSec = directHeatPerSec + stickyFlamesHeatPerSec;
			double timeToIgnite = EnemyInformation.averageTimeToIgnite(totalHeatPerSec);
			double burnDoTUptimeCoefficient = (duration - timeToIgnite) / duration;
			burnDPS = burnDoTUptimeCoefficient * DoTInformation.Burn_DPS;
		}
		else {
			duration = (((double) getFuelTankSize()) / getFlowRate()) + reloadTime;
			burnDPS = DoTInformation.Burn_DPS;
		}
		
		double directDamagePerParticle = getParticleDamage();
		
		// Frozen
		if (primaryTarget && statusEffects[1]) {
			burnDPS = 0;
		}
		// IFG Grenade
		if (primaryTarget && statusEffects[3]) {
			directDamagePerParticle *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		return directDamagePerParticle * getFuelTankSize() / duration + stickyFlamesDPS + burnDPS;
	}

	// Single-target calculations
	@Override
	public double calculateIdealBurstDPS() {
		return calculateDPS(true, true);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateDPS(false, true);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateDPS(false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateDPS(false, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		return calculateDPS(false, false);
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double numTargets = calculateMaxNumTargets();
		double avgTTK = averageTimeToKill();
		double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / avgTTK);
		
		// Total Direct Damage
		double directTotalDamage = numTargets * getParticleDamage() * (getFuelTankSize() + getCarriedFuel());
		
		// Total Sticky Flames Damage
		double stickyFlamesDPS = getSFDamagePerTick() * stickyFlamesTicksPerSec;
		double stickyFlamesDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, getSFDuration(), stickyFlamesDPS);
		double stickyFlamesTotalDamage = stickyFlamesDamagePerEnemy * estimatedNumEnemiesKilled;
		
		// Total Burn Damage
		double directHeatPerSec = getParticleHeat() * getFlowRate();
		double stickyFlamesHeatPerSec = stickyFlamesHeatPerTick * stickyFlamesTicksPerSec;
		double timeToIgnite = EnemyInformation.averageTimeToIgnite(directHeatPerSec + stickyFlamesHeatPerSec);
		double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
		double fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		
		return directTotalDamage + stickyFlamesTotalDamage + fireDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return calculateNumGlyphidsInStream(getFlameReach());
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getFuelTankSize();
		int carriedAmmo = getCarriedFuel();
		double timeToFireMagazine = ((double) magSize) / getFlowRate();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * reloadTime;
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = getParticleDamage();
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// This stat is only applicable to "gun"-type weapons
		return -1.0;
	}

	@Override
	public double utilityScore() {
		double numTargets = calculateMaxNumTargets();
		
		// Mobility
		utilityScores[0] = (getMovespeedWhileFiring() - DwarfInformation.walkSpeed) * UtilityInformation.Movespeed_Utility;
		
		// Armor Break -- Flamethrower ignores Armor of all kinds, so this will always be zero.
		
		// Slow
		// I'm guessing that only half on the enemies hit by the primary stream will also be hit by the sticky flames
		double numTargetsHitBySF = Math.round(numTargets * 0.5);
		utilityScores[3] = numTargetsHitBySF * getSFDuration() * getSFSlow(); 
		
		// Fear
		if (selectedTier4 == 0) {
			utilityScores[4] = 0.13 * numTargets * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
}
