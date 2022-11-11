package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;

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
		
		igniteTemperature = 10; 
		douseTemperature = 0;
		coolingRate = 6;
		freezeTemperature = -10;
		unfreezeTemperature = 0;
		warmingRate = 12;
		
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 4.0;
	}
}