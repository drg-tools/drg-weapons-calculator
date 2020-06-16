package drillerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

/*
	Extracted via UUU:
		Charge Speed 0.7
*/

public class EPC_RegularShot extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private double chargedDirectDamage;
	private double chargedAreaDamage;
	private double chargedAoERadius;
	private int batterySize;
	private double rateOfFire;
	private double maxHeat;
	private double coolingRate;
	private int ammoPerChargedShot;
	private double chargeShotWindup;
	private double heatPerRegularShot;
	private double heatPerSecondWhileCharged;
	
	/*
 	Damage breakdown, sourced from the Wiki:
 	
		Normal Shots
		Damage type is 50% Electric and 50% Kinetic.
		
		Charged Shot (direct damage)
		Damage Type is 50% Electric and 50% Fire.
		
		Charged Shot (area damage)
		Damage type is 50% Electric and 50% Explosive.
		
		Flying Nightmare
		Damage type is Fire.
		Damage done is equal to the Charged Shot direct damage.
		
		Thin Containment Field
		Damage type is Fire.
		Damage done is 240 and is not affected by mods or overclocks.
		
		Persistent Plasma
		Damage type is Electric.
		The area last 6 seconds and deals 5 damage every 0.25 to 0.5 seconds. 
	*/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public EPC_RegularShot() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public EPC_RegularShot(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public EPC_RegularShot(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "EPC (Regular Shots)";
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
		chargeShotWindup = 1.5;  // seconds
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
		tier1[0] = new Mod("Increased Particle Density", "+5 Regular Shot Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Larger Battery", "+24 Battery Size", modIcons.carriedAmmo, 1, 1);
		tier1[2] = new Mod("Higher Charged Plasma Energy", "+15 Charged Shot Direct Damage, +15 Charged Shot Area Damage", modIcons.areaDamage, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Plasma Splash", "+1m Charged Shot AoE Radius", modIcons.aoeRadius, 2, 0);
		tier2[1] = new Mod("Overcharged Plasma Accelerator", "+25% Regular Shot Velocity", modIcons.projectileVelocity, 2, 1, false);
		tier2[2] = new Mod("Reactive Shockwave", "+15 Charged Shot Direct Damage, +15 Charged Shot Area Damage", modIcons.areaDamage, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Charge Efficiency", "-2 Ammo per Charged Shot", modIcons.fuel, 3, 0);
		tier3[1] = new Mod("Crystal Capacitors", "x2.5 Charge Speed", modIcons.chargeSpeed, 3, 1);
		tier3[2] = new Mod("Tweaked Radiator", "+50% Cooling Rate", modIcons.coolingRate, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Heat Shield", "x0.4 Heat per Second when fully charged", modIcons.coolingRate, 4, 0);
		tier4[1] = new Mod("High Density Battery", "+24 Battery Size", modIcons.carriedAmmo, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Flying Nightmare", "Charged Shots now deal their Direct Damage to enemies hit by the AoE while in-flight but it no longer explodes upon impact. Additionally, x0.55 AoE radius, x0.7 Charge Speed.", modIcons.special, 5, 0);
		tier5[1] = new Mod("Thin Containment Field", "Shoot the Charged Shot with a Regular Shot to make it detonate for an extra +240 Damage. Additionally, x0.8 Heat per Regular Shot, and x0.8 Heat per Charged Shot which means it no longer overheats on charged shots.", modIcons.special, 5, 1);
		tier5[2] = new Mod("Plasma Burn", "Regular Shots have an 50% of their Direct Damage added on as Heat Damage per shot", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Energy Rerouting", "+16 Battery Size, x1.5 Charge Speed.", overclockIcons.chargeSpeed, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Magnetic Cooling Unit", "+25% Cooling Rate, x0.7 Heat per Second while Charged.", overclockIcons.coolingRate, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Heat Pipe", "-2 Ammo per Charged Shot, x1.3 Charge Speed, x1.5 Heat per Regular Shot", overclockIcons.fuel, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Heavy Hitter", "x1.6 Regular Shot Direct Damage, x1.5 Heat per Regular Shot, -32 Battery Size", overclockIcons.directDamage, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Overcharger", "x1.5 Charged Shot Direct Damage, x1.5 Charged Shot Area Damage, x1.2 Charged Shot AoE Radius, x1.5 Ammo per Charged Shot, -25% Cooling Rate", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Persistent Plasma", "Upon impact, Charged Shots leave behind a 3m radius field of Persistent Plasma that deals " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + 
				" Electric Damage per Second for 7.6 seconds. -20 Charged Shot Direct Damage, -20 Charged Shot Area Damage", overclockIcons.hourglass, 5);
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
				case 'C': {
					selectedTier3 = 2;
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
	
	@Override
	public EPC_RegularShot clone() {
		return new EPC_RegularShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "EPC_RegularShot";
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
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 3) {
			toReturn = (int) Math.round(toReturn * 1.6);
		}
		
		return toReturn;
	}
	private double getChargedDirectDamage() {
		double toReturn = chargedDirectDamage;
		
		if (selectedTier1 == 2) {
			toReturn += 15;
		}
		if (selectedTier2 == 2) {
			toReturn += 15;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 20;
		}
		
		return toReturn;
	}
	private double getChargedAreaDamage() {
		double toReturn = chargedAreaDamage;
		
		if (selectedTier1 == 2) {
			toReturn += 15;
		}
		if (selectedTier2 == 2) {
			toReturn += 15;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 20;
		}
		
		return toReturn;
	}
	private double getChargedAoERadius() {
		double toReturn = chargedAoERadius;
		
		if (selectedTier2 == 0) {
			toReturn += 1.0;
		}
		
		if (selectedTier5 == 0) {
			toReturn *= 0.55;
		}
		if (selectedOverclock == 4) {
			toReturn *= 1.2;
		}
		
		return toReturn;
	}
	private int getBatterySize() {
		int toReturn = batterySize;
		
		if (selectedTier1 == 1) {
			toReturn += 24;
		}
		if (selectedTier4 == 1) {
			toReturn += 24;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 16;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 32;
		}
		
		return toReturn;
	}
	private double getCoolingRateModifier() {
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
	private int getAmmoPerChargedShot() {
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
		
		return toReturn;
	}
	private double getChargedShotWindup() {
		double toReturn = chargeShotWindup;
		
		if (selectedTier3 == 1) {
			toReturn /= 2.5;
		}
		if (selectedTier5 == 0) {
			toReturn /= 0.7;
		}
		
		if (selectedOverclock == 0) {
			toReturn /= 1.5;
		}
		else if (selectedOverclock == 2) {
			toReturn /= 1.3;
		}
		
		return toReturn;
	}
	private double getHeatPerRegularShot() {
		double toReturn = heatPerRegularShot;
		
		if (selectedTier5 == 1) {
			toReturn *= 0.8;
		}
		
		if (selectedOverclock == 2 || selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getHeatPerChargedShot() {
		// Unless they have Mod Tier 5 "Thin Containment Field" equipped, charged shots guarantee an overheat.
		if (selectedTier5 == 1) {
			// UUU indicates this is 0.25, but that seems wrong to me. I could believe 0.75...
			return maxHeat * 0.8;
		}
		else {
			return maxHeat;
		}
	}
	private double getHeatPerSecondWhileCharged() {
		double toReturn = heatPerSecondWhileCharged;
		
		if (selectedTier4 == 0) {
			toReturn *= 0.4;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= 0.7;
		}
		
		return toReturn;
	}
	private double getRegularShotVelocity() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	
	private int getNumRegularShotsBeforeOverheat() {
		double k = getCoolingRateModifier();
		double h = getHeatPerRegularShot();
		
		double exactAnswer = (maxHeat * rateOfFire) / (rateOfFire * h - k * coolingRate);
		
		return (int) Math.ceil(exactAnswer);
	}
	private double getSecondsBeforeOverheatWhileCharged() {
		return maxHeat / getHeatPerSecondWhileCharged();
	}
	private double getCooldownDuration() {
		return maxHeat / (coolingRate * getCoolingRateModifier());
	}

	@Override
	public StatsRow[] getStats() {
		boolean coolingRateModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 4;
		
		StatsRow[] toReturn = new StatsRow[8];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), selectedTier1 == 0 || selectedOverclock == 3);
		
		toReturn[1] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getRegularShotVelocity()), selectedTier2 == 1, selectedTier2 == 1);
		
		boolean heatPerShotModified = selectedTier5 == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[2] = new StatsRow("Heat/Shot:", getHeatPerRegularShot(), heatPerShotModified);
		
		toReturn[3] = new StatsRow("Shots Fired Before Overheating:", getNumRegularShotsBeforeOverheat(), coolingRateModified || heatPerShotModified);
		
		boolean batterySizeModified = selectedTier1 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Battery Size:", getBatterySize(), batterySizeModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", rateOfFire, false);
		
		toReturn[6] = new StatsRow("Cooling Rate:", convertDoubleToPercentage(getCoolingRateModifier()), coolingRateModified);
		
		toReturn[7] = new StatsRow("Cooldown After Overheating:", getCooldownDuration(), coolingRateModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Because this only models the regular shots of the EPC, it will never do splash damage.
		return false;
	}

	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean weakpoint) {
		double damagePerProjectile;
		if (weakpoint && !statusEffects[1]) {
			// Because this weapon doesn't have its Accuracy handled like the other weapons, I'm choosing to just increase the damage by a weighted average.
			damagePerProjectile = increaseBulletDamageForWeakpoints(getDirectDamage());
		}
		else {
			damagePerProjectile = getDirectDamage();
		}
		
		// Frozen
		if (statusEffects[1]) {
			damagePerProjectile *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			damagePerProjectile *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		int burstSize = getNumRegularShotsBeforeOverheat();
		
		double duration;
		if (burst) {
			duration = burstSize / rateOfFire;
		}
		else {
			duration = burstSize / rateOfFire + getCooldownDuration();
		}
		
		double burnDPS = 0;
		if (selectedTier5 == 2 && !statusEffects[1]) {
			if (burst) {
				// 50% of Direct Damage from the Regular Shots gets added on as Heat Damage.
				double heatDamagePerShot = 0.5 * getDirectDamage();
				double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatDamagePerShot, rateOfFire);
				double fireDoTUptimeCoefficient = (duration - timeToIgnite) / duration;
				
				burnDPS = fireDoTUptimeCoefficient * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		return damagePerProjectile * burstSize / duration + burnDPS;
	}
	
	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false);
	}

	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// EPC has no recoil and no spread per shot, so it can effectively be considered 100% accurate
		return calculateSingleTargetDPS(false, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		// Regular shots can only hit one enemy before disappearing.
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double baseDamage = getDirectDamage() * getBatterySize();
		
		double fireDoTTotalDamage = 0;
		if (selectedTier5 == 2) {
			
			double estimatedNumEnemiesKilled = calculateFiringDuration() / averageTimeToKill();
			double heatDamagePerShot = 0.5 * getDirectDamage();
			double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatDamagePerShot, rateOfFire);
			double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return baseDamage + fireDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// Regular shots can only hit one enemy before disappearing.
		return 1;
	}

	@Override
	public double calculateFiringDuration() {
		int burstSize = getNumRegularShotsBeforeOverheat();
		double timeToFireBurst = burstSize / rateOfFire;
		// Choosing not to use Weapon.numMagazines since the "burst" size isn't adding to total ammo count like normal bullets in a mag do.
		double numBursts = (double) getBatterySize() / (double) burstSize;
		return numBursts * timeToFireBurst + numReloads(getBatterySize(), burstSize) * getCooldownDuration();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		// TODO: should this be increased by Weakpoint bonus?
		double dmgPerShot = getDirectDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			0.5 * getDirectDamage(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0.5 * getDirectDamage()  // Electric
		};
		
		double[] areaDamage = {
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double burnDmg = 0;
		if (selectedTier5 == 2) {
			burnDmg = calculateAverageDoTDamagePerEnemy(EnemyInformation.averageTimeToIgnite(0.5 * getDirectDamage(), rateOfFire), DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
		}
		double[] DoTDamage = {
			burnDmg,  // Fire
			0,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, 0.0, 0.0, 0.0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// EPC doesn't have any utility
		// EPC regular shots also cannot break Light Armor plates
		return 0;
	}
	
	@Override
	public double damagePerMagazine() {
		double baseDamage = getNumRegularShotsBeforeOverheat() * getDirectDamage();
		double fireDoTDamage = 0;
		if (selectedTier5 == 2) {
			double heatDamagePerShot = 0.5 * getDirectDamage();
			double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatDamagePerShot, rateOfFire);
			fireDoTDamage = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
		}
		return baseDamage + fireDoTDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getNumRegularShotsBeforeOverheat() / rateOfFire;
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.add(String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 5 } }", "Icon_Upgrade_DamageGeneral"));
		toReturn.add(String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Battery Capacity\", \"value\": 24 } }", "Icon_Upgrade_Ammo"));
		toReturn.add(String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[2].getText(true), "{ \"ex1\": { \"name\": \"Charged Damage\", \"value\": 15 }, "
				+ "\"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 15 } }", "Icon_Upgrade_AreaDamage"));
		
		// Tier 2
		toReturn.add(String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[0].getText(true), "{ \"ex3\": { \"name\": \"Charged Effect Radius\", \"value\": 1 } }", "Icon_Upgrade_Area"));
		toReturn.add(String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[1].getText(true), "{ \"ex7\": { \"name\": \"Normal Projectile Velocity\", \"value\": 25, \"percent\": true } }", "Icon_Upgrade_ProjectileSpeed"));
		toReturn.add(String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 18, 0, 12, 0, 0, tier2[2].getText(true), "{ \"ex1\": { \"name\": \"Charged Damage\", \"value\": 15 }, "
				+ "\"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 15 } }", "Icon_Upgrade_AreaDamage"));
		
		// Tier 3
		toReturn.add(String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 30, 0, 0, 0, 20, 0, tier3[0].getText(true), "{ \"ex4\": { \"name\": \"Charged Shot Ammo Use\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_Fuel"));
		toReturn.add(String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 30, 0, 20, tier3[1].getText(true), "{ \"ex5\": { \"name\": \"Charge Speed\", \"value\": 2.5, \"multiply\": true } }", "Icon_Upgrade_ChargeUp"));
		toReturn.add(String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2200, 0, 0, 30, 0, 20, 0, tier3[2].getText(true), "{ \"reload\": { \"name\": \"Cooling Rate\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_TemperatureCoolDown"));
		
		// Tier 4
		toReturn.add(String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 0, 15, 0, 36, 25, tier4[0].getText(true), "{ \"ex6\": { \"name\": \"Heat Buildup When Charged\", \"value\": 0.4, \"multiply\": true } }", "Icon_Upgrade_TemperatureCoolDown"));
		toReturn.add(String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 15, 0, 0, 0, 0, tier4[1].getText(true), "{ \"clip\": { \"name\": \"Battery Capacity\", \"value\": 24 } }", "Icon_Upgrade_Ammo"));
		
		// Tier 5
		toReturn.add(String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[0].getText(true), "{ \"ex9\": { \"name\": \"Flying Nightmare\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex5\": { \"name\": \"Charge Speed\", \"value\": 0.7, \"multiply\": true } }", "Icon_Upgrade_Special"));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[1].getText(true), "{ \"ex10\": { \"name\": \"No Charged Shot Insta-Overheat\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex12\": { \"name\": \"Normal Shot Heat Generation\", \"value\": 0.8, \"percent\": true, \"multiply\": true }, \"ex8\": { \"name\": \"Thin Containment Field\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Special"));
		toReturn.add(String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 4400, 40, 0, 0, 0, 110, 60, tier5[2].getText(true), "{ \"ex11\": { \"name\": \"Plasma Burn\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Heat"));
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.add(String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7300, 0, 130, 65, 0, 0, 100, overclocks[0].getText(true), "{ \"clip\": { \"name\": \"Battery Capacity\", \"value\": 16 }, "
				+ "\"ex5\": { \"name\": \"Charge Speed\", \"value\": 1.5, \"multiply\": true } }", "Icon_Upgrade_ChargeUp"));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8900, 0, 0, 125, 95, 0, 80, overclocks[1].getText(true), "{ \"reload\": { \"name\": \"Cooling Rate\", \"value\": 25, \"percent\": true },  "
				+ "\"ex6\": { \"name\": \"Heat Buildup When Charged\", \"value\": 0.7, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_TemperatureCoolDown"));
		
		// Balanced
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7450, 0, 60, 125, 0, 0, 95, overclocks[2].getText(true), "{ \"ex4\": { \"name\": \"Charged Shot Ammo Use\", \"value\": 2, \"subtract\": true }, "
				+ "\"ex5\": { \"name\": \"Charge Speed\", \"value\": 1.3, \"multiply\": true }, \"ex12\": { \"name\": \"Normal Shot Heat Generation\", \"value\": 1.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Fuel"));
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8100, 60, 140, 105, 0, 0, 0, overclocks[3].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1.6, \"multiply\": true }, "
				+ "\"clip\": { \"name\": \"Battery Capacity\", \"value\": 32, \"subtract\": true }, \"ex12\": { \"name\": \"Normal Shot Heat Generation\", \"value\": 1.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"));
		
		// Unstable
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7050, 0, 120, 0, 95, 60, 0, overclocks[4].getText(true), "{ \"ex1\": { \"name\": \"Charged Damage\", \"value\": 1.5, \"multiply\": true }, "
				+ "\"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 1.5, \"multiply\": true }, \"ex3\": { \"name\": \"Charged Effect Radius\", \"value\": 1.2, \"multiply\": true }, \"ex4\": { \"name\": \"Charged Shot Ammo Use\", \"value\": 1.5, \"multiply\": true }, "
				+ "\"reload\": { \"name\": \"Cooling Rate\", \"value\": 25, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"));
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8150, 95, 0, 0, 75, 0, 130, overclocks[5].getText(true), "{ \"ex13\": { \"name\": \"Persistent Plasma\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex1\": { \"name\": \"Charged Damage\", \"value\": 20, \"subtract\": true }, \"ex2\": { \"name\": \"Charged Area Damage\", \"value\": 20, \"subtract\": true } }", "Icon_Upgrade_Duration"));
		
		return toReturn;
	}
}
