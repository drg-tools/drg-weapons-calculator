package drgtools.dpscalc.enemies.glyphid;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageInstance;
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

	// Praetorian's ArmorWasting gets calculated differently than default
	@Override
	public double calculatePercentageOfDamageWastedByArmor(DamageInstance dmgInstance, double generalAccuracy, double weakpointAccuracy,
														   double normalScaling, double largeScaling) {
		int totalNumDmgComponentsPerHit = dmgInstance.getTotalNumberOfDamageComponents();

		double baseHealth = getBaseHealth() * largeScaling;
		double heavyArmorPlateHealth = getArmorBaseHealth() * normalScaling;

		double proportionOfDamageThatHitsMouth = generalAccuracy / 100.0;
		double proportionOfDamageThatHitsArmor = 1.0 - proportionOfDamageThatHitsMouth;

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
				damageDealtToArmor = dmgAlias.getArmorDamageOnDirectHit() * proportionOfDamageThatHitsArmor + dmgAlias.getRadialArmorDamageOnDirectHit();
				damageAffectedByArmor = potentialMaxDamage - damageThatBypassesArmor;

				// 3. Subtract from Armor and Health accordingly
				totalDamageSpent += potentialMaxDamage;
				damageDealtToHealth = damageThatBypassesArmor + damageAffectedByArmor * proportionOfDamageThatHitsMouth;

				if (heavyArmorPlateHealth > 0) {
					if (dmgAlias.armorBreakingIsGreaterThan100Percent()) {
						if (damageDealtToArmor > heavyArmorPlateHealth) {
							damageDealtToHealth += damageAffectedByArmor * proportionOfDamageThatHitsArmor;
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
					damageDealtToHealth += damageAffectedByArmor * proportionOfDamageThatHitsArmor;
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
