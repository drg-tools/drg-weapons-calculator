package drillerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
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
		tankSize = 475;
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
		tier1[0] = new Mod("Larger Pressure Chamber", "x0.65 Pressure Drop Rate", modIcons.magSize, 1, 0);
		tier1[1] = new Mod("Improved Pump", "x1.8 Pressure Gain Rate", modIcons.chargeSpeed, 1, 1);
		tier1[2] = new Mod("Increased Flow Volume", "+1.6 Flow Rate", modIcons.rateOfFire, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Improved 2nd Stage Pump", "-0.4 sec Chargeup Time", modIcons.chargeSpeed, 2, 0);
		tier2[1] = new Mod("Stronger Cooling Unit", "+1 Cold per Particle", modIcons.coldDamage, 2, 1);
		tier2[2] = new Mod("Bypassed Integrity Check", "-1 sec Repressurization Delay", modIcons.coolingRate, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Improved Mixture", "+1 Cold per Particle", modIcons.coldDamage, 3, 0);
		tier3[1] = new Mod("Colder Ice Path", "+4 Ice Path Cold/Tick", modIcons.coldDamage, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("High Water Content", "+3 Damage per Particle", modIcons.directDamage, 4, 0);
		tier4[1] = new Mod("Overclocked Ejection Turbine", "+5m Cold Stream Reach", modIcons.distance, 4, 1);
		tier4[2] = new Mod("Larger Reserve Tank", "+150 Tank Size", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Fragile", "When a particle from Cryo Cannon damages a Frozen enemy and brings its health below 100 \"true\" hp, it has a (1 - hp/100) probability to deal the remaining hp as Kinetic Damage. "
				+ "This damage gets affected by the current Difficulty Scaling from Hazard level and player count, so at higher difficulties Fragile will no longer be able to score the killing blow.", modIcons.addedExplosion, 5, 0);
		tier5[1] = new Mod("Cold Radiance", "Cool down enemies within 4m of you at a rate of 60 Cold/sec. This stacks with the direct stream and Ice Path's cold sources as well.", modIcons.coldDamage, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Improved Thermal Efficiency", "+50 Tank Size, x0.75 Pressure Drop Rate", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Perfectly Tuned Cooler", "+1 Cold per Particle, +3 Ice Path Cold/Tick", overclockIcons.coldDamage, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Ice Spear", "Press the Reload button to consume 35 ammo and fire an Ice Spear that does 350 Direct Damage and 150 Area Damage in a 1.4m radius and stuns enemies for 3 seconds. "
				+ "In exchange, +1 sec Repressurization Delay", overclockIcons.projectileVelocity, 2, false);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Snowball", "Press the Reload button to consume 35 ammo and fire a Snowball that does 200 Cold Damage in a 4m radius, which will freeze most enemies instantly. "
				+ "In exchange, +1 sec Repressurization Delay", overclockIcons.aoeRadius, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Ice Storm", "x2 Damage per Particle, -2 Cold per Particle, -50 Tank Size, x1.5 Pressure Drop Rate", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Ice Shard Path", "+16 Ice Path Damage/Tick, x0 Ice Path Cold/Tick", overclockIcons.directDamage, 5);
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
				case 'C': {
					setSelectedModAtTier(4, 2, false);
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
		
		if (selectedTier2 == 1) {
			toReturn -= 1;
		}
		if (selectedTier3 == 0) {
			toReturn -= 1;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 4) {
			toReturn += 2;
		}
		
		return toReturn;
	}
	private int getTankSize() {
		int toReturn = tankSize;
		
		if (selectedTier4 == 2) {
			toReturn += 150;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 50;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 50;
		}
		
		return toReturn;
	}
	private double getChargeupTime() {
		double toReturn = chargeupTime;
		
		if (selectedTier2 == 0) {
			toReturn -= 0.4;
		}
		
		return toReturn;
	}
	private double getPressureDropModifier() {
		double modifier = 1.0;
		
		if (selectedTier1 == 0) {
			modifier *= 0.65;
		}
		
		if (selectedOverclock == 0) {
			modifier *= 0.75;
		}
		else if (selectedOverclock == 4) {
			modifier *= 1.5;
		}
		
		return modifier;
	}
	private double getFlowRate() {
		double toReturn = flowRate;
		
		if (selectedTier1 == 2) {
			toReturn += 1.6;
		}
		
		return toReturn;
	}
	private double getRepressurizationDelay() {
		double toReturn = repressurizationDelay;
		
		if (selectedTier2 == 2) {
			toReturn -= 1;
		}
		
		if (selectedOverclock == 2 || selectedOverclock == 3) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private double getPressureGainModifier() {
		double modifier = 1.0;
		
		if (selectedTier1 == 1) {
			modifier *= 1.8;
		}
		
		return modifier;
	}
	private double getColdStreamReach() {
		double toReturn = coldStreamReach;
		
		if (selectedTier4 == 1) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private double getIPDamagePerTick() {
		double toReturn = 0;
		
		if (selectedOverclock == 5) {
			toReturn += 16;
		}
		
		return toReturn;
	}
	private double getIPColdPerTick() {
		double toReturn = icePathColdPerTick;
		
		if (selectedTier3 == 1) {
			toReturn -= 4;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[16];
		
		// Stats about the direct stream's DPS
		toReturn[0] = new StatsRow("Damage per Particle:", getParticleDamage(), modIcons.directDamage, selectedTier4 == 0 || selectedOverclock == 4);
		
		boolean coldModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedOverclock == 1 || selectedOverclock == 4;
		// Again, choosing to display Cold as positive even though it's a negative value.
		toReturn[1] = new StatsRow("Cold per Particle:", -1 * getParticleCold(), modIcons.coldDamage, coldModified);
		
		toReturn[2] = new StatsRow("Avg Freeze Multiplier (doesn't affect itself):", averageFreezeMultiplier(), modIcons.special, false);
		
		toReturn[3] = new StatsRow("Cold Stream Reach:", getColdStreamReach(), modIcons.distance, selectedTier4 == 1);
		
		boolean tankSizeModified = selectedTier4 == 2 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[4] = new StatsRow("Tank Size:", getTankSize(), modIcons.carriedAmmo, tankSizeModified);
		
		toReturn[5] = new StatsRow("Chargeup Time:", getChargeupTime(), modIcons.chargeSpeed, selectedTier2 == 0);
		
		boolean pressureDropModified = selectedTier1 == 0 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Pressure Drop Rate:", convertDoubleToPercentage(getPressureDropModifier()), modIcons.magSize, pressureDropModified);
		toReturn[7] = new StatsRow("Pressure Drop Duration:", pressureDropDuration / getPressureDropModifier(), modIcons.hourglass, pressureDropModified);
		
		toReturn[8] = new StatsRow("Flow Rate:", getFlowRate(), modIcons.rateOfFire, selectedTier1 == 2);
		
		boolean delayModified = selectedTier2 == 2 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[9] = new StatsRow("Repressurization Delay:", getRepressurizationDelay(), modIcons.coolingRate, delayModified);
		
		boolean pressureGainModified = selectedTier1 == 1;
		toReturn[10] = new StatsRow("Pressure Gain Rate:", convertDoubleToPercentage(getPressureGainModifier()), modIcons.chargeSpeed, pressureGainModified);
		toReturn[11] = new StatsRow("Pressure Gain Duration:", pressureGainDuration / getPressureGainModifier(), modIcons.hourglass, pressureGainModified);
		
		// Stats about the Ice Path
		toReturn[12] = new StatsRow("Ice Path Damage per Tick:", getIPDamagePerTick(), modIcons.directDamage, selectedOverclock == 5, selectedOverclock == 5);
		
		// I'm choosing to display this as a positive number, even though internally it's negative.
		boolean IPColdModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[13] = new StatsRow("Ice Path Cold per Tick:", -1 * getIPColdPerTick(), modIcons.coldDamage, IPColdModified);
		
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
		double streamColdPerSec = getParticleCold() * getFlowRate();
		double icePathColdPerSec = getIPColdPerTick() * icePathTicksPerSec / 2.0;
		
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
		// Contrary to what some people have told me, CryoCannon does NOT gain bonus damage vs Frozen targets.
		double dmgPerParticle = getParticleDamage();
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
		
		double icePathDPS = 0;
		if (selectedOverclock == 5) {
			icePathDPS = getIPDamagePerTick() * icePathTicksPerSec / 2.0;
		}
		
		return totalDamageDealtPerBurst(primaryTarget) / duration + icePathDPS;
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
		
		double icePathDamage = 0;
		if (selectedOverclock == 5) {
			double numTargets = calculateMaxNumTargets();
			double avgTTK = averageTimeToKill();
			double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / avgTTK);
			
			double icePathDPS = getIPDamagePerTick() * icePathTicksPerSec / 2.0;
			double icePathDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, icePathDuration, icePathDPS);
			
			icePathDamage = icePathDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return baseDamage + fragileDamage + icePathDamage;
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
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 0, 0, 25, 0, 0, tier1[0].getText(true), "{ \"ex3\": { \"name\": \"Pressure Drop Rate\", \"value\": 0.33, \"multiply\": true } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"rate\": { \"name\": \"Chargeup Time\", \"value\": 0.4, \"subtract\": true } }", "Icon_Upgrade_ChargeUp", "Charge Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[2].getText(true), "{ \"ex2\": { \"name\": \"Freezing Power\", \"value\": 1 } }", "Icon_Upgrade_Cold", "Cold"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 0, 15, 24, tier2[0].getText(true), "{ \"clip\": { \"name\": \"Tank Capacity\", \"value\": 75 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 24, 0, 0, 0, 15, 0, tier2[1].getText(true), "{ \"ex1\": { \"name\": \"Cold Stream Reach\", \"value\": 5 } }", "Icon_Upgrade_Distance", "Reach"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 2000, 0, 15, 0, 24, 0, 0, tier2[2].getText(true), "{ \"reload\": { \"name\": \"Repressurization Delay\", \"value\": 1, \"subtract\": true } }", "Icon_Upgrade_TemperatureCoolDown", "Overheat"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 0, 0, 50, 0, 35, tier3[0].getText(true), "{ \"ex4\": { \"name\": \"Pressure Gain Rate\", \"value\": 1.7, \"multiply\": true } }", "Icon_Upgrade_ChargeUp", "Charge Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 0, 50, 35, 0, 0, 0, tier3[1].getText(true), "{ \"ex7\": { \"name\": \"Flow Rate\", \"value\": 20, \"percent\": true } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 0, 72, 0, 48, 50, 0, tier4[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 48, 0, 0, 0, 50, 72, tier4[1].getText(true), "{ \"ex2\": { \"name\": \"Freezing Power\", \"value\": 1 } }", "Icon_Upgrade_Cold", "Cold"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 4800, 50, 0, 48, 0, 0, 72, tier4[2].getText(true), "{ \"clip\": { \"name\": \"Tank Capacity\", \"value\": 150 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 140, 64, 0, 70, 0, 0, tier5[0].getText(true), "{ \"ex5\": { \"name\": \"Frozen Targets can Shatter\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Explosion", "Special"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 0, 0, 64, 0, 140, 70, tier5[1].getText(true), "{ \"ex6\": { \"name\": \"Area Cold Damage\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Cold", "Cold"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8350, 110, 0, 0, 125, 70, 0, overclocks[0].getText(true), "{ \"clip\": { \"name\": \"Tank Capacity\", \"value\": 25 }, "
				+ "\"ex3\": { \"name\": \"Pressure Drop Rate\", \"value\": 0.75, \"multiply\": true } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8750, 65, 110, 130, 0, 0, 0, overclocks[1].getText(true), "{ \"ex2\": { \"name\": \"Freezing Power\", \"value\": 1 }, "
				+ "\"ex7\": { \"name\": \"Flow Rate\", \"value\": 10, \"percent\": true } }", "Icon_Upgrade_Cold"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 8900, 125, 0, 0, 0, 70, 100, overclocks[2].getText(true), "{ \"ex3\": { \"name\": \"Pressure Drop Rate\", \"value\": 2.25, \"multiply\": true }, "
				+ "\"ex4\": { \"name\": \"Pressure Gain Rate\", \"value\": 2.7, \"multiply\": true }, \"ex7\": { \"name\": \"Flow Rate\", \"value\": 10, \"percent\": true } }", "Icon_Upgrade_Duration"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8950, 0, 0, 110, 0, 60, 130, overclocks[3].getText(true), "{ \"reload\": { \"name\": \"Repressurization Delay\", \"value\": 1 }, "
				+ "\"ex8\": { \"name\": \"Ice Spear\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_ProjectileSpeed"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7200, 105, 0, 75, 0, 130, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 2, \"multiply\": true }, "
				+ "\"ex2\": { \"name\": \"Freezing Power\", \"value\": 3, \"subtract\": true }, \"clip\": { \"name\": \"Tank Capacity\", \"value\": 50, \"subtract\": true }, \"ex3\": { \"name\": \"Pressure Drop Rate\", \"value\": 1.5, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8400, 0, 0, 130, 70, 0, 90, overclocks[5].getText(true), "{ \"clip\": { \"name\": \"Tank Capacity\", \"value\": 100, \"subtract\": true }, "
				+ "\"reload\": { \"name\": \"Repressurization Delay\", \"value\": 1 }, \"ex9\": { \"name\": \"Snowball\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Area"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
