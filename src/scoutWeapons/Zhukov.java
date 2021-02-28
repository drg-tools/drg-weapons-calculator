package scoutWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public class Zhukov extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	
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
		fullName = "Zhukov Nuk17";
		weaponPic = WeaponPictures.zhukovs;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 12;
		carriedAmmo = 680;
		magazineSize = 50;  // Really 25
		rateOfFire = 30.0;  // Really 15
		reloadTime = 1.8;
		
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
		tier1[0] = new Mod("Hollow-Point Bullets", "+30% Weakpoint Bonus", modIcons.weakpointBonus, 1, 0);
		tier1[1] = new Mod("High Velocity Rounds", "+1 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "+12 Magazine Size", modIcons.magSize, 2, 0);
		tier2[1] = new Mod("Supercharged Feed Mechanism", "+8 Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Quickfire Ejector", "-0.35 Reload Time", modIcons.reloadSpeed, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Increased Caliber Rounds", "+1 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Better Weight Balance", "x0.5 Base Spread", modIcons.baseSpread, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Blowthrough Rounds", "+1 Penetration", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Expanded Ammo Bags", "+170 Max Ammo", modIcons.carriedAmmo, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Conductive Bullets", "+30% Direct Damage dealt to enemies either being Electrocuted or affected by Scout's IFG grenade", modIcons.electricity, 5, 0);
		tier5[1] = new Mod("Get In, Get Out", "+50% Movement Speed for 2.5 seconds after reloading an empty magazine", modIcons.movespeed, 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Minimal Magazines", "+2 Rate of Fire, +40 Max Ammo", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Custom Casings", "+40 Mag Size, -2 Rate of Fire", overclockIcons.magSize, 1);
		overclocks[2] = new Overclock(Overclock.classification.unstable, "Cryo Minelets", "Any bullets that impact terrain get converted to Cryo Minelets. It takes 0.1 seconds to form the minelets, "
				+ "0.8 seconds to arm them, and they only last for 3 seconds after being armed. If an enemy passes within 1.5m of a minelet, it will detonate and deal 10 Cold to all enemies "
				+ "within range. In exchange, -2 Direct Damage and -12 Magazine Size.", overclockIcons.coldDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Embedded Detonators", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 38 Internal Damage to the enemy "
				+ "upon reloading. If reloading can kill an enemy, an icon will appear next to their healthbar. In exchange: -6 Direct Damage, -20 Magazine Size, -400 Max Ammo.", overclockIcons.specialReload, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Gas Recycling", "+4 Direct Damage, but it can no longer gain bonus damage from hitting a Weakpoint. Additionally, x1.5 Base Spread "
				+ "and -50% Movement Speed while firing.", overclockIcons.directDamage, 4);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		boolean combinationIsValid = true;
		char[] symbols = combination.toCharArray();
		if (combination.length() != 6) {
			System.out.println(combination + " does not have 6 characters, which makes it invalid");
			combinationIsValid = false;
		}
		else {
			List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
			for (int i = 0; i < 5; i ++) {
				if (!validModSymbols.contains(symbols[i])) {
					System.out.println("Symbol #" + (i+1) + ", " + symbols[i] + ", is not a capital letter between A-C or a hyphen");
					combinationIsValid = false;
				}
			}
			if (symbols[0] == 'C') {
				System.out.println("Zhukov's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Zhukov's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Zhukov's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Zhukov's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-5 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			// Start by setting all mods/OC to -1 so that no matter what the old build was, the new build will go through with no problem.
			setSelectedModAtTier(1, -1, false);
			setSelectedModAtTier(2, -1, false);
			setSelectedModAtTier(3, -1, false);
			setSelectedModAtTier(4, -1, false);
			setSelectedModAtTier(5, -1, false);
			setSelectedOverclock(-1, false);
			
			switch (symbols[0]) {
				case 'A': {
					setSelectedModAtTier(1, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(1, 1, false);
					break;
				}
			}
			
			switch (symbols[1]) {
				case 'A': {
					setSelectedModAtTier(2, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(2, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(2, 2, false);
					break;
				}
			}
			
			switch (symbols[2]) {
				case 'A': {
					setSelectedModAtTier(3, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(3, 1, false);
					break;
				}
			}
			
			switch (symbols[3]) {
				case 'A': {
					setSelectedModAtTier(4, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(4, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(4, 2, false);
					break;
				}
			}
			
			switch (symbols[4]) {
				case 'A': {
					setSelectedModAtTier(5, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(5, 1, false);
					break;
				}
			}
			
			switch (symbols[5]) {
				case '1': {
					setSelectedOverclock(0, false);
					break;
				}
				case '2': {
					setSelectedOverclock(1, false);
					break;
				}
				case '3': {
					setSelectedOverclock(2, false);
					break;
				}
				case '4': {
					setSelectedOverclock(3, false);
					break;
				}
				case '5': {
					setSelectedOverclock(4, false);
					break;
				}
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
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
			toReturn -= 2;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 6;
		}
		else if (selectedOverclock == 4) {
			toReturn += 4;
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
		
		if (selectedTier4 == 1) {
			toReturn += 170;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 40;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 400;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier2 == 0) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 40;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 12;
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
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 2) {
			toReturn -= 0.35;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
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
		if (selectedOverclock == 4) {
			// Since this removes the Zhukov's ability to get weakpoint bonus damage, return a -100% to symbolize it.
			return -1.0;
		}
		else if (selectedTier1 == 0){
			return 0.3;
		}
		else {
			return 0;
		}
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
		
		boolean carriedAmmoModified = selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier2 == 2);
		
		String sign = "";
		if (selectedOverclock != 4) {
			sign = "+";
		}
		
		boolean weakpointModified = selectedTier1 == 0 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Weakpoint Bonus:", sign + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, weakpointModified, weakpointModified);
		
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
		
		double effectiveMagazineSize = getMagazineSize() / 2.0;
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
		return (effectiveMagazineSize + effectiveCarriedAmmo) * (getDirectDamage() + getAreaDamage()) * calculateMaxNumTargets();
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
		double horizontalBaseSpread = 36.0 * getBaseSpread();
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
		return effectiveMagazineSize * (getDirectDamage() + getAreaDamage()) * calculateMaxNumTargets();
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
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 100 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 18, 0, 0, 0, 12, 0, tier2[0].getText(true), "{ \"clip\": { \"name\": \"Combined Clip Size\", \"value\": 10 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 0, 0, 12, 0, 18, tier2[1].getText(true), "{ \"rate\": { \"name\": \"Combined Rate of Fire\", \"value\": 8 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[2].getText(true), "{ \"reload\": { \"name\": \"Reload Time\", \"value\": 0.6, \"subtract\": true } }", "Icon_Upgrade_Speed", "Reload Speed"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 30, 0, 20, tier3[1].getText(true), "{ \"ex1\": { \"name\": \"Base Spread\", \"value\": 0.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 25, 0, 0, 15, 0, tier4[0].getText(true), "{ \"ex2\": { \"name\": \"Max Penetrations\", \"value\": 1 } }", "Icon_Upgrade_BulletPenetration", "Blow Through"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 0, 36, 25, 15, 0, tier4[1].getText(true), "{ \"ex4\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 30, \"percent\": true } }", "Icon_Upgrade_Weakspot", "Weak Spot Bonus"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 3800, 25, 36, 0, 0, 0, 15, tier4[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 150 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 0, 0, 60, 40, 0, 110, tier5[0].getText(true), "{ \"ex6\": { \"name\": \"Damage vs Electrically Affected\", \"value\": 30, \"percent\": true } }", "Icon_Upgrade_Electricity", "Electricity"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 40, 60, 0, 0, 110, 0, tier5[1].getText(true), "{ \"ex5\": { \"name\": \"Get in, get out\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_MovementSpeed", "Movement Speed"),
				exportAllMods || false);
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL(boolean exportAllOCs) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8450, 0, 130, 0, 100, 0, 70, overclocks[0].getText(true), "{ \"rate\": { \"name\": \"Combined Rate of Fire\", \"value\": 2 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.4, \"subtract\": true } }", "Icon_Upgrade_Speed"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7700, 0, 95, 0, 75, 140, 0, overclocks[1].getText(true), "{ \"clip\": { \"name\": \"Combined Clip Size\", \"value\": 30 }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 1, \"subtract\": true } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7300, 135, 0, 95, 65, 0, 0, overclocks[2].getText(true), "{ \"ex7\": { \"name\": \"Cryo Minelets\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 1, \"subtract\": true },  \"clip\": { \"name\": \"Combined Clip Size\", \"value\": 10, \"subtract\": true } }", "Icon_Upgrade_Cold"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7550, 65, 0, 90, 0, 0, 135, overclocks[3].getText(true), "{ \"ex8\": { \"name\": \"Embedded Detonators\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 6, \"subtract\": true }, \"clip\": { \"name\": \"Combined Clip Size\", \"value\": 20, \"subtract\": true }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 400, \"subtract\": true } }", "Icon_Overclock_Special_Magazine"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7800, 125, 0, 0, 0, 70, 105, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 5 },  "
				+ "\"ex4\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 0, \"percent\": true, \"multiply\": true }, \"ex1\": { \"name\": \"Base Spread\", \"value\": 1.5, \"percent\": true, \"multiply\": true }, "
				+ "\"ex9\": { \"name\": \"Movement Speed While Using\", \"value\": 50, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
