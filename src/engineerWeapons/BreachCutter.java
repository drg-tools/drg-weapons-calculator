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

// Breach Cutter doesn't gain damage from Frozen, and only its Damage per Tick gets boosted by Weakpoint damage. Impact Damage is unaffected when hitting weakpoint.
public class BreachCutter extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double projectileVelocity;
	protected double burstDamageOnFirstImpact;
	protected double damageTickRate;
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
		burstDamageOnFirstImpact = 35;
		damageTickRate = 50;  // ticks/sec
		damagePerTick = 10.0;
		delayBeforeOpening = 0.2;
		projectileLifetime = 1.5;
		projectileWidth = 1.5;
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
		tier1[0] = new Mod("Improved Case Ejector", "-1.0 Reload Time", modIcons.reloadSpeed, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+2 Magazine Size", modIcons.magSize, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Expanded Ammo Bags", "+4 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Condensed Plasma", "+3.4 Damage per Tick", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[2];
		// Although getStats() shows this change, it has no effect on any numbers in this model. As such, I'm marking as "not modeled".
		tier3[0] = new Mod("Prolonged Power Generation", "+1.5 Projectile Lifetime", modIcons.hourglass, 3, 0);
		tier3[1] = new Mod("Loosened Node Cohesion", "+1.5m Plasma Beam Width", modIcons.aoeRadius, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Armor Breaking", "+200% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Disruptive Frequency Tuning", "+100% Stun Chance, 2 sec Stun duration", modIcons.stun, 4, 1);
		// Although getStats() shows this change, it has no effect on any numbers in this model. As such, I'm marking as "not modeled".
		tier4[2] = new Mod("Quick Deploy", "-0.2 Plasma Expansion Delay", modIcons.duration, 4, 2, false);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Explosive Goodbye", "When the line either expires or the trigger gets pulled again, the current line explodes for 60 Explosive Damage in a 3m radius AoE, and leaves behind a field of Persistent Plasma "
				+ " that does an average of " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for 4.6 seconds in a 3m radius sphere.", modIcons.addedExplosion, 5, 0);
		tier5[1] = new Mod("Plasma Trail", "Leaves behind a Persistent Plasma field that does an average of " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for 4.6 seconds "
				+ "along the entire length of the line's path", modIcons.areaDamage, 5, 1);
		// Since the additional lines neither increase targets hit nor DPS per target, I'm marking it as "not modeled"
		tier5[2] = new Mod("Triple Split Line", "Adds a line above and below the primary projectile (multiple lines hitting doesn't increase DPS)", modIcons.aoeRadius, 5, 2, false);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "High Voltage Crossover", "100% chance to electrocute enemies, which deals an average of " + MathUtils.round(4.0 * DoTInformation.Electro_TicksPerSec, GuiConstants.numDecimalPlaces) + " Electric Damage per "
				+ "Second for 4 seconds.", overclockIcons.electricity, 0);
		// Roll Control has no effect on DPS stats, so it gets marked as "not modeled"
		overclocks[1] = new Overclock(Overclock.classification.clean, "Roll Control", "Doubles the number of lines, and orients them like a flattened X. Holding down the trigger after the lines leave the gun causes the sets of lines to start rolling in opposite directions. "
				+ "On release of the trigger, the lines stop rolling.", overclockIcons.rollControl, 1, false);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Stronger Plasma Current", "+1 Damage per Tick, +0.5 Projectile Lifetime", overclockIcons.directDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Return to Sender", "Holding down the trigger after line leaves the gun activates a remote connection, which on release of the trigger causes "
				+ "the line to change direction and move back towards the gun. In exchange, -1.6 Damage per Tick", overclockIcons.returnToSender, 3);
		// TODO: figure out and implement OC "Lance"
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Lance", "Changes orientation of line to fire like a spear as it moves. Either -Dmg/Tick or +Velocity to balance. Or maybe -Width?", overclockIcons.projectileVelocity, 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Spinning Death", "Instead of flying in a straight line, the projectile now rotates 2 times per second about the Yaw axis. Additionally: x0.09 Projectile Velocity, x0 Impact Damage, "
				+ "x2.5 Projectile Lifetime, +1m Plasma Beam Width, x0.25 Damage per Tick, x0.75 Max Ammo, and x0.5 Magazine Size", overclockIcons.special, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Inferno", "Adds 110% of Damage per Tick as Heat Damage which ignites enemies almost instantly in exchange for -0.6 Damage per Tick and x0.25 Armor Breaking", overclockIcons.heatDamage, 6);
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
				case '7': {
					setSelectedOverclock(6, false);
					break;
				}
			}
			
			// Re-set AoE Efficiency
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
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
	
	protected double getProjectileVelocity() {
		double toReturn = projectileVelocity;
		
		// Spinning Death makes it move a lot slower
		if (selectedOverclock == 5) {
			toReturn *= 0.09;
		}
		
		return toReturn;
	}
	protected double getImpactDamage() {
		if (selectedOverclock == 5) {
			return 0.0;
		}
		else {
			return burstDamageOnFirstImpact;
		}
	}
	protected double getDamagePerTick() {
		double toReturn = damagePerTick;
		
		if (selectedTier2 == 1) {
			toReturn += 3.4;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 1.6;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.25;
		}
		else if (selectedOverclock == 6) {
			toReturn -= 0.6;
		}
		
		return toReturn;
	}
	protected double getDelayBeforeOpening() {
		double toReturn = delayBeforeOpening;
		
		if (selectedTier4 == 2) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	protected double getProjectileLifetime() {
		double toReturn = projectileLifetime;
		
		if (selectedTier3 == 0) {
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
	protected double getProjectileWidth() {
		double toReturn = projectileWidth;
		
		if (selectedTier3 == 1) {
			toReturn += 1.5;
		}
		
		if (selectedOverclock == 5) {
			toReturn += 1.0;
		}
		
		return toReturn;
	}
	protected int getMagazineSize() {
		double toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 2.0;
		}
		
		if (selectedOverclock == 5) {
			toReturn /= 2.0;
		}
		
		return (int) toReturn;
	}
	protected int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 4;
		}
		
		if (selectedOverclock == 5) {
			toReturn *= 0.75;
		}
		
		return (int) toReturn;
	}
	protected double getRateOfFire() {
		// OC "Return to Sender" changes max RoF from 1.5 to 1/(2/3 * Lifetime)
		if (selectedOverclock == 3) {
			// This assumes that people let go of the trigger at the two-thirds distance
			return 3.0 / (2.0 * getProjectileLifetime());
		}
		else {
			return rateOfFire;
		}
	}
	protected double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier1 == 0) {
			toReturn -= 1.0;
		}
		
		return toReturn;
	}
	protected double getArmorBreaking() {
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
		
		toReturn[0] = new StatsRow("Burst Damage on First Impact:", getImpactDamage(), modIcons.areaDamage, selectedOverclock == 5);
		
		boolean dmgPerTickModified = selectedTier2 == 1 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Damage per Tick:", getDamagePerTick(), modIcons.directDamage, dmgPerTickModified);
		
		toReturn[2] = new StatsRow("Damage Ticks per Second:", damageTickRate, modIcons.blank, false);
		
		toReturn[3] = new StatsRow("Projectile Width:", getProjectileWidth(), modIcons.aoeRadius, selectedTier3 == 1 || selectedOverclock == 5);
		
		toReturn[4] = new StatsRow("Projectile Velocity (m/sec):", getProjectileVelocity(), modIcons.projectileVelocity, selectedOverclock == 5);
		
		toReturn[5] = new StatsRow("Delay Before Opening:", getDelayBeforeOpening(), modIcons.duration, selectedTier4 == 2);
		
		boolean lifetimeModified = selectedTier3 == 0 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Projectile Lifetime (sec):", getProjectileLifetime(), modIcons.hourglass, lifetimeModified);
		
		toReturn[7] = new StatsRow("Avg Damage per Projectile to Single Grunt:", calculateAverageDamagePerGrunt(true, true, false, true), modIcons.special, false);
		
		toReturn[8] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedTier1 == 1 || selectedOverclock == 5);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedOverclock == 5;
		toReturn[9] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[10] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, selectedOverclock == 3);
		
		toReturn[11] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 0);
		
		boolean armorBreakingModified = selectedTier4 == 0 || selectedOverclock == 6;
		toReturn[12] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, armorBreakingModified, armorBreakingModified);
		
		boolean stunEquipped = selectedTier4 == 1;
		toReturn[13] = new StatsRow("Stun Chance:", convertDoubleToPercentage(1.0), modIcons.homebrewPowder, stunEquipped, stunEquipped);
		
		toReturn[14] = new StatsRow("Stun Duration:", 2, modIcons.stun, stunEquipped, stunEquipped);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	protected double calculateAverageIgnitionTime() {
		// OC "Inferno" adds 110% of projectile's damage as Heat
		double heatPerSec = 1.1 * damageTickRate * getDamagePerTick();
		return EnemyInformation.averageTimeToIgnite(heatPerSec);
	}
	
	protected double calculateGruntIntersectionTimePerRegularProjectile() {
		double secondsOfIntersection = (2.0 * EnemyInformation.GlyphidGruntBodyAndLegsRadius) / getProjectileVelocity();
		if (selectedOverclock == 3) {
			// OC "Return to Sender" doubles how long a single projectile can intersect a single target
			secondsOfIntersection *= 2.0;
		}
		
		return secondsOfIntersection;
	}
	
	// This method isn't perfect but it's a good start. It should eventually model how the enemies move instead of stand still and work out a couple of math/logic overlaps that I'm choosing to neglect for right now.
	protected double calculateAverageGruntIntersectionTimePerSpinningDeathProjectile() {
		double sdRotationSpeed = 4 * Math.PI;  // Equals 2 full circles per second
		double sdProjectileVelocity = getProjectileVelocity();
		double sdWidth = getProjectileWidth();
		double sdLifetime = getProjectileLifetime();
		
		double R = sdWidth / 2.0;
		double r = EnemyInformation.GlyphidGruntBodyAndLegsRadius;
		
		// double maxNumHitsDownDiameter = ((sdRotationSpeed / Math.PI) / sdProjectileVelocity) * sdWidth;  // 8*w
		// double avgNumHitsDownChords = ((sdRotationSpeed / Math.PI) / sdProjectileVelocity) * ((Math.PI * Math.pow(R, 2)) / sdWidth);  // 2Pi*w
		
		// I'm choosing to model this as if the Spinning Death projectile is centered on (0, 0) and doesn't move, and a Grunt is moving through its damage area at the Projectile Velocity. It helps simplify the math a little bit.
		double horizontalOffsetFromCenterForRepresentativeChord = 1.0;  // (Math.sqrt(Math.pow(maxNumHitsDownDiameter, 2) - Math.pow(avgNumHitsDownChords, 2)) / maxNumHitsDownDiameter) * R; // 0.3095*w
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
				As the Grunt moves through the Spinning Death projectile, there are 4 states of intersection: 
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
		
		return totalNumSecondsThatSpinningDeathIntersectsGrunt;
	}
	
	/*
		I want this method to model the DPS of the projectile as it passes through the entirety of a single grunt. This means
		modeling the impact damage, the listed DPS, the DoTs, and the explosion from Explosive Goodbye.
	*/
	protected double calculateAverageDamagePerGrunt(boolean extendDoTsBeyondIntersection, boolean primaryTarget, boolean weakpoint, boolean ignoreStatusEffects) {
		double intersectionTime;
		if (selectedOverclock == 5) {
			intersectionTime = calculateAverageGruntIntersectionTimePerSpinningDeathProjectile();
		}
		else {
			intersectionTime = calculateGruntIntersectionTimePerRegularProjectile();
		}
		
		double impactDamage = getImpactDamage();
		double dmgPerTick = getDamagePerTick();
		double explosiveGoodbyeDmg = 0;
		if (selectedTier5 == 0 && primaryTarget) {
			explosiveGoodbyeDmg = 60.0;
		}
		
		if (!ignoreStatusEffects) {
			// None of Breach Cutter's damage benefits from enemies being Frozen.
			
			// IFG Grenade
			if (statusEffects[3]) {
				dmgPerTick *= UtilityInformation.IFG_Damage_Multiplier;
				impactDamage *= UtilityInformation.IFG_Damage_Multiplier;
				explosiveGoodbyeDmg *= UtilityInformation.IFG_Damage_Multiplier;
			}
			
			// Weakpoint doesn't apply when enemies are Frozen
			if (weakpoint && !statusEffects[1]) {
				// Only the Dmg/Tick benefits from Weakpoints
				dmgPerTick *= EnemyInformation.averageWeakpointDamageIncrease();
			}
		}
		else {
			if (weakpoint) {
				dmgPerTick *= EnemyInformation.averageWeakpointDamageIncrease();
			}
		}
		
		double baseDamage = impactDamage + intersectionTime * damageTickRate * dmgPerTick + explosiveGoodbyeDmg;
		
		double burnDamage = 0;
		// If Frozen, then they can't Burn. However, the logic gets tricky when trying to ignore Status Effects like Frozen for max damage calculations.
		if ((selectedOverclock == 6 && ignoreStatusEffects) || (selectedOverclock == 6 && !ignoreStatusEffects && !statusEffects[1])) {
			double ignitionTime = calculateAverageIgnitionTime();
			double burnDoTDuration;
			if (extendDoTsBeyondIntersection) {
				burnDoTDuration = DoTInformation.Burn_SecsDuration;
			}
			else {
				burnDoTDuration = intersectionTime - ignitionTime;
			}
			
			burnDamage = DoTInformation.Burn_DPS * burnDoTDuration;
		}
		
		double electrocuteDamage = 0;
		if (selectedOverclock == 0) {
			double electrocuteDoTDuration;
			if (extendDoTsBeyondIntersection) {
				// OC "High Voltage Crossover" has an increased duration of 4 sec
				electrocuteDoTDuration = 4.0;
			}
			else {
				electrocuteDoTDuration = intersectionTime;
			}
			
			// OC "High Voltage Crossover" also has an increased damage of 4 Damage/tick
			electrocuteDamage = 4 * DoTInformation.Electro_TicksPerSec * electrocuteDoTDuration;
		}
		
		double plasmaDamage = 0;
		if (selectedTier5 == 0 || selectedTier5 == 1) {
			double plasmaDoTDuration;
			if (extendDoTsBeyondIntersection) {
				if (selectedTier5 == 0) {
					// I'm estimating that Grunts will walk out of the Explosive Goodbye sphere in about 1.5 seconds
					plasmaDoTDuration = 1.5;
				}
				else {
					// Due to top-level if-statement, this is implicitly selectedTier5 == 1
					// I'm estimating that Grunts will walk out of the Persistent Plasma trail in about 2 seconds
					plasmaDoTDuration = 2.0;
				}
			}
			else {
				// Because intersectionTime takes into account both Spinning Death and Return to Sender, I shouldn't have to worry about them here.
				plasmaDoTDuration = intersectionTime;
			}
			
			plasmaDamage = DoTInformation.Plasma_DPS * plasmaDoTDuration;
		}
		
		return baseDamage + burnDamage + electrocuteDamage + plasmaDamage;
	}
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		// Breach Cutter sometimes deals Splash damage for Explosive Goodbye
		// TODO: in the current model, this splash damage doesn't get used. I'm unsure if I want to keep this.
		return selectedTier5 == 0;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// According to Elythnwaen, Explosive Goodbye does 40 Explosive Damage in a 3m radius, 2m Full Damage radius. 
		// No listed falloff percentage, so I'm just going to use the default 0.25
		// TODO: in the current model, this AoE Efficiency isn't used. I'm unsure if I want to keep this.
		aoeEfficiency = calculateAverageAreaDamage(3, 2, 0.25);
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean primaryTarget, boolean weakpoint) {
		double damagePerProjectileToSingleGrunt = calculateAverageDamagePerGrunt(false, primaryTarget, weakpoint, false);
		double dmgPerMag = damagePerProjectileToSingleGrunt * getMagazineSize();
		
		double duration;
		if (burst) {
			duration = getMagazineSize() / getRateOfFire();
		}
		else {
			duration = getMagazineSize() / getRateOfFire() + getReloadTime();
		}
		
		double baseDPS = dmgPerMag / duration;
		
		double burnDPS = 0;
		// Frozen negates the Burn DoT
		if (selectedOverclock == 6 && !statusEffects[1]) {
			// Because OC "Inferno" ignites all enemies just so dang fast, I'm choosing to over-estimate the Burn DPS for bursts as if they ignite instantly.
			burnDPS = DoTInformation.Burn_DPS;
		}
		
		double electroDPS = 0;
		if (selectedOverclock == 0) {
			// OC "High Voltage Crossover" has an increased damage of 4 dmg/tick
			electroDPS = 4.0 * DoTInformation.Electro_TicksPerSec;
		}
		
		double plasmaDPS = 0;
		if (selectedTier5 == 0 || selectedTier5 == 1) {
			plasmaDPS = DoTInformation.Plasma_DPS;
		}
		
		return baseDPS + burnDPS + electroDPS + plasmaDPS;
	}
	
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		return calculateSingleTargetDPS(burst, true, weakpoint);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		return calculateSingleTargetDPS(false, false, false);
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return calculateMaxNumTargets() * calculateAverageDamagePerGrunt(true, true, false, true) * (getMagazineSize() + getCarriedAmmo());
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
			// ArcticEcho recommended (width + 1) as an estimate for average number of enemies hit by a line simultaneously
			numGruntsHitSimultaneouslyPerRow = (int) Math.floor(width + 1);
		}
		
		int numRowsOfGruntsHitDuringProjectileLifetime = (int) Math.ceil((velocity / (4.0 * EnemyInformation.GlyphidGruntBodyAndLegsRadius)) * lifetime);
		
		// System.out.println("Num grunts per row: " + numGruntsHitSimultaneouslyPerRow + ", Num rows of grunts: " + numRowsOfGruntsHitDuringProjectileLifetime);
		
		return numGruntsHitSimultaneouslyPerRow * numRowsOfGruntsHitDuringProjectileLifetime;
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		// Yes extend DoT durations, yes primary target, no weakpoint
		double dmgPerShot = calculateAverageDamagePerGrunt(true, true, false, true);
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(calculateAverageDamagePerGrunt(true, true, false, true));
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
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
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDamagePerTick(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		int maxNumTargets = calculateMaxNumTargets();
		
		// Slow
		if (selectedOverclock == 0) {
			// OC "High Voltage Crossover" applies an Electrocute DoT that slows movement by 80% for 4 seconds
			// This overrides the built-in 70% slow during intersection, instead of adding to it.
			utilityScores[3] += maxNumTargets * 4.0 * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			// Breach Cutter slows enemy movement by 70% while the line intersects their hitbox.
			double intersectionTime;
			if (selectedOverclock == 5) {
				intersectionTime = calculateAverageGruntIntersectionTimePerSpinningDeathProjectile();
			}
			else {
				intersectionTime = calculateGruntIntersectionTimePerRegularProjectile();
			}
			utilityScores[3] = maxNumTargets * intersectionTime * 0.7;
		}
		
		// Stun
		// T4.B has a 100% chance to stun for 3 seconds
		if (selectedTier4 == 1) {
			utilityScores[5] = maxNumTargets * 3.0 * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		if (selectedOverclock == 6) {
			// OC "Inferno" adds 110% of the Beam DPS as Heat Damage, so the time to Ignite is pretty darn fast
			return EnemyInformation.averageTimeToIgnite(1.1 * getDamagePerTick() * damageTickRate);
		}
		else {
			return -1;
		}
	}
	
	@Override
	public double damagePerMagazine() {
		return calculateMaxNumTargets() * calculateAverageDamagePerGrunt(true, true, false, true) * getMagazineSize();
	}
	
	@Override
	public double timeToFireMagazine() {
		int magSize = getMagazineSize();
		if (magSize > 1) {
			return magSize / getRateOfFire();
		}
		else {
			// Spinning Death without T2.B Mag Size only has one shot before reloading, so much like the Grenade Launcher its time to fire magazine would be zero.
			return 0;
		}
	}
	
	@Override
	public double damageWastedByArmor() {
		return 0;
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
