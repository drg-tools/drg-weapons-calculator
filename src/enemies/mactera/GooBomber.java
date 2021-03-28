package enemies.mactera;

import enemies.Enemy;

public class GooBomber extends Enemy {
	public GooBomber() {
		guessedSpawnProbability = 0.03;
		exactSpawnProbability = 0.005088528493;
		
		enemyName = "Mactera Goo Bomber";
		macteraType = true;
		baseHealth = 800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.9;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		fireResistance = -0.2;
		
		igniteTemperature = 35; 
		douseTemperature = 5;
		coolingRate = 10;
		freezeTemperature = -320;
		unfreezeTemperature = 0;
		warmingRate = 50;
	}
}