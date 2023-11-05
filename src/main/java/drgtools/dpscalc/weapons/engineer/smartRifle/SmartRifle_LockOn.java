package drgtools.dpscalc.weapons.engineer.smartRifle;

import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.utilities.MathUtils;

public class SmartRifle_LockOn extends SmartRifle {
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public SmartRifle_LockOn() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public SmartRifle_LockOn(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public SmartRifle_LockOn(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "LOK-1 Smart Rifle (Lock-On)";
		// This value is false by default, but SmartRifle sets it to true. In order to model the burst-fire Lock-On mechanic, it has to be set back to false.
		customizableRoF = false;
	}
	
	@Override
	public SmartRifle_LockOn clone() {
		return new SmartRifle_LockOn(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "SmartRifle_LockOn";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public double getRateOfFire() {
		/* 
			At its core, the effective RoF for the Lock-On feature is just "1 bullet / (time it takes to lock on one bullet + time it takes to fire one locked-on bullet)".
			It doesn't matter how many bullets get Locked-On before the player fires, the effective RoF will always be the same.
			N Bullets / (N * Locks + N * Bursts) = 1 / (Lock + Burst)
		*/
		return 1.0 / (getLockonTime() + getTimeBetweenBulletsDuringBurst());
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[17];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, selectedTier1 == 0 || selectedOverclock == 3);
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, selectedOverclock == 3, selectedOverclock == 3);
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), modIcons.aoeRadius, selectedOverclock == 3, selectedOverclock == 3);
		
		toReturn[3] = new StatsRow("Acquire Lock-On Range:", getLockonRange(), modIcons.distance, selectedTier2 == 0 || selectedTier2 == 2);
		toReturn[4] = new StatsRow("Acquire Lock-On Threshold (degrees):", getMaxLockonDegree(), modIcons.angle, selectedTier2 == 0 || selectedTier2 == 1);
		toReturn[5] = new StatsRow("Lose Lock-On Threshold (degrees):", getLoseLockonDegree(), modIcons.angle, selectedTier2 == 1 || selectedOverclock == 2);
		boolean timeModified = selectedTier4 == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Time to Acquire a Lock-On:", getLockonTime(), modIcons.duration, timeModified);
		toReturn[7] = new StatsRow("Duration of a Lock-On:", getLockonDuration(), modIcons.hourglass, selectedOverclock == 5, selectedOverclock == 5);
		boolean maxNumLocksModified = selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 4;
		toReturn[8] = new StatsRow("Max Number of Lock-Ons (Full Lock):", getMaxNumLockons(), modIcons.special, maxNumLocksModified);
		toReturn[9] = new StatsRow("RoF During Lock-On Burst:", (1.0 / getTimeBetweenBulletsDuringBurst()), modIcons.rateOfFire, selectedOverclock == 2);
		
