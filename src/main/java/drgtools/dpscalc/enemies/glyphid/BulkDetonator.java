package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class BulkDetonator extends Enemy {
	public BulkDetonator() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.001220056539;
		
		calculateBreakpoints = false;
		
		enemyName = "Glyphid Bulk Detonator";
		baseHealth = 4000;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.2;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.explosive, 0.5);

		temperatureComponent = new CreatureTemperatureComponent(60, 30, 10, 2.5, -490, -200, 300, 1);
		temperatureComponent.setUpdateTime(0.25);
		
		courage = 1.0;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 0.0;
	}
}