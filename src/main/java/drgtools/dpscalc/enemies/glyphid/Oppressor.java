package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;

public class Oppressor extends Enemy {
	public Oppressor() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.003303079899;
		
		enemyName = "Glyphid Oppressor";
		baseHealth = 900;
		normalScaling = false;
		
		hasWeakpoint = true;
		weakpointMultiplier = 1;
		estimatedProbabilityBulletHitsWeakpoint = 1.0;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.melee, -0.5);
		resistances.setResistance(DamageElement.piercing, 0.5);
		resistances.setResistance(DamageElement.fire, 0.66);
		resistances.setResistance(DamageElement.frost, 0.5);
		resistances.setResistance(DamageElement.explosive, 0.66);
		resistances.setResistance(DamageElement.electric, 0.25);
		resistances.setResistance(DamageElement.corrosive, 0.66);
		
		igniteTemperature = 100; 
		douseTemperature = 40;
		coolingRate = 20;
		freezeTemperature = -300;
		unfreezeTemperature = -200;
		warmingRate = 100;
		
		courage = 1.0;  // (technically 100.0 in-game, but I think that's an erroneous value.)
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 0.0;
		
		hasUnbreakableArmor = true;
	}
}
