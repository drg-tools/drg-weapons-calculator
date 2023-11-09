package drgtools.dpscalc.enemies.other;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageInstance;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

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
		resistances.setResistance(DamageElement.fire, qronarShellbackRolling * 0.3 + qronarShellbackUnrolled * -0.5);
		resistances.setResistance(DamageElement.frost, qronarShellbackRolling * 0.3 + qronarShellbackUnrolled * -0.7);
		resistances.setResistance(DamageElement.explosive, qronarShellbackRolling * 0.8);
		resistances.setResistance(DamageElement.electric, qronarShellbackRolling * 1.0);
		resistances.setResistance(DamageElement.corrosive, qronarShellbackRolling * 0.3 + qronarShellbackUnrolled * -0.5);

		temperatureComponent = new CreatureTemperatureComponent(100, 70, 10, 1.5, -120, -90, 10, 2);
		
		hasHeavyArmorHealth = true;
		armorBaseHealth = (6*70 + 14*30)/20.0;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorHealthPlates = 6;
	}

	// Shellback's ArmorWasting gets calculated differently than default
	@Override
	public double calculatePercentageOfDamageWastedByArmor(DamageInstance dmgInstance, double generalAccuracy, double weakpointAccuracy,
														   double normalScaling, double largeScaling) {
		int totalNumDmgComponentsPerHit = dmgInstance.getTotalNumberOfDamageComponents();

		double baseHealth = getBaseHealth() * largeScaling;
		double heavyArmorPlateHealth = getArmorBaseHealth() * normalScaling;

		int i;
		double potentialMaxDamage, damageThatBypassesArmor, damageDealtToArmor, damageAffectedByArmor;
		double totalDamageSpent = 0, damageDealtToHealth, actualDamageDealt = 0;
		DamageComponent dmgAlias;
		while (baseHealth > 0) {
			for (i = 0; i < totalNumDmgComponentsPerHit; i++) {
				// 1. Select the right DamageComponent to evaluate for this loop
				dmgAlias = dmgInstance.getDamageComponentAtIndex(i);

				// 2. Calculate its damage variants
				potentialMaxDamage = dmgAlias.calculateComplicatedDamageDealtPerHit(
					MaterialFlag.normalFlesh,
					getElementalResistances(),
					false,
					0,
					1
				);
				damageThatBypassesArmor = dmgAlias.calculateComplicatedDamageDealtPerHit(
					MaterialFlag.heavyArmor,
					getElementalResistances(),
					false,
					0,
					1
				);
				damageDealtToArmor = dmgAlias.getTotalArmorDamageOnDirectHit();
				damageAffectedByArmor = potentialMaxDamage - damageThatBypassesArmor;

				// 3. Subtract from Armor and Health accordingly
				totalDamageSpent += potentialMaxDamage;
				damageDealtToHealth = damageThatBypassesArmor;

				if (heavyArmorPlateHealth > 0) {
					if (dmgAlias.armorBreakingIsGreaterThan100Percent()) {
						if (damageDealtToArmor > heavyArmorPlateHealth) {
							damageDealtToHealth += damageAffectedByArmor;
							heavyArmorPlateHealth = 0.0;
						}
						else {
							heavyArmorPlateHealth -= damageDealtToArmor;
						}
					}
					else {
						heavyArmorPlateHealth -= damageDealtToArmor;
					}
				}
				else {
					damageDealtToHealth += damageAffectedByArmor;
				}

				actualDamageDealt += damageDealtToHealth;
				baseHealth -= damageDealtToHealth;
			}
		}

		double damageWasted = 1.0 - actualDamageDealt / totalDamageSpent;
		// Mathematica's Chop[] function rounds any number lower than 10^-10 to the integer zero. Imitation, flattery, etc...
		if (damageWasted < Math.pow(10.0, -10.0)) {
			return 0.0;
		}
		else {
			return damageWasted;
		}
	}
}