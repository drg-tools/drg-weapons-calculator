package enemies.mactera;

import enemies.Enemy;

public class Spawn extends Enemy {
	public Spawn() {
		guessedSpawnProbability = 0.08;
		exactSpawnProbability = 0.02550215742;
		
		enemyName = "Mactera Spawn";
		macteraType = true;
		baseHealth = 223;
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
