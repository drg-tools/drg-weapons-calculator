package drgtools.dpscalc.weapons.engineer.shotgun;

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

public class Shotgun extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private int numberOfPellets;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointStunChance;
	private int stunDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Shotgun() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Shotgun(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Shotgun(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Warthog\" Auto 210";
		weaponPic = WeaponPictures.shotgun;
		customizableRoF = true;
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 7;
		numberOfPellets = 8;
		carriedAmmo = 90;
		magazineSize = 6;
		rateOfFire = 2.0;
		reloadTime = 2.0;
		weakpointStunChance = 0.1;
		stunDuration = 3;
		
		// Override default 10m distance
		accEstimator.setDistance(7.0);
		
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
		tier1[0] = new Mod("Supercharged Feed Mechanism", "+1 Rate of Fire", modIcons.rateOfFire, 1, 0);
		tier1[1] = new Mod("Overstuffed Magazine", "+2 Magazine Size", modIcons.magSize, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Ammo Bags", "+42 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Loaded Shells", "+2 Pellets per Shot", modIcons.pelletsPerShot, 2, 1);
		tier2[2] = new Mod("Choke", "x0.5 Base Spread", modIcons.baseSpread, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Recoil Dampener", "x0.4 Recoil", modIcons.recoil, 3, 0);
		tier3[1] = new Mod("Quickfire Ejector", "-0.5 Reload Time", modIcons.reloadSpeed, 3, 1);
		tier3[2] = new Mod("High Capacity Magazine", "+2 Magazine Size", modIcons.magSize, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Tungsten Coated Buckshot", "+400% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Bigger Pellets", "+1 Damage per Pellet", modIcons.directDamage, 4, 1);
		
		// Turret Whip has MaxDmgDadius 1.5m, with damage falloff 50% at 2m radius.
		tier5 = new Mod[2];
		tier5[0] = new Mod("Turret Whip", "Shoot a Turret with the Shotgun to consume 5 turret ammo and fire a projectile in the direction currently being aimed at by the turret, "
				+ "with a 3 second cooldown between projectiles. Each projectile travels at 75 m/sec, does 160 Explosive element Area Damage in a 2m radius, has a 100% chance to stun "
				+ "for 1.5 seconds, inflicts 1.0 Fear, does 50% Friendly Fire damage, and has 200% Armor Breaking.", modIcons.special, 5, 0, false);
		tier5[1] = new Mod("Miner Adjustments", "Changes the Shotgun from semi-automatic to fully automatic, +0.5 Rate of Fire", modIcons.rateOfFire, 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Stunner", "Pellets can now stun an enemy on any body part instead of just weakpoints, and any shots that hit a "
				+ "target that's already stunned deal x1.3 damage.", overclockIcons.stun, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Light-Weight Magazines", "+18 Max Ammo, -0.4 Reload Time", overclockIcons.carriedAmmo, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Magnetic Pellet Alignment", "x0.5 Base Spread, +30% Weakpoint Bonus, x0.75 Rate of Fire", overclockIcons.baseSpread, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Cycle Overload", "+1 Damage per Pellet, +2 Rate of Fire, +0.5 Reload Time, x1.5 Base Spread", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Mini Shells", "+78 Max Ammo, +6 Magazine Size, x0.5 Recoil, -2 Damage per Pellet, and no longer able to stun enemies", overclockIcons.miniShells, 4);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public Shotgun clone() {
		return new Shotgun(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "Shotgun";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.engineerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.shotgunGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		
		if (selectedTier4 == 1) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 2;
		}
		
		return toReturn;
	}
	private int getNumberOfPellets() {
		int toReturn = numberOfPellets;
		
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 42;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 18;
		}
		else if (selectedOverclock == 4) {
			toReturn += 78;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 2;
		}
		if (selectedTier3 == 2) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 6;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier1 == 0) {
			toReturn += 1.0;
		}
		
		if (selectedTier5 == 1) {
			toReturn += 0.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 0.75;
		}
		else if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.5;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.4;
		}
		else if (selectedOverclock == 3) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointStunChance() {
		double toReturn = weakpointStunChance;
		
		// OC "Mini Shells" removes stun chance
		if (selectedOverclock == 4) {
			toReturn = 0;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		
		// OC "Mini Shells" removes stun chance
		if (selectedOverclock == 4) {
			toReturn = 0;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		if (selectedOverclock == 2) {
			return 0.3;
		}
		else {
			return 0;
		}
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 0) {
			return 5.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 2) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn *= 0.4;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		boolean damageModified = selectedTier4 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Damage per Pellet:", getDamagePerPellet(), modIcons.directDamage, damageModified);
		
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPellets(), modIcons.pelletsPerShot, selectedTier2 == 1);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean RoFModified = selectedTier1 == 0 || selectedTier5 == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, RoFModified);
		
		boolean reloadModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, reloadModified);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedOverclock == 2, selectedOverclock == 2);
		
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 0, selectedTier4 == 0);
		
		String stunDescription;
		if (selectedOverclock == 0) {
			stunDescription = "Stun Chance per Pellet:";
		}
		else {
			stunDescription = "Weakpoint Stun Chance per Pellet:";
		}
		toReturn[8] = new StatsRow(stunDescription, convertDoubleToPercentage(getWeakpointStunChance()), modIcons.homebrewPowder, selectedOverclock == 0 || selectedOverclock == 4);
		
		toReturn[9] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, selectedOverclock == 4);
		
		boolean baseSpreadModified = selectedTier2 == 2 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[10] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		boolean recoilModified = selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[11] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	// Adapted from Gunner/Revolver
	@Override
	public double getRecommendedRateOfFire() {
		double recoilPitch = 55 * getRecoil();
		double recoilYaw = 40 * getRecoil();
		double mass = 4.0;
		double springStiffness = 75;
		
		// This number is chosen arbitrarily. It has to be strictly less than the base Recoil's max value times the greatest reduction possible (20%) so that the binary-search doesn't get stuck in an endless loop.
		double desiredIncreaseInRecoil = 1.15;
		double timeToRecoverRecoil = calculateTimeToRecoverRecoil(recoilPitch, recoilYaw, mass, springStiffness, desiredIncreaseInRecoil);
		
		return Math.min(1.0 / timeToRecoverRecoil, getRateOfFire());
	}
	
	private double calculateCumulativeStunChancePerShot() {
		// Because Stunner changes it from weakpoints to anywhere on the body, I'm making the Accuracy change to reflect that.
		double stunAccuracy;
		if (selectedOverclock == 0) {
			stunAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			stunAccuracy = getWeakpointAccuracy() / 100.0;
		}
		int numPelletsThatHaveStunChance = (int) Math.round(getNumberOfPellets() * stunAccuracy);
		if (numPelletsThatHaveStunChance > 0) {
			// Only 1 pellet needs to succeed in order to stun the creature
			return MathUtils.cumulativeBinomialProbability(getWeakpointStunChance(), numPelletsThatHaveStunChance, 1);
		}
		else {
			// This is a special case -- when the Accuracy is so low that none of the pellets are expected to hit a weakpoint, the cumulative binomial probability returns -1, which in turn destroys the Utility Score unnecessarily.
			return 0.0;
		}
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamagePerPellet;
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = ((double) getMagazineSize()) / getCustomRoF();
		}
		else {
			duration = (((double) getMagazineSize()) / getCustomRoF()) + getReloadTime();
		}
		
		double dmgPerPellet = getDamagePerPellet();
		
		// Damage wasted by Armor
		if (armorWasting && !statusEffects[1]) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			dmgPerPellet *= armorWaste;
		}
		
		// Frozen
		if (statusEffects[1]) {
			dmgPerPellet *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			dmgPerPellet *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		if (selectedOverclock == 0) {
			// Stunner OC damage multiplier
			dmgPerPellet *= averageBonusPerMagazineForShortEffects(1.3, 3.0, false, calculateCumulativeStunChancePerShot(), getMagazineSize(), getCustomRoF());
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamagePerPellet = increaseBulletDamageForWeakpoints(dmgPerPellet, getWeakpointBonus(), 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamagePerPellet = dmgPerPellet;
		}
		
		int numPelletsPerShot = getNumberOfPellets();
		double pelletsThatHitWeakpointPerShot = numPelletsPerShot * weakpointAccuracy;
		double pelletsThatHitTargetPerShot = numPelletsPerShot * generalAccuracy - pelletsThatHitWeakpointPerShot;
		
		return (pelletsThatHitWeakpointPerShot * directWeakpointDamagePerPellet + pelletsThatHitTargetPerShot * dmgPerPellet) * getMagazineSize() / duration;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		// Shotgun cannot naturally hit more than one target, unless pellet spread is large enough to hit multiple swarmers. In that case, the DPS is equivalent to the single-target DPS but spread between targets.
		return 0.0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return (double) (getDamagePerPellet() * getNumberOfPellets() * (getMagazineSize() + getCarriedAmmo()));
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1;
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
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet(), getWeakpointBonus()) * getNumberOfPellets();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDamagePerPellet() * getNumberOfPellets());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double horizontalBaseSpread = 12.0 * getBaseSpread();
		double verticalBaseSpread = 6.0 * getBaseSpread();
		double spreadPerShot = 0.0;
		double spreadRecoverySpeed = 12.0;
		double maxBloom = 8.0;
		double minSpreadWhileMoving = 2.0;
		
		double recoilPitch = 55.0 * getRecoil();
		double recoilYaw = 40.0 * getRecoil();
		double mass = 4.0;
		double springStiffness = 75.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getCustomRoF(), getMagazineSize(), 1, 
				horizontalBaseSpread, verticalBaseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving, 
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
//		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
//		double[] directDamage = new double[5];
//		directDamage[0] = getDamagePerPellet() * getNumberOfPellets() * getGeneralAccuracy() / 100.0;  // Kinetic
//
//		double[] areaDamage = new double[5];
//
//		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
//		double[] dot_dps = new double[4];
//		double[] dot_duration = new double[4];
//		double[] dot_probability = new double[4];
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
		double probabilityToBreakLightArmorPlatePerPellet = calculateProbabilityToBreakLightArmor(getDamagePerPellet(), getArmorBreaking());
		double probabilityToBreakLightArmorPlatePerShot = MathUtils.cumulativeBinomialProbability(probabilityToBreakLightArmorPlatePerPellet, getNumberOfPellets(), 1);
		utilityScores[2] = probabilityToBreakLightArmorPlatePerShot * UtilityInformation.ArmorBreak_Utility;
		
		// Fear
		if (selectedTier5 == 0) {
			// Turret Whip projectile does 1.0 Fear Factor in its 2m radius
			utilityScores[4] = calculateFearProcProbability(1.0) * calculateNumGlyphidsInRadius(2.0) * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Stun
		// Weakpoint = 10% stun chance per pellet, 3 sec duration (upgraded with Mod Tier 3 "Stun Duration", or OC "Stunner")
		utilityScores[5] = calculateCumulativeStunChancePerShot() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		if (selectedTier5 == 0) {
			// Turret Whip projectile has 100% chance to stun for 1.5sec in its 2m radius
			utilityScores[5] += calculateNumGlyphidsInRadius(2.0) * 1.5 * UtilityInformation.Stun_Utility;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		return getDamagePerPellet() * getNumberOfPellets() * getMagazineSize();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDamagePerPellet(), getNumberOfPellets(), 0.0, getArmorBreaking(), getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
