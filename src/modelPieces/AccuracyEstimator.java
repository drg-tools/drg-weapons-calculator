package modelPieces;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import utilities.MathUtils;

public class AccuracyEstimator {
	// The distances from which this class estimates the accuracy of a gun
	private static double closeRangeTargetDistanceMeters = 5.0;
	private static double mediumRangeTargetDistanceMeters = 7.0;
	// The distance from which the measurements were taken
	private static double testingDistancePixels = 1074.047528;
	private static double playerRecoilCorrectionCoefficient = 1 - 0.625;
	
	private static double convertSpreadPixelsToRads(double px) {
		return Math.atan((px / (2 * testingDistancePixels)));
	}
	private static double convertRecoilPixelsToRads(double px) {
		return Math.atan(px / testingDistancePixels);
	}
	private static double convertRadiansToMeters(double r, boolean closeRange) {
		if (closeRange) {
			return closeRangeTargetDistanceMeters * Math.tan(r);
		}
		else {
			return mediumRangeTargetDistanceMeters * Math.tan(r);
		}
	}
	// This method also gets used in Gunner/Minigun's accuracy method
	public static double convertSpreadPixelsToMeters(double px, boolean closeRange) {
		if (closeRange) {
			return closeRangeTargetDistanceMeters * px /  (2 * testingDistancePixels);
		}
		else {
			return mediumRangeTargetDistanceMeters * px /  (2 * testingDistancePixels);
		}
	}
	
	private static double spread(int numBulletsFired, double timeElapsed, double baseSpreadRads, double spreadPerShotRads, double spreadRecoveryRads, double maxSpreadRads) {
		// This can never be less than 0 pixels of change
		double calculatedChangeInSpread = Math.max(numBulletsFired * spreadPerShotRads - timeElapsed * spreadRecoveryRads, 0);
		
		// It can also never be so high that it exceeds Max Spread
		double effectiveChangeInSpread = Math.min(calculatedChangeInSpread, maxSpreadRads - baseSpreadRads);
		
		return baseSpreadRads + effectiveChangeInSpread;
	}
	
	private enum inflectionType{increase, decrease, stop, IandD, IandS, DandS, allThree};
	
	// This method, used in conjunction with the enum variable inflectionType, effectively returns the Union of all three possibilities that could happen on an inflection point.
	private static inflectionType combineTwoInflectionTypes(inflectionType A, inflectionType B) {
		// Base case: one of they types is nothing; return the other.
		if (A == null) {
			return B;
		}
		if (B == null) {
			return A;
		}
		
		switch (A) {
			case increase: {
				if (B == inflectionType.decrease) {
					return inflectionType.IandD;
				}
				else if (B == inflectionType.stop) {
					return inflectionType.IandS;
				}
				else if (B == inflectionType.DandS) {
					return inflectionType.allThree;
				}
				else {
					// All other cases already have an Increase, so just return that type.
					return B;
				}
			}
			case decrease: {
				if (B == inflectionType.increase) {
					return inflectionType.IandD;
				}
				else if (B == inflectionType.stop) {
					return inflectionType.DandS;
				}
				else if (B == inflectionType.IandS) {
					return inflectionType.allThree;
				}
				else {
					return B;
				}
			}
			case stop: {
				if (B == inflectionType.increase) {
					return inflectionType.IandS;
				}
				else if (B == inflectionType.decrease) {
					return inflectionType.DandS;
				}
				else if (B == inflectionType.IandD) {
					return inflectionType.allThree;
				}
				else {
					return B;
				}
			}
			case IandD: {
				// Already increasing and decreasing; can only add stop
				if (B == inflectionType.stop) {
					return inflectionType.allThree;
				}
				else {
					return inflectionType.IandD;
				}
			}
			case IandS: {
				// Already increasing and stopping; can only add decrease
				if (B == inflectionType.decrease) {
					return inflectionType.allThree;
				}
				else {
					return inflectionType.IandS;
				}
			}
			case DandS: {
				// Already decreasing and stopping; can only add increase
				if (B == inflectionType.increase) {
					return inflectionType.allThree;
				}
				else {
					return inflectionType.DandS;
				}
			}
			case allThree: {
				// If there are already all three types, it can't be added to any more
				return inflectionType.allThree;
			}
			default: {
				return null;
			}
		}
	}
	
