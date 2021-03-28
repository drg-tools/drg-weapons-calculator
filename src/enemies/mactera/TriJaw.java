package enemies.mactera;

import enemies.Enemy;

public class TriJaw extends Enemy {
	public TriJaw() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.0128254724;
		
		enemyName = "Mactera Tri-Jaw";
		macteraType = true;
		baseHealth = 350;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.8;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		explosiveResistance = -1.0;
		fireResistance = -1.0;
		electricResistance = -0.5;
		
		igniteTemperature = 35; 
		douseTemperature = 5;
		coolingRate = 10;
		freezeTemperature = -100;
		unfreezeTemperature = 0;
		warmingRate = 40;
	}
}