package enemies.other;

import enemies.Enemy;

public class CaveLeech extends Enemy {
	public CaveLeech() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.004552893915;
		
		enemyName = "Cave Leech";
		baseHealth = 100;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		igniteTemperature = 30; 
		douseTemperature = 0;
		coolingRate = 10;
		freezeTemperature = -50;
		unfreezeTemperature = 0;
		warmingRate = 10;
	}
}