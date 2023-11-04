package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Grabber extends Enemy {
	public Grabber() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.001934235977;
		
		enemyName = "Mactera Grabber";
		macteraType = true;
		baseHealth = 500;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.2;

		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.melee, -0.5);

		temperatureComponent = new CreatureTemperatureComponent(30, 0, 10, 1.5, -180, 0, 40, 2);
		temperatureComponent.setDieFrozen(true);
	}
}