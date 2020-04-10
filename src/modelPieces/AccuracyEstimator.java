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
	private static double playerRecoilCorrectionCoefficient = 0.625;
	
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
	
	// This method returns an array of what the change in pixels from Base Spread will be at the moment each bullet gets fired (will need to be converted from Spread Pixels to rads to meters)
	private static double[] spread(double RoF, int magSize, int burstSize, double baseSpreadPixels, double spreadPerShotPixels, double spreadRecoverySpeedPixels, double maxSpread) {
		double delta = 1.0 / RoF;
		double timeToRecoverFromOneShot = spreadPerShotPixels / spreadRecoverySpeedPixels;
		
		// Each key will be the timestamp of the inflection point, and the value will be an enumerated variable that will say how the slope changes at that inflection point
		HashMap<Double, inflectionType> inflectionPoints = new HashMap<Double, inflectionType>();
		
		double[] bulletFiredTimestamps = new double[magSize];
		double currentTime = 0.0;
		double a, b;
		int i;
		for (i = 0; i < magSize; i++) {
			bulletFiredTimestamps[i] = currentTime;
			
			a = currentTime;
			if (inflectionPoints.containsKey(a)) {
				inflectionPoints.put(a, combineTwoInflectionTypes(inflectionPoints.get(a), inflectionType.decrease));
			}
			else {
				inflectionPoints.put(a, inflectionType.decrease);
			}
			
			b = currentTime + timeToRecoverFromOneShot;
			if (inflectionPoints.containsKey(b)) {
				inflectionPoints.put(b, combineTwoInflectionTypes(inflectionPoints.get(b), inflectionType.stop));
			}
			else {
				inflectionPoints.put(b, inflectionType.stop);
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
		
		// In theory, the length of this array should be in the range [magSize + 1, 2 * magSize]
		Set<Double> unsortedTimestampKeys = inflectionPoints.keySet();
		Double[] inflectionPointTimestamps = unsortedTimestampKeys.toArray(new Double[unsortedTimestampKeys.size()]);
		Arrays.sort(inflectionPointTimestamps);
		
		// Now that we should have an array of all the timestamps of the inflection points, it can be converted into an array of slope changes (equivalent to integrating from the 2nd derivative to the 1st derivative)
		double[] slopeAtT = new double[inflectionPointTimestamps.length];
		inflectionType infType;
		double currentSlope = 0.0;
		for (i = 0; i < inflectionPointTimestamps.length; i++) {
			infType = inflectionPoints.get(inflectionPointTimestamps[i]);
			
			switch (infType) {
				case decrease: {
					// A bullet was just fired, so its Spread per Shot was just applied. Begin decreasing the total Spread by decreasing the slope.
					currentSlope -= spreadRecoverySpeedPixels;
					break;
				}
				case stop: {
					// A bullet that was fired just had its Spread per Shot fully recovered by this point. Stop decreasing the total Spread for this particular bullet by increasing the slope.
					currentSlope += spreadRecoverySpeedPixels;
					break;
				}
				case DandS: {
					// One bullet was fired at the same time that a previous bullet finished its spread recovery period. The net change to the slope is 0.
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
			
			// Because Spread Recovery starts instantly, the slope should always be <= 0.
			slopeAtT[i] = currentSlope;
		}
		
		// With the array of slope changes and their corresponding timestamps, it should be possible to accurately recreate what the Spread will look like over time. (equivalent to integrating from the 1st derivative to the function itself)
		double[] spreadAtEachShot = new double[magSize];
		double currentSpreadPixels = baseSpreadPixels;
		spreadAtEachShot[0] = currentSpreadPixels;
		// Add the Spread per Shot from the first shot
		// TODO: If this does have a Max Spread value, this is one of the spots where it would be implemented
		currentSpreadPixels += spreadPerShotPixels;
		
		currentTime = 0.0;
		double deltaTime;
		double timeInterval;
		int inflectionPointIndex = 0;
		for (i = 1; i < magSize; i++) {
			// Start by reducing Spread based on the time elapsed since the last bullet was fired
			deltaTime = bulletFiredTimestamps[i] - currentTime;
			
			if (currentTime + deltaTime > inflectionPointTimestamps[inflectionPointIndex]) {
				/*
					If/when we hit this case, it means that AT LEAST one inflection point passed between when the last bullet fired and when this bullet is about to fire. 
					We need to add to currentSpreadPixels as the inflection points pass and the slope changes until the next inflection point is after "now" 
				*/
				
				// Start by doing the partial interval to get to the current inflection point
				timeInterval = inflectionPointTimestamps[inflectionPointIndex] - currentTime;
				// TODO: If this does have a Max Spread value, this is one of the spots where it would be implemented
				currentSpreadPixels += timeInterval * slopeAtT[inflectionPointIndex];
				
				// Iterate inflection point by inflection point until the next one will be after currentTime + deltaTime
				while (currentTime + deltaTime > inflectionPointTimestamps[inflectionPointIndex + 1]) {
					timeInterval = inflectionPointTimestamps[inflectionPointIndex + 1] - inflectionPointTimestamps[inflectionPointIndex];
					// TODO: If this does have a Max Spread value, this is one of the spots where it would be implemented
					currentSpreadPixels += timeInterval * slopeAtT[inflectionPointIndex];
					
					// Because inflectionPointTimestamps is just the keys of that Hash, it should always have AT LEAST 1 more value than magSize, so it's ok to blindly increment it without having to worry about exceeding array bounds.
					inflectionPointIndex++;
				}
				
				// Finally, do a second partial interval so that the currentSpreadPixels is modeled fully for the deltaTime period
				timeInterval = (currentTime + deltaTime) - inflectionPointTimestamps[inflectionPointIndex];
				// TODO: If this does have a Max Spread value, this is one of the spots where it would be implemented
				currentSpreadPixels += timeInterval * slopeAtT[inflectionPointIndex];
			}
			else {
				currentSpreadPixels += deltaTime * slopeAtT[inflectionPointIndex];
			}
			currentTime += deltaTime;
			
			spreadAtEachShot[i] = currentSpreadPixels;
			
			// Now that some Spread has been reduced to what it would be when the bullet gets fired, add the Spread per Shot of the current bullet for the next loop.
			// TODO: If this does have a Max Spread value, this is where it would be implemented
			currentSpreadPixels += spreadPerShotPixels;
		}
		
		return spreadAtEachShot;
	}
	
	// This method returns an array of what the radians of deviation from the center of the target will be at the moment each bullet gets fired (will need to be converted from rads to meters)
	private static double[] recoil(double RoF, int magSize, int burstSize, double recoilPerShotRads, double rUp, double rDown) {
		double delta = 1.0 / RoF;
		
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
			
			b = currentTime + rUp;
			if (inflectionPoints.containsKey(b)) {
				inflectionPoints.put(b, combineTwoInflectionTypes(inflectionPoints.get(b), inflectionType.decrease));
			}
			else {
				inflectionPoints.put(b, inflectionType.decrease);
			}
			
			c = currentTime + rUp + rDown;
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
		
		// Now that we should have an array of all the timestamps of the inflection points, it can be converted into an array of slope changes (equivalent to integrating from the 2nd derivative to the 1st derivative)
		double[] slopeAtT = new double[inflectionPointTimestamps.length];
		double increaseSlope = recoilPerShotRads / rUp;
		double decreaseSlope = recoilPerShotRads / rDown;
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
					// Two bullets are changing at the same time, one increasing and the other stopping
					currentSlope += increaseSlope + decreaseSlope;
					break;
				}
				case DandS: {
					// Two bullets are changing at the same time, one decreasing and the other stopping
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
		
		// With the array of slope changes and their corresponding timestamps, it should be possible to accurately recreate what the recoil will look like over time. (equivalent to integrating from the 1st derivative to the function itself)
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
		boolean weakpoint, boolean closeRange, double rateOfFire, int magSize, int burstSize,
		double unchangingBaseSpread, double changingBaseSpread, double spreadVariance, double spreadPerShot, double spreadRecoverySpeed,
		double recoilPerShot, double recoilIncreaseInterval, double recoilDecreaseInterval,
		double[] accuracyModifiers
	) {
		/*
			accuracyModifiers should be an array of decimals in this order:
			
				Base Spread
				Spread per Shot
				Spread Recovery Speed
				Max Spread / Spread Variance?
				Recoil per Shot
		*/
		double Sb = unchangingBaseSpread + changingBaseSpread * accuracyModifiers[0];
		double SpS = spreadPerShot * accuracyModifiers[1];
		double Sm = Sb + spreadVariance * accuracyModifiers[2];
		double Sr = spreadRecoverySpeed * accuracyModifiers[3];
		double RpS = convertRecoilPixelsToRads(recoilPerShot) * accuracyModifiers[4] * (1 - playerRecoilCorrectionCoefficient);
		
		// predictedSpread is an array of the pixel values of the width of the crosshair when each bullet gets fired
		double[] predictedSpread = spread(rateOfFire, magSize, burstSize, Sb, SpS, Sr, Sm);
		// predictedRecoil is an array of the radian values of how far off-center the crosshair is when each bullet gets fired
		double[] predictedRecoil = recoil(rateOfFire,  magSize, burstSize, RpS, recoilIncreaseInterval, recoilDecreaseInterval);
		
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
		double crosshairRadius, crosshairRecoil, P; 
		for (int i = 0; i < magSize; i++) {
			// Step 2: calculate the crosshair size at the time the bullet gets fired
			crosshairRadius = convertSpreadPixelsToMeters(predictedSpread[i], closeRange);
			
			// Step 3: calculate how far off-center the crosshair is due to recoil
			crosshairRecoil = convertRadiansToMeters(predictedRecoil[i], closeRange);
			
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
