package drgtools.dpscalc.weapons.driller;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

public class Subata extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointBonus;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Subata() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Subata(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Subata(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Subata 120";
		weaponPic = WeaponPictures.subata;
		customizableRoF = true;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 12;
		carriedAmmo = 160;
		magazineSize = 12;
		rateOfFire = 8.0;
		reloadTime = 1.9;
		weakpointBonus = 0.2;
		// Subata has a hidden 50% Armor Breaking penalty (credit to Elythnwaen for pointing this out to me)
		armorBreaking = 0.5;
		
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
		tier1[0] = new Mod("Improved Alignment", "x0 Base Spread", modIcons.baseSpread, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+5 Magazine Size", modIcons.magSize, 1, 1);
		tier1[2] = new Mod("Quickfire Ejector", "-0.6 Reload Time", modIcons.reloadSpeed, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Increased Caliber Rounds", "+1 Direct Damage", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Propellant", "+1 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Recoil Compensator", "-33% Spread per Shot, x0.5 Recoil", modIcons.recoil, 3, 1);
		tier3[2] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", modIcons.carriedAmmo, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hollow-Point Bullets", "+60% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+3 Direct Damage", modIcons.directDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Volatile Bullets", "+50% Direct Damage dealt to Burning enemies. Bonus damage is Fire element and adds the same amount of Heat, extending the Burn duration.", modIcons.heatDamage, 5, 0);
		tier5[1] = new Mod("Mactera Toxin-Coating", "+20% Damage dealt to Mactera-type enemies", modIcons.special, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a 75% chance to ricochet into a nearby enemy within 10m.", overclockIcons.ricochet, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, overclockIcons.homebrewPowder, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Oversized Magazine", "+10 Magazine Size, +0.5 Reload Time", overclockIcons.magSize, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Automatic Fire", "Changes the Subata from semi-automatic to fully automatic, +2 Rate of Fire, +100% Base Spread, x2.5 Recoil", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Explosive Reload", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 42 Internal Damage to the enemy upon reloading. "
				+ "If reloading can kill an enemy, an icon will appear next to their healthbar. In exchange: x0.5 Magazine Size and x0.5 Max Ammo ", overclockIcons.specialReload, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Tranquilizer Rounds", "Every bullet has a 50% chance to stun an enemy for 6 seconds. -4 Magazine Size, -2 Rate of Fire.", overclockIcons.stun, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public Subata clone() {
		return new Subata(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "Subata";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.drillerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.subataGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public boolean isRofCustomizable() {
		// I'm choosing to disable RoF customization when the user equips OC "Automatic Fire", for obvious reasons.
		if (selectedOverclock == 3) {
			return false;
		}
		else {
			return customizableRoF;
		}
	}
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier2 == 1) {
			toReturn += 1;
		}
		if (selectedTier3 == 0) {
			toReturn += 1;
		}
		if (selectedTier4 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= homebrewPowderCoefficient;
		}
		
		return toReturn;
	}
	private int getAreaDamage() {
		// Equipping the Overclock "Explosive Reload" leaves a detonator inside enemies that does 42 Area Damage per Bullet that deals damage to an enemy upon reloading the Subata
		if (selectedOverclock == 4) {
			return 42;
		}
		else { 
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 40;
		}
		if (selectedTier3 == 2) {
			toReturn += 40;
		}
		
		if (selectedOverclock == 4) {
			toReturn /= 2;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 10;
		}
		else if (selectedOverclock == 4) {
			// Because this is integer division, it will truncate 8.5 down to 8, just like in-game does.
			toReturn /= 2;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 4;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier1 == 2) {
			toReturn -= 0.6;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 0) {
			toReturn += 0.6;
		}
		
		return toReturn;
	}
	private int getMaxRicochets() {
		// According to GreyHound, this ricochet searches for enemies within 10m
		if (selectedOverclock == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		// Additive bonuses first
		if (selectedOverclock == 3) {
			toReturn += 1.0;
		}
		
		// Multiplicative bonuses last
		if (selectedTier1 == 0) {
			toReturn *= 0.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.33;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 2.5;
		}
		
		return toReturn;
	}
	private double getStunChance() {
		if (selectedOverclock == 5) {
			return 0.5;
		}
		else {
			return 0;
		}
	}
	private int getStunDuration() {
		if (selectedOverclock == 5) {
			return 6;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public double getRecommendedRateOfFire() {
		return Math.min(getRateOfFire(), 6);
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedTier4 == 1 || selectedOverclock == 1;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		// This stat only applies to OC "Explosive Reload"
		toReturn[1] = new StatsRow("Explosive Reload Damage:", getAreaDamage(), modIcons.areaDamage, selectedOverclock == 4, selectedOverclock == 4);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, selectedOverclock == 3 || selectedOverclock == 5);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 2 || selectedOverclock == 2);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0);
		
		// Display Subata's hidden 50% armor break penalty
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(armorBreaking), modIcons.armorBreaking, false);
		
		// These two stats only apply to OC "Tranquilizer Rounds"
		boolean tranqRoundsEquipped = selectedOverclock == 5;
		toReturn[8] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), modIcons.homebrewPowder, tranqRoundsEquipped, tranqRoundsEquipped);
		
		toReturn[9] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, tranqRoundsEquipped, tranqRoundsEquipped);
		
		boolean chainHitEquipped = selectedOverclock == 0;
		toReturn[10] = new StatsRow("Weakpoint Chain Hit Chance:", convertDoubleToPercentage(0.75), modIcons.homebrewPowder, chainHitEquipped, chainHitEquipped);
		toReturn[11] = new StatsRow("Max Ricochets:", getMaxRicochets(), modIcons.ricochet, chainHitEquipped, chainHitEquipped);
		
		boolean baseSpreadModified = selectedTier1 == 0 || selectedOverclock == 3;
		toReturn[12] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[13] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, selectedTier3 == 1, selectedTier3 == 1);
		
		boolean recoilModified = selectedOverclock == 3 || selectedTier3 == 1;
		toReturn[14] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
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
			duration = ((double) getMagazineSize()) / getCustomRoF();
		}
		else {
			duration = (((double) getMagazineSize()) / getCustomRoF()) + getReloadTime();
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
		
		// T5.A Volatile Bullets adds 50% of the total damage per bullet as Fire damage (not Heat Damage) if the bullet hits a Burning target
		if (selectedTier5 == 0 && statusEffects[0]) {
			directDamage *= 1.5;
			// U32's version of Explosive Reload no longer benefits from Volatile Bullets
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
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + (bulletsThatHitWeakpoint + bulletsThatHitTarget) * areaDamage) / duration;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		// If "Chain Hit" is equipped, 75% of bullets that hit a weakpoint will ricochet to nearby enemies.
		if (selectedOverclock == 0) {
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.75 * getWeakpointAccuracy() / 100.0;
			double numBulletsRicochetPerMagazine = Math.round(ricochetProbability * getMagazineSize());
			
			double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getCustomRoF()) + getReloadTime();
			
			return numBulletsRicochetPerMagazine * getDirectDamage() / timeToFireMagazineAndReload;
		}
		else {
			return 0.0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		if (selectedOverclock == 0) {
			// Chain Hit
			double ricochetProbability = 0.75 * getWeakpointAccuracy() / 100.0;
			double totalNumRicochets = Math.round(ricochetProbability * (getMagazineSize() + getCarriedAmmo()));
			
			return (getMagazineSize() + getCarriedAmmo() + totalNumRicochets) * getDirectDamage();
		}
		else {
			// Because the OCs Chain Hit and Explosive Reload are mutually exclusive, the Area Damage only needs to be called here.
			return (getMagazineSize() + getCarriedAmmo()) * (getDirectDamage() + getAreaDamage());
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedOverclock == 0) {
			// OC "Chain Hit"
			return 2;
		}
		else {
			return 1;
		}
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
		double baseSpread = 1.5 * getBaseSpread();
		double spreadPerShot = 1.5 * getSpreadPerShot();
		double spreadRecoverySpeed = 7.5;
		double maxBloom = 3.0;
		double minSpreadWhileMoving = 0.5;
		
		double recoilPitch = 30.0 * getRecoil();
		double recoilYaw = 10.0 * getRecoil();
		double mass = 1.0;
		double springStiffness = 60.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getCustomRoF(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		if (selectedTier5 == 0 && statusEffects[0]) {
			directDamage[2] = 0.5 * getDirectDamage();  // Fire
		}
		
		double[] areaDamage = new double[5];
		areaDamage[0] = getAreaDamage();  // Kinetic
		
		double macteraBonus = 0;
		if (selectedTier5 == 1) {
			macteraBonus = 0.2;
		}
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), armorBreaking, getRateOfFire(), 0.0, macteraBonus, 
															statusEffects[1], statusEffects[3], false, selectedOverclock == 4);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		// The Area damage from Explosive Reload doesn't affect the chance to break the Light Armor plates since it's not part of the initial projectile
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), armorBreaking) * UtilityInformation.ArmorBreak_Utility;
		
		// Tranq rounds = 50% chance to stun, 6 second stun
		if (selectedOverclock == 5) {
			utilityScores[5] = getStunChance() * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		return getMagazineSize() * (getDirectDamage() + getAreaDamage());
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), armorBreaking, getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy(), selectedOverclock == 4);
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
