package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.damage.DamageElements.damageElement;
import drgtools.dpscalc.enemies.Enemy;

public class Brundle extends Enemy {
	public Brundle() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.003124535039;
		
		enemyName = "Mactera Brundle";
		macteraType = true;
		baseHealth = 600;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.6;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(damageElement.melee, -1.0);
		resistances.setResistance(damageElement.fire, -1.0);
		resistances.setResistance(damageElement.explosive, -1.0);
		resistances.setResistance(damageElement.electric, -0.5);
		resistances.setResistance(damageElement.corrosive, -1.0);

		igniteTemperature = 35; 
		douseTemperature = 5;
		coolingRate = 10;
		freezeTemperature = -200;
		unfreezeTemperature = 0;
		warmingRate = 40;
		
		hasHeavyArmorHealth = true;
		heavyArmorCoversWeakpoint = true;
		armorBaseHealth = 80;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorHealthPlates = 2;
	}
}