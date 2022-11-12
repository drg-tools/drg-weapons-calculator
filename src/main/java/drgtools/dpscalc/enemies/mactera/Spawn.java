package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Spawn extends Enemy {
	public Spawn() {
		guessedSpawnProbability = 0.08;
		exactSpawnProbability = 0.02550215742;
		
		enemyName = "Mactera Spawn";
		macteraType = true;
		baseHealth = 223;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.8;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.melee, -1.0);
		resistances.setResistance(DamageElement.piercing, -0.33);
		resistances.setResistance(DamageElement.fire, -1.0);
		resistances.setResistance(DamageElement.explosive, -1.0);
		resistances.setResistance(DamageElement.electric, -0.5);
		resistances.setResistance(DamageElement.corrosive, -1.0);

		temperatureComponent = new CreatureTemperatureComponent(35, 5, 10, 1.5, -100, 0, 40, 1);
	}
}
