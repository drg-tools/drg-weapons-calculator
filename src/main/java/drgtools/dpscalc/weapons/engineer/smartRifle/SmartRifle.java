package drgtools.dpscalc.weapons.engineer.smartRifle;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

public class SmartRifle extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double timeBetweenBulletsDuringBurst;
	private double lockonRange;
	private double maxLockonDegree;
	private double loseLockonDegree;
	private double lockonTime;
	private int maxNumLockons;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public SmartRifle() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public SmartRifle(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public SmartRifle(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "LOK-1 Smart Rifle (Hipfired)";
		weaponPic = WeaponPictures.assaultRifle;
		customizableRoF = true;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 21;
		carriedAmmo = 180;
		magazineSize = 36;
		rateOfFire = 6.0;
		reloadTime = 3.0;
		timeBetweenBulletsDuringBurst = 0.06;
		lockonRange = 25.0; // meters
		// Note: these are actual degrees. To compare to Accuracy's terminology, these are equal to "20 Spread" and "140 Spread" respectively.
		maxLockonDegree = 10.0;
		loseLockonDegree = 70.0;
		lockonTime = 0.095;
		maxNumLockons = 12;
		armorBreaking = 1.5;
		
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
		tier1[0] = new Mod("Increased Caliber Rounds", "+6 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Expanded Ammo Bags", "+72 Max Ammo", modIcons.carriedAmmo, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Macro Lens", "x2 Acquire Lock-On Range, x0.2 Acquire Lock-On Threshold", modIcons.baseSpread, 2, 0, false);
		tier2[1] = new Mod("CCD Array Add-On", "+1.3 degrees Acquire Lock-On Threshold, x1.5 Lose Lock-On threshold", modIcons.angle, 2, 1, false);
		tier2[2] = new Mod("Zoom Lens", "+10m Acquire Lock-On Range", modIcons.distance, 2, 2, false);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Electro-Chemical Rounds", "+20% Direct Damage vs enemies afflicted by Electrocution, IFG Grenade, or Burning. "
				+ "The bonus damage element matches the Status Effect's element, extends Burn DoT duration, and can be applied twice per bullet (once for Electric damage, once for Fire damage + Heat).", modIcons.special, 3, 0);
		tier3[1] = new Mod("SM&#7449;T Targeting Software&#8482;", "Lock-On now prioritizes low-health enemies, and cannot stack more locks on a target than the number of shots required to kill it.", modIcons.light, 3, 1, false);
		tier3[2] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Shutter Speed Sensor", "x0.85 Lock-On Time", modIcons.duration, 4, 0);
		tier4[1] = new Mod("Aperture Extension", "+6 Max Number of Lock-Ons", modIcons.numTargets, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Electric Generator Mod", "Targets with 3 or more Locks on them get Electrocuted when the burst gets fired. The Electrocute DoT does 12 Electic-element Damage per Second, slows enemies by 80%, and lasts for 3 seconds.", modIcons.electricity, 5, 0);
		tier5[1] = new Mod("Unstable Lock Mechanism", "Full Lock increases the damage of all shots by 20%", modIcons.directDamage, 5, 1);
		tier5[2] = new Mod("Fear Frequency", "Every time you release a Lock-On burst, the last bullet fired emits an aura of Fear to all enemies near you. For every bullet fired during the Lock-On burst, add 0.15 Fear to the aura (ranges from 15% Base Fear Chance at 1 Lock "
				+ "to 360% Base Fear Chance at 24 Locks). The aura's radius starts at 2.4m and each bullet fired during the Lock-On burst increases it by +0.15m (ranges from 2.55m at 1 Lock to 6m at 24 Locks).", modIcons.fear, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Armor Break Module", "1250% Armor Breaking on Full Lock", overclockIcons.armorBreaking, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Eraser", "+12 Magazine Size, x1.33 Max Number of Lock-Ons", overclockIcons.magSize, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Seeker Rounds", "Lock-On shots will always hit their target and ignore Armor, with x1.33 Lose Lock-On Threshold. In exchange: 47.37% slower Burst Fire speed and +0.5 Reload Time.", overclockIcons.baseSpread, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Explosive Chemical Rounds", "Targets with 3 or more Locks on them will trigger an explosion on the last shot, dealing 50 Area Damage and 0.5 Fear Factor in a 4m radius around them. "
				+ "In exchange: -5 Direct Damage and -36 Max Ammo", overclockIcons.addedExplosion, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Executioner", "+50% Weakpoint Bonus on Full Lock, x0.5 Lock-On Time, x0.66 Max Number of Lock-Ons, -12 Magazine Size, -12 Max Ammo", overclockIcons.weakpointBonus, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neuro-Lasso", "Each Lock on an enemy slows it down by 10% (x0.9), and the slows multiply together on each enemy. In exchange: x1.15 Lock-On Time and Limited Lock-On Duration of 6 seconds", overclockIcons.slowdown, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public SmartRifle clone() {
		return new SmartRifle(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "SmartRifle_Hipfire";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.engineerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.smartRifleGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	protected double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 6;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 5;
		}
		
		return toReturn;
	}
	protected double getAreaDamage() {
		if (selectedOverclock == 3) {
			// 50 Area Damage, 50% falloff, 4m radius, 2m maxdmgradius, 0.5 fear
			return 50;
		}
		else {
			return 0;
		}
	}
	protected double getAoERadius() {
		if (selectedOverclock == 3) {
			return 4;
		}
		else {
			return 0;
		}
	}
	protected int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 1) {
			toReturn += 72;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 36;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 12;
		}
		
		return toReturn;
	}
	protected int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedOverclock == 1) {
			toReturn += 12;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 12;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		// I need this method for Customizable RoF to work correctly.
		return rateOfFire;
	}
	protected double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	protected double getWeakpointBonus() {
		if (selectedOverclock == 4) {
			return 0.5;
		}
		else {
			return 0.0;
		}
	}
	protected double getArmorBreaking() {
		if (selectedOverclock == 0) {
			return 12.5;
		}
		else {
			return armorBreaking;
		}
	}
	protected int getNumberOfPenetrations() {
		if (selectedTier3 == 2) {
			return 3;
		}
		else {
			return 0;
		}
	}
	protected double getTimeBetweenBulletsDuringBurst() {
		double toReturn = timeBetweenBulletsDuringBurst;
		
		if (selectedOverclock == 2) {
			toReturn *= 1.9;
		}
		
		return toReturn;
	}
	protected double getLockonRange() {
		double toReturn = lockonRange;
		
		if (selectedTier2 == 0) {
			toReturn += 25;
		}
		else if (selectedTier2 == 2) {
			toReturn += 10;
		}
		
		
		return toReturn;
	}
	protected double getMaxLockonDegree() {
		double toReturn = maxLockonDegree;
		
		if (selectedTier2 == 0) {
			toReturn *= 0.2; 
		}
		else if (selectedTier2 == 1) {
			toReturn += 1.3;
		}
		
		return toReturn;
	}
	protected double getLoseLockonDegree() {
		double toReturn = loseLockonDegree;
		
		if (selectedTier2 == 1) {
			toReturn *= 1.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 1.33;
		}
		
		return toReturn;
	}
	protected double getLockonTime() {
		double toReturn = lockonTime;
		
		if (selectedTier4 == 0) {
			toReturn *= 0.85;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 1.15;
		}
		
		return toReturn;
	}
	protected double getLockonDuration() {
		if (selectedOverclock == 5) {
			return 6.0;
		}
		else {
			// This will indicate that it has "infinite" duration, barring the enemy leaving the LoseLockonDegree threshold.
			return -1;
		}
	}
	protected int getMaxNumLockons() {
		double toReturn = maxNumLockons;
		
		if (selectedTier4 == 1) {
			toReturn += 6;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= 1.33;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0.66;
		}
		
		return (int) Math.round(toReturn);
	}
	
	@Override
	public double getRecommendedRateOfFire() {
		return rateOfFire;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[7];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, selectedTier1 == 0 || selectedOverclock == 3);
		toReturn[1] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedOverclock == 1 || selectedOverclock == 4);
		boolean maxAmmoModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, maxAmmoModified);
		toReturn[3] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, false);
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 2);
		toReturn[5] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(armorBreaking), modIcons.armorBreaking, false);
		toReturn[6] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), modIcons.blowthrough, selectedTier3 == 2, selectedTier3 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsRadialDamage() {
		// Without the Lock-On mode and OC "Explosive Chemical Rounds" the SmartRifle can't do AoE damage.
		return false;
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
		if (selectedTier3 == 0) {
			// Currently, the +20% Fire-element + Heat and +20% Electric-element damage components are added separately, so this can add up to +40% damage per shot.
			double multiplier = 1.0;
			
			if (statusEffects[0]) {
				multiplier += 0.2;
			}
			if (statusEffects[2] || statusEffects[3]) {
				multiplier += 0.2;
			}
			
			directDamage *= multiplier;
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
		double baseSpread = 1.3;
		double spreadPerShot = 3.5;
		double spreadRecoverySpeed = 10.0;
		double maxBloom = 5.0;
		double minSpreadWhileMoving = 0.7;  // 3.0 while sprinting
		
		// Technically the SmartRifle can have its RecoilPitch range anywhere from 30 to 40, but for simplicity's sake I'm choosing to use the average of 35.
		double recoilPitch = 35.0;
		double recoilYaw = 5.0;
		double mass = 1.5;
		double springStiffness = 70.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getCustomRoF(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
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
//			if (statusEffects[2] || statusEffects[3]) {
//				directDamage[4] = 0.2 * directDamage[0];
//			}
//		}
//
//		double[] areaDamage = new double[5];
//
//		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
//		double[] dot_dps = new double[4];
//		double[] dot_duration = new double[4];
//		double[] dot_probability = new double[4];
//
//		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability,
//															0.0, armorBreaking, getCustomRoF(), 0.0, 0.0,
//															statusEffects[1], statusEffects[3], false, false);
//		return MathUtils.sum(breakpoints);
		return 0;
	}

	@Override
	public double utilityScore() {
		// All of the Utility of this weapon is only accessible via the Lock-On functionality (which this object doesn't model)
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), armorBreaking) * UtilityInformation.ArmorBreak_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		return getMagazineSize() * getDirectDamage() * calculateBlowthroughDamageMultiplier(getNumberOfPenetrations());
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, 0.0, armorBreaking, 0.0, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
