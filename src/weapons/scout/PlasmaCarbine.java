package weapons.scout;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
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

public class PlasmaCarbine extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double rateOfFire;
	private int batteryCapacity;
	private double heatPerShot;
	private double maxHeat;
	private double coolingRate;
	private double cooldownDelay;
	private double overheatDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public PlasmaCarbine() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public PlasmaCarbine(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public PlasmaCarbine(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Drak-25 Plasma Carbine";
		weaponPic = WeaponPictures.assaultRifle;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 9.0;
		rateOfFire = 13.0;
		batteryCapacity = 800;
		heatPerShot = 0.021;  // 0.055 if using active cooling like EPC regular shots
		maxHeat = 1.0;
		coolingRate = 0.5;
		cooldownDelay = 0.3;
		overheatDuration = 2.5;
		
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
		tier1[0] = new Mod("High-Volume Plasma Feed", "+3 Rate of Fire", modIcons.rateOfFire, 1, 0);
		tier1[1] = new Mod("Improved Thermals", "-25% Heat per Shot", modIcons.coolingRate, 1, 1);
		tier1[2] = new Mod("Stronger Particle Accelerator", "+50% Projectile Velocity", modIcons.projectileVelocity, 1, 2, false);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Larger Battery", "+250 Battery Capacity", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Increased Particle Density", "+2 Direct Damage", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Custom Coil Alignment", "-70% Base Spread", modIcons.baseSpread, 3, 0);
		tier3[1] = new Mod("Gen 2 Cooling System", "+50% Cooling Rate, -0.1 sec Cooldown Delay", modIcons.coolingRate, 3, 1);
		tier3[2] = new Mod("Hot Feet", "Move faster while the weapon is Overheated", modIcons.movespeed, 3, 2, false);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Overcharged PCF", "30% Chance to inflict an Electrocute DoT which does " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric "
				+ "Damage per Second and slows enemies by 80% for " + DoTInformation.Electro_SecsDuration + " seconds.", modIcons.electricity, 4, 0, false);
		tier4[1] = new Mod("Plasma Splash", "-5 Direct Damage, +5 Area Damage", modIcons.addedExplosion, 4, 1);
		tier4[2] = new Mod("Destructive Resonance Amp", "+200% Armor Breaking", modIcons.armorBreaking, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Manual Heat Dump", "Press the Reload button to manually activate the Overheat mode, for faster cooling", modIcons.specialReload, 5, 0, false);
		tier5[1] = new Mod("Thermal Feedback Loop", "When the Heat Meter is greater than 50%, the Rate of Fire is increased by +7.6.", modIcons.special, 5, 1, false);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Impact Deflection", "Projectiles are bouncy", overclockIcons.ricochet, 0, false);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Thermal Liquid Coolant", "+25% Cooling Rate, -15% Heat per Shot", overclockIcons.coolingRate, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Rewiring Mod", "Regenerate Ammo while Overheated. In exchange, -320 Battery Capacity", overclockIcons.carriedAmmo, 2, false);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Aggressive Venting", "Upon Overheating, Burn and Fear enemies around you. Additionally, reduces Overheat by -0.7 sec. In exchange, -160 Battery Capacity and -30% Cooling Rate", overclockIcons.special, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Overtuned Particle Accelerator", "+8 Direct Damage, -160 Battery Capacity, +50% Heat per Shot, +133% Base Spread", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Shield Battery Booster", "While Shield is full, increased Direct Damage? and +150% Projectile Velocity. In exchange, -160 Battery Capacity, -50% Cooling Rate, "
				+ "+2.5 Overheat Duration, and +50% Heat per Shot. Additionally, when the weapon Overheats it disables your shield until the Overheat ends.", overclockIcons.damageResistance, 5, false);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Thermal Exhaust Feedback", "After the Heat Meter exceeds 50%, every additional 10% on the Heat Meter adds 5 Fire Damage and 5 Heat to every projectile, "
				+ "up to +25 Damage/Heat at 90%. In exchange, +1.3 sec Overheat Duration and +20% Heat per Shot.", overclockIcons.heatDamage, 6, false);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public PlasmaCarbine clone() {
		return new PlasmaCarbine(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "PlasmaCarbine";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.plasmaCarbineGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// First do additive bonuses
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		if (selectedTier4 == 1) {
			toReturn -= 5;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 8;
		}
		else if (selectedOverclock == 5) {
			// It wasn't listed in Equipment Terminal what the bonus damage was...
		}
		else if (selectedOverclock == 6) {
			/*
				This will be one of the most complex things to model about this gun. After I figure out the cooling rate, heat per shot, and max heat, then I can start modeling how many shots 
				will be fired in each of the 6 tiers before it overheats and then average that bonus damage across the last half of the "magazine". This will get complicated by T5.B's increased RoF.
				
				After some calculus, it averages out to +7.5 Damage+Heat per Particle across the entire "magazine".
			*/
			toReturn += 7.5;
		}
		
		// Then do multiplicative bonuses
		
		return toReturn;
	}
	private double getAreaDamage() {
		if (selectedTier4 == 1) {
			// TODO: still need to get MaxDamageRadius, DamageRadius, and MinDamagePercent from somewhere.
			return 5;
		}
		else {
			return 0;
		}
	}
	private double getAoERadius() {
		if (selectedTier4 == 1) {
			return 0.75;
		}
		else {
			return 0;
		}
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier1 == 0) {
			toReturn += 3.0;
		}
		
		return toReturn;
	}
	private int getBatteryCapacity() {
		int toReturn = batteryCapacity;
		
		if (selectedTier2 == 0) {
			toReturn += 250;
		}
		
		if (selectedOverclock == 2) {
			toReturn -= 320;
		}
		else if (selectedOverclock > 2 && selectedOverclock < 6) {
			toReturn -= 160;
		}
		
		return toReturn;
	}
	private double getHeatPerShot() {
		double modifier = 1.0;
		
		if (selectedTier1 == 1) {
			modifier -= 0.25;
		}
		
		if (selectedOverclock == 1) {
			modifier -= 0.15;
		}
		else if (selectedOverclock == 4 || selectedOverclock == 5) {
			modifier += 0.5;
		}
		else if (selectedOverclock == 6) {
			modifier += 0.2;
		}
		
		return heatPerShot * modifier;
	}
	private double getCoolingRate() {
		double modifier = 1.0;
		
		if (selectedTier3 == 1) {
			modifier += 0.5;
		}
		
		if (selectedOverclock == 1) {
			modifier += 0.25;
		}
		else if (selectedOverclock == 3) {
			modifier -= 0.3;
		}
		else if (selectedOverclock == 5) {
			modifier -= 0.5;
		}
		
		return coolingRate * modifier;
	}
	private double getCooldownDelay() {
		double toReturn = cooldownDelay;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.1;
		}
		
		return toReturn;
	}
	private double getOverheatDuration() {
		double toReturn = overheatDuration;
		
		if (selectedOverclock == 3) {
			toReturn -= 0.7;
		}
		else if (selectedOverclock == 5) {
			toReturn += 2.5;
		}
		else if (selectedOverclock == 6) {
			toReturn += 1.3;
		}
		
		return toReturn;
	}
	private double getProjectileVelocity() {
		double toReturn = 1.0;
		
		if (selectedTier1 == 2) {
			toReturn += 0.5;
		}
		
		if (selectedOverclock == 5) {
			// Need some way to model this interaction with Shield...
			toReturn += 1.5;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 2) {
			return 3.0;
		}
		else {
			return 1.0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 1.33;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[17];
		
		boolean directDamageModified = selectedTier2 == 1 || selectedTier4 == 1 || selectedOverclock > 3;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamage = selectedTier4 == 1;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamage, areaDamage);
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), modIcons.aoeRadius, areaDamage, areaDamage);
		toReturn[3] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getProjectileVelocity()), modIcons.projectileVelocity, selectedTier1 == 2 || selectedOverclock == 5, selectedTier1 == 2 || selectedOverclock == 5);
		
		boolean heatPerShotModified = selectedTier1 == 1 || selectedOverclock == 1 || selectedOverclock > 3;
		toReturn[4] = new StatsRow("Heat/Shot:", getHeatPerShot(), modIcons.blank, heatPerShotModified);
		// TODO: I'll need to investigate if this has "active cooling" like EPC or only cools when not firing, like Minigun and Drills.
		toReturn[5] = new StatsRow("Shots Fired Before Overheating:", calculateNumShotsFiredBeforeOverheating(), modIcons.magSize, heatPerShotModified || selectedTier1 == 0 || selectedTier5 == 1);
		
		boolean batteryCapacityModified = selectedTier2 == 0 || (selectedOverclock > 1 && selectedOverclock < 6); 
		toReturn[6] = new StatsRow("Battery Capacity:", getBatteryCapacity(), modIcons.carriedAmmo, batteryCapacityModified);
		toReturn[7] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier1 == 0);
		boolean heatRoF = selectedTier5 == 1;
		toReturn[8] = new StatsRow("Rate of Fire at High Heat:", getRateOfFire() + 7.6, modIcons.rateOfFire, heatRoF, heatRoF);  // TODO: find the real RoF value, and replace "+2"
		toReturn[9] = new StatsRow("Average Rate of Fire:", calculateAverageRoF(), modIcons.rateOfFire, heatRoF, heatRoF);
		
		boolean coolingRateModified = selectedTier3 == 1 || selectedOverclock % 2 == 1;
		toReturn[10] = new StatsRow("Cooling Rate:", getCoolingRate(), modIcons.coolingRate, coolingRateModified);
		toReturn[11] = new StatsRow("Cooldown Delay:", getCooldownDelay(), modIcons.duration, selectedTier3 == 1);
		toReturn[12] = new StatsRow("Cooldown period (no Overheat):", calculateCoolingPeriod(), modIcons.hourglass, coolingRateModified);
		
		boolean overheatModified = selectedOverclock == 3 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[13] = new StatsRow("Overheat Duration:", getOverheatDuration(), modIcons.coolingRate, overheatModified);
		toReturn[14] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 2, selectedTier4 == 2);
		// Maybe add Electrocute from T4.A and/or Fear from OC "AV" here?
		toReturn[15] = new StatsRow("Bouncy Projectiles:", -1, modIcons.ricochet, selectedOverclock == 0, selectedOverclock == 0);
		toReturn[16] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, selectedTier3 == 0 || selectedOverclock == 4, selectedTier3 == 0 || selectedOverclock == 4);
		// Check to see if T3.A has a Max Bloom bonus listed, and if OC "OPA" has any hidden Accuracy penalties
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private double calculateAverageRoF() {
		// Early exit condition: if T5.B isn't equipped, then this is the same as the base RoF
		if (selectedTier5 != 1) {
			return getRateOfFire();
		}
		
		double baseRoF = getRateOfFire();
		double highHeatBonusRoF = 7.6;  // This might be x1.6 RoF? Hard to tell with only two missions in the vod.
		double magSize = calculateNumShotsFiredBeforeOverheating();
		
		double firstHalf = Math.ceil(magSize / 2.0);
		double lastHalf = Math.floor(magSize / 2.0);
		
		return (firstHalf * baseRoF + lastHalf * (baseRoF + highHeatBonusRoF)) / magSize;
	}
	
	private double calculateNumShotsFiredBeforeOverheating() {
		/*
		double k = getCoolingRate();
		double h = getHeatPerShot();
		
		double exactAnswer;
		double RoF = getRateOfFire();
		if (selectedTier5 == 1) {
			double higherRoF = RoF + 7.6;
			// The bonus RoF makes this calculation a little harder to do.
			exactAnswer = ((0.5 * maxHeat * RoF) / (h * RoF - k)) + ((0.5 * maxHeat * higherRoF) / (h * higherRoF - k));
		}
		else {
			exactAnswer = (maxHeat * RoF) / (h * RoF - k);
		}
		
		// Don't let this return a mag size larger than the battery size.
		return Math.min((int) Math.ceil(exactAnswer), getBatteryCapacity());
		*/
		
		// There are two options: either the Cooling Rate applies between bullets, or (more likely) it doesn't.
		// If it only cools down after the player stops firing, then the "magazine size" is just ceil(maxHeat/heatPerShot)
		// Subtract 1 so that it doesn't Overheat.
		return Math.ceil(maxHeat / getHeatPerShot()) - 1.0;
	}
	
	private double calculateCoolingPeriod() {
		return getCooldownDelay() + maxHeat / getCoolingRate();
	}

	@Override
	public boolean currentlyDealsSplashDamage() {
		return selectedTier4 == 1;
	}
	
	@Override
	protected void setAoEEfficiency() {
		if (selectedTier4 == 1) {
			// TODO: get the real values
			aoeEfficiency = calculateAverageAreaDamage(0.75, 0.5, 0.25);
		}
		else {
			aoeEfficiency = new double[3];
		}
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage, magSize, RoF;
		
		magSize = calculateNumShotsFiredBeforeOverheating();
		RoF = calculateAverageRoF();
		
		// Depending on how it handles, I might not model Accuracy for Drak25...
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		double electrocuteDPS = 0, burnDPS = 0;
		if (burst) {
			duration = magSize / RoF;
			
			if (selectedTier4 == 0) {
				electrocuteDPS = calculateRNGDoTDPSPerMagazine(0.3, DoTInformation.Electro_DPS, (int) magSize);
			}
			
			if (selectedOverclock == 6) {
				// Remember to tie this 7.5 to however the Damage per Particle gets modeled.
				double ignitionTime = EnemyInformation.averageTimeToIgnite(0, 7.5, RoF, 0);
				double burnDoTUptime = (duration - ignitionTime) / duration;
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
		}
		else {
			duration = (magSize / RoF) + calculateCoolingPeriod();
			
			if (selectedTier4 == 0) {
				electrocuteDPS = DoTInformation.Electro_DPS;
			}
			
			if (selectedOverclock == 6) {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
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
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + (bulletsThatHitTarget + bulletsThatHitWeakpoint) * areaDamage) / duration + electrocuteDPS + burnDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 1 && selectedOverclock != 0) {
			double magSize = calculateNumShotsFiredBeforeOverheating();
			double timeToFireMagazineAndReload = (magSize / calculateAverageRoF()) + calculateCoolingPeriod();
			double areaDamage = getAreaDamage();
			
			double areaDamagePerMag = areaDamage * aoeEfficiency[1] * magSize;
			double sustainedAdditionalDPS = areaDamagePerMag / timeToFireMagazineAndReload;
			
			return sustainedAdditionalDPS;
		}
		else if (selectedTier4 != 1 && selectedOverclock == 0) {
			// TODO: bouncy bullets
			return 0;
		}
		else if (selectedTier4 == 1 && selectedOverclock == 0) {
			// TODO: bouncy bullets + plasma splash interaction
			return 0;
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// TODO: Electrocute, Splash, and Burn
		return (getDirectDamage() + getAreaDamage()) * getBatteryCapacity();
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier4 == 1 && selectedOverclock != 0) {
			return (int) aoeEfficiency[2];
		}
		else if (selectedTier4 != 1 && selectedOverclock == 0) {
			// TODO: bouncy bullets
			return 2;
		}
		else if (selectedTier4 == 1 && selectedOverclock == 0) {
			// TODO: bouncy bullets + plasma splash interaction
			return (int) aoeEfficiency[2];
		}
		else {
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = (int) calculateNumShotsFiredBeforeOverheating();
		double numMags = numMagazines(getBatteryCapacity(), magSize);
		double numCooldowns = numReloads(getBatteryCapacity(), magSize);
		return numMags * (double) magSize / calculateAverageRoF() + numCooldowns * calculateCoolingPeriod();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage()) + getAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double baseSpread = 0.9 * getBaseSpread();
		double spreadPerShot = 1.4;
		double spreadRecoverySpeed = 8.1;
		double maxBloom = 4.2;
		double minSpreadWhileMoving = 1.0;
		
		double recoilPitch = 35.0;
		double recoilYaw = 5.0;
		double mass = 1.0;
		double springStiffness = 50.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getRateOfFire(), (int) calculateNumShotsFiredBeforeOverheating(), 1, 
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
															0.0, getArmorBreaking(), getRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Mobility
		if (selectedTier3 == 2) {
			utilityScores[0] = getOverheatDuration() * MathUtils.round(0.5 * DwarfInformation.walkSpeed, 2) * UtilityInformation.Movespeed_Utility;
		}
		
		// Armor Breaking
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getAreaDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// Slow
		if (selectedTier4 == 0) {
			// Electrocute
			utilityScores[3] = calculateMaxNumTargets() * 0.3 * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		}
		
		// Fear
		if (selectedOverclock == 3) {
			// OC "Aggressive Venting"
			double[] aggressiveVentingAoeEfficiency = calculateAverageAreaDamage(5, 3, 0.25);
			int numGlyphidsFeared = (int) Math.round(aggressiveVentingAoeEfficiency[1] * aggressiveVentingAoeEfficiency[2]);
			double probabilityToFear = calculateFearProcProbability(10.0);
			utilityScores[4] = probabilityToFear * numGlyphidsFeared * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedOverclock == 6) {
			double RoF = getRateOfFire();
			double fastRoF = RoF;
			if (selectedTier5 == 1) {
				fastRoF += 7.6;
			}
			double magSize = calculateNumShotsFiredBeforeOverheating();
			// TODO: maybe add a method about Max RoF?
			return magSize / (2 * RoF) + EnemyInformation.averageTimeToIgnite(0, 15.0, fastRoF, 0);
		}
		else {
			return -1;
		}
	}
	
	@Override
	public double damagePerMagazine() {
		return 0;
	}
	
	@Override
	public double timeToFireMagazine() {
		return calculateNumShotsFiredBeforeOverheating() / calculateAverageRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), getArmorBreaking(), 0.0, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
