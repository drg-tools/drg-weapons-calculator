package drgtools.dpscalc.weapons.scout;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.DwarfInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

public class Zhukov extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointBonus;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Zhukov() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Zhukov(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Zhukov(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Zhukov NUK17";
		weaponPic = WeaponPictures.zhukovs;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 12;
		carriedAmmo = 650;
		magazineSize = 50;  // Really 25
		rateOfFire = 30.0;  // Really 15
		reloadTime = 1.8;
		weakpointBonus = 0.15;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+100 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("High Velocity Rounds", "+1 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "+10 Magazine Size", modIcons.magSize, 2, 0);
		tier2[1] = new Mod("Supercharged Feed Mechanism", "+8 Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Quickfire Ejector", "-0.6 Reload Time", modIcons.reloadSpeed, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Increased Caliber Rounds", "+1 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Better Weight Balance", "x0.4 Base Spread", modIcons.baseSpread, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Blowthrough Rounds", "+1 Penetration", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "+30% Weakpoint Bonus", modIcons.weakpointBonus, 4, 1);
		tier4[2] = new Mod("Expanded Ammo Bags", "+100 Max Ammo", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Conductive Bullets", "+30% Direct Damage dealt to enemies either being Electrocuted or affected by Scout's IFG grenade", modIcons.electricity, 5, 0);
		tier5[1] = new Mod("Get In, Get Out", "+50% Movement Speed for 2.5 seconds after reloading an empty magazine", modIcons.movespeed, 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Minimal Magazines", "+2 Rate of Fire, -0.4 Reload Time", overclockIcons.reloadSpeed, 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Custom Casings", "+30 Mag Size, -4 Rate of Fire", overclockIcons.magSize, 1);
		overclocks[2] = new Overclock(Overclock.classification.unstable, "Cryo Minelets", "Any bullets that impact terrain get converted to Cryo Minelets. It takes 0.1 seconds to form the minelets, "
				+ "0.8 seconds to arm them, and they only last for 3 seconds after being armed. If an enemy passes within 1.5m of a minelet, it will detonate and deal 10 Cold to all enemies "
				+ "within range. In exchange, -1 Direct Damage and -10 Magazine Size.", overclockIcons.coldDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Embedded Detonators", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 38 Internal Damage to the enemy "
				+ "upon reloading. If reloading can kill an enemy, an icon will appear next to their healthbar. In exchange: -6 Direct Damage, -20 Magazine Size, -400 Max Ammo.", overclockIcons.specialReload, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Gas Recycling", "+5 Direct Damage, but it can no longer gain bonus damage from hitting a Weakpoint. Additionally, x1.5 Base Spread "
				+ "and -50% Movement Speed while firing.", overclockIcons.directDamage, 4);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public Zhukov clone() {
		return new Zhukov(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Zhukov";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.zhukovsGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 1) {
			toReturn += 1;
		}
		if (selectedTier3 == 0) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 2) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 6;
		}
		else if (selectedOverclock == 4) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private int getAreaDamage() {
		// Equipping the Overclock "Embedded Detonators" leaves a detonator inside enemies that does 38 Internal damage to an enemy upon reloading the Zhukovs
		if (selectedOverclock == 3) {
			return 38;
		}
		else { 
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 100;
		}
		if (selectedTier4 == 2) {
			toReturn += 100;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 400;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier2 == 0) {
			toReturn += 10;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 30;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 10;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 20;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 1) {
			toReturn += 8.0;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 1) {
			toReturn -= 4.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 2) {
			toReturn -= 0.6;
		}
		
		if (selectedOverclock == 0) {
			toReturn -= 0.4;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.4;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		// Early exit: OC "Gas Recycling"
		if (selectedOverclock == 4) {
			// Since this removes the Zhukov's ability to get weakpoint bonus damage, return a -100% to symbolize it.
			return -1.0;
		}
		
		if (selectedTier4 == 1){
			toReturn += 0.3;
		}
		
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = 1.0;
		
		if (selectedOverclock == 4) {
			modifier -= 0.5;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[10];
		
		boolean directDamageModified = selectedTier1 == 1 || selectedTier3 == 0 || (selectedOverclock > 1 && selectedOverclock < 5);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		// This stat only applies to OC "Embedded Detonators"
		toReturn[1] = new StatsRow("Embedded Detonators Damage:", getAreaDamage(), modIcons.areaDamage, selectedOverclock == 3, selectedOverclock == 3);
		
		boolean magSizeModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedTier4 == 2 || selectedOverclock == 3;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier2 == 2 || selectedOverclock == 0);
		
		String sign = "";
		if (selectedOverclock != 4) {
			sign = "+";
		}
		
		boolean weakpointModified = selectedTier4 == 1 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Weakpoint Bonus:", sign + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, weakpointModified);
		
		toReturn[7] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier4 == 0, selectedTier4 == 0);
		
		boolean baseSpreadModified = selectedTier3 == 1 || selectedOverclock == 4;
		toReturn[8] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[9] = new StatsRow("Movespeed While Firing: (m/sec)", getMovespeedWhileFiring(), modIcons.movespeed, selectedOverclock == 4, selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// This weapon can never deal splash damage
		return false;
	}
	
	private double calculateAvgNumBulletsNeededToFreeze() {
		// Minelets do 10 Cold Damage upon detonation, but they have to take 0.1 seconds to arm first.
		// While Frozen, bullets do x3 Direct Damage.
		double effectiveRoF = getRateOfFire() / 2.0;
		double timeToFreeze = EnemyInformation.averageTimeToFreeze(0, -10, effectiveRoF, 0);
		return Math.ceil(timeToFreeze * effectiveRoF);
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration;
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		double effectiveMagazineSize = getMagazineSize() / 2;
		double effectiveRoF = getRateOfFire() / 2.0;
		if (burst) {
			duration = effectiveMagazineSize / effectiveRoF;
		}
		else {
			duration = effectiveMagazineSize / effectiveRoF + getReloadTime();
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
		
		// Conductive Bullets is x1.3 multiplier on Electrocuted targets or targets inside IFG field
		if (selectedTier5 == 0 && (statusEffects[2] || statusEffects[3])) {
			directDamage *= 1.3;
		}
		
		double damagePerMagazine;
		int bulletsThatHitTarget;
		if (selectedOverclock == 2) {
			// Is the primary target already frozen?
			if (statusEffects[1]) {
				// If this is the case, then the Frozen x3 damage has already been applied.
				bulletsThatHitTarget = (int) Math.round(effectiveMagazineSize * generalAccuracy);
			}
			else {
				// First, you have to intentionally miss bullets in order to convert them to Cryo Minelets, then wait 0.9 seconds, and unload the rest of the clip into
				// the now-frozen enemy for x3 damage. Damage vs frozen enemies does NOT benefit from weakpoint damage on top of the frozen multiplier.
				duration += 0.9;
				double numBulletsMissedToBecomeCryoMinelets = calculateAvgNumBulletsNeededToFreeze();
				directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
				bulletsThatHitTarget = (int) Math.round((effectiveMagazineSize - numBulletsMissedToBecomeCryoMinelets) * generalAccuracy);
			}
			
			damagePerMagazine = directDamage * bulletsThatHitTarget;
		}
		else {
			if (weakpoint && selectedOverclock != 4 && !statusEffects[1]) {
				double weakpointAccuracy = getWeakpointAccuracy() / 100.0;
				int bulletsThatHitWeakpoint = (int) Math.round(effectiveMagazineSize * weakpointAccuracy);
				bulletsThatHitTarget = (int) Math.round(effectiveMagazineSize * generalAccuracy) - bulletsThatHitWeakpoint;
				damagePerMagazine = bulletsThatHitWeakpoint * increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus(), 1.0) + bulletsThatHitTarget * directDamage + (bulletsThatHitWeakpoint + bulletsThatHitTarget) * areaDamage;
			}
			else {
				bulletsThatHitTarget = (int) Math.round(effectiveMagazineSize * generalAccuracy);
				damagePerMagazine = (directDamage + areaDamage) * bulletsThatHitTarget;
			}
		}
		
		return damagePerMagazine / duration;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateSingleTargetDPS(false, false, false, false);
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		double effectiveCarriedAmmo = getCarriedAmmo() / 2.0;
		return (effectiveMagazineSize + effectiveCarriedAmmo) * (getDirectDamage() + getAreaDamage()) * calculateBlowthroughDamageMultiplier(getMaxPenetrations());
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		// Because of how this weapon works, all these numbers need to be halved to be accurate.
		int effectiveMagazineSize = getMagazineSize() / 2;
		int effectiveCarriedAmmo = getCarriedAmmo() / 2;
		double effectiveRoF = getRateOfFire() / 2.0;
		
		double timeToFireMagazine = ((double) effectiveMagazineSize) / effectiveRoF;
		return numMagazines(effectiveCarriedAmmo, effectiveMagazineSize) * timeToFireMagazine + numReloads(effectiveCarriedAmmo, effectiveMagazineSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		// Because the Overclock "Gas Recycling" removes the ability to get any weakpoint bonus damage, that has to be modeled here.
		double dmgPerShot;
		if (selectedOverclock == 4) {
			dmgPerShot = getDirectDamage();
		}
		else {
			dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getAreaDamage();
		}
		
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double horizontalBaseSpread = 33.0 * getBaseSpread();
		double verticalBaseSpread = 5.0 * getBaseSpread();
		double recoilPitch = 20.0;
		double recoilYaw = 20.0;
		double mass = 1.0;
		double springStiffness = 100.0;
		
		return accEstimator.calculateRectangularAccuracy(weakpointAccuracy, horizontalBaseSpread, verticalBaseSpread, recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		// T5.A Conductive Bullets multiplies by an additional x1.3 when hitting enemies electrocuted or affected by IFG
		if (selectedTier5 == 0 && (statusEffects[2] || statusEffects[3])) {
			directDamage[0] *= 1.3;
		}
		
		double[] areaDamage = new double[5];
		areaDamage[0] = getAreaDamage();  // Kinetic
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), 1.0, getRateOfFire()/2.0, 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, selectedOverclock == 3);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// OC "Gas Recycling" reduces Scout's movement speed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Mod Tier 5 "Get In, Get Out" gives 50% movement speed increase for 2.5 sec after reloading empty clips
		if (selectedTier5 == 1) {
			// Because this buff lasts 2.5 seconds, but I don't think it's possible to have 100% uptime. Use the uptime as a coefficient to reduce the value of the movespeed buff.
			double effectiveMagazineSize = getMagazineSize() / 2.0;
			double effectiveRoF = getRateOfFire() / 2.0;
			double timeToFireMagazineAndReload = (effectiveMagazineSize / effectiveRoF) + getReloadTime();
			
			// Just because I don't think it's possible doesn't mean I'm not safeguarding against it.
			double uptimeCoefficient = Math.min(2.5 / timeToFireMagazineAndReload, 1);
			
			utilityScores[0] += uptimeCoefficient * MathUtils.round(0.5 * DwarfInformation.walkSpeed, 2) * UtilityInformation.Movespeed_Utility;
		}
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage()) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Cryo Minelets" applies Cryo damage to missed bullets
		if (selectedOverclock == 2) {
			// Cryo minelets: 1 placed per 2 ammo, minelets arm in 0.9 seconds, and detonate in 4 seconds if no enemy is around.
			// Minelets do 10 Cold Damage each, and explode in a 1.5m radius.
			int estimatedNumTargetsSlowedOrFrozen = calculateNumGlyphidsInRadius(1.5);
			
			utilityScores[3] = estimatedNumTargetsSlowedOrFrozen * UtilityInformation.Cold_Utility;
			utilityScores[6] = estimatedNumTargetsSlowedOrFrozen * UtilityInformation.Frozen_Utility;
		}
		else {
			utilityScores[3] = 0;
			utilityScores[6] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedOverclock == 2) {
			double effectiveRoF = getRateOfFire() / 2.0;
			// I'm choosing to add 0.9 to model the 0.1 creation time and 0.8 arming time before minelets deal their Cold damage
			return 0.9 + EnemyInformation.averageTimeToFreeze(0, -10, effectiveRoF, 0);
		}
		else {
			return -1;
		}
	}
	
	@Override
	public double damagePerMagazine() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		return effectiveMagazineSize * (getDirectDamage() + getAreaDamage()) * calculateBlowthroughDamageMultiplier(getMaxPenetrations());
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), 1.0, getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy(), selectedOverclock == 3);
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
