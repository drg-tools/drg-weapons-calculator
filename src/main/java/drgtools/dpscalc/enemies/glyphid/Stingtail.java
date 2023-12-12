package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Stingtail extends Enemy {
	public Stingtail() {
		// TDDO
		// guessedSpawnProbability = ;
		// exactSpawnProbability = ;

		enemyName = "Glyphid Stingtail";
		baseHealth = 400;
		normalScaling = true;

		hasExposedBodySomewhere = true;

		hasWeakpoint = true;
		weakpointMultiplier = 2;
		//estimatedProbabilityBulletHitsWeakpoint ;

		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElements.DamageElement.fire, 0.3);
		resistances.setResistance(DamageElements.DamageElement.explosive, -0.5);
		resistances.setResistance(DamageElements.DamageElement.electric, -0.3);
		resistances.setResistance(DamageElements.DamageElement.piercing, -0.1);

		temperatureComponent = new CreatureTemperatureComponent(75, 40, 7, 2, -100, -30, 7, 1);

		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		// maxMovespeedWhenFeared = ;

		hasHeavyArmorHealth = true;
		heavyArmorCoversWeakpoint = true;
		armorBaseHealth = 50;
		// numArmorHealthPlates = ;
	}
}