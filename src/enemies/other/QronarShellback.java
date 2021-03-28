package enemies.other;

import enemies.Enemy;

public class QronarShellback extends Enemy {
	public QronarShellback() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.001755691117;
		
		calculateBreakpoints = false;
		
		enemyName = "Q'ronar Shellback";
		baseHealth = 450;
		normalScaling = false;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.1;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		// Weighted Q'Ronar Shellback rolling state at 2/3 and non-rolling state at 1/3
		double qronarShellbackRolling = 0.66;
		double qronarShellbackUnrolled = 0.34;
		explosiveResistance = qronarShellbackRolling * 0.8;
		fireResistance = qronarShellbackRolling * 0.3 + qronarShellbackUnrolled * -0.5;
		frostResistance = qronarShellbackRolling * 0.3 + qronarShellbackUnrolled * -0.7;
		electricResistance = qronarShellbackRolling * 1.0;
		
		igniteTemperature = 100; 
		douseTemperature = 70;
		coolingRate = 10;
		freezeTemperature = -120;
		unfreezeTemperature = 0;
		warmingRate = 10;
		
		hasHeavyArmorHealth = true;
		armorBaseHealth = (6*70 + 14*30)/20;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorHealthPlates = 6;
	}
}