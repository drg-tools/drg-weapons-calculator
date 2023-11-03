package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.DamageFlag;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.RicochetFlag;
import drgtools.dpscalc.enemies.ElementalResistancesArray;
import drgtools.dpscalc.modelPieces.statusEffects.PushSTEComponent;
import drgtools.dpscalc.utilities.MathUtils;

import java.util.ArrayList;

/*
	TODO List:
	move the "calculate num enemies hit" into this object
	consider moving the "calculate total damage dealt per hit" into this object?
	add a toString method that nicely formats this stuff
	this might need to have a "DamageInstance" class made which stores 2+ DamageComponents together, that get applied on each hit?
	implement as many of the Utility calculations here as I can
*/

public class DamageComponent {
	protected double damage = 0;
	protected double temperature = 0;

	// The four DamageFlags
	protected boolean benefitsFromWeakpoint;
	protected boolean benefitsFromFrozen;
	protected boolean reducedByArmor;
	protected boolean canDamageArmor;
	protected boolean embeddedDetonator;

	protected double weakpointBonus = 0;
	protected double flatDamage = 0;
	protected DamageElement flatDamageElement = null;

	protected int numBlowthroughs = 0;
	protected RicochetFlag ricochetMaterialFlag;
	protected double ricochetChance = 0;  // [0, 1]
	protected double richochetMaxRange = 0;
	
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
	protected ArrayList<PushSTEComponent> statusEffectsApplied;

