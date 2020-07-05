package scoutWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public abstract class Classic extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double focusedShotMultiplier;
	protected double carriedAmmo;
	protected int magazineSize;
	protected double rateOfFire;
	private double reloadTime;
	protected double delayBeforeFocusing;
	private double focusDuration;
	protected double movespeedWhileFocusing;
	private double weakpointBonus;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	public Classic(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "M1000 Classic (Hipfired)";
		weaponPic = WeaponPictures.classic;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 50;
		focusedShotMultiplier = 2.0;
		carriedAmmo = 96;
		magazineSize = 8;
		rateOfFire = 4.0;
		reloadTime = 2.5;
		delayBeforeFocusing = 0.2;  // seconds
		focusDuration = 0.8;  // seconds
		movespeedWhileFocusing = 0.3;
		weakpointBonus = 0.1;
		armorBreaking = 0.3;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+32 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Increased Caliber Rounds", "x1.2 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Fast-Charging Coils", "x1.6 Focus Speed", modIcons.chargeSpeed, 2, 0);
		tier2[1] = new Mod("Better Weight Balance", "x0.8 Spread per Shot, x0.5 Recoil", modIcons.recoil, 2, 1);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Killer Focus", "+25% Focused Shot Multiplier", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Extended Clip", "+6 Magazine Size", modIcons.magSize, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "+25% Weakpoint Bonus", modIcons.weakpointBonus, 4, 1);
		tier4[2] = new Mod("Hardened Rounds", "+220% Armor Breaking", modIcons.armorBreaking, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Hitting Where it Hurts", "Focused shots Stun enemies for 3 seconds", modIcons.stun, 5, 0);
		tier5[1] = new Mod("Precision Terror", "Killing an enemy with a focused shot to a weakspot will inflict Fear on enemies within 3.5m of the kill", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Killing Machine", "Manually reloading within 1 second after a kill reduces reload time by 0.75 seconds", modIcons.reloadSpeed, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Hoverclock", "Your movement slows down for a few seconds while using focus mode in the air.", overclockIcons.hoverclock, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Minimal Clips", "+16 Max Ammo, -0.2 Reload Time", overclockIcons.carriedAmmo, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Active Stability System", "No movement penalty while Focusing, -25% Focused Shot Multiplier", overclockIcons.movespeed, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Hipster", "+3 Rate of Fire, x1.75 Max Ammo, x0.4 Delay Before Focusing, x0.85 Spread per Shot, +75% Spread Recovery Speed, x0.5 Recoil, x0.6 Direct Damage", overclockIcons.baseSpread, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Electrocuting Focus Shots", "Focused Shots apply an Electrocute DoT which does "
				+ "an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per Second for 4 seconds, -25% Focused Shot Multiplier", overclockIcons.electricity, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Supercooling Chamber", "+125% Focused Shot Multiplier, x0.635 Max Ammo, x0.5 Focus Speed, no movement while focusing", overclockIcons.directDamage, 5);
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
				System.out.println("Classic's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Classic's second tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Classic's third tier of mods only has two choices, so 'C' is an invalid choice.");
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
				case 'C': {
					selectedTier4 = 2;
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
				case 'C': {
					selectedTier5 = 2;
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
	
	public String getDwarfClass() {
		return "Scout";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.classicGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	protected double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedOverclock == 3) {
			toReturn -= 20;
		}
		
		// Multiplicative bonuses last
		if (selectedTier1 == 1) {
			toReturn *= 1.2;
		}
		
		return toReturn;
	}
	protected double getFocusedShotMultiplier() {
		double toReturn = focusedShotMultiplier;
		
		// Additive bonuses first
		if (selectedTier3 == 0) {
			toReturn += 0.25;
		}
		
		if (selectedOverclock == 2 || selectedOverclock == 4) {
			toReturn -= 0.25;
		}
		else if (selectedOverclock == 5) {
			toReturn += 1.25;
		}
		
		return toReturn;
	}
	protected abstract int getCarriedAmmo();
	protected abstract int getMagazineSize();
	protected abstract double getRateOfFire();
	protected double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier5 == 2) {
			// "Killing Machine": if you manually reload within 1 second after a kill, the reload time is reduced by approximately 0.75 seconds.
			// Because Sustained DPS uses this ReloadTime method, I'm choosing to use the Ideal Burst DPS as a quick-and-dirty estimate how often a kill gets scored 
			// so that this doesn't infinitely loop.
			double killingMachineManualReloadWindow = 1.0;
			double killingMachineReloadReduction = 0.75;
			// Just like Gunner/Minigun/Mod/5/CatG, I'm using the incorrect "guess" spawn rates to create a more believable uptime coefficient
			double burstTTK = EnemyInformation.averageHealthPool(false) / calculateIdealBurstDPS();
			// Don't let a high Burst DPS increase this beyond a 100% uptime
			double killingMachineUptimeCoefficient = Math.min(killingMachineManualReloadWindow / burstTTK, 1.0);
			double effectiveReloadReduction = killingMachineUptimeCoefficient * killingMachineReloadReduction;
			
			toReturn -= effectiveReloadReduction;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	protected double getFocusDelay() {
		double toReturn = delayBeforeFocusing;
		// Thanks to LoneXG for telling me that Hipster reduces delay before Focusing
		if (selectedOverclock == 3) {
			toReturn *= 0.4;
		}
		
		return toReturn;
	}
	protected double getFocusDuration() {
		double focusSpeedCoefficient = 1.0;
		if (selectedTier2 == 0) {
			focusSpeedCoefficient *= 1.6;
		}
		
		if (selectedOverclock == 5) {
			focusSpeedCoefficient *= 0.5;
		}
		
		return focusDuration / focusSpeedCoefficient;
	}
	protected double getMovespeedWhileFocusing() {
		double modifier = movespeedWhileFocusing;
		
		if (selectedOverclock == 2) {
			modifier += 0.7;
		}
		else if (selectedOverclock == 5) {
			modifier *= 0;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	protected int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	protected double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	protected double getArmorBreaking() {
		double toReturn = armorBreaking;
		
		if (selectedTier4 == 2) {
			toReturn += 2.2;
		}
		
		return toReturn;
	}
	protected double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.8;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.85;
		}
		
		return toReturn;
	}
	protected double getSpreadRecoverySpeed() {
		if (selectedOverclock == 3) {
			return 1.75;
		}
		else {
			return 1.0;
		}
	}
	protected double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	protected int getStunDuration() {
		if (selectedTier5 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	// Single-target calculations
	protected abstract double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint);

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

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateIdealSustainedDPS();
		}
		else {
			return 0;
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}
	
	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 32 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1.2, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"ex1\": { \"name\": \"Focus Speed\", \"value\": 1.6, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_ChargeUp", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 24, 0, 15, 0, 0, tier2[1].getText(true), "{ \"ex3\": { \"name\": \"Recoil\", \"value\": 0.5, \"multiply\": true } }", "Icon_Upgrade_Recoil", "Recoil"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 50, 0, 0, 0, 35, 0, tier3[0].getText(true), "{ \"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 25, \"percent\": true } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 0, 0, 0, 0, 35, 50, tier3[1].getText(true), "{ \"clip\": { \"name\": \"Clip Size\", \"value\": 6 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 48, 0, 0, 0, 50, 72, tier4[0].getText(true), "{ \"ex4\": { \"name\": \"Max Penetrations\", \"value\": 3 } }", "Icon_Upgrade_BulletPenetration", "Blow Through"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 0, 72, 0, 48, 50, 0, tier4[1].getText(true), "{ \"ex5\": { \"name\": \"Weakpoint Damage\", \"value\": 25, \"percent\": true } }", "Icon_Upgrade_Weakspot", "Weak Spot Bonus"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 4800, 0, 0, 0, 48, 50, 72, tier4[2].getText(true), "{ \"ex6\": { \"name\": \"Armor Breaking\", \"value\": 220, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 64, 70, 0, 140, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Focused Shot Stun Chance\", \"value\": 100, \"percent\": true } }", "Icon_Upgrade_Stun", "Stun"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 70, 140, 0, 0, 0, tier5[1].getText(true), "{ \"ex10\": { \"name\": \"Focus Shot Kill AoE Fear\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_ScareEnemies", "Fear"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 5600, 70, 0, 64, 0, 140, 0, tier5[2].getText(true), "{ \"ex10\": { \"name\": \"Quick Reload After Kill\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Speed", "Reload Speed"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7350, 0, 105, 0, 135, 0, 65, overclocks[0].getText(true), "{ \"ex11\": { \"name\": \"Focus Shot Hover\", \"value\": 1, \"boolean\": true } }", "Icon_Overclock_Slowdown"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8200, 75, 0, 0, 0, 95, 130, overclocks[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 16 }, "
						+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 8150, 70, 90, 135, 0, 0, 0, overclocks[2].getText(true), "{ \"ex8\": { \"name\": \"Focus Mode Movement Speed\", \"value\": 70, \"percent\": true }, "
				+ "\"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 25, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_MovementSpeed"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8900, 0, 0, 80, 125, 105, 0, overclocks[3].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 1.75, \"multiply\": true }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 3 }, \"ex3\": { \"name\": \"Recoil\", \"value\": 0.5, \"multiply\": true }, \"dmg\": { \"name\": \"Damage\", \"value\": 0.6, \"multiply\": true } }", "Icon_Upgrade_Aim"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8850, 0, 120, 75, 95, 0, 0, overclocks[4].getText(true), "{ \"ex12\": { \"name\": \"Electrocuting Focus Shots\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 25, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Electricity"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8500, 70, 0, 0, 0, 90, 130, overclocks[5].getText(true), "{ \"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 125, \"percent\": true }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 0.635, \"multiply\": true }, \"ex1\": { \"name\": \"Focus Speed\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, "
				+ "\"ex8\": { \"name\": \"Focus Mode Movement Speed\", \"value\": 0, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
