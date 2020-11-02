package engineerWeapons;

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
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public class GrenadeLauncher extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double areaDamage;
	private double aoeRadius;
	private int carriedAmmo;
	private int magazineSize;
	private double reloadTime;
	private double fearFactor;
	
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
		weaponPic = WeaponPictures.grenadeLauncher;
		
		// Base stats, before mods or overclocks alter them:
		areaDamage = 110;
		aoeRadius = 3;
		carriedAmmo = 8;
		magazineSize = 1;
		reloadTime = 2.0;
		fearFactor = 1.0;
		
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
		tier1[0] = new Mod("Fragmentary Shell", "+1m AoE Radius", modIcons.aoeRadius, 1, 0);
		tier1[1] = new Mod("Expanded Ammo Bags", "+2 Max Ammo", modIcons.carriedAmmo, 1, 1);
		tier1[2] = new Mod("HE Compound", "+15 Area Damage", modIcons.areaDamage, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Extra Ammo", "+3 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Larger Payload", "+20 Area Damage", modIcons.areaDamage, 2, 1);
		tier2[2] = new Mod("High Velocity Grenades", "+180% Projectile Velocity", modIcons.projectileVelocity, 2, 2, false);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Incendiary Compound", "Lose 50% of Direct, Area, and Armor Damage, and convert it to Heat Damage that will ignite enemies, dealing " + MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) + " Fire Damage per Second", modIcons.heatDamage, 3, 0);
		tier3[1] = new Mod("Pressure Wave", "+500% Armor Breaking", modIcons.armorBreaking, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Homebrew Explosive", "Anywhere from x0.8 - x1.4 damage per shot, averaged to x" + homebrewPowderCoefficient, modIcons.homebrewPowder, 4, 0);
		tier4[1] = new Mod("Nails + Tape", "+1m AoE Radius", modIcons.aoeRadius, 4, 1);
		tier4[2] = new Mod("Concussive Blast", "Stuns creatures within the blast radius for 3 seconds", modIcons.stun, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Proximity Trigger", "Launched grenades will only detonate when they are in close proximity to an enemy or after the projectile comes to a complete stop. "
				+ "Note: the trigger takes a moment to arm, indicated by a green light, and until then the grenade functions as usual.", modIcons.special, 5, 0, false);
		tier5[1] = new Mod("Spiky Grenade", "+60 Direct Damage to any target directly impacted by a grenade.", modIcons.directDamage, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Clean Sweep", "+10 Area Damage, +0.5m AoE Radius", overclockIcons.aoeRadius, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Pack Rat", "+2 Max Ammo", overclockIcons.carriedAmmo, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Compact Rounds", "+4 Max Ammo, -10 Area Damage, -0.5m AoE Radius", overclockIcons.carriedAmmo, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "RJ250 Compound", "Jump and shoot the ground beneath you to Grenade Jump. Can also be used on allies who are jumping. In exchange, -25 Area Damage.", overclockIcons.grenadeJump, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Fat Boy", "x4 Area Damage, +1m AoE Radius, x0.3 Max Ammo, x0.7 Projectile Velocity. Also leaves behind an 8m radius field that does "
				+ "an average of " + MathUtils.round(DoTInformation.Rad_FB_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per Second for 15 seconds.", overclockIcons.areaDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Hyper Propellant", "+385 Direct Damage, +350% Projectile Velocity, changes element from Explosive to Disintegrate, x0.3 AoE Radius, -2 Max Ammo", overclockIcons.projectileVelocity, 5);
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
				case 'C': {
					setSelectedModAtTier(4, 2, false);
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
			
			// Re-set AoE Efficiency
			setAoEEfficiency();
			
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
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "GrenadeLauncher";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.engineerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.grenadeLauncherGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = 0;
		if (selectedTier5 == 1) {
			toReturn += 60;
		}
		
		// There's currently a bug in U32 where Incendiary Compound is only affecting Spiky Grenade's damage. I'm going to model it like it's currently bugged, but this will have to be changed if the bug gets fixed.
		if (selectedTier3 == 0) {
			toReturn /= 2.0;
		}
		
		if (selectedOverclock == 5) {
			toReturn += 385;
		}
		
		return toReturn;
	}
	private double getAreaDamage() {
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
			toReturn *= homebrewPowderCoefficient;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 4;
		}
		
		if (selectedTier3 == 0) {
			toReturn /= 2.0;
		}
		
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier1 == 0) {
			toReturn += 1.0;
		}
		if (selectedTier4 == 1) {
			toReturn += 1.0;
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
			toReturn -= 2;
		}
		
		return (int) Math.round(toReturn);
	}
	private double getArmorBreaking() {
		double toReturn = 1.0;
		if (selectedTier3 == 0) {
			toReturn -= 0.5;
		}
		else if (selectedTier3 == 1) {
			toReturn += 5.0;
		}
		return toReturn;
	}
	private double getStunChance() {
		if (selectedTier4 == 2) {
			return 1.0;
		}
		else {
			return 0;
		}
	}
	private int getStunDuration() {
		if (selectedTier4 == 2) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private double getProjectileVelocity() {
		double toReturn = 1.0;
		
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
		StatsRow[] toReturn = new StatsRow[11];
		
		boolean directDamageModified = selectedTier5 == 1 || selectedTier3 == 0 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified, selectedTier5 == 1 || selectedOverclock == 5);
		
		boolean areaDamageModified = selectedTier1 == 2 || selectedTier2 == 1 || selectedTier3 == 0 || selectedTier4 == 0 || selectedOverclock == 0 || (selectedOverclock > 1 && selectedOverclock < 5);
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier1 == 0 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], modIcons.aoeRadius, aoeRadiusModified);
		
		boolean velocityModified = selectedTier2 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[3] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getProjectileVelocity()), modIcons.projectileVelocity, velocityModified, velocityModified);
		
		toReturn[4] = new StatsRow("Magazine Size:", magazineSize, modIcons.magSize, false);
		
		boolean carriedAmmoModified = selectedTier1 == 1 || selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		toReturn[6] = new StatsRow("Reload Time:", reloadTime, modIcons.reloadSpeed, false);
		
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier3 > -1, selectedTier3 > -1);
		
		toReturn[8] = new StatsRow("Fear Factor:", fearFactor, modIcons.fear, false);
		
		boolean stunEquipped = selectedTier4 == 2;
		toReturn[9] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), modIcons.homebrewPowder, stunEquipped, stunEquipped);
		toReturn[10] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, stunEquipped, stunEquipped);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return true;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// According to Elythnwaen, PGL has a full damage radius of 1.5m, and 15% damage at full radius
		aoeEfficiency = calculateAverageAreaDamage(getAoERadius(), 1.5, 0.15);
	}
	
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double directDamage;
		if (weakpoint && !statusEffects[1]) {
			directDamage = increaseBulletDamageForWeakpoints(getDirectDamage());
		}
		else {
			directDamage = getDirectDamage();
		}
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
		
		double damagePerProjectile = directDamage + areaDamage;
		double baseDPS = damagePerProjectile / reloadTime;
		
		double burnDPS = 0.0;
		// Incendiary Compound
		if (selectedTier3 == 0 && !statusEffects[1]) {
			if (burst) {
				// Heat per Shot shouldn't be affected by IFG or Frozen
				double heatPerGrenade = getDirectDamage() + getAreaDamage();
				double percentageOfEnemiesIgnitedByOneGrenade = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(heatPerGrenade);
				
				burnDPS = percentageOfEnemiesIgnitedByOneGrenade * DoTInformation.Burn_DPS;
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
	public double calculateAdditionalTargetDPS() {
		double totalDPS = getAreaDamage() * aoeEfficiency[1] / reloadTime;
		if (selectedTier3 == 0 && !statusEffects[1]) {
			totalDPS += DoTInformation.Burn_DPS;
		}
		if (selectedOverclock == 4) {
			totalDPS += DoTInformation.Rad_FB_DPS;
		}
		return totalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numShots = 1 + getCarriedAmmo();
		
		double burnDoTTotalDamage = 0;
		if (selectedTier3 == 0) {
			// Technically this is an over-estimation, since the Grenade Launcher can only ignite 74-94% of enemies. However, I'm choosing to artificially increase the 
			// damage dealt by Incendiary Compound to reflect how it would be used as "trash clear" instead of "large enemy killer".
			// I'm also choosing to model this as if the player lets the enemies burn for the full duration, instead of continuing to fire grenades until they die.
			double burnDoTDamagePerEnemy = DoTInformation.Burn_SecsDuration * DoTInformation.Burn_DPS;
			
			// I'm choosing to model this as if the player lets the enemies burn for the full duration, instead of continuing to fire grenades until they die.
			burnDoTTotalDamage = numShots * aoeEfficiency[2] * burnDoTDamagePerEnemy;
		}
		
		double radiationDoTTotalDamage = 0;
		if (selectedOverclock == 4) {
			// I'm guessing that it takes about 4 seconds for enemies to move out of the 8m radius field
			double radiationDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, 4, DoTInformation.Rad_FB_DPS);
			double estimatedNumEnemiesKilled = aoeEfficiency[2] * (calculateFiringDuration() / averageTimeToKill());
			radiationDoTTotalDamage = radiationDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return numShots * (getDirectDamage() + getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2]) + burnDoTTotalDamage + radiationDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return (int) aoeEfficiency[2];
	}

	@Override
	public double calculateFiringDuration() {
		// This is equivalent to counting how many times it has to reload, which is one less than the carried ammo + 1 in the chamber
		return getCarriedAmmo() * reloadTime;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage()) + getAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// This stat is only applicable to "gun"-type weapons
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		double dDamage = getDirectDamage();
		double aDamage = getAreaDamage();
		double explosiveMultiplier, disintegrateMultiplier;
		if (selectedOverclock == 5) {
			disintegrateMultiplier = 1.0;
			explosiveMultiplier = 0.0;
		}
		else {
			explosiveMultiplier = 1.0;
			disintegrateMultiplier = 0.0;
		}
		
		// Disintegrate, Internal, and Kinetic damage are all resistance-less so I can overload the Kinetic portion in Breakpoints()
		double[] directDamage = {
			disintegrateMultiplier * dDamage,  // Kinetic
			explosiveMultiplier * dDamage,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			disintegrateMultiplier * aDamage,  // Kinetic
			explosiveMultiplier * aDamage,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		// Incendiary Compound is a burst of Heat, and gets modeled differently than Radiation
		double heatPerGrenade = 0;
		if (selectedTier3 == 0) {
			heatPerGrenade = getAreaDamage();
		}
		
		double radDamage = 0;
		if (selectedOverclock == 4) {
			radDamage = calculateAverageDoTDamagePerEnemy(0, 4, DoTInformation.Rad_FB_DPS);
		}
		
		double[] DoTDamage = {
			0,  // Fire
			0,  // Electric
			0,  // Poison
			radDamage  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, 0.0, 0.0, heatPerGrenade, statusEffects[1], statusEffects[3], false);
		return MathUtils.sum(breakpoints);
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
		
		// Light Armor Breaking probability
		double AB = getArmorBreaking();
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double areaDamageAB = calculateProbabilityToBreakLightArmor(aoeEfficiency[1] * areaDamage, AB);
		if (directDamage > 0) {
			// Average out the Area Damage Breaking and Direct Damage Breaking
			double directDamageAB = calculateProbabilityToBreakLightArmor(directDamage + areaDamage, AB);
			utilityScores[2] = (directDamageAB + (aoeEfficiency[2] - 1) * areaDamageAB) * UtilityInformation.ArmorBreak_Utility / aoeEfficiency[2];
		}
		else {
			utilityScores[2] = areaDamageAB * UtilityInformation.ArmorBreak_Utility;
		}

		// Fear (baseline function of the Grenade Launcher)
		utilityScores[4] = calculateFearProcProbability(fearFactor) * aoeEfficiency[2] * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
		
		// Stun (T4.C 100% stun chance, 3 sec duration)
		if (selectedTier4 == 2) {
			utilityScores[5] = getStunChance() * aoeEfficiency[2] * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedTier3 == 0) {
			// These methods already divide by 2 when this mod is selected; no need to do it again.
			double heatPerGrenade = getDirectDamage() + getAreaDamage();
			return EnemyInformation.averageTimeToIgnite(0, heatPerGrenade, 1.0 / reloadTime, 0);
		}
		else {
			return -1;
		}
	}
	
	@Override
	public double damagePerMagazine() {
		// Instead of damage per mag, this will be damage per grenade
		double burnDoTDamagePerEnemy = 0;
		if (selectedTier3 == 0) {
			// Again, this is an intentional overestimation.
			burnDoTDamagePerEnemy = DoTInformation.Burn_SecsDuration * DoTInformation.Burn_DPS;
		}
		
		double radiationDoTDamagePerEnemy = 0;
		if (selectedOverclock == 4) {
			// I'm guessing that it takes about 4 seconds for enemies to move out of the 8m radius field
			radiationDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, 4, DoTInformation.Rad_FB_DPS);
		}
		
		return getDirectDamage() + (getAreaDamage() * aoeEfficiency[1] + burnDoTDamagePerEnemy + radiationDoTDamagePerEnemy) * aoeEfficiency[2];
	}
	
	@Override
	public double timeToFireMagazine() {
		// Grenade Launcher fires its projectile instantly, and then reloads.
		return 0;
	}
	
	@Override
	public double damageWastedByArmor() {
		double weakpointAccuracy = EnemyInformation.probabilityBulletWillHitWeakpoint() * 100.0;
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), getArmorBreaking(), 0.0, 100.0, weakpointAccuracy);
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
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ex1\": { \"name\": \"Effect Radius\", \"value\": 1 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 2 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[2].getText(true), "{ \"dmg\": { \"name\": \"Area Damage\", \"value\": 15 } }", "Icon_Upgrade_AreaDamage", "Area Damage"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 3 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[1].getText(true), "{ \"dmg\": { \"name\": \"Area Damage\", \"value\": 20 } }", "Icon_Upgrade_AreaDamage", "Area Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 0, 0, 12, 0, 18, tier2[2].getText(true), "{ \"ex4\": { \"name\": \"Projectile Velocity\", \"value\": 180, \"percent\": true } }", "Icon_Upgrade_ProjectileSpeed", "Projectile Speed"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[0].getText(true), "{ \"ex5\": { \"name\": \"% Converted to Fire\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Heat", "Heat"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 30, 0, 0, 0, 20, 0, tier3[1].getText(true), "{ \"ex2\": { \"name\": \"Armor Breaking\", \"value\": 500, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 25, 36, 0, 0, 0, 15, tier4[0].getText(true), "{ \"dmg\": { \"name\": \"Area Damage\", \"value\": " + homebrewPowderCoefficient + ", \"multiply\": true } }", 
				"Icon_Overclock_ChangeOfHigherDamage", "Randomized Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 0, 0, 25, 15, 36, tier4[1].getText(true), "{ \"ex1\": { \"name\": \"Effect Radius\", \"value\": 1 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 3800, 15, 0, 0, 0, 36, 25, tier4[2].getText(true), "{ \"ex6\": { \"name\": \"Stun Chance\", \"value\": 100, \"percent\": true } }", "Icon_Upgrade_Stun", "Stun"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 110, 40, 0, 60, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Proximity Trigger\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Special", "Special"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 0, 0, 110, 60, 0, 40, tier5[1].getText(true), "{ \"ex8\": { \"name\": \"Direct Damage\", \"value\": 60 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8100, 0, 105, 135, 0, 70, 0, overclocks[0].getText(true), "{ \"dmg\": { \"name\": \"Area Damage\", \"value\": 10 }, "
				+ "\"ex1\": { \"name\": \"Effect Radius\", \"value\": 0.5 } }", "Icon_Upgrade_Area"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7950, 120, 80, 0, 0, 105, 0, overclocks[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 2 } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7900, 0, 120, 70, 0, 100, 0, overclocks[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 4 }, "
				+ "\"dmg\": { \"name\": \"Area Damage\", \"value\": 10, \"subtract\": true }, \"ex1\": { \"name\": \"Effect Radius\", \"value\": 0.5, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8800, 0, 65, 120, 0, 110, 0, overclocks[3].getText(true), "{ \"ex10\": { \"name\": \"RJ250 Compound\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Area Damage\", \"value\": 25, \"subtract\": true } }", "Icon_Overclock_ExplosionJump"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8300, 105, 120, 0, 0, 70, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Area Damage\", \"value\": 4, \"multiply\": true }, "
				+ "\"ex1\": { \"name\": \"Effect Radius\", \"value\": 1 }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 0.3, \"multiply\": true }, \"ex4\": { \"name\": \"Projectile Velocity\", \"value\": 0.7, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_AreaDamage"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8950, 130, 0, 0, 90, 0, 70, overclocks[5].getText(true), "{ \"ex8\": { \"name\": \"Direct Damage\", \"value\": 385 }, "
				+ "\"ex4\": { \"name\": \"Projectile Velocity\", \"value\": 350, \"percent\": true }, \"ex1\": { \"name\": \"Effect Radius\", \"value\": 0.3, \"multiply\": true }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_ProjectileSpeed"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
