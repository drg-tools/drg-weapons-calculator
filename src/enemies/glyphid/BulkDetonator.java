package enemies.glyphid;

import enemies.Enemy;

public class BulkDetonator extends Enemy {
	public BulkDetonator() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.001220056539;
		
		calculateBreakpoints = false;
		
		enemyName = "Glyphid Bulk Detonator";
		baseHealth = 4000;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.2;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		explosiveResistance = 0.5;
		
		temperatureUpdateTime = 0.25;
		igniteTemperature = 60; 
		douseTemperature = 30;
		coolingRate = 10;
		freezeTemperature = -490;
		unfreezeTemperature = -200;
		warmingRate = 300;
		
		courage = 1.0;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 0.0;
	}
}