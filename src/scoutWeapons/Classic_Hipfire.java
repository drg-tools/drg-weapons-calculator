package scoutWeapons;

import guiPieces.ButtonIcons.modIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.EnemyInformation;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class Classic_Hipfire extends Classic {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Classic_Hipfire() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Classic_Hipfire(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Classic_Hipfire(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "M1000 Classic (Hipfired)";
	}
	
	@Override
	public Classic_Hipfire clone() {
		return new Classic_Hipfire(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "Classic_Hipfire";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	protected int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 32;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 16;
		}
		else if (selectedOverclock == 3) {
			toReturn += 72;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.635;
		}
		
		return (int) Math.round(toReturn);
	}
	@Override
	protected int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 1) {
			toReturn += 6;
		}
		
		return toReturn;
	}
	@Override
	protected double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedOverclock == 3) {
			toReturn += 3;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[11];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, selectedOverclock == 3 || selectedTier1 == 1);
		
		toReturn[1] = new StatsRow("Clip Size:", getMagazineSize(), modIcons.magSize, selectedTier3 == 1);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[3] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedOverclock == 3);
		
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier5 == 2 || selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 1);
		
		toReturn[6] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 2);
		
		toReturn[7] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier4 == 0, selectedTier4 == 0);
		
		boolean spsModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[8] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, spsModified, spsModified);
		
		toReturn[9] = new StatsRow("Spread Recovery:", convertDoubleToPercentage(getSpreadRecoverySpeed()), modIcons.baseSpread, selectedOverclock == 3, selectedOverclock == 3);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[10] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// Single-target calculations
	@Override
	protected double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = ((double) getMagazineSize()) / getRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage();
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage, getWeakpointBonus());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		// Because this is modeling Hipfired shots, there's no way that the Electrocuting Focus Shots will proc. As such, Electrocution DoT is not modeled.
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage) / duration;
	}

	// Multi-target calculations
	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalDamage = (getMagazineSize() + getCarriedAmmo()) * getDirectDamage();
		
		if (selectedTier4 == 0) {
			return 4.0 * totalDamage;
		}
		else {
			return totalDamage;
		}
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double unchangingBaseSpread = 16;
		double changingBaseSpread = 0;
		double spreadVariance = 138;
		double spreadPerShot = 94;
		double spreadRecoverySpeed = 174;
		double recoilPerShot = 86.83317338;
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 3.0 / 10.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 6.0 / 5.0;
		
		double[] modifiers = {1.0, getSpreadPerShot(), getSpreadRecoverySpeed(), 1.0, getRecoil()};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, accuracyDistance, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval, modifiers);
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			getDirectDamage(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		double[] DoTDamage = {
			0,  // Fire
			0,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, getWeakpointBonus(), 0.0, 0.0, statusEffects[1], statusEffects[3]);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Because almost all of M1k's Utility is based on Focused Shots, Hipfire is pretty meager as Utility goes...
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		return getDirectDamage() * getMagazineSize() * calculateMaxNumTargets();
	}
}
