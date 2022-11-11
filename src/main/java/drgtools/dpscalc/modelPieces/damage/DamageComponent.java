package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.modelPieces.statusEffects.MultipleSTEs;
import drgtools.dpscalc.modelPieces.statusEffects.PushSTEComponent;
import drgtools.dpscalc.utilities.MathUtils;

// TODO: move the "calculate num enemies hit" into this object
// TODO: consider moving the "calculate total damage dealt per hit" into this object?
// TODO: after finishing building this new feature, see how easy it would be to implement into Breakpoints' method.

public class DamageComponent {
	protected double damage = 0;
	protected double temperature = 0;
	protected boolean benefitsFromWeakpoint;
	protected boolean benefitsFromFrozen;
	protected boolean reducedByArmor;
	protected boolean canDamageArmor;

	protected double weakpointBonus = 0;
	protected int numBlowthroughs = 0;
	protected int numHitscanTracersPerShot = 1;  // Used for shotguns. Simpler than tracking NumPellets identical DamageComponents, just move the multiplier inside.

	protected double bonusDamage = 0;
	protected DamageElement bonusDmgElement = null;
	
	protected double radialDamage = 0;
	protected double radialTemperature = 0;
	protected double maxDmgRadius;
	protected double damageRadius;
	protected double falloff;

	protected boolean stunOnWeakpointOnly;
	protected double stunChance = 0;
	protected double stunDuration = 0;
	protected double baseFearChance = 0;
	protected double armorBreaking = 1.0;
	protected double friendlyFire = 1.0;
	
	protected TemperatureElement tempElement;  // applies to both Damage and RadialDamage; can be either Heat or Cold. This can be assumed because it's illogical to do both Heat & Cold simultaneously.
	protected double[] damageElements;
	protected int startingDamageElementIndex = -1;
	protected double[] radialDamageElements;
	protected int startingRadialDamageElementIndex = -1;
	protected MultipleSTEs statusEffectsApplied;

	// Shortcut constructor for what is referred to as "Direct Damage"
	public DamageComponent(double dmg, DamageElement dmgElement, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, 0, null, true, true, true, true,
				0, null, 0, 0, 0, 0, false, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for damage-only Direct+Radial, like Autocannon or PGL
	public DamageComponent(double dmg, DamageElement dmgElement, double rdlDmg, DamageElement rdlDmgElement, double mdr,
                           double dmgRd, double fal, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, 0, null, true, true, true, true, rdlDmg,
				rdlDmgElement, 0, mdr, dmgRd, fal, false, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for any DamageComponent that neither is "Direct Damage" nor uses RadialDamage
	public DamageComponent(double dmg, DamageElement dmgElement, double temp, TemperatureElement tmpElement,
                           boolean weakpoint, boolean frozen, boolean armor, boolean damagesArmor,
                           double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, temp, tmpElement, weakpoint, frozen, armor, damagesArmor, 0,null,
				0, 0, 0, 0, false, 0, 0, 0, ab, ff, baselineConversions);
	}

	public DamageComponent(double dmg, DamageElement dmgElement, double temp, TemperatureElement tmpElement,
                           boolean weakpoint, boolean frozen, boolean armor, boolean damagesArmor,
                           double rdlDmg, DamageElement rdlDmgElement, double radTemp, double mdr, double dmgRd, double fal,
                           boolean stunOnlyWeakpoint, double stunChnc, double stunDur, double fear, double ab, double ff,
                           DamageConversion[] baselineConversions) {
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

		stunOnWeakpointOnly = stunOnlyWeakpoint;
		stunChance = stunChnc;
		stunDuration = stunDur;
		baseFearChance = fear;
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
	public void setWeakpointBonus(double in) {
		weakpointBonus = in;
	}
	public void setNumBlowthroughs(int in) {
		numBlowthroughs = in;
	}
	public void setNumPelletsPerShot(int in) {
		numHitscanTracersPerShot = in;
	}
	public void setRadialDamage(double in) {
		radialDamage = in;
	}
	public void setDamageRadius(double in) {
		damageRadius = in;
	}
	public void setStunChance(double in) {
		stunChance = in;
	}
	public void setStunDuration(double in) {
		stunDuration = in;
	}
	public void setBaseFearChance(double in) {
		baseFearChance = in;
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
	public void setBonusDamage(DamageElement bnsDmgElement, double bonus) {
		bonusDmgElement = bnsDmgElement;
		bonusDamage = bonus;
	}

	public void setStatusEffectsApplied(PushSTEComponent[] stes) {
		statusEffectsApplied = new MultipleSTEs(stes);
	}

	// TODO: add a toString method that nicely formats this stuff
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

	// TODO: implement as many of the Utility calculations here as I can
	// TODO: this is probably where I can implement the buggy Radial Damage insanity?
	public double probabilityToBreakArmorStrengthPlate(double armorStrength) {
		return 0;
	}
}
