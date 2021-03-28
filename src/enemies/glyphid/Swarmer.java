package enemies.glyphid;

import enemies.Enemy;

public class Swarmer extends Enemy {
	public Swarmer() {
		guessedSpawnProbability = 0.17;
		exactSpawnProbability = 0.2503794078;
		
		enemyName = "Glyphid Swarmer";
		baseHealth = 12;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		igniteTemperature = 5; 
		douseTemperature = 0;
		coolingRate = 1;
		freezeTemperature = -20;
		unfreezeTemperature = 0;
		warmingRate = 2;
		
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 3.5;
	}
}
