package enemies.other;

import enemies.Enemy;

public class SpitballInfector extends Enemy {
	public SpitballInfector() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.003660169618;
		
		calculateBreakpoints = false;
		
		enemyName = "Spitball Infector";
		baseHealth = 800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.4;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		fireResistance = -1.0;
		
		igniteTemperature = 30; 
		douseTemperature = 0;
		coolingRate = 10;
		freezeTemperature = -50;
		unfreezeTemperature = 0;
		warmingRate = 10;
	}
}