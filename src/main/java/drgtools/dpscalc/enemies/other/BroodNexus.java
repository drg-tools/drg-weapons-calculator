package drgtools.dpscalc.enemies.other;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

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

		temperatureComponent = new CreatureTemperatureComponent(30, 0, 4, 1.5, -50, 0, 4, 2);
		temperatureComponent.setTempChangeScale(4.0);
	}
}