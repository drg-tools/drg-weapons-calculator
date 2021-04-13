package weapons.scout;

import java.util.ArrayList;

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
import spreadCurves.ClassicCurve;
import utilities.ConditionalArrayList;
import utilities.MathUtils;
import weapons.Weapon;

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
	private double focusDuration;
	protected double movespeedWhileFocusing;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	public Classic(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		weaponPic = WeaponPictures.classic;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 60;
		focusedShotMultiplier = 2.0;
		carriedAmmo = 96;
		magazineSize = 8;
		rateOfFire = 4.0;
		reloadTime = 2.5;
		focusDuration = 1.0 / 1.6;  // seconds
		movespeedWhileFocusing = 0.3;
		armorBreaking = 0.3;
		
		accEstimator.setSpreadCurve(new ClassicCurve());
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Increased Caliber Rounds", "+6 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Fast-Charging Coils", "+25% Focus Speed", modIcons.chargeSpeed, 2, 0);
		tier2[1] = new Mod("Better Weight Balance", "-30% Spread per Shot, x0.8 Max Bloom, x0.5 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Hardened Rounds", "+220% Armor Breaking", modIcons.armorBreaking, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Killer Focus", "+25% Focused Shot Multiplier", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Extended Clip", "+6 Clip Size", modIcons.magSize, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "+20% Weakpoint Bonus", modIcons.weakpointBonus, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Hitting Where it Hurts", "Focused shots Stun enemies for 3 seconds", modIcons.stun, 5, 0);
		tier5[1] = new Mod("Precision Terror", "Killing an enemy with a focused shot to a weakpoint will inflict 2.5 Fear Factor on enemies within 4m of the kill", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Killing Machine", "Manually reloading within 1 second after a kill reduces reload time by 0.75 seconds", modIcons.reloadSpeed, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Hoverclock", "While Focusing in midair, your current velocity is reduced by 80% for 1.5 seconds or until you fire/stop focusing. Getting a kill or touching the ground lets you Hover again.", overclockIcons.hoverclock, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Minimal Clips", "x1.25 Clip Size, -0.2 Reload Time", overclockIcons.carriedAmmo, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Active Stability System", "No movement penalty while Focusing, +19% Focus Speed, +0.5 Reload Time", overclockIcons.movespeed, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Hipster", "+3 Rate of Fire, x1.913 Max Ammo, -10% Spread per Shot, x0.85 Max Bloom, x0.5 Recoil, -18 Direct Damage", overclockIcons.baseSpread, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Electrocuting Focus Shots", "Focused Shots apply an Electrocute DoT which does "
				+ "an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per Second for 4 seconds, -25% Focused Shot Multiplier", overclockIcons.electricity, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Supercooling Chamber", "+125% Focused Shot Multiplier, x0.665 Max Ammo, x0.6 Focus Speed, no movement while focusing", overclockIcons.directDamage, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
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
		
		if (selectedTier1 == 1) {
			toReturn += 6;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 18;
		}
		
		return toReturn;
	}
	protected double getFocusedShotMultiplier() {
		double toReturn = focusedShotMultiplier;
		
		// Additive bonuses first
		if (selectedTier3 == 0) {
			toReturn += 0.25;
		}
		
		if (selectedOverclock == 4) {
			toReturn -= 0.25;
		}
		else if (selectedOverclock == 5) {
			toReturn += 1.25;
		}
		
		return toReturn;
	}
	protected int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 40;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.913;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.665;
		}
		
		return (int) Math.round(toReturn);
	}
	protected int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 1) {
			toReturn += 6;
		}
		
		if (selectedOverclock == 1) {
			toReturn = (int) Math.ceil(toReturn * 1.25);
		}
		
		return toReturn;
	}
	protected double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier5 == 2) {
			// "Killing Machine": if you manually reload within 1 second after a kill, the reload time is reduced by approximately 0.75 seconds.
			// Because Sustained DPS uses this ReloadTime method, I'm choosing to use the Ideal Burst DPS as a quick-and-dirty estimate how often a kill gets scored 
			// so that this doesn't infinitely loop.
			double killingMachineManualReloadWindow = 1.0;
			double killingMachineReloadReduction = 0.75;
			// Just like Gunner/Minigun/Mod/5/CatG, I'm using the incorrect "guess" spawn rates to create a more believable uptime coefficient
			double burstTTK = EnemyInformation.averageHealthPool(false) / calculateSingleTargetDPS(true, false, false, false);
			// Don't let a high Burst DPS increase this beyond a 100% uptime
			double killingMachineUptimeCoefficient = Math.min(killingMachineManualReloadWindow / burstTTK, 1.0);
			double effectiveReloadReduction = killingMachineUptimeCoefficient * killingMachineReloadReduction;
			
			toReturn -= effectiveReloadReduction;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	protected double getFocusDuration() {
		double focusSpeedCoefficient = 1.0;
		if (selectedTier2 == 0) {
			focusSpeedCoefficient += 0.25;
		}
		
		if (selectedOverclock == 2) {
			focusSpeedCoefficient += 0.19;
		}
		else if (selectedOverclock == 5) {
			focusSpeedCoefficient *= 0.6;
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
		double toReturn = 0.0;
		
		if (selectedTier4 == 1) {
			toReturn += 0.2;
		}
		
		return toReturn;
	}
	protected double getArmorBreaking() {
		double toReturn = armorBreaking;
		
		if (selectedTier2 == 2) {
			toReturn += 2.2;
		}
		
		return toReturn;
	}
	protected double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.3;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 0.1;
		}
		
		return toReturn;
	}
	protected double getMaxBloom() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.8;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.85;
		}
		
		return toReturn;
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
	public double averageTimeToCauterize() {
		// Neither Hipfire nor Focused Shots can deal Temperature Damage
		return -1;
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 40 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 10 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"ex1\": { \"name\": \"Focus Speed\", \"value\": 30, \"percent\": true } }", "Icon_Upgrade_ChargeUp", "Accuracy"),
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
				+ "\"ex1\": { \"name\": \"Focus Speed\", \"value\": 20, \"percent\": true }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.5 } }", "Icon_Upgrade_MovementSpeed"),
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
