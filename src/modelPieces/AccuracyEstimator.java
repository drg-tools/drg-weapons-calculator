package modelPieces;

import utilities.MathUtils;

public class AccuracyEstimator {
	public static double targetRadius = 0.4; // meters
	public static double targetDistance = 5.0; // meters
	
	// 0.4, 5.0 feels good for general Accuracy estimation
	// 0.2, 3.7 feels really good for Weakpoint Accuracy estimation
	
	/*
		Except for RoF and magSize, all of these parameters should be passed in as degrees of deviation from a central axis 
		which are strictly less than 90 degrees.
	*/
	public static double calculateAccuracy(double rateOfFire, int numBulletsPerMagazine, int burstSize,
			 double baseSpread, double spreadPerShot, double maxSpread, double spreadRecovery,
			 double recoilPerShot, double maxRecoil, double recoilRecovery) {
		
		
		double RoF = rateOfFire;
		int magSize = numBulletsPerMagazine;
		// The time that passes between each shot
		double deltaT = 1.0 / RoF;
		
		double Sb = baseSpread;
		double Ss = spreadPerShot;
		double Sr = spreadRecovery;
		// TODO: For most guns, the max spread is calculated by adding the base spread + max spread -- thus base and max spread are tied together and grow/shrink by the same value (not same multiplier)
		// This should be refactored to represent that.
		double Sm = maxSpread;
		
		// I'm applying a 0.5 multiplier to all of these recoil coefficients, to factor in the player counter-acting the recoil by 50%.
		// Intentionally using 1 - Counter so that I can change the player's efficiency directly, rather than having to do indirect math every time I want to change the value.
		double playerRecoilCorrectionCoefficient = (1.0 - 0.5);
		double Rs = recoilPerShot * playerRecoilCorrectionCoefficient;
		double Rr = recoilRecovery * playerRecoilCorrectionCoefficient;
		double Rm = maxRecoil * playerRecoilCorrectionCoefficient;
		
		double sumOfAllProbabilities = 0.0;
		double timeElapsed = 0.0;
		
		double reticleRadius, recoil, P; 
		for (int i = 0; i < magSize; i++) {
			reticleRadius = convertDegreesToMeters(radius(i, timeElapsed, Sb, Ss, Sr, Sm));
			// TODO: recoil only starts recovering 0.25 seconds after the gun either stops firing, or in BRT's case after the mouse is clicked to start a burst
			// This means that guns with RoF >= 4 don't have any recoil recovered until they stop firing
			recoil = convertDegreesToMeters(recoil(i, timeElapsed, Rs, Rr, Rm));
			
			if (targetRadius >= reticleRadius) {
				if (recoil <= targetRadius - reticleRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
					P = 1.0;
				}
				else if (recoil >= targetRadius + reticleRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = areaOfLens(targetRadius, reticleRadius, recoil) / (Math.PI * Math.pow(targetRadius, 2));
				}
			}
			else {
				if (recoil <= reticleRadius - targetRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
					P = Math.pow((targetRadius / reticleRadius), 2);
				}
				else if (recoil >= reticleRadius + targetRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = areaOfLens(reticleRadius, targetRadius, recoil) / (Math.PI * Math.pow(reticleRadius, 2));
				}
			}
			
			// System.out.println("P for bullet # " + (i + 1) + ": " + P);
			sumOfAllProbabilities += P;
			
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				// If this gun both has a burst-fire mode and is currently firing a burst, change deltaT
				timeElapsed += 0.05;
			}
			else {
				// If this gun either doesn't have a burst-fire mode, or the burst has completed and it has to wait before it can fire the next burst
				timeElapsed += deltaT;
			}
		}
		
