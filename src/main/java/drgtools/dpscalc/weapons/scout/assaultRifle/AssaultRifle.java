package drgtools.dpscalc.weapons.scout.assaultRifle;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.GuiConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.DoTInformation;
import drgtools.dpscalc.modelPieces.DwarfInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

public class AssaultRifle extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double weakpointStunChance;
	private double stunDuration;
	private double reloadTime;
	private double weakpointBonus;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public AssaultRifle() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public AssaultRifle(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public AssaultRifle(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Deepcore GK2";
		weaponPic = WeaponPictures.assaultRifle;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 16;
		carriedAmmo = 360;
		magazineSize = 30;
		rateOfFire = 8.0;
		weakpointStunChance = 0.15;
		stunDuration = 1.5;
		reloadTime = 1.8;
		weakpointBonus = 0;
		
		accEstimator.setSpreadCurve(new AssaultRifle_SpreadCurve());
		
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
		tier1[0] = new Mod("Gyro Stabilisation", "x0 Base Spread", modIcons.baseSpread, 1, 0);
		tier1[1] = new Mod("Supercharged Feed Mechanism", "+1 Rate of Fire", modIcons.rateOfFire, 1, 1);
		tier1[2] = new Mod("Quickfire Ejector", "x0.73 Reload Time", modIcons.rateOfFire, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Increased Caliber Rounds", "+3 Direct Damage", modIcons.directDamage, 2, 0);
		tier2[1] = new Mod("Expanded Ammo Bags", "+120 Max Ammo", modIcons.carriedAmmo, 2, 1);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Floating Barrel", "x0.5 Recoil", modIcons.recoil, 3, 0);
		tier3[1] = new Mod("High Capacity Magazine", "+10 Magazine Size", modIcons.magSize, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hollow-Point Bullets", "+20% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("Hardened Rounds", "+500% Armor Breaking", modIcons.armorBreaking, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Battle Frenzy", "After killing an enemy, gain +50% Movement Speed and increase Spread Recovery Speed by x12.5 for 2.5 seconds", modIcons.movespeed, 5, 0);
		tier5[1] = new Mod("Improved Gas System", "+2 Rate of Fire", modIcons.baseSpread, 5, 1);
		tier5[2] = new Mod("Stun", "+35% chance to Stun on Weakpoint hit", modIcons.stun, 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Ammo", "+5 Magazine Size, x0.7 Recoil", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Gas Rerouting", "+1 Rate of Fire, x0.84 Reload Time", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, overclockIcons.homebrewPowder, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Overclocked Firing Mechanism", "+3 Rate of Fire, x2 Recoil", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Bullets of Mercy", "+50% Damage dealt to enemies that are burning, electrocuted, poisoned, stunned, or frozen. In exchange, x0.6 Magazine Size", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "AI Stability Engine", "x0 Recoil, x2.11 Spread Recovery Speed, +50% Weakpoint Bonus, -1 Direct Damage, -2 Rate of Fire", overclockIcons.baseSpread, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Electrifying Reload", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 3 Electric Damage to the enemy upon reloading, in addition to afflicting that " +
				"enemy with an Electrocute DoT that does an average of"  + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " per Second and slows enemies by 80% for 6 seconds. " +
				"In exchange, -10 Magazine Size and -60 Max Ammo.", overclockIcons.specialReload, 6);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public AssaultRifle clone() {
		return new AssaultRifle(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "AssaultRifle";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.assaultRifleGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// First do additive bonuses
		if (selectedTier2 == 0) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 5) {
			toReturn -= 1;
		}
		
		// Then do multiplicative bonuses
		if (selectedOverclock == 2) {
			toReturn *= homebrewPowderCoefficient;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 1) {
			toReturn += 120;
		}

		if(selectedOverclock == 6) {
			toReturn -= 60;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 1) {
			toReturn += 10;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 5;
		}
		else if (selectedOverclock == 4) {
			toReturn = (int) Math.round(toReturn * 0.6);
		}
		else if (selectedOverclock == 6) {
			toReturn -= 10;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier1 == 1) {
			toReturn += 1.0;
		}
		if (selectedTier5 == 1) {
			toReturn += 2.0;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 3) {
			toReturn += 3.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getWeakpointStunChance() {
		double toReturn = weakpointStunChance;
		
		if (selectedTier5 == 2) {
			toReturn += 0.35;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;

		if (selectedTier1 == 2) {
			toReturn *= 0.73;
		}

		if (selectedOverclock == 1) {
			toReturn *= 0.84;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 0) {
			toReturn += 0.2;
		}
		
		if (selectedOverclock == 5) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 1) {
			return 6.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		if (selectedTier1 == 0) {
			return 0.0;
		}
		else {
			return 1.0;
		}
	}
	private double getSpreadRecoverySpeed() {
		// I'm choosing to model it as if these two effects do not stack.
		if (selectedOverclock == 5) {
			return 2.11;
		}
		else if (selectedTier5 == 1) {
			// According to the MikeGSG, Battle Cool increases Spread Recovery Speed by x12.5 for 1.5 seconds after a kill.
			return averageBonusPerMagazineForShortEffects(12.5, 1.5, true, 0.0, getMagazineSize(), getRateOfFire());
		}
		else {
			return 1.0;
		}
	}
	private double getSpreadRecoverySpeedValue() {
		// I'm choosing to model it as if these two effects do not stack.
		if (selectedOverclock == 5) {
			return 17.1;
		}
		else if (selectedTier5 == 1) {
			// According to the MikeGSG, Battle Cool increases Spread Recovery Speed by x12.5 for 1.5 seconds after a kill.
			return 8.1 * averageBonusPerMagazineForShortEffects(12.5, 1.5, true, 0.0, getMagazineSize(), getRateOfFire());
		}
		else {
			return 8.1;
		}
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 0) {
			toReturn *= 0.7;
		}
		else if (selectedOverclock == 3) {
			toReturn *= 2.0;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		boolean directDamageModified = selectedTier2 == 0  || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean magSizeModified = selectedTier3 == 2 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		toReturn[2] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, selectedTier2 == 1 || selectedOverclock == 6);
		
		boolean rofModified = selectedTier1 == 1 || selectedTier5 == 1 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[3] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, rofModified);
		
		toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 2 || selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0 || selectedOverclock == 5);
		
		toReturn[6] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 1, selectedTier4 == 1);
		
		toReturn[7] = new StatsRow("Weakpoint Stun Chance:", convertDoubleToPercentage(getWeakpointStunChance()), modIcons.homebrewPowder, selectedTier5 == 2);
		
		toReturn[8] = new StatsRow("Stun Duration:", stunDuration, modIcons.stun, false);
		
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, selectedTier1 == 0, selectedTier1 == 0);
		
		boolean SRSmodified = selectedTier5 == 0 || selectedOverclock == 5;
		toReturn[10] = new StatsRow("Spread Recovery:", convertDoubleToPercentage(getSpreadRecoverySpeed()), modIcons.baseSpread, SRSmodified, SRSmodified);
		
		boolean recoilModified = selectedTier3 == 0 || selectedOverclock == 0 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[11] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsRadialDamage() {
		return false;
	}
	
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
		
		// Bullets of Mercy OC damage increase
		if (selectedOverclock == 4) {
			double BoMDamageMultiplier = 1.33;
			if (statusEffects[0] || statusEffects[1] || statusEffects[2] || statusEffects[3]) {
				directDamage *= BoMDamageMultiplier;
			}
			else {
				// If no Status Effects are active, then it only procs on the weapon's built-in Stun.
				directDamage *= averageBonusPerMagazineForShortEffects(BoMDamageMultiplier, 1.5, false, getWeakpointStunChance(), getMagazineSize(), getRateOfFire());
			}
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
		
		double electroDPS = 0;
		if (selectedOverclock == 6) {
			double electroDoTUptimeCoefficient = Math.min(6.0 / duration, 1);
			electroDPS += electroDoTUptimeCoefficient * DoTInformation.Electro_DPS;
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage) / duration + electroDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		// Deepcore can't hit any additional targets in a single shot
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalDamage = (getMagazineSize() + getCarriedAmmo()) * getDirectDamage();
		
		double electrocutionDoTTotalDamage = 0;
		if (selectedOverclock == 6) {
			double electrocuteDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, 6.0, DoTInformation.Electro_DPS);
			double estimatedNumEnemiesKilled = calculateFiringDuration() / averageTimeToKill();
			
			electrocutionDoTTotalDamage = electrocuteDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return totalDamage + electrocutionDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// Deepcore can't hit any additional targets in a single shot
		return 1;
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
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double baseSpread = 0.9 * getBaseSpread();
		double spreadPerShot = 1.4;
		double spreadRecoverySpeed = getSpreadRecoverySpeedValue();
		double maxBloom = 4.2;
		double minSpreadWhileMoving = 1.0;
		
		double recoilPitch = 35.0 * getRecoil();
		double recoilYaw = 5.0 * getRecoil();
		double mass = 1.0;
		double springStiffness = 50.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getRateOfFire(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
//		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
//		double[] directDamage = new double[5];
//		directDamage[0] = getDirectDamage();  // Kinetic
//
//		// OC "Bullets of Mercy" is a x1.33 Damage multiplier vs enemies afflicted by Status Effects.
//		if (selectedOverclock == 4 && (statusEffects[0] || statusEffects[1] || statusEffects[2] || statusEffects[3])) {
//			directDamage[0] *= 1.33;
//		}
//
//		double[] areaDamage = new double[5];
//
//		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
//		double[] dot_dps = new double[4];
//		double[] dot_duration = new double[4];
//		double[] dot_probability = new double[4];
//
//		if (selectedOverclock == 6) {
//			dot_dps[0] = DoTInformation.Electro_DPS;
//			dot_duration[0] = 6.0;
//			dot_probability[0] = 1.0;
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
		// Mod Tier 5 "Battle Frenzy" grants a 50% movespeed increase on kill for 2.5 seconds
		if (selectedTier5 == 0) {
			// Again, using incorrect "guess" Spawn Rates to create believable uptimeCoefficient
			double uptimeCoefficient = Math.min(2.5 / averageTimeToKill(false), 1);
			utilityScores[0] = uptimeCoefficient * MathUtils.round(0.5 * DwarfInformation.walkSpeed, 2) * UtilityInformation.Movespeed_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Electrifying Reload" = 100% chance to electrocute on reload
		if (selectedOverclock == 6) {
			// This formula is entirely made up. It's designed to increase number electrocuted with Mag Size, and decrease it with Rate of Fire.
			int numEnemiesElectrocutedPerMagazine = (int) Math.ceil(2.0 * getMagazineSize() / getRateOfFire());
			utilityScores[3] = numEnemiesElectrocutedPerMagazine * 6.0 * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Innate Weakpoint stun = 10% chance for 1.5 sec stun (improved to 40% by Mod Tier 5 "Stun")
		utilityScores[5] = (getWeakpointAccuracy() / 100.0) * getWeakpointStunChance() * stunDuration * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		double baseDamage = getMagazineSize() * getDirectDamage();
		
		double electrocutionDoTDamage = 0;
		if (selectedOverclock == 6) {
			electrocutionDoTDamage = calculateAverageDoTDamagePerEnemy(0, 4.0, DoTInformation.Electro_DPS);
		}
		
		return baseDamage + electrocutionDoTDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, 0.0, getArmorBreaking(), getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
