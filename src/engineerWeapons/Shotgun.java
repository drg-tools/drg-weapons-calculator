package engineerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

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
		damagePerPellet = 12;
		numberOfPellets = 6;
		carriedAmmo = 84;
		magazineSize = 6;
		rateOfFire = 1.6;
		reloadTime = 2.0;
		weakpointStunChance = 0.15;
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
		tier1 = new Mod[3];
		tier1[0] = new Mod("Supercharged Feed Mechanism", "+0.3 Rate of Fire", modIcons.rateOfFire, 1, 0);
		tier1[1] = new Mod("Overstuffed Magazine", "+2 Magazine Size", modIcons.magSize, 1, 1);
		tier1[2] = new Mod("Quickfire Ejector", "-0.5 Reload Time", modIcons.reloadSpeed, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Ammo Bags", "+18 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Loaded Shells", "+1 Pellet per Shot", modIcons.pelletsPerShot, 2, 1);
		tier2[2] = new Mod("Choke", "x0.5 Base Spread", modIcons.baseSpread, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Expanded Ammo Bags", "+18 Max Ammo", modIcons.carriedAmmo, 3, 0);
		tier3[1] = new Mod("Bigger Pellets", "+2 Damage per Pellet", modIcons.directDamage, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Tungsten Coated Buckshot", "+300% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Recoil Dampener", "x0.4 Recoil", modIcons.recoil, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Quickerfire Ejectorer", "-0.7 Reload Time", modIcons.reloadSpeed, 5, 0);
		tier5[1] = new Mod("Miner Adjustments", "Changes the Shotgun from semi-automatic to fully automatic, +0.4 Rate of Fire", modIcons.rateOfFire, 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Stunner", "Pellets can now stun an enemy on any body part instead of just weakpoints, and any shots that hit a "
				+ "target that's already stunned deal x1.4 damage.", overclockIcons.stun, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Turret Whip", "Shoot your turrets to make them shoot a projectile that does 120 Area Damage in a 1.5m Radius. Turret Whip projectile has a 100% chance to Stun for 1.5 seconds "
				+ "inflicts 0.5 Fear to all enemies it damages. 3 second cooldown per Sentry.", overclockIcons.special, 1, false);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Magnetic Pellet Alignment", "x0.5 Base Spread, +30% Weakpoint Bonus, x0.75 Rate of Fire", overclockIcons.baseSpread, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Cycle Overload", "+1 Damage per Pellet, +0.8 Rate of Fire, +0.7 Reload Time, x1.5 Base Spread", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Mini Shells", "+60 Max Ammo, +6 Magazine Size, x0.5 Recoil, -3 Damage per Pellet, and no longer able to stun enemies", overclockIcons.miniShells, 4);
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
			if (symbols[2] == 'C') {
				System.out.println("Shotgun's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Shotgun's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Shotgun's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
				case 'C': {
					setSelectedModAtTier(3, 2, false);
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
		
		if (selectedTier3 == 1) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 3;
		}
		
		return toReturn;
	}
	private int getNumberOfPellets() {
		int toReturn = numberOfPellets;
		
		if (selectedTier2 == 1) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 18;
		}
		if (selectedTier3 == 0) {
			toReturn += 18;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 60;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
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
			toReturn += 0.3;
		}
		
		if (selectedTier5 == 1) {
			toReturn += 0.4;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 0.75;
		}
		else if (selectedOverclock == 3) {
			toReturn += 0.8;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier1 == 2) {
			toReturn -= 0.5;
		}
		if (selectedTier5 == 0) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 0.7;
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
			return 4.0;
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
		
		if (selectedTier4 == 1) {
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
		
		boolean damageModified = selectedTier3 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Damage per Pellet:", getDamagePerPellet(), modIcons.directDamage, damageModified);
		
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPellets(), modIcons.pelletsPerShot, selectedTier2 == 1);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean RoFModified = selectedTier1 == 0 || selectedTier5 == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, RoFModified);
		
		boolean reloadModified = selectedTier1 == 2 || selectedTier5 == 0 || selectedOverclock == 3;
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
		
		boolean recoilModified = selectedTier4 == 1 || selectedOverclock == 4;
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
			dmgPerPellet *= averageBonusPerMagazineForShortEffects(1.4, 3.0, false, calculateCumulativeStunChancePerShot(), getMagazineSize(), getCustomRoF());
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
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDamagePerPellet() * getNumberOfPellets() * getGeneralAccuracy() / 100.0;  // Kinetic
		
		double[] areaDamage = new double[5];
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), getArmorBreaking(), getCustomRoF(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		double probabilityToBreakLightArmorPlatePerPellet = calculateProbabilityToBreakLightArmor(getDamagePerPellet(), getArmorBreaking());
		double probabilityToBreakLightArmorPlatePerShot = MathUtils.cumulativeBinomialProbability(probabilityToBreakLightArmorPlatePerPellet, getNumberOfPellets(), 1);
		utilityScores[2] = probabilityToBreakLightArmorPlatePerShot * UtilityInformation.ArmorBreak_Utility;
		
		// Fear
		if (selectedTier5 == 0) {
			// Turret Whip projectile does 0.5 Fear Factor in its 1.5m radius
			utilityScores[4] = calculateFearProcProbability(0.5) * calculateNumGlyphidsInRadius(1.5) * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Stun
		// Weakpoint = 15% stun chance per pellet, 3 sec duration (upgraded with Mod Tier 3 "Stun Duration", or OC "Stunner")
		utilityScores[5] = calculateCumulativeStunChancePerShot() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		if (selectedTier5 == 0) {
			// Turret Whip projectile has 100% chance to stun for 1.5sec in its 1.5m radius
			utilityScores[5] += calculateNumGlyphidsInRadius(1.5) * 1.5 * UtilityInformation.Stun_Utility;
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
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 1 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 2 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 40 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 0, 0, 0, 15, 24, tier2[1].getText(true), "{ \"ex1\": { \"name\": \"Pellets\", \"value\": 2 } }", "Icon_Upgrade_Shotgun_Pellet", "Pellet Count"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 2000, 24, 0, 15, 0, 0, 0, tier2[2].getText(true), "{ \"ex5\": { \"name\": \"Base Spread\", \"value\": 0.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 0, 0, 35, 0, 50, tier3[0].getText(true), "{ \"ex4\": { \"name\": \"Recoil\", \"value\": 0.4, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Recoil", "Recoil"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 35, 0, 0, 0, 50, 0, tier3[1].getText(true), "{ \"reload\": { \"name\": \"Reload Time\", \"value\": 0.5, \"subtract\": true } }", "Icon_Upgrade_Speed", "Reload Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2800, 0, 50, 0, 0, 35, 0, tier3[2].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 3 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 48, 0, 0, 0, 50, 72, tier4[0].getText(true), "{ \"ex6\": { \"name\": \"Armor Breaking\", \"value\": 400, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 0, 72, 0, 48, 50, 0, tier4[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 0, 0, 0, 64, 70, 140, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Turret Whip\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Special", "Special"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 70, 0, 140, 0, 0, tier5[1].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 0.5 }, "
				+ "\"ex8\": { \"name\": \"Miner Adjustments\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7350, 60, 100, 0, 0, 0, 135, overclocks[0].getText(true), "{ \"ex10\": { \"name\": \"Stun Chance on all body parts\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex11\": { \"name\": \"Bonus Damage Vs Stunned\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7250, 105, 0, 60, 125, 0, 0, overclocks[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 20 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.4, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7900, 0, 0, 75, 0, 120, 105, overclocks[2].getText(true), "{ \"ex5\": { \"name\": \"Base Spread\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, "
				+ "\"ex9\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 30, \"percent\": true }, \"rate\": { \"name\": \"Rate of Fire\", \"value\": 0.75, \"multiply\": true } }", "Icon_Upgrade_Accuracy"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8050, 0, 125, 80, 100, 0, 0, overclocks[3].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 2 }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.5 }, \"ex5\": { \"name\": \"Base Spread\", \"value\": 1.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_FireRate"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7700, 90, 0, 0, 125, 65, 0, overclocks[4].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 90 }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 6 }, \"ex4\": { \"name\": \"Recoil\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, \"dmg\": { \"name\": \"Damage\", \"value\": 2, \"subtract\": true }, "
				+ "\"ex2\": { \"name\": \"Weakpoint Stun Duration\", \"value\": 0, \"multiply\": true }, \"ex3\": { \"name\": \"Weakpoint Stun Chance Per Pellet\", \"value\": 0, \"percent\": true, \"multiply\": true } }", "Icon_Overclock_SmallBullets"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
