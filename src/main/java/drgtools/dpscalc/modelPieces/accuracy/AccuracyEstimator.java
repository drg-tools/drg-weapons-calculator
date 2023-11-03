package drgtools.dpscalc.modelPieces.accuracy;

import javax.swing.JPanel;

public abstract class AccuracyEstimator {
	// With these two values, recoil should be reduced to 0% in exactly 0.5 seconds.
	protected double delayBeforePlayerReaction = 0.15;  // seconds
	protected double playerRecoilRecoveryPerSecond = 1.00/0.35;  // Percentage of max recoil that the player recovers per second

	// This variable determines the minimum value for Recoil(t) to fall to before that recoil is discarded by successive shots
	protected double recoilGoal = 0.1;

	protected double targetDistanceMeters;
	protected boolean modelRecoil;
	protected boolean dwarfIsMoving;
	protected boolean visualizeGeneralAccuracy;
	protected boolean canBeVisualized;

	protected double rateOfFire, burstInterval;
	protected int magSize, burstSize;
	protected SpreadSettings spreadSettings;
	protected RecoilSettings recoilSettings;
	protected double[] bulletFiredTimestamps;
	protected double naturalFrequency, initialRecoilVelocity, recoilPerShotEndTime;
	
	protected AccuracyEstimator() {
		// Start at 10m distance for all weapons in AccuracyEstimator, but let it be changed by individual weapons as necessary.
		targetDistanceMeters = 10.0;
		visualizeGeneralAccuracy = true;
		dwarfIsMoving = false;
		modelRecoil = true;
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
	
	protected double convertDegreesToMeters(double degrees) {
		// Because both recoil and spread use degrees as their output, I have to first convert from degrees to radians for the Math package
		return targetDistanceMeters * Math.tan(degrees * Math.PI / 180.0);
	}

	protected void calculateBulletFiredTimestamps() {
		bulletFiredTimestamps = new double[magSize];
		
		double timeBetweenBursts = 1.0 / rateOfFire;
		
		double currentTime = 0.0;
		for (int i = 0; i < magSize; i++) {
			bulletFiredTimestamps[i] = currentTime;
			
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				currentTime += burstInterval;
			}
			else if (burstSize > 1 && (i+1) % burstSize == 0) {
				currentTime += burstInterval + timeBetweenBursts;
			}
			else {
				// Either this gun doesn't have a burst mode, or it just fired the last bullet during a burst
				currentTime += timeBetweenBursts;
			}
		}
	}

	protected double getRecoilPerShotOverTime(double t) {
		return Math.pow(Math.E, -1.0 * naturalFrequency * t) * (initialRecoilVelocity * t);
	}
	
	protected double getTotalRecoilAtTime(double t, boolean playerReducingRecoil) {
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

	public abstract double estimateAccuracy(boolean weakpointTarget);
	
	public boolean visualizerIsReady() {
		return canBeVisualized;
	}
	
	public abstract JPanel getVisualizer();
}
