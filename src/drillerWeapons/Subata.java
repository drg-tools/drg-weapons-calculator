package drillerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

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
		tier1 = new Mod[2];
		tier1[0] = new Mod("High Capacity Magazine", "+6 Magazine Size", modIcons.magSize, 1, 0);
		tier1[1] = new Mod("Quickfire Ejector", "-0.6 Reload Time", modIcons.reloadSpeed, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Expanded Ammo Bags", "+25 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Improved Alignment", "-100% Base Spread", modIcons.baseSpread, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Increased Caliber Rounds", "+2 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Recoil Compensator", "-20% Spread per Shot, x0.5 Recoil", modIcons.recoil, 3, 1);
		tier3[2] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", modIcons.carriedAmmo, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hollow-Point Bullets", "+45% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+3 Direct Damage", modIcons.directDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Chain Hit", "Any shot that hits a weakspot has a 50% chance to ricochet into a nearby enemy.", modIcons.ricochet, 5, 0);
		tier5[1] = new Mod("Mactera Toxin-Coating", "+50% Damage dealt to Mactera-type enemies", modIcons.special, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Elemental Rounds", "+30% Direct Damage dealt to enemies that are either Chilled or Heated (have a non-zero Temperature)", overclockIcons.directDamage, 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Burst Fire", "Changes the Subata from semi-automatic to 3-round burst fire. In exchange, -4.8 Rate of Fire", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Oversized Magazine", "+10 Magazine Size, +0.5 Reload Time", overclockIcons.magSize, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Automatic Fire", "Changes the Subata from semi-automatic to fully automatic, +2 Rate of Fire, +100% Base Spread, x2.5 Recoil", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Explosive Reload", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 42 Internal Damage to the enemy upon reloading. "
				+ "If reloading can kill an enemy, an icon will appear next to their healthbar. In exchange: x0.5 Magazine Size and x0.5 Max Ammo ", overclockIcons.specialReload, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Tranquilizer Rounds", "Every bullet has a 50% chance to stun an enemy for 6 seconds. -4 Magazine Size, -2 Rate of Fire.", overclockIcons.stun, 5);
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
				System.out.println("Subata's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Subata's second tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Subata's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Subata's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
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
				case 'C': {
					setSelectedModAtTier(1, 2, false);
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
				case '6': {
					setSelectedOverclock(5, false);
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
		// I'm choosing to disable RoF customization when the user equips OC "Burst Fire" or "Automatic Fire", for obvious reasons.
		if (selectedOverclock == 1 || selectedOverclock == 3) {
			return false;
		}
		else {
			return customizableRoF;
		}
	}
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		if (selectedTier4 == 1) {
			toReturn += 3;
		}
		
		// OC "Elemental Bullets" adds 30% of the total damage per bullet as Disintegrate damage if the bullet hits a Chilled or Heated target
		if (selectedOverclock == 0 && (statusEffects[0] || statusEffects[1])) {
			toReturn *= 1.3;
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
			toReturn += 25;
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
		
		if (selectedTier1 == 0) {
			toReturn += 6;
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
		
		if (selectedOverclock == 1) {
			toReturn -= 4.8;
		}
		else if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier1 == 1) {
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
			toReturn += 0.45;
		}
		
		return toReturn;
	}
	private int getMaxRicochets() {
		// According to GreyHound, this ricochet searches for enemies within 10m
		if (selectedTier5 == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn -= 1.0;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 1.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn -= 0.2;
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
		
		boolean directDamageModified = selectedTier3 == 0 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		// This stat only applies to OC "Explosive Reload"
		toReturn[1] = new StatsRow("Explosive Reload Damage:", getAreaDamage(), modIcons.areaDamage, selectedOverclock == 4, selectedOverclock == 4);
		
		boolean magSizeModified = selectedTier1 == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean RoFModified = selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, RoFModified);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 1 || selectedOverclock == 2);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0);
		
		// Display Subata's hidden 50% armor break penalty
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(armorBreaking), modIcons.armorBreaking, false);
		
		// These two stats only apply to OC "Tranquilizer Rounds"
		boolean tranqRoundsEquipped = selectedOverclock == 5;
		toReturn[8] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), modIcons.homebrewPowder, tranqRoundsEquipped, tranqRoundsEquipped);
		
		toReturn[9] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, tranqRoundsEquipped, tranqRoundsEquipped);
		
		boolean chainHitEquipped = selectedTier5 == 0;
		toReturn[10] = new StatsRow("Weakpoint Chain Hit Chance:", convertDoubleToPercentage(0.5), modIcons.homebrewPowder, chainHitEquipped, chainHitEquipped);
		toReturn[11] = new StatsRow("Max Ricochets:", getMaxRicochets(), modIcons.ricochet, chainHitEquipped, chainHitEquipped);
		
		boolean baseSpreadModified = selectedTier2 == 1 || selectedOverclock == 3;
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
		
		double timeToFireMagazine = timeToFireMagazine();
		
		if (burst) {
			duration = timeToFireMagazine;
		}
		else {
			duration = timeToFireMagazine + getReloadTime();
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
		// If "Chain Hit" is equipped, 50% of bullets that hit a weakpoint will ricochet to nearby enemies.
		if (selectedTier5 == 0) {
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.5 * getWeakpointAccuracy() / 100.0;
			double numBulletsRicochetPerMagazine = Math.round(ricochetProbability * getMagazineSize());

			double timeToFireMagazineAndReload = timeToFireMagazine() + getReloadTime();
			
			return numBulletsRicochetPerMagazine * (getDirectDamage() + getAreaDamage()) / timeToFireMagazineAndReload;
		}
		else {
			return 0.0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalNumRicochets = 0;
		if (selectedTier5 == 0) {
			// Chain Hit
			double ricochetProbability = 0.5 * getWeakpointAccuracy() / 100.0;
			totalNumRicochets = Math.round(ricochetProbability * (getMagazineSize() + getCarriedAmmo()));
		}
		
		return (getMagazineSize() + getCarriedAmmo() + totalNumRicochets) * (getDirectDamage() + getAreaDamage());
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier5 == 0) {
			// T5.A "Chain Hit"
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
		double timeToFireMagazine = timeToFireMagazine();
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
		
		int burstSize = 1;
		if (selectedOverclock == 1) {
			burstSize = 3;
		}
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getCustomRoF(), getMagazineSize(), burstSize, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		if (selectedOverclock == 0 && (statusEffects[0] || statusEffects[1])) {
			directDamage[0] *= 1.3;
		}
		
		double[] areaDamage = new double[5];
		areaDamage[0] = getAreaDamage();  // Kinetic
		
		double macteraBonus = 0;
		if (selectedTier5 == 1) {
			macteraBonus = 0.5;
		}
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), armorBreaking, getCustomRoF(), 0.0, macteraBonus, 
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
		if (selectedOverclock == 1) {
			double timeToFireBurst = (3 - 1) * 0.05;
			double delayBetweenBursts = 1.0 / getCustomRoF();
			int numBurstsPerMagazine = getMagazineSize() / 3;
			
			return numBurstsPerMagazine * timeToFireBurst + (numBurstsPerMagazine - 1) * delayBetweenBursts;
		}
		else {
			return ((double) getMagazineSize()) / getCustomRoF();
		}
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), armorBreaking, getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy(), selectedOverclock == 4);
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
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ex2\": { \"name\": \"Base Spread\", \"value\": 0, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 5 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[2].getText(true), "{ \"reload\": { \"name\": \"Reload Time\", \"value\": 0.6, \"subtract\": true } }", "Icon_Upgrade_Speed", "Reload Speed"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 40 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 30, 0, 0, 20, 0, tier3[0].getText(true), "{ \"dmg\\\": { \"name\": \"Damage\", \"value\": 1 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[1].getText(true), "{ \"ex3\": { \"name\": \"Spread Per Shot\", \"value\": 20, \"percent\": true, \"subtract\": true }, "
				+ "\"recoil\": { \"name\": \"Recoil\", \"value\": 0.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2200, 20, 0, 30, 0, 0, 0, tier3[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 40 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 0, 36, 25, 0, 15, tier4[0].getText(true), "{ \"ex1\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 60, \"percent\": true } }", "Icon_Upgrade_Weakspot", "Weak Spot Bonus"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 15, 36, 0, 0, 25, 0, tier4[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 110, 0, 0, 40, 0, 60, tier5[0].getText(true), "{ \"ex4\": { \"name\": \"Bonus Fire Damage to Burning Targets\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Heat", "Heat"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 0, 40, 110, 0, 60, 0, tier5[1].getText(true), "{ \"ex5\": { \"name\": \"Damage Vs Mactera\", \"value\": 20, \"percent\": true } }", "Icon_Upgrade_Special", "Special"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7600, 0, 65, 0, 120, 0, 100, overclocks[0].getText(true), "{ \"ex6\": { \"name\": \"Weakpoint Chain Hit Chance\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Ricoshet"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7150, 70, 135, 0, 100, 0, 0, overclocks[1].getText(true), "{ \"ex7\": { \"name\": \"Randomized Damage\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": " + homebrewPowderCoefficient + ", \"multiply\": true } }", "Icon_Overclock_ChangeOfHigherDamage"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 9000, 0, 70, 130, 0, 0, 110, overclocks[2].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 10 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.5 } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7400, 0, 95, 0, 65, 120, 0, overclocks[3].getText(true), "{ \"ex8\": { \"name\": \"Automatic Fire\", \"value\": 1, \"boolean\": true }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 2 }, \"ex2\": { \"name\": \"Base Spread\", \"value\": 100, \"percent\": true }, \"recoil\": { \"name\": \"Recoil\", \"value\": 2.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_FireRate"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8100, 65, 0, 125, 0, 95, 0, overclocks[4].getText(true), "{ \"ex9\": { \"name\": \"Explosive Reload\", \"value\": 1, \"boolean\": true }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 0.5, \"multiply\": true }, \"clip\": { \"name\": \"Magazine Size\", \"value\": 0.5, \"multiply\": true } }", "Icon_Overclock_Special_Magazine"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 7150, 0, 0, 75, 95, 0, 135, overclocks[5].getText(true), "{ \"ex10\": { \"name\": \"Stun Chance\", \"value\": 50, \"percent\": true }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 4, \"subtract\": true }, \"rate\": { \"name\": \"Rate of Fire\", \"value\": 4, \"subtract\": true } }", "Icon_Upgrade_Stun"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
