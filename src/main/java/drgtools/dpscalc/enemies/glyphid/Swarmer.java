package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Swarmer extends Enemy {
	public Swarmer() {
		guessedSpawnProbability = 0.17;
		exactSpawnProbability = 0.2503794078;
		
		enemyName = "Glyphid Swarmer";
		baseHealth = 12;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;

		temperatureComponent = new CreatureTemperatureComponent(5, 0, 1, 1.5, -20, 0, 2, 2);
		temperatureComponent.setDieOnFire(true);
		temperatureComponent.setDieFrozen(true);
		
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 3.5;
	}
}
