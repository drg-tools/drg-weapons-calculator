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
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public class BreachCutter extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double projectileVelocity;
	private double burstDamageOnFirstImpact;
	private double damageTickRate;
	private double damagePerTick;
	private double delayBeforeOpening;
	private double projectileLifetime;
	private double projectileWidth;
	private int magazineSize;
	private int carriedAmmo;
	private double rateOfFire;
	private double reloadTime;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public BreachCutter() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public BreachCutter(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public BreachCutter(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Breach Cutter";
		weaponPic = WeaponPictures.breachCutter;
		
		// Base stats, before mods or overclocks alter them:
		projectileVelocity = 10;  // m/sec
		// In the game files this is listed as "Burn" damage, which translates to Fire Element damage in this program's terminology
		burstDamageOnFirstImpact = 50;
		damageTickRate = 50;  // ticks/sec
		damagePerTick = 11.5;
		delayBeforeOpening = 0.2;
		projectileLifetime = 1.5;
		projectileWidth = 2;
		magazineSize = 4;
		carriedAmmo = 12;
		rateOfFire = 1.5;
		reloadTime = 3.0;
		
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
		tier1[0] = new Mod("Prolonged Power Generation", "+1.5 Projectile Lifetime", modIcons.hourglass, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+2 Clip Size", modIcons.magSize, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Ammo Bags", "+8 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Condensed Plasma", "+3.5 Damage per Tick", modIcons.directDamage, 2, 1);
		tier2[2] = new Mod("Loosened Node Cohesion", "+1m Plasma Beam Width", modIcons.aoeRadius, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Quick Deploy", "-0.2 Plasma Expansion Delay", modIcons.duration, 3, 0);
		tier3[1] = new Mod("Loosened Node Cohesion", "+1m Plasma Beam Width", modIcons.aoeRadius, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Armor Breaking", "+200% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Disruptive Frequency Tuning", "+100% Stun Chance, 3 sec Stun duration", modIcons.stun, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Explosive Goodbye", "When the line either expires or the trigger gets pulled again, the current line explodes for 40 Explosive Damage in a 3m radius AoE, and leaves behind a field of Persistent Plasma "
				+ " that does an average of " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for 4.6 seconds in a 3m radius sphere.", modIcons.addedExplosion, 5, 0, false);
		tier5[1] = new Mod("Plasma Trail", "Leaves behind a Persistent Plasma field that does an average of " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for 4.6 seconds "
				+ "along the entire length of the line's path", modIcons.areaDamage, 5, 1, false);
		tier5[2] = new Mod("Triple Split Line", "Adds a line above and below the primary projectile (multiple lines hitting doesn't increase DPS)", modIcons.aoeRadius, 5, 2, false);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Light-Weight Cases", "+4 Max Ammo, -0.2 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Roll Control", "Holding down the trigger after the line leaves the gun causes the line to start rolling. On release of the trigger, the line stops rolling.", overclockIcons.rollControl, 1, false);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Stronger Plasma Current", "+1 Damage per Tick, +0.5 Projectile Lifetime", overclockIcons.directDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Return to Sender", "Holding down the trigger after line leaves the gun activates a remote connection, which on release of the trigger causes "
				+ "the line to change direction and move back towards the gun. In exchange, -4 Max Ammo", overclockIcons.returnToSender, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "High Voltage Crossover", "100% chance to electrocute enemies, which deals an average of 16.0 Electric Damage per Second for 4 seconds. In exchange, -2 Magazine Size.", overclockIcons.electricity, 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Spinning Death", "Spinning Death, x0.05 Projectile Velocity, x0 Impact Damage, x2.5 Projectile Lifetime, x0.2 Damage per Tick, x0.5 Max Ammo, and x0.25 Magazine Size", overclockIcons.special, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Inferno", "Adds 90% of Damage per Tick as Heat Damage which ignites enemies almost instantly in exchange for -3.5 Damage per Tick, -4 Max Ammo, and x0.25 Armor Breaking", overclockIcons.heatDamage, 6, false);
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
				System.out.println("Breach Cutter's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Breach Cutter's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Breach Cutter's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-7 or a hyphen");
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
				case '7': {
					selectedOverclock = 6;
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
	public BreachCutter clone() {
		return new BreachCutter(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "BreachCutter";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.engineerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.breachCutterGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getProjectileVelocity() {
		double toReturn = projectileVelocity;
		
		// Spinning Death makes it move a lot slower
		if (selectedOverclock == 5) {
			toReturn = 0.5;
		}
		
		return toReturn;
	}
	private double getImpactDamage() {
		if (selectedOverclock == 5) {
			return 0.0;
		}
		else {
			return burstDamageOnFirstImpact;
		}
	}
	private double getDamagePerTick() {
		double toReturn = damagePerTick;
		
		if (selectedTier2 == 1) {
			toReturn += 3.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.2;
		}
		else if (selectedOverclock == 6) {
			toReturn -= 3.5;
		}
		
		return toReturn;
	}
	private double getDelayBeforeOpening() {
		double toReturn = delayBeforeOpening;
		
		if (selectedTier3 == 0) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getProjectileLifetime() {
		double toReturn = projectileLifetime;
		
		if (selectedTier1 == 0) {
			toReturn += 1.5;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 2.5;
		}
		
		return toReturn;
	}
	private double getProjectileWidth() {
		double toReturn = projectileWidth;
		
		if (selectedTier2 == 2) {
			toReturn += 1;
		}
		if (selectedTier3 == 1) {
			toReturn += 1;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 4) {
			toReturn -= 2;
		}
		else if (selectedOverclock == 5) {
			toReturn = (int) Math.ceil(toReturn / 4.0);
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 8;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 4;
		}
		else if (selectedOverclock == 3 || selectedOverclock == 6) {
			toReturn -= 4;
		}
		else if (selectedOverclock == 5) {
			toReturn /= 2;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedOverclock == 0) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		double toReturn = 1.0;
		
		if (selectedTier4 == 0) {
			toReturn += 2.0;
		}
		
		if (selectedOverclock == 6) {
			toReturn /= 4.0;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		toReturn[0] = new StatsRow("Burst Damage on First Impact:", getImpactDamage(), selectedOverclock == 5);
		
		boolean dmgPerTickModified = selectedTier2 == 1 || selectedOverclock == 2 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Damage per Tick:", getDamagePerTick(), dmgPerTickModified);
		
		toReturn[2] = new StatsRow("Damage Ticks per Second:", damageTickRate, false);
		
		toReturn[3] = new StatsRow("Projectile Width:", getProjectileWidth(), selectedTier2 == 2 || selectedTier3 == 1);
		
		toReturn[4] = new StatsRow("Projectile Velocity (m/sec):", getProjectileVelocity(), selectedOverclock == 5);
		
		toReturn[5] = new StatsRow("Delay Before Opening:", getDelayBeforeOpening(), selectedTier3 == 0);
		
		boolean lifetimeModified = selectedTier1 == 0 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Projectile Lifetime (sec):", getProjectileLifetime(), lifetimeModified);
		
		double singleGruntDamage;
		int numGrunts = calculateMaxNumTargets();
		if (selectedOverclock == 5) {
			singleGruntDamage = calculateAverageDamageToGruntPerSpinningDeathProjectile();
		}
		else {
			singleGruntDamage = calculateDamageToGruntPerRegularProjectile();
		}
		toReturn[7] = new StatsRow("Damage per Projectile:", numGrunts * singleGruntDamage, false);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[8] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedOverclock == 0 || selectedOverclock == 3 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[9] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[10] = new StatsRow("Rate of Fire:", rateOfFire, false);
		
		toReturn[11] = new StatsRow("Reload Time:", getReloadTime(), selectedOverclock == 0);
		
		boolean armorBreakingModified = selectedTier4 == 0 || selectedOverclock == 6;
		toReturn[12] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), armorBreakingModified, armorBreakingModified);
		
		boolean stunEquipped = selectedTier4 == 1;
		toReturn[13] = new StatsRow("Stun Chance:", convertDoubleToPercentage(1.0), stunEquipped, stunEquipped);
		
		toReturn[14] = new StatsRow("Stun Duration:", 3, stunEquipped, stunEquipped);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private double calculateDamageToGruntPerRegularProjectile() {
		// TODO: model High Voltage Crossover and Inferno in this method
		
		double secondsOfIntersection = 2.0 * EnemyInformation.GlyphidGruntBodyAndLegsRadius / getProjectileVelocity();
		if (selectedOverclock == 3) {
			// OC "Return to Sender" doubles how long a single projectile can intersect a single target
			secondsOfIntersection *= 2.0;
		}
		
		return getImpactDamage() + secondsOfIntersection * damageTickRate * getDamagePerTick();
	}
	
	// This method isn't perfect but it's a good start. It should eventually model how the enemies move instead of stand still and work out a couple of math/logic overlaps that I'm choosing to neglect for right now.
	private double calculateAverageDamageToGruntPerSpinningDeathProjectile() {
		double sdRotationSpeed = 4 * Math.PI;  // Equals 2 full circles per second
		double sdProjectileVelocity = getProjectileVelocity();
		double sdWidth = getProjectileWidth();
		double sdLifetime = getProjectileLifetime();
		
		double R = sdWidth / 2.0;
		double r = EnemyInformation.GlyphidGruntBodyAndLegsRadius;
		
		double maxNumHitsDownDiameter = ((sdRotationSpeed / Math.PI) / sdProjectileVelocity) * sdWidth;  // 8*w
		double avgNumHitsDownChords = ((sdRotationSpeed / Math.PI) / sdProjectileVelocity) * ((Math.PI * Math.pow(R, 2)) / sdWidth);  // 2Pi*w
		
		// I'm choosing to model this as if the Spinning Death projectile is centered on (0, 0) and doesn't move, and a Grunt is moving through its damage area at the Projectile Velocity. It helps simplify the math a little bit.
		double horizontalOffsetFromCenterForRepresentativeChord = (Math.sqrt(Math.pow(maxNumHitsDownDiameter, 2) - Math.pow(avgNumHitsDownChords, 2)) / maxNumHitsDownDiameter) * R; // 0.3095*w
		double representativeChordLength = 2.0 * Math.sqrt(Math.pow(R, 2));
		double verticalOffsetForCenterOfGrunt = Math.sqrt(Math.pow(R + r, 2) - Math.pow(horizontalOffsetFromCenterForRepresentativeChord, 2));
		
		double totalNumSecondsThatSpinningDeathIntersectsGrunt = 0.0;
		double distanceBetweenCirclesCenters, radiansAngleOfIntersection, lensChordLength, lengthOfTangentSegment;
		
		double timeElapsed = 0.0;
		double timeInterval = 1.0 / (sdRotationSpeed / Math.PI);
		double totalDistanceTraveledVertically = 0.0;
		double distanceMovedPerInterval = sdProjectileVelocity * timeInterval;
		while (timeElapsed < sdLifetime && totalDistanceTraveledVertically < (representativeChordLength + 2 * r)) {
			/*
				As the Grunt moves through the Spinning Death projectile, there are 3 states of intersection: 
					1. If the two centers are further apart than their combined radii, then there's no overlap.
					2. When the center of Grunt is still outside the SD circle, the area intersected is a Lens (like AccuracyEstimator) and the angle of rotation intersected is 
						proportional to the chord length across the Lens. Find the chord, translate it to arc length of the SD projectile, and find the radians. Divide radians by rotational speed.
					3. When the center of the Grunt is inside the SD circle but it's far enough away that the center of SD isn't yet inside the Grunt's circle. The angle of intersection is the angle
						between the two lines that intersect at SD's center and are both tangent to Grunt's circle. Divide radians by rotational speed.
					4. When the center of SD is inside Grunt's circle, then angle of intersection is technically infinite. For this case, just add the full timeInterval and move on to the next loop.
			*/
			distanceBetweenCirclesCenters = Math.sqrt(Math.pow(horizontalOffsetFromCenterForRepresentativeChord, 2) + Math.pow(verticalOffsetForCenterOfGrunt, 2));
			
			// Case 1: No overlap
			if (distanceBetweenCirclesCenters >= R + r) {
				// Do nothing, just move onto next loop
			}
			
			// Case 2: Lens
			else if (distanceBetweenCirclesCenters >= R && distanceBetweenCirclesCenters < R + r) {
				/*
					This is by far the most complicated case to calculate. Because we know that this case is a Lens, there's a formula to find the length of the chord shared by the two circles
					inside the Lens. Using that chord length and some more geometry, we can calculate the angle of intersection
				*/
				// Sourced from https://mathworld.wolfram.com/Circle-CircleIntersection.html
				lensChordLength = (1.0 / distanceBetweenCirclesCenters) * Math.sqrt((-distanceBetweenCirclesCenters + r - R) * (-distanceBetweenCirclesCenters - r + R) * (-distanceBetweenCirclesCenters + r + R) * (distanceBetweenCirclesCenters + r + R));
				radiansAngleOfIntersection = 2.0 * Math.asin(lensChordLength / (2.0 * R));
				totalNumSecondsThatSpinningDeathIntersectsGrunt += radiansAngleOfIntersection / sdRotationSpeed;
			}
			
			// Case 3: Tangents
			else if (distanceBetweenCirclesCenters >= r && distanceBetweenCirclesCenters < R) {
				/*
					Because Tangents are by definition at right-angles to the center of the circle, and we know the lengths of the two radii, we can use simple trigonometry to calculate
					the angle of intersection.
				*/
				lengthOfTangentSegment = Math.sqrt(Math.pow(distanceBetweenCirclesCenters, 2) - Math.pow(r, 2));
				radiansAngleOfIntersection = 2.0 * Math.atan(r / lengthOfTangentSegment);
				totalNumSecondsThatSpinningDeathIntersectsGrunt += radiansAngleOfIntersection / sdRotationSpeed;
			}
			
			// Case 4: Complete overlap
			else if (distanceBetweenCirclesCenters < r) {
				totalNumSecondsThatSpinningDeathIntersectsGrunt += timeInterval;
			}
			
			timeElapsed += timeInterval;
			totalDistanceTraveledVertically += distanceMovedPerInterval;
			verticalOffsetForCenterOfGrunt -= distanceMovedPerInterval;
		}
		
		// System.out.println("According to the Spinning Death method, on average a Grunt would be intersected for " + totalNumSecondsThatSpinningDeathIntersectsGrunt + " seconds. This should be STRICTLY LESS THAN projectile total lifetime: " + sdLifetime);
		
		return totalNumSecondsThatSpinningDeathIntersectsGrunt * damageTickRate * getDamagePerTick();
	}
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		// Breach Cutter sometimes deals Splash damage for Explosive Goodbye
		return selectedTier5 == 0;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// According to Elythnwaen, Explosive Goodbye does 40 Explosive Damage in a 3m radius, 2m Full Damage radius. 
		// No listed falloff percentage, so I'm just going to use the default 0.25
		aoeEfficiency = calculateAverageAreaDamage(3, 2, 0.25);
	}
	
	// Single-target calculations
	@Override
	public double calculateIdealBurstDPS() {
		return damageTickRate * getDamagePerTick();
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return 1;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return 1;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return 1;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		return 1;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double singleGruntDamage;
		int numGrunts = calculateMaxNumTargets();
		if (selectedOverclock == 5) {
			singleGruntDamage = calculateAverageDamageToGruntPerSpinningDeathProjectile();
		}
		else {
			singleGruntDamage = calculateDamageToGruntPerRegularProjectile();
		}
		return numGrunts * singleGruntDamage * (getMagazineSize() + getCarriedAmmo());
	}

	@Override
	public int calculateMaxNumTargets() {
		int numGruntsHitSimultaneouslyPerRow;
		double width = getProjectileWidth();
		double velocity = getProjectileVelocity();
		double lifetime = getProjectileLifetime();
		if (selectedOverclock == 5) {
			numGruntsHitSimultaneouslyPerRow = calculateNumGlyphidsInRadius(width / 2.0);
		}
		else {
			numGruntsHitSimultaneouslyPerRow = (int) (1.0 + 2.0 * Math.ceil(((width / 2.0) - EnemyInformation.GlyphidGruntBodyRadius) / EnemyInformation.GlyphidGruntBodyAndLegsRadius));
		}
		
		int numRowsOfGruntsHitDuringProjectileLifetime = (int) Math.ceil((velocity / (4.0 * EnemyInformation.GlyphidGruntBodyAndLegsRadius)) * lifetime);
		
		// System.out.println("Num grunts per row: " + numGruntsHitSimultaneouslyPerRow + ", Num rows of grunts: " + numRowsOfGruntsHitDuringProjectileLifetime);
		
		return numGruntsHitSimultaneouslyPerRow * numRowsOfGruntsHitDuringProjectileLifetime;
	}

	@Override
	public double calculateFiringDuration() {
		return 1;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		if (selectedOverclock == 5) {
			return calculateAverageDamageToGruntPerSpinningDeathProjectile();
		}
		else {
			return calculateDamageToGruntPerRegularProjectile();
		}
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Breach Cutter can't be aimed like normal weapons
		return -1;
	}
	
	@Override
	public int breakpoints() {
		// I'm not sure if Breakpoints is needed for Breach Cutter or not... but as it stands it would be impossible to model without knowing the dimensions of all the other creatures
		return 0;
	}

	@Override
	public double utilityScore() {
		return 0;
	}
	
	@Override
	public double damagePerMagazine() {
		return 1;
	}
	
	@Override
	public double timeToFireMagazine() {
		return 1;
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 20, 0, 0, 0, tier1[0].getText(true), "{ \"ex1\": { \"name\": \"Projectile Lifetime\", \"value\": 1.5 } }", "Icon_Upgrade_Duration", "Delay"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 2 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 18, 12, 0, 0, 0, tier2[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 8 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 0, 18, 0, 12, 0, tier2[1].getText(true), "{ \"dmg\": { \"name\": \"Beam DPS\", \"value\": 175 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 12, 0, 0, 18, 0, 0, tier2[2].getText(true), "{ \"ex2\": { \"name\": \"Plasma Beam Width\", \"value\": 1 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 20, 0, 30, 0, tier3[0].getText(true), "{ \"ex3\": { \"name\": \"Plasma Expansion Delay\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_Duration", "Charge Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 20, 30, 0, 0, 0, 0, tier3[1].getText(true), "{ \"ex2\": { \"name\": \"Plasma Beam Width\", \"value\": 1 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 25, 15, 36, 0, 0, tier4[0].getText(true), "{ \"ex4\": { \"name\": \"Armor Breaking\", \"value\": 200, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 25, 0, 15, 0, 36, 0, tier4[1].getText(true), "{ \"ex13\": { \"name\": \"Stun Chance\", \"value\": 100, \"percent\": true }, "
				+ "\"ex14\": { \"name\": \"Stun Duration\", \"value\": 3 } }", "Icon_Upgrade_Stun", "Stun"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[0].getText(true), "{ \"ex5\": { \"name\": \"Explosive Goodbye\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Explosion", "Explosion"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 110, 40, 0, 60, 0, 0, tier5[1].getText(true), "{ \"ex6\": { \"name\": \"Plasma Trail\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_AreaDamage", "Area Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 4400, 0, 0, 40, 0, 110, 60, tier5[2].getText(true), "{ \"ex7\": { \"name\": \"Triple Split Line\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Area", "Area of effect"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8700, 0, 130, 0, 100, 0, 80, overclocks[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 4 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8150, 80, 0, 135, 95, 0, 0, overclocks[1].getText(true), "{ \"ex8\": { \"name\": \"Roll Control\", \"value\": 1, \"boolean\": true } }", "Icon_Overclock_Spinning_Linecutter"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 8650, 75, 0, 0, 100, 0, 140, overclocks[2].getText(true), "{ \"dmg\": { \"name\": \"Beam DPS\", \"value\": 50 }, "
				+ "\"ex1\": { \"name\": \"Projectile Lifetime\", \"value\": 0.5 } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7950, 0, 140, 80, 0, 100, 0, overclocks[3].getText(true), "{ \"ex9\": { \"name\": \"Return to Sender\", \"value\": 1, \"boolean\": true }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 4, \"subtract\": true } }", "Icon_Overclock_ForthAndBack_Linecutter"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7300, 0, 75, 120, 95, 0, 0, overclocks[4].getText(true), "{ \"ex11\": { \"name\": \"High Voltage Crossover\", \"value\": 1, \"boolean\": true }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_Electricity"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8250, 100, 120, 0, 0, 80, 0, overclocks[5].getText(true), "{ \"ex10\": { \"name\": \"Spinning Death\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Beam DPS\", \"value\": 0.2, \"multiply\": true }, \"ex1\": { \"name\": \"Projectile Lifetime\", \"value\": 2.5, \"multiply\": true }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 0.5, \"multiply\": true }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 0.25, \"multiply\": true } }", "Icon_Upgrade_Special"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[6].getShortcutRepresentation(), overclocks[6].getName(), 7550, 135, 0, 0, 70, 0, 90, overclocks[6].getText(true), "{ \"ex12\": { \"name\": \"Inferno\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Beam DPS\", \"value\": 175, \"subtract\": true }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 4, \"subtract\": true }, \"ex4\": { \"name\": \"Armor Breaking\", \"value\": 0.25, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Heat"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
