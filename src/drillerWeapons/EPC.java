package drillerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

/*
	Extracted via UUU:
		Charge Speed 0.7
*/

public abstract class EPC extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private double chargedDirectDamage;
	private double chargedAreaDamage;
	private double chargedAoERadius;
	private int batterySize;
	// This needs to be protected instead of private so that EPC_RegularShot.getStats() can access its static value.
	protected double rateOfFire;
	protected double maxHeat;
	protected double coolingRate;
	private int ammoPerChargedShot;
	private double chargeShotWindup;
	private double heatPerRegularShot;
	private double heatPerSecondWhileCharged;
	
	/*
 	Damage breakdown, sourced from the Wiki:
 	
		Normal Shots
		Damage type is 50% Electric and 50% Kinetic.
		
		Charged Shot (direct damage)
		65% Electric / 25 % Fire / 10% Disintegrate for the single target part.
		
		Charged Shot (area damage)
		65% Explosive / 25% Fire / 10% Disintegrate for the AoE part.
		
		Flying Nightmare
		Damage type is Fire.
		Damage done is equal to the Charged Shot direct damage.
		
		Thin Containment Field
		Damage type is Fire.
		Damage done is 240 and is not affected by mods or overclocks.
		
		Persistent Plasma
		Damage type is Electric.
		The area last 6 seconds and deals 5 damage every 0.25 seconds. 
	*/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	public EPC(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		weaponPic = WeaponPictures.EPC;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 20;
		chargedDirectDamage = 60;
		chargedAreaDamage = 60;
		chargedAoERadius = 2.0;
		batterySize = 120;
		rateOfFire = 8.0;
		maxHeat = 1.0;
		coolingRate = 0.4;
		ammoPerChargedShot = 8;
		chargeShotWindup = 1.0 / 0.7;  // seconds
		heatPerRegularShot = 0.13;
		heatPerSecondWhileCharged = 2.0;
		
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
		tier1[0] = new Mod("Increased Particle Density", "+4 Regular Shot Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Larger Battery", "+24 Battery Size", modIcons.carriedAmmo, 1, 1);
		tier1[2] = new Mod("Higher Charged Plasma Energy", "+15 Charged Shot Direct Damage, +15 Charged Shot Area Damage", modIcons.areaDamage, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Plasma Splash", "+0.8m Charged Shot AoE Radius", modIcons.aoeRadius, 2, 0);
		tier2[1] = new Mod("High Density Battery", "+32 Battery Size", modIcons.carriedAmmo, 2, 1);
		tier2[2] = new Mod("Reactive Shockwave", "+20 Charged Shot Direct Damage, +20 Charged Shot Area Damage", modIcons.areaDamage, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Charge Efficiency", "-2 Ammo per Charged Shot", modIcons.fuel, 3, 0);
		tier3[1] = new Mod("Crystal Capacitors", "x2.4 Charge Speed", modIcons.chargeSpeed, 3, 1);
		tier3[2] = new Mod("Tweaked Radiator", "+50% Cooling Rate", modIcons.coolingRate, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Heat Shield", "x0.4 Heat per Second when fully charged", modIcons.coolingRate, 4, 0);
		tier4[1] = new Mod("Overcharged Plasma Accelerator", "+25% Regular Shot Velocity", modIcons.projectileVelocity, 4, 1, false);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Flying Nightmare", "Charged Shots now deal their Direct Damage to enemies hit by the AoE while in-flight but it no longer explodes upon impact. Additionally, x1.15 Charged Shot Direct Damage, x0.6 AoE radius, x0.8 Charge Speed.", modIcons.aoeRadius, 5, 0);
		tier5[1] = new Mod("Thin Containment Field", "Shoot the Charged Shot with a Regular Shot before it impacts anything to make it detonate for 210 Damage and carve terrain within a 2.5m radius. "
				+ "Additionally, x0.8 Heat per Regular Shot, and x0.25 Heat per Charged Shot which means it no longer overheats on charged shots.", modIcons.special, 5, 1);
		tier5[2] = new Mod("Plasma Burn", "Regular Shots have 50% of their Direct Damage added on as Heat Damage per shot", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Energy Rerouting", "+16 Battery Size, x1.5 Charge Speed.", overclockIcons.chargeSpeed, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Magnetic Cooling Unit", "+25% Cooling Rate, +20% Regular Shot Weakpoint Bonus", overclockIcons.coolingRate, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Heat Pipe", "-2 Ammo per Charged Shot, x1.3 Charge Speed, -0.5m AoE Radius", overclockIcons.fuel, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Heavy Hitter", "x1.5 Regular Shot Direct Damage, x1.5 Heat per Regular Shot, +200% Recoil, -40 Battery Size", overclockIcons.directDamage, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Overcharger", "x1.5 Charged Shot Direct Damage, x1.5 Charged Shot Area Damage, x1.2 Charged Shot AoE Radius, x1.5 Ammo per Charged Shot, -25% Cooling Rate", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Persistent Plasma", "Upon impact, Charged Shots leave behind a 3m radius field of Persistent Plasma that deals " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + 
				" Fire Damage per Second for 7.6 seconds. -15 Charged Shot Direct Damage, -15 Charged Shot Area Damage", overclockIcons.hourglass, 5);
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
			if (symbols[3] == 'C') {
				System.out.println("EPC's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
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
				case 'C': {
					setSelectedModAtTier(5, 2, false);
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
			
			// Re-set AoE Efficiency
			setAoEEfficiency();
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.drillerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.EPCGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	protected int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 4;
		}
		
		if (selectedOverclock == 3) {
			toReturn = (int) Math.round(toReturn * 1.5);
		}
		
		return toReturn;
	}
	protected double getRegularShotWeakpointBonus() {
		if (selectedOverclock == 1) {
			return 0.2;
		}
		else {
			return 0.0;
		}
	}
	protected double getChargedDirectDamage() {
		double toReturn = chargedDirectDamage;
		
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			return 0;
		}
		
		if (selectedTier1 == 2) {
			toReturn += 15;
		}
		if (selectedTier2 == 2) {
			toReturn += 20;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 15;
		}
		
		// Multiplicative Damage boosts last
		if (selectedTier5 == 0) {
			toReturn *= 1.15;
		}
		
		return toReturn;
	}
	protected double getChargedAreaDamage() {
		double toReturn = chargedAreaDamage;
		
		// Early exit condition: if Flying Nightmare is equipped, then the projectile no longer explodes upon impact, which effectively sets the Area Damage to 0.
		if (selectedTier5 == 0) {
			return 0;
		}
		
		if (selectedTier1 == 2) {
			toReturn += 15;
		}
		if (selectedTier2 == 2) {
			toReturn += 20;
		}
		
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			return 210;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 15;
		}
		
		return toReturn;
	}
	protected double getChargedAoERadius() {
		double toReturn = chargedAoERadius;
		
		if (selectedTier2 == 0) {
			toReturn += 0.8;
		}
		if (selectedOverclock == 2) {
			toReturn -= 0.5;
		}
		
		if (selectedTier5 == 0) {
			toReturn *= 0.6;
		}
		if (selectedOverclock == 4) {
			toReturn *= 1.2;
		}
		
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			return 2.5;
		}
		
		return toReturn;
	}
	protected int getBatterySize() {
		int toReturn = batterySize;
		
		if (selectedTier1 == 1) {
			toReturn += 24;
		}
		if (selectedTier2 == 1) {
			toReturn += 32;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 16;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 40;
		}
		
		return toReturn;
	}
	protected double getCoolingRateModifier() {
		double modifier = 1.0;
		
		if (selectedTier3 == 2) {
			modifier += 0.5;
		}
		
		if (selectedOverclock == 1) {
			modifier += 0.25;
		}
		else if (selectedOverclock == 4) {
			modifier -= 0.25;
		}
		
		return modifier;
	}
	protected int getAmmoPerChargedShot() {
		int toReturn = ammoPerChargedShot;
		
		if (selectedTier3 == 0) {
			toReturn -= 2;
		}
		
		if (selectedOverclock == 2) {
			toReturn -= 2;
		}
		else if (selectedOverclock == 4) {
			toReturn = (int) Math.round(toReturn * 1.5);
		}
		
		if (selectedTier5 == 1) {
			// Thin Containment Field costs 1 additional ammo to fire a regular shot to detonate the TFC projectile for the +240 AoE damage
			toReturn += 1;
		}
		
		return toReturn;
	}
	protected double getChargedShotWindup() {
		double toReturn = chargeShotWindup;
		
		if (selectedTier3 == 1) {
			toReturn /= 2.4;
		}
		if (selectedTier5 == 0) {
			toReturn /= 0.8;
		}
		
		if (selectedOverclock == 0) {
			toReturn /= 1.5;
		}
		else if (selectedOverclock == 2) {
			toReturn /= 1.3;
		}
		
		return toReturn;
	}
	protected double getHeatPerRegularShot() {
		double toReturn = heatPerRegularShot;
		
		if (selectedTier5 == 1) {
			toReturn *= 0.8;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	protected double getHeatPerChargedShot() {
		// Unless they have Mod Tier 5 "Thin Containment Field" equipped, charged shots guarantee an overheat.
		if (selectedTier5 == 1) {
			// If TFC is equipped, then the Charged Shot only costs 25% max heat, and one Regular Shot to detonate the TFC field
			// Don't let this return more than the max heat, though!
			return Math.min(maxHeat * 0.25 + getHeatPerRegularShot(), maxHeat);
		}
		else {
			return maxHeat;
		}
	}
	protected double getHeatPerSecondWhileCharged() {
		double toReturn = heatPerSecondWhileCharged;
		
		if (selectedTier4 == 0) {
			toReturn *= 0.4;
		}
		
		return toReturn;
	}
	protected double getRegularShotVelocity() {
		double toReturn = 1.0;
		
		if (selectedTier4 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	
	protected double getSecondsBeforeOverheatWhileCharged() {
		return maxHeat / getHeatPerSecondWhileCharged();
	}
	protected double getCooldownDuration() {
		return getHeatPerChargedShot() / (coolingRate * getCoolingRateModifier());
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return -1.0;
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 5 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Battery Capacity\", \"value\": 24 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[2].getText(true), "{ \"ex1\": { \"name\": \"Charged Damage\", \"value\": 15 }, "
				+ "\"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 15 } }", "Icon_Upgrade_AreaDamage", "Damage"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[0].getText(true), "{ \"ex3\": { \"name\": \"Charged Effect Radius\", \"value\": 1 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[1].getText(true), "{ \"ex7\": { \"name\": \"Normal Projectile Velocity\", \"value\": 25, \"percent\": true } }", "Icon_Upgrade_ProjectileSpeed", "Projectile Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 18, 0, 12, 0, 0, tier2[2].getText(true), "{ \"ex1\": { \"name\": \"Charged Damage\", \"value\": 15 }, "
				+ "\"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 15 } }", "Icon_Upgrade_AreaDamage", "Area Damage"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 30, 0, 0, 0, 20, 0, tier3[0].getText(true), "{ \"ex4\": { \"name\": \"Charged Shot Ammo Use\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_Fuel", "Energy Consumption"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 30, 0, 20, tier3[1].getText(true), "{ \"ex5\": { \"name\": \"Charge Speed\", \"value\": 2.5, \"multiply\": true } }", "Icon_Upgrade_ChargeUp", "Charge Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2200, 0, 0, 30, 0, 20, 0, tier3[2].getText(true), "{ \"reload\": { \"name\": \"Cooling Rate\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_TemperatureCoolDown", "Cooling"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 0, 15, 0, 36, 25, tier4[0].getText(true), "{ \"ex6\": { \"name\": \"Heat Buildup When Charged\", \"value\": 0.4, \"multiply\": true } }", "Icon_Upgrade_TemperatureCoolDown", "Cooling"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 15, 0, 0, 0, 0, tier4[1].getText(true), "{ \"clip\": { \"name\": \"Battery Capacity\", \"value\": 24 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[0].getText(true), "{ \"ex9\": { \"name\": \"Flying Nightmare\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex5\": { \"name\": \"Charge Speed\", \"value\": 0.8, \"multiply\": true } }", "Icon_Upgrade_Area", "Special"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[1].getText(true), "{ \"ex10\": { \"name\": \"No Charged Shot Insta-Overheat\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex12\": { \"name\": \"Normal Shot Heat Generation\", \"value\": 0.8, \"percent\": true, \"multiply\": true }, \"ex8\": { \"name\": \"Thin Containment Field\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Special", "Special"),
				exportAllMods || false);;
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 4400, 40, 0, 0, 0, 110, 60, tier5[2].getText(true), "{ \"ex11\": { \"name\": \"Plasma Burn\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Heat", "Heat"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7300, 0, 130, 65, 0, 0, 100, overclocks[0].getText(true), "{ \"clip\": { \"name\": \"Battery Capacity\", \"value\": 16 }, "
				+ "\"ex5\": { \"name\": \"Charge Speed\", \"value\": 1.5, \"multiply\": true } }", "Icon_Upgrade_ChargeUp"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8900, 0, 0, 125, 95, 0, 80, overclocks[1].getText(true), "{ \"reload\": { \"name\": \"Cooling Rate\", \"value\": 25, \"percent\": true },  "
				+ "\"ex6\": { \"name\": \"Heat Buildup When Charged\", \"value\": 0.7, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_TemperatureCoolDown"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7450, 0, 60, 125, 0, 0, 95, overclocks[2].getText(true), "{ \"ex4\": { \"name\": \"Charged Shot Ammo Use\", \"value\": 2, \"subtract\": true }, "
				+ "\"ex5\": { \"name\": \"Charge Speed\", \"value\": 1.3, \"multiply\": true }, \"ex12\": { \"name\": \"Normal Shot Heat Generation\", \"value\": 1.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Fuel"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8100, 60, 140, 105, 0, 0, 0, overclocks[3].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1.6, \"multiply\": true }, "
				+ "\"clip\": { \"name\": \"Battery Capacity\", \"value\": 32, \"subtract\": true }, \"ex12\": { \"name\": \"Normal Shot Heat Generation\", \"value\": 1.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7050, 0, 120, 0, 95, 60, 0, overclocks[4].getText(true), "{ \"ex1\": { \"name\": \"Charged Damage\", \"value\": 1.5, \"multiply\": true }, "
				+ "\"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 1.5, \"multiply\": true }, \"ex3\": { \"name\": \"Charged Effect Radius\", \"value\": 1.2, \"multiply\": true }, \"ex4\": { \"name\": \"Charged Shot Ammo Use\", \"value\": 1.5, \"multiply\": true }, "
				+ "\"reload\": { \"name\": \"Cooling Rate\", \"value\": 25, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8150, 95, 0, 0, 75, 0, 130, overclocks[5].getText(true), "{ \"ex13\": { \"name\": \"Persistent Plasma\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex1\": { \"name\": \"Charged Damage\", \"value\": 20, \"subtract\": true }, \"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 20, \"subtract\": true } }", "Icon_Upgrade_Duration"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
