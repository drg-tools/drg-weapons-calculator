package drgtools.dpscalc.enemies.mactera;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageInstance;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class Brundle extends Enemy {
	public Brundle() {
		guessedSpawnProbability = 0.01;
		exactSpawnProbability = 0.003124535039;
		
		enemyName = "Mactera Brundle";
		baseHealth = 600;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.6;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		resistances.setResistance(DamageElement.melee, -1.0);
		resistances.setResistance(DamageElement.fire, -1.0);
		resistances.setResistance(DamageElement.explosive, -1.0);
		resistances.setResistance(DamageElement.electric, -0.5);
		resistances.setResistance(DamageElement.corrosive, -1.0);

		temperatureComponent = new CreatureTemperatureComponent(35, 5, 10, 1.5, -200, 0, 40, 1);
		temperatureComponent.setDieFrozen(true);
		
		hasHeavyArmorHealth = true;
		heavyArmorCoversWeakpoint = true;
		armorBaseHealth = 80;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorHealthPlates = 2;
	}

	// Brundle's ArmorWasting gets calculated differently than default
	@Override
	public double calculatePercentageOfDamageWastedByArmor(DamageInstance dmgInstance, double generalAccuracy, double weakpointAccuracy,
														   double normalScaling, double largeScaling) {
		int totalNumDmgComponentsPerHit = dmgInstance.getTotalNumberOfDamageComponents();

		double baseHealth = getBaseHealth() * normalScaling;
		double heavyArmorPlateHealth = getArmorBaseHealth() * normalScaling;

		int i;
		double potentialWeakpointDamage, damageThatBypassesArmor, damageDealtToArmor, damageAffectedByArmor;
		double totalDamageSpent = 0, damageDealtToHealth, actualDamageDealt = 0;
		DamageComponent dmgAlias;
		while (baseHealth > 0) {
			for (i = 0; i < totalNumDmgComponentsPerHit; i++) {
				// 1. Select the right DamageComponent to evaluate for this loop
				dmgAlias = dmgInstance.getDamageComponentAtIndex(i);

				// 2. Calculate its damage variants
				potentialWeakpointDamage = dmgAlias.getTotalComplicatedDamageDealtPerHit(
					MaterialFlag.weakpoint,
					getElementalResistances(),
					false,
					getWeakpointMultiplier(),  // 3.0 from Brundle
					1
				);
				damageThatBypassesArmor = dmgAlias.getTotalComplicatedDamageDealtPerHit(
					MaterialFlag.heavyArmor,
					getElementalResistances(),
					false,
					0,
					1
				);
				damageDealtToArmor = dmgAlias.getTotalArmorDamageOnDirectHit();
				damageAffectedByArmor = potentialWeakpointDamage - damageThatBypassesArmor;

				// 3. Subtract from Armor and Health accordingly
				totalDamageSpent += potentialWeakpointDamage;
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