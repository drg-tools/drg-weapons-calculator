package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Exploder extends Enemy {
	public Exploder() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.03895253682;
		
		enemyName = "Glyphid Exploder";
		baseHealth = 20;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.1;

		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.melee, -0.25);

		temperatureComponent = new CreatureTemperatureComponent(10, 0, 6, 2, -10, 0, 12, 2);
		temperatureComponent.setDieOnFire(true);
		
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 4.0;
	}
}