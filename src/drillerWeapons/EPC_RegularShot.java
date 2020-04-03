package drillerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;

// TODO: the wiki page for this weapon got updated. Change numbers to match what they have.
public class EPC_RegularShot extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int chargedDirectDamage;
	private int chargedAreaDamage;
	private double chargedAoERadius;
	private int batterySize;
	private double rateOfFire;
	private double maxHeat;
	private double regularCoolingRate;
	private double overheatedCoolingRate;
	private int ammoPerChargedShot;
	private double chargeShotWindup;
	private double heatPerRegularShot;
	private double heatPerSecondWhileCharged;
	private double regularShotVelocity;
	
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
		Damage done is equal to the Charged Shot direct damage and is affected by mods, but is NOT affected by overclocks.
		
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
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 20;
		chargedDirectDamage = 60;
		chargedAreaDamage = 60;
		chargedAoERadius = 2.0;
		batterySize = 120;
		rateOfFire = 7.0;
		maxHeat = 8.4;
		regularCoolingRate = 2.8;  // Calculated with some fancy algebra
		overheatedCoolingRate = 3.5;  // Want this to work out to 2.4 sec overheat cooldown by default
		ammoPerChargedShot = 8;
		chargeShotWindup = 1.5;  // seconds
		heatPerRegularShot = 1.0;
		heatPerSecondWhileCharged = maxHeat * 2.0;  // Want this to work out to 0.5 sec of heat buildup before overheating by default
		regularShotVelocity = 1.0;
		
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
		tier1[0] = new Mod("Increased Particle Density", "Increased damage caused by normal shots", 1, 0);
		tier1[1] = new Mod("Larger Battery", "The good thing about clips, magazines, ammo drums, fuel tanks ... you can always get bigger variants.", 1, 1);
		tier1[2] = new Mod("Higher Charged Plasma Energy", "Increases the direct damage for the charged projectile.", 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Plasma Splash", "Greater damage radius for the charged projectile explosion.", 2, 0);
		tier2[1] = new Mod("Overcharged Plasma Accelerator", "Increases the movement speed of EPC's normal projectiles.", 2, 1);
		tier2[2] = new Mod("Reactive Shockwave", "More bang for the buck! Increases the damage done within the Area of Effect!", 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Improved Charge Efficiency", "Each charged shot uses less energy.", 3, 0);
		tier3[1] = new Mod("Crystal Capacitors", "Prepare a charged shot much faster.", 3, 1);
		tier3[2] = new Mod("Tweaked Radiator", "Increases the rate at which the weapon sheds heat, letting you shoot more rounds before overheating and also recovering faster from an overheat.", 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Heat Shield", "Reduces how fast the weapon overheats when holding a charged shot.", 4, 0);
		tier4[1] = new Mod("High Density Battery", "The good thing about clips, magazines, ammo drums, fuel tanks ...you can always get bigger variants.", 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Flying Nightmare", "The charged projectile deals damage to nearby enemies while it flies but takes longer to charge up.", 5, 0);
		tier5[1] = new Mod("Thin Containment Field", "A weaker containment field takes less energy to create thus producing less heat for Charged Shots. Be aware that any high-energy impact will destabilize the Charged Projectile causing a large area implosion.", 5, 1);
		tier5[2] = new Mod("Bouncy Plasma", "Regular shots now ricochet, please try not to hit yourself or your teammates while pulling off trick shots.", 5, 2, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Energy Rerouting", "A masterwork of engineering that improves charge speed and energy efficiency without affecting overall performance!", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Magnetic Cooling Unit", "A high-tech solution to cleanly improve the cooling rate increasing the number of slots that can be fired before overheating and also the speed of recovery from an overheat as well as how long a charge can be held.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Heat Pipe", "By channeling exhaust heat back into the charge chamber a shot can be charged using less energy. This does however make the weapon less efficient at dissipating heat.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Heavy Hitter", "Some extensive tweaking to how the shots are prepared can increase the pure damage of the weapon but at the cost of a lower projectile velocity and a reduced battery size.", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Overcharger", "Pushing the EPC to the limit will give you a significant increase in charge shot damage but at the heavy cost of slow charge speed and decreased cooling efficiency", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Persistent Plasma", "By changing how the plasma is layered within the charged projectile a slow and persistent discharge can be achieved upon impact. However this does reduce the instance damage done.", 5);
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
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 5;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 10;
		}
		
		return toReturn;
	}
	private int getChargedDirectDamage() {
		int toReturn = chargedDirectDamage;
		
		if (selectedTier1 == 2) {
			toReturn += 30;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 40;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 20;
		}
		
		return toReturn;
	}
	private int getChargedAreaDamage() {
		int toReturn = chargedAreaDamage;
		
		if (selectedTier2 == 2) {
			toReturn += 20;
		}
		
		if (selectedOverclock == 5) {
			toReturn -= 20;
		}
		
		return toReturn;
	}
	private double getChargedAoERadius() {
		double toReturn = chargedAoERadius;
		
		if (selectedTier2 == 0) {
			toReturn += 1.5;
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
			toReturn -= 16;
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
		else if (selectedOverclock == 2) {
			modifier -= 0.5;
		}
		else if (selectedOverclock == 4) {
			modifier -= 0.5;
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
		
		return toReturn;
	}
	private double getChargedShotWindup() {
		double toReturn = chargeShotWindup;
		
		if (selectedTier3 == 1) {
			toReturn /= 3.0;
		}
		if (selectedTier5 == 0) {
			toReturn /= 0.7;
		}
		
		if (selectedOverclock == 0) {
			toReturn /= 1.5;
		}
		else if (selectedOverclock == 4) {
			toReturn /= 0.5;
		}
		
		return toReturn;
	}
	private double getHeatPerRegularShot() {
		double toReturn = heatPerRegularShot;
		
		if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getHeatPerChargedShot() {
		// Unless they have Mod Tier 5 "Thin Containment Field" equipped, charged shots guarantee an overheat.
		if (selectedTier5 == 1) {
			return maxHeat * 0.7;  // This 70% coefficient is a guess; hard to measure.
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
		double toReturn = regularShotVelocity;
		
		if (selectedTier2 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	
	private int getNumRegularShotsBeforeOverheat() {
		double k = getCoolingRateModifier();
		double h = getHeatPerRegularShot();
		
		double exactAnswer = (maxHeat * rateOfFire) / (h * (rateOfFire - k * regularCoolingRate));
		
		return (int) Math.ceil(exactAnswer);
	}
	private double getSecondsBeforeOverheatWhileCharged() {
		return maxHeat / getHeatPerSecondWhileCharged();
	}
	private double getCooldownDuration() {
		return maxHeat / (overheatedCoolingRate * getCoolingRateModifier());
	}

	@Override
	public StatsRow[] getStats() {
		boolean coolingRateModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 4;
		
		StatsRow[] toReturn = new StatsRow[15];
		
		toReturn[0] = new StatsRow("Regular Shot Direct Damage:", getDirectDamage(), selectedTier1 == 0 || selectedOverclock == 3);
		
		toReturn[1] = new StatsRow("Regular Shot Velocity:", convertDoubleToPercentage(getRegularShotVelocity()), selectedTier2 == 1);
		
		toReturn[2] = new StatsRow("Heat/Regular Shot:", getHeatPerRegularShot(), selectedOverclock == 3);
		
		toReturn[3] = new StatsRow("Regular Shots Fired Before Overheating:", getNumRegularShotsBeforeOverheat(), coolingRateModified || selectedOverclock == 3);
		
		boolean chargedDirectDamageModified = selectedTier1 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Charged Shot Direct Damage:", getChargedDirectDamage(), chargedDirectDamageModified);
		
		toReturn[5] = new StatsRow("Charged Shot Area Damage:", getChargedAreaDamage(), selectedTier2 == 2 || selectedOverclock == 5);
		
		toReturn[6] = new StatsRow("Charged Shot AoE Radius:", getChargedAoERadius(), selectedTier2 == 0);
		
		boolean windupModified = selectedTier3 == 1 || selectedTier5 == 0 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[7] = new StatsRow("Charged Shot Windup:", getChargedShotWindup(), windupModified);
		
		toReturn[8] = new StatsRow("Heat/Sec While Charged:", getHeatPerSecondWhileCharged(), selectedTier4 == 0 || selectedOverclock == 1);
		
		toReturn[9] = new StatsRow("Seconds Charged Shot Can Be Held Before Overheating:", getSecondsBeforeOverheatWhileCharged(), selectedTier4 == 0 || selectedOverclock == 1);
		
		toReturn[10] = new StatsRow("Ammo/Charged Shot:", getAmmoPerChargedShot(), selectedTier3 == 0 || selectedOverclock == 2);
		
		boolean batterySizeModified = selectedTier1 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[11] = new StatsRow("Battery Size:", getBatterySize(), batterySizeModified);
		
		toReturn[12] = new StatsRow("Rate of Fire:", rateOfFire, false);
		
		toReturn[13] = new StatsRow("Cooling Rate:", convertDoubleToPercentage(getCoolingRateModifier()), coolingRateModified);
		
		toReturn[14] = new StatsRow("Cooldown After Overheating:", getCooldownDuration(), coolingRateModified);
		
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
		if (weakpoint) {
			// Because this weapon doesn't have its Accuracy handled like the other weapons, I'm choosing to just increase the damage by a weighted average.
			damagePerProjectile = increaseBulletDamageForWeakpoints(getDirectDamage());
		}
		else {
			damagePerProjectile = getDirectDamage();
		}
		
		int burstSize = getNumRegularShotsBeforeOverheat();
		
		double duration;
		if (burst) {
			duration = burstSize / rateOfFire;
		}
		else {
			duration = burstSize / rateOfFire + getCooldownDuration();
		}
		
		return damagePerProjectile * burstSize / duration;
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
		// Regular shots can only hit one enemy before disappearing. I'm choosing not to model Bouncy Plasma.
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return getDirectDamage() * getBatterySize();
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
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / calculateIdealSustainedDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = getDirectDamage();
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return -1.0;
	}

	@Override
	public double utilityScore() {
		// EPC doesn't have any utility
		return 0;
	}
}
