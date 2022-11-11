package drgtools.dpscalc.weapons;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import drgtools.dpscalc.enemies.glyphid.Grunt;
import drgtools.dpscalc.guiPieces.GuiConstants;
import drgtools.dpscalc.guiPieces.accuracyEstimator.AccuracyAnimation;
import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.guiPieces.accuracyEstimator.RectangularAccuracyVisualizer;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.weapons.SpreadCurve;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.utilities.Point2D;

public class AccuracyEstimator {
	
	private double delayBeforePlayerReaction;
	private double playerRecoilRecoveryPerSecond;
	
	private boolean circularAccuracy;
	private double targetDistanceMeters;
	private boolean modelRecoil;
	private boolean dwarfIsMoving;
	private boolean visualizeGeneralAccuracy;
	private boolean canBeVisualized;
	
	private SpreadCurve spreadTransformingCurve;
	
	private double[] bulletFiredTimestamps;
	
	private double rateOfFire;
	private int magSize, burstSize;
	private double horizontalBaseSpread, verticalBaseSpread, avgBaseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, spreadPenaltyWhileMoving;
	private double recoilPitch, recoilYaw, mass, springStiffness;
	private double naturalFrequency, initialVelocity, recoilGoal, recoilPerShotEndTime;
	
	// Used by Projectile Accuracy
	private double expectedPerfectAccuracyTravelTime;
	private double gravityAcceleration;
	private double ceilingHeight;
	
	public AccuracyEstimator() {
		// With these two values, recoil should be reduced to 0% in exactly 0.5 seconds.
		delayBeforePlayerReaction = 0.15;  // seconds
		playerRecoilRecoveryPerSecond = 1.00/0.35;  // Percentage of max recoil that the player recovers per second
		
		// Define the "max travel time" that a projectile would be expected to still hit its target dead-center
		expectedPerfectAccuracyTravelTime = 0.3;  // seconds
		gravityAcceleration = 9.8;  // m/sec^2
		ceilingHeight = 10.0;  // meters
		
		// Start at 10m distance for all weapons in AccuracyEstimator, but let it be changed by individual weapons as necessary.
		targetDistanceMeters = 10.0;
		visualizeGeneralAccuracy = true;
		dwarfIsMoving = false;
		modelRecoil = true;
		
		spreadTransformingCurve = null;
		
		// This variable determines the minimum value for Recoil(t) to fall to before that recoil is discarded by successive shots
		recoilGoal = 0.1;
		
		canBeVisualized = false;
	}
	
	// Setters and Getters
	public void setDistance(double newDistance) {
		targetDistanceMeters = newDistance;
	}
	public double getDistance() {
		return targetDistanceMeters;
	}
	
	public void setModelRecoil(boolean newValue) {
		modelRecoil = newValue;
	}
	public boolean isModelingRecoil() {
		return modelRecoil;
	}
	
	public void setDwarfIsMoving(boolean newValue) {
		dwarfIsMoving = newValue;
	}
	public boolean getDwarfIsMoving() {
		return dwarfIsMoving;
	}
	
	public void makeVisualizerShowGeneralAccuracy(boolean value) {
		visualizeGeneralAccuracy = value;
	}
	public boolean visualizerShowsGeneralAccuracy() {
		return visualizeGeneralAccuracy;
	}
	
	public void setSpreadCurve(SpreadCurve sc) {
		spreadTransformingCurve = sc;
	}
	
	private double convertDegreesToMeters(double degrees) {
		// Because both recoil and spread use degrees as their output, I have to first convert from degrees to radians for the Math package
		return targetDistanceMeters * Math.tan(degrees * Math.PI / 180.0);
	}
	
