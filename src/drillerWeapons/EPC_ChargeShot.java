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

public class EPC_ChargeShot extends Weapon {
	
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
		The area last 6 seconds and deals 5 damage every 0.25 seconds. 
	*/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public EPC_ChargeShot() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public EPC_ChargeShot(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public EPC_ChargeShot(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "EPC (Charged Shots)";
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
			
			// Re-set AoE Efficiency
			setAoEEfficiency();
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public EPC_ChargeShot clone() {
		return new EPC_ChargeShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "EPC_ChargeShot";
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
		
		// Early exit condition: if Flying Nightmare is equipped, then the projectile no longer explodes upon impact, which effectively sets the Area Damage to 0.
		if (selectedTier5 == 0) {
			return 0;
		}
		
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
		
		if (selectedTier5 == 1) {
			/*
				"Thin Containment Field
				Damage done is 240 and is not affected by mods or overclocks."
				
				Also, it will cost +1 regular shot to detonate the TCF projectile to get the 240 damage
			*/
			toReturn += 240;
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
	private double getRateOfFire() {
		double timeToFireChargedShot = getChargedShotWindup();
		double timeToCoolDownAfterChargedShot = getCooldownDuration();
		
		return 1 / (timeToFireChargedShot + timeToCoolDownAfterChargedShot);
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
		
		if (selectedTier5 == 1) {
			// Thin Containment Field costs 1 additional ammo to fire a regular shot to detonate the TFC projectile for the +240 AoE damage
			toReturn += 1;
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
			// If TFC is equipped, then the Charged Shot only fills up 80% of the meter, and one Regular Shot to detonate the TFC field
			// Don't let this return more than the max heat, though!
			return Math.min(maxHeat * 0.8 + getHeatPerRegularShot(), maxHeat);
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
		// If they have Thin Containment Field equipped, then each Charged Shot only fills the meter to 80% plus the one Regular Shot
		if (selectedTier5 == 1) {
			return getHeatPerChargedShot() / (coolingRate * getCoolingRateModifier());
		}
		else {
			return maxHeat / (coolingRate * getCoolingRateModifier());
		}
	}

	@Override
	public StatsRow[] getStats() {
		boolean coolingRateModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 4;
		
		StatsRow[] toReturn = new StatsRow[11];
		
		boolean chargedDirectDamageModified = selectedTier1 == 2 || selectedTier2 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getChargedDirectDamage(), chargedDirectDamageModified);
		
		boolean chargedAreaDamageModified = selectedTier1 == 2 || selectedTier2 == 2 || selectedTier5 == 0 || selectedTier5 == 1 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getChargedAreaDamage(), chargedAreaDamageModified);
		
		boolean radiusModified = selectedTier2 == 0 || selectedTier5 == 0 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], radiusModified);
		
		boolean windupModified = selectedTier3 == 1 || selectedTier5 == 0 || selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[3] = new StatsRow("Charged Shot Windup:", getChargedShotWindup(), windupModified);
		
		toReturn[4] = new StatsRow("Heat/Sec While Charged:", getHeatPerSecondWhileCharged(), selectedTier4 == 0 || selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Seconds Charged Shot can be Held Before Overheating:", getSecondsBeforeOverheatWhileCharged(), selectedTier4 == 0 || selectedOverclock == 1);
		
		boolean ammoPerShotModified = selectedTier3 == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedTier5 == 1;
		toReturn[6] = new StatsRow("Ammo/Charged Shot:", getAmmoPerChargedShot(), ammoPerShotModified);
		
		boolean batterySizeModified = selectedTier1 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[7] = new StatsRow("Battery Size:", getBatterySize(), batterySizeModified);
		
		// This is equivalent to "Did either the time to charge a shot or the time to cool down after a shot change?"
		//boolean RoFModified = selectedTier3 == 1 || selectedTier3 == 2 || selectedTier5 == 0 || (selectedOverclock > -1 && selectedOverclock < 3) || selectedOverclock == 4;
		boolean RoFModified = windupModified || selectedTier5 == 1 || coolingRateModified;
		toReturn[8] = new StatsRow("Rate of Fire:", getRateOfFire(), RoFModified);
		
		toReturn[9] = new StatsRow("Cooling Rate:", convertDoubleToPercentage(getCoolingRateModifier()), coolingRateModified);
		
		toReturn[10] = new StatsRow("Cooldown After Overheating:", getCooldownDuration(), coolingRateModified || selectedTier5 == 1);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Because this only models the charged shots of the EPC, it will always do splash damage.
		return true;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// According to Elythnwaen, EPC has a 1.25m full damage radius, and 33% damage falloff at full radius
		aoeEfficiency = calculateAverageAreaDamage(getChargedAoERadius(), 1.25, 0.33);
	}

	// Single-target calculations
	private double calculateSingleTargetDPS() {
		/*
			Much like the Grenade Launcher, the DPS of this gun is modeled by the damage done per projectile divided by the time to fire another projectile.
			Because this is modeling the most efficient DPS, it will model releasing the charged shot as soon as it's available, instead of holding it until it automatically overheats.
			This means that the mods and OCs that affect heat gain while charged won't affect DPS.
			
			Additionally, the burst dps == sustained dps == sustained weakpoint dps == sustained weakpoint + accuracy dps because the charged shots' direct damage don't deal weakpoint damage, 
			the accuracy is ignored because it's manually aimed, and the magSize is effectively 1 due to the overheat mechanic.
		*/
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			return getChargedDirectDamage() * getRateOfFire();
		}
		
		double baseDPS = (getChargedDirectDamage() + getChargedAreaDamage()) * getRateOfFire();
		
		if (selectedOverclock == 5) {
			return baseDPS + DoTInformation.Plasma_DPS;
		}
		else {
			return baseDPS;
		}
	}
	
	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS();
	}

