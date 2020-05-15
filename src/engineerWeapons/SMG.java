package engineerWeapons;

import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

public class SMG extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double electrocutionDoTChance;
	private int directDamage;
	private int electricDamage;
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
		electrocutionDoTChance = 0.2;
		// Electrocution DoTs do not stack; it only refreshes the duration.
		directDamage = 9;
		electricDamage = 0; 
		// Added onto the direct damage of each bullet; does not affect DoT damage. Affected by weakpoint bonuses and elemental weaknesses/resistances
		magazineSize = 30;
		carriedAmmo = 420;
		rateOfFire = 11.0;
		reloadTime = 2.0;
		
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
		tier1[0] = new Mod("Increased Caliber Rounds", "+2 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Upgraded Capacitors", "+30% Chance to Electrocute an enemy", modIcons.electricity, 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "+120 Max Ammo", modIcons.carriedAmmo, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "+10 Magazine Size", modIcons.magSize, 2, 0);
		tier2[1] = new Mod("Recoil Dampener", "x0.5 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Improved Gas System", "+3 Rate of Fire", modIcons.rateOfFire, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("High Velocity Rounds", "+2 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "+120 Max Ammo", modIcons.carriedAmmo, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Hollow-Point Bullets", "+30% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("Conductive Bullets", "+30% Direct Damage dealt to enemies either being Electrocuted or affected by Scout's IFG grenade", modIcons.electricity, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Magazine Capacity Tweak", "+20 Magazine Size", modIcons.magSize, 5, 0);
		tier5[1] = new Mod("Electric Arc", "Every time the SMG either applies or refreshes an Electrocute DoT, there's a 25% chance that enemies near the primary target will be electrocuted as well.", modIcons.electricity, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Super-Slim Rounds", "+5 Magazine Size, x0.8 Base Spread", overclockIcons.magSize, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Well Oiled Machine", "+2 Rate of Fire, -0.2 Reload Time", overclockIcons.rateOfFire, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "EM Refire Booster", "+2 Electric Damage per bullet, +4 Rate of Fire, x1.5 Base Spread", overclockIcons.rateOfFire, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Light-Weight Rounds", "+180 Max Ammo, -1 Direct Damage, -2 Rate of Fire", overclockIcons.carriedAmmo, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Turret Arc", "If a bullet fired from the SMG hits a turret and applies an Electrocute DoT, that turret deals constant Electric Damage in a small radius around it. "
				+ "Additionally, if 2 turrets are less than 10m apart and both are electrocuted at the same time, then an electric arc will pass between them for 10 seconds. -120 Max Ammo, -2 Rate of Fire", overclockIcons.electricity, 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Turret EM Discharge", "If a bullet fired from the SMG hits a turret and applies an Electrocute DoT, then an explosion deals electric damage to all enemies "
				+ "with a 5m radius. -5% Chance to Electrocute an enemy, -3 Direct Damage", overclockIcons.areaDamage, 5, false);
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
				System.out.println("SMG's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("SMG's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("SMG's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			switch (symbols[0]) {
				case '-': {
					selectedTier1 = -1;
					break;
				}
				case 'A': {
					selectedTier1 = 0;
					break;
				}
				case 'B': {
					selectedTier1 = 1;
					break;
				}
				case 'C': {
					selectedTier1 = 2;
					break;
				}
			}
			
			switch (symbols[1]) {
				case '-': {
					selectedTier2 = -1;
					break;
				}
				case 'A': {
					selectedTier2 = 0;
					break;
				}
				case 'B': {
					selectedTier2 = 1;
					break;
				}
				case 'C': {
					selectedTier2 = 2;
					break;
				}
			}
			
			switch (symbols[2]) {
				case '-': {
					selectedTier3 = -1;
					break;
				}
				case 'A': {
					selectedTier3 = 0;
					break;
				}
				case 'B': {
					selectedTier3 = 1;
					break;
				}
			}
			
			switch (symbols[3]) {
				case '-': {
					selectedTier4 = -1;
					break;
				}
				case 'A': {
					selectedTier4 = 0;
					break;
				}
				case 'B': {
					selectedTier4 = 1;
					break;
				}
			}
			
			switch (symbols[4]) {
				case '-': {
					selectedTier5 = -1;
					break;
				}
				case 'A': {
					selectedTier5 = 0;
					break;
				}
				case 'B': {
					selectedTier5 = 1;
					break;
				}
			}
			
			switch (symbols[5]) {
				case '-': {
					selectedOverclock = -1;
					break;
				}
				case '1': {
					selectedOverclock = 0;
					break;
				}
				case '2': {
					selectedOverclock = 1;
					break;
				}
				case '3': {
					selectedOverclock = 2;
					break;
				}
				case '4': {
					selectedOverclock = 3;
					break;
				}
				case '5': {
					selectedOverclock = 4;
					break;
				}
				case '6': {
					selectedOverclock = 5;
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
			toReturn += 0.3;
		}
		
		if (selectedOverclock == 5) {
			toReturn -= 0.05;
		}
		
		return toReturn;
	}
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 2;
		}
		
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		
		return toReturn;
	}
	private int getElectricDamage() {
		int toReturn = electricDamage;
		
		if (selectedOverclock == 2) {
			toReturn += 2;
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
	private double getRateOfFire() {
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
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = 0.0;
		
		if (selectedTier4 == 0) {
			toReturn += 0.3;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[11];
		
		toReturn[0] = new StatsRow("Electrocute DoT Chance:", convertDoubleToPercentage(getElectrocutionDoTChance()), selectedTier1 == 1 || selectedOverclock == 5);
		toReturn[1] = new StatsRow("Electrocute DoT DPS:", DoTInformation.Electro_DPS, false);
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 0 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		toReturn[3] = new StatsRow("Electric Damage:", getElectricDamage(), selectedOverclock == 2);
		
		boolean magSizeModified = selectedTier2 == 0 || selectedTier5 == 0 || selectedOverclock == 0;
		toReturn[4] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedTier3 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		boolean RoFModified = selectedTier2 == 2 || (selectedOverclock > 0 && selectedOverclock < 5);
		toReturn[6] = new StatsRow("Rate of Fire:", getRateOfFire(), RoFModified);
		
		toReturn[7] = new StatsRow("Reload Time:", getReloadTime(), selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 0, selectedTier4 == 0);
		
		boolean baseSpreadModified = selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		toReturn[10] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), selectedTier2 == 1, selectedTier2 == 1);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private double calculateDamagePerBullet(boolean weakpointBonus) {
		double directDamage = getDirectDamage();
		
		if (selectedTier4 == 1) {
			double conductiveBulletsDamageMultiplier = 1.3;
			if (statusEffects[2] || statusEffects[3]) {
				directDamage *= conductiveBulletsDamageMultiplier;
			}
			else {
				// To model a 30% physical damage increase to electrocuted targets, average out how many bullets/mag that would get the buff after a DoT proc, and then spread that bonus across every bullet.
				double DoTChance = getElectrocutionDoTChance();
				double meanBulletsFiredBeforeProc = MathUtils.meanRolls(DoTChance);
				double numBulletsFiredAfterProc = getMagazineSize() - meanBulletsFiredBeforeProc;
				
				directDamage *= (meanBulletsFiredBeforeProc + numBulletsFiredAfterProc * conductiveBulletsDamageMultiplier) / getMagazineSize();
			}
		}
		
		// According to the wiki, Electric damage gets bonus from Weakpoints too
		double totalDamage = directDamage + getElectricDamage();
		
		// Frozen
		if (statusEffects[1]) {
			totalDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			totalDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		if (weakpointBonus && !statusEffects[1]) {
			return increaseBulletDamageForWeakpoints2(totalDamage, getWeakpointBonus());
		}
		else {
			return totalDamage;
		}
	}
	
	private double calculateBurstElectrocutionDoTDPS() {
		return calculateRNGDoTDPSPerMagazine(getElectrocutionDoTChance(), DoTInformation.Electro_DPS, getMagazineSize());
	}

	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, duration;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
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
		double directWeakpointDamage = calculateDamagePerBullet(weakpoint);
		double directDamage = calculateDamagePerBullet(false);
		
		if (weakpoint) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
		}
		else {
			weakpointAccuracy = 0.0;
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage) / duration + electrocuteDPS;
	}

	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false, false);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(false, true, true);
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
		totalDamage += calculateDamagePerBullet(false) * (getMagazineSize() + getCarriedAmmo());
		
		/* 
			There's no good way to model RNG-based mechanics' max damage, such as the Electrocute DoT. I'm choosing
			to model it as how much DPS it does per magazine times how many seconds it takes to fire every bullet. 
			This value should always be less than the full DoT DPS times firing duration.
		*/
		totalDamage += calculateBurstElectrocutionDoTDPS() * calculateFiringDuration();
		
		if (selectedTier5 == 1) {
			totalDamage += calculateAdditionalTargetDPS() * calculateFiringDuration();
		}
		
		return totalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// TODO: I had modeled this method like it could only hit one other target, but looking at its visual effect I think it might be able to hit more than 1 around the primary target.
		if (selectedTier5 == 1) {
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
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = calculateDamagePerBullet(true);
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double unchangingBaseSpread = 59.5;
		double changingBaseSpread = 33.5;
		double spreadVariance = 36;
		double spreadPerShot = 12;
		double spreadRecoverySpeed = 72;
		double recoilPerShot = 41;
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 5.0 / 64.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 5.0 / 16.0;
		
		double[] modifiers = {getBaseSpread(), 1.0, 1.0, 1.0, getRecoil()};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, false, getRateOfFire(), getMagazineSize(), 1, 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval, modifiers);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getElectricDamage()) * UtilityInformation.ArmorBreak_Utility;
		
		// Innate ability to Electrocute applies an 80% slow to enemies (proc chance increased/decreased by mods and OCs)
		utilityScores[3] = getElectrocutionDoTChance() * calculateMaxNumTargets() * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}