	// Shortcut constructor for what is referred to as "Direct Damage"
	public DamageComponent(double dmg, DamageElement dmgElement, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, 0, null, true, true, true, true, false,
				0, null, 0, 0, 0, 0, false, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for damage-only Direct+Radial, like Autocannon or PGL
	public DamageComponent(double dmg, DamageElement dmgElement, double rdlDmg, DamageElement rdlDmgElement, double mdr,
                           double dmgRd, double fal, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, 0, null, true, true, true, true, false, rdlDmg,
				rdlDmgElement, 0, mdr, dmgRd, fal, false, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for any DamageComponent that neither is "Direct Damage" nor uses RadialDamage
	public DamageComponent(double dmg, DamageElement dmgElement, double temp, TemperatureElement tmpElement,
                           boolean weakpoint, boolean frozen, boolean armor, boolean damagesArmor, boolean embDet,
                           double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, temp, tmpElement, weakpoint, frozen, armor, damagesArmor, embDet, 0, null,
				0, 0, 0, 0, false, 0, 0, 0, ab, ff, baselineConversions);
	}

	public DamageComponent(double dmg, DamageElement dmgElement, double temp, TemperatureElement tmpElement,
                           boolean weakpoint, boolean frozen, boolean armor, boolean damagesArmor, boolean embDet,
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
		embeddedDetonator = embDet;

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

		statusEffectsApplied = new ArrayList<>();
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
	public void setRadialDamage(double in) {
		radialDamage = in;
	}
	public void setDamageRadius(double in) {
		damageRadius = in;
	}
	public void setStun(boolean onWeakpointOnly, double chance, double duration) {
		stunOnWeakpointOnly = onWeakpointOnly;
		stunChance = chance;
		stunDuration = duration;
	}
	public void setBaseFearChance(double in) {
		baseFearChance = in;
	}
	public void setArmorBreaking(double in) {
		armorBreaking = in;
	}
	// For Zhukovs OC "Gas Recycling" in particular. AFAIK that's the only upgrade in-game that changes one of the "damage flags" like this.
	public void setDamageFlag(DamageFlag flag, boolean value) {
		switch (flag) {
			case benefitsFromWeakpoint: {
				benefitsFromWeakpoint = value;
				break;
			}
			case benefitsFromFrozen: {
				benefitsFromFrozen = value;
				break;
			}
			case reducedByArmor: {
				reducedByArmor = value;
				break;
			}
			case canDamageArmor: {
				canDamageArmor = value;
				break;
			}
			case embeddedDetonator: {
				embeddedDetonator = value;
				break;
			}
			default: {
				break;
			}
		}
	}
	public boolean getDamageFlag(DamageFlag flag) {
		switch (flag) {
			case benefitsFromWeakpoint: {
				return benefitsFromWeakpoint;
			}
			case benefitsFromFrozen: {
				return benefitsFromFrozen;
			}
			case reducedByArmor: {
				return reducedByArmor;
			}
			case canDamageArmor: {
				return canDamageArmor;
			}
			case embeddedDetonator: {
				return embeddedDetonator;
			}
			default: {
				return false;
			}
		}
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
			// Theoretically, the only upgrades that do conversion have 0 heat as base. that makes it safe to do it this way, i think?
			temperature += dc.getPercentage() * damage;
			radialTemperature += dc.getPercentage() * radialDamage;
		}

		// Only subtract once, in case it converts to (Fire-element + Heat) or (Frost-element + Cold).
		if (dc.convertsInsteadOfAdds()) {
			// This is an implicit check for "has the starting element for damage been defined?", because this value will be -1 if the initial value was set to Null.
			if (startingDamageElementIndex > -1) {
				// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
				damageElements[startingDamageElementIndex] = Math.max(damageElements[startingDamageElementIndex] - dc.getPercentage(), 0);
			}
			if (startingRadialDamageElementIndex > -1) {
				// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
				radialDamageElements[startingRadialDamageElementIndex] = Math.max(radialDamageElements[startingRadialDamageElementIndex] - dc.getPercentage(), 0);
			}
		}
	}

	// Used by Subata T5.B and SMG OC "EMRB"
	public void setFlatDamage(DamageElement flatDmgElement, double dmg) {
		// From my testing, FlatDamageBonus isn't affected by DamageConversions, like SMG T4.B Conductive Bullets, nor does it do damage to ArmorHealth.
		// Thus, it probably doesn't affect ArmorStrength either.
		flatDamageElement = flatDmgElement;
		flatDamage = dmg;
	}

	public void addStatusEffectApplied(PushSTEComponent pste) {
		statusEffectsApplied.add(pste);
	}
	public ArrayList<PushSTEComponent> getStatusEffectsApplied() {
		return statusEffectsApplied;
	}

	public double getRawDamage() {
		if (damage > 0) {
			return MathUtils.sum(damageElements) * damage + flatDamage;
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
		double fltDamage = flatDamage * creatureResistances.getResistance(flatDamageElement);
		return damage * MathUtils.vectorDotProduct(damageElements, creatureResistances.getResistances()) + fltDamage;
	}
	public double getResistedRadialDamage(ElementalResistancesArray creatureResistances) {
		// TODO: the other half of where I could implement Radial-type Resistance if wanted. (Hiveguard, Caretaker)
		return radialDamage * MathUtils.vectorDotProduct(radialDamageElements, creatureResistances.getResistances());
	}

	public double getTotalComplicatedDamageDealtPerHit(MaterialFlag targetMaterial, ElementalResistancesArray creatureResistances,
													   boolean IFG, double weakpointMultiplier, double lightArmorReduction) {
		/*
			To the best of my current understanding, this is the order of operations when evaluating how much health gets removed from an enemy when they get damaged by a player's weapon:
			1. The base damage is fetched and has its element set.
			2. Baseline elemental conversions get applied
			3. All mods that add or multiply the base damage get applied
			4. Any elemental conversions from mods get applied
			5. Overclock that adds or multiplies the base damage get applied
			6. Any elemental conversions from Overclock get applied
			7. The damage gets affected by the target creature's elemental resistances (if any)
			8. Weakpoint, Armor, or Frozen have their respective effects on the damage
			9. IFG multiplies the base damage
			10. Damage gets divided by the Difficulty Scaling Resistance
			11. Damage gets multiplied by PST_DamageResistance (seen on Elite enemies, mostly, but also the new Rockpox)
			12. that final number gets added to the creature's Damage tally. (equivalent to subtracting from their True HP value). once the Damage >= True HP, the creature dies.
		*/
		double dmg = getResistedDamage(creatureResistances);
		double rdlDmg = getResistedRadialDamage(creatureResistances);

		switch (targetMaterial) {
			case weakpoint: {
				if (benefitsFromWeakpoint) {
					dmg *= (1.0 + weakpointBonus) * weakpointMultiplier;
				}
				break;
			}
			case frozen: {
				if (benefitsFromFrozen) {
					// TODO: if I ever wanted to model Dreadnoughts using a different Frozen multiplier, here's where I could do it
					dmg *= UtilityInformation.Frozen_Damage_Multiplier;
				}
				break;
			}
			case lightArmor: {
				if (reducedByArmor) {
					dmg *= lightArmorReduction;
				}
			}
			case heavyArmor: {
				if (reducedByArmor || embeddedDetonator) {
					dmg *= 0;
				}
			}
			default: {
				// Includes normalFlesh; don't need to do anything special
				break;
			}
		}

		double totalDamage = dmg + rdlDmg;
		if (IFG) {
			totalDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}

		// So, this number is the total amount of health that this DamageComponent will attempt to subtract from the Creature's health.
		// Before that happens, it still needs to get reduced by the Difficulty Scaling Resistance, and potentially DamageResistance after that.
		// The final product gets added to the Creature's Damage value, and when Damage >= Health it dies.
		return totalDamage;
	}

	public boolean appliesTemperature(TemperatureElement desiredTemp) {
		return tempElement == desiredTemp;
	}
	public double getTemperatureDealtPerHit(TemperatureElement desiredTemp) {
		if (appliesTemperature(desiredTemp)) {
			return temperature + radialTemperature;
		}
		else {
			return 0;
		}
	}

	public boolean armorBreakingIsGreaterThan100Percent() {
		return armorBreaking > 1.0;
	}
	public double getArmorDamageOnDirectHit() {
		if (damage > 0 && canDamageArmor && !embeddedDetonator) {
			return damage * MathUtils.sum(damageElements) * armorBreaking;
		}
		else {
			return 0;
		}
	}
	public double getRadialArmorDamageOnDirectHit() {
		if (damage == 0 && radialDamage > 0 ) {
			return radialDamage * MathUtils.sum(radialDamageElements) * armorBreaking;
		}
		else if (damage > 0 && canDamageArmor && !embeddedDetonator && radialDamage > 0) {
			// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
			// Bug where Radial Damage only does 25% AB on direct hit when Damage > 0, 100% AB at 0.5m from direct hit, and then 25% everywhere else til it reaches Radius.
			return 0.25 * radialDamage * MathUtils.sum(radialDamageElements) * armorBreaking;
		}
		else {
			return 0;
		}
	}
	public double getTotalArmorDamageOnDirectHit() {
		// After discussions with Dagadegatto in Nov 2022, this is being modeled as if Elemental Resistances are not inherited by Armor Plates anymore.
		// Testing shows that FlatDamageBonus doesn't do damage to Armor, just like it doesn't benefit from DamageConversions.
		// Testing shows that IFG doesn't affect damage dealt to ArmorHealth plates.
		return getArmorDamageOnDirectHit() + getRadialArmorDamageOnDirectHit();
	}
	public double getRadialArmorDamageAtRadius(double distanceFromDirectHit) {
		if (radialDamage > 0) {
			// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
			double baseRadialArmorDamage = radialDamage * MathUtils.sum(radialDamageElements) * armorBreaking;
			if (distanceFromDirectHit <= 0.5) {
				return baseRadialArmorDamage;
			}
			else if (distanceFromDirectHit > damageRadius) {
				return 0;
			}
			else {
				// TODO: add radial falloff
				return 0.25 * baseRadialArmorDamage;
			}

			/*
			if (distanceFromDirectHit <= maxDmgRadius) {
				return baseRadialArmorDamage;
			}
			else if (distanceFromDirectHit > damageRadius) {
				return 0;
			}
			else {
				double falloffMultiplier = (1.0 - falloff) * (damageRadius - distanceFromDirectHit) / (damageRadius - maxDmgRadius) + falloff;
				return baseRadialArmorDamage * falloffMultiplier;
			}
			*/
		}
		else {
			return 0;
		}
	}
}
