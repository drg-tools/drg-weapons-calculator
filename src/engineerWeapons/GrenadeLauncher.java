package engineerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

// TODO: While Incendiary Compound has been modeled, I think it deserves to be refactored. It feels sloppy how it turned out.

public class GrenadeLauncher extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int areaDamage;
	private double aoeRadius;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double fearChance;
	private double armorBreakChance;
	private double stunChance;
	private int stunDuration;
	private double projectileVelocity;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public GrenadeLauncher() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public GrenadeLauncher(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public GrenadeLauncher(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Deepcore 40MM PGL";
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 0;
		areaDamage = 110;
		aoeRadius = 2.5;
		carriedAmmo = 8;
		magazineSize = 1;
		rateOfFire = 2.0;
		reloadTime = 2.0;
		fearChance = 1.0;
		armorBreakChance = 0.5;
		stunChance = 0.0;
		stunDuration = 0;
		projectileVelocity = 1.0;
		
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
		tier1[0] = new Mod("Fragmentary Shell", "Damage radius increase", 1, 0);
		tier1[1] = new Mod("Expanded Ammo Bags", "Expanded Ammo Bags", 1, 1);
		tier1[2] = new Mod("HE Compound", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Extra Ammo", "Expanded Ammo Bags", 2, 0);
		tier2[1] = new Mod("Larger Payload", "More bang for the buck! Increases the damage done within the Area of Effect!", 2, 1);
		tier2[2] = new Mod("High Velocity Grenades", "We souped up the ejection mechanisms of your gun, so the projectiles are now fired at a much higher velocity.", 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Incendiary Compound", "50% damage converted to heat damage", 3, 0);
		tier3[1] = new Mod("Pressure Wave", "We're proud of this one. Armor shredding. Tear through that high-impact plating of those bug buggers like butter. What could be finer?", 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Homebrew Explosive", "More damage on average but it's a bit inconsistent with a spread of 80% to 140%", 4, 0);
		tier4[1] = new Mod("Nails + Tape", "Fire in the hole! The Area of Effect is increased. (We advise keeping the term \"safe distance\" close to your heart)", 4, 1);
		tier4[2] = new Mod("Concussive Blast", "Stuns creatures within the blast radius", 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Proximity Trigger", "Grenades will explode when they are close to an enemy. Damage goes up the longer the projectile flies. Up to +100%", 5, 0, false);
		tier5[1] = new Mod("Spiky Grenade", "Deals damage on direct impact", 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Clean Sweep", "Increases the explosion radius and damage without any unwanted effects.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Pack Rat", "You found a way to pack away two more rounds somewhere", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Compact Rounds", "Smaller and lighter rounds means more rounds in the pocket at the cost of the explosion's effective radius and damage", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "RJ250 Compound", "Trade raw damage for the ability to use explosions to move yourself and your teammates. (~33% self-damage)", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Fat Boy", "Big and deadly and dirty. Too bad plutonium is so heavy that you can only take a few rounds with you. And remember to take care with the fallout.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Hyper Propellant", "New super-high velocity projectiles trade explosive range for raw damage in a tight area. The larger rounds also limit the total amount you can carry.", 5);
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
				System.out.println("Grenade Launcher's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Grenade Launcher's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
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
				case 'C': {
					selectedTier3 = 2;
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
	public GrenadeLauncher clone() {
		return new GrenadeLauncher(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		if (selectedTier5 == 1) {
			toReturn += 60;
		}
		if (selectedOverclock == 5) {
			toReturn += 250;
		}
		if (selectedTier3 == 0) {
			toReturn /= 2;
		}
		return toReturn;
	}
	private int getAreaDamage() {
		double toReturn = areaDamage;
		if (selectedTier1 == 2) {
			toReturn += 15;
		}
		if (selectedTier2 == 1) {
			toReturn += 20;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 10;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 10;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 25;
		}

		if (selectedTier4 == 0) {
			toReturn = toReturn * 1.1;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 4;
		}
		
		if (selectedTier3 == 0) {
			toReturn /= 2.0;
		}
		
		return (int) Math.round(toReturn);
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier1 == 0) {
			toReturn += 1.0;
		}
		if (selectedTier4 == 1) {
			toReturn += 1.5;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 0.5;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 0.5;
		}
		else if (selectedOverclock == 4) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.3;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier1 == 1) {
			toReturn += 2;
		}
		if (selectedTier2 == 0) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 2;
		}
		else if (selectedOverclock == 2) {
			toReturn += 4;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0.3;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.6;
		}
		
		return (int) Math.round(toReturn);
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		if (selectedTier3 == 1) {
			toReturn += 5.0;
		}
		return toReturn;
	}
	private double getStunChance() {
		double toReturn = stunChance;
		
		if (selectedTier4 == 2) {
			toReturn += 1.0;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		
		if (selectedTier4 == 2) {
			toReturn += 3;
		}

		return toReturn;
	}
	private double getProjectileVelocity() {
		double toReturn = projectileVelocity;
		
		if (selectedTier2 == 2) {
			toReturn += 1.8;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 0.7;
		}
		else if (selectedOverclock == 5) {
			toReturn += 3.5;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		boolean directDamageModified = selectedTier5 == 1 || selectedTier3 == 0 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified, selectedTier5 == 1 || selectedOverclock == 5);
		
		boolean areaDamageModified = selectedTier1 == 2 || selectedTier2 == 1 || selectedTier3 == 0 || selectedTier4 == 0 || selectedOverclock == 0 || (selectedOverclock > 1 && selectedOverclock < 5);
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier1 == 0 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), aoeRadiusModified);
		
		toReturn[3] = new StatsRow("Magazine Size:", magazineSize, false);
		
		boolean carriedAmmoModified = selectedTier1 == 1 || selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Carried Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", rateOfFire, false);
		toReturn[6] = new StatsRow("Reload Time:", reloadTime, false);
		
		boolean velocityModified = selectedTier2 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[7] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getProjectileVelocity()), velocityModified);
		
		toReturn[8] = new StatsRow("Fear Chance:", convertDoubleToPercentage(fearChance), false);
		
		toReturn[9] = new StatsRow("Armor Break Chance:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier3 == 1);
		
		boolean stunEquipped = selectedTier4 == 2;
		toReturn[10] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), stunEquipped, stunEquipped);
		toReturn[11] = new StatsRow("Stun Duration:", getStunDuration(), stunEquipped, stunEquipped);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return true;
	}
	
	private double calculateSingleTargetDPS(boolean burst, boolean weakpoint) {
		double directDamage;
		if (weakpoint) {
			directDamage = increaseBulletDamageForWeakpoints(getDirectDamage());
		}
		else {
			directDamage = getDirectDamage();
		}
		
		double damagePerProjectile = directDamage + getAreaDamage();
		double baseDPS = damagePerProjectile / reloadTime;
		
		double burnDPS = 0.0;
		// Incendiary Compound
		if (selectedTier3 == 0) {
			if (burst) {
				double heatPerGrenade = getDirectDamage() + getAreaDamage();
				double RoF = 1.0 / reloadTime;
				double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatPerGrenade, RoF);
				double burnDoTUptime = (reloadTime - timeToIgnite) / reloadTime;
				
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		double radDPS = 0.0;
		// Fat Boy OC
		if (selectedOverclock == 4) {
			// double FBduration = 15;
			// double FBradius = 8;
			radDPS = DoTInformation.Rad_FB_DPS;
		}
		
		return baseDPS + burnDPS + radDPS;
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
		// Because the Grenade Launcher has to be aimed manually, its Accuracy isn't applicable.
		return calculateSingleTargetDPS(false, true);
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		// TODO: reduce this by its AoE efficiency percentage
		double totalDPS = getAreaDamage() / reloadTime;
		if (selectedTier3 == 0) {
			totalDPS += DoTInformation.Burn_DPS;
		}
		if (selectedOverclock == 4) {
			totalDPS += DoTInformation.Rad_FB_DPS;
		}
		return totalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numTargets = calculateMaxNumTargets();
		double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
		
		// Now that I have Incendiary Compound modeled "correctly", I'm very dissatisfied with it. TODO: look over this math again later, see if something is messed up.
		double burnDoTTotalDamage = 0;
		if (selectedTier3 == 0) {
			double heatPerGrenade = getDirectDamage() + getAreaDamage();
			double RoF = 1 / reloadTime;
			double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatPerGrenade, RoF);
			
			double burnDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, EnemyInformation.averageBurnDuration(), DoTInformation.Burn_DPS);
			burnDoTTotalDamage = burnDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		double radiationDoTTotalDamage = 0;
		if (selectedOverclock == 4) {
			double FBdmgPerTick = 25;
			double FBticksPerSec = 1/0.9;
			double fatBoyDPS = FBdmgPerTick * FBticksPerSec;
			// I'm guessing that it takes about 4 seconds for enemies to move out of the 8m radius field
			double radiationDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, 4, fatBoyDPS);
			radiationDoTTotalDamage = radiationDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		int numShots = 1 + getCarriedAmmo();
		return numShots * (getDirectDamage() + (getAreaDamage()) * calculateMaxNumTargets()) + burnDoTTotalDamage + radiationDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		
		double radius = getAoERadius();
		double[] foo = calculateAverageAreaDamage(radius, radius/2.0, 0.75, 0.33);
		//System.out.println(foo[0] + " " + foo[1] + " " + foo[2]);
		
		return calculateNumGlyphidsInRadius(getAoERadius());
	}

	@Override
	public double calculateFiringDuration() {
		// This is equivalent to counting how many times it has to reload, which is one less than the carried ammo + 1 in the chamber
		return getCarriedAmmo() * reloadTime;
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage()) + getAreaDamage();
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Manually aimed; return -1
		return -1.0;
	}

	@Override
	public double utilityScore() {
		// OC "RJ250 Compound" gives a ton of Mobility (8m vertical, 12m horizontal)
		if (selectedOverclock == 3) {
			// For now, until I think of a better system, I'll just add the max vertical jump and max horizontal jump distances at 1/2 weight each.
			// Ideally I would like to calculate the m/sec velocity of launch, but that could take a while to test and calculate.
			utilityScores[0] = (0.5 * 8 + 0.5 * 12) * UtilityInformation.BlastJump_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Armor Breaking
		utilityScores[2] = (getArmorBreakChance() - 1) * calculateMaxNumTargets() * UtilityInformation.ArmorBreak_Utility;
		
		// Because the Stun from Concussive Blast keeps them immobolized while they're trying to run in Fear, I'm choosing to make the Stun/Fear Utility scores NOT additive.
		if (selectedTier4 == 2) {
			// Concussive Blast = 100% stun, 2 sec duration
			utilityScores[4] = 0;
			utilityScores[5] = getStunChance() * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			// Built-in Fear is 100%, but it doesn't seem to work 100% of the time... 
			utilityScores[4] = fearChance * calculateMaxNumTargets() * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
}
