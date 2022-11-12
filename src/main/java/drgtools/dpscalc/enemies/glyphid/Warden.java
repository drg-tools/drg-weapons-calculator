package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Warden extends Enemy {
	public Warden() {
		guessedSpawnProbability = 0.02;
		exactSpawnProbability = 0.00208302336;
		
		enemyName = "Glyphid Warden";
		baseHealth = 800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.5;

		temperatureComponent = new CreatureTemperatureComponent(50, 25, 6, 2, -70, -30, 6, 2);
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.9;
		
		hasHeavyArmorRNG = true;
		armorStrength = 15;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 3;
	}
	
	@Override
	public String getWeakpointName() {
		return "Orb";
	}
}
