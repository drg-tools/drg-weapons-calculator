package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Spreader extends Enemy {
	public Spreader() {
		// TDDO
		// guessedSpawnProbability = ;
		// exactSpawnProbability = ;

		enemyName = "Glyphid Septic Spreader";
		baseHealth = 270;
		normalScaling = true;

		hasWeakpoint = true;
		weakpointMultiplier = 2;
		//estimatedProbabilityBulletHitsWeakpoint ;

		temperatureComponent = new CreatureTemperatureComponent(35, 5, 6, 2, -50, 0, 6, 2);

		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		// maxMovespeedWhenFeared = ;

		hasLightArmor = true;
		armorStrength = 20;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		// numArmorStrengthPlates = ;
	}

	@Override
	public String getWeakpointName() {
		return "Sac";
	}
}