		toReturn[10] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedOverclock == 1 || selectedOverclock == 4);
		boolean maxAmmoModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[11] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, maxAmmoModified);
		toReturn[12] = new StatsRow("Effective RoF:", getRateOfFire(), modIcons.rateOfFire, timeModified || selectedOverclock == 2);
		toReturn[13] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 2);
		toReturn[14] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedOverclock == 4, selectedOverclock == 4);
		toReturn[15] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedOverclock == 0);
		toReturn[16] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), modIcons.blowthrough, selectedTier3 == 2, selectedTier3 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private int estimateNumberOfTargetsPerFullLock() {
		double damagePerBullet = getDirectDamage();
		if (selectedTier5 == 1) {
			damagePerBullet *= 1.2;
		}
		
		// I chose 108 because it's the Grunt's eHP on Haz4+
		// I am also intentionally not accounting for the Area Damage dealt by OC "Explosive Chemical Rounds"
		double avgNumBulletsPerLock = Math.round(108.0 / damagePerBullet);
		return (int) Math.floor(getMaxNumLockons() / avgNumBulletsPerLock);
	}

	@Override
	public boolean currentlyDealsRadialDamage() {
		return selectedOverclock == 3;
	}
	
	@Override
	protected void recalculateRadialEfficiency() {
		if (selectedOverclock == 3) {
			aoeEfficiency = calculateAverageAreaDamage(4.0, 2.0, 0.5);
		}
		else {
			aoeEfficiency = new double[3];
		}
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		// I'm choosing to model the lock-on functionality as if it hits Weakpoints the "generic" percentage of the time, and has a 100% general accuracy.
		double weakpointAccuracy = EnemyInformation.probabilityBulletWillHitWeakpoint();
		
		double duration, directWeakpointDamage;
		double magSize = getMagazineSize();
		double RoF = getRateOfFire();
		
		if (burst) {
			duration = magSize / RoF;
		}
		else {
			duration = (magSize / RoF) + getReloadTime();
		}
		
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
		// Damage wasted by Armor
		// Special case: OC "Seeker Rounds" bypasses Armor entirely
		if (armorWasting && !statusEffects[1] && selectedOverclock != 2) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			directDamage *= armorWaste;
		}
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
			areaDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double damageBoostMultiplier = 1.0;
		// T3.A "Electro-Chemical Rounds" damage increase
		if (selectedTier3 == 0) {
			// Currently, the +20% Fire-element + Heat and +20% Electric-element damage components are added separately, so this can add up to +40% damage per shot.
			if (statusEffects[0]) {
				damageBoostMultiplier += 0.2;
			}
			
			if (statusEffects[2] || statusEffects[3]) {
				damageBoostMultiplier += 0.2;
			}
			else if (selectedTier5 == 0) {
				// As far as I can tell, T5.A applies its 3 sec Electrocute DoT on the first bullet of the burst, so the first bullet doesn't get the +20% damage bonus.
				// To imitate that effect, I'm re-using the short effect method and pretending it's a 100% chance to proc (which should trick the method into applying the damage on the second bullet)
				damageBoostMultiplier += (averageBonusPerMagazineForShortEffects(1.2, duration, false, 1.0, magSize, RoF) - 1.0);
			}
		}
		
		// T5.B "Unstable Lock Mechanism" damage increase
		if (selectedTier5 == 1) {
			// This is just +20% Kinetic damage on Full Lock, and it does NOT affect the 50 Area Damage
			// According to the test I did with Arthiio0, this bonus damage is multiplicative with at least the Electric half of T3.A.
			damageBoostMultiplier *= 1.2;
		}
		
		// Because the Area Damage from OC "Explosive Chemical Rounds" is a different damagecomponent, it doesn't get increased by T3.A or T5.B
		directDamage *= damageBoostMultiplier;
		
		if (weakpoint && !statusEffects[1]) {
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus(), 1.0);
		}
		else {
			directWeakpointDamage = directDamage;
		}
		
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) magSize - bulletsThatHitWeakpoint;
		
		/* 
			OC "Explosive Chemical Rounds" deals 50 Area Damage in a 4m radius to any target that has 3 or more lock on it. The explosion happens on the last bullet of the burst locked onto the enemy.
			There are three ways to model this: best case (every third bullet), worst case (only as many explosions as magSize/maxNumLocks), or some average case. I'm going to choose to model the 
			average case for now; in particular I'm going to model it as if there's only 1 explosion per burst that can kill the average eHP.
		*/
		double explosionDamage = 0.0;
		if (selectedOverclock == 3) {
			explosionDamage = areaDamage * estimateNumberOfTargetsPerFullLock() * Math.round(magSize /  getMaxNumLockons());
		}
		
		double electrocuteDPS = 0;
		if (selectedTier5 == 0) {
			electrocuteDPS = 12.0;
		}
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + explosionDamage) / duration + electrocuteDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		/*
			Hoo boy, this is gonna be almost as nasty as Revolver. So, there's a blowthrough which will do the regular DPS to any enemy behind the primary target, 
			but there's ALSO the OC "Explosive Chemical Rounds" which will do 50 Area Damage on the last bullet locked onto an enemy with 3 or more locks on it. 
			Modeling how those two things interact will be pretty hard...
		*/
		
		double magSize = getMagazineSize();
		double duration = magSize / getRateOfFire() + getReloadTime();
		
		double blowthroughDPS = 0;
		if (selectedTier3 == 2) {
			// Just the Direct Damage Ideal Sustained DPS
			blowthroughDPS = getDirectDamage() * magSize / duration;
		}
		
		double explosionDPS = 0;
		if (selectedOverclock == 3) {
			// Re-using what I did for primary target damage
			double areaDamage = getAreaDamage() * aoeEfficiency[1];
			explosionDPS = areaDamage * estimateNumberOfTargetsPerFullLock() * Math.floor(magSize / getMaxNumLockons()) / duration;
		}
		
		return blowthroughDPS + explosionDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double directDamage = (magSize + carriedAmmo) * getDirectDamage() * calculateBlowthroughDamageMultiplier(getNumberOfPenetrations());
		double numTargetsPerMag = estimateNumberOfTargetsPerFullLock() * Math.floor(magSize / getMaxNumLockons());
		
		double areaDamage = 0;
		if (selectedOverclock == 3) {
			// 50 Area Damage in a 4m radius per target with 3+ locks on them
			areaDamage = getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2] * numTargetsPerMag * numMagazines(carriedAmmo, magSize);
		}
		
		double electrocuteDamage = 0;
		if (selectedTier5 == 0) {
			// Electrocute is 12 DPS for 3 seconds
			electrocuteDamage = 12 * 3 * numTargetsPerMag * numMagazines(carriedAmmo, magSize);
		}
		
		return directDamage + areaDamage + electrocuteDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedOverclock == 3) {
			return (int) aoeEfficiency[2] + getNumberOfPenetrations();
		}
		else {
			return 1 + getNumberOfPenetrations();
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		double enemyHP = EnemyInformation.averageHealthPool();
		if (selectedOverclock == 3) {
			enemyHP -= getAreaDamage();
		}
		return Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		// This is also being modeled as if the player is min-maxing the explosion every 3 rounds.
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage() / 3.0);
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Because this object only models the auto-aiming feature of SmartRifle, the Hitscan Accuracy has no need to be estimated.
		return -1;
	}
	
	@Override
	public int breakpoints() {
//		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
//		double[] directDamage = new double[5];
//		directDamage[0] = getDirectDamage();  // Kinetic
//
//		// T3.A "Electro-Chemical Rounds" is a x1.2 Damage multiplier vs enemies afflicted by Burning, Electrocute, or IFG
//		if (selectedTier3 == 0) {
//			// Burning adds Fire-element
//			if (statusEffects[0]) {
//				directDamage[2] = 0.2 * directDamage[0];
//			}
//
//			// Electrocute/IFG adds Electric Element
//			if (statusEffects[2] || statusEffects[3] || selectedTier5 == 0) {
//				directDamage[4] = 0.2 * directDamage[0];
//			}
//		}
//
//		if (selectedTier5 == 1) {
//			// This multiplies with T3.A's damage bonuses.
//			directDamage = MathUtils.vectorScalarMultiply(1.2, directDamage);
//		}
//
//		double[] areaDamage = new double[5];
//		if (selectedOverclock == 3) {
//			// In contrast to the DPS calculations, I'm choosing to model this one as if the player is only using 3-round bursts to trigger
//			// as many explosions as possible. To emulate this, I'm assigning 1/3 of the explosion to each shot.
//			areaDamage[1] = getAreaDamage() / 3.0;
//		}
//
//		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
//		double[] dot_dps = new double[4];
//		double[] dot_duration = new double[4];
//		double[] dot_probability = new double[4];
//		if (selectedTier5 == 0) {
//			// The Electrocute DoT is a 100% chance to proc on the third Locked bullet. To emulate that, I'm going to have Breakpoints
//			// pretend that it has a 1/3 chance to occur on every bullet.
//			dot_dps[0] = 12.0;
//			dot_duration[0] = 3.0;
//			dot_probability[0] = 0.3333;
//		}
//
//		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability,
//															getWeakpointBonus(), getArmorBreaking(), getRateOfFire(), 0.0, 0.0,
//															statusEffects[1], statusEffects[3], false, false);
//		return MathUtils.sum(breakpoints);
		return 0;
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// Re-using estimateNumberOfTargetsPerFullLock() snippet
		// This is a bit of an over-estimation; technically avg eHP is closer to 180.
		double avgNumLocksPerEnemy = Math.floor(108.0 / getDirectDamage());
		double avgNumEnemiesLockedOnto = getMaxNumLockons() / avgNumLocksPerEnemy;
		
		// Slow
		utilityScores[3] = 0;
		// T5.A "Electric Generator Mod" applies an Electrocute DoT that slows enemies by 80% for 3 seconds.
		if (selectedTier5 == 0) {
			utilityScores[3] += avgNumEnemiesLockedOnto * 3.0 * UtilityInformation.Electrocute_Slow_Utility;
		}
		
		// OC "Neuro-Lasso" slows every enemy by x0.9 for each Lock on it (stacks with itself)
		if (selectedOverclock == 5) {
			utilityScores[3] += avgNumEnemiesLockedOnto * getLockonDuration() * (1.0 - Math.pow(0.9, avgNumLocksPerEnemy));
		}
		
		// Fear
		utilityScores[4] = 0;
		// T5.C "Fear Frequency" does a burst of Fear around the player on the last bullet fired during a Lock-On Burst. 2.4m + 0.15m/bullet radius, and +15% Fear per bullet.
		if (selectedTier5 == 2) {
			double fearRadius = 2.4 + 0.15 * avgNumLocksPerEnemy;
			double fearFactor = 0.15 * avgNumLocksPerEnemy;
			
			double numEnemiesHitByFear = calculateNumGlyphidsInRadius(fearRadius, false);
			double fearDuration = 0;
			if (selectedOverclock == 5) {
				fearDuration = EnemyInformation.averageFearDuration(1.0 - Math.pow(0.9, avgNumLocksPerEnemy), getLockonDuration());
			}
			else {
				fearDuration = EnemyInformation.averageFearDuration();
			}
			utilityScores[4] += calculateFearProcProbability(fearFactor) * numEnemiesHitByFear * fearDuration * UtilityInformation.Fear_Utility;
		}
		
		// OC "Explosive Chemical Rounds" inflicts 0.5 Fear to all enemies within its 4m radius
		if (selectedOverclock == 3) {
			// Just like SMG OC "Turret EM Discharge", I'm choosing to artificially halve the radius of the AoE Fear effect to get more realistic numbers.
			double numEnemiesFearedPerExplosion = calculateNumGlyphidsInRadius(2.0, false);
			utilityScores[4] += calculateFearProcProbability(0.5) * numEnemiesFearedPerExplosion * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		int magSize = getMagazineSize();
		double directDamage = magSize * getDirectDamage() * calculateBlowthroughDamageMultiplier(getNumberOfPenetrations());
		double numTargetsPerMag = estimateNumberOfTargetsPerFullLock() * Math.floor(magSize / getMaxNumLockons());
		
		double areaDamage = 0;
		if (selectedOverclock == 3) {
			// 50 Area Damage in a 4m radius per target with 3+ locks on them
			areaDamage = getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2] * numTargetsPerMag;
		}
		
		double electrocuteDamage = 0;
		if (selectedTier5 == 0) {
			// Electrocute is 12 DPS for 3 seconds
			electrocuteDamage = 12 * 3 * numTargetsPerMag;
		}
		
		return directDamage + areaDamage + electrocuteDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public double damageWastedByArmor() {
		double wpAcc = EnemyInformation.probabilityBulletWillHitWeakpoint();
		double genAcc = (1.0 - wpAcc) * 100;
		wpAcc *= 100;
		
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage() / 3.0, getArmorBreaking(), getWeakpointBonus(), genAcc, wpAcc);
		if (selectedOverclock == 2) {
			return 0.0;
		}
		else {
			return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
		}
	}
}
