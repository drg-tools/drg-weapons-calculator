package drgtools.dpscalc.damage;

import drgtools.dpscalc.damage.DamageElements.damageElement;
import drgtools.dpscalc.damage.DamageElements.temperatureElement;
import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.utilities.MathUtils;

// TODO: add Weakpoint Bonus value for the "direct damage" side
// TODO: after finishing building this new feature, see how easy it would be to implement into Breakpoints' method.

public class DamageComponent {
	protected double damage = 0;
	protected double temperature = 0;
	protected boolean benefitsFromWeakpoint;
	protected boolean benefitsFromFrozen;
	protected boolean reducedByArmor;
	protected boolean canDamageArmor;

	protected double bonusDamage = 0;
	protected damageElement bonusDmgElement = null;
	
	protected double radialDamage = 0;
	protected double radialTemperature = 0;
	protected double maxDmgRadius;
	protected double damageRadius;
	protected double falloff;
	
	protected double armorBreaking;
	protected double friendlyFire;
	
	protected temperatureElement tempElement;  // applies to both Damage and RadialDamage; can be either Heat or Cold. This can be assumed because it's illogical to do both Heat & Cold simultaneously.
	protected double[] damageElements;
	protected int startingDamageElementIndex = -1;
	protected double[] radialDamageElements;
	protected int startingRadialDamageElementIndex = -1;
	// protected StatusEffect[] statusEffectsApplied;

	// Shortcut constructor for what is referred to as "Direct Damage"
	public DamageComponent(double dmg, damageElement dmgElement, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, 0, null, true, true, true, true,
				0, null, 0, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for damage-only Direct+Radial, like Autocannon or PGL
	public DamageComponent(double dmg, damageElement dmgElement, double rdlDmg, damageElement rdlDmgElement, double mdr,
						   double dmgRd, double fal, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, 0, null, true, true, true, true, rdlDmg,
				rdlDmgElement, 0, mdr, dmgRd, fal, ab, ff, baselineConversions);
	}

	// Shortcut constructor for any DamageComponent that neither is "Direct Damage" nor uses RadialDamage
	public DamageComponent(double dmg, damageElement dmgElement, double temp, temperatureElement tmpElement,
						   boolean weakpoint, boolean frozen, boolean armor, boolean damagesArmor,
						   double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, temp, tmpElement, weakpoint, frozen, armor, damagesArmor, 0,null,
				0, 0, 0, 0, ab, ff, baselineConversions);
	}

	public DamageComponent(double dmg, damageElement dmgElement, double temp, temperatureElement tmpElement,
						   boolean weakpoint, boolean frozen, boolean armor, boolean damagesArmor,
						   double rdlDmg, damageElement rdlDmgElement, double radTemp, double mdr, double dmgRd, double fal,
						   double ab, double ff, DamageConversion[] baselineConversions) {
		damage = dmg;
		temperature = temp;
		tempElement = tmpElement;
		benefitsFromWeakpoint = weakpoint;
		benefitsFromFrozen = frozen;
		reducedByArmor = armor;
		canDamageArmor = damagesArmor;

		radialDamage = rdlDmg;
		radialTemperature = radTemp;
		maxDmgRadius = mdr;
		damageRadius = dmgRd;
		falloff = fal;

		armorBreaking = ab;
		friendlyFire = ff;
		
		damageElements = new double[DamageElements.numElements];
		if (dmgElement != null) {
			startingDamageElementIndex = DamageElements.getElementIndex(dmgElement);
			damageElements[startingDamageElementIndex] = 1.0;
		}
		
		radialDamageElements = new double[DamageElements.numElements];
		if (rdlDmgElement != null) {
			startingRadialDamageElementIndex = DamageElements.getElementIndex(rdlDmgElement);
			radialDamageElements[startingRadialDamageElementIndex] = 1.0;
		}
		
		if (baselineConversions != null && baselineConversions.length > 0) {
			for (int i = 0; i < baselineConversions.length; i++) {
				applyDamageConversion(baselineConversions[i]);
			}
		}
	}
	
	public void setDamage(double in) {
		damage = in;
	}
	public void setRadialDamage(double in) {
		radialDamage = in;
	}
	public void setDamageRadius(double in) {
		damageRadius = in;
	}
	public void setArmorBreaking(double in) {
		armorBreaking = in;
	}
	// For Zhukovs OC "Gas Recycling" in particular. AFAIK that's the only upgrade in-game that changes one of the "damage flags" like this.
	public void setBenefitsFromWeakpoint(boolean in) {
		benefitsFromWeakpoint = in;
	}
	
	public void applyDamageConversion(DamageConversion dc) {
		if (dc.convertsToDamage()) {
			int elementIndex = DamageElements.getElementIndex(dc.getDamageElement());
			damageElements[elementIndex] += dc.getPercentage();
			radialDamageElements[elementIndex] += dc.getPercentage();
		}

		if (dc.convertsToTemperature()) {
			tempElement = dc.getTemperatureElement();
			// TODO: this feels risky. Should it be stored as a % until it gets evaluated, like the Damage does?
			temperature += dc.getPercentage() * damage;
			radialTemperature += dc.getPercentage() * radialDamage;
		}

		// Only subtract once, in case it converts to (Fire-element + Heat) or (Frost-element + Cold).
		if (dc.convertsInsteadOfAdds()) {
			// This is an implicit check for "has the starting element for damage been defined?", because this value will be -1 if the initial value was set to Null.
			if (startingDamageElementIndex > -1) {
				damageElements[startingDamageElementIndex] = Math.max(damageElements[startingDamageElementIndex] - dc.getPercentage(), 0);
			}
			if (startingRadialDamageElementIndex > -1) {
				radialDamageElements[startingRadialDamageElementIndex] = Math.max(radialDamageElements[startingRadialDamageElementIndex] - dc.getPercentage(), 0);
			}
		}
	}

	// Used by Subata T5.B and SMG OC "EMRB"
	public void addBonusDamage(damageElement bnsDmgElement, double bonus) {
		bonusDmgElement = bnsDmgElement;
		bonusDamage = bonus;
	}

	// TODO: add a way that this class can store Status Effects
	// TODO: add a utility method that nicely formats this stuff
	// TODO: this might need to have a "DamageInstance" class made which stores 2+ DamageComponents together, that get applied on each hit?

	public double getRawDamage() {
		if (damage > 0) {
			return MathUtils.sum(damageElements) * damage + bonusDamage;
		}
		else {
			return 0;
		}
	}
	public double getRawRadialDamage() {
		if (radialDamage > 0) {
			return MathUtils.sum(radialDamageElements) * radialDamage;
		}
		else {
			return 0;
		}
	}
	
	public double getResistedDamage(ElementalResistancesArray creatureResistances) {
		double bnsDamage = bonusDamage * creatureResistances.getResistance(bonusDmgElement);
		return damage * MathUtils.vectorDotProduct(damageElements, creatureResistances.getResistances()) + bnsDamage;
	}
	public double getResistedRadialDamage(ElementalResistancesArray creatureResistances) {
		return radialDamage * MathUtils.vectorDotProduct(radialDamageElements, creatureResistances.getResistances());
	}
	
	// TODO: this is probably where I can implement the buggy Radial Damage insanity?
	public double probabilityToBreakArmorStrengthPlate(double armorStrength) {
		return 0;
	}
}