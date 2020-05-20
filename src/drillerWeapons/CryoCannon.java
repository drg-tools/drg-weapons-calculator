package drillerWeapons;

import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.MathUtils;

public class CryoCannon extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double particleDamage;
	private double particleCold;
	private int tankSize;
	private double chargeupTime;
	private double pressureDropDuration;
	private double flowRate;
	private double repressurizationDelay;
	private double pressureGainDuration;
	private double coldStreamReach;
	
	private double icePathColdPerTick;
	private double icePathTicksPerSec;
	private double icePathDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public CryoCannon() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public CryoCannon(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public CryoCannon(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Cryo Cannon";
		weaponPic = WeaponPictures.cryoCannon;
		
		// Base stats, before mods or overclocks alter them:
		particleDamage = 6;
		particleCold = -8;
		tankSize = 400;
		chargeupTime = 0.5;
		pressureDropDuration = 6.5;
		flowRate = 8.0;
		repressurizationDelay = 1;
		pressureGainDuration = 5;
		coldStreamReach = 10;
		
		icePathColdPerTick = -8;
		icePathTicksPerSec = 1.0 / 0.5;
		icePathDuration = 3;
		
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
		tier1[0] = new Mod("Larger Pressure Chamber", "x0.5 Pressure Drop Rate", modIcons.magSize, 1, 0);
		tier1[1] = new Mod("Improved 2nd Stage Pump", "-0.4 sec Chargeup Time", modIcons.chargeSpeed, 1, 1);
		tier1[2] = new Mod("Stronger Cooling Unit", "+1 Cold per Particle", modIcons.coldDamage, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Pressure Reserve Tank", "+50 Tank Size", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Overclocked Ejection Turbine", "+5m Cold Stream Reach", modIcons.distance, 2, 1);
		tier2[2] = new Mod("Bypassed Integrity Check", "-1 sec Repressurization Delay", modIcons.coolingRate, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Improved Pump", "x1.7 Pressure Gain Rate", modIcons.chargeSpeed, 3, 0);
		tier3[1] = new Mod("Increased Flow Volume", "+20% Flow Rate", modIcons.rateOfFire, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("High Water Content", "+3 Damage per Particle", modIcons.directDamage, 4, 0);
		tier4[1] = new Mod("Improved Mixture", "+1 Cold per Particle", modIcons.coldDamage, 4, 1);
		tier4[2] = new Mod("Larger Reserve Tank", "+150 Tank Size", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Fragile", "Every particle that hits a Frozen enemy has a chance to deal a large chunk of damage", modIcons.addedExplosion, 5, 0, false);
		tier5[1] = new Mod("Cold Radiance", "Cool down enemies within 5m of you at a rate of ??? Cold/sec. This stacks with the direct stream and Ice Path's cold sources as well.", modIcons.coldDamage, 5, 1, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Improved Thermal Efficiency", "+25 Tank Size, x0.75 Pressure Drop Rate", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Perfectly Tuned Cooler", "+1 Cold per Particle, +10% Flow Rate", overclockIcons.coldDamage, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Flow Rate Expansion", "x2.7 Pressure Gain Rate, +10% Flow Rate, x2.25 Pressure Drop Rate", overclockIcons.duration, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Ice Spear", "Press the Reload button to consume 35 ammo and fire an Ice Spear that does 250 Direct Damage and 125 Area Damage in a 2.5m radius. "
				+ "In exchange, +1 sec Repressurization Delay", overclockIcons.projectileVelocity, 3, false);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Ice Storm", "x2 Damage per Particle, -3 Cold per Particle, -50 Tank Size, x1.5 Pressure Drop Rate", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Snowball", "Press the Reload button to consume 50 ammo and fire a Snowball that does 100 Cold Damage in a 3.5m radius, which will freeze most enemies instantly. "
				+ "In exchange, -100 Tank Size, +1 sec Repressurization Delay", overclockIcons.aoeRadius, 5);
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
			if (symbols[2] == 'C') {
				System.out.println("CryoCannon's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("CryoCannon's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
	public CryoCannon clone() {
		return new CryoCannon(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "CryoCannon";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.drillerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.cryCannonGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getParticleDamage() {
		double toReturn = particleDamage;
		
		if (selectedTier4 == 0) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 2;
		}
		
		return toReturn;
	}
	// Because Cold Damage has negative values, adding Cold Damage is the same as subtracting from the current value
	private double getParticleCold() {
		double toReturn = particleCold;
		
		if (selectedTier1 == 2) {
			toReturn -= 1;
		}
		if (selectedTier4 == 1) {
			toReturn -= 1;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 4) {
			toReturn += 3;
		}
		
		return toReturn;
	}
	private int getTankSize() {
		int toReturn = tankSize;
		
		if (selectedTier2 == 0) {
			toReturn += 50;
		}
		if (selectedTier4 == 2) {
			toReturn += 150;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 25;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 50;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 100;
		}
		
		return toReturn;
	}
	private double getChargeupTime() {
		double toReturn = chargeupTime;
		
		if (selectedTier1 == 1) {
			toReturn -= 0.4;
		}
		
		return toReturn;
	}
	private double getPressureDropModifier() {
		double modifier = 1.0;
		
		if (selectedTier1 == 0) {
			modifier *= 0.5;
		}
		
		if (selectedOverclock == 0) {
			modifier *= 0.75;
		}
		else if (selectedOverclock == 2) {
			modifier *= 2.25;
		}
		else if (selectedOverclock == 4) {
			modifier *= 1.5;
		}
		
		return modifier;
	}
	private double getFlowRate() {
		double modifier = 1.0;
		
		if (selectedTier3 == 1) {
			modifier += 0.2;
		}
		
		if (selectedOverclock == 1 || selectedOverclock == 2) {
			modifier += 0.1;
		}
		
		return flowRate * modifier;
	}
	private double getRepressurizationDelay() {
		double toReturn = repressurizationDelay;
		
		if (selectedTier2 == 2) {
			toReturn -= 1;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 5) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private double getPressureGainModifier() {
		double modifier = 1.0;
		
		if (selectedTier3 == 0) {
			modifier *= 1.7;
		}
		
		if (selectedOverclock == 2) {
			modifier *= 2.7;
		}
		
		return modifier;
	}
	private double getColdStreamReach() {
		double toReturn = coldStreamReach;
		
		if (selectedTier2 == 1) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[16];
		
		// Stats about the direct stream's DPS
		toReturn[0] = new StatsRow("Damage per Particle:", getParticleDamage(), selectedTier4 == 0 || selectedOverclock == 4);
		
		boolean coldModified = selectedTier1 == 2 || selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 4;
		// Again, choosing to display Cold as positive even though it's a negative value.
		toReturn[1] = new StatsRow("Cold per Particle:", -1 * getParticleCold(), coldModified);
		
		boolean freezingTimeModified = false;
		toReturn[2] = new StatsRow("Avg Time to Freeze:", averageTimeToFreeze(false), freezingTimeModified);
		toReturn[3] = new StatsRow("Avg Freeze Multiplier:", averageFreezeMultiplier(), freezingTimeModified);
		
		toReturn[4] = new StatsRow("Cold Stream Reach:", getColdStreamReach(), selectedTier2 == 1);
		
		boolean tankSizeModified = selectedTier2 == 0 || selectedTier4 == 2 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Tank Size:", getTankSize(), tankSizeModified);
		
		toReturn[6] = new StatsRow("Chargeup Time:", getChargeupTime(), selectedTier1 == 1);
		
		boolean pressureDropModified = selectedTier1 == 0 || selectedOverclock % 2 == 0 ;
		toReturn[7] = new StatsRow("Pressure Drop Rate:", convertDoubleToPercentage(getPressureDropModifier()), pressureDropModified);
		toReturn[8] = new StatsRow("Pressure Drop Duration:", pressureDropDuration / getPressureDropModifier(), pressureDropModified);
		
		boolean flowRateModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[9] = new StatsRow("Flow Rate:", getFlowRate(), flowRateModified);
		
		boolean delayModified = selectedTier2 == 2 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[10] = new StatsRow("Repressurization Delay:", getRepressurizationDelay(), delayModified);
		
		boolean pressureGainModified = selectedTier3 == 0 || selectedOverclock == 2;
		toReturn[11] = new StatsRow("Pressure Gain Rate:", convertDoubleToPercentage(getPressureGainModifier()), pressureGainModified);
		toReturn[12] = new StatsRow("Pressure Gain Duration:", pressureGainDuration / getPressureGainModifier(), pressureGainModified);
		
		// Stats about the Ice Path
		// I'm choosing to display this as a positive number, even though internally it's negative.
		toReturn[13] = new StatsRow("Ice Path Cold per Tick:", -1 * icePathColdPerTick, false);
		
		toReturn[14] = new StatsRow("Ice Path Ticks per Sec:", icePathTicksPerSec, false);
		
		toReturn[15] = new StatsRow("Ice Path Duration", icePathDuration, false);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	private double averageTimeToFreeze(boolean refreeze) {
		double streamColdPerSec = getParticleCold() * getFlowRate();
		double icePathColdPerSec = icePathColdPerTick * icePathTicksPerSec / 2.0;
		double totalColdPerSec = streamColdPerSec + icePathColdPerSec;
		
		if (refreeze) {
			return EnemyInformation.averageTimeToRefreeze(totalColdPerSec);
		}
		else {
			return EnemyInformation.averageTimeToFreeze(totalColdPerSec);
		}
	}
	
	private double totalDamageDealtPerBurst(boolean primaryTarget) {
		double dmgPerParticle = getParticleDamage();
		double firingTime = pressureDropDuration / getPressureDropModifier();
		double flowRate = getFlowRate();
		
		double timeToFreeze = averageTimeToFreeze(false);
		double freezeDuration = EnemyInformation.averageFreezeDuration();
		double timeToRefreeze = averageTimeToFreeze(true);
		
		// Status Effects
		double frozenMultiplier;
		if (primaryTarget && statusEffects[0]) {
			// Burning cancels Frozen
			frozenMultiplier = 1.0;
		}
		else if (primaryTarget && statusEffects[1]) {
			// Already Frozen
			dmgPerParticle *= UtilityInformation.Frozen_Damage_Multiplier;
			// Don't multiply by 3 two times!
			frozenMultiplier = 1.0;
		}
		else {
			frozenMultiplier = UtilityInformation.Frozen_Damage_Multiplier;
		}
		
		if (primaryTarget && statusEffects[3]) {
			dmgPerParticle *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		if (firingTime <= timeToFreeze) {
			return firingTime * flowRate * dmgPerParticle;
		}
		
		double totalDamage = 0;
		double particlesFired = Math.round(timeToFreeze * flowRate);
		totalDamage += particlesFired * dmgPerParticle;
		firingTime -= timeToFreeze;
		boolean currentlyFrozen = true;
		while (firingTime > 0) {
			if (currentlyFrozen) {
				if (firingTime > freezeDuration) {
					particlesFired = Math.round(freezeDuration * flowRate);
					totalDamage += particlesFired * dmgPerParticle * frozenMultiplier;
					firingTime -= freezeDuration;
					currentlyFrozen = false;
				}
				else {
					totalDamage += firingTime * flowRate * dmgPerParticle * frozenMultiplier;
					firingTime = 0;
				}
			}
			else {
				if (firingTime > timeToRefreeze) {
					particlesFired = Math.round(timeToRefreeze * flowRate);
					totalDamage += particlesFired * dmgPerParticle;
					firingTime -= timeToRefreeze;
					currentlyFrozen = true;
				}
				else {
					totalDamage += firingTime * flowRate * dmgPerParticle;
					firingTime = 0;
				}
			}
		}
		
		return totalDamage;
	}
	
	private double averageFreezeMultiplier() {
		double firingTime = pressureDropDuration / getPressureDropModifier();
		double flowRate = getFlowRate();
		
		double timeToFreeze = averageTimeToFreeze(false);
		double freezeDuration = EnemyInformation.averageFreezeDuration();
		double timeToRefreeze = averageTimeToFreeze(true);
		
		if (firingTime <= timeToFreeze) {
			return 1.0;
		}
		
		double totalDamage = 0;
		double totalParticles = 0;
		double particlesFired = Math.round(timeToFreeze * flowRate);
		totalDamage += particlesFired;
		totalParticles += particlesFired;
		firingTime -= timeToFreeze;
		boolean currentlyFrozen = true;
		while (firingTime > 0) {
			if (currentlyFrozen) {
				if (firingTime > freezeDuration) {
					particlesFired = Math.round(freezeDuration * flowRate);
					totalDamage += particlesFired * UtilityInformation.Frozen_Damage_Multiplier;
					totalParticles += particlesFired;
					firingTime -= freezeDuration;
					currentlyFrozen = false;
				}
				else {
					totalDamage += firingTime * flowRate * UtilityInformation.Frozen_Damage_Multiplier;
					totalParticles += firingTime * flowRate;
					firingTime = 0;
				}
			}
			else {
				if (firingTime > timeToRefreeze) {
					particlesFired = Math.round(timeToRefreeze * flowRate);
					totalDamage += particlesFired;
					totalParticles += particlesFired;
					firingTime -= timeToRefreeze;
					currentlyFrozen = true;
				}
				else {
					totalDamage += firingTime * flowRate;
					totalParticles += firingTime * flowRate;
					firingTime = 0;
				}
			}
		}
		
		return totalDamage / totalParticles;
	}
	
	// Because the Cryo Cannon hits multiple targets with its stream, bypasses armor, and doesn't get weakpoint bonuses, this one method should be usable for all the DPS categories.
	private double calculateDPS(boolean burst, boolean primaryTarget) {
		double firingTime = pressureDropDuration / getPressureDropModifier();
		double waitingTime = getRepressurizationDelay() + pressureGainDuration / getPressureGainModifier() + getChargeupTime();
		
		double duration;
		if (burst) {
			duration = firingTime;
		}
		else {
			duration = firingTime + waitingTime;
		}
		
		return totalDamageDealtPerBurst(primaryTarget) / duration;
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
		// Every other weapon I've modeled so far is just raw damage, without any increases from weakpoints or Cryo Minelets. 
		// I'm choosing to make Cryo Cannon the exception because it relies so much on freezing enemies.
		double firingTime = pressureDropDuration / getPressureDropModifier();
		double flowRate = getFlowRate();
		double numParticlesFiredPerBurst = Math.floor(firingTime * flowRate);
		
		double tankSize = getTankSize();
		double numFullBursts = Math.floor(tankSize / numParticlesFiredPerBurst);
		
		return (numFullBursts  + ((tankSize - numFullBursts * numParticlesFiredPerBurst) / numParticlesFiredPerBurst)) * totalDamageDealtPerBurst(false) * calculateMaxNumTargets();
	}

	@Override
	public int calculateMaxNumTargets() {
		return calculateNumGlyphidsInStream(getColdStreamReach());
	}

	@Override
	public double calculateFiringDuration() {
		double firingTime = pressureDropDuration / getPressureDropModifier();
		double waitingTime = getRepressurizationDelay() + pressureGainDuration / getPressureGainModifier() + getChargeupTime();
		double flowRate = getFlowRate();
		double numParticlesFiredPerBurst = Math.floor(firingTime * flowRate);
		
		double tankSize = getTankSize();
		double numFullBursts = Math.floor(tankSize / numParticlesFiredPerBurst);
		
		return numFullBursts * (firingTime + waitingTime) + (tankSize - numFullBursts * numParticlesFiredPerBurst) / flowRate;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = getParticleDamage() * averageFreezeMultiplier();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// This stat is only applicable to "gun"-type weapons
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		// Cryo Cannon particles don't need to be counted for Breakpoint calculations
		return 0;
	}

	@Override
	public double utilityScore() {
		// Armor Break -- Cryo Cannon ignores Armor of all kinds, so this will always be zero.
		
		// Slow
		double numTargets = calculateMaxNumTargets();
		utilityScores[3] = numTargets * UtilityInformation.Cold_Utility;
		
		// Freeze
		double freezeDuration = EnemyInformation.averageFreezeDuration();
		double freezeUptime = freezeDuration / (EnemyInformation.averageTimeToFreeze(getParticleCold() * getFlowRate() + icePathColdPerTick * icePathTicksPerSec / 2.0) + freezeDuration);
		utilityScores[6] = freezeUptime * numTargets * UtilityInformation.Frozen_Utility;
		
		// According to Elythnwaen, Snowball does 200 Cold Damage in a 4m radius, 2m full damage, 50% falloff at edge
		if (selectedOverclock == 5) {
			double[] snowballAoEEfficiency = calculateAverageAreaDamage(4, 2, 0.5);
			double avgColdDamage = -200 * snowballAoEEfficiency[1];
			utilityScores[6] += snowballAoEEfficiency[2] * EnemyInformation.percentageEnemiesFrozenBySingleBurstOfCold(avgColdDamage) * UtilityInformation.Frozen_Utility;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		return totalDamageDealtPerBurst(false) * calculateMaxNumTargets();
	}
	
	@Override
	public double timeToFireMagazine() {
		return pressureDropDuration / getPressureDropModifier();
	}
}
