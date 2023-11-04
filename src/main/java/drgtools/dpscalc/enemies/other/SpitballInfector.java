package drgtools.dpscalc.enemies.other;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class SpitballInfector extends Enemy {
	public SpitballInfector() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.003660169618;
		
		calculateBreakpoints = false;
		
		enemyName = "Spitball Infector";
		baseHealth = 800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.4;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.fire, -1.0);

		temperatureComponent = new CreatureTemperatureComponent(30, 0, 10, 1.5, -50, 0, 10, 2);
	}
}