	private static double[] recoil(double RoF, int magSize, int burstSize, double recoilPerShotPixels, int[] rUp, int[] rDown) {
		double delta = 1.0 / RoF;
		double U = (double) rUp[0] / (double) rUp[1];
		double D = (double) rDown[0] / (double) rDown[1];
		
		// Each key will be the timestamp of the inflection point, and the value will be an enumerated variable that will say how the slope changes at that inflection point
		HashMap<Double, inflectionType> inflectionPoints = new HashMap<Double, inflectionType>();
		
		double[] bulletFiredTimestamps = new double[magSize];
		double currentTime = 0.0;
		double a, b, c;
		int i;
		for (i = 0; i < magSize; i++) {
			bulletFiredTimestamps[i] = currentTime;
			
			a = currentTime;
			if (inflectionPoints.containsKey(a)) {
				inflectionPoints.put(a, combineTwoInflectionTypes(inflectionPoints.get(a), inflectionType.increase));
			}
			else {
				inflectionPoints.put(a, inflectionType.increase);
			}
			
			b = currentTime + U;
			if (inflectionPoints.containsKey(b)) {
				inflectionPoints.put(b, combineTwoInflectionTypes(inflectionPoints.get(b), inflectionType.decrease));
			}
			else {
				inflectionPoints.put(b, inflectionType.decrease);
			}
			
			c = currentTime + U + D;
			if (inflectionPoints.containsKey(c)) {
				inflectionPoints.put(c, combineTwoInflectionTypes(inflectionPoints.get(c), inflectionType.stop));
			}
			else {
				inflectionPoints.put(c, inflectionType.stop);
			}
			
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				// During burst; add 1/20th second
				currentTime += 0.05;
			}
			else {
				// Either this gun doesn't have a burst mode, or it just fired the last bullet during a burst
				currentTime += delta;
			}
		}
		
		// In theory, the length of this array should be in the range [magSize + 2, 3 * magSize]
		Set<Double> unsortedTimestampKeys = inflectionPoints.keySet();
		Double[] inflectionPointTimestamps = unsortedTimestampKeys.toArray(new Double[unsortedTimestampKeys.size()]);
		Arrays.sort(inflectionPointTimestamps);
		
		// Now that we should have an array of all the timestamps of the inflection points, it can be converted into an array of slope changes
		double[] slopeAtT = new double[inflectionPointTimestamps.length];
		double increaseSlope = recoilPerShotPixels / U;
		double decreaseSlope = recoilPerShotPixels / D;
		inflectionType infType;
		double currentSlope = 0.0;
		for (i = 0; i < inflectionPointTimestamps.length; i++) {
			infType = inflectionPoints.get(inflectionPointTimestamps[i]);
			
			switch (infType) {
				case increase: {
					// A bullet was just fired; add the increasing delta-slope
					currentSlope += increaseSlope;
					break;
				}
				case decrease: {
					// A bullet just changed from increasing to decreasing; subtract both delta-slopes from the current slope
					currentSlope -= (increaseSlope + decreaseSlope);
					break;
				}
				case stop: {
					// A bullet just finished its recoil cycle; remove the decreasing delta-slope.
					currentSlope += decreaseSlope;
					break;
				}
				case IandD: {
					// Two bullets are changing at the same time, one increasing and the other decreasing
					currentSlope -= decreaseSlope;
					break;
				}
				case IandS: {
					// Two bullets are changing at the same time, one increasing and the other decreasing
					currentSlope += increaseSlope + decreaseSlope;
					break;
				}
				case DandS: {
					// Two bullets are changing at the same time, one increasing and the other decreasing
					currentSlope -= increaseSlope;
					break;
				}
				case allThree: {
					// Three bullets are changing at the same time, but because of how the math works out this is a net change of 0 to the slope
					currentSlope += 0.0;
					break;
				}
				default: {
					// Theoretically this should never happen, but I'm adding it anyway just for sanity check.
					// I'm having this be a HUGE change to the slope so that it will negatively affect the model in a substantial way; big enough to be noticeable from the GUI's accuracy of 2% or something...
					currentSlope *= -10.0;
					break;
				}
			}
			
			slopeAtT[i] = currentSlope;
		}
		