	@Override
	public double calculateIdealSustainedDPS() {
		// Because it can only fire one charged shot before having to cool down, its sustained DPS = burst DPS
		return calculateSingleTargetDPS();
	}

	@Override
	public double sustainedWeakpointDPS() {
		// EPC can't get weakpoint bonus damage, and sustained = burst in this mode.
		return calculateSingleTargetDPS();
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// Because the Charged Shots have to be aimed manually, Accuracy isn't applicable.
		return calculateSingleTargetDPS();
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			return getChargedDirectDamage() * getRateOfFire();
		}
		
		if (selectedOverclock == 5) {
			return getChargedAreaDamage() * aoeEfficiency[1] * getRateOfFire() + DoTInformation.Plasma_DPS;
		}
		else {
			return getChargedAreaDamage() * aoeEfficiency[1] * getRateOfFire();
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numberOfChargedShots = (int) Math.ceil(getBatterySize() / getAmmoPerChargedShot());
		
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			double directDamage = getChargedDirectDamage();
			double numTargetsHitPerShot = calculateMaxNumTargets();
			return numberOfChargedShots * directDamage * numTargetsHitPerShot;
		}
		
		double baseDamage = numberOfChargedShots * (getChargedDirectDamage() + getChargedAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2]);
		if (selectedOverclock == 5) {
			/*
				Since Persistent Plasma is a DoT that last 6 seconds, but doesn't guarantee to hit every target for that full 6 seconds, 
				I'm choosing to have its total damage be equal to how the DoT DPS times firing duration times the max num targets divided by 3.
				The divide by 3 is to simulate the fact that the enemies are not stationary within the DoT field, and will move out of it before 
				the duration expires.
			*/
			double persistentPlasmaDamage = DoTInformation.Plasma_DPS * calculateFiringDuration() * aoeEfficiency[2] / 3.0;
			return baseDamage + persistentPlasmaDamage;
		}
		else {
			return baseDamage;
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			double numTargetsHitSimultaneously = aoeEfficiency[2];
			// This is an arbitrary number to multiply how many targets the Flying Nightmare projectile will hit along its path. In all liklihood this is probably incorrect, but Flying Nightmare is horrendous to try to model.
			double distanceTraveledTargetMultiplier = 3;
			return (int) Math.round(numTargetsHitSimultaneously * distanceTraveledTargetMultiplier);
		}
		else {
			return (int) aoeEfficiency[2];
		}
	}

	@Override
	public double calculateFiringDuration() {
		double firingInterval = getChargedShotWindup() + getCooldownDuration();
		int numChargedShots = (int) Math.ceil(getBatterySize() / getAmmoPerChargedShot());
		return numChargedShots * firingInterval;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = getChargedDirectDamage() + getChargedAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			0,  // Kinetic
			0,  // Explosive
			0.5 * getChargedDirectDamage(),  // Fire
			0,  // Frost
			0.5 * getChargedDirectDamage()  // Electric
		};
		
		double[] areaDamage = {
			0.5 * getChargedAreaDamage(),  // Explosive
			0,  // Fire
			0,  // Frost
			0.5 * getChargedAreaDamage(),  // Electric
		};
		
		double persistentPlasmaDamage = 0;
		if (selectedOverclock == 5) {
			persistentPlasmaDamage = calculateAverageDoTDamagePerEnemy(0, 7.6, DoTInformation.Plasma_DPS);
		}
		double[] DoTDamage = {
			0,  // Fire
			persistentPlasmaDamage,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, -1.0, 0.0, 0.0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		// EPC charged shot's AoE damage is 50% Explosive, so it does have a chance to break Light Armor plates
		// Additionally, to average out this probability to break all Light Armor plates inside the AoE, multiply it by its AoE Efficiency coefficient, too.
		utilityScores[2] = calculateProbabilityToBreakLightArmor(aoeEfficiency[1] * 0.5 * getChargedAreaDamage()) * UtilityInformation.ArmorBreak_Utility;
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		// Instead of damage per mag, this will be damage per Charged Shot
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			double directDamage = getChargedDirectDamage();
			double numTargetsHitPerShot = calculateMaxNumTargets();
			return directDamage * numTargetsHitPerShot;
		}
		else {
			return getChargedDirectDamage() + getChargedAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		}
	}
	
	@Override
	public double timeToFireMagazine() {
		return getChargedShotWindup();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.add(String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[0].getText(true), ""));
		toReturn.add(String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[1].getText(true), ""));
		toReturn.add(String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[2].getText(true), ""));
		
		// Tier 2
		toReturn.add(String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[0].getText(true), ""));
		toReturn.add(String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[1].getText(true), ""));
		toReturn.add(String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 18, 0, 12, 0, 0, tier2[2].getText(true), ""));
		
		// Tier 3
		toReturn.add(String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 30, 0, 0, 0, 20, 0, tier3[0].getText(true), ""));
		toReturn.add(String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 30, 0, 20, tier3[1].getText(true), ""));
		toReturn.add(String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2200, 0, 0, 30, 0, 20, 0, tier3[2].getText(true), ""));
		
		// Tier 4
		toReturn.add(String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 0, 15, 0, 36, 25, tier4[0].getText(true), ""));
		toReturn.add(String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 15, 0, 0, 0, 0, tier4[1].getText(true), ""));
		
		// Tier 5
		toReturn.add(String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[0].getText(true), ""));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[1].getText(true), ""));
		toReturn.add(String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 4400, 40, 0, 0, 0, 110, 60, tier5[2].getText(true), ""));
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.add(String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7300, 0, 130, 65, 0, 0, 100, overclocks[0].getText(true), ""));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8900, 0, 0, 125, 95, 0, 80, overclocks[1].getText(true), ""));
		
		// Balanced
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7450, 0, 60, 125, 0, 0, 95, overclocks[2].getText(true), ""));
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8100, 60, 140, 105, 0, 0, 0, overclocks[3].getText(true), ""));
		
		// Unstable
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7050, 0, 120, 0, 95, 60, 0, overclocks[4].getText(true), ""));
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8150, 95, 0, 0, 75, 0, 130, overclocks[5].getText(true), ""));
		
		return toReturn;
	}
}
