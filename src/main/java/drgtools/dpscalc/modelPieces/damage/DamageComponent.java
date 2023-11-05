package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.DamageFlag;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.RicochetFlag;
import drgtools.dpscalc.enemies.ElementalResistancesMap;
import drgtools.dpscalc.modelPieces.statusEffects.PushSTEComponent;
import drgtools.dpscalc.utilities.MathUtils;

import java.util.ArrayList;
import java.util.EnumMap;

public class DamageComponent {
	protected double damage = 0;
	protected DamageElement baseDamageElement;
	protected EnumMap<DamageElement, Double> damageElements;

	// The five DamageFlags
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
	protected double ricochetMaxRange = 0;
	
	protected double radialDamage = 0;
	protected DamageElement baseRadialDamageElement;
	protected EnumMap<DamageElement, Double> radialDamageElements;
	protected double maxDmgRadius;
	protected double damageRadius;
	protected double falloff;

	protected boolean stunOnWeakpointOnly;
	protected double stunChance = 0;
	protected double stunDuration = 0;
	protected double baseFearChance = 0;
	protected double armorBreaking = 1.0;
	protected double friendlyFire = 1.0;

	protected ArrayList<PushSTEComponent> statusEffectsApplied;
	protected ArrayList<ConditionalDamageConversion> conditionalDamageConversions;

	// Shortcut constructor for what is referred to as "Direct Damage"
	public DamageComponent(double dmg, DamageElement dmgElement, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, true, true, true, true, false,
				0, null, 0, 0, 0,
				false, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for damage-only Direct+Radial, like Autocannon or PGL
	public DamageComponent(double dmg, DamageElement dmgElement, double rdlDmg, DamageElement rdlDmgElement, double mdr,
                           double dmgRd, double fal, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, true, true, true, true, false,
				rdlDmg, rdlDmgElement, mdr, dmgRd, fal,
				false, 0, 0, 0, ab, ff, baselineConversions);
	}

	// Shortcut constructor for any DamageComponent that neither is "Direct Damage" nor uses RadialDamage
	public DamageComponent(double dmg, DamageElement dmgElement, boolean weakpoint, boolean frozen, boolean armor,
						   boolean damagesArmor, boolean embDet, double ab, double ff, DamageConversion[] baselineConversions) {
		this(dmg, dmgElement, weakpoint, frozen, armor, damagesArmor, embDet,
				0, null, 0, 0, 0,
				false, 0, 0, 0, ab, ff, baselineConversions);
	}

