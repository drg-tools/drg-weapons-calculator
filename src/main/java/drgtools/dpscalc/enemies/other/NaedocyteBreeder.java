package drgtools.dpscalc.enemies.other;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

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

		temperatureComponent = new CreatureTemperatureComponent(60, 30, 10, 2.5, -150, 0, 0, 1);
		temperatureComponent.setDieFrozen(true);
	}
}