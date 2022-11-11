package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;

public class Guard extends Enemy {
	public Guard() {
		guessedSpawnProbability = 0.07;
		exactSpawnProbability = 0.05400981997;
		
		enemyName = "Glyphid Grunt Guard";
		baseHealth = 270;
		normalScaling = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.5;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.fire, 0.25);
		resistances.setResistance(DamageElement.frost, 0.3);
		resistances.setResistance(DamageElement.explosive, 0.3);
		resistances.setResistance(DamageElement.corrosive, 0.2);

		igniteTemperature = 60; 
		douseTemperature = 40;
		coolingRate = 6;
		freezeTemperature = -80;
		unfreezeTemperature = -40;
		warmingRate = 6;
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.7;
		
		hasLightArmor = true;
		armorStrength = 15;
		hasHeavyArmorHealth = true;
		armorBaseHealth = 60;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 2;
		numArmorHealthPlates = 4;
	}
}
