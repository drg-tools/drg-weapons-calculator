package drillerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
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
		stickyFlamesDuration = 4.0;
		stickyFlamesSlow = 0.3;
		
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
		tier1[0] = new Mod("High Pressure Ejector", "+5m Flame Reach", modIcons.distance, 1, 0);
		tier1[1] = new Mod("More Fuel", "+75 Max Fuel", modIcons.carriedAmmo, 1, 1);
		tier1[2] = new Mod("Sticky Flame Duration", "+3 sec Sticky Flames duration", modIcons.hourglass, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Tanks", "+25 Tank Size", modIcons.magSize, 2, 0);
		tier2[1] = new Mod("Oversized Valves", "+0.9 Flow Rate", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Sticky Flame Slowdown", "+20% Sticky Flames Slow", modIcons.slowdown, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Unfiltered Fuel", "+5 Damage per Particle", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("More Fuel", "+75 Max Fuel", modIcons.carriedAmmo, 3, 1);
		tier3[2] = new Mod("Sticky Flame Duration", "+3 sec Sticky Flames duration", modIcons.hourglass, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("It Burns!", "Every ammo consumed deals 0.13 Fear Factor to all enemies hit by that particle", modIcons.fear, 4, 0);
		tier4[1] = new Mod("Triple Filtered Fuel", "+20 Heat per Particle", modIcons.heatDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Heat Radiance", "Deal 60 Fire Damage per second and 60 Heat per second to all enemies within 3m of you. The Heat/sec stacks with the direct stream and Sticky Flames' heat sources as well.", modIcons.heatDamage, 5, 0);
		tier5[1] = new Mod("Targets Explode", "If the direct stream kills an enemy, there's a 50% chance that they will explode and deal 55 Fire Damage and 55 Heat to all enemies within a 3m radius.", modIcons.addedExplosion, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Lighter Tanks", "+75 Max Fuel", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Sticky Additive", "+2 Damage per Particle, +3 sec Sticky Flame duration", overclockIcons.hourglass, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Fuel Stream Diffuser", "+5m Flame Reach, +1.5 Flow Rate, -75 Ammo", overclockIcons.distance, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Face Melter", "+5 Damage per Particle, -25 Tank Size, x0.3 Heat per Particle", overclockIcons.directDamage, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Sticky Flame Damage", "+5 Sticky Flame Damage/Tick, +4 sec Sticky Flame Duration, -20% Sticky Flame Slow, -25 Tank Size", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Sticky Flame Crowd Control", "+30% Sticky Flame Slow, +4 sec Sticky Flame Duration, -5 Sticky Flame Damage/Tick, -1.2 Flow Rate", overclockIcons.slowdown, 5);
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
				System.out.println("Flamethrower's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
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
			// Start by setting all mods/OC to -1 so that no matter what the old build was, the new build will go through with no problem.
			setSelectedModAtTier(1, -1, false);
			setSelectedModAtTier(2, -1, false);
			setSelectedModAtTier(3, -1, false);
			setSelectedModAtTier(4, -1, false);
			setSelectedModAtTier(5, -1, false);
			setSelectedOverclock(-1, false);
			
			switch (symbols[0]) {
				case 'A': {
					setSelectedModAtTier(1, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(1, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(1, 2, false);
					break;
				}
			}
			
			switch (symbols[1]) {
				case 'A': {
					setSelectedModAtTier(2, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(2, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(2, 2, false);
					break;
				}
			}
			
			switch (symbols[2]) {
				case 'A': {
					setSelectedModAtTier(3, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(3, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(3, 2, false);
					break;
				}
			}
			
			switch (symbols[3]) {
				case 'A': {
					setSelectedModAtTier(4, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(4, 1, false);
					break;
				}
			}
			
			switch (symbols[4]) {
				case 'A': {
					setSelectedModAtTier(5, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(5, 1, false);
					break;
				}
			}
			
			switch (symbols[5]) {
				case '1': {
					setSelectedOverclock(0, false);
					break;
				}
				case '2': {
					setSelectedOverclock(1, false);
					break;
				}
				case '3': {
					setSelectedOverclock(2, false);
					break;
				}
				case '4': {
					setSelectedOverclock(3, false);
					break;
				}
				case '5': {
					setSelectedOverclock(4, false);
					break;
				}
				case '6': {
					setSelectedOverclock(5, false);
					break;
				}
			}
			
			// Re-set AoE Efficiency
			setAoEEfficiency();
			
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
	public int getDwarfClassID() {
		return DatabaseConstants.drillerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.flamethrowerGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getParticleDamage() {
		double toReturn = particleDamage;
		
		if (selectedTier3 == 0) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 2;
		}
		else if (selectedOverclock == 3) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private double getParticleHeat() {
		double toReturn = particleHeat;
		
		if (selectedTier4 == 1) {
			toReturn += 20;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.3;
		}
		
		return toReturn;
	}
	private int getCarriedFuel() {
		int toReturn = carriedFuel;
		
		if (selectedTier1 == 1) {
			toReturn += 75;
		}
		if (selectedTier3 == 1) {
			toReturn += 75;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 75;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 75;
		}
		
		return toReturn;
	}
	private int getFuelTankSize() {
		int toReturn = fuelTankSize;
		
		if (selectedTier2 == 0) {
			toReturn += 25;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn -= 25;
		}
		
		return toReturn;
	}
	private double getFlowRate() {
		double toReturn = flowRate;
		
		if (selectedTier2 == 1) {
			toReturn += 0.9;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 1.2;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		return toReturn;
	}
	private double getFlameReach() {
		double toReturn = flameReach;
		
		if (selectedTier1 == 0) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private double getSFDamagePerTick() {
		double toReturn = stickyFlamesDamagePerTick;
		
		if (selectedOverclock == 4) {
			toReturn += 5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 5;
		}
		
		return toReturn;
	}
	private double getSFDuration() {
		double toReturn = stickyFlamesDuration;
		
		if (selectedTier1 == 2) {
			toReturn += 3;
		}
		if (selectedTier3 == 2) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 3;
		}
		else if (selectedOverclock == 4 || selectedOverclock == 5) {
			toReturn += 4;
		}
		
		return toReturn;
	}
	private double getSFSlow() {
		// From Elythnwaen: T3.B is actually a x0.5 multiplier applied to the baseline x0.9, so 0.9 * 0.5 = 0.45, which is a 55% slow, not 50%.
		double toReturn = stickyFlamesSlow;
		
		if (selectedTier2 == 2) {
			toReturn += 0.2;
		}
		
		if (selectedOverclock == 4) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 5) {
			toReturn += 0.3;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[14];
		
		// Stats about the direct stream's DPS
		boolean damageModified = selectedTier3 == 0 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[0] = new StatsRow("Damage per Particle:", getParticleDamage(), modIcons.directDamage, damageModified);
		
		toReturn[1] = new StatsRow("Heat per Particle:", getParticleHeat(), modIcons.heatDamage, selectedTier4 == 1 || selectedOverclock == 3);
		
		toReturn[2] = new StatsRow("Flame Reach:", getFlameReach(), modIcons.distance, selectedTier1 == 0 || selectedOverclock == 2);
		
		boolean tankSizeModified = selectedTier2 == 0 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Fuel Tank Size:", getFuelTankSize(), modIcons.magSize, tankSizeModified);
		
		boolean carriedFuelModified = selectedTier1 == 1 || selectedTier3 == 1 || selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[4] = new StatsRow("Max Fuel:", getCarriedFuel(), modIcons.carriedAmmo, carriedFuelModified);
		
		boolean flowRateModified = selectedTier2 == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Flow Rate:", getFlowRate(), modIcons.rateOfFire, flowRateModified);
		
		toReturn[6] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, false);
		
		toReturn[7] = new StatsRow("Fear Factor per Particle:", 0.13, modIcons.fear, selectedTier4 == 0, selectedTier4 == 0);
		
		// Burn DPS
		toReturn[8] = new StatsRow("Burn DoT DPS:", DoTInformation.Burn_DPS, modIcons.heatDamage, false);
		
		// Stats about the Sticky Flames
		toReturn[9] = new StatsRow("Sticky Flames Dmg per Tick:", getSFDamagePerTick(), modIcons.directDamage, selectedOverclock == 4 || selectedOverclock == 5);
		
		toReturn[10] = new StatsRow("Sticky Flames Heat per Tick:", stickyFlamesHeatPerTick, modIcons.heatDamage, false);
		
		toReturn[11] = new StatsRow("Sticky Flames Avg Ticks/Sec:", stickyFlamesTicksPerSec, modIcons.blank, false);
		
		boolean SFDurationModified = selectedTier1 == 2 || selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[12] = new StatsRow("Sticky Flames Duration:", getSFDuration(), modIcons.hourglass, SFDurationModified);
		
		boolean SFSlowModified = selectedTier2 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[13] = new StatsRow("Sticky Flames Slow:", convertDoubleToPercentage(getSFSlow()), modIcons.slowdown, SFSlowModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return selectedTier5 == 1;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// T5.B "Targets Explode" does 55 Fire Damage and Heat in a 3m radius, max damage radius 0.5m, and falls off to 25% minimum damage at 3m.
		if (selectedTier5 == 1) {
			aoeEfficiency = calculateAverageAreaDamage(3.0, 0.5, 0.25);
		}
		else {
			aoeEfficiency = new double[3];
		}
	}
	
	// Because the Flamethrower hits multiple targets with its stream, bypasses armor, and doesn't get weakpoint bonuses, this one method should be usable for all the DPS categories.
	private double calculateDPS(boolean burst, boolean primaryTarget) {
		double duration, burnDPS;
		
		double heatRadianceDmgAndHeatPerTick = 0;
		int numTicksHeatRadianceWillProc = 0; 
		if (selectedTier5 == 0) {
			// 60 Heat/sec in a 3m radius
			// I want this to be less effective with far-reaching streams to model how the further the steam flies the less likely it is that the enemies will be within the 3m.
			heatRadianceDmgAndHeatPerTick = 60.0 * 3.0 / getFlameReach();
			// Because Heat Radiance only procs after every full second of firing, I'm choosing to take the floor() of how many seconds a single magazine can be fired.
			numTicksHeatRadianceWillProc = (int) Math.floor(((double) getFuelTankSize()) / getFlowRate()); 
		}
		
		double stickyFlamesDPS = getSFDamagePerTick() * stickyFlamesTicksPerSec / 2.0;
		
		if (burst) {
			duration = ((double) getFuelTankSize()) / getFlowRate();
			
			double burnDoTUptimeCoefficient = (duration - averageTimeToCauterize()) / duration;
			burnDPS = burnDoTUptimeCoefficient * DoTInformation.Burn_DPS;
		}
		else {
			duration = (((double) getFuelTankSize()) / getFlowRate()) + getReloadTime();
			burnDPS = DoTInformation.Burn_DPS;
		}
		
		double directDamagePerParticle = getParticleDamage();
		
		double temperatureShock = 0;
		// Frozen
		if (primaryTarget && statusEffects[1]) {
			burnDPS = 0;
			temperatureShock = 200;
		}
		// IFG Grenade
		if (primaryTarget && statusEffects[3]) {
			directDamagePerParticle *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double heatRadianceDamagePerTank = heatRadianceDmgAndHeatPerTick * numTicksHeatRadianceWillProc;
		return (directDamagePerParticle * getFuelTankSize() + heatRadianceDamagePerTank + temperatureShock) / duration + stickyFlamesDPS + burnDPS;
	}

	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		return calculateDPS(burst, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		double sustainedDPS = calculateDPS(false, false);
		
		double targetsExplodeDPS = 0;
		if (selectedTier5 == 1) {
			// This only has a 50% chance to proc every kill.
			targetsExplodeDPS = 0.5 * 55.0 * aoeEfficiency[1] / averageTimeToKill();
		}
		
		return sustainedDPS + targetsExplodeDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double numTargets = calculateMaxNumTargets();
		double avgTTK = averageTimeToKill();
		double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / avgTTK);
		
		// Total Direct Damage
		double directTotalDamage = numTargets * getParticleDamage() * (getFuelTankSize() + getCarriedFuel());
		
		// Total Sticky Flames Damage
		double stickyFlamesDPS = getSFDamagePerTick() * stickyFlamesTicksPerSec / 2.0;
		double stickyFlamesDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, getSFDuration(), stickyFlamesDPS);
		double stickyFlamesTotalDamage = stickyFlamesDamagePerEnemy * estimatedNumEnemiesKilled;
		
		// Total Heat Radiance Damage
		double heatRadianceTotalDamage = 0;
		if (selectedTier5 == 0) {
			// 60 Fire + Heat/sec in a 3m radius
			double numTicksOfHeatRadiance = numMagazines(getCarriedFuel(), getFuelTankSize()) * (int) Math.floor(((double) getFuelTankSize()) / getFlowRate());
			// I'm choosing to model this as if the player is kiting enemies, keeping them about 1.5m away so that they don't receive melee attacks.
			int numGlyphidsHitByHeatRadiancePerTick = calculateNumGlyphidsInRadius(3.0) - calculateNumGlyphidsInRadius(1.5);
			heatRadianceTotalDamage = 60 * numTicksOfHeatRadiance * numGlyphidsHitByHeatRadiancePerTick;
		}
		
		// Total Targets Explode Damage
		double targetsExplodeDamage = 0;
		if (selectedTier5 == 1) {
			// 50% chance to do 55 Fire Damage + Heat in a 3m radius.
			double averageDamagePerTargetExplosion = 55.0 * aoeEfficiency[1] * aoeEfficiency[2];
			// I'm arbitrarily multiplying the number of targets hit by flame stream by 0.5 (a second time, beyond Targets Explode's 50% chance to proc) to represent 
			// that the stream doesn't get 100% of the kills; the Burn DoT and Sticky Flames get a lot of killing blows too.
			double expectedNumberOfTargetExplosions = 0.5 * (0.5 * numTargets) * (numMagazines(getCarriedFuel(), getFuelTankSize()) * timeToFireMagazine() / avgTTK);
			targetsExplodeDamage = averageDamagePerTargetExplosion * expectedNumberOfTargetExplosions;
		}
		
		// Total Burn Damage
		double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(averageTimeToCauterize(), DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
		double fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		
		return directTotalDamage + stickyFlamesTotalDamage + heatRadianceTotalDamage + targetsExplodeDamage + fireDoTTotalDamage;
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
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = getParticleDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getParticleDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// This stat is only applicable to "gun"-type weapons
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		// Flamethrower particles don't need to be counted for Breakpoint calculations
		return 0;
	}

	@Override
	public double utilityScore() {
		double numTargets = calculateMaxNumTargets();
		
		// Armor Break -- Flamethrower ignores Armor of all kinds, so this will always be zero.
		
		// Slow
		// I'm guessing that only half on the enemies hit by the primary stream will also be hit by the sticky flames
		double numTargetsHitBySF = Math.round(numTargets * 0.5);
		utilityScores[3] = numTargetsHitBySF * getSFDuration() * getSFSlow(); 
		
		// Fear
		if (selectedTier4 == 0) {
			double probabilityToFear = calculateFearProcProbability(0.13);
			double fearDuration = EnemyInformation.averageFearDuration(getSFSlow(), getSFDuration());
			utilityScores[4] = probabilityToFear * numTargets * fearDuration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		double stickyFlamesHeatPerSec = stickyFlamesHeatPerTick * stickyFlamesTicksPerSec / 2.0;
		
		double heatRadianceDmgAndHeatPerTick = 0;
		if (selectedTier5 == 0) {
			// 60 Heat/sec in a 3m radius
			// I want this to be less effective with far-reaching streams to model how the further the steam flies the less likely it is that the enemies will be within the 3m.
			heatRadianceDmgAndHeatPerTick = 60.0 * 3.0 / getFlameReach();
		}
		
		return EnemyInformation.averageTimeToIgnite(0, getParticleHeat(), getFlowRate(), stickyFlamesHeatPerSec + heatRadianceDmgAndHeatPerTick);
	}
	
	@Override
	public double damagePerMagazine() {
		double numTargets = calculateMaxNumTargets();
		
		// Total Direct Damage
		double directTotalDamage = numTargets * getParticleDamage() * getFuelTankSize();
		
		// Total Burn Damage
		double timeToIgnite = EnemyInformation.averageTimeToIgnite(0, getParticleHeat(), getFlowRate(), 0);
		double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
		double fireDoTTotalDamage = fireDoTDamagePerEnemy * numTargets;
		
		return directTotalDamage + fireDoTTotalDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getFuelTankSize() / getFlowRate();
	}
	
	@Override
	public double damageWastedByArmor() {
		// Flamethrower's stream ignores all armor.
		return 0;
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[0].getText(true), "{ \"clip\": { \"name\": \"Tank Size\", \"value\": 25 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[1].getText(true), "{ \"ex6\": { \"name\": \"Flame Reach\", \"value\": 5 } }", "Icon_Upgrade_Distance", "Reach"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 5 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 0, 0, 0, 15, 24, tier2[1].getText(true), "{ \"ex11\": { \"name\": \"Heat\", \"value\": 10 } }", "Icon_Upgrade_Heat", "Heat"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 2000, 24, 15, 0, 0, 0, 0, tier2[2].getText(true), "{ \"ex4\": { \"name\": \"Sticky Flame Duration\", \"value\": 3 } }", "Icon_Upgrade_Duration", "Delay"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 35, 50, 0, 0, 0, tier3[0].getText(true), "{ \"rate\": { \"name\": \"Fuel Flow Rate\", \"value\": 30, \"percent\": true } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 0, 0, 35, 0, 0, 50, tier3[1].getText(true), "{ \"ex3\": { \"name\": \"Sticky Flame Slowdown\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Sticky", "Slowdown"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2800, 0, 0, 50, 35, 0, 0, tier3[2].getText(true), "{ \"ammo\": { \"name\": \"Max Fuel\", \"value\": 75 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 50, 48, 72, 0, 0, 0, tier4[0].getText(true), "{ \"ex5\": { \"name\": \"Fear Factor\", \"value\": 13, \"percent\": true } }", "Icon_Upgrade_ScareEnemies", "Fear"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 50, 0, 48, 0, 72, 0, tier4[1].getText(true), "{ \"ex4\": { \"name\": \"Sticky Flame Duration\", \"value\": 3 } }", "Icon_Upgrade_Duration", "Delay"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 4800, 0, 72, 0, 48, 50, 0, tier4[2].getText(true), "{ \"ammo\": { \"name\": \"Max Fuel\", \"value\": 75 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 64, 70, 0, 140, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Area Heat\", \"value\": 10 } }", "Icon_Upgrade_Heat", "Heat"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 0, 0, 0, 64, 70, 140, tier5[1].getText(true), "{ \"ex9\": { \"name\": \"Killed Targets Explode %\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Explosion", "Explosion"),
				exportAllMods || false);
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL(boolean exportAllOCs) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7500, 0, 125, 90, 75, 0, 0, overclocks[0].getText(true), "{ \"ammo\": { \"name\": \"Max Fuel\", \"value\": 75 } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8250, 100, 80, 0, 0, 0, 130, overclocks[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 }, "
				+ "\"ex4\": { \"name\": \"Sticky Flame Duration\", \"value\": 1 } }", "Icon_Upgrade_Duration"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7450, 0, 70, 130, 0, 0, 90, overclocks[2].getText(true), "{ \"clip\": { \"name\": \"Tank Size\", \"value\": 25 }, "
				+ "\"ex6\": { \"name\": \"Flame Reach\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7100, 0, 100, 0, 0, 80, 125, overclocks[3].getText(true), "{ \"ex6\": { \"name\": \"Flame Reach\", \"value\": 5 }, "
				+ "\"rate\": { \"name\": \"Fuel Flow Rate\", \"value\": 20, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Distance"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7000, 90, 0, 0, 130, 70, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 2 }, "
				+ "\"rate\": { \"name\": \"Fuel Flow Rate\", \"value\": 30, \"percent\": true }, \"ammo\": { \"name\": \"Max Fuel\", \"value\": 75, \"subtract\": true }, "
				+ "\"ex10\": { \"name\": \"Movement Speed While Using\", \"value\": 50, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8800, 75, 0, 0, 0, 110, 140, overclocks[5].getText(true), "{ \"ex1\": { \"name\": \"Increased Sticky Flame Damage\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex4\": { \"name\": \"Sticky Flame Duration\", \"value\": 6 }, \"clip\": { \"name\": \"Tank Size\", \"value\": 25, \"subtract\": true }, \"ammo\": { \"name\": \"Max Fuel\", \"value\": 75, \"subtract\": true } }", "Icon_Upgrade_Duration"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