		return sumOfAllProbabilities / magSize * 100.0;
	}
	
	// Both radius() and recoil() return degrees of deviation and need to have their outputs changed to meters before use.
	private static double radius(int numBulletsFired, double timeElapsed, double Sb, double Ss, double Sr, double Sm) {
		return Sb + Math.min(Math.max(numBulletsFired * Ss - timeElapsed * Sr, 0), Sm - Sb);
	}
	
	private static double recoil(int numBulletsFired, double timeElapsed, double Rs, double Rr, double Rm) {
		return Math.min(Math.max(numBulletsFired * Rs - timeElapsed * Rr, 0), Rm);
	}
	
	private static double areaOfLens(double R, double r, double d) {
		// Sourced from https://en.wikipedia.org/wiki/Lens_(geometry)
		double firstThird = Math.pow(r, 2) * Math.acos((Math.pow(d, 2) + Math.pow(r, 2) - Math.pow(R, 2)) / (2 * d * r));
		double secondThird = Math.pow(R, 2) * Math.acos((Math.pow(d, 2) + Math.pow(R, 2) - Math.pow(r, 2)) / (2 * d * R));
		double finalThird = 0.5 * Math.sqrt((-d + r + R) * (d - r + R) * (d + r - R) * (d + r + R));
		
		return firstThird + secondThird - finalThird;
	}
	
	public static double convertDegreesToMeters(double degrees) {
		double radians = degrees * Math.PI / 180.0;
		return targetDistance * Math.tan(radians);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static double targetDistanceMeters = 8.0;
	private static double testingDistancePixels = 1074.047528;
	
	private static double convertSpreadPixelsToRads(double px) {
		return Math.atan((px / (2 * testingDistancePixels)));
	}
	private static double convertRecoilPixelsToRads(double px) {
		return Math.atan(px / testingDistancePixels);
	}
	private static double convertRadiansToMeters(double r) {
		return targetDistanceMeters * Math.tan(r);
	}
	
	private static double spread2(int numBulletsFired, double timeElapsed, double baseSpreadRads, double spreadPerShotRads, double spreadRecoveryRads, double maxSpreadRads) {
		// This can never be less than 0 pixels of change
		double calculatedChangeInSpread = Math.max(numBulletsFired * spreadPerShotRads - timeElapsed * spreadRecoveryRads, 0);
		
		// It can also never be so high that it exceeds Max Spread
		double effectiveChangeInSpread = Math.min(calculatedChangeInSpread, maxSpreadRads - baseSpreadRads);
		
		return baseSpreadRads + effectiveChangeInSpread;
	}
	
	private static boolean bulletFiredOnThisTick(int thisTick, int numBulletsFiredSoFar, int burstSize, int ticksBetweenBursts, int ticksBetweenBulletsDuringBurst) {
		if (burstSize > 1) {
			int numberOfFullBurstsFired = numBulletsFiredSoFar / burstSize;  // Using int division truncation as a stand-in for the Math.floor function
			int numberOfBulletsFiredDuringThisBurst = numBulletsFiredSoFar % burstSize;
			int numBurstDelays = numberOfFullBurstsFired * (burstSize - 1);
			thisTick -= numBurstDelays * ticksBetweenBulletsDuringBurst + (numberOfFullBurstsFired - 1) * ticksBetweenBursts;
			
			if (numberOfBulletsFiredDuringThisBurst > 0) {
				thisTick -= ticksBetweenBursts + (numberOfBulletsFiredDuringThisBurst - 1) * ticksBetweenBulletsDuringBurst;
				return thisTick % ticksBetweenBulletsDuringBurst == 0;
			}
			else {
				return thisTick % ticksBetweenBursts == 0;
			}
		}
		else {
			return thisTick % ticksBetweenBursts == 0;
		}
	}
	
	private static double[] recoil2(double RoF, int magSize, int burstSize, double recoilPerShotPixels, int[] recoilUpFraction, int[] recoilDownFraction) {
		
		// Step 1: find the LCM of the three denominators and convert all fractions to use the common tickrate as their new denominator
		int[] RoFFraction = {10, (int) Math.round(RoF * 10.0)};  // Because some RoF have one decimal, multiply it by 10/10 to make it integers but maintain ratio
		int lcm = MathUtils.leastCommonMultiple(recoilUpFraction[1], recoilDownFraction[1]);
		lcm = MathUtils.leastCommonMultiple(lcm, RoFFraction[1]);
		
		int ticksBetweenBulletsDuringBurst;
		if (burstSize > 1) {
			// If this gun has burst-fire, then the difference between ticks needs to be compatible with the 20 RoF
			lcm = MathUtils.leastCommonMultiple(lcm, 20);
			ticksBetweenBulletsDuringBurst = lcm / 20;
		}
		else {
			ticksBetweenBulletsDuringBurst = 0;
		}
		int ticksBetweenBursts = RoFFraction[0] * lcm / RoFFraction[1];
		int ticksRecoilIncreasesAfterFiringBullet = recoilUpFraction[0] * lcm / recoilUpFraction[1];
		int ticksRecoilDecreasesAfterIncreasing = recoilDownFraction[0] * lcm / recoilDownFraction[1];
		
		// Step 2: calculate how many ticks it will take from the start of firing until the recoil has completely reset
		int totalTicksRequiredForSimulation = 0;
		if (burstSize > 1) {
			int numBursts = magSize / burstSize;
			totalTicksRequiredForSimulation = numBursts * (burstSize - 1) * ticksBetweenBulletsDuringBurst + (numBursts - 1) * ticksBetweenBursts + ticksRecoilIncreasesAfterFiringBullet + ticksRecoilDecreasesAfterIncreasing;
		}
		else {
			totalTicksRequiredForSimulation = (magSize - 1) * ticksBetweenBursts + ticksRecoilIncreasesAfterFiringBullet + ticksRecoilDecreasesAfterIncreasing;
		}
		
		// Step 3: model the entire period of time, tick-by-tick, and store the pixel distances in the array to return
		double increasingSlope = recoilPerShotPixels * recoilUpFraction[1] / (double) recoilUpFraction[0];  // pixels/sec
		double decreasingSlope = recoilPerShotPixels * recoilDownFraction[1] / (double) recoilDownFraction[0];  // pixels/sec
		double currentSlope = 0;
		double totalPixels = 0;
		int totalNumBulletsFired = 0;
		
		int a=0, b=0;
		int[] ticksBulletsWereFired = new int[magSize];
		int[] ticksBulletsStopIncreasing = new int[magSize];
		int[] ticksBulletsStopDecreasing = new int[magSize];
		
		double[] recoilAtTick = new double[totalTicksRequiredForSimulation];
		for (int tick = 0; tick < totalTicksRequiredForSimulation; tick++) {
			// Check if a bullet gets fired this tick
			if (totalNumBulletsFired < magSize && bulletFiredOnThisTick(tick, totalNumBulletsFired, burstSize, ticksBetweenBursts, ticksBetweenBulletsDuringBurst)) {
				ticksBulletsWereFired[totalNumBulletsFired] = tick;
				ticksBulletsStopIncreasing[totalNumBulletsFired] = tick + ticksRecoilIncreasesAfterFiringBullet;
				ticksBulletsStopDecreasing[totalNumBulletsFired] = tick + ticksRecoilIncreasesAfterFiringBullet + ticksRecoilDecreasesAfterIncreasing;
				totalNumBulletsFired++;
				currentSlope += increasingSlope;
			}
			
			// Check if a previously fired bullet changes from increase to decrease on this tick
			if (a < magSize && tick == ticksBulletsStopIncreasing[a]) {
				a++;
				currentSlope -= (increasingSlope + decreasingSlope);
			}
			
			// Check if a previously fired bullet has finished decreasing
			if (b < magSize && tick == ticksBulletsStopDecreasing[b]) {
				b++;
				currentSlope += decreasingSlope;
			}
			
			totalPixels = totalPixels + currentSlope / lcm;
			recoilAtTick[tick] = MathUtils.round(totalPixels, 4);
		}
		
		double[] toReturn = new double[magSize];
		// The first bullet is always fired when recoil == 0
		toReturn[0] = 0.0;
		for (int i = 1; i < magSize; i++) {
			// Get the value of recoil at the end of tick BEFORE this bullet gets fired because that would be the value of recoil WHEN the bullet gets fired
			toReturn[i] = recoilAtTick[ticksBulletsWereFired[i] - 1];
		}
		return toReturn;
	}
	
	public static double calculateAccuracy2(
		boolean weakpoint, double rateOfFire, double magSize, double burstSize,
		double unchangingBaseSpread, double changingBaseSpread, double spreadVariance, double spreadPerShot, double spreadRecoverySpeed,
		double recoilPerShot, int[] recoilIncreaseFraction, int[] recoilDecreaseFraction
	) {
		double RoF = rateOfFire;
		// The time that passes between each shot
		double deltaT = 1.0 / RoF;
		
		// Calculate all the base pixel values before converting to radians
		double baseSpread = unchangingBaseSpread + changingBaseSpread;
		double maxSpread = baseSpread + spreadVariance;
		
		// Convert from pixelage (specific per monitor/FoV combination) to radians of deviation from central axis (almost universal if the math is right)
		double Sb = convertSpreadPixelsToRads(baseSpread);
		double SpS = convertSpreadPixelsToRads(spreadPerShot);
		double Sm = convertSpreadPixelsToRads(maxSpread);
		double Sr = convertSpreadPixelsToRads(spreadRecoverySpeed);
		
		// I'm applying a -75% multiplier to all of these recoil values to factor in the player counter-acting the recoil.
		// Intentionally using 1 - PlayerCorrection so that I can change the player's efficiency directly, rather than having to do indirect math every time I want to change the value.
		double playerRecoilCorrectionCoefficient = (1.0 - 0.75);
		double RpS = recoilPerShot * playerRecoilCorrectionCoefficient;
		double[] predictedRecoil = recoil2(rateOfFire, (int) magSize, (int) burstSize, RpS, recoilIncreaseFraction, recoilDecreaseFraction);
		
		// Step 1: establish the target size
		// Due to mathematical limitations, I'm forced to model the targets as if they're circular even though it would be a better approximation if the targets were elliptical
		double targetRadius;
		if (weakpoint) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		double sumOfAllProbabilities = 0.0;
		double timeElapsed = 0.0;
		
		double crosshairRadius, crosshairRecoil, P; 
		for (int i = 0; i < magSize; i++) {
			// TODO: redo the radius() and recoil() methods for calculateAccuracy2
			// Step 2: calculate the crosshair size at the time the bullet gets fired
			crosshairRadius = convertRadiansToMeters(spread2(i, timeElapsed, Sb, SpS, Sr, Sm));
			
			// Step 3: calculate how far off-center the crosshair is due to recoil
			crosshairRecoil = convertRadiansToMeters(convertRecoilPixelsToRads(predictedRecoil[i]));
			
			// Step 4: calculate the area of overlap (if any) between the crosshair size, crosshair recoil, and target area
			// Step 5: divide the overlap by the target area for the probability that at the current bullet will hit
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
					P = areaOfLens(targetRadius, crosshairRadius, crosshairRecoil) / (Math.PI * Math.pow(targetRadius, 2));
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
					P = areaOfLens(crosshairRadius, targetRadius, crosshairRecoil) / (Math.PI * Math.pow(crosshairRadius, 2));
				}
			}
			
			// System.out.println("P for bullet # " + (i + 1) + ": " + P);
			sumOfAllProbabilities += P;
			
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				// If this gun both has a burst-fire mode and is currently firing a burst, change deltaT
				timeElapsed += 0.05;
			}
			else {
				// If this gun either doesn't have a burst-fire mode, or the burst has completed and it has to wait before it can fire the next burst
				timeElapsed += deltaT;
			}
		}
		
		// Step 6: redo steps 2 through 5 for each bullet in the magazine fired at max RoF, sum up the probabilities, and divide by magSize for an approximate estimation of Accuracy
		return sumOfAllProbabilities / magSize * 100.0;
	}
}