	private void calculateBulletFiredTimestamps() {
		bulletFiredTimestamps = new double[magSize];
		
		double timeBetweenBursts = 1.0 / rateOfFire;
		
		double currentTime = 0.0;
		for (int i = 0; i < magSize; i++) {
			bulletFiredTimestamps[i] = currentTime;
			
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				// During burst; add 1/20th second
				currentTime += 0.05;
			}
			else {
				// Either this gun doesn't have a burst mode, or it just fired the last bullet during a burst
				currentTime += timeBetweenBursts;
			}
		}
	}
	
	private double getTotalSpreadAtTime(double t) {
		// This method is modeled as if every bullet was fired at maximum possible RoF
		
		// For practicality purposes, I have to model it as if the exact moment the bullet gets fired its Total Spread stays the same, and then gets added a very short time afterwards.
		double spreadPerShotAddTime = 0.01;
		double minimumSpread;
		if (dwarfIsMoving) {
			minimumSpread = spreadPenaltyWhileMoving;
		}
		else {
			minimumSpread = 0.0;
		}
		double currentSpread = minimumSpread;
		double bulletFiredTimestamp, nextTimestamp;
		for (int i = 0; i < bulletFiredTimestamps.length; i++) {
			bulletFiredTimestamp = bulletFiredTimestamps[i];
			
			// Early exit condition: if t is before any bullet timestamp, that means that this for loop should stop evaluating
			if (t < bulletFiredTimestamp) {
				break;
			}
			
			if (t > bulletFiredTimestamp + spreadPerShotAddTime) {
				currentSpread = Math.min(currentSpread + spreadPerShot, maxBloom);
			}
			
			if (i < bulletFiredTimestamps.length - 1) {
				nextTimestamp = bulletFiredTimestamps[i+1];
				if (t >= nextTimestamp) {
					currentSpread = Math.max(currentSpread - (nextTimestamp - bulletFiredTimestamp) * spreadRecoverySpeed, minimumSpread);
				}
				else {
					currentSpread = Math.max(currentSpread - (t - bulletFiredTimestamp) * spreadRecoverySpeed, minimumSpread);
				}
			}
			else {
				// The last bullet is allowed to trail off to Base Spread
				currentSpread = Math.max(currentSpread - (t - bulletFiredTimestamp) * spreadRecoverySpeed, minimumSpread);
			}
		}
		
		if (spreadTransformingCurve != null) {
			return avgBaseSpread + spreadTransformingCurve.convertSpreadValue(currentSpread);
		}
		else {
			return avgBaseSpread + currentSpread;
		}
	}
	
	private double getRecoilPerShotOverTime(double t) {
		return Math.pow(Math.E, -1.0 * naturalFrequency * t) * (initialVelocity * t);
	}
	
	private double getTotalRecoilAtTime(double t, boolean playerReducingRecoil) {
		double total = 0.0;
		double bulletFiredTimestamp;
		
		// Early exit condition: if the user disables "model recoil" just return 0 for all t
		if (!modelRecoil) {
			return 0;
		}
		
		if (playerReducingRecoil) {
			// I'm choosing to model player-reduced recoil as if it goes to zero after 0.5 seconds. For weapons with RoF <=2, that means each burst of bullets become their own pocket of recoil, independent of each other.
			if (rateOfFire > 2) {
				// Early exit condition: if t > 0.5, then the recoil will always be zero.
				if (t > delayBeforePlayerReaction + 1.0/playerRecoilRecoveryPerSecond) {
					return 0;
				}
				
				for (int i = 0; i < bulletFiredTimestamps.length; i++) {
					bulletFiredTimestamp = bulletFiredTimestamps[i];
					if (bulletFiredTimestamp <= t && t <= bulletFiredTimestamp + recoilPerShotEndTime) {
						total += getRecoilPerShotOverTime(t - bulletFiredTimestamp);
					}
				}
				
				double playerReductionMultiplier = 1.0;
				if (t > delayBeforePlayerReaction) {
					playerReductionMultiplier = Math.max(1.0 - (t - delayBeforePlayerReaction) * playerRecoilRecoveryPerSecond, 0);
				}
				
				return total * playerReductionMultiplier;
			}
			else {
				// 1. Find the timestamp of the first bullet of the most recent burst
				int burstStartIndex = magSize - burstSize;  // Default to the last burst in the magazine 
				for (int i = 1; i < magSize / burstSize; i++) {
					if (bulletFiredTimestamps[i * burstSize] > t) {
						burstStartIndex = (i - 1) * burstSize;
						break;
					}
				}
				
				// 2. Add up the total recoil of that burst
				for (int i = 0; i < burstSize; i++) {
					bulletFiredTimestamp = bulletFiredTimestamps[burstStartIndex + i];
					if (bulletFiredTimestamp <= t && t <= bulletFiredTimestamp + recoilPerShotEndTime) {
						total += getRecoilPerShotOverTime(t - bulletFiredTimestamp);
					}
				}
				
				// 3. Apply player reduction to that burst relative to t
				double playerReductionMultiplier = 1.0;
				if ((t - bulletFiredTimestamps[burstStartIndex]) > delayBeforePlayerReaction) {
					playerReductionMultiplier = Math.max(1.0 - ((t - bulletFiredTimestamps[burstStartIndex]) - delayBeforePlayerReaction) * playerRecoilRecoveryPerSecond, 0);
				}
				
				return total * playerReductionMultiplier;
			}
		}
		else {
			for (int i = 0; i < bulletFiredTimestamps.length; i++) {
				bulletFiredTimestamp = bulletFiredTimestamps[i];
				if (bulletFiredTimestamp <= t && t <= bulletFiredTimestamp + recoilPerShotEndTime) {
					total += getRecoilPerShotOverTime(t - bulletFiredTimestamp);
				}
			}
			
			return total;
		}
	}
	
	public double calculateCircularAccuracy(
			boolean weakpointTarget, double RoF, int mSize, int bSize, 
			double hBaseSpread, double vBaseSpread, double SpS, double SRS, double MB, double movePenalty,
			double rPitch, double rYaw, double m, double sStiffness
		) {
		/*
			Step 1: Calculate when bullets will be fired for this magazine, and store the timestamps internally
		*/
		circularAccuracy = true;
		rateOfFire = RoF;
		magSize = mSize;
		burstSize = bSize;
		calculateBulletFiredTimestamps();
		
		/*
			Step 2: Calculate what the Current Spread value will be across the whole magazine and store that internally
		*/
		horizontalBaseSpread = hBaseSpread;
		verticalBaseSpread = vBaseSpread;
		// Engineer/Shotgun uses an ellipse instead of a circle. To estimate its accuracy using Lens intersections, I have to approximate that ellipse as a circle with equal area.
		avgBaseSpread = Math.sqrt(horizontalBaseSpread * verticalBaseSpread);
		spreadPerShot = SpS;
		spreadRecoverySpeed = SRS;
		maxBloom = MB;
		spreadPenaltyWhileMoving = movePenalty;
		
		/*
			Step 3: Calculate what the recoil will be at any given time, t, and then store both the raw recoil and player-reduced recoil for use later
		*/
		recoilPitch = rPitch;
		recoilYaw = rYaw;
		mass = m;
		springStiffness = sStiffness;
		naturalFrequency = Math.sqrt(springStiffness / mass);
		initialVelocity = Math.hypot(recoilPitch, recoilYaw);
		if (initialVelocity > 0) {
			recoilPerShotEndTime = -1.0 * MathUtils.lambertInverseWNumericalApproximation(-naturalFrequency * recoilGoal / initialVelocity) / naturalFrequency;
		}
		else {
			recoilPerShotEndTime = 0.0;
		}
		
		/*
			Step 4: Use Spread and Player-Reduced Recoil to calculate the size and offset of the crosshair relative to the static target for each bullet in the magazine
		*/
		canBeVisualized = true;
		
		double targetRadius = 0.0;
		if (weakpointTarget) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		double sumOfAllProbabilities = 0.0;
		double bulletFiredTimestamp, crosshairRadius, crosshairRecoil, P; 
		for (int i = 0; i < magSize; i++) {
			bulletFiredTimestamp = bulletFiredTimestamps[i];
			// Spread Units are like the FoV setting; it needs to be divided by 2 before it can be used in trigonometry correctly
			crosshairRadius = convertDegreesToMeters(getTotalSpreadAtTime(bulletFiredTimestamp) / 2.0);
			crosshairRecoil = convertDegreesToMeters(getTotalRecoilAtTime(bulletFiredTimestamp, true));
			
			if (targetRadius >= crosshairRadius) {
				if (crosshairRecoil <= targetRadius - crosshairRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
					P = 1.0;
				}
				else if (crosshairRecoil >= targetRadius + crosshairRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = MathUtils.areaOfLens(targetRadius, crosshairRadius, crosshairRecoil) / (Math.PI * Math.pow(targetRadius, 2));
				}
			}
			else {
				if (crosshairRecoil <= crosshairRadius - targetRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
					P = Math.pow((targetRadius / crosshairRadius), 2);
				}
				else if (crosshairRecoil >= crosshairRadius + targetRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = MathUtils.areaOfLens(crosshairRadius, targetRadius, crosshairRecoil) / (Math.PI * Math.pow(crosshairRadius, 2));
				}
			}
			
			// System.out.println("P for bullet # " + (i + 1) + ": " + P);
			sumOfAllProbabilities += P;
		}
		
		return sumOfAllProbabilities / magSize * 100.0;
	}
	
	// TODO: someday I might like to model Recoil into this, too...
	public double calculateRectangularAccuracy(boolean weakpoint, double hBaseSpread, double vBaseSpread, double rPitch, double rYaw, double m, double sStiffness) {
		circularAccuracy = false;
		
		// Set up these variables for the Visualizer
		horizontalBaseSpread = hBaseSpread;
		verticalBaseSpread = vBaseSpread;
		spreadPerShot = 0;
		spreadRecoverySpeed = 0;
		maxBloom = 0;
		spreadPenaltyWhileMoving = 0;
		recoilPitch = rPitch;
		recoilYaw = rYaw;
		mass = m;
		springStiffness = sStiffness;
		naturalFrequency = Math.sqrt(springStiffness / mass);
		initialVelocity = Math.hypot(recoilPitch, recoilYaw);
		if (initialVelocity > 0) {
			recoilPerShotEndTime = -1.0 * MathUtils.lambertInverseWNumericalApproximation(-naturalFrequency * recoilGoal / initialVelocity) / naturalFrequency;
		}
		else {
			recoilPerShotEndTime = 0.0;
		}
		canBeVisualized = true;
		
		// Spread Units are like the FoV setting; it needs to be divided by 2 before it can be used in trigonometry correctly
		double crosshairHeightMeters = convertDegreesToMeters(verticalBaseSpread / 2.0);
		double crosshairWidthMeters = convertDegreesToMeters(horizontalBaseSpread / 2.0);
		double targetRadius;
		if (weakpoint) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		/*
			After Lunari pointed out that the old model didn't work in extreme values (like 1m distance away from targets), I basically scrapped the old model
			and decided to brute-force it with a double for-loop. It's not pretty, it's not elegant, but by golly it's gonna be RIGHT. People are trusting me to 
			model this stuff correctly, and this has been bugged for months with only Lunari questioning it. Makes me wonder what else I have wrong in here...
		*/
		double w = crosshairWidthMeters * 2.0, h = crosshairHeightMeters * 2.0;
		double horizontalProbability, verticalProbability, totalProbability;
		double sumOfProbabilitiesInsideTarget = 0.0, sumOfAllProbabilities = 0.0;
		double precision = 50.0;
		
		double i, j;
		for (i = 0.0; i < w; i += w / precision) {
			horizontalProbability = MathUtils.probabilityInNormalDistribution(0, w, i);
			
			for (j = 0.0; j < h; j += h / precision) {
				verticalProbability = MathUtils.probabilityInNormalDistribution(0, h, j);
				totalProbability = horizontalProbability * verticalProbability;
				
				sumOfAllProbabilities += totalProbability;
				if (Math.hypot(crosshairWidthMeters - i, crosshairHeightMeters - j) <= targetRadius) {
					sumOfProbabilitiesInsideTarget += totalProbability;
				}
			}
		}
		
		return (sumOfProbabilitiesInsideTarget / sumOfAllProbabilities) * 100.0;
	}

	private double calculateProjectileTravelTime(double projectileVelocity, double gravityMultiplier, double launchAngleRadians) {
		double verticalVelocity = Math.sin(launchAngleRadians) * projectileVelocity;
		return 2 * verticalVelocity / (gravityAcceleration * gravityMultiplier);
	}
	
	private double calculateProjectileDistance(double projectileVelocity, double gravityMultiplier, double launchAngleRadians) {
		double horizontalVelocity = Math.cos(launchAngleRadians) * projectileVelocity;
		double travelTime = calculateProjectileTravelTime(projectileVelocity, gravityMultiplier, launchAngleRadians);
		return horizontalVelocity * travelTime;
	}
	
	private double calculateMinimumProjectileLaunchAngle(double projectileVelocity, double gravityMultiplier, double goalHorizontalDistance) {
		// angle to get projectile from (0, 0) to (x, y)
		// tan(theta) = (V^2 +- SqRt[V^4 - g(g*x^2 + 2y*v^2)])/(gx)
		// Because this method assumes flat ground, y=0 simplifies the equation to:
		// theta = arctan((V^2 +- SqRt[V^4 - g^2 * x^2])/(gx))
		
		double rootA = Math.atan((Math.pow(projectileVelocity, 2) + Math.sqrt(
				Math.pow(projectileVelocity, 4) - Math.pow(gravityAcceleration * gravityMultiplier, 2) * Math.pow(goalHorizontalDistance, 2))
		) / (gravityAcceleration * gravityMultiplier * goalHorizontalDistance));
		
		double rootB = Math.atan((Math.pow(projectileVelocity, 2) - Math.sqrt(
				Math.pow(projectileVelocity, 4) - Math.pow(gravityAcceleration * gravityMultiplier, 2) * Math.pow(goalHorizontalDistance, 2))
		) / (gravityAcceleration * gravityMultiplier * goalHorizontalDistance));
		
		// Disallow negative launch angles
		rootA = Math.max(rootA, 0);
		rootB = Math.max(rootB, 0);
		
		return Math.min(rootA, rootB);
	}
	
	public double calculateProjectileAccuracy(boolean weakpoint, double projectileRadius, double projectileVelocity, double gravityMultiplier) {
		double actualTravelTime = 0;
		
		// Some projectiles aren't affected by gravity and just fly in a straight line
		if (gravityMultiplier == 0) {
			// If the targetDistanceMeters is greater than "max distance", calculate how much additional time would be needed for the projectile to travel to its target
			actualTravelTime = targetDistanceMeters / projectileVelocity;
		}
		// The rest of the projectiles are pulled downwards by gravity.
		else {
			// For all projectiles that are affected by gravity, I want to limit their effective range by enforcing a ceiling. This method calculates the max angle
			// that the projectile can be launched at and still pass underneath the ceiling, which will then inform the max distance and max travel time values later.
			
			// Max Height = V^2 * sin(theta)^2 / (2 * g)
			// theta = arcsin(SqRt[(Max Height * 2 * g) / (V^2)])
			double maxProjectileLaunchAngle = Math.asin(Math.sqrt((ceilingHeight * 2 * gravityAcceleration * gravityMultiplier) / Math.pow(projectileVelocity, 2)));  // radians
			double absoluteMaxDistance = calculateProjectileDistance(projectileVelocity, gravityMultiplier, maxProjectileLaunchAngle);
			
			// Early exit condition: if a projectile is affected by gravity and the launch angle required to travel the targetDistance is so high that it would hit the ceiling, return 0% Accuracy to indicate it's impossible.
			if (absoluteMaxDistance < targetDistanceMeters) {
				return 0;
			}
			
			// At this point, it's now safe to assume that the projectile CAN reach the target; it's just a question of how long it takes.
			double minimumLaunchAngle = calculateMinimumProjectileLaunchAngle(projectileVelocity, gravityMultiplier, targetDistanceMeters);  // radians
			actualTravelTime = calculateProjectileTravelTime(projectileVelocity, gravityMultiplier, minimumLaunchAngle);
		}
		
		// If the travel time is greater than the expected time to have every projectile hit dead-on, estimate how much distance the target would move horizontally (account for Hazard speed scaling multiplier)
		double targetMovementOffset = 0;  // meters
		if (actualTravelTime > expectedPerfectAccuracyTravelTime) {
			double gruntMovespeed = new Grunt().getMaxMovespeedWhenFeared() * EnemyInformation.getMovespeedDifficultyScaling();
			// This is the angle from which the Grunt is walking towards you. 0 = straight in line with your crosshair, 90 = directly to your right or left
			double angleOfIncidence = 15.0; // degrees
			double horizontalTargetMovementSpeed = gruntMovespeed * Math.sin(angleOfIncidence * Math.PI / 180.0);
			
			targetMovementOffset = (actualTravelTime - expectedPerfectAccuracyTravelTime) * horizontalTargetMovementSpeed;
		}
		
		// Finally, calculate the Lens formed by the Projectile Circle cross-section and the Target Circle. Offset from center by the distance traveled, instead of by Recoil.
		// This re-uses the logic from Circular Accuracy
		double targetRadius = 0.0;
		if (weakpoint) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		double P = 0;  // probability of hitting target
		if (targetRadius >= projectileRadius) {
			if (targetMovementOffset <= targetRadius - projectileRadius) {
				// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
				P = 1.0;
			}
			else if (targetMovementOffset >= targetRadius + projectileRadius) {
				// In this case, the two circles have no intersection.
				P = 0.0;
			}
			else {
				// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
				P = MathUtils.areaOfLens(targetRadius, projectileRadius, targetMovementOffset) / (Math.PI * Math.pow(targetRadius, 2));
			}
		}
		else {
			if (targetMovementOffset <= projectileRadius - targetRadius) {
				// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
				P = Math.pow((targetRadius / projectileRadius), 2);
			}
			else if (targetMovementOffset >= projectileRadius + targetRadius) {
				// In this case, the two circles have no intersection.
				P = 0.0;
			}
			else {
				// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
				P = MathUtils.areaOfLens(projectileRadius, targetRadius, targetMovementOffset) / (Math.PI * Math.pow(projectileRadius, 2));
			}
		}
		
		return P * 100.0;
	}
	
	public boolean visualizerIsReady() {
		return canBeVisualized;
	}
	
	public JPanel getVisualizer() {
		JPanel toReturn = new JPanel();
		
		double sampleDensity = 100.0;
		int i;
		
		if (circularAccuracy) {
			// Part 1: figuring out stuff before rendering
			double lastBulletFiredTimestamp = bulletFiredTimestamps[bulletFiredTimestamps.length - 1];
			// For Engineer/Shotgun, SRS = 0, so I have to use Math.min() to sidestep 0/0 errors.
			double loopDuration = lastBulletFiredTimestamp + Math.max((getTotalSpreadAtTime(lastBulletFiredTimestamp + 0.01) - avgBaseSpread) / Math.max(spreadRecoverySpeed, 0.00001), recoilPerShotEndTime);
			
			// There will be this many data points taken per second (should be at least 10?)
			// This number should match the FPS in AccuracyAnimation so that every frame is just pulling a the next value
			double timeBetweenSamples = 1.0 / sampleDensity;
			int numSamples = (int) Math.ceil(loopDuration * sampleDensity) + 1;
			
			// Part 2: constructing the datasets to plot
			double currentTime, currentValue;
			
			ArrayList<Point2D> rawSpreadData = new ArrayList<Point2D>();
			ArrayList<Point2D> convertedSpreadData = new ArrayList<Point2D>();
			ArrayList<Point2D> rawRecoilData = new ArrayList<Point2D>();
			ArrayList<Point2D> convertedRawRecoilData = new ArrayList<Point2D>();
			ArrayList<Point2D> reducedRecoilData = new ArrayList<Point2D>();
			ArrayList<Point2D> convertedReducedRecoilData = new ArrayList<Point2D>();
			
			double maxSpread = 0.0;
			double minSpread = 10000.0;
			double maxRawRecoil = 0.0;
			double maxReducedRecoil = 0.0;
			for (i = 0; i < numSamples; i++) {
				currentTime = i * timeBetweenSamples;
				
				// Spread
				currentValue = getTotalSpreadAtTime(currentTime);
				minSpread = Math.min(minSpread, currentValue);
				maxSpread = Math.max(maxSpread, currentValue);
				rawSpreadData.add(new Point2D(currentTime, currentValue));
				convertedSpreadData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue / 2.0)));
				
				// Raw Recoil
				currentValue = getTotalRecoilAtTime(currentTime, false);
				maxRawRecoil = Math.max(maxRawRecoil, currentValue);
				rawRecoilData.add(new Point2D(currentTime, currentValue));
				convertedRawRecoilData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue)));
				
				// Reduced Recoil
				currentValue = getTotalRecoilAtTime(currentTime, true);
				maxReducedRecoil = Math.max(maxReducedRecoil, currentValue);
				reducedRecoilData.add(new Point2D(currentTime, currentValue));
				convertedReducedRecoilData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue)));
			}
			
			// Part 3: displaying the data
			JPanel granularDataPanel = new JPanel();
			granularDataPanel.setPreferredSize(new Dimension(420, 684));
			granularDataPanel.setLayout(new BoxLayout(granularDataPanel, BoxLayout.PAGE_AXIS));
			JPanel variables = new JPanel();
			variables.setLayout(new GridLayout(4, 4));
			variables.add(new JLabel("Avg Base Spread:"));
			variables.add(new JLabel(MathUtils.round(avgBaseSpread, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Recoil Pitch:"));
			variables.add(new JLabel(MathUtils.round(recoilPitch, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Spread per Shot:"));
			variables.add(new JLabel(MathUtils.round(spreadPerShot, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Recoil Yaw:"));
			variables.add(new JLabel(MathUtils.round(recoilYaw, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Spread Recovery:"));
			variables.add(new JLabel(MathUtils.round(spreadRecoverySpeed, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Mass:"));
			variables.add(new JLabel(MathUtils.round(mass, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Max Bloom:"));
			variables.add(new JLabel(MathUtils.round(maxBloom, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Spring Stiffness:"));
			variables.add(new JLabel(MathUtils.round(springStiffness, GuiConstants.numDecimalPlaces) + ""));
			granularDataPanel.add(variables);
			
			JPanel recoilPerShotPanel = new JPanel();
			//recoilPerShotPanel.setPreferredSize(new Dimension(228, 162));
			recoilPerShotPanel.setLayout(new BoxLayout(recoilPerShotPanel, BoxLayout.PAGE_AXIS));
			recoilPerShotPanel.add(new JLabel("Recoil per Shot Graph"));
			ArrayList<Point2D> recoilPerShotData = new ArrayList<Point2D>();
			double t;
			for (i = 0; i < (int) Math.ceil(recoilPerShotEndTime * sampleDensity) + 1; i++) {
				t = i*0.01;
				recoilPerShotData.add(new Point2D(t, getRecoilPerShotOverTime(t)));
			}
			double maxRecoilPerShot = getRecoilPerShotOverTime(1.0 / naturalFrequency);
			LineGraph recoilPerShot = new LineGraph(recoilPerShotData, recoilPerShotEndTime, Math.max(3.0, maxRecoilPerShot));
			recoilPerShot.setGraphAnimation(false);
			recoilPerShotPanel.add(recoilPerShot);
			granularDataPanel.add(recoilPerShotPanel);
			
			JPanel lineGraphsPanel = new JPanel();
			lineGraphsPanel.setLayout(new BoxLayout(lineGraphsPanel, BoxLayout.PAGE_AXIS));
			
			LineGraph spreadGraph = new LineGraph(rawSpreadData, loopDuration, Math.max(maxSpread, 8.0));
			new Thread(spreadGraph).start();
			JPanel spreadGraphAndLabel = new JPanel();
			spreadGraphAndLabel.setLayout(new BoxLayout(spreadGraphAndLabel, BoxLayout.PAGE_AXIS));
			spreadGraphAndLabel.add(new JLabel("Crosshair diameter (degrees) vs Time (seconds)"));
			spreadGraphAndLabel.add(spreadGraph);
			spreadGraphAndLabel.setBorder(GuiConstants.blackLine);
			lineGraphsPanel.add(spreadGraphAndLabel);
			
			LineGraph rawRecoilGraph = new LineGraph(rawRecoilData, loopDuration, Math.max(maxRawRecoil, 17.0));
			new Thread(rawRecoilGraph).start();
			JPanel rawRecoilGraphAndLabel = new JPanel();
			rawRecoilGraphAndLabel.setLayout(new BoxLayout(rawRecoilGraphAndLabel, BoxLayout.PAGE_AXIS));
			rawRecoilGraphAndLabel.add(new JLabel("Recoil offset (degrees) vs Time (seconds)"));
			rawRecoilGraphAndLabel.add(rawRecoilGraph);
			rawRecoilGraphAndLabel.setBorder(GuiConstants.blackLine);
			lineGraphsPanel.add(rawRecoilGraphAndLabel);
			
			LineGraph playerReducedRecoilGraph = new LineGraph(reducedRecoilData, loopDuration, Math.max(maxRawRecoil, 17.0));
			new Thread(playerReducedRecoilGraph).start();
			JPanel reducedRecoilAndGraph = new JPanel();
			reducedRecoilAndGraph.setLayout(new BoxLayout(reducedRecoilAndGraph, BoxLayout.PAGE_AXIS));
			reducedRecoilAndGraph.add(new JLabel("Player-reduced recoil offset (degrees) vs Time (seconds)"));
			reducedRecoilAndGraph.add(playerReducedRecoilGraph);
			reducedRecoilAndGraph.setBorder(GuiConstants.blackLine);
			lineGraphsPanel.add(reducedRecoilAndGraph);
			
			AccuracyAnimation rawRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration, 
					convertedSpreadData, convertDegreesToMeters(maxSpread), 
					convertedRawRecoilData, convertDegreesToMeters(maxRawRecoil));
			rawRecoilGif.setBorder(GuiConstants.blackLine);
			new Thread(rawRecoilGif).start();
			
			AccuracyAnimation reducedRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration, 
					convertedSpreadData, convertDegreesToMeters(maxSpread), 
					convertedReducedRecoilData, convertDegreesToMeters(maxReducedRecoil));
			reducedRecoilGif.setBorder(GuiConstants.blackLine);
			new Thread(reducedRecoilGif).start();
			
			toReturn.add(granularDataPanel);
			toReturn.add(lineGraphsPanel);
			toReturn.add(rawRecoilGif);
			toReturn.add(reducedRecoilGif);
		
			/*
				If I ever want to re-add the Spread Curve transformation graphs, this is where I could easily do it.
				
				if (spreadTransformingCurve != null) {
					toReturn.add(spreadTransformingCurve.getGraph());
				}
			*/
		}
		else {
			// This is where the code for showing how Rectangular Accuracy gets visualized.
			
			// First: show the Spread and Recoil stats on the left-hand side, just like Circular Accuracy.
			JPanel granularDataPanel = new JPanel();
			granularDataPanel.setPreferredSize(new Dimension(420, 684));
			granularDataPanel.setLayout(new BoxLayout(granularDataPanel, BoxLayout.PAGE_AXIS));
			JPanel variables = new JPanel();
			variables.setLayout(new GridLayout(5, 4));
			variables.add(new JLabel("Horiz. Spread:"));
			variables.add(new JLabel(MathUtils.round(horizontalBaseSpread, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Recoil Pitch:"));
			variables.add(new JLabel(MathUtils.round(recoilPitch, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Vertical Spread:"));
			variables.add(new JLabel(MathUtils.round(verticalBaseSpread, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Recoil Yaw:"));
			variables.add(new JLabel(MathUtils.round(recoilYaw, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Spread per Shot:"));
			variables.add(new JLabel(MathUtils.round(spreadPerShot, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Mass:"));
			variables.add(new JLabel(MathUtils.round(mass, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Spread Recovery:"));
			variables.add(new JLabel(MathUtils.round(spreadRecoverySpeed, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Spring Stiffness:"));
			variables.add(new JLabel(MathUtils.round(springStiffness, GuiConstants.numDecimalPlaces) + ""));
			variables.add(new JLabel("Max Bloom:"));
			variables.add(new JLabel(MathUtils.round(maxBloom, GuiConstants.numDecimalPlaces) + ""));
			granularDataPanel.add(variables);
			
			JPanel recoilPerShotPanel = new JPanel();
			//recoilPerShotPanel.setPreferredSize(new Dimension(228, 162));
			recoilPerShotPanel.setLayout(new BoxLayout(recoilPerShotPanel, BoxLayout.PAGE_AXIS));
			recoilPerShotPanel.add(new JLabel("Recoil per Shot Graph"));
			ArrayList<Point2D> recoilPerShotData = new ArrayList<Point2D>();
			double t;
			for (i = 0; i < (int) Math.ceil(recoilPerShotEndTime * sampleDensity) + 1; i++) {
				t = i*0.01;
				recoilPerShotData.add(new Point2D(t, getRecoilPerShotOverTime(t)));
			}
			double maxRecoilPerShot = getRecoilPerShotOverTime(1.0 / naturalFrequency);
			LineGraph recoilPerShot = new LineGraph(recoilPerShotData, recoilPerShotEndTime, Math.max(3.0, maxRecoilPerShot));
			recoilPerShot.setGraphAnimation(false);
			recoilPerShotPanel.add(recoilPerShot);
			granularDataPanel.add(recoilPerShotPanel);
			
			// Second: because Recoil is not yet modeled in Rectangular Accuracy, I can just have a custom-rendered panel that I color pixel-by-pixel to show how it works and put in in them middle+right 2/3 of the pop-up.
			RectangularAccuracyVisualizer rectAccVis = new RectangularAccuracyVisualizer(visualizeGeneralAccuracy, convertDegreesToMeters(horizontalBaseSpread/2.0), convertDegreesToMeters(verticalBaseSpread/2.0));
			
			toReturn.add(granularDataPanel);
			toReturn.add(rectAccVis);
		}
		
		return toReturn;
	}
}