		// With the array of slope changes and their corresponding timestamps, it should be possible to accurately recreate what the recoil will look like over time.
		double[] recoilAtEachShot = new double[magSize];
		double currentRecoilPixels = 0.0;
		recoilAtEachShot[0] = currentRecoilPixels;
		currentTime = 0.0;
		double deltaTime;
		double timeInterval;
		int inflectionPointIndex = 0;
		for (i = 1; i < magSize; i++) {
			deltaTime = bulletFiredTimestamps[i] - currentTime;
			
			if (currentTime + deltaTime > inflectionPointTimestamps[inflectionPointIndex]) {
				/*
					If/when we hit this case, it means that AT LEAST one inflection point passed between when the last bullet fired and when this bullet is about to fire. 
					We need to add to currentRecoilPixels as the inflection points pass and the slope changes until the next inflection point is after "now" 
				*/
				
				// Start by doing the partial interval to get to the current inflection point
				timeInterval = inflectionPointTimestamps[inflectionPointIndex] - currentTime;
				currentRecoilPixels += timeInterval * slopeAtT[inflectionPointIndex];
				
				// Iterate inflection point by inflection point until the next one will be after currentTime + deltaTime
				while (currentTime + deltaTime > inflectionPointTimestamps[inflectionPointIndex + 1]) {
					timeInterval = inflectionPointTimestamps[inflectionPointIndex + 1] - inflectionPointTimestamps[inflectionPointIndex];
					currentRecoilPixels += timeInterval * slopeAtT[inflectionPointIndex];
					
					// Because inflectionPointTimestamps is just the keys of that Hash, it should always have AT LEAST 2 more values than magSize, so it's ok to blindly increment it without having to worry about exceeding array bounds.
					inflectionPointIndex++;
				}
				
				// Finally, do a second partial interval so that the currentRecoilPixels is modeled fully for the deltaTime period
				timeInterval = (currentTime + deltaTime) - inflectionPointTimestamps[inflectionPointIndex];
				currentRecoilPixels += timeInterval * slopeAtT[inflectionPointIndex];
			}
			else {
				currentRecoilPixels += deltaTime * slopeAtT[inflectionPointIndex];
			}
			currentTime += deltaTime;
			recoilAtEachShot[i] = currentRecoilPixels;
		}
		
		return recoilAtEachShot;
	}
	
	private static double areaOfLens(double R, double r, double d) {
		// Sourced from https://en.wikipedia.org/wiki/Lens_(geometry)
		double firstThird = Math.pow(r, 2) * Math.acos((Math.pow(d, 2) + Math.pow(r, 2) - Math.pow(R, 2)) / (2 * d * r));
		double secondThird = Math.pow(R, 2) * Math.acos((Math.pow(d, 2) + Math.pow(R, 2) - Math.pow(r, 2)) / (2 * d * R));
		double finalThird = 0.5 * Math.sqrt((-d + r + R) * (d - r + R) * (d + r - R) * (d + r + R));
		
		return firstThird + secondThird - finalThird;
	}
	
	public static double calculateCircularAccuracy(
		boolean weakpoint, boolean closeRange, double rateOfFire, double magSize, double burstSize,
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
		
		double RpS = recoilPerShot * playerRecoilCorrectionCoefficient;
		
		double[] predictedRecoil = recoil(rateOfFire, (int) magSize, (int) burstSize, RpS, recoilIncreaseFraction, recoilDecreaseFraction);
		
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
			// Step 2: calculate the crosshair size at the time the bullet gets fired
			crosshairRadius = convertRadiansToMeters(spread(i, timeElapsed, Sb, SpS, Sr, Sm), closeRange);
			
			// Step 3: calculate how far off-center the crosshair is due to recoil
			crosshairRecoil = convertRadiansToMeters(convertRecoilPixelsToRads(predictedRecoil[i]), closeRange);
			
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
	
	public static double calculateRectangularAccuracy(boolean weakpoint, boolean closeRange, double crosshairWidthPixels, double crosshairHeightPixels) {
		double crosshairHeightMeters = AccuracyEstimator.convertSpreadPixelsToMeters(crosshairHeightPixels, closeRange);
		double crosshairWidthMeters = AccuracyEstimator.convertSpreadPixelsToMeters(crosshairWidthPixels, closeRange);
		double targetRadius;
		if (weakpoint) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		/*
			From observation, it looks like the horizontal distribution of bullets followed a bell curve such that the highest probabilities were in the center of the rectangle, 
			and the lower probabilities were near the edges. To model that, I'm choosing to calculate the sum of the probabilities that the horizontal spread will be within 
			the target radius as well as the probability of vertical spread being within the target radius, and then taking the area of the "probability ellipse" formed by those two numbers.
		*/
		// Convert the target radius in meters to the unit-less probability ellipse
		double endOfProbabilityCurve = 2.0 * Math.sqrt(2.0);
		double horizontalProbabilityRatio = endOfProbabilityCurve * targetRadius / crosshairWidthMeters;
		double hProb = MathUtils.areaUnderNormalDistribution(-1.0 * horizontalProbabilityRatio, horizontalProbabilityRatio);
		double verticalProbabilityRatio = endOfProbabilityCurve * targetRadius / crosshairHeightMeters;
		double vProb = MathUtils.areaUnderNormalDistribution(-1.0 * verticalProbabilityRatio, verticalProbabilityRatio);
		
		double areaOfProbabilityEllipse = Math.PI * hProb * vProb / 4.0;
		
		return areaOfProbabilityEllipse * 100.0;
	}
}
