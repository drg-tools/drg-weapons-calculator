package weapons.engineer;

import guiPieces.customButtons.ButtonIcons.modIcons;
import modelPieces.EnemyInformation;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

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
		
		toReturn[3] = new StatsRow("Lock-On Range:", getLockonRange(), modIcons.distance, selectedTier2 == 0 || selectedTier2 == 2);
		boolean thresholdModified = selectedTier2 == 0 || selectedTier2 == 1 || selectedOverclock == 2;
		toReturn[4] = new StatsRow("Lock-On Threshold (degrees):", getLockonThreshold(), modIcons.angle, thresholdModified);
		boolean timeModified = selectedTier4 == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Time to Acquire a Lock-On:", getLockonTime(), modIcons.duration, timeModified);
		toReturn[6] = new StatsRow("Duration of a Lock-On:", getLockonDuration(), modIcons.hourglass, selectedOverclock == 5);
		boolean maxNumLocksModified = selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[7] = new StatsRow("Max Number of Lock-Ons (Full Lock):", getMaxNumLockons(), modIcons.special, maxNumLocksModified);
		toReturn[8] = new StatsRow("RoF During Lock-On Burst:", (1.0 / getTimeBetweenBulletsDuringBurst()), modIcons.rateOfFire, selectedOverclock == 2);
		
		toReturn[9] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedOverclock == 1 || selectedOverclock == 4);
		boolean maxAmmoModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[10] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, maxAmmoModified);
		toReturn[11] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, timeModified || selectedOverclock == 2);
		toReturn[12] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 2);
		toReturn[13] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedOverclock == 4, selectedOverclock == 4);
		toReturn[14] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedOverclock == 0, selectedOverclock == 0);
		toReturn[15] = new StatsRow("Full Lock Fear Factor:", 2.5, modIcons.fear, selectedTier5 == 2, selectedTier5 == 2);
		toReturn[16] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), modIcons.blowthrough, selectedTier3 == 2, selectedTier3 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return selectedOverclock == 3;
	}
	
	@Override
	protected void setAoEEfficiency() {
		if (selectedOverclock == 3) {
			aoeEfficiency = calculateAverageAreaDamage(3.0, 1.5, 0.5);
		}
		else {
			aoeEfficiency = new double[3];
		}
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage;
		int magSize = getMagazineSize();
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = ((double) magSize) / getCustomRoF();
		}
		else {
			duration = (((double) magSize) / getCustomRoF()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage();
		
		// Damage wasted by Armor
		if (armorWasting && !statusEffects[1]) {
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
		}
		
		// T3.A "Electro-Chemical Rounds" damage increase
		if (selectedTier3 == 0 && (statusEffects[0] || statusEffects[2] || statusEffects[3])) {
			directDamage *= 1.33;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, 0.0, 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage) / duration;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier3 == 2) {
			return calculateSingleTargetDPS(false, false, false, false);
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return (getMagazineSize() + getCarriedAmmo()) * getDirectDamage() * calculateBlowthroughDamageMultiplier(getNumberOfPenetrations());
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getNumberOfPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getCustomRoF();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Because this object only models the auto-aiming feature of SmartRifle, the Hitscan Accuracy has no need to be estimated.
		return -1;
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		// T4.A "Electro-Chemical Rounds" is a x1.33 Damage multiplier vs enemies afflicted by Burning, Electrocute, or IFG
		if (selectedTier3 == 0 && (statusEffects[0] || statusEffects[2] || statusEffects[3])) {
			directDamage[0] *= 1.33;
		}
		
		double[] areaDamage = new double[5];
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															0.0, 1.0, getCustomRoF(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// All of the Utility of this weapon is only accessible via the Lock-On functionality (which this object doesn't model)
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage()) * UtilityInformation.ArmorBreak_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		return getMagazineSize() * getDirectDamage();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, 0.0, 1.0, 0.0, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
