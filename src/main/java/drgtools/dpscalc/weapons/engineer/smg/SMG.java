package drgtools.dpscalc.weapons.engineer.smg;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.DoTInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

// TODO: 50% AB
public class SMG extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double electrocutionDoTChance;
	private double directDamage;
	private double electricDamage;
	private int magazineSize;
	private int carriedAmmo;
	private double rateOfFire;
	private double reloadTime;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public SMG() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public SMG(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public SMG(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Stubby\" Voltaic SMG";
		weaponPic = WeaponPictures.SMG;
		
		// Base stats, before mods or overclocks alter them:
		electrocutionDoTChance = 0.25;
		// Electrocution DoTs do not stack; it only refreshes the duration.
		directDamage = 9;
		electricDamage = 0; 
		// Added onto the direct damage of each bullet; does not affect DoT damage. Affected by weakpoint bonuses and elemental weaknesses/resistances
		magazineSize = 30;
		carriedAmmo = 420;
		rateOfFire = 11.0;
		reloadTime = 2.0;
		
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
		tier1 = new Mod[3];
		tier1[0] = new Mod("Increased Caliber Rounds", "+3 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Upgraded Capacitors", "+25% Chance to Electrocute an enemy", modIcons.electricity, 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "+120 Max Ammo", modIcons.carriedAmmo, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "+10 Magazine Size", modIcons.magSize, 2, 0);
		tier2[1] = new Mod("Recoil Dampener", "x0.7 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Improved Gas System", "+3 Rate of Fire", modIcons.rateOfFire, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("High Velocity Rounds", "+3 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "+120 Max Ammo", modIcons.carriedAmmo, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hollow-Point Bullets", "+25% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("Conductive Bullets", "+25% Direct Damage dealt to enemies either being Electrocuted or affected by Scout's IFG grenade", modIcons.electricity, 4, 1);
		tier4[2] = new Mod("Hardened Rounds", "+250% Armor Breaking", modIcons.armorBreaking, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Magazine Capacity Tweak", "+20 Magazine Size", modIcons.magSize, 5, 0);
		tier5[1] = new Mod("Electric Arc", "Every time the SMG applies its Electrocute DoT, there's a 25% chance that all enemies within a 2.75m radius of the primary target will be electrocuted as well.", modIcons.electricity, 5, 1);
		tier5[2] = new Mod("Improved Accuracy", "x0.6 Base Spread", modIcons.baseSpread, 5, 2);

		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Super-Slim Rounds", "+5 Magazine Size, x0.8 Base Spread", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Well Oiled Machine", "+2 Rate of Fire, -0.2 Reload Time", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "EM Refire Booster", "+2 Electric Damage per bullet, +4 Rate of Fire, x1.5 Base Spread", overclockIcons.rateOfFire, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Light-Weight Rounds", "+180 Max Ammo, -1 Direct Damage, -2 Rate of Fire", overclockIcons.carriedAmmo, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Turret Arc", "If a bullet fired from the SMG hits a turret and applies an Electrocute DoT, that turret will apply an 80% slow and 30 Electric DPS in a 2m radius around it for 20 seconds. "
				+ "Additionally, if 2 turrets are less than 15m apart and both are electrocuted at the same time, then an electric beam will pass between them that also slows and damages enemies until the first turret's electrocute expires. "
				+ "-120 Max Ammo, -2 Rate of Fire", overclockIcons.electricity, 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Turret EM Discharge", "If a bullet fired from the SMG hits a turret and applies an Electrocute DoT, it triggers an explosion that deals 60 Electric Damage and 0.5 Fear to all enemies "
				+ "within a 5m radius, as well as Electrocuting them. There's a 1.5 second cooldown between explosions. -2 Direct Damage, -5 Magazine Size.", overclockIcons.areaDamage, 5, false);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public SMG clone() {
		return new SMG(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "SMG";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.engineerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.SMGGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getElectrocutionDoTChance() {
		double toReturn = electrocutionDoTChance;
		
		if (selectedTier1 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedTier1 == 0) {
			toReturn += 3;
		}
		if (selectedTier3 == 0) {
			toReturn += 3;
		}
		if (selectedOverclock == 3) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 2;
		}
		
		// Multiplicative bonuses last
		if (selectedTier4 == 1) {
			toReturn *= conductiveBulletsMultiplier();
		}
		
		return toReturn;
	}
	private double getElectricDamage() {
		double toReturn = electricDamage;
		
		// Additive bonuses first
		if (selectedOverclock == 2) {
			toReturn += 2;
		}
		
		// Multiplicative bonuses last
		if (selectedTier4 == 1) {
			toReturn *= conductiveBulletsMultiplier();
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier2 == 0) {
			toReturn += 10;
		}
		
		if (selectedTier5 == 0) {
			toReturn += 20;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 5;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 2) {
			toReturn += 120;
		}
		
		if (selectedTier3 == 1) {
			toReturn += 120;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 180;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 120;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 2) {
			toReturn += 3.0;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 2) {
			toReturn += 4.0;
		}
		else if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;

		if (selectedTier5 == 2) {
			toReturn *= 0.6;
		}

		if (selectedOverclock == 0) {
			toReturn *= 0.8;
		}
		else if (selectedOverclock == 2) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.7;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = 0.0;
		
		if (selectedTier4 == 0) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}

	private double getArmorBreaking() {
		if (selectedTier4 == 2) {
			return 3.0;
		}
		else {
			return 0.5;
		}
	}
	
	private double conductiveBulletsMultiplier() {
		double conductiveBulletsDamageMultiplier = 1.25;
		if (statusEffects[2] || statusEffects[3]) {
			return conductiveBulletsDamageMultiplier;
		}
		else {
			// To model a 30% direct damage increase to electrocuted targets, average out how many bullets/mag that would get the buff after a DoT proc, and then spread that bonus across every bullet.
			double numBulletsBeforeElectrocute = Math.ceil(MathUtils.meanRolls(getElectrocutionDoTChance()));
			return averageBonusPerMagazineForLongEffects(conductiveBulletsDamageMultiplier, numBulletsBeforeElectrocute, getMagazineSize());
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		toReturn[0] = new StatsRow("Electrocute DoT Chance:", convertDoubleToPercentage(getElectrocutionDoTChance()), modIcons.homebrewPowder, selectedTier1 == 1);
		toReturn[1] = new StatsRow("Electrocute DoT DPS:", DoTInformation.Electro_DPS, modIcons.electricity, false);
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 0 || selectedTier4 == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		toReturn[3] = new StatsRow("Electric Damage:", getElectricDamage(), modIcons.directDamage, selectedTier4 == 1 || selectedOverclock == 2, selectedOverclock == 2);
		
		boolean magSizeModified = selectedTier2 == 0 || selectedTier5 == 0 || selectedOverclock == 0 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedTier3 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean RoFModified = selectedTier2 == 2 || (selectedOverclock > 0 && selectedOverclock < 5);
		toReturn[6] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, RoFModified);
		
		toReturn[7] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0, selectedTier4 == 0);
		
		boolean baseSpreadModified = selectedTier5 == 2 || selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[10] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, selectedTier2 == 1, selectedTier2 == 1);

		toReturn[11] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 2, selectedTier4 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private double calculateBurstElectrocutionDoTDPS() {
		return calculateRNGDoTDPSPerMagazine(getElectrocutionDoTChance(), DoTInformation.Electro_DPS, getMagazineSize());
	}

	@Override
	public boolean currentlyDealsRadialDamage() {
		// T5.B Electric Arc has a 2.75m radius AoE that can electrocute nearby enemies
		return selectedTier5 == 1;
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
		
		double electrocuteDPS;
		if (burst) {
			duration = ((double) getMagazineSize()) / getRateOfFire();
			electrocuteDPS = calculateBurstElectrocutionDoTDPS();
		}
		else {
			duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			electrocuteDPS = DoTInformation.Electro_DPS;
		}
		
		double weakpointAccuracy;
		double directDamage = getDirectDamage() + getElectricDamage();
		
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
		
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus(), 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage) / duration + electrocuteDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier5 == 1) {
			return 0.25 * calculateBurstElectrocutionDoTDPS();
		}
		else {
			return 0.0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// First, how much direct damage can be dealt without DoT calculations. Second, add the DoTs on the primary targets. Third, if necessary, add the secondary target DoTs.
		double totalDamage = 0;
		totalDamage += (getDirectDamage() + getElectricDamage()) * (getMagazineSize() + getCarriedAmmo());
		
		/* 
			There's no good way to model RNG-based mechanics' max damage, such as the Electrocute DoT. I'm choosing
			to model it as how much DPS it does per magazine times how many seconds it takes to fire every bullet. 
			This value should always be less than the full DoT DPS times firing duration.
			
			TODO: maybe change this to imitate how AC NTP has its Max Dmg calculated?
		*/
		totalDamage += calculateBurstElectrocutionDoTDPS() * calculateFiringDuration();
		
		if (selectedTier5 == 1) {
			// Don't double-count the DoTs already calculated for the primary target
			totalDamage += (calculateMaxNumTargets() - 1) * calculateAdditionalTargetDPS() * calculateFiringDuration();
		}
		
		return totalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier5 == 1) {
			// T5.B "Electric Arc" causes a 2.75m AoE around the primary target 25% of the time that it procs an Electrocute DoT
			return calculateNumGlyphidsInRadius(2.75);
		}
		else {
			return 1;
		}
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
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage() + getElectricDamage());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getElectricDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double baseSpread = 3.0 * getBaseSpread();
		double spreadPerShot = 1.5;
		double spreadRecoverySpeed = 10.0;
		double maxBloom = 4.0;
		double minSpreadWhileMoving = 1.5;
		
		// Technically the SMG can have its RecoilPitch range anywhere from 35 to 40, but for simplicity's sake I'm choosing to use the average of 37.5.
		double recoilPitch = 37.5 * getRecoil();
		double recoilYaw = 7.0 * getRecoil();
		double mass = 1.0;
		double springStiffness = 40.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getRateOfFire(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
//		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
//		double[] directDamage = new double[5];
//		directDamage[0] = getDirectDamage();  // Kinetic
//		directDamage[4] = getElectricDamage();  // Electric
//
//		double[] areaDamage = new double[5];
//
//		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
//		double[] dot_dps = new double[4];
//		double[] dot_duration = new double[4];
//		double[] dot_probability = new double[4];
//
//		dot_dps[0] = DoTInformation.Electro_DPS;
//		dot_duration[0] = DoTInformation.Electro_SecsDuration;
//		dot_probability[0] = getElectrocutionDoTChance();
//
//		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability,
//															getWeakpointBonus(), 1.0, getRateOfFire(), 0.0, 0.0,
//															statusEffects[1], statusEffects[3], false, false);
//		return MathUtils.sum(breakpoints);
		return 0;
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getElectricDamage()) * UtilityInformation.ArmorBreak_Utility;
		
		// Innate ability to Electrocute applies an 80% slow to enemies (proc chance increased/decreased by mods and OCs)
		utilityScores[3] = getElectrocutionDoTChance() * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		if (selectedTier5 == 1) {
			utilityScores[3] += getElectrocutionDoTChance() * 0.25 * (calculateMaxNumTargets() - 1) * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		}
		if (selectedOverclock == 4) {
			// Turret Arc can emit a beam up to 15m long that applies a 80% slow, doing 6 Electric Damage per tick, 5 ticks/sec for up to 20 seconds.
			// Using Grunts' 2m length and 2.9 m/sec movespeed as a baseline, I expect it will take 2 / (0.3 * 2.9) = 2.3 seconds to pass through
			int numEnemiesSlowedByTurretArc = calculateNumGlyphidsInStream(15.0);
			utilityScores[3] += numEnemiesSlowedByTurretArc * 2.3 * 0.8;
		}
		
		// Fear
		if (selectedOverclock == 5) {
			// OC "Turret EM Discharge" inflicts 0.5 Fear in a 5m radius around the sentry. Also, since the enemies will be electrocuted the Fear duration gets increased.
			// 5m radius returns 41 Grunts, which is more than I think would realistically be hit by these explosions. As such, I'm artificially halving the Fear radius to 2.5m
			utilityScores[4] = calculateFearProcProbability(0.5) * calculateNumGlyphidsInRadius(5.0/2.0, false) * EnemyInformation.averageFearDuration(0.8, 3) * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		double timeBeforeElectrocute = MathUtils.meanRolls(getElectrocutionDoTChance()) / getRateOfFire();
		return (getDirectDamage() + getElectricDamage()) * getMagazineSize() + calculateAverageDoTDamagePerEnemy(timeBeforeElectrocute, DoTInformation.Electro_SecsDuration, DoTInformation.Electro_DPS);
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage() + getElectricDamage(), 1, 0.0, 1.0, getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
