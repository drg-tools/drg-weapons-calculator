package weapons.scout;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;
import weapons.Weapon;

/*
	Extracted via UUU:
		ShockWaveLength: 150
		Radius: 150
		Distance: 250
		
		ShotgunJump Force: 1000
		Fear Factor Radius: 500
*/

public class Boomstick extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private int numberOfPellets;
	private int frontalConeDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double stunChance;
	private double stunDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Boomstick() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Boomstick(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Boomstick(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Jury-Rigged Boomstick";
		weaponPic = WeaponPictures.boomstick;
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 12;
		numberOfPellets = 8;
		frontalConeDamage = 20;
		carriedAmmo = 24;
		magazineSize = 2;
		rateOfFire = 1.5;
		reloadTime = 2.0;
		stunChance = 0.3;
		stunDuration = 2.5;
		
		// Override default 10m distance
		accEstimator.setDistance(4.0);
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+8 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Double-Sized Buckshot", "+3 Damage per Pellet", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Double Trigger", "+7.5 Rate of Fire", modIcons.rateOfFire, 2, 0);
		tier2[1] = new Mod("Quickfire Ejector", "-0.7 Reload Time", modIcons.reloadSpeed, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Stun", "+20% Stun Chance per Pellet, +3.5 seconds Stun duration", modIcons.stun, 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "+12 Max Ammo", modIcons.carriedAmmo, 3, 1);
		tier3[2] = new Mod("High Capacity Shells", "+3 Pellets per Shot", modIcons.pelletsPerShot, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Tungsten Coated Buckshot", "+300% Armor Breaking", modIcons.armorBreaking, 4, 1);
		tier4[2] = new Mod("Improved Blast Wave", "+20 Blastwave Damage to any enemies in the area extending 4m infront of you.", modIcons.special, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Auto Reload", "Reloads automatically when unequipped for more than 5 seconds", modIcons.reloadSpeed, 5, 0, false);
		tier5[1] = new Mod("Fear The Boomstick", "Deal 0.5 Fear to all enemies within 5m of you every time you pull the trigger", modIcons.fear, 5, 1);
		tier5[2] = new Mod("White Phosphorous Shells", "Convert 50% of Pellet and Blastwave damage to Fire element and add 50% of the Damage per Pellet and Blastwave damage as Heat which can ignite enemies, dealing " + 
		MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) + " Fire Damage per Second", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Shells", "+6 Max Ammo, -0.2 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Double Barrel", "Fire both barrels with a single tigger pull as a 2-round burst. Additionally, +1 Damage per Pellet.", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Special Powder", "Jump off of the ground and fire the shotgun to \"blast jump\", which adds 13 m/sec to your velocity.", overclockIcons.shotgunJump, 2);
		overclocks[3] = new Overclock(Overclock.classification.clean, "Stuffed Shells", "+1 Damage per Pellet, +1 Pellet per Shot", overclockIcons.pelletsPerShot, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Shaped Shells", "-50% Base Spread, -4 Max Ammo", overclockIcons.baseSpread, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Jumbo Shells", "+8 Damage per Pellet, -10 Max Ammo, +0.5 Reload Time", overclockIcons.directDamage, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public Boomstick clone() {
		return new Boomstick(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Boomstick";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.boomstickGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		
		if (selectedTier1 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1 || selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 5) {
			toReturn += 8;
		}
		
		return toReturn;
	}
	private int getNumberOfPellets() {
		int toReturn = numberOfPellets;
		
		if (selectedTier3 == 2) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private int getBlastwaveDamage() {
		// Hits enemies within 4m in front of Scout
		// Explosive-element Area-type damage, instead of direct damage. Bulks + Dreads resist it.
		int toReturn = frontalConeDamage;
		
		if (selectedTier4 == 2) {
			toReturn += 20;
		}
		
		return toReturn;
	}
	private int getBurstSize() {
		// OC "Double Barrel" makes the Boomstick fire both barrels as a 2-shot burst at 20 RoF.
		if (selectedOverclock == 1) {
			return 2;
		}
		else {
			return 0;
		}
	}
	private int getMagazineSize() {
		return magazineSize;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 8;
		}
		if (selectedTier3 == 1) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 6;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 4;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 10;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 0) {
			toReturn += 7.5;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 0) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 5) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getStunChancePerPellet() {
		double toReturn = stunChance;
		
		if (selectedTier3 == 0) {
			toReturn += 0.2;
		}
		
		return toReturn;
	}
	private double getStunDuration() {
		double toReturn = stunDuration;
		
		if (selectedTier3 == 0) {
			toReturn += 3.5;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 1) {
			return 4.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		if (selectedOverclock == 4) {
			return 0.5;
		}
		else {
			return 1.0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[14];
		
		boolean damageModified = selectedTier1 == 1 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Damage per Pellet:", getDamagePerPellet(), modIcons.directDamage, damageModified);
		
		boolean pelletsModified = selectedTier3 == 2 || selectedOverclock == 3;
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPellets(), modIcons.pelletsPerShot, pelletsModified);
		
		toReturn[2] = new StatsRow("Blastwave Damage:", getBlastwaveDamage(), modIcons.areaDamage, selectedTier4 == 2);
		
		// Only display this row when OC "Double Barrel" is equipped
		toReturn[3] = new StatsRow("Burst Size:", getBurstSize(), modIcons.rateOfFire, selectedOverclock == 1, selectedOverclock == 1);
		
		toReturn[4] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, false);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedTier3 == 1 || selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[6] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier2 == 0);
		
		boolean reloadTimeModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock == 5;
		toReturn[7] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, reloadTimeModified);
		
		toReturn[8] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 1, selectedTier4 == 1);
		
		toReturn[9] = new StatsRow("Fear Factor:", 0.5, modIcons.fear, selectedTier5 == 1, selectedTier5 == 1);
		
		toReturn[10] = new StatsRow("Stun Chance per Pellet:", convertDoubleToPercentage(getStunChancePerPellet()), modIcons.homebrewPowder, selectedTier3 == 0);
		
		toReturn[11] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, selectedTier3 == 0);
		
		toReturn[12] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[13] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, selectedOverclock == 4, selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Technically the Blastwave is Area Damage, but this flag is for spherical Area Damage like Grenade Launcher or Autocannon.
		return false;
	}
	
	// Copied over from Engineer/Shotgun
	private double calculateCumulativeStunChancePerShot() {
		double stunAccuracy = getGeneralAccuracy() / 100.0;
		int numPelletsThatHaveStunChance = (int) Math.round(getNumberOfPellets() * stunAccuracy);
		if (numPelletsThatHaveStunChance > 0) {
			// Only 1 pellet needs to succeed in order to stun the creature
			return MathUtils.cumulativeBinomialProbability(getStunChancePerPellet(), numPelletsThatHaveStunChance, 1);
		}
		else {
			// This is a special case -- when the Accuracy is so low that none of the pellets are expected to hit a weakpoint, the cumulative binomial probability returns -1, which in turn destroys the Utility Score unnecessarily.
			return 0.0;
		}
	}
	
	private double calculateTimeToIgnite(boolean accuracy) {
		// This method gets used by the Tier 5 Mod "White Phosphorous Shells"
		int numPelletsThatApplyHeat;
		if (accuracy) {
			numPelletsThatApplyHeat = (int) Math.round(getGeneralAccuracy() * getNumberOfPellets() / 100.0);
		}
		else {
			numPelletsThatApplyHeat = getNumberOfPellets();
		}
		
		// 50% of Direct Damage from the pellets gets added on as Heat Damage.
		double heatDamagePerShot = 0.5 * (getDamagePerPellet() * numPelletsThatApplyHeat + getBlastwaveDamage());
		if (selectedOverclock == 1) {
			// Double Barrel fires both barrels in a 2-shot burst at 20 RoF.
			return EnemyInformation.averageTimeToIgnite(0, 2.0 * heatDamagePerShot, 1.0 / getReloadTime(), 0);
		}
		else {
			return EnemyInformation.averageTimeToIgnite(0, heatDamagePerShot, getRateOfFire(), 0);
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
		
		if (selectedOverclock == 1) {
			// Because OC "Double Barrel" fires both barrels in a 2-shot burst at 20 RoF, it only takes 0.05 seconds to expend both shots and before it needs to reload.
			duration = 0.05 + getReloadTime();
		}
		else {
			if (burst) {
				duration = ((double) getMagazineSize()) / getRateOfFire();
			}
			else {
				duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			}
		}
		
		double dmgPerPellet = getDamagePerPellet();
		double blastwaveDamage = getBlastwaveDamage();
		
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
			blastwaveDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamagePerPellet = increaseBulletDamageForWeakpoints(dmgPerPellet, 0.0, 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamagePerPellet = dmgPerPellet;
		}
		
		// They way it's currently modeled, any time the WPS mod and Double Barrel OC are equipped simultaneously, then the Reload Time doesn't affect the Fire DoT Uptime.
		double burnDPS = 0;
		if (selectedTier5 == 2 && !statusEffects[1]) {
			if (burst) {
				double timeToIgnite = calculateTimeToIgnite(accuracy);
				double fireDoTUptimeCoefficient = (duration - timeToIgnite) / duration;
				
				burnDPS = fireDoTUptimeCoefficient * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		int numPelletsPerShot = getNumberOfPellets();
		double pelletsThatHitWeakpointPerShot = numPelletsPerShot * weakpointAccuracy;
		double pelletsThatHitTargetPerShot = numPelletsPerShot * generalAccuracy - pelletsThatHitWeakpointPerShot;
		
		return (pelletsThatHitWeakpointPerShot * directWeakpointDamagePerPellet + pelletsThatHitTargetPerShot * dmgPerPellet + blastwaveDamage) * getMagazineSize() / duration + burnDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		double magSize = getMagazineSize();
		double secondaryDamagePerShot;
		if (selectedTier4 == 0) {
			secondaryDamagePerShot = getDamagePerPellet() * getNumberOfPellets() + getBlastwaveDamage();
		}
		else {
			secondaryDamagePerShot = getBlastwaveDamage();
		}
		
		double additionalDPS = 0;
		if (selectedOverclock == 1) {
			additionalDPS = secondaryDamagePerShot * magSize / (0.05 + getReloadTime());
		}
		else {
			double timeToFireMagazineAndReload = (magSize / getRateOfFire()) + getReloadTime();
			additionalDPS = secondaryDamagePerShot * magSize / timeToFireMagazineAndReload;
		}
		
		// Penetrations can ignite, too
		if (selectedTier4 == 0 && selectedTier5 == 2) {
			additionalDPS += DoTInformation.Burn_DPS;
		}
		
		return additionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int directDamagePerShot = getDamagePerPellet() * getNumberOfPellets();
		// The frontal blastwave is a 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
		int gruntsHitByBlastwave = 4;
		int blastwaveDamagePerShot = gruntsHitByBlastwave * getBlastwaveDamage();
		double multitargetDamageMultiplier = calculateBlowthroughDamageMultiplier(getMaxPenetrations());
		int numShots = getMagazineSize() + getCarriedAmmo();
		double totalDamage = numShots * (directDamagePerShot*multitargetDamageMultiplier + blastwaveDamagePerShot);
		
		double fireDoTTotalDamage = 0;
		if (selectedTier5 == 2) {
			
			double estimatedNumEnemiesKilled = multitargetDamageMultiplier * (calculateFiringDuration() / averageTimeToKill());
			double fireDoTDamagePerEnemy;
			if (selectedOverclock == 1) {
				// Double barrel fires twice in a row, so it's double the heat of half the damage. Works out to just damage = heat.
				double percentageOfEnemiesIgnitedPerShot = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(directDamagePerShot + getBlastwaveDamage());
				fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				
				fireDoTTotalDamage += numShots * (percentageOfEnemiesIgnitedPerShot * multitargetDamageMultiplier) * fireDoTDamagePerEnemy;
			}
			else {
				double timeBeforeIgnite = calculateTimeToIgnite(false);
				fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				
				fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
			}
		}
		
		return totalDamage + fireDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		
		double timeToFireMagazine = 0;
		if (selectedOverclock == 1) {
			timeToFireMagazine = 0.05;
		}
		else {
			timeToFireMagazine = ((double) magSize) / getRateOfFire();
		}
		
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet()) * getNumberOfPellets() + getBlastwaveDamage();
		if (selectedOverclock == 1) {
			// Because the player cannot shoot only one shot with Double Barrel, I'm choosing to double the damage per shot to penalize this method accordingly.
			dmgPerShot *= 2.0;
		}
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDamagePerPellet() * getNumberOfPellets() + getBlastwaveDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Even though this gun does have significant recoil, it recovers from that recoil entirely in 0.5 seconds. Rather than make an overly 
		// complicated model for 2 shots, I'm just going to use the accuracy for a single shot.
		double horizontalBaseSpread = 35.0 * getBaseSpread();
		double verticalBaseSpread = 10.0 * getBaseSpread();
		double recoilPitch = 120.0;
		double recoilYaw = 10.0;
		double mass = 2.0;
		double springStiffness = 100.0;
		
		return accEstimator.calculateRectangularAccuracy(weakpointAccuracy, horizontalBaseSpread, verticalBaseSpread, recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		double direct = getDamagePerPellet() * getNumberOfPellets() * getGeneralAccuracy() / 100.0;
		
		// Because Accuracy affects these Breakpoints, I'm choosing to implement Asher's suggestion to only add Blastwave damage when AccuracyEstimator.distance <= 4
		double area;
		if (accEstimator.getDistance() <= 4.0) {
			area = getBlastwaveDamage();
		}
		else {
			area = 0;
		}
		
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		double[] areaDamage = new double[5];
		
		// According to Elythnwaen, White Phosphorus Shells not only adds 50% of kinetic + explosive damage to Heat, it also converts 50% to Fire.
		if (selectedTier5 == 2) {
			directDamage[0] = 0.5 * direct;  // Kinetic
			directDamage[2] = 0.5 * direct;  // Fire
			
			areaDamage[1] = 0.5 * area;  // Explosive
			areaDamage[2] = 0.5 * area;  // Fire
		}
		else {
			directDamage[0] = direct;  // Kinetic
			areaDamage[1] = area;  // Explosive
		}
		
		double heatPerShot = 0;
		if (selectedTier5 == 2) {
			heatPerShot = 0.5 * (direct + area);
		}
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															0.0, getArmorBreaking(), getRateOfFire(), heatPerShot, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}
	@Override
	public double utilityScore() {
		// OC "Special Powder" adds 13 m/sec to your velocity
		if (selectedOverclock == 2) {
			utilityScores[0] = 13 * UtilityInformation.BlastJump_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Light Armor Breaking probability
		double probabilityToBreakLightArmorPlatePerPellet = calculateProbabilityToBreakLightArmor(getDamagePerPellet(), getArmorBreaking());
		double probabilityToBreakLightArmorPlatePerShot = MathUtils.cumulativeBinomialProbability(probabilityToBreakLightArmorPlatePerPellet, getNumberOfPellets(), 1);
		utilityScores[2] = probabilityToBreakLightArmorPlatePerShot * UtilityInformation.ArmorBreak_Utility;
		
		// Mod Tier 5 "Fear the Boomstick" = 0.5 Fear to enemies within 5m
		if (selectedTier5 == 1) {
			// A 5m radius returns 41 grunts, which is just too many. I'm choosing to reduce the radius by half, which brings it down to 12.
			int gruntsHitByBlastwave = calculateNumGlyphidsInRadius(5.0 / 2.0);
			double probabilityToFear = calculateFearProcProbability(0.5);
			utilityScores[4] = probabilityToFear * gruntsHitByBlastwave * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Innate Stun = 30% chance per pellet for 2.5 sec (improved by T3.A)
		utilityScores[5] = calculateCumulativeStunChancePerShot() * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedTier5 == 2) {
			return calculateTimeToIgnite(false);
		}
		else {
			return -1;
		}
	}
	
	@Override
	public double damagePerMagazine() {
		// 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
		int gruntsHitByBlastwave = 4;
		return getMagazineSize() * (getDamagePerPellet() * getNumberOfPellets() * calculateBlowthroughDamageMultiplier(getMaxPenetrations()) + getBlastwaveDamage() * gruntsHitByBlastwave);
	}
	
	@Override
	public double timeToFireMagazine() {
		if (selectedOverclock == 1) {
			return 0.05;
		}
		else {
			return getMagazineSize() / getRateOfFire();
		}
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDamagePerPellet(), getNumberOfPellets(), getBlastwaveDamage(), getArmorBreaking(), 0.0, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
