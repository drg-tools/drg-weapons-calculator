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
	
	/*
	 	Extracted from WPN_MicroMissileLauncher
			MaxHomingProjectiles 9
			MinTracerDistance 250 (2.5m)
				
		Extracted from PRJ_MicroMissile
			AccelerationCurve CRV_MML_Acceleration ???
			MaxPropulsionTime 30
			HomingAccelerationMagnitude 20000 ???
	*/
	
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
		aoeRadius = 1.4;  // meters
		magazineSize = 36;
		carriedAmmo = 288;
		movespeedWhileFiring = 0.5;
		rateOfFire = 3.0;
		reloadTime = 3.5;  // seconds
		startingVelocity = 10.0;
		maxVelocity = 15.0;
		
		// Override default 10m distance for OC "Salvo Module"
		accEstimator.setDistance(5.0);
		
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
		tier1[0] = new Mod("Missile Belts", "+72 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Pressurized Gas Cylinder", "+4 Direct Damage", modIcons.directDamage, 1, 1);
		tier1[2] = new Mod("Increased Blast Radius", "+0.6m AoE Radius", modIcons.aoeRadius, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Bigger Jet Engine", "+5 m/sec Max Velocity, x2 Turn Rate", modIcons.projectileVelocity, 2, 0, false);
		tier2[1] = new Mod("Anti-Tank Missiles", "x2 Armor Breaking", modIcons.armorBreaking, 2, 1);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Nano Missiles", "x2 Magazine Size", modIcons.magSize, 3, 0);
		tier3[1] = new Mod("Improved Feed Mechanism", "+1 Rate of Fire", modIcons.rateOfFire, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Shrapnel Load", "+50% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("Zip Fuel", "+4 Area Damage", modIcons.areaDamage, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Napalm-Infused Rounds", "Converts 33% of damage to Heat", modIcons.heatDamage, 5, 0);
		tier5[1] = new Mod("Uncontrolled Decompression", "25% Chance to Stun enemies for 3 seconds", modIcons.stun, 5, 1);
		tier5[2] = new Mod("Nitroglycerin Compound", "For every 0.75 seconds that a missile is flying through the air, it gains +1 Area Damage (up to a maximum of +10).", modIcons.lastShotDamage, 5, 2, false);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Manual Guidance Cutoff", "Releasing the trigger disables the guidance system. Additionally, x1.33 Max Velocity.", overclockIcons.rollControl, 0, false);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Overtuned Feed Mechanism", "x1.2 Max Velocity, +1 Rate of Fire", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Fragmentation Missiles", "+2 Area Damage, +0.5m AoE Radius", overclockIcons.aoeRadius, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Plasma Burster Missiles", "Missiles are no longer destroyed when they impact enemies. Each missile can damage enemies up to 5 times, and there's a maximum of 18 missiles "
				+ "in the air at once, and each missile has a lifetime of 20 seconds. Additionally: x1.3 Turn Rate, x0.5 Direct Damage, x0.5 Area Damage, x0.5 AoE Radius, x0.75 Max Velocity, -108 Max Ammo", overclockIcons.blowthrough, 3);
		// TODO: i should model how the 0.9 sec arming time works, and how not every missile fired will hit an enemy or turn into a mine with bonus damage/radius.
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Minelayer System", "When missiles impact terrain, they transform into mines that will detonate when enemies get too close. Mines have a 15 second lifetime, do x2.75 Area Damage, "
				+ "have a x1.5 AoE Radius and trigger when enemies get within 2m. In exchange, you can no longer guide the missiles, x0 Turn Rate, and -36 Max Ammo.", overclockIcons.special, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Jet Fuel Homebrew", "x2.5 Direct Damage, x1.5 Max Velocity, increases Starting Velocity to Max, x0.5 Area Damage, -0.5m AoE Radius, x0.75 Magazine Size, -72 Max Ammo", overclockIcons.projectileVelocity, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Salvo Module", "Hold down the trigger to load up to 9 missiles into a single shot. Salvo Missiles have their Starting Velocity and Max Velocity increased to 20 m/sec by default. "
				+ "For each missile added to the salvo, all missiles deal more damage up to +4/+4 at 9 rockets. In exchange, manual guidance is disabled for all missiles in the salvo.", overclockIcons.numPellets2, 6, false);
		
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
		
		if (selectedTier5 == 0) {
			toReturn *= 0.67;
		}
		
		if (selectedOverclock == 6) {
			// TODO: figure out Salvo Module's damage bonus. i know it's +4/+4 at 9 rockets, but haven't yet figured out the scaling.
			toReturn += 4;
		}
		
		// Multiplicative bonuses last
		if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 2.5;
		}
		
		return toReturn;
	}
	private double getAreaDamage() {
		double toReturn = areaDamage;
		
		if (selectedTier4 == 1) {
			toReturn += 4;
		}
		
		if (selectedTier5 == 0) {
			toReturn *= 0.67;
		}
		else if (selectedTier5 == 2) {
			// TODO: figure out how to model T5.C's +1/0.75 sec mechanic, and how it interacts with the two OCs' multiplicative boosts.
			toReturn += 1;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 2;
		}
		else if (selectedOverclock == 3 || selectedOverclock == 5) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 4) {
			// the Minelets left behind get x2.75 Area Damage
			toReturn *= 2.75;
		}
		else if (selectedOverclock == 6) {
			toReturn += 4;
		}
		
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		
		if (selectedTier1 == 2) {
			toReturn += 0.6;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		else if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 4) {
			// the Minelets left behind get x1.5 AoE Radius
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 0.5;
		}
		
		return toReturn;
	}
	private double getMaxDmgRadius() {
		if (selectedOverclock == 3) {
			return 0.01;
		}
		else {
			return 1.0;
		}
	}
	private int getMagazineSize() {
		double toReturn = magazineSize;
		
		if (selectedTier3 == 0) {
			toReturn *= 2;
		}
		
		if (selectedOverclock == 5) {
			toReturn *= 0.75;
		}
		
		return (int) toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 72;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 108;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 36;
		}
		else if (selectedOverclock == 5) {
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
			return 2.0;
		}
		else {
			return 1.0;
		}
	}
	private double getStartingVelocity() {
		if (selectedOverclock == 5) {
			// Jet Fuel sets Start speed = Max speed
			return getMaxVelocity();
		}
		else if (selectedOverclock == 6) {
			// Salvo Rockets start at 20 m/sec
			return 20.0;
		}
		else {
			return startingVelocity;
		}
	}
	private double getMaxVelocity() {
		double baseSpeed = maxVelocity;
		
		if (selectedOverclock == 6) {
			// Salvo Rockets have their Max Speed increased to 20 m/sec
			baseSpeed = 20.0;
		}
		
		if (selectedTier2 == 0) {
			baseSpeed += 5.0;
		}
		
		double modifier = 1.0;
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
		
		return baseSpeed * modifier;
	}
	private double getTurnRate() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 0) {
			toReturn *= 2.0;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.3;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		// Technically it looks like Salvo Module has the 50% Base Spread bonus, too, but then it also has the massive penalties when charging up the shots.
		if (selectedOverclock == 0 || selectedOverclock == 4) {
			return 0.5;
		}
		else if (selectedOverclock == 5) {
			return 0.2;
		}
		else {
			return 1.0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[16];
		
		boolean directDamageModified = selectedTier1 == 1 || selectedTier5 == 0 || selectedTier5 == 2 || selectedOverclock == 3 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamageModified = selectedTier4 == 1 || selectedTier5 == 0 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 4 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier1 == 2 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), modIcons.aoeRadius, aoeRadiusModified);
		toReturn[3] = new StatsRow("Starting Velocity:", getStartingVelocity(), modIcons.projectileVelocity, selectedOverclock == 5 || selectedOverclock == 6);
		
		boolean maxVelocityModified = selectedTier2 == 0 || selectedOverclock == 0 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[4] = new StatsRow("Max Velocity:", getMaxVelocity(), modIcons.projectileVelocity, maxVelocityModified);
		
		boolean turnRateModified = selectedTier2 == 0 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[5] = new StatsRow("Turn Rate:", convertDoubleToPercentage(getTurnRate()), modIcons.projectileVelocity, turnRateModified);
		toReturn[6] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedTier3 == 0 || selectedOverclock == 5);
		
		boolean maxAmmoModified = selectedTier1 == 0 || selectedOverclock == 3 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[7] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, maxAmmoModified);
		toReturn[8] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier3 == 1 || selectedOverclock == 1);
		toReturn[9] = new StatsRow("Reload Time:", reloadTime, modIcons.reloadSpeed, false);
		toReturn[10] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0, selectedTier4 == 0);
		toReturn[11] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier2 == 1, selectedTier2 == 1);
		
		boolean stunEnabled = selectedTier5 == 1;
		toReturn[12] = new StatsRow("Stun Chance:", convertDoubleToPercentage(0.25), modIcons.homebrewPowder, stunEnabled, stunEnabled);
		toReturn[13] = new StatsRow("Stun Duration:", 3, modIcons.stun, stunEnabled, stunEnabled);
		
		boolean baseSpreadModified = selectedOverclock == 0 || selectedOverclock == 4 || selectedOverclock == 5;  // selectedOverclock == 6?
		toReturn[14] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[15] = new StatsRow("Movement Speed While Using: (m/sec)", MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2), modIcons.movespeed, false);
		
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
		aoeEfficiency = calculateAverageAreaDamage(getAoERadius(), getMaxDmgRadius(), 0.5);
	}
	
	private double calculateTimeToLoadFullSalvo() {
		/*
			WPN_MicroMissileLauncher
				BuckShotDelay 0.5   <-- This value seems unused?
				ChargedProjectileLauncher
					ProjectileChangeChargeValue 0.11
					
			GetAll WPN_MicroMissileLauncher_C ChargeTime
				Returns 2.0  at RoF 3
				Returns 1.25 at RoF 4 (from T3.B)
		*/
		if (selectedTier3 == 1) {
			return 1.25;
		}
		else {
			return 2.0;
		}
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
		// I'm choosing to model the DPS of OC "Salvo Module" as if the player loads 9 rockets for every salvo to maximize damage.
		int magSize = getMagazineSize();
		double timeToFireMagazine;
		if (selectedOverclock == 6) {
			timeToFireMagazine = calculateTimeToLoadFullSalvo() * magSize / 9.0;
		}
		else {
			timeToFireMagazine = magSize / getRateOfFire();
		}
		
		double duration, burnDPS = 0;
		if (burst) {
			duration = timeToFireMagazine;
			
			if (selectedTier5 == 0 && !statusEffects[1]) {
				double timeToIgnite = averageTimeToCauterize();
				double burnDoTUptime = (duration - timeToIgnite) / duration;
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
		}
		else {
			duration = timeToFireMagazine + reloadTime;
			
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
		double timeToFireMagazineAndReload;
		if (selectedOverclock == 6) {
			timeToFireMagazineAndReload = calculateTimeToLoadFullSalvo() * magSize / 9.0 + reloadTime;
		}
		else {
			timeToFireMagazineAndReload = magSize / getRateOfFire() + reloadTime;
		}
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
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double magSize = getMagazineSize();
		double carriedAmmo = getCarriedAmmo();
		double damagePerRocket = directDamage + areaDamage * aoeEfficiency[2] * aoeEfficiency[1];
		double baseDamage = damagePerRocket * (magSize + carriedAmmo);
		
		double burnDamage = 0;
		if (selectedTier5 == 0) {
			if (selectedOverclock == 6) {
				// Because T5.A converts 33% of damage to Heat, I just need to take 1/2 of the total damage (67% / 33% = 2.03)
				double burstOfHeat = (4 * directDamage + 9 * areaDamage * aoeEfficiency[1]) / 2.03;
				double percentageIgnitedPerBurst = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(burstOfHeat);
				double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				double numEnemiesHitPerSalvo = aoeEfficiency[2] * 1.5;  // This is a pure guess. I'm running out of time so I just have to get it done and move on.
				double numSalvos = (magSize / 9.0) * (1 + carriedAmmo / magSize);
				burnDamage = numSalvos * numEnemiesHitPerSalvo * percentageIgnitedPerBurst * fireDoTDamagePerEnemy;
			}
			else {
				// Because T5.A converts 33% of damage to Heat, I just need to take 1/2 of the total damage (67% / 33% = 2.03)
				double avgHeatPerRocket = damagePerRocket / (2.03 * aoeEfficiency[2]);
				double timeToIgnite = EnemyInformation.averageTimeToIgnite(0, avgHeatPerRocket, getRateOfFire(), 0);
				double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				double estimatedNumEnemiesKilled = aoeEfficiency[2] * (calculateFiringDuration() / averageTimeToKill());
				burnDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
			}
		}
		
		return baseDamage + burnDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return (int) aoeEfficiency[2];
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine;
		if (selectedOverclock == 6) {
			timeToFireMagazine = calculateTimeToLoadFullSalvo() * magSize / 9.0;
		}
		else {
			timeToFireMagazine = magSize / getRateOfFire();
		}
		
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
		/*
			WPN_MicroMissileLauncher.ChargedProjectileLauncher.VerticalSpread and .HorizontalSpread
			Base Spread: 10
			
			Manual Guidance Cutoff and Minelayer System both have x0.5 Base Spread,
			Jet Fuel Homebrew has a x0.2 Base Spread
			Salvo Module adds +6 Horizontal degrees and +4 Vertical degrees (12 and 8 spread, respectively
			
			RecoilPitch: 3
			RecoilYaw: 2
			CanRecoilDown: false
			SpringStiffness: 50
			Mass: 1
		*/
		if (selectedOverclock == 6) {
			return accEstimator.calculateRectangularAccuracy(weakpointAccuracy, 22.0, 18.0, 3, 2, 1.0, 50);
		}
		else {
			return -1.0;
		}
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		double aDamage = getAreaDamage();
		double[] areaDamage = new double[5];
		if (selectedOverclock == 3) {
			// OC "Plasma Burster Missiles" changes the Radial Damage from Explosive to Fire/Electric 
			areaDamage[2] = 0.5 * aDamage;  // Fire
			areaDamage[4] = 0.5 * aDamage;  // Electric
		}
		else {
			areaDamage[1] = aDamage;  // Explosive
		}
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		double heatPerShot = 0;
		if (selectedTier5 == 0) {
			// Because T5.A converts 33% of damage to Heat, I just need to take 1/2 of the total damage (67% / 33% = 2.03)
			heatPerShot = (getDirectDamage() + getAreaDamage()) / 2.03;
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
			// Because T5.A converts 33% of damage to Heat, I just need to take 1/2 of the total damage (67% / 33% = 2.03)
			double heatPerRocket = (getDirectDamage() + getAreaDamage()) / 2.03;
			return EnemyInformation.averageTimeToIgnite(0, heatPerRocket, getRateOfFire(), 0);
		}
		else {
			return -1;
		}
	}

	@Override
	public double damagePerMagazine() {
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double magSize = getMagazineSize();
		double damagePerRocket = directDamage + areaDamage * aoeEfficiency[2] * aoeEfficiency[1];
		double baseDamage = damagePerRocket * magSize;
		
		double burnDamage = 0;
		if (selectedTier5 == 0) {
			if (selectedOverclock == 6) {
				// Because T5.A converts 33% of damage to Heat, I just need to take 1/2 of the total damage (67% / 33% = 2.03)
				double burstOfHeat = (4 * directDamage + 9 * areaDamage * aoeEfficiency[1]) / 2.03;
				double percentageIgnitedPerBurst = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(burstOfHeat);
				double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				double numEnemiesHitPerSalvo = aoeEfficiency[2] * 1.5;  // This is a pure guess. I'm running out of time so I just have to get it done and move on.
				double numSalvos = magSize / 9.0;
				burnDamage = numSalvos * numEnemiesHitPerSalvo * percentageIgnitedPerBurst * fireDoTDamagePerEnemy;
			}
			else {
				// Because T5.A converts 33% of damage to Heat, I just need to take 1/2 of the total damage (67% / 33% = 2.03)
				double avgHeatPerRocket = damagePerRocket / (2.03 * aoeEfficiency[2]);
				double timeToIgnite = EnemyInformation.averageTimeToIgnite(0, avgHeatPerRocket, getRateOfFire(), 0);
				double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
				double estimatedNumEnemiesKilled = aoeEfficiency[2] * (calculateFiringDuration() / averageTimeToKill());
				burnDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
			}
		}
		
		return baseDamage + burnDamage;
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
