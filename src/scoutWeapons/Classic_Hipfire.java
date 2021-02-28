package scoutWeapons;

import guiPieces.customButtons.ButtonIcons.modIcons;
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
	public double getRateOfFire() {
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
		
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier5 == 2 || selectedOverclock == 1 || selectedOverclock == 2);
		
		toReturn[5] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 1);
		
		toReturn[6] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 2);
		
		toReturn[7] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier4 == 0, selectedTier4 == 0);
		
		boolean spsModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[8] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, spsModified, spsModified);
		
		toReturn[9] = new StatsRow("Max Bloom:", convertDoubleToPercentage(getMaxBloom()), modIcons.baseSpread, spsModified, spsModified);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[10] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
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
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus(), 1.0);
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
		return (getMagazineSize() + getCarriedAmmo()) * getDirectDamage() * calculateMaxNumTargets();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Internally M1k starts with BaseSpread = 0, but that's only when fully zoomed in. When in hipfire mode, it has a +1 Base Spread penalty
		double baseSpread = 1.0;
		double spreadPerShot = 3.0 * getSpreadPerShot();
		double spreadRecoverySpeed = 8.5;
		double maxBloom = 5.0 * getMaxBloom();
		double minSpreadWhileMoving = 1.0;
		
		double recoilPitch = 50.0 * getRecoil();
		double recoilYaw = 5.0 * getRecoil();
		double mass = 4.0;
		double springStiffness = 70.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getRateOfFire(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		double[] areaDamage = new double[5];
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), getArmorBreaking(), getRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
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
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, 0.0, getArmorBreaking(), getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
