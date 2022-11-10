package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.enemies.Enemy;

public class TriJaw extends Enemy {
	public TriJaw() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.0128254724;
		
		enemyName = "Mactera Tri-Jaw";
		macteraType = true;
		baseHealth = 350;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.8;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(damageElement.melee, -1.0);
		resistances.setResistance(damageElement.fire, -1.0);
		resistances.setResistance(damageElement.explosive, -1.0);
		resistances.setResistance(damageElement.electric, -0.5);
		resistances.setResistance(damageElement.corrosive, -1.2);
		
		igniteTemperature = 35; 
		douseTemperature = 5;
		coolingRate = 10;
		freezeTemperature = -100;
		unfreezeTemperature = 0;
		warmingRate = 40;
	}
}