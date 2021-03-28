package enemies.other;

import enemies.Enemy;

public class NaedocyteBreeder extends Enemy {
	public NaedocyteBreeder() {
		guessedSpawnProbability = 0.02;
		exactSpawnProbability = 0.000684421961;
		
		calculateBreakpoints = false;
		
		enemyName = "Naedocyte Breeder";
		baseHealth = 1500;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.1;
		
		igniteTemperature = 60; 
		douseTemperature = 30;
		coolingRate = 10;
		freezeTemperature = -150;
		unfreezeTemperature = 0;
		warmingRate = 40;
	}
}