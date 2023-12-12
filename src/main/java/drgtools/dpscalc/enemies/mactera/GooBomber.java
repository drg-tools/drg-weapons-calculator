package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class GooBomber extends Enemy {
	public GooBomber() {
		guessedSpawnProbability = 0.03;
		exactSpawnProbability = 0.005088528493;
		
		enemyName = "Mactera Goo Bomber";
    baseHealth = 800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.9;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.melee, -0.5);
		resistances.setResistance(DamageElement.piercing, -0.2);
		resistances.setResistance(DamageElement.fire, -0.2);
		resistances.setResistance(DamageElement.corrosive, -0.5);

		temperatureComponent = new CreatureTemperatureComponent(35, 5, 10, 1.5, -320, 0, 50, 1);
		temperatureComponent.setDieFrozen(true);
	}
}