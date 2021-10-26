package weapons.engineer;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;
import weapons.Weapon;

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
	private double lockonThreshold;
	private double lockonTime;
	private double lockonDuration;
	private int maxNumLockons;
	
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
		directDamage = 20;
		carriedAmmo = 180;
		magazineSize = 36;
		rateOfFire = 6.0;
		reloadTime = 3.0;
		timeBetweenBulletsDuringBurst = 0.08;
		lockonRange = 25.0; // meters
		lockonThreshold = 40.0; // degrees
		lockonTime = 0.25;
		lockonDuration = 4.0;
		maxNumLockons = 12;
		
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
		tier1[0] = new Mod("Increased Caliber Rounds", "+7 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Expanded Ammo Bags", "+72 Max Ammo", modIcons.carriedAmmo, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Macro Lens", "+25m Lock-On Range, -50% Lock-On Threshold", modIcons.aoeRadius, 2, 0, false);
		tier2[1] = new Mod("CCD Array Add-On", "+50% Lock-On Threshold", modIcons.angle, 2, 1, false);
		tier2[2] = new Mod("Zoom Lens", "+10m Lock-On Range", modIcons.distance, 2, 2, false);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Electro-Chemical Rounds", "x1.33 Damage vs enemies afflicted by Electrocution, IFG Grenade, or Burning", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("SM&#7449;T Targeting Software", "Lock-On now prioritizes low-health enemies, and cannot stack more locks on a target than the number of shots required to kill it.", modIcons.light, 3, 1, false);
		tier3[2] = new Mod("Piercing Rounds", "+1 Penetration", modIcons.blowthrough, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Shutter Speed Sensor", "-20% Lock-On Time", modIcons.baseSpread, 4, 0);
		tier4[1] = new Mod("Aperture Extension", "+6 Max Number of Lock-Ons", modIcons.baseSpread, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Electric Generator Mod", "Targets with 3 or more Locks on them get Electrocuted when shot", modIcons.electricity, 5, 0);
		tier5[1] = new Mod("Unstable Lock Mechanism", "Full Lock increases the damage of all shots by 20%", modIcons.directDamage, 5, 1);
		tier5[2] = new Mod("High Frequency Bullet", "Full Lock scares enemies", modIcons.fear, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Armor Break Module", "Armor Breaking on Full Lock", overclockIcons.armorBreaking, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Eraser", "+12 Magazine Size, +6 Max Number of Lock-Ons", overclockIcons.magSize, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Seeker Rounds", "Lock-On shots will always hit their target and ignore Armor, with +33% Lock-On Threshold. In exchange: 25% slower Burst Fire speed and +0.5 Reload Time.", overclockIcons.baseSpread, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Explosive Chemical Rounds", "Targets with 3 or more Locks on them will explode when shot, dealing 15 Area Damage in a 3m radius around them. "
				+ "In exchange: -5 Direct Damage and -36 Max Ammo", overclockIcons.addedExplosion, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Executioner", "+70% Weakpoint Bonus on Full Lock, -50% Lock-On Time, -12 Magazine Size, -12 Max Ammo", overclockIcons.weakpointBonus, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neuro-Lasso", "Each Lock on an enemy slows it down by 5%, up to a maximum of 50%. In exchange: +75% Lock-On Time, Limited Lock-On Duration, and -6 Max Number of Lock-Ons", overclockIcons.slowdown, 5);
		
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
			toReturn += 7;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 5;
		}
		
		return toReturn;
	}
	protected double getAreaDamage() {
		if (selectedOverclock == 3) {
			return 15;
		}
		else {
			return 0;
		}
	}
	protected double getAoERadius() {
		if (selectedOverclock == 3) {
			return 3;
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
			return 0.7;
		}
		else {
			return 0.0;
		}
	}
	protected double getArmorBreaking() {
		if (selectedOverclock == 0) {
			return 6.0;
		}
		else {
			return 1.0;
		}
	}
	protected int getNumberOfPenetrations() {
		if (selectedTier3 == 2) {
			return 1;
		}
		else {
			return 0;
		}
	}
	protected double getTimeBetweenBulletsDuringBurst() {
		double toReturn = timeBetweenBulletsDuringBurst;
		
		if (selectedOverclock == 2) {
			toReturn *= 1.25;
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
	protected double getLockonThreshold() {
		double modifier = 1.0;
		
		if (selectedTier2 == 0) {
			modifier -= 0.5; 
		}
		else if (selectedTier2 == 1) {
			modifier += 0.5;
		}
		
		if (selectedOverclock == 2) {
			modifier += 0.33;
		}
		
		return lockonThreshold * modifier;
	}
	protected double getLockonTime() {
		double modifier = 1.0;
		
		if (selectedTier4 == 0) {
			modifier -= 0.2;
		}
		
		if (selectedOverclock == 4) {
			modifier -= 0.5;
		}
		else if (selectedOverclock == 5) {
			modifier += 0.75;
		}
		
		return lockonTime * modifier;
	}
	protected double getLockonDuration() {
		double toReturn = lockonDuration;
		
		if (selectedOverclock == 5) {
			toReturn -= 2;
		}
		
		return toReturn;
	}
	protected int getMaxNumLockons() {
		int toReturn = maxNumLockons;
		
		if (selectedTier4 == 1) {
			toReturn += 6;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 6;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 6;
		}
		
		return toReturn;
	}
	
	@Override
	public double getRecommendedRateOfFire() {
		return rateOfFire;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[6];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, selectedTier1 == 0 || selectedOverclock == 3);
		toReturn[1] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedOverclock == 1 || selectedOverclock == 4);
		boolean maxAmmoModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, maxAmmoModified);
		toReturn[3] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, false);
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 2);
		toReturn[5] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), modIcons.blowthrough, selectedTier3 == 2, selectedTier3 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
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
		double baseSpread = 0.9;
		double spreadPerShot = 1.4;
		double spreadRecoverySpeed = 8.1;
		double maxBloom = 4.2;
		double minSpreadWhileMoving = 1.0;
		
		double recoilPitch = 35.0;
		double recoilYaw = 5.0;
		double mass = 1.0;
		double springStiffness = 50.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getCustomRoF(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
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