	public DamageComponent(double dmg, DamageElement dmgElement, boolean weakpoint, boolean frozen, boolean armor,
						   boolean damagesArmor, boolean embDet, double rdlDmg, DamageElement rdlDmgElement, double mdr,
						   double dmgRd, double fal, boolean stunOnlyWeakpoint, double stunChnc, double stunDur,
						   double fear, double ab, double ff, DamageConversion[] baselineConversions) {
		damage = dmg;
		baseDamageElement = dmgElement;
		damageElements = new EnumMap<>(DamageElement.class);
		if (baseDamageElement != null) {
			damageElements.put(baseDamageElement, 1.0);
		}

		benefitsFromWeakpoint = weakpoint;
		benefitsFromFrozen = frozen;
		reducedByArmor = armor;
		canDamageArmor = damagesArmor;
		embeddedDetonator = embDet;

		radialDamage = rdlDmg;
		baseRadialDamageElement = rdlDmgElement;
		radialDamageElements = new EnumMap<>(DamageElement.class);
		if (baseRadialDamageElement != null) {
			radialDamageElements.put(baseRadialDamageElement, 1.0);
		}
		maxDmgRadius = mdr;
		damageRadius = dmgRd;
		falloff = fal;

		stunOnWeakpointOnly = stunOnlyWeakpoint;
		stunChance = stunChnc;
		stunDuration = stunDur;
		baseFearChance = fear;
		armorBreaking = ab;
		friendlyFire = ff;
		
		if (baselineConversions != null && baselineConversions.length > 0) {
			for (int i = 0; i < baselineConversions.length; i++) {
				applyDamageConversion(baselineConversions[i]);
			}
		}

		statusEffectsApplied = new ArrayList<>();
		conditionalDamageConversions = new ArrayList<>();
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
	public void setRicochet(double chance, RicochetFlag condition, double distance) {
		ricochetChance = chance;
		ricochetMaterialFlag = condition;
		ricochetMaxRange = distance;
	}
	// TODO: these two methods might be able to be deleted?
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

	/*
		So, this is the source of the "Damage Conversion Bug". I'm betting that when a developer at GSG first implemented
		elemental conversions, they just took a shortcut and assumed that all of the conversions would be 50% converted,
		with no additions, and only one conversion would be applied ever. The logic currently implemented supports that
		model. However, as the game has evolved, now there are Conversions that add instead of converting, AND there are
		situations like the EPC's Charged Shots that can have 3 or more conversions applied.

		Simply put, the (buggy) logic currently used in the game is "if this converts damage, subtract that percentage
		from the base element but don't allow it to go below zero". as such, if there are multiple elements that convert
		and altogether add up to greater than 100%, then there will be 0% left of the base element and the
		DamageComponent can do >100% of the listed damage. PGL Hyper Propellent, BC Inferno, and EPC Burning Nightmare
		are the top examples of this bug in action.

		Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
	*/
	public void applyDamageConversion(DamageConversion dc) {
		DamageElement conv = dc.getConvertedElement();
		if (damageElements.containsKey(conv)) {
			damageElements.put(conv, damageElements.get(conv) + dc.getPercentage());
		}
		else {
			damageElements.put(conv, dc.getPercentage());
		}

		if (radialDamageElements.containsKey(conv)) {
			radialDamageElements.put(conv, radialDamageElements.get(conv) + dc.getPercentage());
		}
		else {
			radialDamageElements.put(conv, dc.getPercentage());
		}

		if (dc.convertsInsteadOfAdds()) {
			if (baseDamageElement != null) {
				damageElements.put(baseDamageElement, Math.max(damageElements.get(baseDamageElement) - dc.getPercentage(), 0));
			}
			if (baseRadialDamageElement != null) {
				radialDamageElements.put(baseRadialDamageElement, Math.max(radialDamageElements.get(baseRadialDamageElement) - dc.getPercentage(), 0));
			}
		}
	}

	/*
		SplitSentro documented this bug: https://drg.pleasefix.gg/projects/DEEP-ROCK-GALACTIC/issues/DRG-747

		As I was reading through it, I took away this understanding:
		1. Homebrew Powder functions LIKE a DamageConversion
		2. HBP gets applied before any other Mod/OC DamageConversions
		3. HBP makes its random roll, then multiplies both the BaseDamageElement and BaseRadialDamageElement by that value.
		4. After HBP is applied, then the other DamageConversions get applied later which subtract from the Base Elements

		In order to model that understanding, I'm making this method to be called before the Weapons use the applyDamageConversion() above.
		I don't know how this would interact with BaselineConversions, but AFAIK there aren't any. The onus is on the Weapon files to
		call this method first, before doing the other conversions. (Theoretically, this only matters to PGL)
	*/
	public void applyHomebrewDamage(double minRoll, double maxRoll) {
		double averageDamage = (minRoll + maxRoll) / 2.0;
		if (damage > 0) {
			damageElements.put(baseDamageElement, damageElements.get(baseDamageElement) * averageDamage);
		}
		if (radialDamage > 0) {
			radialDamageElements.put(baseRadialDamageElement, radialDamageElements.get(baseRadialDamageElement) * averageDamage);
		}
	}

	// Used by SMG OC "EMRB"
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

	public void addConditionalDamageConversion(ConditionalDamageConversion cdc) {
		conditionalDamageConversions.add(cdc);
	}

	private double calculateFalloffAtDistance(double r) {
		if (radialDamage > 0) {
			// Early exit conditions
			if (r <= maxDmgRadius) {
				return 1.0;
			}
			else if (r > damageRadius) {
				return -1;
			}

			return ((1.0 - falloff) * (damageRadius - r)) / (damageRadius - maxDmgRadius) + falloff;
		}
		else {
			return -1;
		}
	}

	public double getRawDamage() {
		if (damage > 0) {
			return damage * MathUtils.sumDamage(damageElements) + flatDamage;
		}
		else {
			return 0;
		}
	}
	public double getRawRadialDamage() {
		if (radialDamage > 0) {
			return radialDamage * MathUtils.sumDamage(radialDamageElements);
		}
		else {
			return 0;
		}
	}
	public double getRawRadialDamageAtRadius(double distanceFromCenter) {
		if (radialDamage > 0) {
			// Early exit conditions
			if (distanceFromCenter <= maxDmgRadius) {
				return radialDamage * MathUtils.sumDamage(radialDamageElements);
			}
			else if (distanceFromCenter > damageRadius) {
				return 0;
			}

			// In order to preserve the radialDamageElements' proportions, make a deep copy that can be edited
			EnumMap<DamageElement, Double> rdlDmgElementsCopy = new EnumMap<>(DamageElement.class);
			for (DamageElement el: radialDamageElements.keySet()) {
				rdlDmgElementsCopy.put(el, radialDamageElements.get(el));
			}
			// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
			// Radial Damage Falloff only applies to the BASE element, and not to any converted elements
			double falloffAtR = calculateFalloffAtDistance(distanceFromCenter);
			rdlDmgElementsCopy.put(baseRadialDamageElement, falloffAtR * rdlDmgElementsCopy.get(baseRadialDamageElement));

			return radialDamage * MathUtils.sumDamage(rdlDmgElementsCopy);
		}
		else {
			return -1;
		}
	}
	
	public double getResistedDamage(ElementalResistancesMap creatureResistances) {
		double fltDamage = flatDamage * creatureResistances.getResistance(flatDamageElement);
		return damage * creatureResistances.multiplyDamageByElements(damageElements) + fltDamage;
	}
	public double getResistedRadialDamage(ElementalResistancesMap creatureResistances) {
		// TODO: the other half of where I could implement Radial-type Resistance if wanted. (Hiveguard, Caretaker)
		return radialDamage * creatureResistances.multiplyDamageByElements(radialDamageElements);
	}
	public double getResistedRadialDamageAtRadius(ElementalResistancesMap creatureResistances, double distanceFromCenter) {
		if (radialDamage > 0) {
			// Early exit conditions
			if (distanceFromCenter <= maxDmgRadius) {
				return radialDamage * creatureResistances.multiplyDamageByElements(radialDamageElements);
			}
			else if (distanceFromCenter > damageRadius) {
				return 0;
			}

			// In order to preserve the radialDamageElements' proportions, make a deep copy that can be edited
			EnumMap<DamageElement, Double> rdlDmgElementsCopy = new EnumMap<>(DamageElement.class);
			for (DamageElement el: radialDamageElements.keySet()) {
				rdlDmgElementsCopy.put(el, radialDamageElements.get(el));
			}
			// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
			// Radial Damage Falloff only applies to the BASE element, and not to any converted elements
			double falloffAtR = calculateFalloffAtDistance(distanceFromCenter);
			rdlDmgElementsCopy.put(baseRadialDamageElement, falloffAtR * rdlDmgElementsCopy.get(baseRadialDamageElement));

			return radialDamage * creatureResistances.multiplyDamageByElements(rdlDmgElementsCopy);
		}
		else {
			return -1;
		}
	}

	public double getTotalComplicatedDamageDealtPerHit(Enemy target, MaterialFlag targetMaterial) {
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

		EnumMap<DamageElement, Double> damageElementsCopy = null, radialDamageElementsCopy = null;
		if (conditionalDamageConversions.size() > 0) {
			// Make a deep copy of the current damageElements maps, in case they get changed by the ConditionalDamageConversions
			damageElementsCopy = new EnumMap<>(DamageElement.class);
			for (DamageElement el: damageElements.keySet()) {
				damageElementsCopy.put(el, damageElements.get(el));
			}

			radialDamageElementsCopy = new EnumMap<>(DamageElement.class);
			for (DamageElement el: radialDamageElements.keySet()) {
				radialDamageElementsCopy.put(el, radialDamageElements.get(el));
			}

			// Check if the ConditionalDamageConversions can be applied
			for (ConditionalDamageConversion cdc: conditionalDamageConversions) {
				if (cdc.shouldApplyConversion(target)) {
					applyDamageConversion(cdc.getDamageConversion());
				}
			}
		}

		double dmg = getResistedDamage(target.getElementalResistances());
		double rdlDmg = getResistedRadialDamage(target.getElementalResistances());

		switch (targetMaterial) {
			case weakpoint: {
				if (benefitsFromWeakpoint) {
					dmg *= (1.0 + weakpointBonus) * target.getWeakpointMultiplier();
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
					dmg *= target.getArmorStrengthReduction();
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
		if (target.currentlyAffectedByIFG()) {
			totalDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}

		/*
			If any ConditionalDamageConversions were evaluated, it would break the baseline damageElement maps.
			Revert the changes by updating the class variable to point to the deep copies made at the start of this method.
			This is done to make calling this method safe to do over and over, without having to rebuild the whole DamageComponent
			from scratch each time.
		*/
		if (conditionalDamageConversions.size() > 0) {
			damageElements = damageElementsCopy;
			radialDamageElements = radialDamageElementsCopy;
		}

		// So, this number is the total amount of health that this DamageComponent will attempt to subtract from the Creature's health.
		// Before that happens, it still needs to get reduced by the Difficulty Scaling Resistance, and potentially DamageResistance after that.
		// The final product gets added to the Creature's Damage value, and when Damage >= Health it dies.
		return totalDamage;
	}

	public boolean appliesTemperature(DamageElement desiredTemp) {
		if (desiredTemp == DamageElement.heat) {
			return (
				damageElements.getOrDefault(DamageElement.fireAndHeat, 0.0) > 0 ||
				damageElements.getOrDefault(DamageElement.heat, 0.0) > 0 ||
				radialDamageElements.getOrDefault(DamageElement.fireAndHeat, 0.0) > 0 ||
				radialDamageElements.getOrDefault(DamageElement.heat, 0.0) > 0
			);
		}
		else if (desiredTemp == DamageElement.cold) {
			return (
				damageElements.getOrDefault(DamageElement.frostAndCold, 0.0) > 0 ||
				damageElements.getOrDefault(DamageElement.cold, 0.0) > 0 ||
				radialDamageElements.getOrDefault(DamageElement.frostAndCold, 0.0) > 0 ||
				radialDamageElements.getOrDefault(DamageElement.cold, 0.0) > 0
			);
		}
		else {
			return false;
		}
	}
	public double getTemperatureDealtPerDirectHit(DamageElement desiredTemp) {
		if (appliesTemperature(desiredTemp)) {
			if (desiredTemp == DamageElement.heat) {
				double heat = damage * (damageElements.getOrDefault(DamageElement.fireAndHeat, 0.0) + damageElements.getOrDefault(DamageElement.heat, 0.0));
				double radialHeat = radialDamage * (radialDamageElements.getOrDefault(DamageElement.fireAndHeat, 0.0) + radialDamageElements.getOrDefault(DamageElement.heat, 0.0));
				return heat + radialHeat;
			}
			else if (desiredTemp == DamageElement.cold) {
				double cold = damage * (damageElements.getOrDefault(DamageElement.frostAndCold, 0.0) + damageElements.getOrDefault(DamageElement.cold, 0.0));
				double radialCold = radialDamage * (radialDamageElements.getOrDefault(DamageElement.frostAndCold, 0.0) + radialDamageElements.getOrDefault(DamageElement.cold, 0.0));
				return cold + radialCold;
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	// TODO: do I also need a RadialTemperatureAtRadius method?

	public boolean armorBreakingIsGreaterThan100Percent() {
		return armorBreaking > 1.0;
	}
	public double getArmorDamageOnDirectHit() {
		if (damage > 0 && canDamageArmor && !embeddedDetonator) {
			return damage * MathUtils.sumDamage(damageElements) * armorBreaking;
		}
		else {
			return 0;
		}
	}
	public double getRadialArmorDamageOnDirectHit() {
		if (damage == 0 && radialDamage > 0 ) {
			return radialDamage * MathUtils.sumDamage(radialDamageElements) * armorBreaking;
		}
		else if (damage > 0 && canDamageArmor && !embeddedDetonator && radialDamage > 0) {
			// Key phrase: "modeling a bug" (for Ctrl + Shift + F finding later)
			// Bug where Radial Damage only does 25% AB on direct hit when Damage > 0, 100% AB at 0.5m from direct hit, and then 25% everywhere else til it reaches Radius.
			return 0.25 * radialDamage * MathUtils.sumDamage(radialDamageElements) * armorBreaking;
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
	public double getRadialArmorDamageAtRadius(double distanceFromCenter) {
		if (radialDamage > 0) {
			// According to GSG, Radial Armor Damage getting a 25% penalty is "not a bug". Sadness.
			if (distanceFromCenter <= maxDmgRadius) {
				return 0.25 * radialDamage * MathUtils.sumDamage(radialDamageElements) * armorBreaking;
			}
			else if (distanceFromCenter > damageRadius) {
				return 0;
			}
			else {
				return 0.25 * getRawRadialDamageAtRadius(distanceFromCenter) * armorBreaking;
			}
		}
		else {
			return 0;
		}
	}

	public String prettyPrint(){
		return prettyPrint(0);
	}
	public String prettyPrint(int indentLevel) {
		String indent = "    ";
		String toReturn = "";

		if (damage > 0) {
			toReturn += indent.repeat(indentLevel) + "Does " + damage + " Damage (originally " + DamageElements.prettyPrint(baseDamageElement) + ")\n";
			toReturn += indent.repeat(indentLevel) + "Damage Flags:\n";
			toReturn += indent.repeat(indentLevel+1) + "Benefits from Weakpoint: " + benefitsFromWeakpoint + "\n";
			toReturn += indent.repeat(indentLevel+1) + "Benefits from Frozen: " + benefitsFromFrozen + "\n";
			toReturn += indent.repeat(indentLevel+1) + "Gets reduced by Armor: " + reducedByArmor + "\n";
			toReturn += indent.repeat(indentLevel+1) + "Can damage Armor: " + canDamageArmor + "\n";
			toReturn += indent.repeat(indentLevel+1) + "Embedded Detonator: " + embeddedDetonator + "\n";

			if (weakpointBonus > 0) {
				toReturn += indent.repeat(indentLevel) + "With a +" + weakpointBonus * 100.0 + "% Weakpoint Bonus\n";
			}

			// If any DamageConversions have been applied, this will evaluate as true.
			if (damageElements.keySet().size() > 1) {
				toReturn += indent.repeat(indentLevel) + "After applying all DamageConversions:\n";
				for (DamageElement el: damageElements.keySet()) {
					toReturn += indent.repeat(indentLevel+1) + damage * damageElements.get(el) + " " + DamageElements.prettyPrint(el) + " Damage\n";
				}
			}
		}

		if (flatDamage > 0 && flatDamageElement != null) {
			toReturn += indent.repeat(indentLevel) + "Does " + flatDamage + " " + DamageElements.prettyPrint(flatDamageElement) + " FlatBonusDamage";
		}

		if (radialDamage > 0) {
			toReturn += indent.repeat(indentLevel) + "Does " + radialDamage + " RadialDamage (originally " + DamageElements.prettyPrint(baseRadialDamageElement) + ")\n";
			toReturn += indent.repeat(indentLevel+1) + "Max Damage Radius: " + maxDmgRadius + "m\n";
			toReturn += indent.repeat(indentLevel+1) + "Damage Radius: " + damageRadius + "m\n";
			toReturn += indent.repeat(indentLevel+1) + "Falloff at Outer Edge: " + falloff * 100.0 + "%\n";

			// If any DamageConversions have been applied, this will evaluate as true.
			if (radialDamageElements.keySet().size() > 1) {
				toReturn += indent.repeat(indentLevel) + "After applying all DamageConversions:\n";
				for (DamageElement el: radialDamageElements.keySet()) {
					toReturn += indent.repeat(indentLevel+1) + radialDamage * radialDamageElements.get(el) + " " + DamageElements.prettyPrint(el) + " RadialDamage\n";
				}
			}
		}

		toReturn += indent.repeat(indentLevel) + "Has " + friendlyFire * 100.0 + "% Friendly Fire modifier\n";
		toReturn += indent.repeat(indentLevel) + "Has " + armorBreaking * 100.0 + "% Armor Breaking\n";

		if (stunChance > 0) {
			if (stunOnWeakpointOnly) {
				toReturn += indent.repeat(indentLevel) + "Has a " + stunChance * 100.0 + "% chance to Stun enemies when hitting Weakpoints for " + stunDuration + " seconds\n";
			}
			else {
				toReturn += indent.repeat(indentLevel) + "Has a " + stunChance * 100.0 + "% chance to Stun enemies for " + stunDuration + " seconds\n";
			}
		}

		if (baseFearChance > 0) {
			toReturn += indent.repeat(indentLevel) + "Has a " + baseFearChance * 100.0 + "% Base Fear Chance\n";
		}

		if (statusEffectsApplied.size() > 0) {
			toReturn += indent.repeat(indentLevel) + "Can apply the following Status Effects:\n";
			for (PushSTEComponent pstec: statusEffectsApplied) {
				pstec.prettyPrint(indentLevel + 1);
			}
		}

		if (conditionalDamageConversions.size() > 0) {
			toReturn += indent.repeat(indentLevel) + "Has these ConditionalDamageConversions:\n";
			for (ConditionalDamageConversion cdc: conditionalDamageConversions) {
				cdc.prettyPrint(indentLevel + 1);
			}
		}

		if (numBlowthroughs > 0) {
			toReturn += indent.repeat(indentLevel) + "Can blowthrough this many enemies: " + numBlowthroughs + "\n";
		}

		if (ricochetChance > 0 && ricochetMaterialFlag != null) {
			toReturn += indent.repeat(indentLevel) + "Has a " + ricochetChance * 100.0 + "% chance to Ricochet off of " +
					DamageFlags.prettyPrint(ricochetMaterialFlag) + " into an enemy less than " + ricochetMaxRange + "m away\n";
		}

		return toReturn;
	}
}
