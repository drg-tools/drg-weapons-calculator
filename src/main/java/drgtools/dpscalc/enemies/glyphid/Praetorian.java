package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Praetorian extends Enemy {
	public Praetorian() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.02074096117;
		
		enemyName = "Glyphid Praetorian";
		baseHealth = 750;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 1;
		estimatedProbabilityBulletHitsWeakpoint = 0.4;

		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.piercing, 0.3);

		temperatureComponent = new CreatureTemperatureComponent(100, 40, 10, 2.5, -150, -100, 10, 1);
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.0;
		
		hasHeavyArmorHealth = true;
		armorBaseHealth = 100;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorHealthPlates = 6;
	}
	
	@Override
	public String getBodyshotName() {
		// Prepend an extra space to insert this between the name and the trailing colon
		return " (Mouth)";
	}
}
