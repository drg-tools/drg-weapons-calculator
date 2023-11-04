package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class WebSpitter extends Enemy {
	public WebSpitter() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.02963844666;
		
		enemyName = "Glyphid Web Spitter";
		baseHealth = 39.9;
		normalScaling = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.1;

		temperatureComponent = new CreatureTemperatureComponent(30, 0, 6, 2, -75, 0, 10, 2);
		
		courage = 0.3;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.5;
		
		hasLightArmor = true;
		armorStrength = 10;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 3;
	}
}
