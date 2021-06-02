package weapons.driller;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import utilities.MathUtils;
import weapons.Weapon;

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
		tankSize = 500;
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
		tier1[0] = new Mod("Larger Pressure Chamber", "x0.33 Pressure Drop Rate", modIcons.magSize, 1, 0);
		tier1[1] = new Mod("Faster Turbine Spinup", "-0.4 sec Chargeup Time", modIcons.chargeSpeed, 1, 1);
		tier1[2] = new Mod("Stronger Cooling Unit", "+1 Cold per Particle", modIcons.coldDamage, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Larger Reserve Tank", "+75 Tank Size", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Overclocked Ejection Turbine", "+5m Cold Stream Reach", modIcons.distance, 2, 1);
		tier2[2] = new Mod("Bypassed Integrity Check", "-1 sec Repressurization Delay", modIcons.coolingRate, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Improved Pump", "x1.75 Pressure Gain Rate", modIcons.chargeSpeed, 3, 0);
		tier3[1] = new Mod("Increased Flow Volume", "+1.6 Flow Rate", modIcons.rateOfFire, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hard Mixture", "+3 Damage per Particle", modIcons.directDamage, 4, 0);
		tier4[1] = new Mod("Supercooling Mixture", "+1 Cold per Particle", modIcons.coldDamage, 4, 1);
		tier4[2] = new Mod("Larger Reserve Tank", "+150 Tank Size", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Fragile", "When a particle from Cryo Cannon damages a Frozen enemy and brings its health below 100 \"true\" hp, it has a (1 - hp/100) probability to deal the remaining hp as Kinetic Damage. "
				+ "This damage gets affected by the current Difficulty Scaling from Hazard level and player count, so at higher difficulties Fragile will no longer be able to score the killing blow.", modIcons.addedExplosion, 5, 0);
		tier5[1] = new Mod("Cold Radiance", "After every full second of firing, deal 60 Cold in a 4m radius around you. This Cold/sec stacks with the direct stream and Ice Path's cold sources as well.", modIcons.coldDamage, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Improved Thermal Efficiency", "+25 Tank Size, x0.75 Pressure Drop Rate", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Tuned Cooler", "+1 Cold per Particle, +0.8 Flow Rate, x0.8 Pressure Gain Rate, +0.2 sec Chargeup Time", overclockIcons.coldDamage, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Flow Rate Expansion", "x2.7 Pressure Gain Rate, +0.8 Flow Rate, x2.25 Pressure Drop Rate", overclockIcons.duration, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Ice Spear", "Press the Reload button to consume 50 ammo and fire an Ice Spear that does 350 Direct Damage and 150 Area Damage in a 1.4m radius and stuns enemies for 3 seconds. "
				+ "In exchange, +1 sec Repressurization Delay", overclockIcons.projectileVelocity, 3, false);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Ice Storm", "x2 Damage per Particle, x2 Damage vs Frozen Enemies, -3 Cold per Particle, -75 Tank Size, x1.5 Pressure Drop Rate", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Snowball", "Press the Reload button to consume 35 ammo and fire a Snowball that does 200 Cold in a 4m radius, which will freeze most enemies instantly. "
				+ "In exchange, -100 Tank Size, +1 sec Repressurization Delay", overclockIcons.aoeRadius, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
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
			toReturn += 75;
		}
		if (selectedTier4 == 2) {
			toReturn += 150;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 25;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 75;
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
		
		if (selectedOverclock == 1) {
			toReturn += 0.2;
		}
		
		return toReturn;
	}
	private double getPressureDropModifier() {
		double modifier = 1.0;
		
		if (selectedTier1 == 0) {
			modifier *= 0.33;
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
			modifier *= 1.75;
		}
		
		if (selectedOverclock == 1) {
			modifier *= 0.8;
		}
		else if (selectedOverclock == 2) {
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
		toReturn[0] = new StatsRow("Damage per Particle:", getParticleDamage(), modIcons.directDamage, selectedTier4 == 0 || selectedOverclock == 4);
		
		boolean coldModified = selectedTier1 == 2 || selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 4;
		// Again, choosing to display Cold as positive even though it's a negative value.
		toReturn[1] = new StatsRow("Cold per Particle:", -1 * getParticleCold(), modIcons.coldDamage, coldModified);
		
		toReturn[2] = new StatsRow("Avg Freeze Multiplier for other weapons:", averageFreezeMultiplier(UtilityInformation.Frozen_Damage_Multiplier), modIcons.special, false);
		
		toReturn[3] = new StatsRow("Avg Freeze Multiplier for itself:", averageFreezeMultiplier(2.0), modIcons.special, selectedOverclock == 4, selectedOverclock == 4);
		
		toReturn[4] = new StatsRow("Cold Stream Reach:", getColdStreamReach(), modIcons.distance, selectedTier2 == 1);
		
		boolean tankSizeModified = selectedTier2 == 0 || selectedTier4 == 2 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Tank Size:", getTankSize(), modIcons.carriedAmmo, tankSizeModified);
		
		toReturn[6] = new StatsRow("Chargeup Time:", getChargeupTime(), modIcons.chargeSpeed, selectedTier1 == 1 || selectedOverclock == 1);
		
		boolean pressureDropModified = selectedTier1 == 0 || selectedOverclock % 2 == 0 ;
		toReturn[7] = new StatsRow("Pressure Drop Rate:", convertDoubleToPercentage(getPressureDropModifier()), modIcons.magSize, pressureDropModified);
		toReturn[8] = new StatsRow("Pressure Drop Duration:", pressureDropDuration / getPressureDropModifier(), modIcons.hourglass, pressureDropModified);
		
		boolean flowRateModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[9] = new StatsRow("Flow Rate:", getFlowRate(), modIcons.rateOfFire, flowRateModified);
		
		boolean delayModified = selectedTier2 == 2 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[10] = new StatsRow("Repressurization Delay:", getRepressurizationDelay(), modIcons.coolingRate, delayModified);
		
		boolean pressureGainModified = selectedTier3 == 0 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[11] = new StatsRow("Pressure Gain Rate:", convertDoubleToPercentage(getPressureGainModifier()), modIcons.chargeSpeed, pressureGainModified);
		toReturn[12] = new StatsRow("Pressure Gain Duration:", pressureGainDuration / getPressureGainModifier(), modIcons.hourglass, pressureGainModified);
		
		// Stats about the Ice Path
		// I'm choosing to display this as a positive number, even though internally it's negative.
		toReturn[13] = new StatsRow("Ice Path Cold per Tick:", -1 * icePathColdPerTick, modIcons.coldDamage, false);
		
		toReturn[14] = new StatsRow("Ice Path Ticks per Sec:", icePathTicksPerSec, modIcons.blank, false);
		
		toReturn[15] = new StatsRow("Ice Path Duration", icePathDuration, modIcons.hourglass, false);
		
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
		double icePathColdPerSec = icePathColdPerTick * icePathTicksPerSec / 2.0;
		
		double coldRadianceColdPerSec = 0;
		if (selectedTier5 == 1) {
			// 60 Cold/sec in a 4m radius
			// I want this to be less effective with far-reaching streams to model how the further the steam flies the less likely it is that the enemies will be within the 4m.
			coldRadianceColdPerSec = -60.0 * 4.0 / getColdStreamReach();
		}
		
		if (refreeze) {
			return EnemyInformation.averageTimeToRefreeze(getParticleCold() * getFlowRate() + icePathColdPerSec + coldRadianceColdPerSec);
		}
		else {
			return EnemyInformation.averageTimeToFreeze(0, getParticleCold(), getFlowRate(), icePathColdPerSec + coldRadianceColdPerSec);
		}
	}
	
	private double totalDamageDealtPerBurst(boolean primaryTarget) {
		// Contrary to what some people have told me, CryoCannon does NOT gain bonus damage vs Frozen targets unless OC "Ice Storm" is equipped.
		double dmgPerParticle = getParticleDamage();
		if (selectedOverclock == 4) {
			dmgPerParticle *= averageFreezeMultiplier(2.0);
		}
		double firingTime = pressureDropDuration / getPressureDropModifier();
		double flowRate = getFlowRate();
		
		double fragileDamage = 0;
		if (selectedTier5 == 0) {
			if (primaryTarget && statusEffects[0]) {
				// Burning prevents Frozen, which negates Fragile
				fragileDamage = 0;
			}
			else {
				double averageHealth = EnemyInformation.averageHealthPool(true);  // This already returns health multiplied by resistances, so this is the "effective" hp, not "internal" hp 
				double averageResistance = EnemyInformation.averageDifficultyScalingResistance();
				double avgNumParticlesBeforeFragileCanProc = Math.ceil((averageHealth - 100.0 * averageResistance) / dmgPerParticle);  // This will get the Effective HP below 100 * Resistance, which is the same as getting Internal HP below 100
				double expectedNumParticlesForFragileKill = Math.ceil(recursiveFragileAmmoSpent(100.0, dmgPerParticle, averageResistance));  // This number is how many particles it will take to kill the creature once below 100 Internal HP
				double totalAmmoForAverageFragileKill = avgNumParticlesBeforeFragileCanProc + expectedNumParticlesForFragileKill;
				
				double totalNumFragileKills = Math.floor(firingTime * flowRate / totalAmmoForAverageFragileKill);
				fragileDamage = totalNumFragileKills * recursiveFragileDamage(100.0, dmgPerParticle, averageResistance) * averageResistance;
			}
		}
		
		double temperatureShock = 0;
		// Status Effects
		if (primaryTarget && statusEffects[0]) {
			// Burning prevents the enemy from being Frozen, which doesn't directly affect DPS, but it does proc TempShock.
			temperatureShock = 200;
		}
		
		if (primaryTarget && statusEffects[3]) {
			dmgPerParticle *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		return firingTime * flowRate * dmgPerParticle + fragileDamage + temperatureShock;
	}
	
	private double averageFreezeMultiplier(double baseMultiplier) {
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
					totalDamage += particlesFired * baseMultiplier;
					totalParticles += particlesFired;
					firingTime -= freezeDuration;
					currentlyFrozen = false;
				}
				else {
					totalDamage += firingTime * flowRate * baseMultiplier;
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
	
	private double recursiveFragileDamage(double currentTrueHP, double particleDamage, double resistance) {
		/*
			TriggerHappyBro had the idea to model this using a recursive function, and I'm choosing to implement it. I had originally thought to do it iteratively using a while-loop,
			but it was returning a very weird dataset that wasn't accounting for overkill properly. As a result, I switched to this recursive method because it produces more reliable
			results even thought it's a little less computationally efficient.
			
			Fundamentally, this method just has to calculate three things at each step:
				1. How much damage Fragile could do if it procs on this particle
				2. How much damage Fragile could do on later procs if it does happen on this particle
				3. How much damage Fragile could do on later procs if it doesn't happen on this particle
			
			Once those three values are calculated, it just multiplies each returned value by its expected probability to happen. Because the probability that Fragile procs is multiplied
			against both the damage dealt by the proc and the potential damage afterward, those two are added together to save one CPU cycle.
			
			I did a little work on WolframAlpha, and found via the integral of x * (1 - x/100) from 0 to 100 divided by 100 that the true average of damage * probability 
			should be 100/6, so I would expect the average first proc of Fragile to be at 78.8675 internal hp.
			
			I spent about 5 weeks trying to find or create a formula that would let me recreate this method's data mathematically, but ultimately failed. I know it's a f(x, y) = z function
			and that it has an approximately 1/x partial derivative with respect to Damage per Particle and some kind of piece-wise linear partial derivative with respect to Difficulty 
			Scaling Resistance, but I was unable to create the function needed to approximate these outputs. As a result I have to settle for this recursive function because it works.
		*/
		
		currentTrueHP -= particleDamage / resistance;
		
		// Base case: damage from frost particle kills enemy outright
		if (currentTrueHP <= 0) {
			return 0;
		}
		
		double probability = 1.0 - currentTrueHP / 100.0;
		double fragileDamage = Math.min(currentTrueHP, currentTrueHP / resistance);
		double fragileProcs = recursiveFragileDamage(currentTrueHP - fragileDamage, particleDamage, resistance);
		double noProc = recursiveFragileDamage(currentTrueHP, particleDamage, resistance);
		
		return probability * (fragileDamage + fragileProcs) + (1.0 - probability) * noProc;
	}
	
	private double recursiveFragileAmmoSpent(double currentTrueHP, double particleDamage, double resistance) {
		/*
			Same logic as the Damage method, but this one counts ammo spent.
		*/
		
		currentTrueHP -= particleDamage / resistance;
		
		// Base case: damage from frost particle kills enemy outright
		if (currentTrueHP <= 0) {
			return 1.0;
		}
		
		double probability = 1.0 - currentTrueHP / 100.0;
		double fragileDamage = Math.min(currentTrueHP, currentTrueHP / resistance);
		double fragileProcs = recursiveFragileAmmoSpent(currentTrueHP - fragileDamage, particleDamage, resistance);
		double noProc = recursiveFragileAmmoSpent(currentTrueHP, particleDamage, resistance);
		
		return 1.0 + probability * fragileProcs + (1.0 - probability) * noProc;
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
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		return calculateDPS(burst, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		return calculateDPS(false, false);
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// Because Cryo Cannon doesn't gain bonus damage vs Frozen targets, the total damage is pretty simple to calculate.
		int numTargets = calculateMaxNumTargets();
		double dmgPerParticle = getParticleDamage();
		double tankSize = getTankSize();
		double baseDamage = numTargets * dmgPerParticle * tankSize;
		
		double fragileDamage = 0;
		if (selectedTier5 == 0) {
			// Adapted from totalDamagePerBurst() above
			double averageHealth = EnemyInformation.averageHealthPool(true);  // This already returns health multiplied by resistances, so this is the "effective" hp, not "internal" hp 
			double averageResistance = EnemyInformation.averageDifficultyScalingResistance();
			double avgNumParticlesBeforeFragileCanProc = Math.ceil((averageHealth - 100.0 * averageResistance) / dmgPerParticle);  // This will get the Effective HP below 100 * Resistance, which is the same as getting Internal HP below 100
			double expectedNumParticlesForFragileKill = Math.ceil(recursiveFragileAmmoSpent(100.0, dmgPerParticle, averageResistance));  // This number is how many particles it will take to kill the creature once below 100 Internal HP
			double totalAmmoForAverageFragileKill = avgNumParticlesBeforeFragileCanProc + expectedNumParticlesForFragileKill;
			
			double totalNumFragileKills = numTargets * Math.floor(tankSize / totalAmmoForAverageFragileKill);
			fragileDamage = totalNumFragileKills * recursiveFragileDamage(100.0, dmgPerParticle, averageResistance) * averageResistance;
		}
		
		return baseDamage + fragileDamage;
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
		// Cryo Cannon particles don't need to be counted for Breakpoint calculations
		return 0;
	}

	@Override
	public double utilityScore() {
		// Armor Break -- Cryo Cannon ignores Armor of all kinds, so this will always be zero.
		
		// Slow
		double numTargets = calculateMaxNumTargets();
		utilityScores[3] = numTargets * UtilityInformation.Cold_Utility;
		
		// Stun
		// OC "Ice Spear" stuns all enemies in a 1.4m radius for 3 seconds
		if (selectedOverclock == 3) {
			utilityScores[5] = calculateNumGlyphidsInRadius(1.4) * 3.0 * calculateNumGlyphidsInRadius(1.4);
		}
		else {
			utilityScores[5] = 0;
		}
		
		// Freeze
		double freezeDuration = EnemyInformation.averageFreezeDuration();
		double freezeUptime = freezeDuration / (averageTimeToFreeze(false) + freezeDuration);
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
	public double averageTimeToCauterize() {
		return averageTimeToFreeze(false);
	}
	
	@Override
	public double damagePerMagazine() {
		return totalDamageDealtPerBurst(false) * calculateMaxNumTargets();
	}
	
	@Override
	public double timeToFireMagazine() {
		return pressureDropDuration / getPressureDropModifier();
	}
	
	@Override
	public double damageWastedByArmor() {
		// Cryo Cannon's stream ignores all Armor.
		return 0;
	}
}
