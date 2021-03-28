package enemies.other;

import enemies.Enemy;

public class BroodNexus extends Enemy {
	public BroodNexus() {
		guessedSpawnProbability = 0.02;
		exactSpawnProbability = 0.001666418688;
		
		calculateBreakpoints = false;
		
		enemyName = "Glyphid Brood Nexus";
		baseHealth = 1800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.9;
		
		temperatureChangeScale = 4.0;
		igniteTemperature = 30; 
		douseTemperature = 0;
		coolingRate = 4;
		freezeTemperature = -50;
		unfreezeTemperature = 0;
		warmingRate = 4;
	}
}