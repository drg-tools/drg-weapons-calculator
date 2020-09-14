package modelPieces;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import guiPieces.AccuracyAnimation;
import guiPieces.GuiConstants;
import guiPieces.LineGraph;
import spreadCurves.SpreadCurve;
import utilities.MathUtils;

public class AccuracyEstimator {
	// The distance from which the measurements were taken
	private final double testingDistancePixels = 1074.047528;
	
	private double delayBeforePlayerReaction;
	private double playerRecoilRecoveryPerSecond;
	
	private double targetDistanceMeters;
	private boolean modelRecoil;
	private boolean visualizeGeneralAccuracy;
	
	private SpreadCurve spreadTransformer;
	
	private double[] bulletFiredTimestamps;
	private HashMap<Double, Double> spreadOverTime;
	private HashMap<Double, Double> recoilOverTime;
	private HashMap<Double, Double> reducedRecoilOverTime;
	
	public AccuracyEstimator() {
		// With these two values, recoil should be reduced to 0% in exactly 0.5 seconds.
		delayBeforePlayerReaction = 0.15;  // seconds
		playerRecoilRecoveryPerSecond = 1.00/0.35;  // Percentage of max recoil that the player recovers per second
		
		// Start at 7m distance for all weapons in AccuracyEstimator, but let it be overwritten by shotgun classes.
		targetDistanceMeters = 7.0;
		visualizeGeneralAccuracy = true;
		modelRecoil = true;
		
		spreadTransformer = null;
		
		// Setting these all as length 0 arrays so that I can use length > 0 checks even if they never get values added to them.
		// spreadOverTime should have length (2*MagSize + 1)
		spreadOverTime = new HashMap<Double, Double>();
		// recoilOverTime should have length between [MagSize + 2, 3*MagSize]
		recoilOverTime = new HashMap<Double, Double>();
		reducedRecoilOverTime = new HashMap<Double, Double>();
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
	
	public void makeVisualizerShowGeneralAccuracy(boolean value) {
		visualizeGeneralAccuracy = value;
	}
	public boolean visualizerShowsGeneralAccuracy() {
		return visualizeGeneralAccuracy;
	}
	
	public void setSpreadCurve(SpreadCurve sc) {
		spreadTransformer = sc;
	}
	
	// Other methods
	private double convertRecoilPixelsToRads(double px) {
		return Math.atan(px / testingDistancePixels);
	}
	private double convertRadiansToMeters(double rads) {
		return targetDistanceMeters * Math.tan(rads);
	}
	private double convertSpreadPixelsToMeters(double px) {
		return targetDistanceMeters * px /  (2 * testingDistancePixels);
	}
	
	private enum inflectionType{increase, decrease, stop, IandD, IandS, DandS, allThree};
	
	// This method, used in conjunction with the enum variable inflectionType, effectively returns the Union of all three possibilities that could happen on an inflection point.
	private inflectionType combineTwoInflectionTypes(inflectionType A, inflectionType B) {
		// Base case: one of the types is nothing; return the other.
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
	
	// This method returns an array of what the crosshair width will be at the moment each bullet gets fired (will need to be converted from Spread Pixels to meters)
	private double[] spread(double RoF, int magSize, int burstSize, double baseSpreadPixels, double spreadPerShotPixels, double spreadRecoverySpeedPixels, double maxSpreadPixels, boolean invertedSpread) {
		spreadOverTime = new HashMap<Double, Double>();
		double timeElapsed = 0.0;
		
		double[] spreadAtEachShot = new double[magSize];
		double currentSpreadPixels = baseSpreadPixels;
		spreadAtEachShot[0] = currentSpreadPixels;
		spreadOverTime.put(timeElapsed, currentSpreadPixels);
		
		// Add the Spread per Shot for the first shot
		if (invertedSpread) {
			currentSpreadPixels = Math.max(currentSpreadPixels + spreadPerShotPixels, maxSpreadPixels);
		}
		else {
			currentSpreadPixels = Math.min(currentSpreadPixels + spreadPerShotPixels, maxSpreadPixels);  // This value can never go above Max Spread
		}
		spreadOverTime.put(timeElapsed + 0.001, currentSpreadPixels);
		
		double timeBetweenBursts = 1 / RoF;
		double deltaTime;
		for (int i = 1; i < magSize; i++) {
			// Start by decreasing spread based on how much time has passed since the last bullet was fired
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				// During burst; 1/20th second
				deltaTime = 0.05;
			}
			else {
				// Either this gun doesn't have a burst mode, or it just fired the last bullet during a burst
				deltaTime = timeBetweenBursts;
			}
			timeElapsed += deltaTime;
			
			if (invertedSpread) {
				// Special case: the way Minigun's accuracy works, the SRS stops acting on the crosshair when it's at full accuracy.
				if (currentSpreadPixels > maxSpreadPixels) {
					currentSpreadPixels = Math.min(currentSpreadPixels - deltaTime * spreadRecoverySpeedPixels, baseSpreadPixels);
				}
			}
			else {
				currentSpreadPixels = Math.max(currentSpreadPixels - deltaTime * spreadRecoverySpeedPixels, baseSpreadPixels);  // This value can never go below Base Spread
			}
			
			// Mark what the current spread is when this bullet gets fired
			spreadAtEachShot[i] = currentSpreadPixels;
			spreadOverTime.put(timeElapsed, currentSpreadPixels);
			
			// Add the Spread per Shot from this bullet for the next loop
			if (invertedSpread) {
				currentSpreadPixels = Math.max(currentSpreadPixels + spreadPerShotPixels, maxSpreadPixels);
			}
			else {
				currentSpreadPixels = Math.min(currentSpreadPixels + spreadPerShotPixels, maxSpreadPixels);  // This value can never go above Max Spread
			}
			
			// Add a point for the crosshair width after the bullet is fired, too
			spreadOverTime.put(timeElapsed + 0.001, currentSpreadPixels);
		}
		
		// Add a point at the end for how long it takes SRS to bring crosshair back to minimum size
		double timeToResetSpread;
		if (invertedSpread) {
			timeToResetSpread = (baseSpreadPixels - currentSpreadPixels) / spreadRecoverySpeedPixels;
		}
		else {
			// Special case: Engineer/Shotgun has no Spread changes, so its SRS is 0, and as a result it can't be used in a denominator.
			if (spreadRecoverySpeedPixels > 0) {
				timeToResetSpread = (currentSpreadPixels - baseSpreadPixels) / spreadRecoverySpeedPixels;
			}
			else {
				timeToResetSpread = 0;
			}
		}
		spreadOverTime.put(timeElapsed + timeToResetSpread, baseSpreadPixels);
		
		return spreadAtEachShot;
	}
	
	// This method returns an array of what the radians of deviation from the center of the target will be at the moment each bullet gets fired (will need to be converted from rads to meters)
	private double[] recoil(double RoF, int magSize, int burstSize, double recoilPerShotRads, double rUp, double rDown) {
		double delta = 1.0 / RoF;
		
		// Each key will be the timestamp of the inflection point, and the value will be an enumerated variable that will say how the slope changes at that inflection point
		HashMap<Double, inflectionType> inflectionPoints = new HashMap<Double, inflectionType>();
		
		bulletFiredTimestamps = new double[magSize];
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
		
		// In theory, the length of this array should be in the range [MagSize + 2, 3*MagSize]
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
		
		saveRecoilForVisualizer(inflectionPointTimestamps, slopeAtT, RoF, magSize, burstSize);
		
		// With the array of slope changes and their corresponding timestamps, it should be possible to accurately recreate what the recoil will look like over time before player counter-action. (equivalent to integrating from the 1st derivative to the function itself)
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
		
		// Finally, reduce the predicted recoil as if the player was pulling the mouse downwards to compensate for the ever-increasing recoil.
		// I'm choosing to model it such that if the RoF <= 2, each shot has recoil but the player can account for it all in the 0.5+ sec between shots (functionally no recoil for slow RoF)
		double timeSpentReducingRecoil, totalReduction;
		for (i = 1; i < magSize; i++) {
			if (RoF > 2) {
				if (bulletFiredTimestamps[i] > delayBeforePlayerReaction) {
					timeSpentReducingRecoil = bulletFiredTimestamps[i] - delayBeforePlayerReaction;
					totalReduction = Math.max(1.0 - timeSpentReducingRecoil * playerRecoilRecoveryPerSecond, 0);
					recoilAtEachShot[i] = recoilAtEachShot[i] * totalReduction;
				}
			}
			else {
				// I could write a very long and complicated chunk of code to walk through the logic of this, but it's functionally equivalent to just use the first burst's recoil values for all bursts in the magazine
				recoilAtEachShot[i] = recoilAtEachShot[i % burstSize];
			}
		}
		
		return recoilAtEachShot;
	}
	
	private double areaOfLens(double R, double r, double d) {
		// Sourced from https://en.wikipedia.org/wiki/Lens_(geometry)
		double firstThird = Math.pow(r, 2) * Math.acos((Math.pow(d, 2) + Math.pow(r, 2) - Math.pow(R, 2)) / (2 * d * r));
		double secondThird = Math.pow(R, 2) * Math.acos((Math.pow(d, 2) + Math.pow(R, 2) - Math.pow(r, 2)) / (2 * d * R));
		double finalThird = 0.5 * Math.sqrt((-d + r + R) * (d - r + R) * (d + r - R) * (d + r + R));
		
		return firstThird + secondThird - finalThird;
	}
	
	public double calculateCircularAccuracy(
		boolean weakpoint, double rateOfFire, int magSize, int burstSize,
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
		double Sr = spreadRecoverySpeed * accuracyModifiers[2];
		double Sm = Sb + spreadVariance * accuracyModifiers[3];
		double RpS = convertRecoilPixelsToRads(recoilPerShot) * accuracyModifiers[4];
		
		// predictedSpread is an array of the pixel values of the width of the crosshair when each bullet gets fired
		double[] predictedSpread = spread(rateOfFire, magSize, burstSize, Sb, SpS, Sr, Sm, SpS < 0);
		// predictedRecoil is an array of the radian values of how far off-center the crosshair is when each bullet gets fired
		double[] predictedRecoil;
		if (modelRecoil) {
			predictedRecoil = recoil(rateOfFire,  magSize, burstSize, RpS, recoilIncreaseInterval, recoilDecreaseInterval);
		}
		else {
			predictedRecoil = recoil(rateOfFire,  magSize, burstSize, 0.0, recoilIncreaseInterval, recoilDecreaseInterval);
		}
		
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
			crosshairRadius = convertSpreadPixelsToMeters(predictedSpread[i]);
			
			// Step 3: calculate how far off-center the crosshair is due to recoil
			crosshairRecoil = convertRadiansToMeters(predictedRecoil[i]);
			
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
	
	public double calculateRectangularAccuracy(boolean weakpoint, double crosshairWidthPixels, double crosshairHeightPixels) {
		double crosshairHeightMeters = convertSpreadPixelsToMeters(crosshairHeightPixels);
		double crosshairWidthMeters = convertSpreadPixelsToMeters(crosshairWidthPixels);
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
	
	public boolean visualizerIsReady() {
		return spreadOverTime.size() > 0;
	}
	
	private void saveRecoilForVisualizer(Double[] inflectionPointTimestamps, double[] slopeAtT, double RoF, int magSize, int burstSize) {
		recoilOverTime = new HashMap<Double, Double>();
		reducedRecoilOverTime = new HashMap<Double, Double>();
		
		// It always starts at zero recoil when t=0
		recoilOverTime.put(0.0, 0.0);
		reducedRecoilOverTime.put(0.0, 0.0);
		
		double timeBetweenInflectionPoints;
		double currentRecoilValue = 0.0;
		double timeElapsed = 0.0;
		double timeSpentReducingRecoil, totalReduction;
		
		// Intentionally starting at 1 so that I can use (current - old) for time duration
		for (int i = 1; i < inflectionPointTimestamps.length; i++) {
			timeBetweenInflectionPoints = inflectionPointTimestamps[i] - inflectionPointTimestamps[i - 1];
			timeElapsed += timeBetweenInflectionPoints;
			
			currentRecoilValue += slopeAtT[i - 1] * timeBetweenInflectionPoints;
			recoilOverTime.put(inflectionPointTimestamps[i], currentRecoilValue);
			
			// I'm not satisfied with how this displays for RoF <= 2. I want each burst to have the same recoil as if it was the first burst fired, but after 8 attempts I couldn't get it to work.
			// The actual values used are correctly imitating first burst, but I can't figure out a good way to visualize it...
			if (timeElapsed > delayBeforePlayerReaction) {
				timeSpentReducingRecoil = timeElapsed - delayBeforePlayerReaction;
				totalReduction = Math.max(1.0 - timeSpentReducingRecoil * playerRecoilRecoveryPerSecond, 0);
				reducedRecoilOverTime.put(inflectionPointTimestamps[i], currentRecoilValue * totalReduction);
			}
			else {
				reducedRecoilOverTime.put(inflectionPointTimestamps[i], currentRecoilValue);
			}
		}
	}
	
	public JPanel getVisualizer() {
		JPanel toReturn = new JPanel();
		
		// Part 1: figuring out stuff before rendering
		Set<Double> unsortedTimestampKeys = spreadOverTime.keySet();
		Double[] spreadOverTimeTimestamps = unsortedTimestampKeys.toArray(new Double[unsortedTimestampKeys.size()]);
		Arrays.sort(spreadOverTimeTimestamps);
		
		unsortedTimestampKeys = recoilOverTime.keySet();
		Double[] recoilOverTimeTimestamps = unsortedTimestampKeys.toArray(new Double[unsortedTimestampKeys.size()]);
		Arrays.sort(recoilOverTimeTimestamps);
		
		// Both of these arrays have the last timestamp at the end
		double loopDuration = Math.max(spreadOverTimeTimestamps[spreadOverTimeTimestamps.length - 1], recoilOverTimeTimestamps[recoilOverTimeTimestamps.length - 1]);
		
		// In addition to finding the biggest values in each group, these for loops will be used to duplicate the spread and playerRecoil arrays but sized into meters at distance
		// for the animation to pull from
		HashMap<Double, Double> spreadMeters = new HashMap<Double, Double>();
		HashMap<Double, Double> rawRecoilMeters = new HashMap<Double, Double>();
		HashMap<Double, Double> reducedRecoilMeters = new HashMap<Double, Double>();
		int i;
		double currentTimestamp, currentValue;
		double maxSpread = 0.0;
		double minSpread = 10000.0;
		for (i = 0; i < spreadOverTimeTimestamps.length; i++) {
			currentTimestamp = spreadOverTimeTimestamps[i];
			currentValue = spreadOverTime.get(currentTimestamp);
			if (currentValue > maxSpread) {
				maxSpread = currentValue;
			}
			
			if (currentValue < minSpread) {
				minSpread = currentValue;
			}
			
			spreadMeters.put(currentTimestamp, convertSpreadPixelsToMeters(currentValue));
		}
		
		double maxRawRecoil = 0.0;
		double maxReducedRecoil = 0.0;
		for (i = 0; i < recoilOverTimeTimestamps.length; i++) {
			currentTimestamp = recoilOverTimeTimestamps[i];
			currentValue = recoilOverTime.get(currentTimestamp);
			if (currentValue > maxRawRecoil) {
				maxRawRecoil = currentValue;
			}
			
			rawRecoilMeters.put(currentTimestamp, convertRadiansToMeters(currentValue));
			
			currentValue = reducedRecoilOverTime.get(currentTimestamp);
			if (currentValue > maxReducedRecoil) {
				maxReducedRecoil = currentValue;
			}
			
			reducedRecoilMeters.put(currentTimestamp, convertRadiansToMeters(currentValue));
		}
		
		JPanel lineGraphsPanel = new JPanel();
		lineGraphsPanel.setLayout(new BoxLayout(lineGraphsPanel, BoxLayout.PAGE_AXIS));
		
		LineGraph spreadGraph = new LineGraph(spreadOverTimeTimestamps, spreadOverTime, loopDuration, Math.max(maxSpread, 150));
		new Thread(spreadGraph).start();
		JPanel spreadGraphAndLabel = new JPanel();
		spreadGraphAndLabel.setLayout(new BoxLayout(spreadGraphAndLabel, BoxLayout.PAGE_AXIS));
		spreadGraphAndLabel.add(new JLabel("Crosshair radius (pixels) vs Time (seconds)"));
		spreadGraphAndLabel.add(spreadGraph);
		spreadGraphAndLabel.setBorder(GuiConstants.blackLine);
		lineGraphsPanel.add(spreadGraphAndLabel);
		
		LineGraph rawRecoilGraph = new LineGraph(recoilOverTimeTimestamps, recoilOverTime, loopDuration, Math.max(maxRawRecoil, 0.3));
		new Thread(rawRecoilGraph).start();
		JPanel rawRecoilGraphAndLabel = new JPanel();
		rawRecoilGraphAndLabel.setLayout(new BoxLayout(rawRecoilGraphAndLabel, BoxLayout.PAGE_AXIS));
		rawRecoilGraphAndLabel.add(new JLabel("Recoil offset (radians) vs Time (seconds)"));
		rawRecoilGraphAndLabel.add(rawRecoilGraph);
		rawRecoilGraphAndLabel.setBorder(GuiConstants.blackLine);
		lineGraphsPanel.add(rawRecoilGraphAndLabel);
		
		LineGraph playerReducedRecoilGraph = new LineGraph(recoilOverTimeTimestamps, reducedRecoilOverTime, loopDuration, Math.max(maxRawRecoil, 0.3));
		new Thread(playerReducedRecoilGraph).start();
		JPanel reducedRecoilAndGraph = new JPanel();
		reducedRecoilAndGraph.setLayout(new BoxLayout(reducedRecoilAndGraph, BoxLayout.PAGE_AXIS));
		reducedRecoilAndGraph.add(new JLabel("Player-reduced recoil offset (radians) vs Time (seconds)"));
		reducedRecoilAndGraph.add(playerReducedRecoilGraph);
		reducedRecoilAndGraph.setBorder(GuiConstants.blackLine);
		lineGraphsPanel.add(reducedRecoilAndGraph);
		
		AccuracyAnimation rawRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration, 
				spreadOverTimeTimestamps, spreadMeters, convertSpreadPixelsToMeters(minSpread), convertSpreadPixelsToMeters(maxSpread), 
				recoilOverTimeTimestamps, rawRecoilMeters, convertRadiansToMeters(maxReducedRecoil));
		rawRecoilGif.setBorder(GuiConstants.blackLine);
		new Thread(rawRecoilGif).start();
		
		AccuracyAnimation reducedRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration, 
				spreadOverTimeTimestamps, spreadMeters, convertSpreadPixelsToMeters(minSpread), convertSpreadPixelsToMeters(maxSpread), 
				recoilOverTimeTimestamps, reducedRecoilMeters, convertRadiansToMeters(maxReducedRecoil));
		reducedRecoilGif.setBorder(GuiConstants.blackLine);
		new Thread(reducedRecoilGif).start();
		
		toReturn.add(lineGraphsPanel);
		toReturn.add(rawRecoilGif);
		toReturn.add(reducedRecoilGif);
		
		/*
			If I ever want to re-add the Spread Curve transformation graphs, this is where I could easily do it.
			
			if (spreadTransformer != null) {
				toReturn.add(spreadTransformer.getGraph());
			}
		*/
		
		return toReturn;
	}
}
