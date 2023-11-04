package drgtools.dpscalc.enemies.other;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class CaveLeech extends Enemy {
	public CaveLeech() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.004552893915;
		
		enemyName = "Cave Leech";
		baseHealth = 100;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;

		temperatureComponent = new CreatureTemperatureComponent(30, 0, 10, 1.5, -50, 0, 10, 2);
	}
}