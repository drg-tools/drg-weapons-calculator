package weapons.driller;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import utilities.MathUtils;
import weapons.Weapon;

/*
	Extracted via UUU:
		Charge Speed 0.6
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
	private double heatPerChargedShot;
	private double heatPerSecondWhileCharged;
	
	/*
 	Damage breakdown, sourced from Elythnwaen:
 	
		Normal Shots
		Damage type is 50% Electric and 50% Kinetic.
		
		Charged Shot (direct damage)
		65% Electric / 25 % Fire / 10% Disintegrate for the single target part.
		
		Charged Shot (area damage)
		65% Explosive / 25% Fire / 10% Disintegrate for the AoE part.
		
		Flying Nightmare
		Damage type inherits from normal Charged Shots
		Damage done is equal to the Charged Shot direct damage.
		
		Thin Containment Field
		Damage type is Fire.
		Damage done is 240 and is not affected by mods or overclocks.
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
		chargeShotWindup = 1.0 / 0.6;  // seconds
		heatPerRegularShot = 0.13;
		heatPerChargedShot = 0.4;
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
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Heat Shield", "x0.4 Heat per Second when fully charged", modIcons.coolingRate, 2, 0);
		tier2[1] = new Mod("Overcharged Plasma Accelerator", "+25% Regular Shot Velocity", modIcons.projectileVelocity, 2, 1, false);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Charge Efficiency", "-2 Ammo per Charged Shot", modIcons.fuel, 3, 0);
		tier3[1] = new Mod("Crystal Capacitors", "x2.5 Charge Speed", modIcons.chargeSpeed, 3, 1);
		tier3[2] = new Mod("Tweaked Radiator", "+50% Cooling Rate", modIcons.coolingRate, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Expanded Plasma Splash", "+1m Charged Shot AoE Radius", modIcons.aoeRadius, 4, 0);
		tier4[1] = new Mod("High Density Battery", "+24 Battery Size", modIcons.carriedAmmo, 4, 1);
		tier4[2] = new Mod("Reactive Shockwave", "+15 Charged Shot Direct Damage, +15 Charged Shot Area Damage", modIcons.areaDamage, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Flying Nightmare", "Charged Shots now deal their Direct Damage to enemies hit by the projectile while in-flight, but it no longer explodes upon impact (functionally removing Area Damage). Deals x3 damage vs Frozen targets. "
				+ "Additionally: x1.2 Charged Shot Direct Damage, x0.4 AoE Radius.", modIcons.aoeRadius, 5, 0);
		tier5[1] = new Mod("Thin Containment Field", "Shoot the Charged Shot with a Regular Shot before it impacts anything to make it detonate for 240 Damage and carve terrain within a 3m radius. "
				+ "Additionally, x0.8 Heat per Regular Shot and x0.8 Heat per Charged Shot", modIcons.special, 5, 1);
		tier5[2] = new Mod("Plasma Burn", "Regular Shots also do [5 plus 25% of their Direct Damage] Heat per shot which can ignite enemies, dealing " + MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) + " Fire Damage per Second.", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Energy Rerouting", "+16 Battery Size, x1.5 Charge Speed.", overclockIcons.chargeSpeed, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Magnetic Cooling Unit", "+25% Cooling Rate, x0.7 Heat per Second while Charged.", overclockIcons.coolingRate, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Heat Pipe", "-2 Ammo per Charged Shot, x1.3 Charge Speed, x2 Heat per Charged shot, x2 Heat per Second while Charged", overclockIcons.fuel, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Heavy Hitter", "x1.6 Regular Shot Direct Damage, x1.5 Heat per Regular Shot, -32 Battery Size", overclockIcons.directDamage, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Overcharger", "x1.5 Charged Shot Direct Damage, x1.5 Charged Shot Area Damage, x1.2 Charged Shot AoE Radius, +2 Ammo per Charged Shot, -25% Cooling Rate", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Persistent Plasma", "Upon impact, Charged Shots leave behind a 3m radius field of Persistent Plasma that deals " + MathUtils.round(DoTInformation.Plasma_EPC_DPS, GuiConstants.numDecimalPlaces) + 
				" Fire Damage per Second and slows enemies by 20% for 7.6 seconds. -15 Charged Shot Direct Damage, -15 Charged Shot Area Damage", overclockIcons.hourglass, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
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
			toReturn += 5;
		}
		
		if (selectedOverclock == 3) {
			toReturn = (int) Math.round(toReturn * 1.6);
		}
		
		return toReturn;
	}
	protected double getChargedDirectDamage() {
		double toReturn = chargedDirectDamage;
		
		if (selectedTier1 == 2) {
			toReturn += 15;
		}
		if (selectedTier4 == 2) {
			toReturn += 15;
		}
		
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			return 0;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 15;
		}
		
		// Multiplicative bonuses last
		if (selectedTier5 == 0) {
			toReturn *= 1.2;
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
		if (selectedTier4 == 2) {
			toReturn += 15;
		}
		
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			return 240;
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
		
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			return 3.0;
		}
		
		if (selectedTier4 == 0) {
			toReturn += 1.0;
		}
		
		if (selectedTier5 == 0) {
			toReturn *= 0.4;
		}
		if (selectedOverclock == 4) {
			toReturn *= 1.2;
		}
		
		return toReturn;
	}
	protected int getBatterySize() {
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
			toReturn += 2;
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
			toReturn /= 2.5;
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
		double toReturn = heatPerChargedShot;
		
		if (selectedTier5 == 1) {
			toReturn *= 0.8;
		}
		
		if (selectedOverclock == 2) {
			toReturn *= 2.0;
		}
		
		return toReturn;
	}
	protected double getHeatPerSecondWhileCharged() {
		double toReturn = heatPerSecondWhileCharged;
		
		if (selectedTier2 == 0) {
			toReturn *= 0.4;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= 0.7;
		}
		else if (selectedOverclock == 2) {
			toReturn *= 2.0;
		}
		
		return toReturn;
	}
	protected double getRegularShotVelocity() {
		// From what Omega Sentinel said in the Discord, Regular Shots move at 35 m/sec by default, and Charged Shots move at 13 m/sec.
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	
	protected double getSecondsBeforeOverheatWhileCharged() {
		return maxHeat / getHeatPerSecondWhileCharged();
	}
	protected double getChargedShotCooldownDuration() {
		return getHeatPerChargedShot() / (coolingRate * getCoolingRateModifier());
	}
	protected double getOverheatCooldownDuration() {
		return maxHeat / (coolingRate * getCoolingRateModifier());
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return -1.0;
	}
}
