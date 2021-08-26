package weapons.gunner;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;
import weapons.Weapon;

public class GuidedRocketLauncher extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double areaDamage;
	private double aoeRadius;
	private int magazineSize;
	private int carriedAmmo;
	private double movespeedWhileFiring;
	private double rateOfFire;
	private double reloadTime;
	private double startingVelocity;
	private double maxVelocity;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public GuidedRocketLauncher() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public GuidedRocketLauncher(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public GuidedRocketLauncher(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Hurricane\" Guided Rocket System";
		weaponPic = WeaponPictures.autocannon;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 16;
		areaDamage = 20;
		aoeRadius = 1.5;  // meters
		magazineSize = 36;
		carriedAmmo = 288;
		movespeedWhileFiring = 0.5;
		rateOfFire = 3.0;
		reloadTime = 3.0;  // seconds
		startingVelocity = 10.0;
		maxVelocity = 20.0;
		
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
		tier1[0] = new Mod("Missile Round Belts", "+72 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Pressurized Gas Cylinder", "+4 Direct Damage", modIcons.directDamage, 1, 1);
		tier1[2] = new Mod("Increased Blast Radius", "+0.5m AoE Radius", modIcons.aoeRadius, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Bigger Jet Engine", "+33% Max Velocity, +100% Turn Rate", modIcons.projectileVelocity, 2, 0, false);
		tier2[1] = new Mod("Anti-Tank Missiles", "+300% Armor Breaking", modIcons.armorBreaking, 2, 1);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Nano Missiles", "+36 Magazine Size", modIcons.magSize, 3, 0);
		tier3[1] = new Mod("Improved Feed Mechanism", "+1 Rate of Fire", modIcons.rateOfFire, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Shrapnel Load", "+50% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("Zip Fuel", "+5 Area Damage", modIcons.areaDamage, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Napalm-Infused Rounds", "Adds 50% of damage as Heat", modIcons.heatDamage, 5, 0);
		tier5[1] = new Mod("Uncontrolled Decompression", "25% Chance to Stun enemies for 3 seconds", modIcons.stun, 5, 1);
		tier5[2] = new Mod("Nitroglycerin Compound", "Rockets do more damage the longer they're airborne", modIcons.special, 5, 2, false);  // TODO: get the correct image; I think I remember seeing this as an older version of the image of Max Ammo.
																																			// TODO: find the damage bonus vs distance traveled.
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Manual Guidance Cutoff", "Releasing the trigger disables the guidance system. Additionally, x1.33 Max Velocity.", overclockIcons.rollControl, 0, false);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Overtuned Feed Mechanism", "x1.2 Max Velocity, +1 Rate of Fire", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Fragmentation Missiles", "+2 Area Damage, +0.5m AoE Radius", overclockIcons.aoeRadius, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Plasma Burster Missiles", "Missiles are no longer destroyed when they impact enemies. Additionally: +100% Turn Rate, "
				+ "x0.25 Direct Damage, x0.5 AoE Radius, x0.75 Max Velocity", overclockIcons.blowthrough, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Minelayer System", "When missiles impact terrain, they transform into mines that will detonate when enemies get too close. Mines have a 10 second lifetime and can be detonated by damage. "
				+ "In exchange, you can no longer guide the missiles, x0 Turn Rate, and -72 Max Ammo.", overclockIcons.special, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Jet Fuel Homebrew", "x1.5 Max Velocity, increases Starting Velocity to Max, x1.5 Direct Damage, x0.5 Area Damage, -0.5m AoE Radius, -18 Magazine Size, -72 Max Ammo", overclockIcons.projectileVelocity, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Salvo Module", "Hold down the trigger to load up to 9 missiles into a single shot. For each missile added to the burst, "
				+ "all missiles get faster and deal more damage. In exchange, manual guidance is disabled.", overclockIcons.rateOfFire, 6, false);  // TODO: find the damage+velocity bonus per rocket in the burst
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public GuidedRocketLauncher clone() {
		return new GuidedRocketLauncher(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "GuidedRocketLauncher";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.gunnerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.guidedRocketLauncherGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedTier1 == 1) {
			toReturn += 4;
		}
		
		// Multiplicative bonuses last
		if (selectedOverclock == 3) {
			toReturn *= 0.25;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getAreaDamage() {
		double toReturn = areaDamage;
		
		// Additive bonuses first
		if (selectedTier4 == 1) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 2;
		}
		// Multiplicative bonuses last
		else if (selectedOverclock == 5) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		
		if (selectedTier1 == 2) {
			toReturn += 0.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		else if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 0.5;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 0) {
			toReturn += 36;
		}
		
		if (selectedOverclock == 5) {
			toReturn -= 18;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 72;
		}
		
		if (selectedOverclock == 4 || selectedOverclock == 5) {
			toReturn -= 72;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier3 == 1) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		if (selectedTier4 == 0) {
			return 0.5;
		}
		else {
			return 0;
		}
	}
	private double getArmorBreaking() {
		if (selectedTier2 == 1) {
			return 4.0;
		}
		else {
			return 1.0;
		}
	}
	private double getStartingVelocity() {
		if (selectedOverclock == 5) {
			return getMaxVelocity();
		}
		else {
			return startingVelocity;
		}
	}
	private double getMaxVelocity() {
		double modifier = 1.0;
		
		if (selectedTier2 == 0) {
			modifier += 0.33;
		}
		
		if (selectedOverclock == 0) {
			modifier *= 1.33;
		}
		else if (selectedOverclock == 1) {
			modifier *= 1.2;
		}
		else if (selectedOverclock == 3) {
			modifier *= 0.75;
		}
		else if (selectedOverclock == 5) {
			modifier *= 1.5;
		}
		
		return maxVelocity * modifier;
	}
	private double getTurnRate() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 0) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamageModified = selectedTier4 == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier1 == 2 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), modIcons.aoeRadius, aoeRadiusModified);
		toReturn[3] = new StatsRow("Starting Velocity:", getStartingVelocity(), modIcons.projectileVelocity, selectedOverclock == 5);
		
		boolean maxVelocityModified = selectedTier2 == 0 || selectedOverclock == 0 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Max Velocity:", getMaxVelocity(), modIcons.projectileVelocity, maxVelocityModified);
		
		boolean turnRateModified = selectedTier2 == 0 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[5] = new StatsRow("Turn Rate:", convertDoubleToPercentage(getTurnRate()), modIcons.projectileVelocity, turnRateModified);
		toReturn[6] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedTier3 == 0 || selectedOverclock == 5);
		
		boolean maxAmmoModified = selectedTier1 == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[7] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, maxAmmoModified);
		toReturn[8] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier3 == 1 || selectedOverclock == 1);
		toReturn[9] = new StatsRow("Reload Time:", reloadTime, modIcons.reloadSpeed, false);
		toReturn[10] = new StatsRow("Weakpoint Bonus:", convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0, selectedTier4 == 0);
		toReturn[11] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier2 == 1, selectedTier2 == 1);
		
		boolean stunEnabled = selectedTier5 == 1;
		toReturn[12] = new StatsRow("Stun Chance:", convertDoubleToPercentage(0.25), modIcons.homebrewPowder, stunEnabled, stunEnabled);
		toReturn[13] = new StatsRow("Stun Duration:", 3, modIcons.stun, stunEnabled, stunEnabled);
		
		toReturn[14] = new StatsRow("Movement Speed While Using: (m/sec)", MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2), modIcons.movespeed, false);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return true;
	}
	
	@Override
	protected void setAoEEfficiency() {
		aoeEfficiency = calculateAverageAreaDamage(getAoERadius(), 0.75, 0.5);
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		int magSize = getMagazineSize();
		double RoF = getRateOfFire();
		
		double duration, burnDPS = 0;
		if (burst) {
			duration = magSize / RoF;
			
			if (selectedTier5 == 0 && !statusEffects[1]) {
				double timeToIgnite = averageTimeToCauterize();
				double burnDoTUptime = (duration - timeToIgnite) / duration;
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
		}
		else {
			duration = magSize / RoF + reloadTime;
			
			if (selectedTier5 == 0 && !statusEffects[1]) {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
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
			areaDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double directWeakpointDamage;
		if (weakpoint && !statusEffects[1]) {
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus());
		}
		else {
			directWeakpointDamage = directDamage;
		}
		
		return (magSize * (directWeakpointDamage + areaDamage)) / duration + burnDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		double magSize = (double) getMagazineSize();
		double timeToFireMagazineAndReload = (magSize / getRateOfFire()) + reloadTime;
		double areaDamage = getAreaDamage();
		
		double areaDamagePerMag = areaDamage * aoeEfficiency[1] * magSize;
		double sustainedAdditionalDPS = areaDamagePerMag / timeToFireMagazineAndReload;
		
		if (selectedTier5 == 0) {
			sustainedAdditionalDPS += DoTInformation.Burn_DPS;
		}
		
		return sustainedAdditionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// TODO: Burn DoT
		return (getDirectDamage() + getAreaDamage() * aoeEfficiency[2] * aoeEfficiency[1]) * (getMagazineSize() + getCarriedAmmo());
	}

	@Override
	public int calculateMaxNumTargets() {
		return (int) aoeEfficiency[2];
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = magSize / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * reloadTime;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		double[] areaDamage = new double[5];
		areaDamage[1] = getAreaDamage();  // Explosive
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		double heatPerShot = 0;
		if (selectedTier5 == 0) {
			heatPerShot = (getDirectDamage() + getAreaDamage()) / 2.0;
		}
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), getArmorBreaking(), getRateOfFire(), heatPerShot, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		double AB = getArmorBreaking();
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double directDamageAB = calculateProbabilityToBreakLightArmor(directDamage + areaDamage, AB);
		double areaDamageAB = calculateProbabilityToBreakLightArmor(aoeEfficiency[1] * areaDamage, AB);
		// Average out the Area Damage Breaking and Direct Damage Breaking
		utilityScores[2] = (directDamageAB + (aoeEfficiency[2] - 1) * areaDamageAB) * UtilityInformation.ArmorBreak_Utility / aoeEfficiency[2];
		
		// Stun
		if (selectedTier5 == 1) {
			utilityScores[5] = 0.25 * aoeEfficiency[2] * 3.0 * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedTier5 == 0) {
			double heatPerRocket = (getDirectDamage() + getAreaDamage()) / 2.0;
			return EnemyInformation.averageTimeToIgnite(0, heatPerRocket, getRateOfFire(), 0);
		}
		else {
			return -1;
		}
	}

	@Override
	public double damagePerMagazine() {
		// TODO: Burn DoT
		double damagePerBullet = getDirectDamage() + getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		return damagePerBullet * getMagazineSize();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public double damageWastedByArmor() {
		double weakpointAccuracy = EnemyInformation.probabilityBulletWillHitWeakpoint() * 100.0;
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), getArmorBreaking(), getWeakpointBonus(), 100.0, weakpointAccuracy);
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
