package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.damage.DamageElements.damageElement;
import drgtools.dpscalc.enemies.Enemy;

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

		resistances.setResistance(damageElement.melee, -0.5);
		
		igniteTemperature = 30; 
		douseTemperature = 0;
		coolingRate = 10;
		freezeTemperature = -180;
		unfreezeTemperature = 0;
		warmingRate = 40;
	}
}