package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Menace extends Enemy {
	public Menace() {
		guessedSpawnProbability = 0.02;
		exactSpawnProbability = 0.001577146258;
		
		enemyName = "Glyphid Menace";
		baseHealth = 700;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.7;

		temperatureComponent = new CreatureTemperatureComponent(30, 0, 6, 2, -50, 0, 6, 2);
		
		courage = 0.7;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.5;
		
		hasHeavyArmorRNG = true;
		armorStrength = (1*1 + 2*10)/3.0;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 3;
	}
}
